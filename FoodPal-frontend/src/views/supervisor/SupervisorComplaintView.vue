<template>
  <div class="page-container">
    <a-page-header title="投诉处理" sub-title="查看并处理学生投诉" @back="() => $router.back()" />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="8" :md="6">
          <a-select v-model:value="status" placeholder="状态筛选" allow-clear @change="refreshList">
            <a-select-option value="pending_review">待审核</a-select-option>
            <a-select-option value="pending_rectify">待整改</a-select-option>
            <a-select-option value="rectified">已整改待复核</a-select-option>
            <a-select-option value="completed">已完成</a-select-option>
            <a-select-option value="rejected">已驳回</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="16" :md="18">
          <a-space>
            <a-button @click="refreshList">刷新</a-button>
            <a-button type="primary" @click="exportData" :loading="exporting">导出Excel</a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: ComplaintItem) => row.id as number"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openDetail(record)">详情</a-button>
              <a-button
                v-if="record.status === 'pending_review' || record.status === 'rectified'"
                type="link"
                size="small"
                @click="openProcess(record)"
              >处理</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card :bordered="false" style="margin-top: 16px" title="投诉排行榜 Top10">
      <a-table :columns="rankingColumns" :data-source="rankingData" :pagination="false" size="small" :row-key="(row:any) => row.merchantId" />
    </a-card>

    <!-- 投诉详情弹窗 -->
    <a-modal v-model:open="detailOpen" title="投诉详情" width="720px" :footer="null">
      <a-descriptions bordered :column="1" v-if="currentComplaint">
        <a-descriptions-item label="投诉编号">{{ currentComplaint.complaintNo }}</a-descriptions-item>
        <a-descriptions-item label="投诉标题">{{ currentComplaint.complaintTitle }}</a-descriptions-item>
        <a-descriptions-item label="投诉内容">{{ currentComplaint.complaintContent }}</a-descriptions-item>
        <a-descriptions-item label="投诉图片">
          <a-space wrap v-if="evidenceImages.length">
            <a-image
              v-for="(url, index) in evidenceImages"
              :key="`${url}-${index}`"
              :src="url"
              :width="140"
              :preview="true"
            />
          </a-space>
          <span v-else>-</span>
        </a-descriptions-item>
        <a-descriptions-item label="投诉人">{{ currentComplaint.studentName }}</a-descriptions-item>
        <a-descriptions-item label="被投诉商户">{{ currentComplaint.merchantName }}</a-descriptions-item>
        <a-descriptions-item label="当前状态">
          <a-tag :color="statusColor(currentComplaint.status)">{{ statusText(currentComplaint.status) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="处理说明">{{ progressText(currentComplaint) }}</a-descriptions-item>
        <a-descriptions-item label="整改要求">{{ currentComplaint.rectifyRequirement || '-' }}</a-descriptions-item>
        <a-descriptions-item label="整改结果">{{ currentComplaint.rectifyResult || '-' }}</a-descriptions-item>
        <a-descriptions-item label="学生反馈">{{ currentComplaint.feedback || '-' }}</a-descriptions-item>
        <a-descriptions-item label="学生评价" v-if="currentComplaint.resultRating">
          <a-rate :value="currentComplaint.resultRating" disabled />
        </a-descriptions-item>
        <a-descriptions-item label="提交时间">{{ currentComplaint.createTime }}</a-descriptions-item>
        <a-descriptions-item label="处理时间">{{ currentComplaint.processTime || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 处理投诉弹窗 -->
    <a-modal v-model:open="processOpen" title="投诉处理" @ok="submitProcess" :confirm-loading="submitting" width="720px">
      <a-form layout="vertical">
        <a-form-item label="投诉编号">
          <a-input :value="currentComplaint?.complaintNo" disabled />
        </a-form-item>
        <a-form-item label="投诉标题">
          <a-input :value="currentComplaint?.complaintTitle" disabled />
        </a-form-item>
        <a-form-item label="投诉内容">
          <a-textarea :value="currentComplaint?.complaintContent" :rows="3" disabled />
        </a-form-item>
        <a-form-item label="处理状态" required>
          <a-select v-model:value="processForm.status">
            <a-select-option value="pending_rectify">下达整改</a-select-option>
            <a-select-option value="completed">处理完成</a-select-option>
            <a-select-option value="rejected">驳回投诉</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="处理进度">
          <a-input v-model:value="processForm.processProgress" placeholder="请输入处理进度描述" />
        </a-form-item>
        <a-form-item label="整改要求" v-if="processForm.status === 'pending_rectify'">
          <a-textarea v-model:value="processForm.rectifyRequirement" :rows="2" placeholder="下达整改时请填写整改要求" />
        </a-form-item>
        <a-form-item label="整改结果">
          <a-textarea v-model:value="processForm.rectifyResult" :rows="2" />
        </a-form-item>
        <a-form-item label="反馈给学生">
          <a-textarea v-model:value="processForm.feedback" :rows="2" placeholder="处理结果将通知学生" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  getComplaintRanking,
  listComplaints,
  processComplaint,
  exportComplaints,
  type ComplaintItem
} from '../../api';
import { normalizeImageUrl } from '../../utils/image';

const columns = [
  { title: '编号', dataIndex: 'complaintNo', key: 'complaintNo', width: 160 },
  { title: '投诉标题', dataIndex: 'complaintTitle', key: 'complaintTitle', width: 180 },
  { title: '学生', dataIndex: 'studentName', key: 'studentName', width: 120 },
  { title: '商户', dataIndex: 'merchantName', key: 'merchantName', width: 160 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '进度', dataIndex: 'processProgress', key: 'processProgress', width: 180 },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const }
];

const rankingColumns = [
  { title: '商户', dataIndex: 'merchantName', key: 'merchantName' },
  { title: '投诉数量', dataIndex: 'complaintCount', key: 'complaintCount' }
];

const loading = ref(false);
const submitting = ref(false);
const exporting = ref(false);
const status = ref<string | undefined>(undefined);
const tableData = ref<ComplaintItem[]>([]);
const rankingData = ref<Array<{ merchantId: number; merchantName: string; complaintCount: number }>>([]);

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true
});

