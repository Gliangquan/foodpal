<template>
  <div class="profile-container">
    <a-page-header
      class="theme-page-header"
      title="个人中心"
      sub-title="管理您的个人信息"
      @back="() => $router.back()"
    />

    <a-row class="profile-grid" :gutter="24">
      <a-col :xs="24" :sm="24" :md="8">
        <a-card :bordered="false" class="profile-card themed-card">
          <div class="profile-card-top">
            <div class="avatar-section">
              <div class="avatar-halo">
                <a-avatar
                  :size="120"
                  :src="userInfo.userAvatar"
                  style="background-color: #1890ff"
                >
                  <template #icon>
                    <user-outlined />
                  </template>
                </a-avatar>
              </div>
              <div class="user-basic">
                <h2>{{ userInfo.userName || '未设置昵称' }}</h2>
                <p class="role-tag">
                  <a-tag :color="getRoleColor(userInfo.userRole)">
                    {{ getRoleLabel(userInfo.userRole) }}
                  </a-tag>
                </p>
                <p class="account">@{{ userInfo.userAccount || 'unknown' }}</p>
              </div>
            </div>
          </div>

          <div class="info-section">
            <div class="info-item">
              <span class="label">手机号</span>
              <span class="value">{{ userInfo.userPhone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱</span>
              <span class="value">{{ userInfo.userEmail || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间</span>
              <span class="value">{{ formatDate(userInfo.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">最后更新</span>
              <span class="value">{{ formatDate(userInfo.updateTime) }}</span>
            </div>
          </div>

          <a-button type="primary" block class="primary-action-btn" @click="showEditModal">
            编辑信息
          </a-button>
        </a-card>
      </a-col>

      <a-col :xs="24" :sm="24" :md="16">
        <a-card :bordered="false" title="详细信息" class="details-card themed-card">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="账号">
              {{ userInfo.userAccount }}
            </a-descriptions-item>
            <a-descriptions-item label="昵称">
              {{ userInfo.userName }}
            </a-descriptions-item>
            <a-descriptions-item label="手机号">
              {{ userInfo.userPhone || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="邮箱">
              {{ userInfo.userEmail || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="角色">
              <a-tag :color="getRoleColor(userInfo.userRole)">
                {{ getRoleLabel(userInfo.userRole) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="简介" :span="2">
              {{ userInfo.userProfile || '-' }}
            </a-descriptions-item>
          </a-descriptions>

          <template v-if="userInfo.userRole === 'merchant'">
            <a-divider />
            <h3 class="section-title">商户资料</h3>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="工作年限">
                {{ userInfo.workYears || '-' }} 年
              </a-descriptions-item>
              <a-descriptions-item label="认证状态">
                <a-tag :color="getStatusColor(userInfo.qualificationStatus)">
                  {{ getStatusLabel(userInfo.qualificationStatus) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="经营特色" :span="2">
                {{ userInfo.specialties || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="经营区域" :span="2">
                {{ userInfo.serviceArea || '-' }}
              </a-descriptions-item>
            </a-descriptions>
          </template>

          <template v-if="userInfo.userRole === 'user' || userInfo.userRole === 'student'">
            <a-divider />
            <h3 class="section-title">学生信息</h3>
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="年龄">
                {{ userInfo.age || '-' }}
              </a-descriptions-item>
              <a-descriptions-item label="性别">
                {{ userInfo.gender === 'male' ? '男' : userInfo.gender === 'female' ? '女' : '-' }}
              </a-descriptions-item>
            </a-descriptions>
          </template>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:visible="isEditModalVisible"
      title="编辑个人信息"
      width="600px"
      wrap-class-name="profile-edit-modal"
      @ok="handleEdit"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="昵称" required>
          <a-input v-model:value="editForm.userName" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="editForm.userPhone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="邮箱">
          <a-input v-model:value="editForm.userEmail" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="头像">
          <div class="avatar-upload-row">
            <a-avatar :size="56" :src="editForm.userAvatar" style="background-color: #1890ff">
              <template #icon>
                <user-outlined />
              </template>
            </a-avatar>
            <a-upload
              :show-upload-list="false"
              accept=".jpg,.jpeg,.png,.webp"
              :before-upload="beforeAvatarUpload"
            >
              <a-button :loading="avatarUploading">上传头像</a-button>
            </a-upload>
          </div>
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea v-model:value="editForm.userProfile" placeholder="请输入个人简介" :rows="4" />
        </a-form-item>

        <template v-if="userInfo.userRole === 'merchant'">
          <a-form-item label="经营年限">
            <a-input-number v-model:value="editForm.workYears" placeholder="请输入经营年限" />
          </a-form-item>
          <a-form-item label="经营特色">
            <a-textarea v-model:value="editForm.specialties" placeholder="请输入经营特色" :rows="3" />
          </a-form-item>
          <a-form-item label="经营区域">
            <a-input v-model:value="editForm.serviceArea" placeholder="请输入经营区域" />
          </a-form-item>
        </template>

        <template v-if="userInfo.userRole === 'user' || userInfo.userRole === 'student'">
          <a-form-item label="年龄">
            <a-input-number v-model:value="editForm.age" placeholder="请输入年龄" />
          </a-form-item>
          <a-form-item label="性别">
            <a-select v-model:value="editForm.gender" placeholder="请选择性别">
              <a-select-option value="male">男</a-select-option>
              <a-select-option value="female">女</a-select-option>
            </a-select>
          </a-form-item>
        </template>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { getLoginUser, updateMyUser, uploadFile } from '../api';
import dayjs from 'dayjs';

const userInfo = ref({
  id: null,
  userAccount: '',
  userName: '',
  userRole: '',
  userPhone: '',
  userEmail: '',
  userAvatar: '',
  userProfile: '',
  createTime: '',
  updateTime: '',
  workYears: null,
  specialties: '',
  serviceArea: '',
  qualificationStatus: '',
  age: null,
  gender: '',
});

const isEditModalVisible = ref(false);
const avatarUploading = ref(false);

const editForm = reactive({
  userName: '',
  userPhone: '',
  userEmail: '',
  userAvatar: '',
  userProfile: '',
  workYears: null,
  specialties: '',
  serviceArea: '',
  age: null,
  gender: '',
});

const getRoleLabel = (role: string) => {
  const map: Record<string, string> = {
    'user': '学生',
    'student': '学生',
    'merchant': '商户',
    'supervisor': '监督管理员',
    'admin': '管理员',
  };
  return map[role] || role;
};

const getRoleColor = (role: string) => {
  const map: Record<string, string> = {
    'user': 'blue',
    'student': 'blue',
    'merchant': 'orange',
    'supervisor': 'gold',
    'admin': 'red',
  };
  return map[role] || 'default';
};

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
  };
  return map[status] || status;
};

const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    'pending': 'orange',
    'approved': 'green',
    'rejected': 'red',
  };
  return map[status] || 'default';
};

const formatDate = (date: string) => {
  if (!date) return '-';
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss');
};

const fetchUserInfo = async () => {
  try {
    const res = await getLoginUser();
    if (res.code === 0) {
      userInfo.value = res.data;
      Object.assign(editForm, res.data);
    } else {
      message.error(res.message || '获取个人信息失败');
    }
  } catch (error) {
    message.error('获取个人信息失败');
  }
};

const showEditModal = () => {
  Object.assign(editForm, userInfo.value);
  isEditModalVisible.value = true;
};

const beforeAvatarUpload = (file: File) => {
  const isImage = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'].includes(file.type);
  if (!isImage) {
    message.error('仅支持 jpg/jpeg/png/webp 格式');
    return false;
  }
  const isLt1M = file.size / 1024 / 1024 < 1;
  if (!isLt1M) {
    message.error('头像大小不能超过 1MB');
    return false;
  }
  void handleAvatarUpload(file);
  return false;
};

const handleAvatarUpload = async (file: File) => {
  avatarUploading.value = true;
  try {
    const res = await uploadFile(file, 'user_avatar');
    if (res.code !== 0 || !res.data) {
      message.error(res.message || '头像上传失败');
      return;
    }
    editForm.userAvatar = res.data;
    userInfo.value.userAvatar = res.data;
    message.success('头像上传成功');
  } catch (error) {
    message.error('头像上传失败');
  } finally {
    avatarUploading.value = false;
  }
};

const handleEdit = async () => {
  if (avatarUploading.value) {
    message.warning('头像上传中，请稍候');
    return;
  }

  const payload = {
    userName: editForm.userName?.trim() || undefined,
    userAvatar: editForm.userAvatar || undefined,
    userProfile: editForm.userProfile?.trim() || undefined,
    userPhone: editForm.userPhone?.trim() || undefined,
    userEmail: editForm.userEmail?.trim() || undefined,
    gender: editForm.gender || undefined,
    age: editForm.age ?? undefined,
  };

  if (payload.userPhone && !/^1\d{10}$/.test(payload.userPhone)) {
    message.warning('请输入有效手机号');
    return;
  }
  if (payload.userEmail && !/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(payload.userEmail)) {
    message.warning('请输入有效邮箱');
    return;
  }

  try {
    const res = await updateMyUser(payload);
    if (res.code === 0) {
      message.success('更新成功');
      isEditModalVisible.value = false;
      fetchUserInfo();
    } else {
      message.error(res.message || '更新失败');
    }
  } catch (error: any) {
    message.error(error?.message || '更新失败');
  }
};

onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
.profile-container {
  min-height: calc(100vh - 64px);
  padding: 20px;
  background:
    radial-gradient(circle at 96% 4%, rgba(24, 144, 255, 0.16), transparent 24%),
    linear-gradient(180deg, #f5f7fa 0%, #eef3f9 100%);
}

.theme-page-header {
  padding: 12px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ffffff 0%, #f7fbff 100%);
  border: 1px solid rgba(24, 144, 255, 0.16);
  box-shadow: 0 10px 26px rgba(31, 71, 130, 0.08);
}

:deep(.theme-page-header .ant-page-header-heading-title) {
  font-size: 22px;
  font-weight: 700;
  color: #0f2440;
}

:deep(.theme-page-header .ant-page-header-heading-sub-title) {
  color: #5f7391;
  font-size: 13px;
}

.profile-grid {
  margin-top: 18px;
}

.themed-card {
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(24, 144, 255, 0.12);
  box-shadow: 0 14px 34px rgba(34, 62, 101, 0.08);
  background: #ffffff;
}

.profile-card {
  text-align: center;
}

.profile-card-top {
  position: relative;
  padding: 28px 20px 24px;
  background: linear-gradient(150deg, #1890ff 0%, #096dd9 52%, #0050b3 100%);
}

.profile-card-top::after {
  content: '';
  position: absolute;
  top: -60px;
  right: -40px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.13);
  pointer-events: none;
}

.avatar-section {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.avatar-halo {
  width: 140px;
  height: 140px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 30% 30%, #ffffff 0%, rgba(255, 255, 255, 0.75) 46%, rgba(255, 255, 255, 0.2) 100%);
  box-shadow: 0 8px 26px rgba(10, 50, 105, 0.35);
}

.user-basic {
  width: 100%;
}

.user-basic h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(7, 25, 57, 0.22);
}

.role-tag {
  margin: 8px 0;
}

.role-tag :deep(.ant-tag) {
  font-weight: 600;
  border-radius: 999px;
  padding: 2px 10px;
}

.account {
  margin: 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 13px;
  letter-spacing: 0.2px;
}

.info-section {
  padding: 14px 18px 0;
  text-align: left;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 4px;
  border-bottom: 1px solid #edf3fb;
  gap: 10px;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #6f7f96;
  font-weight: 500;
  font-size: 13px;
}

.info-item .value {
  color: #10233f;
  font-weight: 600;
  font-size: 13px;
  text-align: right;
  word-break: break-all;
}

.primary-action-btn {
  margin: 16px 18px 18px;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  font-weight: 600;
  letter-spacing: 0.3px;
  box-shadow: 0 10px 20px rgba(24, 144, 255, 0.28);
}

.primary-action-btn:hover,
.primary-action-btn:focus {
  background: linear-gradient(135deg, #40a9ff 0%, #1677ff 100%);
}

.details-card {
  height: 100%;
}

:deep(.details-card .ant-card-head) {
  min-height: 56px;
  border-bottom: 1px solid #ebf2fb;
}

:deep(.details-card .ant-card-head-title) {
  font-size: 18px;
  font-weight: 700;
  color: #10233f;
}

:deep(.details-card .ant-descriptions-view) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.details-card .ant-descriptions-item-label) {
  font-weight: 600;
  color: #415571;
  background: #f5f9ff;
}

.section-title {
  margin-top: 16px;
  margin-bottom: 16px;
  color: #123459;
  font-size: 17px;
  font-weight: 700;
}

.avatar-upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-upload-row :deep(.ant-btn) {
  border-radius: 8px;
  border-color: #d0e6ff;
  color: #1669cc;
  font-weight: 600;
}

:deep(.profile-edit-modal .ant-modal-content) {
  border-radius: 14px;
  overflow: hidden;
}

:deep(.profile-edit-modal .ant-modal-header) {
  background: linear-gradient(135deg, #f7fbff 0%, #edf5ff 100%);
  border-bottom: 1px solid #e7f1ff;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 12px;
  }

  .theme-page-header {
    border-radius: 12px;
    padding: 10px 12px;
  }

  .avatar-halo {
    width: 116px;
    height: 116px;
  }

  .user-basic h2 {
    font-size: 20px;
  }

  .info-section {
    padding-left: 14px;
    padding-right: 14px;
  }

  .primary-action-btn {
    margin: 14px;
  }
}
</style>
