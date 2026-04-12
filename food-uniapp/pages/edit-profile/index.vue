<template>
  <view class="page-content">
    <uni-section title="头像" class="section"></uni-section>
    <uni-card :border="false" padding="24">
      <uni-file-picker v-model="avatarFiles" limit="1" file-mediatype="image" @select="onAvatarSelect" />
    </uni-card>

    <uni-section title="基本资料" class="section"></uni-section>
    <uni-card :border="false" padding="24">
      <view class="input-item">
        <text class="field-label">昵称</text>
        <uni-easyinput v-model="form.userName" placeholder="请输入昵称" />
      </view>
      <view class="input-item">
        <text class="field-label">手机号</text>
        <uni-easyinput v-model="form.userPhone" type="number" placeholder="请输入手机号" />
      </view>
      <view class="input-item">
        <text class="field-label">邮箱</text>
        <uni-easyinput v-model="form.userEmail" placeholder="请输入邮箱" />
      </view>
      <view class="input-item">
        <text class="field-label">性别</text>
        <uni-data-select v-model="form.gender" :localdata="genderOptions" />
      </view>
      <view class="input-item">
        <text class="field-label">年龄</text>
        <uni-easyinput v-model="form.age" type="number" placeholder="请输入年龄" />
      </view>
      <view class="input-item">
        <text class="field-label">个人简介</text>
        <uni-easyinput type="textarea" v-model="form.userProfile" :maxlength="300" placeholder="请输入个人简介（300字内）" />
      </view>
    </uni-card>

    <view class="flex gap-md action-row">
      <button class="btn-primary" style="flex: 1;" @tap="saveProfile">保存</button>
      <button class="btn-secondary" style="flex: 1;" @tap="cancelEdit">取消</button>
    </view>
  </view>
</template>

<script>
import { userApi, fileApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';

export default {
  data() {
    return {
      currentUser: null,
      form: {
        userName: '',
        userPhone: '',
        userEmail: '',
        gender: '保密',
        age: '',
        userProfile: '',
        userAvatar: ''
      },
      avatarFiles: [],
      avatarUploading: false,
      genderOptions: [
        { text: '男', value: '男' },
        { text: '女', value: '女' },
        { text: '保密', value: '保密' }
      ]
    };
  },
  async onLoad() {
    await this.loadUser();
  },
  methods: {
    async loadUser() {
      let user = null;
      try {
        user = await userApi.fetchCurrentUser();
      } catch (error) {
        user = uni.getStorageSync('userInfo');
      }
      if (!user) return;
      this.currentUser = user;
      this.form = {
        ...this.form,
        ...user,
        age: user.age === null || user.age === undefined ? '' : String(user.age)
      };
      if (user.userAvatar) {
        this.avatarFiles = [{ url: normalizeFileUrl(user.userAvatar), name: 'avatar' }];
      }
    },
    async onAvatarSelect(e) {
      const file = e.tempFiles && e.tempFiles[0];
      if (!file) return;
      const tempPath = file.path || file.url || '';
      if (!tempPath) return;
      this.avatarUploading = true;
      try {
        const remoteUrl = await fileApi.upload(tempPath, 'user_avatar');
        this.form.userAvatar = remoteUrl;
        this.avatarFiles = [{ url: normalizeFileUrl(remoteUrl), name: 'avatar' }];
        uni.showToast({ title: '头像上传成功', icon: 'success' });
      } catch (error) {
        uni.showToast({ title: error.message || '头像上传失败', icon: 'none' });
      } finally {
        this.avatarUploading = false;
      }
    },
    validateForm() {
      const name = (this.form.userName || '').trim();
      if (!name) {
        uni.showToast({ title: '请输入昵称', icon: 'none' });
        return false;
      }

      const phone = (this.form.userPhone || '').trim();
      if (phone && !/^1\d{10}$/.test(phone)) {
        uni.showToast({ title: '请输入有效手机号', icon: 'none' });
        return false;
      }

      const email = (this.form.userEmail || '').trim();
      if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        uni.showToast({ title: '请输入有效邮箱', icon: 'none' });
        return false;
      }

      const ageValue = (this.form.age || '').toString().trim();
      if (ageValue) {
        const age = Number(ageValue);
        if (Number.isNaN(age) || age < 0 || age > 120) {
          uni.showToast({ title: '年龄范围应为0-120', icon: 'none' });
          return false;
        }
      }

      return true;
    },
    buildPayload() {
      const payload = {
        userName: (this.form.userName || '').trim(),
        userAvatar: this.form.userAvatar || undefined,
        userProfile: (this.form.userProfile || '').trim() || undefined,
        userPhone: (this.form.userPhone || '').trim() || undefined,
        userEmail: (this.form.userEmail || '').trim() || undefined,
        gender: this.form.gender || undefined,
        age: this.form.age === '' || this.form.age === null || this.form.age === undefined
          ? undefined
          : Number(this.form.age)
      };
      return payload;
    },
    async saveProfile() {
      if (!this.validateForm()) return;
      if (this.avatarUploading) {
        uni.showToast({ title: '头像上传中，请稍候', icon: 'none' });
        return;
      }
      try {
        const payload = this.buildPayload();
        await userApi.updateProfile(payload);
        const refreshed = await userApi.fetchCurrentUser();
        if (refreshed) {
          uni.setStorageSync('userInfo', refreshed);
        }
        uni.showToast({ title: '保存成功', icon: 'success' });
        setTimeout(() => {
          uni.navigateBack();
        }, 600);
      } catch (error) {
        uni.showToast({ title: error.message || '保存失败', icon: 'none' });
      }
    },
    cancelEdit() {
      uni.navigateBack();
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.field-label {
  display: block;
  font-size: 22rpx;
  color: #6f7c95;
  margin-bottom: 8rpx;
}

.input-item {
  margin-bottom: 16rpx;
}

.input-item:last-child {
  margin-bottom: 0;
}

.action-row {
  margin-top: 32rpx;
}
</style>
