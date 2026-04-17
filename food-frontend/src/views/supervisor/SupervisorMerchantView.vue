<template>
  <div class="page-container">
    <a-page-header title="商户监管" sub-title="查看商户信息、菜品明细与投诉情况" @back="() => $router.back()" />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="8" :md="6">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索商户名称"
            allow-clear
            @search="refreshList"
          />
        </a-col>
        <a-col :xs="24" :sm="8" :md="6">
          <a-select v-model:value="auditStatus" placeholder="审核状态" allow-clear @change="refreshList">
            <a-select-option value="pending">待审核</a-select-option>
            <a-select-option value="approved">已通过</a-select-option>
            <a-select-option value="rejected">已驳回</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="8" :md="12">
          <a-space>
            <a-button @click="refreshList">刷新</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: MerchantProfile) => row.id as number"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'auditStatus'">
            <a-tag :color="auditStatusColor(record.auditStatus)">
              {{ auditStatusText(record.auditStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
              <a-button type="link" size="small" @click="viewDishes(record)">查看菜品明细</a-button>
              <a-button type="link" size="small" @click="viewComplaints(record)">投诉记录</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 商户详情弹窗 -->
    <a-modal v-model:open="detailOpen" title="商户详情" width="720px" :footer="null">
      <a-descriptions bordered :column="1" v-if="currentMerchant">
        <a-descriptions-item label="商户ID">{{ currentMerchant.id }}</a-descriptions-item>
        <a-descriptions-item label="店铺名称">{{ currentMerchant.merchantName }}</a-descriptions-item>
        <a-descriptions-item label="联系人">{{ currentMerchant.contactName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ currentMerchant.contactPhone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="营业时间">{{ currentMerchant.businessHours || '-' }}</a-descriptions-item>
        <a-descriptions-item label="地理位置">{{ currentMerchant.location || '-' }}</a-descriptions-item>
        <a-descriptions-item label="店铺描述">{{ currentMerchant.description || '-' }}</a-descriptions-item>
        <a-descriptions-item label="当前状态">
          <a-tag :color="currentMerchant.status === 1 ? 'green' : 'red'">
            {{ currentMerchant.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="审核状态">
          <a-tag :color="auditStatusColor(currentMerchant.auditStatus)">
            {{ auditStatusText(currentMerchant.auditStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ currentMerchant.createTime }}</a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <div class="violation-section">
        <h4>违规提醒</h4>
        <a-textarea
          v-model:value="violationForm.content"
          :rows="3"
          placeholder="如发现商户存在违规行为，请填写提醒内容"
        />
        <div class="violation-actions">
          <a-button type="primary" @click="submitViolation" :loading="submitting">发送违规提醒</a-button>
        </div>
      </div>
    </a-modal>

    <!-- 商户菜品弹窗 -->
    <a-modal v-model:open="dishesOpen" :title="`${currentMerchant?.merchantName} - 菜品监管明细`" width="900px" :footer="null">
      <a-table
        :columns="dishColumns"
        :data-source="dishData"
        :loading="dishLoading"
        :pagination="dishPagination"
        :row-key="(row: DishItem) => row.id as number"
        @change="handleDishTableChange"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'supplyStatus'">
            <a-tag :color="record.supplyStatus === 1 ? 'green' : 'red'">
              {{ record.supplyStatus === 1 ? '可供应' : '不可供应' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'isSpecial'">
            <a-tag v-if="record.isSpecial" color="red">特价</a-tag>
            <span v-else>-</span>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 商户投诉弹窗 -->
    <a-modal v-model:open="complaintsOpen" :title="`${currentMerchant?.merchantName} - 投诉记录`" width="900px" :footer="null">
      <a-table
        :columns="complaintColumns"
        :data-source="complaintData"
        :loading="complaintLoading"
        :pagination="complaintPagination"
        :row-key="(row: ComplaintItem) => row.id as number"
        @change="handleComplaintTableChange"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="complaintStatusColor(record.status)">{{ complaintStatusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'processProgress'">
            <span>{{ complaintProgressText(record) }}</span>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  listMerchants,
  listDishes,
  listComplaints,
  sendViolationNotice,
  type MerchantProfile,
  type DishItem,
  type ComplaintItem
} from '../../api';

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 70 },
  { title: '店铺名称', dataIndex: 'merchantName', key: 'merchantName', width: 180 },
  { title: '联系人', dataIndex: 'contactName', key: 'contactName', width: 120 },
  { title: '联系电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 140 },
  { title: '营业时间', dataIndex: 'businessHours', key: 'businessHours', width: 150 },
  { title: '状态', key: 'status', width: 90 },
  { title: '审核状态', key: 'auditStatus', width: 100 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' as const }
];

const dishColumns = [
  { title: '菜品名称', dataIndex: 'dishName', key: 'dishName' },
  { title: '分类', dataIndex: 'category', key: 'category', width: 100 },
  { title: '价格', dataIndex: 'dishPrice', key: 'dishPrice', width: 100 },
  { title: '供应状态', key: 'supplyStatus', width: 100 },
  { title: '是否特价', key: 'isSpecial', width: 100 },
  { title: '点赞数', dataIndex: 'likeCount', key: 'likeCount', width: 80 },
  { title: '收藏数', dataIndex: 'favoriteCount', key: 'favoriteCount', width: 80 }
];

const complaintColumns = [
  { title: '投诉编号', dataIndex: 'complaintNo', key: 'complaintNo', width: 160 },
  { title: '投诉标题', dataIndex: 'complaintTitle', key: 'complaintTitle' },
  { title: '状态', key: 'status', width: 100 },
  { title: '处理说明', key: 'processProgress', width: 220 },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
];

const loading = ref(false);
const submitting = ref(false);
const keyword = ref('');
const auditStatus = ref<string | undefined>(undefined);
const tableData = ref<MerchantProfile[]>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
});

const detailOpen = ref(false);
const currentMerchant = ref<MerchantProfile | null>(null);
const violationForm = reactive({
  content: ''
});

const dishesOpen = ref(false);
const dishLoading = ref(false);
const dishData = ref<DishItem[]>([]);
const dishPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

const complaintsOpen = ref(false);
const complaintLoading = ref(false);
const complaintData = ref<ComplaintItem[]>([]);
const complaintPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

const auditStatusText = (value?: string) => {
  if (value === 'pending') return '待审核';
  if (value === 'approved') return '已通过';
  if (value === 'rejected') return '已驳回';
  return '-';
};

const auditStatusColor = (value?: string) => {
  if (value === 'pending') return 'gold';
  if (value === 'approved') return 'green';
  if (value === 'rejected') return 'red';
  return 'default';
};

const complaintStatusText = (value?: string) => {
  if (value === 'pending_review') return '待审核';
  if (value === 'pending_rectify') return '待整改';
  if (value === 'rectified') return '待复核';
  if (value === 'completed') return '已完成';
  if (value === 'rejected') return '已驳回';
  return value || '-';
};

const complaintStatusColor = (value?: string) => {
  if (value === 'pending_review') return 'orange';
  if (value === 'pending_rectify') return 'gold';
  if (value === 'rectified') return 'blue';
  if (value === 'completed') return 'green';
  if (value === 'rejected') return 'red';
  return 'default';
};

const complaintProgressText = (record?: ComplaintItem) => {
  if (!record) return '-';
  if (record.status === 'pending_review') return '待监督员处理';
  if (record.status === 'pending_rectify') return record.rectifyRequirement || '已通知商户整改';
  if (record.status === 'rectified') return record.rectifyResult || '商户已提交整改结果，待监督员复核';
  if (record.status === 'completed') return record.feedback || record.rectifyResult || '投诉已处理完成';
  if (record.status === 'rejected') return record.feedback || '投诉已驳回';
  return record.processProgress || '-';
};

const refreshList = async () => {
  loading.value = true;
  try {
    const res = await listMerchants({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined,
      auditStatus: auditStatus.value || undefined
    });
    const page = res.data || res;
    tableData.value = page.records || [];
    pagination.total = page.total || 0;
  } catch (error: any) {
    message.error(error?.message || '加载商户失败');
  } finally {
    loading.value = false;
  }
};

const openDetail = (record: MerchantProfile) => {
  currentMerchant.value = record;
  violationForm.content = '';
  detailOpen.value = true;
};

const submitViolation = async () => {
  if (!violationForm.content.trim()) {
    message.warning('请填写违规提醒内容');
    return;
  }
  if (!currentMerchant.value?.id) {
    message.warning('商户信息不存在');
    return;
  }
  submitting.value = true;
  try {
    await sendViolationNotice(currentMerchant.value.id, violationForm.content.trim());
    message.success('违规提醒已发送');
    violationForm.content = '';
  } catch (error: any) {
    message.error(error?.message || '发送失败');
  } finally {
    submitting.value = false;
  }
};

const viewDishes = async (record: MerchantProfile) => {
  currentMerchant.value = record;
  dishesOpen.value = true;
  await loadDishes();
};

const loadDishes = async () => {
  if (!currentMerchant.value?.id) return;
  dishLoading.value = true;
  try {
    const res = await listDishes({
      current: dishPagination.current,
      size: dishPagination.pageSize,
      merchantId: currentMerchant.value.id
    });
    const page = res.data || res;
    dishData.value = page.records || [];
    dishPagination.total = page.total || 0;
  } catch (error: any) {
    message.error(error?.message || '加载菜品失败');
  } finally {
    dishLoading.value = false;
  }
};

const handleDishTableChange = (pag: any) => {
  dishPagination.current = pag.current;
  dishPagination.pageSize = pag.pageSize;
  loadDishes();
};

const viewComplaints = async (record: MerchantProfile) => {
  currentMerchant.value = record;
  complaintsOpen.value = true;
  await loadComplaints();
};

const loadComplaints = async () => {
  if (!currentMerchant.value?.id) return;
  complaintLoading.value = true;
  try {
    const res = await listComplaints({
      current: complaintPagination.current,
      size: complaintPagination.pageSize,
      merchantId: currentMerchant.value.id
    });
    const page = res.data || res;
    complaintData.value = page.records || [];
    complaintPagination.total = page.total || 0;
  } catch (error: any) {
    message.error(error?.message || '加载投诉失败');
  } finally {
    complaintLoading.value = false;
  }
};

const handleComplaintTableChange = (pag: any) => {
  complaintPagination.current = pag.current;
  complaintPagination.pageSize = pag.pageSize;
  loadComplaints();
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  refreshList();
};

onMounted(refreshList);
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.violation-section {
  margin-top: 16px;
}

.violation-section h4 {
  margin-bottom: 12px;
  color: #ff4d4f;
}

.violation-actions {
  margin-top: 12px;
  text-align: right;
}
</style>
