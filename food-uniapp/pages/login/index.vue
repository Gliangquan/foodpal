<template>
  <view class="page-container flex-center">
    <view class="card" style="width: 100%; max-width: 640rpx;">
      <view class="page-header">
        <text class="title">高校食堂通</text>
        <text class="subtitle">{{ showRegister ? '注册账号' : '账号登录' }}</text>
      </view>

      <view v-if="!showRegister" class="form-wrapper">
        <uni-segmented-control
          :current="loginTypeIndex"
          :values="['账号登录', '手机登录']"
          style-type="button"
          active-color="#2f65f9"
          @clickItem="switchLoginType"
        />

        <view class="form">
          <view class="input-item" v-if="loginType === 'account'">
            <uni-easyinput v-model="form.userAccount" placeholder="请输入账号" />
          </view>
          <view class="input-item" v-if="loginType === 'phone'">
            <uni-easyinput v-model="form.userPhone" type="number" placeholder="请输入手机号" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="form.userPassword" type="password" placeholder="请输入密码" />
          </view>

          <view class="remember-row">
            <checkbox-group @change="toggleRemember">
              <label class="checkbox-label">
                <checkbox :checked="rememberPassword" color="#2f65f9" />
                <text class="checkbox-text">记住密码</text>
              </label>
            </checkbox-group>
          </view>

          <view class="btn-wrapper">
            <button class="btn-primary" :disabled="loading" @tap="handleLogin">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </view>
        </view>

        <view class="links">
          <text @tap="showRegister = true">注册账号</text>
          <text @tap="forgetPassword">忘记密码</text>
        </view>
      </view>

      <view v-else class="form-wrapper">
        <view class="form">
          <view class="input-item">
            <text class="label">注册角色</text>
            <uni-segmented-control
              :current="registerTypeIndex"
              :values="['学生', '商户', '监督员']"
              style-type="button"
              active-color="#2f65f9"
              @clickItem="switchRegisterType"
            />
          </view>

          <view class="input-item">
            <uni-easyinput v-model="registerForm.userAccount" placeholder="请输入账号" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.userName" placeholder="请输入姓名/昵称" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.userPhone" type="number" placeholder="请输入手机号" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.userEmail" placeholder="请输入邮箱（选填，建议填写）" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.userPassword" type="password" placeholder="请输入密码" />
          </view>
          <view class="input-item">
            <uni-easyinput v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" />
          </view>

          <view class="btn-wrapper">
            <button class="btn-primary" :disabled="registering" @tap="handleRegister">
              {{ registering ? '注册中...' : '注册' }}
            </button>
          </view>
        </view>

        <view class="links">
          <text @tap="showRegister = false">返回登录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { userApi, canteenApi } from '@/utils/api.js';
import { setToken } from '@/utils/request.js';
import { applyTabbarConfig } from '@/utils/tabbar.js';

const REGISTER_ROLES = ['student', 'merchant', 'supervisor'];

