<template>
  <div class="page-container">
    <a-page-header title="商户管理" sub-title="维护食堂窗口和商户资料" @back="() => $router.back()" />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search v-model:value="keyword" placeholder="搜索商户名称、联系人或电话" allow-clear @search="refreshList" />
        </a-col>
        <a-col :xs="24" :sm="12" :md="4">
          <a-select v-model:value="statusFilter" placeholder="状态筛选" allow-clear @change="refreshList" style="width: 100%">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="4">
          <a-select v-model:value="auditStatusFilter" placeholder="审核状态" allow-clear @change="refreshList" style="width: 100%">
            <a-select-option value="pending">待审核</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="rejected">已驳回</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="10">
          <a-space>
            <a-button type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              新增商户
            </a-button>
            <a-button @click="handleBatchDelete" danger :disabled="!selectedRowKeys.length">
              <template #icon><DeleteOutlined /></template>
              批量删除
            </a-button>
            <a-button @click="refreshList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :xs="12" :sm="6">
          <a-statistic title="总商户数" :value="statistics.totalMerchants" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="启用中" :value="statistics.activeMerchants" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="待审核" :value="statistics.pendingMerchants" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已驳回" :value="statistics.rejectedMerchants" />
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: MerchantProfile) => row.id as number"
        @change="handleTableChange"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'avatar'">
            <a-image
              v-if="record.avatar"
              :src="record.avatar"
              :width="60"
              :height="60"
              :preview="true"
              style="object-fit: cover; border-radius: 4px"
            />
            <a-avatar v-else :size="60" style="background-color: #87d068">
              {{ record.merchantName ? record.merchantName.charAt(0).toUpperCase() : 'M' }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'merchantName'">
            <div>
              <div style="font-weight: 500">{{ record.merchantName }}</div>
              <div style="font-size: 12px; color: #999">{{ record.location || '-' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'bindUser'">
            <div class="bind-user-cell">
              <div class="bind-user-header">
                <span class="bind-user-name">{{ getMerchantUserDisplay(record.userId).name }}</span>
                <a-tag
                  v-if="getMerchantUserDisplay(record.userId).statusText"
                  :color="getMerchantUserDisplay(record.userId).statusColor"
                >
                  {{ getMerchantUserDisplay(record.userId).statusText }}
                </a-tag>
              </div>
              <div class="bind-user-meta">{{ getMerchantUserDisplay(record.userId).accountLabel }}</div>
              <div class="bind-user-meta">{{ getMerchantUserDisplay(record.userId).phoneLabel }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'contact'">
            <div class="contact-cell">
              <div class="contact-name">{{ getContactDisplay(record).name }}</div>
              <div class="contact-meta">{{ getContactDisplay(record).phone }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'auditStatus'">
            <a-space direction="vertical" :size="4">
              <a-tag :color="auditColor(record.auditStatus)">{{ auditText(record.auditStatus) }}</a-tag>
              <a-button
                v-if="record.auditStatus === 'pending' && record.pendingData"
                type="link"
                size="small"
                @click="openAudit(record)"
              >
                查看待审核内容
              </a-button>
            </a-space>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEdit(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-popconfirm title="确定删除该商户吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" :title="form.id ? '编辑商户' : '新增商户'" @ok="submit" :confirm-loading="submitting" width="720px">
      <a-form layout="vertical">
        <div class="form-section-title">账号与绑定信息</div>
        <div class="form-section-tip">先确认绑定账号，再补充商户展示信息，录入顺序更符合后台操作习惯。</div>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item
              label="关联商户账号"
              required
              :extra="form.id ? '编辑时可查看全部账号，已绑定或已禁用账号不可误选。' : '新增时默认只显示可直接绑定的商户账号。'"
            >
              <a-space direction="vertical" style="width: 100%" :size="8">
                <a-select
                  v-model:value="form.userId"
                  style="width: 100%"
                  placeholder="请选择商户账号"
                  show-search
                  :filter-option="filterMerchantUserOption"
                  :options="merchantUserOptions"
                  @change="handleMerchantUserChange"
                />
                <a-button type="dashed" block @click="openCreateMerchantAccount">
                  <template #icon><PlusOutlined /></template>
                  新建商户账号
                </a-button>
              </a-space>
            </a-form-item>
            <div v-if="selectedMerchantUser" class="account-help-text">
              已选择：{{ selectedMerchantUser.userName || '未命名用户' }} / {{ selectedMerchantUser.userAccount || '无账号' }}
              <span v-if="selectedMerchantUser.userPhone"> / {{ selectedMerchantUser.userPhone }}</span>
              <span class="account-status-text" :class="selectedMerchantUser.status === 0 ? 'is-danger' : 'is-success'">
                {{ selectedMerchantUser.status === 0 ? '账号已停用' : '账号可用' }}
              </span>
            </div>
            <div v-else class="account-help-text">
              当前可选 {{ availableMerchantUserCount }} 个账号
              <span v-if="form.id">，其余账号会显示为已绑定或已禁用</span>
            </div>

            <div v-if="createdAccount.userAccount" class="created-account-card">
              <div class="created-account-title">刚创建的商户账号</div>
              <div class="created-account-row">
                <span>登录账号：</span>
                <strong>{{ createdAccount.userAccount }}</strong>
                <a-button type="link" size="small" @click="copyText(createdAccount.userAccount, '账号已复制')">
                  <template #icon><CopyOutlined /></template>
                  复制
                </a-button>
              </div>
              <div class="created-account-row">
                <span>初始密码：</span>
                <strong>{{ createdAccount.password }}</strong>
                <a-button type="link" size="small" @click="copyText(createdAccount.password, '密码已复制')">
                  <template #icon><CopyOutlined /></template>
                  复制
                </a-button>
              </div>
              <div class="created-account-tip">请尽快把账号和密码交给商户负责人，并提醒首次登录后修改密码。若该账号尚未生成商户资料，系统会自动绑定本次新增的商户信息。</div>
            </div>

            <div v-if="selectedMerchantSummary" class="bind-preview-card">
              <div class="bind-preview-title">提交前确认</div>
              <div class="bind-preview-row">
                <span>绑定账号：</span>
                <strong>{{ selectedMerchantSummary.name }}</strong>
                <a-tag :color="selectedMerchantSummary.statusColor">{{ selectedMerchantSummary.statusText }}</a-tag>
              </div>
              <div class="bind-preview-row">
                <span>登录账号：</span>
                <span>{{ selectedMerchantSummary.account }}</span>
              </div>
              <div class="bind-preview-row">
                <span>账号手机号：</span>
                <span>{{ selectedMerchantSummary.phone }}</span>
              </div>
              <div class="bind-preview-row">
                <span>商户名称：</span>
                <span>{{ selectedMerchantSummary.merchantName }}</span>
              </div>
              <div class="bind-preview-row">
                <span>联系人：</span>
                <span>{{ selectedMerchantSummary.contactName }}</span>
              </div>
              <div class="bind-preview-row">
                <span>联系电话：</span>
                <span>{{ selectedMerchantSummary.contactPhone }}</span>
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <a-form-item label="商户名称" required extra="商户名称需保持唯一，避免后台列表难以区分。">
              <a-input v-model:value="form.merchantName" placeholder="请输入商户名称" />
            </a-form-item>
          </a-col>
        </a-row>

        <div class="form-section-title" style="margin-top: 4px">商户展示信息</div>
        <div class="form-section-tip">这些内容会直接影响后台识别和前台展示，建议一次性补齐。</div>
        <a-form-item label="商户门头图片" extra="建议上传清晰的门头或档口图片，方便管理员和学生快速识别。">
          <ImageUpload v-model="form.avatar" type="merchant" />
        </a-form-item>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="联系人">
              <a-input v-model:value="form.contactName" placeholder="优先使用商户账号姓名，可手动修改" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="联系电话" extra="请填写 11 位手机号，方便学生联系商户。">
              <a-input v-model:value="form.contactPhone" placeholder="优先使用商户账号手机号，可手动修改" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="营业时间" extra="建议填写学生容易理解的时间范围，如早餐/午餐/晚餐营业时段。">
              <a-input v-model:value="form.businessHours" placeholder="如：07:00-20:00" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="位置" extra="建议写到食堂楼层或区域，方便学生线下寻找。">
              <a-input v-model:value="form.location" placeholder="如：一食堂2楼" />
            </a-form-item>
          </a-col>
        </a-row>

        <div class="form-section-title" style="margin-top: 4px">后台状态设置</div>
        <div class="form-section-tip">仅在确有需要时调整审核和启用状态，避免新建后看不到或误上线。</div>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="审核状态">
              <a-select v-model:value="form.auditStatus">
                <a-select-option value="pending">待审核</a-select-option>
                <a-select-option value="approved">通过</a-select-option>
                <a-select-option value="rejected">驳回</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status">
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">停用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="描述" extra="可填写档口特色、主营餐品或补充说明，方便后台和前台理解商户信息。">
          <a-textarea v-model:value="form.description" :rows="3" placeholder="请输入商户描述" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="accountModalOpen"
      title="新建商户账号"
      @ok="submitMerchantAccount"
      :confirm-loading="accountSubmitting"
      width="560px"
    >
      <a-form layout="vertical">
        <a-form-item label="登录账号" required>
          <a-input v-model:value="accountForm.userAccount" placeholder="请输入登录账号，建议英文或数字组合" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="账号姓名">
              <a-input v-model:value="accountForm.userName" placeholder="请输入商户负责人姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="手机号" required>
              <a-input v-model:value="accountForm.userPhone" placeholder="请输入手机号" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="账号状态">
          <a-select v-model:value="accountForm.status">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-alert
          message="系统会自动创建商户账号，默认初始密码为 12345678。创建后会自动选中该账号。"
          type="info"
          show-icon
        />
      </a-form>
    </a-modal>

    <a-modal v-model:open="auditModalOpen" title="审核商户信息" width="900px" :footer="null">
      <a-alert
        message="商户提交了信息修改申请，请对比审核"
        type="info"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-table :columns="compareColumns" :data-source="compareData" :pagination="false" bordered>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'current'">
            <span v-if="column.dataIndex === 'avatar'">
              <a-image v-if="record.current" :src="record.current" :width="60" />
              <span v-else>-</span>
            </span>
            <span v-else>{{ record.current || '-' }}</span>
          </template>
          <template v-if="column.key === 'pending'">
            <span v-if="column.dataIndex === 'avatar'">
              <a-image v-if="record.pending" :src="record.pending" :width="60" />
              <span v-else>-</span>
            </span>
            <span v-else :style="record.changed ? 'color: #ff4d4f; font-weight: 500' : ''">
              {{ record.pending || '-' }}
            </span>
          </template>
        </template>
      </a-table>

      <div style="margin-top: 16px; text-align: right">
        <a-space>
          <a-button @click="auditModalOpen = false">取消</a-button>
          <a-popconfirm title="确定驳回此次修改申请吗？" @confirm="handleAudit('rejected')">
            <a-button danger :loading="auditing">驳回</a-button>
          </a-popconfirm>
          <a-popconfirm title="确定通过此次修改申请吗？" @confirm="handleAudit('approved')">
            <a-button type="primary" :loading="auditing">通过</a-button>
          </a-popconfirm>
        </a-space>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, DeleteOutlined, ReloadOutlined, EditOutlined, CopyOutlined } from '@ant-design/icons-vue';
import ImageUpload from '../../components/ImageUpload.vue';
import { addUser, deleteMerchant, listMerchants, saveMerchant, updateMerchant, listUserVOByPage, type MerchantProfile } from '../../api';

interface MerchantUserOption {
  id: number;
  userAccount?: string;
  userName?: string;
  userPhone?: string;
  status?: number;
}

const DEFAULT_MERCHANT_PASSWORD = '12345678';

const columns = [
  { title: '门头', key: 'avatar', width: 100, fixed: 'left' },
  { title: '商户名称', key: 'merchantName', width: 200 },
  { title: '关联账号', key: 'bindUser', width: 220 },
  { title: '联系人', key: 'contact', width: 150 },
  { title: '营业时间', dataIndex: 'businessHours', key: 'businessHours', width: 150 },
  { title: '位置', dataIndex: 'location', key: 'location', width: 150 },
  { title: '审核状态', dataIndex: 'auditStatus', key: 'auditStatus', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
];

const compareColumns = [
  { title: '字段', dataIndex: 'field', key: 'field', width: 120 },
  { title: '当前值', dataIndex: 'current', key: 'current' },
  { title: '待审核值', dataIndex: 'pending', key: 'pending' },
];

const loading = ref(false);
const submitting = ref(false);
const auditing = ref(false);
const accountSubmitting = ref(false);
const keyword = ref('');
const statusFilter = ref<number | undefined>(undefined);
const auditStatusFilter = ref<string | undefined>(undefined);
const tableData = ref<MerchantProfile[]>([]);
const modalOpen = ref(false);
const auditModalOpen = ref(false);
const accountModalOpen = ref(false);
const selectedRowKeys = ref<number[]>([]);
const currentAuditRecord = ref<MerchantProfile | null>(null);
const merchantUsers = ref<MerchantUserOption[]>([]);
const compareData = ref<Array<{ field: string; current: any; pending: any; changed: boolean; dataIndex?: string }>>([]);

const statistics = reactive({
  totalMerchants: 0,
  activeMerchants: 0,
  pendingMerchants: 0,
  rejectedMerchants: 0,
});

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['10', '20', '50', '100'],
  showTotal: (total: number) => `共 ${total} 条`,
});

const form = reactive<MerchantProfile>({
  id: undefined,
  userId: undefined,
  merchantName: '',
  contactName: '',
  contactPhone: '',
  avatar: '',
  businessHours: '',
  location: '',
  description: '',
  auditStatus: 'approved',
  status: 1,
});

const accountForm = reactive({
  userAccount: '',
  userName: '',
  userPhone: '',
  status: 1,
});

const createdAccount = reactive({
  userAccount: '',
  password: DEFAULT_MERCHANT_PASSWORD,
});

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys;
  },
}));

