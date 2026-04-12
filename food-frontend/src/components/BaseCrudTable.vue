<template>
  <div>
    <h2>{{ title }}</h2>
    
    <!-- 搜索区域 -->
    <div class="search-container">
      <a-form layout="inline" :model="searchForm">
        <slot name="searchFields"></slot>
        <a-form-item>
          <a-button type="primary" @click="fetchList">搜索</a-button>
          <a-button style="margin-left: 8px" type="primary" @click="showAddModal">添加</a-button>
          <a-button style="margin-left: 8px" @click="resetSearch">重置</a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="selectedRowKeys.length" class="batch-actions">
      <span>已选 {{ selectedRowKeys.length }} 项:</span>
      <a-button
        type="primary"
        danger
        size="small"
        @click="handleBatchDelete"
        style="margin-left: 12px"
      >
        批量删除
      </a-button>
      <a-button size="small" @click="clearSelection" style="margin-left: 8px">
        取消选择
      </a-button>
    </div>

    <!-- 表格 -->
    <a-table
      :dataSource="dataList"
      :columns="columns"
      :row-selection="rowSelection"
      rowKey="id"
      bordered
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <slot name="bodyCell" :column="column" :record="record"></slot>
        <template v-if="column.key === 'action'">
          <a-button type="link" @click="showEditModal(record)">编辑</a-button>
          <a-button type="link" danger @click="confirmDelete(record.id)">
            删除
          </a-button>
        </template>
      </template>
    </a-table>

    <!-- 添加/编辑弹窗 -->
    <a-modal
      v-model:visible="isModalVisible"
      :title="isEdit ? '编辑' : '添加'"
      @ok="handleSubmit"
      @cancel="resetForm"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <slot name="formFields" :formData="formData" :isEdit="isEdit"></slot>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';

interface Props {
  title: string;
  columns: any[];
  api: any;
  defaultSearchForm?: any;
  defaultFormData?: any;
}

const props = withDefaults(defineProps<Props>(), {
  defaultSearchForm: () => ({}),
  defaultFormData: () => ({}),
});

const emit = defineEmits(['add', 'edit', 'delete']);

// 响应式数据
const selectedRowKeys = ref<number[]>([]);
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
});

const searchForm = reactive({ ...props.defaultSearchForm });
const formData = ref({ ...props.defaultFormData });
const dataList = ref([]);
const isModalVisible = ref(false);
const isEdit = ref(false);

// 计算属性
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => (selectedRowKeys.value = keys),
  selections: [
    {
      key: 'all',
      text: '全选当页',
      onSelect: () => {
        selectedRowKeys.value = dataList.value.map((item) => item.id);
      },
    },
  ],
}));

// 方法
const showAddModal = () => {
  isEdit.value = false;
  formData.value = { ...props.defaultFormData };
  isModalVisible.value = true;
};

const showEditModal = (record) => {
  isEdit.value = true;
  formData.value = { ...record };
  isModalVisible.value = true;
};

const fetchList = async (resetPage = true) => {
  try {
    if (resetPage) pagination.current = 1;
    
    const params = {
      ...searchForm,
      current: pagination.current,
      pageSize: pagination.pageSize,
    };

    const res = await props.api.listByPage(params);
    if (res.code === 0) {
      dataList.value = res.data.records;
      pagination.total = res.data.total;
    } else {
      message.error(res.message);
    }
  } catch (error) {
    message.error('数据加载失败');
  }
};

const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchList(false);
};

const resetSearch = () => {
  Object.assign(searchForm, props.defaultSearchForm);
  fetchList();
};

const clearSelection = () => {
  selectedRowKeys.value = [];
};

const confirmDelete = async (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除吗？',
    onOk: () => deleteItem(id),
  });
};

const deleteItem = async (id: number) => {
  try {
    const res = await props.api.delete({ id });
    if (res.code === 0) {
      message.success('删除成功');
      emit('delete', id);
      fetchList();
    } else {
      message.error(res.message);
    }
  } catch (error) {
    message.error('删除失败');
  }
};

const handleBatchDelete = async () => {
  Modal.confirm({
    title: '批量删除确认',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个项目吗？`,
    onOk: async () => {
      try {
        for (const id of selectedRowKeys.value) {
          await props.api.delete({ id });
        }
        message.success(`成功删除 ${selectedRowKeys.value.length} 条记录`);
        clearSelection();
        fetchList();
      } catch (error) {
        message.error('批量删除失败');
      }
    },
  });
};

const handleSubmit = async () => {
  try {
    let res;
    if (isEdit.value) {
      res = await props.api.update(formData.value);
      emit('edit', formData.value);
    } else {
      res = await props.api.add(formData.value);
      emit('add', formData.value);
    }
    
    if (res.code === 0) {
      message.success(isEdit.value ? '编辑成功' : '添加成功');
      resetForm();
      isModalVisible.value = false;
      fetchList();
    } else {
      message.error(res.message);
    }
  } catch (error) {
    message.error(isEdit.value ? '编辑失败' : '添加失败');
  }
};

const resetForm = () => {
  formData.value = { ...props.defaultFormData };
};

// 生命周期
onMounted(() => {
  fetchList();
});

defineExpose({
  fetchList,
  resetSearch,
});
</script>

<style scoped>
.search-container {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
}

.batch-actions {
  margin-bottom: 16px;
  padding: 8px 16px;
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 4px;
}

:deep(.ant-form-item) {
  margin-bottom: 16px;
}

:deep(.ant-table-selection-column) {
  width: 48px !important;
}
</style>