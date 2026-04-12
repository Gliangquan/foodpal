<template>
  <div class="page-container">
    <a-page-header title="店铺管理" sub-title="管理店铺基本信息" />

    <a-card :bordered="false" style="margin-top: 16px">
      <a-descriptions title="店铺信息" bordered :column="2">
        <a-descriptions-item label="店铺名称">{{ profile.merchantName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="联系人">{{ profile.contactName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="联系电话">{{ profile.contactPhone || '-' }}</a-descriptions-item>
        <a-descriptions-item label="营业时间">{{ profile.businessHours || '-' }}</a-descriptions-item>
        <a-descriptions-item label="店铺位置" :span="2">{{ profile.location || '-' }}</a-descriptions-item>
        <a-descriptions-item label="店铺简介" :span="2">{{ profile.description || '-' }}</a-descriptions-item>
        <a-descriptions-item label="审核状态">
          <a-tag :color="getAuditStatusColor(profile.auditStatus)">
            {{ getAuditStatusText(profile.auditStatus) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="店铺状态">
          <a-tag :color="profile.status === 1 ? 'green' : 'red'">
            {{ profile.status === 1 ? '营业中' : '已关闭' }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <a-button type="primary" @click="openEditModal">编辑店铺信息</a-button>
    </a-card>

    <!-- 编辑模态框 -->
    <a-modal v-model:open="modalOpen" title="编辑店铺信息" width="600px" @ok="submit" :confirm-loading="submitting">
      <a-form layout="vertical">
        <a-form-item label="店铺名称" required>
          <a-input v-model:value="form.merchantName" placeholder="请输入店铺名称" />
        </a-form-item>
        <a-form-item label="联系人" required>
          <a-input v-model:value="form.contactName" placeholder="请输入联系人姓名" />
        </a-form-item>
        <a-form-item label="联系电话" required>
          <a-input v-model:value="form.contactPhone" placeholder="请输入联系电话" />
        </a-form-item>
        <a-form-item label="营业时间">
          <a-input v-model:value="form.businessHours" placeholder="例如：8:00-20:00" />
        </a-form-item>
        <a-form-item label="店铺位置">
          <a-input v-model:value="form.location" placeholder="请输入店铺位置" />
        </a-form-item>
        <a-form-item label="店铺简介">
          <a-textarea v-model:value="form.description" :rows="4" placeholder="请输入店铺简介" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getMerchantProfile, updateMerchantProfile } from '@/api/modules/canteen';

interface MerchantProfile {
  id?: number;
  merchantName?: string;
  contactName?: string;
  contactPhone?: string;
  businessHours?: string;
  location?: string;
  description?: string;
  status?: number;
  auditStatus?: string;
}

const profile = ref<MerchantProfile>({});
const modalOpen = ref(false);
const submitting = ref(false);
const form = ref<MerchantProfile>({});

const loadProfile = async () => {
  try {
    const res = await getMerchantProfile();
    profile.value = res.data || {};
  } catch (error: any) {
    message.error(error?.message || '加载失败');
  }
};

const openEditModal = () => {
  form.value = { ...profile.value };
  modalOpen.value = true;
};

const submit = async () => {
  if (!form.value.merchantName?.trim()) {
    message.warning('请输入店铺名称');
    return;
  }
  if (!form.value.contactName?.trim()) {
    message.warning('请输入联系人姓名');
    return;
  }
  if (!form.value.contactPhone?.trim()) {
    message.warning('请输入联系电话');
    return;
  }

  submitting.value = true;
  try {
    // 只发送需要更新的字段
    const updateData = {
      merchantName: form.value.merchantName,
      contactName: form.value.contactName,
      contactPhone: form.value.contactPhone,
      businessHours: form.value.businessHours,
      location: form.value.location,
      description: form.value.description,
      avatar: form.value.avatar
    };
    await updateMerchantProfile(updateData);
    message.success('保存成功，等待管理员审核');
    modalOpen.value = false;
    await loadProfile();
  } catch (error: any) {
    message.error(error?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};

const getAuditStatusText = (status?: string) => {
  const map: Record<string, string> = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝'
  };
  return map[status || ''] || '未知';
};

const getAuditStatusColor = (status?: string) => {
  const map: Record<string, string> = {
    pending: 'orange',
    approved: 'green',
    rejected: 'red'
  };
  return map[status || ''] || 'default';
};

onMounted(loadProfile);
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