const selectedMerchantUser = computed(() => merchantUsers.value.find(user => user.id === form.userId) || null);

const selectedMerchantSummary = computed(() => {
  if (!selectedMerchantUser.value) {
    return null;
  }
  const accountMeta = merchantUserMeta.value.find(user => user.id === selectedMerchantUser.value?.id);
  return {
    name: selectedMerchantUser.value.userName || '未命名用户',
    account: selectedMerchantUser.value.userAccount || '未设置登录账号',
    phone: selectedMerchantUser.value.userPhone || '未登记手机号',
    contactName: form.contactName?.trim() || selectedMerchantUser.value.userName || '未填写联系人',
    contactPhone: form.contactPhone?.trim() || selectedMerchantUser.value.userPhone || '未填写联系电话',
    merchantName: form.merchantName?.trim() || '未填写商户名称',
    statusText: accountMeta?.statusText || (selectedMerchantUser.value.status === 0 ? '已禁用' : '可绑定'),
    statusColor: accountMeta?.statusColor || (selectedMerchantUser.value.status === 0 ? 'red' : 'green')
  };
});

const merchantUserMeta = computed(() => {
  const boundUserIds = new Set(
    tableData.value
      .map(item => item.userId)
      .filter((id): id is number => typeof id === 'number' && id > 0 && id !== form.userId)
  );

  return merchantUsers.value.map(user => {
    const isDisabledAccount = user.status === 0;
    const isBound = boundUserIds.has(user.id);
    const isCurrent = user.id === form.userId;
    const available = !isDisabledAccount && !isBound;
    return {
      ...user,
      isDisabledAccount,
      isBound,
      isCurrent,
      available,
      disabled: !available && !isCurrent,
      statusText: isDisabledAccount ? '已禁用' : isBound ? '已绑定' : '可绑定',
      statusColor: isDisabledAccount ? 'red' : isBound ? 'orange' : 'green',
    };
  });
});