const detailOpen = ref(false);
const processOpen = ref(false);
const currentComplaint = ref<ComplaintItem | null>(null);
const processForm = reactive({
  status: 'pending_rectify',
  processProgress: '',
  rectifyRequirement: '',
  rectifyResult: '',
  feedback: ''
});

const parseEvidenceUrls = (raw?: string) => {
  if (!raw) return [] as string[];
  const source = raw.trim();
  if (!source) return [] as string[];

  const normalizeList = (list: unknown[]) => list
    .map(item => normalizeImageUrl(typeof item === 'string' ? item.trim() : String(item ?? '').trim()))
    .filter(Boolean);

  if (source.startsWith('[') && source.endsWith(']')) {
    try {
      const parsed = JSON.parse(source);
      if (Array.isArray(parsed)) {
        return normalizeList(parsed);
      }
    } catch {
    }
  }

  return normalizeList(source.split(','));
};

const evidenceImages = computed(() => parseEvidenceUrls(currentComplaint.value?.evidenceUrls));

const statusText = (value?: string) => {
  if (value === 'pending_review') return '待审核';
  if (value === 'pending_rectify') return '待整改';
  if (value === 'rectified') return '待复核';
  if (value === 'completed') return '已完成';
  if (value === 'rejected') return '已驳回';
  return value || '-';
};

const statusColor = (value?: string) => {
  if (value === 'pending_review') return 'orange';
  if (value === 'pending_rectify') return 'gold';
  if (value === 'rectified') return 'blue';
  if (value === 'completed') return 'green';
  if (value === 'rejected') return 'red';
  return 'default';
};

const progressText = (record?: ComplaintItem | null) => {
  if (!record) return '待处理';
  if (record.status === 'pending_review') return '待监督员处理';
  if (record.status === 'pending_rectify') return record.rectifyRequirement || '已通知商户整改';
  if (record.status === 'rectified') return record.rectifyResult || '商户已提交整改结果，请监督员确认是否通过';
  if (record.status === 'completed') return record.feedback || record.rectifyResult || '投诉已处理完成';
  if (record.status === 'rejected') return record.feedback || '投诉已驳回';
  return record.processProgress || '待处理';
};

const refreshList = async () => {
  loading.value = true;
  try {
    const res = await listComplaints({
      current: pagination.current,
      size: pagination.pageSize,
      status: status.value || undefined
    });
    const page = res.data || res;
    tableData.value = page.records || [];
    pagination.total = page.total || 0;
  } catch (error: any) {
    message.error(error?.message || '加载投诉失败');
  } finally {
    loading.value = false;
  }
};

const refreshRanking = async () => {
  try {
    const res = await getComplaintRanking(10);
    rankingData.value = res.data || [];
  } catch (error: any) {
    message.error(error?.message || '加载排行榜失败');
  }
};

const openDetail = (record: ComplaintItem) => {
  currentComplaint.value = record;
  detailOpen.value = true;
};

const openProcess = (record: ComplaintItem) => {
  currentComplaint.value = record;
  processForm.status = record.status === 'rectified' ? 'completed' : 'pending_rectify';
  processForm.processProgress = record.processProgress || '';
  processForm.rectifyRequirement = record.rectifyRequirement || '';
  processForm.rectifyResult = record.rectifyResult || '';
  processForm.feedback = record.feedback || '';
  processOpen.value = true;
};

const submitProcess = async () => {
  if (!currentComplaint.value?.id) return;
  if (processForm.status === 'pending_rectify' && !processForm.rectifyRequirement) {
    message.warning('下达整改时必须填写整改要求');
    return;
  }
  submitting.value = true;
  try {
    await processComplaint({
      complaintId: currentComplaint.value.id,
      status: processForm.status,
      processProgress: processForm.processProgress || undefined,
      rectifyRequirement: processForm.rectifyRequirement || undefined,
      rectifyResult: processForm.rectifyResult || undefined,
      feedback: processForm.feedback || undefined
    });
    message.success('投诉处理成功');
    processOpen.value = false;
    await refreshList();
    await refreshRanking();
  } catch (error: any) {
    message.error(error?.message || '处理失败');
  } finally {
    submitting.value = false;
  }
};

const exportData = async () => {
  exporting.value = true;
  try {
    const blob = await exportComplaints({ status: status.value || undefined });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    const filename = `投诉记录_${new Date().toISOString().slice(0, 10)}.csv`;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    message.success('导出成功');
  } catch (error: any) {
    message.error(error?.message || '导出失败');
  } finally {
    exporting.value = false;
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  refreshList();
};

onMounted(async () => {
  await refreshList();
  await refreshRanking();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
