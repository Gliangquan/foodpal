<template>
  <div class="page-container">
    <a-page-header
      title="学生管理"
      sub-title="仅管理学生账号信息"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 24px">
      <a-row :gutter="16" style="margin-bottom: 24px">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input-search
            v-model:value="keyword"
            placeholder="搜索账号、昵称或手机号"
            @search="handleSearch"
            allow-clear
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="4">
          <a-select
            v-model:value="statusFilter"
            placeholder="状态筛选"
            allow-clear
            @change="handleSearch"
            style="width: 100%"
          >
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="24" :md="14">
          <a-space>
            <a-button type="primary" @click="showAddModal">
              <template #icon><PlusOutlined /></template>
              新增学生
            </a-button>
            <a-button @click="handleBatchDelete" danger :disabled="!selectedRowKeys.length">
              <template #icon><DeleteOutlined /></template>
              批量删除
            </a-button>
            <a-button @click="fetchData">
              <template #icon><ReloadOutlined /></template>
              刷新
            </a-button>
          </a-space>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col :xs="12" :sm="6">
          <a-statistic title="学生总数" :value="statistics.studentCount" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="启用中" :value="statistics.enabledCount" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="已禁用" :value="statistics.disabledCount" />
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-statistic title="在线统计" :value="statistics.activeUsers" />
        </a-col>
      </a-row>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1400 }"
        :row-key="record => record.id"
        :row-selection="rowSelection"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userAvatar'">
            <a-avatar
              v-if="record.userAvatar"
              :src="record.userAvatar"
              :size="48"
              style="cursor: pointer"
            />
            <a-avatar v-else :size="48" style="background-color: #87d068">
              {{ record.userName ? record.userName.charAt(0).toUpperCase() : 'S' }}
            </a-avatar>
          </template>
          <template v-else-if="column.key === 'userName'">
            <div>
              <div style="font-weight: 500">{{ record.userName || '-' }}</div>
              <div style="font-size: 12px; color: #999">{{ record.userAccount }}</div>
            </div>
          </template>
          <template v-else-if="column.key === 'userRole'">
            <a-tag color="blue">学生</a-tag>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ record.status === 1 ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="showDetailModal(record)">
                <template #icon><EyeOutlined /></template>
                详情
              </a-button>
              <a-button type="link" size="small" @click="showEditModal(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-popconfirm
                title="确定将该学生密码重置为 12345678 吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleResetPassword(record)"
              >
                <a-button type="link" size="small">重置密码</a-button>
              </a-popconfirm>
              <a-popconfirm
                title="确定删除该学生吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
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

    <a-modal
      v-model:visible="detailModalVisible"
      title="学生详情"
      :footer="null"
      width="700px"
    >
      <a-descriptions :column="2" bordered v-if="selectedUser">
        <a-descriptions-item label="用户ID">{{ selectedUser.id }}</a-descriptions-item>
        <a-descriptions-item label="账号">{{ selectedUser.userAccount }}</a-descriptions-item>
        <a-descriptions-item label="昵称">{{ selectedUser.userName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="角色"><a-tag color="blue">学生</a-tag></a-descriptions-item>
        <a-descriptions-item label="手机号">{{ selectedUser.userPhone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="邮箱">{{ selectedUser.userEmail || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="selectedUser.status === 1 ? 'green' : 'red'">
            {{ selectedUser.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="个人简介">{{ selectedUser.userProfile || '-' }}</a-descriptions-item>
        <a-descriptions-item label="注册时间" :span="2">{{ selectedUser.createTime || '-' }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <a-modal
      v-model:visible="editModalVisible"
      :title="isEdit ? '编辑学生' : '新增学生'"
      @ok="handleSubmit"
      width="650px"
      :confirm-loading="submitting"
    >
      <a-form :model="formData" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="账号" required>
              <a-input v-model:value="formData.userAccount" placeholder="请输入账号" :disabled="isEdit" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="昵称">
              <a-input v-model:value="formData.userName" placeholder="请输入昵称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="formData.status" placeholder="请选择状态">
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">禁用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="头像">
              <ImageUpload v-model="formData.userAvatar" type="user" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="手机号">
              <a-input v-model:value="formData.userPhone" placeholder="请输入11位手机号" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="邮箱">
              <a-input v-model:value="formData.userEmail" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="个人简介">
          <a-textarea v-model:value="formData.userProfile" :rows="3" placeholder="请输入个人简介" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { message } from 'ant-design-vue';
import {
  PlusOutlined,
  DeleteOutlined,
  ReloadOutlined,
  EyeOutlined,
  EditOutlined
} from '@ant-design/icons-vue';
import ImageUpload from '../../components/ImageUpload.vue';
import {
  listUserVOByPage,
  deleteUser,
  batchDeleteUser,
  addUser,
  updateUser,
  adminResetUserPassword,
  getUserStatistics
} from '../../api';

const PHONE_RE = /^1\d{10}$/;
const EMAIL_RE = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80, fixed: 'left' },
  { title: '头像', key: 'userAvatar', width: 100, fixed: 'left' },
  { title: '昵称', key: 'userName', width: 160 },
  { title: '角色', dataIndex: 'userRole', key: 'userRole', width: 100 },
  { title: '手机号', dataIndex: 'userPhone', key: 'userPhone', width: 160 },
  { title: '邮箱', dataIndex: 'userEmail', key: 'userEmail', width: 220 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '注册时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 280, fixed: 'right' },
];

const tableData = ref([]);
const loading = ref(false);
const keyword = ref('');
const statusFilter = ref<number | undefined>(undefined);
const detailModalVisible = ref(false);
const editModalVisible = ref(false);
const selectedUser = ref<any>(null);
const isEdit = ref(false);
const submitting = ref(false);
const selectedRowKeys = ref<number[]>([]);

const statistics = reactive({
  studentCount: 0,
  enabledCount: 0,
  disabledCount: 0,
  activeUsers: 0,
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

const formData = reactive({
  id: null as number | null,
  userAccount: '',
  userName: '',
  userAvatar: '',
  userPhone: '',
  userEmail: '',
  userRole: 'student',
  status: 1,
  userProfile: '',
});

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys;
  },
}));

const validateForm = () => {
  if (!formData.userAccount.trim()) {
    message.warning('请输入账号');
    return false;
  }
  if (formData.userPhone && !PHONE_RE.test(formData.userPhone.trim())) {
    message.warning('请输入有效手机号');
    return false;
  }
  if (formData.userEmail && !EMAIL_RE.test(formData.userEmail.trim())) {
    message.warning('请输入有效邮箱');
    return false;
  }
  return true;
};

const handleSearch = () => {
  pagination.current = 1;
  fetchData();
};

const fetchData = async () => {
  loading.value = true;
  try {
    const response = await listUserVOByPage({
      current: pagination.current,
      pageSize: pagination.pageSize,
      keyword: keyword.value || undefined,
      userRole: 'student',
      status: statusFilter.value,
    });

    if (response.code === 0) {
      tableData.value = response.data.records || [];
      pagination.total = response.data.total || 0;
    } else {
      message.error(response.message || '获取数据失败');
    }
  } catch (error) {
    message.error('获取数据失败');
  } finally {
    loading.value = false;
  }
};

const loadStatistics = async () => {
  try {
    const response = await getUserStatistics('student');
    if (response.code === 0) {
      statistics.studentCount = response.data.studentCount || 0;
      statistics.enabledCount = response.data.enabledCount || 0;
      statistics.disabledCount = response.data.disabledCount || 0;
      statistics.activeUsers = response.data.activeUsers || 0;
    }
  } catch (error) {
    console.error('获取统计数据失败', error);
  }
};

const showDetailModal = (record: any) => {
  selectedUser.value = record;
  detailModalVisible.value = true;
};

const resetForm = () => {
  formData.id = null;
  formData.userAccount = '';
  formData.userName = '';
  formData.userAvatar = '';
  formData.userPhone = '';
  formData.userEmail = '';
  formData.userRole = 'student';
  formData.status = 1;
  formData.userProfile = '';
};

const showAddModal = () => {
  isEdit.value = false;
  resetForm();
  editModalVisible.value = true;
};

const showEditModal = (record: any) => {
  isEdit.value = true;
  Object.assign(formData, {
    id: record.id,
    userAccount: record.userAccount || '',
    userName: record.userName || '',
    userAvatar: record.userAvatar || '',
    userPhone: record.userPhone || '',
    userEmail: record.userEmail || '',
    userRole: 'student',
    status: record.status ?? 1,
    userProfile: record.userProfile || '',
  });
  editModalVisible.value = true;
};

const handleSubmit = async () => {
  if (!validateForm()) {
    return;
  }

  submitting.value = true;
  try {
    if (isEdit.value && formData.id) {
      const response = await updateUser({
        id: formData.id,
        userName: formData.userName || undefined,
        userAvatar: formData.userAvatar || undefined,
        userPhone: formData.userPhone.trim() || undefined,
        userEmail: formData.userEmail.trim() || undefined,
        userRole: 'student',
        status: formData.status,
        userProfile: formData.userProfile || undefined,
      });
      if (response.code === 0) {
        message.success('编辑成功');
      }
    } else {
      const response = await addUser({
        userAccount: formData.userAccount.trim(),
        userName: formData.userName || undefined,
        userAvatar: formData.userAvatar || undefined,
        userPhone: formData.userPhone.trim() || undefined,
        userEmail: formData.userEmail.trim() || undefined,
        userRole: 'student',
        status: formData.status,
        userProfile: formData.userProfile || undefined,
      });
      if (response.code === 0) {
        message.success('新增成功');
      }
    }
    editModalVisible.value = false;
    await fetchData();
    await loadStatistics();
  } catch (error: any) {
    message.error(error?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};

const handleResetPassword = async (record: any) => {
  try {
    const response = await adminResetUserPassword({ id: record.id });
    if (response.code === 0) {
      message.success(`已将 ${record.userName || record.userAccount} 的密码重置为 12345678`);
    }
  } catch (error: any) {
    message.error(error?.message || '重置密码失败');
  }
};

const handleDelete = async (record: any) => {
  try {
    const response = await deleteUser({ id: record.id });
    if (response.code === 0) {
      message.success(`删除学生 ${record.userAccount} 成功`);
      await fetchData();
      await loadStatistics();
    }
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的学生');
    return;
  }

  try {
    const response = await batchDeleteUser({ ids: selectedRowKeys.value, softDelete: true });
    if (response.code === 0) {
      message.success(`批量删除 ${response.data} 个学生成功`);
      selectedRowKeys.value = [];
      await fetchData();
      await loadStatistics();
    }
  } catch (error: any) {
    message.error(error?.message || '批量删除失败');
  }
};

const handleTableChange = (pag: any) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  fetchData();
};

onMounted(() => {
  fetchData();
  loadStatistics();
});
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