const availableMerchantUserCount = computed(() => merchantUserMeta.value.filter(user => user.available).length);

const merchantUserOptions = computed(() => {
  const users = form.id ? merchantUserMeta.value : merchantUserMeta.value.filter(user => user.available || user.isCurrent);
  return users.map(user => ({
    value: user.id,
    disabled: user.disabled,
    label: `${user.userName || '未命名用户'}（${user.userAccount || '无账号'}${user.userPhone ? ` / ${user.userPhone}` : ''}） - ${user.statusText}`,
  }));
});

const auditText = (status?: string) => {
  if (status === 'approved') return '已通过';
  if (status === 'rejected') return '已驳回';
  return '待审核';
};

const auditColor = (status?: string) => {
  if (status === 'approved') return 'green';
  if (status === 'rejected') return 'red';
  return 'orange';
};

const resetForm = () => {
  form.id = undefined;
  form.userId = undefined;
  form.merchantName = '';
  form.contactName = '';
  form.contactPhone = '';
  form.avatar = '';
  form.businessHours = '';
  form.location = '';
  form.description = '';
  form.auditStatus = 'approved';
  form.status = 1;
  createdAccount.userAccount = '';
  createdAccount.password = DEFAULT_MERCHANT_PASSWORD;
};

const resetAccountForm = () => {
  accountForm.userAccount = '';
  accountForm.userName = '';
  accountForm.userPhone = '';
  accountForm.status = 1;
};

