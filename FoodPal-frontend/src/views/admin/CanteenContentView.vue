<template>
  <div class="page-container">
    <a-page-header title="资讯与公告" sub-title="管理商户动态与系统公告" @back="() => $router.back()" />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="dynamic" tab="商户动态" />
        <a-tab-pane key="announcement" tab="系统公告" />
      </a-tabs>

      <div style="display: flex; justify-content: flex-end; margin-bottom: 16px;">
        <a-space v-if="activeTab === 'dynamic'">
          <a-input-search
            v-model:value="dynamicKeyword"
            placeholder="搜索动态标题或商户"
            allow-clear
            @search="refreshDynamicList"
            style="width: 200px"
          />
          <a-button @click="handleBatchDeleteDynamic" danger :disabled="!dynamicSelectedKeys.length">
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
          <a-button type="primary" @click="openCreateDynamic">
            <template #icon><PlusOutlined /></template>
            新增动态
          </a-button>
          <a-button @click="refreshDynamicList">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>

        <a-space v-else>
          <a-input-search
            v-model:value="announcementKeyword"
            placeholder="搜索公告标题"
            allow-clear
            @search="refreshAnnouncementList"
            style="width: 200px"
          />
          <a-button @click="handleBatchDeleteAnnouncement" danger :disabled="!announcementSelectedKeys.length">
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
          <a-button type="primary" @click="openCreateAnnouncement">
            <template #icon><PlusOutlined /></template>
            新增公告
          </a-button>
          <a-button @click="refreshAnnouncementList">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </div>

      <!-- 动态统计 -->
      <a-row :gutter="16" style="margin-bottom: 16px" v-if="activeTab === 'dynamic'">
        <a-col :xs="12" :sm="6">
          <a-statistic title="总动态数" :value="dynamicStatistics.total" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已发布" :value="dynamicStatistics.published" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="待审核" :value="dynamicStatistics.pending" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="涉及商户" :value="dynamicStatistics.merchantCount" />
        </a-col>
      </a-row>

      <!-- 公告统计 -->
      <a-row :gutter="16" style="margin-bottom: 16px" v-if="activeTab === 'announcement'">
        <a-col :xs="12" :sm="6">
          <a-statistic title="总公告数" :value="announcementStatistics.total" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已发布" :value="announcementStatistics.published" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="置顶中" :value="announcementStatistics.pinned" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已撤回" :value="announcementStatistics.withdrawn" />
        </a-col>
      </a-row>

      <a-table
        v-if="activeTab === 'dynamic'"
        :columns="dynamicColumns"
        :data-source="dynamicData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: DynamicItem) => row.id as number"
        :row-selection="dynamicRowSelection"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'coverImage'">
            <a-image
              v-if="record.coverImage"
              :src="record.coverImage"
              :width="60"
              :height="60"
              :preview="true"
              style="object-fit: cover; border-radius: 4px;"
            />
            <a-avatar v-else :size="60" style="background-color: #87d068">
              {{ record.title ? record.title.charAt(0).toUpperCase() : 'D' }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'title'">
            <div>
              <div style="font-weight: 500">{{ record.title }}</div>
              <div style="font-size: 12px; color: #999">{{ record.newsType || '-' }}</div>
              <div style="font-size: 12px; color: #999">{{ record.merchantName || '平台发布' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-space direction="vertical" :size="2">
              <a-tag :color="record.status === 'published' ? 'green' : 'orange'">
                {{ record.status === 'published' ? '已发布' : '草稿' }}
              </a-tag>
              <a-tag :color="record.auditStatus === 'approved' ? 'green' : record.auditStatus === 'rejected' ? 'red' : 'gold'">
                {{ record.auditStatus === 'approved' ? '已通过' : record.auditStatus === 'rejected' ? '已驳回' : '待审核' }}
              </a-tag>
            </a-space>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEditDynamic(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button v-if="record.status === 'published'" type="link" size="small" @click="handleWithdrawDynamic(record)">
                撤回
              </a-button>
              <a-button type="link" size="small" @click="handleAudit(record, 'approved')">通过</a-button>
              <a-button type="link" size="small" danger @click="handleAudit(record, 'rejected')">驳回</a-button>
              <a-popconfirm title="确定删除该动态吗？" @confirm="handleDeleteDynamic(record)">
                <a-button type="link" size="small" danger>
                  <template #icon><DeleteOutlined /></template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-table
        v-else
        :columns="announcementColumns"
        :data-source="announcementData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: AnnouncementItem) => row.id as number"
        :row-selection="announcementRowSelection"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'coverImage'">
            <a-image
              v-if="record.coverImage"
              :src="record.coverImage"
              :width="60"
              :height="60"
              :preview="true"
              style="object-fit: cover; border-radius: 4px;"
            />
            <a-avatar v-else :size="60" style="background-color: #87d068">
              {{ record.title ? record.title.charAt(0).toUpperCase() : 'A' }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'title'">
            <div>
              <div style="font-weight: 500">{{ record.title }}</div>
              <div style="font-size: 12px; color: #999">{{ record.announcementType || '-' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-space>
              <a-tag :color="getAnnouncementStatusColor(record.status)">
                {{ getAnnouncementStatusText(record.status) }}
              </a-tag>
              <a-tag v-if="record.isTop === 1" color="red">置顶</a-tag>
              <a-tag v-if="isExpired(record.expireTime)" color="gray">已过期</a-tag>
            </a-space>
          </template>
          <template v-else-if="column.key === 'expireTime'">
            <div>
              <div :class="{ 'expired-text': isExpired(record.expireTime) }">
                {{ record.expireTime ? formatDateTime(record.expireTime) : '永久展示' }}
              </div>
              <div v-if="record.expireTime && !isExpired(record.expireTime)" class="remaining-time">
                剩余 {{ getRemainingTime(record.expireTime) }}
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEditAnnouncement(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handlePin(record)">
                {{ record.isTop === 1 ? '取消置顶' : '置顶' }}
              </a-button>
              <a-button
                v-if="record.status === 'published'"
                type="link"
                size="small"
                @click="handleWithdrawAnnouncement(record)"
              >
                撤回
              </a-button>
              <a-button
                v-else-if="record.status === 'withdrawn'"
                type="link"
                size="small"
                @click="handleRepublishAnnouncement(record)"
              >
                重新发布
              </a-button>
              <a-button
                v-else
                type="link"
                size="small"
                @click="handlePublish(record)"
              >
                发布
              </a-button>
              <a-popconfirm title="确定删除该公告吗？" @confirm="handleDeleteAnnouncement(record)">
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

    <!-- 动态编辑模态框 -->
    <a-modal
      v-model:open="dynamicModalOpen"
      :title="dynamicForm.id ? '编辑动态' : '新增动态'"
      width="840px"
      @ok="submitDynamic"
      :confirm-loading="submitting"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="dynamicForm.title" placeholder="请输入动态标题" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="资讯类型">
              <a-select v-model:value="dynamicForm.newsType">
                <a-select-option value="new_dish">新品</a-select-option>
                <a-select-option value="activity">活动</a-select-option>
                <a-select-option value="notice">通知</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="关联商户">
              <a-select v-model:value="dynamicForm.merchantId" placeholder="请选择商户账号/商户">
                <a-select-option v-for="merchant in merchantOptions" :key="merchant.id" :value="merchant.id">
                  {{ merchant.merchantName }} / {{ merchant.accountName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="封面图片">
          <ImageUpload v-model="dynamicForm.coverImage" type="user" />
        </a-form-item>
        <a-form-item label="内容" required>
          <a-textarea v-model:value="dynamicForm.content" :rows="6" placeholder="请输入动态内容" />
        </a-form-item>
        <a-form-item label="展示时间区间">
          <a-range-picker v-model:value="dynamicRange" show-time style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 公告编辑模态框 -->
    <a-modal
      v-model:open="announcementModalOpen"
      :title="announcementForm.id ? '编辑公告' : '新增公告'"
      width="840px"
      @ok="submitAnnouncement"
      :confirm-loading="submitting"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="announcementForm.title" placeholder="请输入公告标题" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="公告类型">
              <a-select v-model:value="announcementForm.announcementType">
                <a-select-option value="system">系统通知</a-select-option>
                <a-select-option value="canteen">食堂通知</a-select-option>
                <a-select-option value="emergency">紧急通知</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="排序">
              <a-input-number v-model:value="announcementForm.sortNo" style="width: 100%" :min="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="封面图片">
          <ImageUpload v-model="announcementForm.coverImage" type="user" />
        </a-form-item>
        <a-form-item label="内容" required>
          <a-textarea v-model:value="announcementForm.content" :rows="6" placeholder="请输入公告内容" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="announcementForm.status">
                <a-select-option value="published">发布</a-select-option>
                <a-select-option value="draft">草稿</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="过期时间">
              <a-date-picker v-model:value="announcementForm.expireTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue';
import dayjs, { Dayjs } from 'dayjs';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  DeleteOutlined,
  ReloadOutlined,
  EditOutlined
} from '@ant-design/icons-vue';
import ImageUpload from '../../components/ImageUpload.vue';
import {
  auditDynamic,
  deleteAnnouncement,
  deleteDynamic,
  listAnnouncements,
  listDynamics,
  listMerchants,
  pinAnnouncement,
  publishAnnouncement,
  saveAnnouncement,
  saveDynamic,
  updateAnnouncement,
  updateDynamic,
  withdrawAnnouncement,
  withdrawDynamic,
  type AnnouncementItem,
  type DynamicItem
} from '../../api';

const dynamicColumns = [
  { title: '封面', key: 'coverImage', width: 100, fixed: 'left' },
  { title: '标题 / 商户', key: 'title', width: 280 },
  { title: '状态', key: 'status', width: 160 },
  { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 160 },
  { title: '截止时间', dataIndex: 'expireTime', key: 'expireTime', width: 160 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 300, fixed: 'right' },
];

const announcementColumns = [
  { title: '封面', key: 'coverImage', width: 100, fixed: 'left' },
  { title: '标题', key: 'title', width: 250 },
  { title: '类型', dataIndex: 'announcementType', key: 'announcementType', width: 100 },
  { title: '状态', key: 'status', width: 120 },
  { title: '展示期限', key: 'expireTime', width: 160 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 80 },
  { title: '发布时间', dataIndex: 'publishTime', key: 'publishTime', width: 160 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' },
];

const loading = ref(false);
const submitting = ref(false);
const activeTab = ref('dynamic');
const dynamicKeyword = ref('');
const announcementKeyword = ref('');
const dynamicData = ref<DynamicItem[]>([]);
const announcementData = ref<AnnouncementItem[]>([]);
const dynamicModalOpen = ref(false);
const announcementModalOpen = ref(false);
const dynamicSelectedKeys = ref<number[]>([]);
const announcementSelectedKeys = ref<number[]>([]);

const dynamicStatistics = reactive({
  total: 0,
  published: 0,
  pending: 0,
  merchantCount: 0,
});

const announcementStatistics = reactive({
  total: 0,
  published: 0,
  pinned: 0,
  withdrawn: 0,
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

const dynamicForm = reactive<DynamicItem>({
  title: '',
  content: '',
  coverImage: '',
  newsType: 'notice',
  merchantId: undefined,
  expireTime: undefined
});

const dynamicRange = ref<[Dayjs, Dayjs] | null>(null);
const merchantOptions = ref<Array<{ id: number; merchantName: string; accountName: string }>>([]);

const announcementForm = reactive<AnnouncementItem>({
  title: '',
  content: '',
  coverImage: '',
  announcementType: 'system',
  sortNo: 0,
  status: 'published'
});

const dynamicRowSelection = computed(() => ({
  selectedRowKeys: dynamicSelectedKeys.value,
  onChange: (keys: number[]) => {
    dynamicSelectedKeys.value = keys;
  },
}));

const announcementRowSelection = computed(() => ({
  selectedRowKeys: announcementSelectedKeys.value,
  onChange: (keys: number[]) => {
    announcementSelectedKeys.value = keys;
  },
}));

const handleTabChange = (key: string) => {
  activeTab.value = key;
  if (key === 'dynamic') {
    refreshDynamicList();
  } else {
    refreshAnnouncementList();
  }
};

const refreshDynamicList = async () => {
  loading.value = true;
  try {
    const res = await listDynamics({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: dynamicKeyword.value || undefined,
      onlyPublished: false
    });
    const page = res.data || res;
    dynamicData.value = page.records || [];
    pagination.total = page.total || 0;
    
    dynamicStatistics.total = dynamicData.value.length;
    dynamicStatistics.published = dynamicData.value.filter(d => d.status === 'published').length;
    dynamicStatistics.pending = dynamicData.value.filter(d => d.auditStatus === 'pending').length;
    dynamicStatistics.merchantCount = new Set(dynamicData.value.map(d => d.merchantId).filter(Boolean)).size;
  } catch (error: any) {
    message.error(error?.message || '加载动态失败');
  } finally {
    loading.value = false;
  }
};

const refreshAnnouncementList = async () => {
  loading.value = true;
  try {
    const res = await listAnnouncements({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: announcementKeyword.value || undefined
    });
    const page = res.data || res;
    announcementData.value = page.records || [];
    pagination.total = page.total || 0;
    
    const now = new Date();
    announcementStatistics.total = announcementData.value.length;
    announcementStatistics.published = announcementData.value.filter(a => a.status === 'published').length;
    announcementStatistics.pinned = announcementData.value.filter(a => a.isTop === 1).length;
    announcementStatistics.withdrawn = announcementData.value.filter(a => a.status === 'withdrawn').length;
  } catch (error: any) {
    message.error(error?.message || '加载公告失败');
  } finally {
    loading.value = false;
  }
};

const openCreateDynamic = () => {
  dynamicForm.id = undefined;
  dynamicForm.title = '';
  dynamicForm.content = '';
  dynamicForm.coverImage = '';
  dynamicForm.newsType = 'notice';
  dynamicForm.merchantId = undefined;
  dynamicForm.expireTime = undefined;
  dynamicRange.value = null;
  dynamicModalOpen.value = true;
};

const openEditDynamic = (record: DynamicItem) => {
  Object.assign(dynamicForm, record);
  dynamicRange.value = record.publishTime && record.expireTime
    ? [dayjs(record.publishTime), dayjs(record.expireTime)]
    : null;
  dynamicModalOpen.value = true;
};

const submitDynamic = async () => {
  if (!dynamicForm.title || !dynamicForm.content) {
    message.warning('请填写标题和内容');
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      ...dynamicForm,
      publishTime: dynamicRange.value?.[0]?.format('YYYY-MM-DD HH:mm:ss'),
      expireTime: dynamicRange.value?.[1]?.format('YYYY-MM-DD HH:mm:ss')
    };
    if (dynamicForm.id) {
      await updateDynamic(payload);
      message.success('动态更新成功');
    } else {
      await saveDynamic(payload);
      message.success('动态创建成功');
    }
    dynamicModalOpen.value = false;
    await refreshDynamicList();
  } catch (error: any) {
    message.error(error?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};

const openCreateAnnouncement = () => {
  announcementForm.id = undefined;
  announcementForm.title = '';
  announcementForm.content = '';
  announcementForm.coverImage = '';
  announcementForm.announcementType = 'system';
  announcementForm.sortNo = 0;
  announcementForm.status = 'published';
  announcementForm.expireTime = undefined;
  announcementModalOpen.value = true;
};

const openEditAnnouncement = (record: AnnouncementItem) => {
  Object.assign(announcementForm, {
    ...record,
    expireTime: record.expireTime ? dayjs(record.expireTime) : undefined
  });
  announcementModalOpen.value = true;
};

const submitAnnouncement = async () => {
  if (!announcementForm.title || !announcementForm.content) {
    message.warning('请填写标题和内容');
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      ...announcementForm,
      expireTime: announcementForm.expireTime ? dayjs(announcementForm.expireTime as any).format('YYYY-MM-DD HH:mm:ss') : undefined
    };
    if (announcementForm.id) {
      await updateAnnouncement(payload);
      message.success('公告更新成功');
    } else {
      await saveAnnouncement(payload);
      message.success('公告创建成功');
    }
    announcementModalOpen.value = false;
    await refreshAnnouncementList();
  } catch (error: any) {
    message.error(error?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};

const handleWithdrawDynamic = async (record: DynamicItem) => {
  try {
    await withdrawDynamic(record.id);
    message.success('撤回成功');
    await refreshDynamicList();
  } catch (error: any) {
    message.error(error?.message || '撤回失败');
  }
};

const handleAudit = async (record: DynamicItem, auditStatus: 'approved' | 'rejected') => {
  try {
    let auditReason = '';
    if (auditStatus === 'rejected') {
      auditReason = window.prompt('请输入驳回原因（必填）')?.trim() || '';
      if (!auditReason) {
        message.warning('请填写驳回原因');
        return;
      }
    }
    await auditDynamic(record.id, auditStatus, auditReason || undefined);
    message.success(auditStatus === 'approved' ? '审核通过' : '已驳回');
    await refreshDynamicList();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  }
};

const handleDeleteDynamic = async (record: DynamicItem) => {
  try {
    await deleteDynamic(record.id);
    message.success('删除成功');
    await refreshDynamicList();
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

const handleBatchDeleteDynamic = async () => {
  if (dynamicSelectedKeys.value.length === 0) {
    message.warning('请选择要删除的动态');
    return;
  }

  try {
    let successCount = 0;
    for (const id of dynamicSelectedKeys.value) {
      try {
        await deleteDynamic(id);
        successCount++;
      } catch (error) {
        console.error(`删除动态 ${id} 失败`, error);
      }
    }
    if (successCount > 0) {
      message.success(`批量删除 ${successCount} 个动态成功`);
      dynamicSelectedKeys.value = [];
      await refreshDynamicList();
      return;
    }
    message.error('批量删除失败，请稍后重试');
  } catch (error) {
    message.error('批量删除失败');
  }
};

const handlePin = async (record: AnnouncementItem) => {
  try {
    await pinAnnouncement(record.id, record.isTop !== 1);
    message.success('操作成功');
    await refreshAnnouncementList();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  }
};

const handlePublish = async (record: AnnouncementItem) => {
  try {
    await publishAnnouncement(record.id, record.status === 'published' ? 'unpublished' : 'published');
    message.success('操作成功');
    await refreshAnnouncementList();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  }
};

const handleWithdrawAnnouncement = async (record: AnnouncementItem) => {
  try {
    await withdrawAnnouncement(record.id);
    message.success('公告已撤回');
    await refreshAnnouncementList();
  } catch (error: any) {
    message.error(error?.message || '撤回失败');
  }
};

const handleRepublishAnnouncement = async (record: AnnouncementItem) => {
  try {
    await publishAnnouncement(record.id, 'published');
    message.success('公告已重新发布');
    await refreshAnnouncementList();
  } catch (error: any) {
    message.error(error?.message || '发布失败');
  }
};

const getAnnouncementStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'published': 'green',
    'unpublished': 'orange',
    'withdrawn': 'red'
  };
  return colorMap[status] || 'default';
};

const getAnnouncementStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'published': '已发布',
    'unpublished': '未发布',
    'withdrawn': '已撤回'
  };
  return textMap[status] || status;
};

const isExpired = (expireTime: string | null) => {
  if (!expireTime) return false;
  return new Date(expireTime) < new Date();
};

const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getRemainingTime = (expireTime: string) => {
  if (!expireTime) return '';
  const now = new Date().getTime();
  const expire = new Date(expireTime).getTime();
  const diff = expire - now;
  
  if (diff <= 0) return '已过期';
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  
  if (days > 0) return `${days}天${hours}小时`;
  if (hours > 0) return `${hours}小时`;
  return '不足1小时';
};

const handleDeleteAnnouncement = async (record: AnnouncementItem) => {
  try {
    await deleteAnnouncement(record.id);
    message.success('删除成功');
    await refreshAnnouncementList();
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

const handleBatchDeleteAnnouncement = async () => {
  if (announcementSelectedKeys.value.length === 0) {
    message.warning('请选择要删除的公告');
    return;
  }

  try {
    let successCount = 0;
    for (const id of announcementSelectedKeys.value) {
      try {
        await deleteAnnouncement(id);
        successCount++;
      } catch (error) {
        console.error(`删除公告 ${id} 失败`, error);
      }
    }
    if (successCount > 0) {
      message.success(`批量删除 ${successCount} 个公告成功`);
      announcementSelectedKeys.value = [];
      await refreshAnnouncementList();
      return;
    }
    message.error('批量删除失败，请稍后重试');
  } catch (error) {
    message.error('批量删除失败');
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  if (activeTab.value === 'dynamic') {
    refreshDynamicList();
  } else {
    refreshAnnouncementList();
  }
};

const loadMerchantOptions = async () => {
  try {
    const res = await listMerchants({ current: 1, size: 100 });
    const page = res.data || res;
    merchantOptions.value = (page.records || []).map((item: any) => ({
      id: item.id,
      merchantName: item.merchantName,
      accountName: item.contactName || `商户ID ${item.id}`
    }));
  } catch (error) {
    merchantOptions.value = [];
  }
};

onMounted(() => {
  refreshDynamicList();
  loadMerchantOptions();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.expired-text {
  color: #999;
  text-decoration: line-through;
}

.remaining-time {
  font-size: 12px;
  color: #52c41a;
}
</style>
