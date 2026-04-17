<template>
  <div class="login-wrapper">
    <div class="login-container">
      <div class="form-container">
        <div class="form-header">
          <h1 class="form-title">登录</h1>
        </div>

        <!-- 登录方式切换 -->
        <a-segmented
          v-model:value="activeKey"
          :options="[
            { label: '账号登录', value: 'account' },
            { label: '手机号登录', value: 'phone' }
          ]"
          block
          style="margin-bottom: 24px"
        />

        <!-- 账号登录表单 -->
        <a-form
          v-if="activeKey === 'account'"
          :model="loginForm"
          @finish="handleLogin"
          layout="vertical"
          class="login-form"
        >
          <a-form-item
            name="userAccount"
            :rules="[{ required: true, message: '请输入账号' }]"
          >
            <a-input
              v-model:value="loginForm.userAccount"
              placeholder="账号"
              size="large"
            >
              <template #prefix>
                <user-outlined />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item
            name="userPassword"
            :rules="[{ required: true, message: '请输入密码' }]"
          >
            <a-input-password
              v-model:value="loginForm.userPassword"
              placeholder="密码"
              size="large"
            >
              <template #prefix>
                <lock-outlined />
              </template>
            </a-input-password>
          </a-form-item>
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              block
              size="large"
              :loading="loading"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 手机号登录表单 -->
        <a-form
          v-if="activeKey === 'phone'"
          :model="loginForm"
          @finish="handleLogin"
          layout="vertical"
          class="login-form"
        >
          <a-form-item
            name="userPhone"
            :rules="[{ required: true, message: '请输入手机号' }]"
          >
            <a-input
              v-model:value="loginForm.userPhone"
              placeholder="手机号"
              size="large"
            >
              <template #prefix>
                <phone-outlined />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item
            name="userPassword"
            :rules="[{ required: true, message: '请输入密码' }]"
          >
            <a-input-password
              v-model:value="loginForm.userPassword"
              placeholder="密码"
              size="large"
            >
              <template #prefix>
                <lock-outlined />
              </template>
            </a-input-password>
          </a-form-item>
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              block
              size="large"
              :loading="loading"
            >
              登录
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 底部链接 -->
        <div class="form-footer">
          <a-button type="link" @click="handleRegister">注册账号</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  UserOutlined,
  LockOutlined,
  PhoneOutlined,
} from '@ant-design/icons-vue';
import { userLogin } from '../api';
import type { UserLoginRequest } from '../api';

const activeKey = ref<'phone' | 'account'>('account');
const loginForm = ref({
  userPhone: '',
  userAccount: '',
  userPassword: '',
});
const loading = ref(false);

// 登录方法
const handleLogin = async () => {
  try {
    loading.value = true;
    const params: UserLoginRequest = {
      loginType: activeKey.value,
      userPassword: loginForm.value.userPassword,
    };

    if (activeKey.value === 'phone') {
      params.userPhone = loginForm.value.userPhone;
    } else {
      params.userAccount = loginForm.value.userAccount;
    }

    const response = await userLogin(params);
    if (response.code === 0) {
      localStorage.setItem('user', JSON.stringify(response.data));
      if (response.data?.token) {
        localStorage.setItem('token', response.data.token);
      }
      message.success('登录成功');
      const role = response.data?.userRole;
      if (role === 'merchant') {
        window.location.href = '/merchant/profile';
      } else if (role === 'supervisor') {
        window.location.href = '/supervisor/complaints';
      } else if (role === 'admin') {
        window.location.href = '/admin/users';
      } else {
        window.location.href = '/';
      }
    } else {
      message.error(response.message || '登录失败');
    }
  } catch (error) {
    message.error('登录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 注册方法
const handleRegister = () => {
  window.location.href = '/register';
};
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

.form-container {
  background: white;
  border-radius: 8px;
  padding: 40px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.form-header {
  text-align: center;
  margin-bottom: 32px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

/* 登录表单 */
.login-form {
  margin-bottom: 16px;
}

.login-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.login-form :deep(.ant-input),
.login-form :deep(.ant-input-password) {
  border-radius: 4px;
}

/* 底部链接 */
.form-footer {
  text-align: center;
  margin-top: 16px;
}

.form-footer :deep(.ant-btn-link) {
  color: #1890ff;
  font-size: 14px;
  padding: 0;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .form-container {
    padding: 24px;
  }

  .form-title {
    font-size: 24px;
  }
}
</style>