const filterMerchantUserOption = (input: string, option: { label?: string | number }) => {
  return String(option?.label || '').toLowerCase().includes(input.toLowerCase());
};

const MERCHANT_USER_PAGE_SIZE = 100;

const loadMerchantUsers = async () => {
  try {
    let current = 1;
    let total = 0;
    const allRecords: any[] = [];

    do {
      const res = await listUserVOByPage({ current, pageSize: MERCHANT_USER_PAGE_SIZE, userRole: 'merchant' } as any);
      const page = res.data || res;
      const records = page.records || [];
      total = Number(page.total || 0);
      allRecords.push(...records);
      current += 1;
    } while (allRecords.length < total);

    merchantUsers.value = allRecords.map((item: any) => ({
      id: item.id,
      userAccount: item.userAccount,
      userName: item.userName,
      userPhone: item.userPhone,
      status: item.status,
    }));
  } catch (error) {
    message.error('加载商户账号列表失败');
  }
};

const handleMerchantUserChange = (userId: number) => {
  const user = merchantUsers.value.find(item => item.id === userId);
  if (!user) return;
  if (!form.contactName?.trim()) form.contactName = user.userName || '';
  if (!form.contactPhone?.trim()) form.contactPhone = user.userPhone || '';
  if (!form.merchantName?.trim() && user.userName?.trim()) form.merchantName = user.userName.trim();
};

