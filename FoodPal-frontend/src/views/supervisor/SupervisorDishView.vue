<template>
  <div class="page-container">
    <a-page-header title="菜品监管" sub-title="监督商户菜品信息、供应状态与特价活动" @back="() => $router.back()" />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索菜品名称或商户"
            allow-clear
            @search="handleSearch"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="4">
          <a-select v-model:value="category" placeholder="分类筛选" allow-clear @change="handleSearch" style="width: 100%">
            <a-select-option value="主食">主食</a-select-option>
            <a-select-option value="面食">面食</a-select-option>
            <a-select-option value="汤类">汤类</a-select-option>
            <a-select-option value="凉菜">凉菜</a-select-option>
            <a-select-option value="小吃">小吃</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="4">
          <a-select v-model:value="supplyStatus" placeholder="供应状态" allow-clear @change="handleSearch" style="width: 100%">
            <a-select-option :value="1">可供应</a-select-option>
            <a-select-option :value="0">不可供应</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="4">
          <a-select v-model:value="specialFilter" placeholder="特价筛选" allow-clear @change="handleSearch" style="width: 100%">
            <a-select-option value="active">特价中</a-select-option>
            <a-select-option value="expired">已过期</a-select-option>
            <a-select-option value="none">无特价</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="24" :md="6">
          <a-space>
            <a-button @click="refreshList">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :xs="12" :sm="6">
          <a-statistic title="监管菜品数" :value="statistics.totalDishes" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="可供应" :value="statistics.availableDishes" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="特价中" :value="statistics.specialDishes" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="互动总量" :value="statistics.totalInteractions" />
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: DishItem) => row.id as number"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'dishImage'">
            <a-image
              v-if="record.dishImage"
              :src="resolveDishImageForAdmin(record.dishImage)"
              :width="56"
              :height="56"
              :preview="true"
              style="object-fit: cover; border-radius: 8px;"
            />
            <a-avatar v-else :size="56" style="background-color: #87d068">
              {{ record.dishName ? record.dishName.charAt(0).toUpperCase() : 'D' }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'dishName'">
            <div>
              <div style="font-weight: 500">{{ record.dishName }}</div>
              <div style="font-size: 12px; color: #999">{{ record.category || '-' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'merchantName'">
            <div>{{ record.merchantName || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'price'">
            <div>
              <div>原价：¥{{ record.dishPrice }}</div>
              <div v-if="record.specialPrice" style="margin-top: 4px;">
                <a-tag :color="record.isSpecial ? 'red' : 'orange'">
                  {{ record.isSpecial ? '当前特价' : '历史特价' }} ¥{{ record.specialPrice }}
                </a-tag>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'supplyStatus'">
            <a-tag :color="record.supplyStatus === 1 ? 'green' : 'orange'">
              {{ record.supplyStatus === 1 ? '可供应' : '不可供应' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'interactions'">
            <div style="font-size: 12px; line-height: 1.8;">
              <div>点赞 {{ record.likeCount || 0 }}</div>
              <div>收藏 {{ record.favoriteCount || 0 }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'specialPeriod'">
            <div style="font-size: 12px; line-height: 1.8;">
              <div>{{ record.specialStartTime || '-' }}</div>
              <div>{{ record.specialEndTime || '-' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openDetail(record)">
              <template #icon><EyeOutlined /></template>
              查看详情
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="detailOpen" title="菜品监管详情" :footer="null" width="760px">
      <a-descriptions bordered :column="2" v-if="currentDish">
        <a-descriptions-item label="菜品名称">{{ currentDish.dishName }}</a-descriptions-item>
        <a-descriptions-item label="所属商户">{{ currentDish.merchantName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="分类">{{ currentDish.category || '-' }}</a-descriptions-item>
        <a-descriptions-item label="供应状态">
          <a-tag :color="currentDish.supplyStatus === 1 ? 'green' : 'orange'">
            {{ currentDish.supplyStatus === 1 ? '可供应' : '不可供应' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="原价">¥{{ currentDish.dishPrice }}</a-descriptions-item>
        <a-descriptions-item label="当前价格">¥{{ currentDish.currentPrice ?? currentDish.dishPrice }}</a-descriptions-item>
        <a-descriptions-item label="特价价格">{{ currentDish.specialPrice ? `¥${currentDish.specialPrice}` : '-' }}</a-descriptions-item>
        <a-descriptions-item label="是否特价中">
          <a-tag :color="currentDish.isSpecial ? 'red' : 'default'">{{ currentDish.isSpecial ? '是' : '否' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="特价开始">{{ currentDish.specialStartTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="特价结束">{{ currentDish.specialEndTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="点赞数">{{ currentDish.likeCount || 0 }}</a-descriptions-item>
        <a-descriptions-item label="收藏数">{{ currentDish.favoriteCount || 0 }}</a-descriptions-item>
        <a-descriptions-item label="食材组成" :span="2">{{ currentDish.ingredients || '-' }}</a-descriptions-item>
        <a-descriptions-item label="营养成分" :span="2">{{ currentDish.nutritionInfo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="菜品图片" :span="2">
          <a-image
            v-if="currentDish.dishImage"
            :src="resolveDishImageForAdmin(currentDish.dishImage)"
            :width="180"
            :preview="true"
          />
          <span v-else style="color: #999">暂无图片</span>
        </a-descriptions-item>
        <a-descriptions-item label="更新时间" :span="2">{{ currentDish.updateTime || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import { ReloadOutlined, EyeOutlined } from '@ant-design/icons-vue';
import { listDishes, type DishItem } from '../../api';
import { resolveDishImageForAdmin } from '../../utils/image';

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '图片', key: 'dishImage', width: 90 },
  { title: '菜品', key: 'dishName', width: 180 },
  { title: '商户', dataIndex: 'merchantName', key: 'merchantName', width: 180 },
  { title: '价格', key: 'price', width: 160 },
  { title: '供应状态', dataIndex: 'supplyStatus', key: 'supplyStatus', width: 110 },
  { title: '互动', key: 'interactions', width: 100 },
  { title: '特价周期', key: 'specialPeriod', width: 220 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' as const },
];

const loading = ref(false);
const keyword = ref('');
const category = ref<string | undefined>(undefined);
const supplyStatus = ref<number | undefined>(undefined);
const specialFilter = ref<string | undefined>(undefined);
const tableData = ref<DishItem[]>([]);
const detailOpen = ref(false);
const currentDish = ref<DishItem | null>(null);

const statistics = reactive({
  totalDishes: 0,
  availableDishes: 0,
  specialDishes: 0,
  totalInteractions: 0,
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

const handleSearch = () => {
  pagination.current = 1;
  refreshList();
};

const applySpecialFilter = (records: DishItem[]) => {
  const now = new Date();
  if (specialFilter.value === 'active') {
    return records.filter(item => item.isSpecial);
  }
  if (specialFilter.value === 'expired') {
    return records.filter(item => item.specialPrice && item.specialEndTime && new Date(item.specialEndTime) <= now);
  }
  if (specialFilter.value === 'none') {
    return records.filter(item => !item.specialPrice);
  }
  return records;
};

const refreshList = async () => {
  loading.value = true;
  try {
    const res = await listDishes({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      supplyStatus: supplyStatus.value,
    });
    const page = res.data || res;
    const records = applySpecialFilter(page.records || []);
    tableData.value = records;
    pagination.total = page.total || 0;

    statistics.totalDishes = page.total || 0;
    statistics.availableDishes = records.filter(item => item.supplyStatus === 1).length;
    statistics.specialDishes = records.filter(item => item.isSpecial).length;
    statistics.totalInteractions = records.reduce((sum, item) => sum + (item.likeCount || 0) + (item.favoriteCount || 0), 0);
  } catch (error: any) {
    message.error(error?.message || '加载菜品失败');
  } finally {
    loading.value = false;
  }
};

const openDetail = (record: DishItem) => {
  currentDish.value = record;
  detailOpen.value = true;
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
</style>