export default {
  data() {
    return {
      showRegister: false,
      loginType: 'account',
      registerType: 'student',
      rememberPassword: false,
      form: {
        userAccount: '',
        userPhone: '',
        userPassword: ''
      },
      registerForm: {
        userAccount: '',
        userName: '',
        userPhone: '',
        userEmail: '',
        userPassword: '',
        confirmPassword: '',
        userRole: 'student'
      },
      loading: false,
      registering: false
    };
  },
  computed: {
    loginTypeIndex() {
      return this.loginType === 'account' ? 0 : 1;
    },
    registerTypeIndex() {
      return REGISTER_ROLES.indexOf(this.registerType);
    }
  },
  onLoad() {
    const user = uni.getStorageSync('userInfo');
    if (user && user.id) {
      this.goHome();
      return;
    }
    // 加载记住的密码
    const savedLoginInfo = uni.getStorageSync('savedLoginInfo');
    if (savedLoginInfo) {
      this.form.userAccount = savedLoginInfo.userAccount || '';
      this.form.userPassword = savedLoginInfo.userPassword || '';
      this.rememberPassword = true;
    }
  },
  methods: {
    switchLoginType(e) {
      this.loginType = e.currentIndex === 0 ? 'account' : 'phone';
    },
    toggleRemember(e) {
      this.rememberPassword = e.detail.value.length > 0;
    },
    switchRegisterType(e) {
      const role = REGISTER_ROLES[e.currentIndex] || 'student';
      this.registerType = role;
      this.registerForm.userRole = role;
    },
    async handleLogin() {
      if (this.loading) return;

      const { userAccount, userPhone, userPassword } = this.form;
      if (this.loginType === 'account' && !userAccount) {
        return uni.showToast({ title: '请输入账号', icon: 'none' });
      }
      if (this.loginType === 'phone' && !userPhone) {
        return uni.showToast({ title: '请输入手机号', icon: 'none' });
      }
      if (!userPassword) {
        return uni.showToast({ title: '请输入密码', icon: 'none' });
      }

      this.loading = true;
      try {
        const payload = {
          loginType: this.loginType,
          userAccount: this.loginType === 'account' ? userAccount : undefined,
          userPhone: this.loginType === 'phone' ? userPhone : undefined,
          userPassword
        };
        const user = await userApi.login(payload);
        setToken(user.token);
        uni.setStorageSync('userInfo', user);
        applyTabbarConfig(user);
        
        // 保存或清除记住的密码
        if (this.rememberPassword) {
          uni.setStorageSync('savedLoginInfo', {
            userAccount: this.form.userAccount,
            userPassword: this.form.userPassword
          });
        } else {
          uni.removeStorageSync('savedLoginInfo');
        }
        
        const isMerchant = String(user.userRole || '').toLowerCase() === 'merchant';
        let shouldGuideMerchantProfile = false;
        if (isMerchant) {
          try {
            const merchantProfile = await canteenApi.getMyMerchantProfile();
            shouldGuideMerchantProfile = !merchantProfile || !merchantProfile.id;
          } catch (e) {
            shouldGuideMerchantProfile = false;
          }
        }

        uni.showToast({
          title: shouldGuideMerchantProfile ? '登录成功，请先完善店铺信息' : '登录成功',
          icon: 'success'
        });
        setTimeout(() => {
          this.goHome(shouldGuideMerchantProfile);
        }, 600);
      } catch (error) {
        uni.showToast({ title: error.message || '登录失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    async handleRegister() {
      if (this.registering) return;
      const { userAccount, userName, userPhone, userEmail, userPassword, confirmPassword, userRole } = this.registerForm;
      if (!userAccount || !userName || !userPhone || !userPassword || !confirmPassword) {
        return uni.showToast({ title: '请填写完整信息', icon: 'none' });
      }
      if (userPassword !== confirmPassword) {
        return uni.showToast({ title: '两次密码不一致', icon: 'none' });
      }
      this.registering = true;
      try {
        if (userEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(userEmail)) {
          return uni.showToast({ title: '请输入有效邮箱', icon: 'none' });
        }
        await userApi.register({
          userAccount,
          userName,
          userPhone,
          userEmail: userEmail || undefined,
          userPassword,
          checkPassword: confirmPassword,
          userRole
        });
        uni.showToast({ title: '注册成功，请登录', icon: 'success' });
        this.showRegister = false;
      } catch (error) {
        uni.showToast({ title: error.message || '注册失败', icon: 'none' });
      } finally {
        this.registering = false;
      }
    },
    forgetPassword() {
      uni.showToast({ title: '请联系管理员重置密码', icon: 'none' });
    },
    goHome(guideMerchantProfile = false) {
      if (guideMerchantProfile) {
        uni.reLaunch({ url: '/pages/merchant/profile' });
        return;
      }
      uni.switchTab({
        url: '/pages/index/index',
        fail: () => {
          uni.reLaunch({ url: '/pages/index/index' });
        }
      });
    }
  }
};
</script>

<style lang="scss">
@import "@/styles/common.scss";

.remember-row {
  margin: 16rpx 0;
}

.checkbox-label {
  display: flex;
  align-items: center;
}

.checkbox-text {
  margin-left: 8rpx;
  font-size: 26rpx;
  color: #617198;
}
</style>