const openCreateMerchantAccount = () => {
  resetAccountForm();
  accountModalOpen.value = true;
};

const validateMerchantAccountForm = () => {
  const account = accountForm.userAccount.trim();
  const phone = accountForm.userPhone.trim();
  if (!account) {
    message.warning('请输入登录账号');
    return false;
  }
  if (!/^[a-zA-Z0-9_]{4,20}$/.test(account)) {
    message.warning('登录账号请填写 4-20 位英文、数字或下划线');
    return false;
  }
  if (!phone) {
    message.warning('请输入手机号');
    return false;
  }
  if (!/^1\d{10}$/.test(phone)) {
    message.warning('请输入正确的 11 位手机号');
    return false;
  }
  return true;
};

const validateMerchantForm = () => {
  const merchantName = form.merchantName?.trim();
  const contactPhone = form.contactPhone?.trim();

  if (!form.userId || !merchantName) {
    message.warning('请选择商户账号并填写商户名称');
    return false;
  }

  const selectedUser = merchantUserMeta.value.find(user => user.id === form.userId);
  if (!selectedUser) {
    message.warning('请选择有效的商户账号');
    return false;
  }
  if (selectedUser.isDisabledAccount) {
    message.warning('当前商户账号已停用，不能绑定');
    return false;
  }
  if (selectedUser.isBound && !selectedUser.isCurrent) {
    message.warning('当前商户账号已绑定其他商户，请重新选择');
    return false;
  }
  if (contactPhone && !/^1\d{10}$/.test(contactPhone)) {
    message.warning('请输入正确的联系电话');
    return false;
  }

  const duplicatedMerchant = tableData.value.find(item => {
    if (!item.merchantName) return false;
    return item.merchantName.trim() === merchantName && (!form.id || item.id !== form.id);
  });
  if (duplicatedMerchant) {
    message.warning('商户名称已存在，请换一个名称');
    return false;
  }

  form.merchantName = merchantName;
  form.contactPhone = contactPhone || '';
  form.contactName = form.contactName?.trim() || '';
  form.location = form.location?.trim() || '';
  form.businessHours = form.businessHours?.trim() || '';
  form.description = form.description?.trim() || '';
  return true;
};

