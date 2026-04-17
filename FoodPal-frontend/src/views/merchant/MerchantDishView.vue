<template>
  <div class="page-container">
    <a-page-header title="菜品管理" sub-title="管理店铺菜品" />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-row :gutter="12" style="margin-bottom: 16px">
        <a-col :xs="24" :sm="8" :md="6">
          <a-input-search v-model:value="keyword" placeholder="搜索菜品名称" allow-clear @search="refreshList" />
        </a-col>
        <a-col :xs="24" :sm="8" :md="6">
          <a-button type="primary" @click="openCreate">
            <template #icon><plus-outlined /></template>
            新增菜品
          </a-button>
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="(row: any) => row.id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'dishImage'">
            <a-image
              v-if="record.dishImage"
              :src="record.dishImage"
              :width="60"
              :height="60"
              style="object-fit: cover; border-radius: 4px;"
            />
          </template>
          <template v-else-if="column.key === 'price'">
            <div>
              <span>¥{{ record.dishPrice }}</span>
              <div v-if="record.specialPrice" style="margin-top: 4px;">
                <a-tag color="red" size="small">特价 ¥{{ record.specialPrice }}</a-tag>
              </div>
            </div>
          </template>
          <template v-else-if="column.key === 'supplyStatus'">
            <a-tag :color="record.supplyStatus === 1 ? 'green' : 'orange'">
              {{ record.supplyStatus === 1 ? '可供应' : '不可供应' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
              <a-button type="link" size="small" @click="toggleSupply(record)">
                {{ record.supplyStatus === 1 ? '停止供应' : '恢复供应' }}
              </a-button>
              <a-popconfirm title="确定删除该菜品吗？" @confirm="handleDelete(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 编辑模态框 -->
    <a-modal v-model:open="modalOpen" :title="form.id ? '编辑菜品' : '新增菜品'" width="600px" @ok="submit" :confirm-loading="submitting">
      <a-form layout="vertical">
        <a-form-item label="菜品名称" required>
          <a-input v-model:value="form.dishName" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="价格" required>
              <a-input-number v-model:value="form.dishPrice" :min="0" :step="0.1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="分类">
              <a-input v-model:value="form.category" placeholder="如：主食、面食" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="供应状态">
          <a-select v-model:value="form.supplyStatus">
            <a-select-option :value="1">可供应</a-select-option>
            <a-select-option :value="0">不可供应</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { listDishes, saveDish, updateDish, deleteDish } from '@/api/modules/canteen';

const columns = [
  { title: '图片', key: 'dishImage', width: 80 },
  { title: '菜品名称', dataIndex: 'dishName', key: 'dishName' },
  { title: '分类', dataIndex: 'category', key: 'category' },
  { title: '价格', key: 'price' },
  { title: '供应状态', key: 'supplyStatus' },
  { title: '操作', key: 'action', width: 250 },
];

const tableData = ref<any[]>([]);
const loading = ref(false);
const pagination = ref({ current: 1, pageSize: 10, total: 0 });
const keyword = ref('');
const modalOpen = ref(false);
const submitting = ref(false);
const form = ref<any>({});

const refreshList = async () => {
  loading.value = true;
  try {
    const res = await listDishes({
      current: pagination.value.current,
      size: pagination.value.pageSize,
      keyword: keyword.value || undefined,
    });
    const page = res.data || res;
    tableData.value = page.records || [];
    pagination.value.total = page.total || 0;
  } catch (error: any) {
    message.error(error?.message || '加载失败');
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  form.value = { supplyStatus: 1 };
  modalOpen.value = true;
};

const openEdit = (record: any) => {
  form.value = { ...record };
  modalOpen.value = true;
};

const submit = async () => {
  if (!form.value.dishName?.trim()) {
    message.warning('请输入菜品名称');
    return;
  }
  if (!form.value.dishPrice || form.value.dishPrice <= 0) {
    message.warning('请输入有效价格');
    return;
  }

  submitting.value = true;
  try {
    if (form.value.id) {
      await updateDish(form.value);
    } else {
      await saveDish(form.value);
    }
    message.success('保存成功');
    modalOpen.value = false;
    await refreshList();
  } catch (error: any) {
    message.error(error?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};

const toggleSupply = async (record: any) => {
  try {
    await updateDish({
      id: record.id,
      supplyStatus: record.supplyStatus === 1 ? 0 : 1
    });
    message.success('更新成功');
    await refreshList();
  } catch (error: any) {
    message.error(error?.message || '更新失败');
  }
};

const handleDelete = async (record: any) => {
  try {
    await deleteDish(record.id);
    message.success('删除成功');
    await refreshList();
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  refreshList();
};

onMounted(refreshList);
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
