<template>
  <div class="page-container">
    <a-page-header title="投诉管理" sub-title="处理学生对商户的投诉并跟踪整改" @back="() => $router.back()" />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索编号、标题、学生或商户"
            allow-clear
            @search="handleSearch"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="5">
          <a-select
            v-model:value="status"
            placeholder="状态筛选"
            allow-clear
            @change="handleSearch"
            style="width: 100%"
          >
            <a-select-option value="pending_review">待审核</a-select-option>
            <a-select-option value="pending_rectify">待整改</a-select-option>
            <a-select-option value="rectified">已整改待复核</a-select-option>
            <a-select-option value="completed">已完成</a-select-option>
            <a-select-option value="rejected">已驳回</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="24" :md="13">
          <a-space>
            <a-button @click="refreshList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
            <a-button type="primary" @click="exportData" :loading="exporting">
              <template #icon><ExportOutlined /></template>
              导出Excel
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :xs="12" :sm="6">
          <a-statistic title="总投诉数" :value="statistics.totalComplaints" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="待处理" :value="statistics.pendingComplaints" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="待整改" :value="statistics.rectifyComplaints" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已完成" :value="statistics.completedComplaints" />
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
          <template v-if="column.key === 'complaintNo'">
            <a-tag color="blue" style="font-family: monospace">{{ record.complaintNo }}</a-tag>
          </template>
          <template v-else-if="column.key === 'complaintTitle'">
            <div style="font-weight: 500">{{ record.complaintTitle }}</div>
          </template>
          <template v-else-if="column.key === 'complaintContent'">
            <a-tooltip :title="record.complaintContent">
              <div style="max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                {{ record.complaintContent }}
              </div>
            </a-tooltip>
          </template>
          <template v-else-if="column.key === 'studentName'">
            <div>
              <div>{{ record.studentName || '-' }}</div>
              <div style="font-size: 12px; color: #999">{{ record.studentAccount || '-' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'processProgress'">
            <div style="font-size: 12px; color: #595959;">{{ getProgressText(record) }}</div>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openProcess(record)">
                <template #icon><CheckCircleOutlined /></template>
                处理
              </a-button>
              <a-button type="link" size="small" @click="viewDetail(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-card :bordered="false" style="margin-top: 16px" title="商户投诉排行榜 Top10">
      <a-table :columns="rankingColumns" :data-source="rankingData" :pagination="false" size="small" :row-key="(row:any) => row.merchantId" />
    </a-card>

    <a-modal v-model:open="processOpen" title="投诉处理" @ok="submitProcess" :confirm-loading="submitting" width="720px">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="投诉编号">
              <a-input :value="currentComplaint?.complaintNo" disabled />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="提交时间">
              <a-input :value="currentComplaint?.createTime" disabled />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="投诉标题">
          <a-input :value="currentComplaint?.complaintTitle" disabled />
        </a-form-item>
        <a-form-item label="投诉内容">
          <a-textarea :value="currentComplaint?.complaintContent" :rows="3" disabled />
        </a-form-item>
        <a-form-item label="证据图片" v-if="evidenceImages.length">
          <a-space wrap>
            <a-image
              v-for="(url, index) in evidenceImages"
              :key="`${url}-${index}`"
              :src="url"
              :width="140"
              :preview="true"
            />
          </a-space>
        </a-form-item>
        <a-form-item label="处理状态" required>
          <a-select v-model:value="processForm.status">
            <a-select-option value="pending_rectify">下达整改</a-select-option>
            <a-select-option value="completed">处理完成</a-select-option>
            <a-select-option value="rejected">驳回投诉</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="整改要求" v-if="processForm.status === 'pending_rectify'">
          <a-textarea v-model:value="processForm.rectifyRequirement" :rows="2" placeholder="请输入整改要求" />
        </a-form-item>
        <a-form-item label="整改结果" v-if="processForm.status === 'completed'">
          <a-textarea v-model:value="processForm.rectifyResult" :rows="2" placeholder="请输入整改结果" />
        </a-form-item>
        <a-form-item label="反馈给学生">
          <a-textarea v-model:value="processForm.feedback" :rows="2" placeholder="请输入反馈内容" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="detailOpen" title="投诉详情" :footer="null" width="760px">
      <a-descriptions :column="2" bordered v-if="detailComplaint">
        <a-descriptions-item label="投诉编号" :span="2">
          <a-tag color="blue" style="font-family: monospace">{{ detailComplaint.complaintNo }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="投诉标题" :span="2">{{ detailComplaint.complaintTitle }}</a-descriptions-item>
        <a-descriptions-item label="投诉学生">{{ detailComplaint.studentName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="被投诉商户">{{ detailComplaint.merchantName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="投诉内容" :span="2">{{ detailComplaint.complaintContent }}</a-descriptions-item>
        <a-descriptions-item label="证据图片" :span="2">
          <a-space wrap v-if="detailEvidenceImages.length">
            <a-image
              v-for="(url, index) in detailEvidenceImages"
              :key="`${url}-${index}`"
              :src="url"
              :width="140"
              :preview="true"
            />
          </a-space>
          <span v-else style="color: #999">暂无证据</span>
        </a-descriptions-item>
        <a-descriptions-item label="处理状态">
          <a-tag :color="statusColor(detailComplaint.status)">{{ statusText(detailComplaint.status) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="处理说明">{{ getProgressText(detailComplaint) }}</a-descriptions-item>
        <a-descriptions-item label="整改要求">{{ detailComplaint.rectifyRequirement || '-' }}</a-descriptions-item>
        <a-descriptions-item label="整改结果">{{ detailComplaint.rectifyResult || '-' }}</a-descriptions-item>
        <a-descriptions-item label="反馈内容" :span="2">{{ detailComplaint.feedback || '-' }}</a-descriptions-item>
        <a-descriptions-item label="处理评价" :span="2">
          <a-rate v-model:value="detailComplaint.resultRating" disabled v-if="detailComplaint.resultRating" />
          <span v-else style="color: #999">未评价</span>
        </a-descriptions-item>
        <a-descriptions-item label="提交时间">{{ detailComplaint.createTime }}</a-descriptions-item>
        <a-descriptions-item label="处理时间">{{ detailComplaint.processTime || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  ReloadOutlined,
  ExportOutlined,
  CheckCircleOutlined,
  EyeOutlined
} from '@ant-design/icons-vue';
import { getComplaintRanking, listComplaints, processComplaint, exportComplaints, type ComplaintItem } from '../../api';
import { normalizeImageUrl } from '../../utils/image';

const columns = [
  { title: '编号', dataIndex: 'complaintNo', key: 'complaintNo', width: 160, fixed: 'left' },
  { title: '投诉标题', key: 'complaintTitle', width: 180 },
  { title: '投诉内容', key: 'complaintContent', width: 220 },
  { title: '学生', dataIndex: 'studentName', key: 'studentName', width: 160 },
  { title: '商户', dataIndex: 'merchantName', key: 'merchantName', width: 180 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '处理说明', dataIndex: 'processProgress', key: 'processProgress', width: 220 },
  { title: '提交时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 180, fixed: 'right' },
];

const rankingColumns = [
  { title: '排名', key: 'rank', width: 80 },
  { title: '商户', dataIndex: 'merchantName', key: 'merchantName' },
  { title: '投诉数量', dataIndex: 'complaintCount', key: 'complaintCount' }
];

const loading = ref(false);
const submitting = ref(false);
const exporting = ref(false);
const keyword = ref('');
const status = ref<string | undefined>(undefined);
const tableData = ref<ComplaintItem[]>([]);
const rankingData = ref<Array<{ merchantId: number; merchantName: string; complaintCount: number; rank?: number }>>([]);

const statistics = reactive({
  totalComplaints: 0,
  pendingComplaints: 0,
  rectifyComplaints: 0,
  completedComplaints: 0,
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

const processOpen = ref(false);
const detailOpen = ref(false);
const currentComplaint = ref<ComplaintItem | null>(null);
const detailComplaint = ref<ComplaintItem | null>(null);
const processForm = reactive({
  status: 'pending_rectify',
  rectifyRequirement: '',
  rectifyResult: '',
  feedback: '',
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
      // fallback to comma split
    }
  }

  return normalizeList(source.split(','));
};

const evidenceImages = computed(() => parseEvidenceUrls(currentComplaint.value?.evidenceUrls));
const detailEvidenceImages = computed(() => parseEvidenceUrls(detailComplaint.value?.evidenceUrls));

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

const getProgressText = (record?: ComplaintItem | null) => {
  if (!record) return '-';
  if (record.status === 'pending_review') return '待监督员处理';
  if (record.status === 'pending_rectify') return record.rectifyRequirement || '已通知商户整改';
  if (record.status === 'rectified') return record.rectifyResult || '商户已提交整改结果，待监督员复核';
  if (record.status === 'completed') return record.feedback || record.rectifyResult || '投诉已处理完成';
  if (record.status === 'rejected') return record.feedback || '投诉已驳回';
  return record.processProgress || '-';
};

const handleSearch = () => {
  pagination.current = 1;
  refreshList();
};

const refreshList = async () => {
  loading.value = true;
  try {
    const res = await listComplaints({
      current: pagination.current,
      size: pagination.pageSize,
      status: status.value || undefined,
      keyword: keyword.value || undefined
    });
    const page = res.data || res;
    tableData.value = page.records || [];
    pagination.total = page.total || 0;

    statistics.totalComplaints = page.total || 0;
    statistics.pendingComplaints = tableData.value.filter(c => c.status === 'pending_review').length;
    statistics.rectifyComplaints = tableData.value.filter(c => c.status === 'pending_rectify').length;
    statistics.completedComplaints = tableData.value.filter(c => c.status === 'completed').length;
  } catch (error: any) {
    message.error(error?.message || '加载投诉失败');
  } finally {
    loading.value = false;
  }
};

const loadRanking = async () => {
  try {
    const res = await getComplaintRanking(10);
    if (res.code === 0) {
      rankingData.value = (res.data || []).map((item, index) => ({ ...item, rank: index + 1 }));
    }
  } catch (error) {
    console.error('加载投诉排行榜失败', error);
  }
};

const openProcess = (record: ComplaintItem) => {
  currentComplaint.value = record;
  processForm.status = record.status === 'rectified' ? 'completed' : 'pending_rectify';
  processForm.rectifyRequirement = record.rectifyRequirement || '';
  processForm.rectifyResult = record.rectifyResult || '';
  processForm.feedback = record.feedback || '';
  processOpen.value = true;
};

const viewDetail = (record: ComplaintItem) => {
  detailComplaint.value = record;
  detailOpen.value = true;
};

const submitProcess = async () => {
  if (!currentComplaint.value || !currentComplaint.value.id) {
    message.warning('请先选择投诉');
    return;
  }
  if (processForm.status === 'pending_rectify' && !processForm.rectifyRequirement.trim()) {
    message.warning('下达整改时必须填写整改要求');
    return;
  }
  submitting.value = true;
  try {
    await processComplaint({
      complaintId: currentComplaint.value.id,
      status: processForm.status,
      processProgress: processForm.status === 'pending_rectify'
        ? (processForm.rectifyRequirement || '已通知商户整改')
        : (processForm.feedback || processForm.rectifyResult || '投诉已处理完成'),
      rectifyRequirement: processForm.rectifyRequirement || undefined,
      rectifyResult: processForm.rectifyResult || undefined,
      feedback: processForm.feedback || undefined,
    });
    message.success('处理成功');
    processOpen.value = false;
    await refreshList();
    await loadRanking();
  } catch (error: any) {
    message.error(error?.message || '处理失败');
  } finally {
    submitting.value = false;
  }
};

const exportData = async () => {
  exporting.value = true;
  try {
    const blob = await exportComplaints({ status: status.value });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    const filename = `投诉记录_${new Date().toISOString().slice(0, 10)}.xlsx`;
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

onMounted(() => {
  refreshList();
  loadRanking();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