const copyText = async (text: string, successText: string) => {
  if (!text) return;
  try {
    if (typeof navigator !== 'undefined' && navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text);
      message.success(successText);
      return;
    }
  } catch (error) {
  }

  try {
    const input = document.createElement('input');
    input.value = text;
    document.body.appendChild(input);
    input.select();
    document.execCommand('copy');
    document.body.removeChild(input);
    message.success(successText);
  } catch (error) {
    message.error('复制失败，请手动复制');
  }
};

const submitMerchantAccount = async () => {
  if (!validateMerchantAccountForm()) return;

  accountSubmitting.value = true;
  try {
    const response = await addUser({
      userAccount: accountForm.userAccount.trim(),
      userName: accountForm.userName.trim() || undefined,
      userPhone: accountForm.userPhone.trim(),
      userRole: 'merchant',
      status: accountForm.status,
    });

    if (response.code === 0 && response.data) {
      createdAccount.userAccount = accountForm.userAccount.trim();
      createdAccount.password = DEFAULT_MERCHANT_PASSWORD;
      await loadMerchantUsers();
      form.userId = response.data;
      handleMerchantUserChange(response.data);
      accountModalOpen.value = false;
      message.success('商户账号创建成功，已自动选中该账号');
    }
  } catch (error: any) {
    message.error(error?.message || '商户账号创建失败');
  } finally {
    accountSubmitting.value = false;
  }
};

const getMerchantUserDisplay = (userId?: number) => {
  const user = merchantUsers.value.find(item => item.id === userId);
  const isBound = tableData.value.some(item => item.userId === userId);
  if (!user) {
    return {
      name: '未关联账号',
      account: '-',
      phone: '-',
      statusText: '',
      statusColor: 'default',
      accountLabel: '未绑定账号',
      phoneLabel: '未登记手机号'
    };
  }
  const account = user.userAccount || '-';
  const phone = user.userPhone || '-';
  return {
    name: user.userName || '未命名用户',
    account,
    phone,
    statusText: user.status === 0 ? '已禁用' : isBound ? '已绑定' : '可绑定',
    statusColor: user.status === 0 ? 'red' : isBound ? 'orange' : 'green',
    accountLabel: account === '-' ? '未设置登录账号' : `账号：${account}`,
    phoneLabel: phone === '-' ? '未登记手机号' : `手机号：${phone}`
  };
};

const getContactDisplay = (record: MerchantProfile) => {
  const name = record.contactName?.trim() || '未填写联系人';
  const phone = record.contactPhone?.trim() || '未填写联系电话';
  return {
    name,
    phone,
  };
};

const refreshList = async () => {
  loading.value = true;
  try {
    const res = await listMerchants({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      auditStatus: auditStatusFilter.value,
    });
    const page = res.data || res;
    tableData.value = page.records || [];
    pagination.total = page.total || 0;
    statistics.totalMerchants = page.total || 0;
    statistics.activeMerchants = tableData.value.filter(m => m.status === 1).length;
    statistics.pendingMerchants = tableData.value.filter(m => m.auditStatus === 'pending').length;
    statistics.rejectedMerchants = tableData.value.filter(m => m.auditStatus === 'rejected').length;
  } catch (error: any) {
    message.error(error?.message || '加载商户失败');
  } finally {
    loading.value = false;
  }
};

const openCreate = async () => {
  resetForm();
  await loadMerchantUsers();
  modalOpen.value = true;
};

const openEdit = async (record: MerchantProfile) => {
  createdAccount.userAccount = '';
  createdAccount.password = DEFAULT_MERCHANT_PASSWORD;
  await loadMerchantUsers();
  Object.assign(form, record);
  modalOpen.value = true;
};

