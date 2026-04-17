<template>
  <div class="page-container">
    <a-page-header title="菜品管理（旧页）" sub-title="该页面已不再提供给管理员使用，请改走监督员菜品监管" @back="() => $router.back()" />

    <a-card :bordered="false" style="margin-top: 16px">
      <!-- 搜索和筛选 -->
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="8" :md="5">
          <a-input-search v-model:value="keyword" placeholder="搜索菜品名称或商户" allow-clear @search="refreshList" />
        </a-col>
        <a-col :xs="24" :sm="8" :md="4">
          <a-select v-model:value="category" placeholder="分类筛选" allow-clear @change="refreshList" style="width: 100%">
            <a-select-option value="主食">主食</a-select-option>
            <a-select-option value="面食">面食</a-select-option>
            <a-select-option value="汤类">汤类</a-select-option>
            <a-select-option value="凉菜">凉菜</a-select-option>
            <a-select-option value="小吃">小吃</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="8" :md="4">
          <a-select v-model:value="supplyStatus" placeholder="供应状态" allow-clear @change="refreshList" style="width: 100%">
            <a-select-option :value="1">可供应</a-select-option>
            <a-select-option :value="0">不可供应</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="8" :md="4">
          <a-select v-model:value="specialFilter" placeholder="特价筛选" allow-clear @change="refreshList" style="width: 100%">
            <a-select-option value="active">特价中</a-select-option>
            <a-select-option value="expired">已过期</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="8" :md="7">
          <a-space>
            <a-button type="primary" @click="openCreate">
              <template #icon><PlusOutlined /></template>
              新增菜品
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

      <!-- 统计信息 -->
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :xs="12" :sm="6">
          <a-statistic title="总菜品数" :value="statistics.totalDishes" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="可供应" :value="statistics.availableDishes" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="特价中" :value="statistics.specialDishes" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="总点赞" :value="statistics.totalLikes" />
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: DishItem) => row.id as number"
        @change="handleTableChange"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'dishImage'">
            <a-image
              v-if="record.dishImage"
              :src="resolveDishImageForAdmin(record.dishImage)"
              :width="60"
              :height="60"
              :preview="true"
              style="object-fit: cover; border-radius: 4px;"
            />
            <a-avatar v-else :size="60" style="background-color: #87d068">
              {{ record.dishName ? record.dishName.charAt(0).toUpperCase() : 'D' }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'dishName'">
            <div>
              <div style="font-weight: 500">{{ record.dishName }}</div>
              <div style="font-size: 12px; color: #999">{{ record.category || '-' }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'merchant'">
            <div style="font-size: 12px;">{{ record.merchantName || '-' }}</div>
          </template>
          <template v-else-if="column.key === 'price'">
            <div>
              <span>¥{{ record.dishPrice }}</span>
              <template v-if="record.specialPrice">
                <div style="margin-top: 4px;">
                  <a-tag :color="record.isSpecial ? 'red' : 'orange'" size="small">
                    {{ record.isSpecial ? '特价' : '待生效' }} ¥{{ record.specialPrice }}
                  </a-tag>
                </div>
                <div v-if="!record.isSpecial && record.specialStartTime" style="font-size: 11px; color: #999; margin-top: 2px;">
                  {{ new Date(record.specialStartTime) > new Date() ? '未开始' : '已过期' }}
                </div>
              </template>
            </div>
          </template>
          <template v-else-if="column.key === 'stats'">
            <div style="font-size: 12px;">
              <div><LikeOutlined style="margin-right: 4px;" />{{ record.likeCount || 0 }}</div>
              <div><StarOutlined style="margin-right: 4px;" />{{ record.favoriteCount || 0 }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'supplyStatus'">
            <a-tag :color="record.supplyStatus === 1 ? 'green' : 'orange'">
              {{ record.supplyStatus === 1 ? '可供应' : '不可供应' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEdit(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button v-if="!record.specialPrice" type="link" size="small" @click="openSpecialModal(record)">
                设置特价
              </a-button>
              <a-popconfirm v-else title="确定取消特价吗？" @confirm="handleCancelSpecial(record)">
                <a-button type="link" size="small">
                  取消特价
                </a-button>
              </a-popconfirm>
              <a-popconfirm title="确定删除该菜品吗？" @confirm="handleDelete(record)">
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

    <a-modal v-model:open="modalOpen" :title="form.id ? '编辑菜品' : '新增菜品'" width="780px" @ok="submit" :confirm-loading="submitting">
      <a-form layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="商户ID" required>
              <a-input-number v-model:value="form.merchantId" style="width: 100%" :min="1" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="菜品名称" required>
              <a-input v-model:value="form.dishName" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="原价" required>
              <a-input-number v-model:value="form.dishPrice" :min="0" :step="0.1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="分类">
              <a-input v-model:value="form.category" placeholder="如：主食、面食" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="供应状态">
              <a-select v-model:value="form.supplyStatus">
                <a-select-option :value="1">可供应</a-select-option>
                <a-select-option :value="0">不可供应</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="菜品图片">
          <ImageUpload v-model="form.dishImage" type="dish" />
        </a-form-item>

        <a-divider>特价设置</a-divider>

        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item label="特价价格">
              <a-input-number v-model:value="form.specialPrice" :min="0" :step="0.1" style="width: 100%" placeholder="不填则不设置特价" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="特价开始">
              <a-date-picker v-model:value="specialStart" show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="特价结束">
              <a-date-picker v-model:value="specialEnd" show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="食材组成">
              <a-textarea v-model:value="form.ingredients" :rows="2" placeholder="如：猪肉、白菜、豆腐" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="营养成分">
              <a-textarea v-model:value="form.nutritionInfo" :rows="2" placeholder="如：蛋白质、碳水化合物" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 特价设置模态框 -->
    <a-modal 
      v-model:open="specialModalOpen" 
      title="设置特价活动" 
      width="600px" 
      @ok="submitSpecial" 
      :confirm-loading="specialSubmitting"
    >
      <a-form layout="vertical">
        <a-form-item label="菜品名称">
          <a-input v-model:value="specialForm.dishName" disabled />
        </a-form-item>
        <a-form-item label="原价">
          <a-input-number v-model:value="specialForm.dishPrice" disabled style="width: 100%" />
        </a-form-item>
        <a-form-item label="特价价格" required>
          <a-input-number 
            v-model:value="specialForm.specialPrice" 
            :min="0.01" 
            :max="specialForm.dishPrice - 0.01" 
            :step="0.1" 
            style="width: 100%" 
            placeholder="必须低于原价"
          />
          <div style="color: #999; font-size: 12px; margin-top: 4px;">
            折扣: {{ ((specialForm.specialPrice / specialForm.dishPrice) * 10).toFixed(1) }}折
          </div>
        </a-form-item>
        <a-form-item label="特价开始时间" required>
          <a-date-picker 
            v-model:value="specialForm.specialStartTime" 
            show-time 
            value-format="YYYY-MM-DD HH:mm:ss" 
            style="width: 100%" 
            placeholder="选择开始时间"
          />
        </a-form-item>
        <a-form-item label="特价结束时间" required>
          <a-date-picker 
            v-model:value="specialForm.specialEndTime" 
            show-time 
            value-format="YYYY-MM-DD HH:mm:ss" 
            style="width: 100%" 
            placeholder="选择结束时间"
          />
        </a-form-item>
        <a-form-item label="限购数量">
          <a-input-number 
            v-model:value="specialForm.purchaseLimit" 
            :min="1" 
            style="width: 100%" 
            placeholder="每人限购数量，不填则不限购"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  DeleteOutlined,
  ReloadOutlined,
  EditOutlined,
  DollarOutlined,
  LikeOutlined,
  StarOutlined
} from '@ant-design/icons-vue';
import ImageUpload from '../../components/ImageUpload.vue';
import { resolveDishImageForAdmin } from '../../utils/image';
import { 
  deleteDish, 
  listDishes, 
  saveDish, 
  updateDish, 
  setSpecialDish,
  cancelSpecialDish,
  type DishItem 
} from '../../api';

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80, fixed: 'left' },
  { title: '图片', key: 'dishImage', width: 100, fixed: 'left' },
  { title: '菜品名称', key: 'dishName', width: 200 },
  { title: '商户', key: 'merchant', width: 120 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 100 },
  { title: '价格', key: 'price', width: 120 },
  { title: '互动', key: 'stats', width: 80 },
  { title: '供应', dataIndex: 'supplyStatus', key: 'supplyStatus', width: 100 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 160 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
];

const loading = ref(false);
const submitting = ref(false);
const keyword = ref('');
const category = ref<string | undefined>(undefined);
const supplyStatus = ref<number | undefined>(undefined);
const specialFilter = ref<string | undefined>(undefined);
const tableData = ref<DishItem[]>([]);
const modalOpen = ref(false);
const specialStart = ref<string | null>(null);
const specialEnd = ref<string | null>(null);
const selectedRowKeys = ref<number[]>([]);
const specialModalOpen = ref(false);
const specialSubmitting = ref(false);

const specialForm = reactive({
  dishId: 0,
  dishName: '',
  dishPrice: 0,
  specialPrice: 0,
  specialStartTime: '',
  specialEndTime: '',
  purchaseLimit: undefined as number | undefined,
});

const statistics = reactive({
  totalDishes: 0,
  availableDishes: 0,
  specialDishes: 0,
  totalLikes: 0,
  totalFavorites: 0,
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

const form = reactive<DishItem>({
  id: undefined,
  merchantId: undefined,
  dishName: '',
  dishPrice: 0,
  category: '',
  ingredients: '',
  nutritionInfo: '',
  dishImage: '',
  supplyStatus: 1,
  specialPrice: undefined
});

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys;
  },
}));

const resetForm = () => {
  form.id = undefined;
  form.merchantId = undefined;
  form.dishName = '';
  form.dishPrice = 0;
  form.category = '';
  form.ingredients = '';
  form.nutritionInfo = '';
  form.dishImage = '';
  form.supplyStatus = 1;
  form.specialPrice = undefined;
  specialStart.value = null;
  specialEnd.value = null;
};

const refreshList = async () => {
  loading.value = true;
  try {
    const now = new Date();
    const res = await listDishes({
      current: pagination.current,
      size: pagination.pageSize,
      keyword: keyword.value || undefined,
      category: category.value || undefined,
      supplyStatus: supplyStatus.value
    });
    const page = res.data || res;
    
    let dishes = page.records || [];
    
    // 特价筛选
    if (specialFilter.value === 'active') {
      dishes = dishes.filter(d => d.isSpecial && new Date(d.specialEndTime || '') > now);
    } else if (specialFilter.value === 'expired') {
      dishes = dishes.filter(d => d.isSpecial && new Date(d.specialEndTime || '') <= now);
    }
    
    tableData.value = dishes;
    pagination.total = page.total || 0;
    
    // 更新统计信息
    statistics.totalDishes = page.total || 0;
    statistics.availableDishes = dishes.filter(d => d.supplyStatus === 1).length;
    statistics.specialDishes = dishes.filter(d => d.isSpecial).length;
    statistics.totalLikes = dishes.reduce((sum, d) => sum + (d.likeCount || 0), 0);
    statistics.totalFavorites = dishes.reduce((sum, d) => sum + (d.favoriteCount || 0), 0);
  } catch (error: any) {
    message.error(error?.message || '加载菜品失败');
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  resetForm();
  modalOpen.value = true;
};

const openEdit = (record: DishItem) => {
  Object.assign(form, record);
  specialStart.value = record.specialStartTime || null;
  specialEnd.value = record.specialEndTime || null;
  modalOpen.value = true;
};

const submit = async () => {
  if (!form.merchantId || !form.dishName) {
    message.warning('请至少填写商户ID和菜品名称');
    return;
  }
  submitting.value = true;
  try {
    const payload: DishItem = {
      ...form,
      specialStartTime: specialStart.value || undefined,
      specialEndTime: specialEnd.value || undefined
    };

    if (form.id) {
      await updateDish(payload);
      message.success('菜品更新成功');
    } else {
      await saveDish(payload);
      message.success('菜品创建成功');
    }
    modalOpen.value = false;
    await refreshList();
  } catch (error: any) {
    message.error(error?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};

const handleDelete = async (record: DishItem) => {
  if (!record.id) return;
  try {
    await deleteDish(record.id);
    message.success('删除成功');
    await refreshList();
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的菜品');
    return;
  }

  try {
    let successCount = 0;
    for (const id of selectedRowKeys.value) {
      try {
        await deleteDish(id);
        successCount++;
      } catch (error) {
        console.error(`删除菜品 ${id} 失败`, error);
      }
    }
    if (successCount > 0) {
      message.success(`成功删除 ${successCount} 个菜品`);
      selectedRowKeys.value = [];
      await refreshList();
      return;
    }
    message.error('批量删除失败，请稍后重试');
  } catch (error: any) {
    message.error('批量删除失败');
  }
};

const openSpecialModal = (record: DishItem) => {
  specialForm.dishId = record.id!;
  specialForm.dishName = record.dishName || '';
  specialForm.dishPrice = record.dishPrice || 0;
  specialForm.specialPrice = 0;
  specialForm.specialStartTime = '';
  specialForm.specialEndTime = '';
  specialForm.purchaseLimit = undefined;
  specialModalOpen.value = true;
};

const submitSpecial = async () => {
  if (!specialForm.specialPrice || specialForm.specialPrice <= 0) {
    message.warning('请输入有效的特价价格');
    return;
  }
  if (specialForm.specialPrice >= specialForm.dishPrice) {
    message.warning('特价价格必须低于原价');
    return;
  }
  if (!specialForm.specialStartTime || !specialForm.specialEndTime) {
    message.warning('请选择特价开始和结束时间');
    return;
  }
  if (specialForm.specialStartTime >= specialForm.specialEndTime) {
    message.warning('特价开始时间必须早于结束时间');
    return;
  }

  try {
    specialSubmitting.value = true;
    await setSpecialDish({
      dishId: specialForm.dishId,
      specialPrice: specialForm.specialPrice,
      specialStartTime: specialForm.specialStartTime,
      specialEndTime: specialForm.specialEndTime,
      purchaseLimit: specialForm.purchaseLimit,
    });
    message.success('特价设置成功');
    specialModalOpen.value = false;
    await refreshList();
  } catch (error: any) {
    message.error(error?.message || '特价设置失败');
  } finally {
    specialSubmitting.value = false;
  }
};

const handleCancelSpecial = async (record: DishItem) => {
  if (!record.id) return;
  try {
    await cancelSpecialDish(record.id);
    message.success('特价已取消');
    await refreshList();
  } catch (error: any) {
    message.error(error?.message || '取消特价失败');
  }
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
