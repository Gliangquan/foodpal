<template>
  <div class="register-wrapper">
    <div class="register-container">
      <div class="form-container">
        <div class="form-header">
          <h1 class="form-title">注册</h1>
        </div>

        <!-- 注册表单 -->
        <a-form
          :model="registerForm"
          @finish="handleRegister"
          layout="vertical"
          class="register-form"
        >
          <!-- 用户名 -->
          <a-form-item
            name="username"
            :rules="[{ required: true, message: '请输入用户名' }]"
          >
            <a-input
              v-model:value="registerForm.username"
              placeholder="用户名"
              size="large"
            >
              <template #prefix>
                <user-outlined />
              </template>
            </a-input>
          </a-form-item>

          <!-- 手机号 -->
          <a-form-item
            name="phone"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
            ]"
          >
            <a-input
              v-model:value="registerForm.phone"
              placeholder="手机号"
              size="large"
            >
              <template #prefix>
                <phone-outlined />
              </template>
            </a-input>
          </a-form-item>

          <!-- 密码 -->
          <a-form-item
            name="password"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码至少6位' }
            ]"
          >
            <a-input-password
              v-model:value="registerForm.password"
              placeholder="密码"
              size="large"
            >
              <template #prefix>
                <lock-outlined />
              </template>
            </a-input-password>
          </a-form-item>

          <!-- 确认密码 -->
          <a-form-item
            name="confirmPassword"
            :rules="[
              { required: true, message: '请确认密码' },
              { validator: validateConfirmPassword }
            ]"
          >
            <a-input-password
              v-model:value="registerForm.confirmPassword"
              placeholder="确认密码"
              size="large"
            >
              <template #prefix>
                <lock-outlined />
              </template>
            </a-input-password>
          </a-form-item>

          <!-- 注册按钮 -->
          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              block
              size="large"
              :loading="loading"
            >
              注册
            </a-button>
          </a-form-item>
        </a-form>

        <!-- 跳转登录链接 -->
        <div class="form-footer">
          <span>已有账号？</span>
          <a-button type="link" @click="handleLogin">去登录</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { message } from 'ant-design-vue';
import {
  UserOutlined,
  LockOutlined,
  PhoneOutlined,
} from '@ant-design/icons-vue';
import { userRegister } from '../api';
import type { UserRegisterRequest } from '../api';

const registerForm = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
});

const loading = ref(false);

// 自定义验证：确认密码
const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value !== registerForm.password) {
    return Promise.reject('两次输入的密码不一致');
  }
  return Promise.resolve();
};

// 注册方法
const handleRegister = async () => {
  try {
    loading.value = true;

    const params: UserRegisterRequest = {
      userAccount: registerForm.username,
      userPhone: registerForm.phone,
      userPassword: registerForm.password,
      checkPassword: registerForm.confirmPassword,
    };

    const response = await userRegister(params);
    if (response.code === 0) {
      message.success('注册成功');
      window.location.href = '/login';
    } else {
      message.error(response.message || '注册失败');
    }
  } catch (error) {
    message.error('注册失败，请稍后重试');
  } finally {
    loading.value = false;
  }
};

// 跳转登录页面
const handleLogin = () => {
  window.location.href = '/login';
};
</script>

<style scoped>
.register-wrapper {
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-container {
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

/* 注册表单 */
.register-form {
  margin-bottom: 16px;
}

.register-form :deep(.ant-form-item) {
  margin-bottom: 16px;
}

.register-form :deep(.ant-input),
.register-form :deep(.ant-input-password) {
  border-radius: 4px;
}

/* 底部链接 */
.form-footer {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.form-footer :deep(.ant-btn-link) {
  color: #1890ff;
  font-size: 14px;
  padding: 0 4px;
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