const submit = async () => {
  if (!validateMerchantForm()) return;

  submitting.value = true;
  try {
    if (form.id) {
      await updateMerchant(form);
      message.success('商户更新成功');
    } else {
      await saveMerchant(form);
      message.success('商户创建成功');
    }
    modalOpen.value = false;
    await Promise.all([refreshList(), loadMerchantUsers()]);
  } catch (error: any) {
    message.error(error?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (record: MerchantProfile) => {
  if (!record.id) return;
  try {
    await deleteMerchant(record.id);
    message.success('删除成功');
    await Promise.all([refreshList(), loadMerchantUsers()]);
    window.dispatchEvent(new Event('foodpal-data-changed'));
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的商户');
    return;
  }

  try {
    let successCount = 0;
    for (const id of selectedRowKeys.value) {
      try {
        await deleteMerchant(id);
        successCount++;
      } catch (error) {
        console.error(`删除商户 ${id} 失败`, error);
      }
    }

    if (successCount > 0) {
      message.success(`批量删除 ${successCount} 个商户成功`);
      selectedRowKeys.value = [];
      await Promise.all([refreshList(), loadMerchantUsers()]);
      window.dispatchEvent(new Event('foodpal-data-changed'));
    } else {
      message.error('批量删除失败');
    }
  } catch (error) {
    message.error('批量删除失败');
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  refreshList();
};

const openAudit = (record: MerchantProfile) => {
  currentAuditRecord.value = record;

  let pendingData: any = {};
  try {
    if ((record as any).pendingData) {
      pendingData = JSON.parse((record as any).pendingData);
    }
  } catch (e) {
    console.error('解析 pendingData 失败', e);
    message.error('待审核数据格式错误');
    return;
  }

  const fields = [
    { key: 'merchantName', label: '商户名称' },
    { key: 'contactName', label: '联系人' },
    { key: 'contactPhone', label: '联系电话' },
    { key: 'avatar', label: '门头图片' },
    { key: 'businessHours', label: '营业时间' },
    { key: 'location', label: '位置' },
    { key: 'description', label: '描述' },
  ];

  compareData.value = fields.map(field => {
    const currentValue = (record as any)[field.key];
    const pendingValue = pendingData[field.key];
    return {
      field: field.label,
      current: currentValue,
      pending: pendingValue,
      changed: currentValue !== pendingValue,
      dataIndex: field.key,
    };
  });

  auditModalOpen.value = true;
};

const handleAudit = async (auditStatus: 'approved' | 'rejected') => {
  if (!currentAuditRecord.value || !currentAuditRecord.value.id) {
    message.error('审核数据错误');
    return;
  }

  auditing.value = true;
  try {
    await updateMerchant({ id: currentAuditRecord.value.id, auditStatus });
    message.success(auditStatus === 'approved' ? '审核通过' : '已驳回');
    auditModalOpen.value = false;
    currentAuditRecord.value = null;
    await refreshList();
  } catch (error: any) {
    message.error(error?.message || '审核失败');
  } finally {
    auditing.value = false;
  }
};

onMounted(async () => {
  await Promise.all([refreshList(), loadMerchantUsers()]);
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.account-help-text {
  margin-top: -8px;
  color: #8c8c8c;
  font-size: 12px;
  line-height: 20px;
}

.account-status-text {
  display: inline-block;
  margin-left: 8px;
  font-weight: 500;
}

.account-status-text.is-success {
  color: #389e0d;
}

.account-status-text.is-danger {
  color: #cf1322;
}

.created-account-card {
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
}

.created-account-title {
  margin-bottom: 8px;
  font-weight: 600;
  color: #237804;
}

.created-account-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 30px;
  color: #262626;
}

.created-account-tip {
  margin-top: 6px;
  color: #595959;
  font-size: 12px;
}

.bind-user-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bind-user-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.bind-user-name {
  font-weight: 500;
  color: #262626;
}

.bind-user-meta {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 18px;
}

.bind-preview-card {
  margin-top: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

.bind-preview-title {
  margin-bottom: 8px;
  font-weight: 600;
  color: #262626;
}

.bind-preview-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 30px;
  color: #595959;
  flex-wrap: wrap;
}

.bind-preview-row strong {
  color: #262626;
}

.contact-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-name {
  font-weight: 500;
  color: #262626;
}

.contact-meta {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 18px;
}

.form-section-title {
  margin-bottom: 4px;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.form-section-tip {
  margin-bottom: 12px;
  color: #8c8c8c;
  font-size: 12px;
  line-height: 20px;
}
</style>
