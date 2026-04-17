<template>
  <view class="page-content">
    <view class="header-card">
      <text class="title">店铺管理</text>
      <text class="sub">管理您的店铺信息和资料</text>
    </view>
    
    <uni-card :border="false" padding="24">
      <view class="avatar-section">
        <image class="merchant-avatar" :src="getMerchantAvatar(merchant.avatar)" mode="aspectFill" />
        <button size="mini" @tap="uploadAvatar">{{ merchant.avatar ? '更换头像' : '上传头像' }}</button>
      </view>
      
      <uni-forms :model="merchant" label-position="top">
        <uni-forms-item label="店铺名称" required>
          <uni-easyinput v-model="merchant.merchantName" placeholder="请输入店铺名称" />
        </uni-forms-item>
        
        <uni-forms-item label="联系人">
          <uni-easyinput v-model="merchant.contactName" placeholder="请输入联系人姓名" />
        </uni-forms-item>
        
        <uni-forms-item label="联系电话">
          <uni-easyinput v-model="merchant.contactPhone" placeholder="请输入联系电话" />
        </uni-forms-item>
        
        <uni-forms-item label="营业时间">
          <uni-easyinput v-model="merchant.businessHours" placeholder="例如：08:00-22:00" />
        </uni-forms-item>
        
        <uni-forms-item label="地理位置">
          <uni-easyinput v-model="merchant.location" placeholder="请输入店铺地址" />
        </uni-forms-item>
        
        <uni-forms-item label="店铺描述">
          <uni-easyinput v-model="merchant.description" type="textarea" placeholder="请输入店铺描述" />
        </uni-forms-item>
      </uni-forms>
      
      <view class="audit-tip">
        店铺资料修改需管理员审核，通过后才会正式生效。
      </view>
      <button class="submit-btn" type="primary" @tap="saveProfile">提交修改</button>
    </uni-card>
    
    <uni-card :border="false" padding="24" style="margin-top: 20rpx;">
      <text class="section-title">店铺统计</text>
      <view class="stat-grid">
        <view class="stat-item">
          <text class="stat-num">{{ stats.dishCount || 0 }}</text>
          <text class="stat-label">菜品数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.complaintCount || 0 }}</text>
          <text class="stat-label">投诉数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.likeCount || 0 }}</text>
          <text class="stat-label">点赞数</text>
        </view>
      </view>
    </uni-card>
  </view>
</template>

<script>
import { canteenApi, userApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';

export default {
  data() {
    return {
      defaultImg: '/static/logo.png',
      merchant: {
        merchantName: '',
        contactName: '',
        contactPhone: '',
        avatar: '',
        businessHours: '',
        location: '',
        description: ''
      },
      stats: {
        dishCount: 0,
        complaintCount: 0,
        likeCount: 0
      }
    };
  },
  onShow() {
    this.loadMerchantProfile();
  },
  methods: {
    getMerchantAvatar(url) {
      return normalizeFileUrl(url) || this.defaultImg;
    },
    async loadMerchantProfile() {
      try {
        // 获取当前用户信息
        const userInfo = await userApi.fetchCurrentUser();
        if (!userInfo) {
          uni.showToast({ title: '请先登录', icon: 'none' });
          return;
        }
        
        // 获取商户资料
        const result = await canteenApi.getMyMerchantProfile();
        if (result) {
          this.merchant = { ...this.merchant, ...result };
        }
        
        // 获取统计数据
        await this.loadStats();
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      }
    },
    async loadStats() {
      try {
        // 获取菜品数
        const dishRes = await canteenApi.listMyDishes({ current: 1, size: 1 });
        this.stats.dishCount = dishRes.total || 0;
        
        // 获取投诉数
        const complaintRes = await canteenApi.listMyComplaints({ current: 1, size: 1 });
        this.stats.complaintCount = complaintRes.total || 0;
      } catch (error) {
        console.error('加载统计失败:', error);
      }
    },
    uploadAvatar() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          // 上传图片到服务器
          this.uploadFile(tempFilePath, 'avatar');
        }
      });
    },
    async uploadFile(filePath, type) {
      try {
        uni.showLoading({ title: '上传中...' });
        const result = await canteenApi.uploadFile(filePath, type);
        if (result && result.url) {
          this.merchant.avatar = result.url;
          uni.showToast({ title: '上传成功', icon: 'success' });
        }
      } catch (error) {
        uni.showToast({ title: error.message || '上传失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    async saveProfile() {
      if (!this.merchant.merchantName) {
        uni.showToast({ title: '请输入店铺名称', icon: 'none' });
        return;
      }
      
      try {
        uni.showLoading({ title: '保存中...' });
        await canteenApi.updateMerchantProfile(this.merchant);
        uni.showToast({ title: '资料已提交，请等待管理员审核', icon: 'none' });
        await this.loadMerchantProfile();
      } catch (error) {
        uni.showToast({ title: error.message || '保存失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.header-card {
  background: #fff;
  border-radius: 18rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1e2f4f;
}

.sub {
  display: block;
  margin-top: 8rpx;
  color: #6d7892;
  font-size: 24rpx;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30rpx;
}

.merchant-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  margin-bottom: 16rpx;
  background-color: #f5f7fa;
}

.audit-tip {
  margin-top: 20rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  background: #fff7e6;
  color: #ad6800;
  font-size: 24rpx;
  line-height: 1.6;
}

.submit-btn {
  margin-top: 30rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1e2f4f;
  margin-bottom: 20rpx;
}

.stat-grid {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: #2f65f9;
}

.stat-label {
  display: block;
  font-size: 24rpx;
  color: #6d7892;
  margin-top: 8rpx;
}
</style>
