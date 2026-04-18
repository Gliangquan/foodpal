<template>
  <view class="page-content">
    <!-- 用户信息卡片 -->
    <uni-card :border="false" padding="24" class="user-card" @tap="goEdit">
      <view class="user-row">
        <view class="avatar-wrap" @tap.stop="chooseAvatar">
          <image class="avatar" :src="getAvatarUrl(user.userAvatar)" mode="aspectFill" />
          <view class="avatar-edit">{{ avatarUploading ? '上传中' : '更换' }}</view>
        </view>
        <view class="user-main">
          <text class="name">{{ user.userName || '未设置昵称' }}</text>
          <text class="meta">{{ user.userAccount || '-' }}</text>
          <view class="user-tags">
            <uni-tag :text="roleText" size="small" :type="getRoleType()" />
          </view>
        </view>
        <view class="edit-hint">
        </view>
      </view>
    </uni-card>

    <!-- 统计数据 -->
    <uni-card :border="false" padding="20" style="margin: 0 20rpx 16rpx;">
      <view class="stat-grid">
        <view class="stat-item" @tap="goMyFavorites">
          <text class="stat-num">{{ favorites.length }}</text>
          <text class="stat-label">收藏菜品</text>
        </view>
        <view class="stat-item" @tap="goComplaintList">
          <text class="stat-num">{{ complaints.length }}</text>
          <text class="stat-label">投诉记录</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ getOnlineDays() }}</text>
          <text class="stat-label">在线天数</text>
        </view>
      </view>
    </uni-card>

    <!-- 收藏菜品预览 -->
    <uni-section title="我的收藏" type="line">
      <template v-slot:right v-if="favorites.length > 3">
        <text class="section-extra" @tap="goMyFavorites">查看全部</text>
      </template>
    </uni-section>
    <view v-if="favorites.length" class="favorites-preview">
      <view 
        class="favorite-card" 
        v-for="item in favorites.slice(0, 3)" 
        :key="item.id"
        @tap="goDishDetail(item)"
      >
        <image class="favorite-thumb" :src="getImageUrl(item.dishImage)" mode="aspectFill" />
        <view class="favorite-info">
          <text class="favorite-name">{{ item.dishName }}</text>
          <text class="favorite-price">¥{{ item.currentPrice || item.dishPrice }}</text>
          <uni-tag 
            :text="item.supplyStatus === 1 ? '可供应' : '已售罄'" 
            :type="item.supplyStatus === 1 ? 'success' : 'warning'" 
            size="mini"
          />
        </view>
      </view>
    </view>
    <view v-else class="empty-box-small">
      <text class="empty-text">暂无收藏菜品</text>
    </view>

    <!-- 功能菜单 -->
    <template v-if="isStudent">
      <uni-section title="我的服务" type="line"></uni-section>
      <uni-list :border="false" class="menu-list">
        <uni-list-item 
          title="发起投诉" 
          note="遇到问题可以发起投诉"
          showArrow 
          clickable 
          @click="goComplaintCreate"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/tixing.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="我的收藏" 
          note="收藏喜欢的菜品"
          showArrow 
          clickable 
          @click="goMyFavorites"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/xianhua.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="资讯公告" 
          note="查看最新动态和公告"
          showArrow 
          clickable 
          @click="goContent"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/zixun.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="编辑资料" 
          note="修改个人信息"
          showArrow 
          clickable 
          @click="goEdit"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/bianji.png" mode="aspectFit" />
          </template>
        </uni-list-item>
      </uni-list>
    </template>

    <template v-if="isMerchant">
      <uni-section title="商户管理" type="line"></uni-section>
      <uni-list :border="false" class="menu-list">
        <uni-list-item 
          title="店铺信息" 
          note="管理店铺基本信息"
          showArrow 
          clickable 
          @click="goMerchantProfile"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/shangdian.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="菜品管理" 
          note="管理店铺菜品"
          showArrow 
          clickable 
          @click="goDishManage"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/fenlei.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="发布动态" 
          note="发布店铺动态"
          showArrow 
          clickable 
          @click="goPublishDynamic"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/zixun.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="投诉处理" 
          note="处理用户投诉"
          showArrow 
          clickable 
          @click="goComplaintList"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/tixing.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="编辑资料" 
          note="修改个人信息"
          showArrow 
          clickable 
          @click="goEdit"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/bianji.png" mode="aspectFit" />
          </template>
        </uni-list-item>
      </uni-list>
    </template>

    <template v-if="isSupervisor">
      <uni-section title="监督管理" type="line"></uni-section>
      <uni-list :border="false" class="menu-list">
        <uni-list-item 
          title="投诉管理" 
          note="处理学生投诉"
          showArrow 
          clickable 
          @click="goSupervisorComplaints"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/tixing.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="商户监管" 
          note="监管商户情况"
          showArrow 
          clickable 
          @click="goSupervisorMerchants"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/shangdian.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="投诉排行榜" 
          note="查看投诉排行"
          showArrow 
          clickable 
          @click="goComplaintRanking"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/redu.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="资讯公告" 
          note="查看最新动态和公告"
          showArrow 
          clickable 
          @click="goContent"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/zixun.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="编辑资料" 
          note="修改个人信息"
          showArrow 
          clickable 
          @click="goEdit"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/bianji.png" mode="aspectFit" />
          </template>
        </uni-list-item>
      </uni-list>
    </template>

    <template v-if="isAdmin">
      <uni-section title="系统管理" type="line"></uni-section>
      <uni-list :border="false" class="menu-list">
        <uni-list-item 
          title="用户管理" 
          note="管理系统用户"
          showArrow 
          clickable 
          @click="goAdminUser"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/wode.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="商户管理" 
          note="管理商户信息"
          showArrow 
          clickable 
          @click="goAdminMerchant"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/shangdian.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="公告管理" 
          note="管理系统公告"
          showArrow 
          clickable 
          @click="goAdminAnnouncement"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/zixun.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="投诉管理" 
          note="处理投诉信息"
          showArrow 
          clickable 
          @click="goSupervisorComplaints"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/tixing.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="商户监管" 
          note="监管商户情况"
          showArrow 
          clickable 
          @click="goSupervisorMerchants"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/shangdian.png" mode="aspectFit" />
          </template>
        </uni-list-item>
        <uni-list-item 
          title="编辑资料" 
          note="修改个人信息"
          showArrow 
          clickable 
          @click="goEdit"
        >
          <template v-slot:header>
            <image class="menu-icon-img" src="/static/icon_med/bianji.png" mode="aspectFit" />
          </template>
        </uni-list-item>
      </uni-list>
    </template>

    <view class="logout-wrap">
      <button type="warn" class="logout-btn" @tap="logout">退出登录</button>
    </view>
  </view>
</template>

<script>
import avatar from '@/components/avatar.vue';
import { canteenApi, fileApi, userApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';
import { roleLabel, isStudentRole, isMerchantRole, isSupervisorRole, isAdminRole } from '@/utils/permission.js';
import { applyTabbarConfig } from '@/utils/tabbar.js';

export default {
  components: {
    avatar
  },
  data() {
    return {
      user: {},
      roleText: '访客',
      isStudent: false,
      isMerchant: false,
      isSupervisor: false,
      isAdmin: false,
      favorites: [],
      complaints: [],
      avatarUploading: false
    }
  },
  async onShow() {
    await this.loadUser();
    await Promise.all([this.loadFavorites(), this.loadComplaints()]);
  },
  methods: {
    async loadUser() {
      try {
        this.user = await userApi.fetchCurrentUser();
        uni.setStorageSync('userInfo', this.user);
      } catch (error) {
        this.user = uni.getStorageSync('userInfo') || {};
      }
      this.roleText = roleLabel(this.user);
      this.isStudent = isStudentRole(this.user);
      this.isMerchant = isMerchantRole(this.user);
      this.isSupervisor = isSupervisorRole(this.user);
      this.isAdmin = isAdminRole(this.user);
      applyTabbarConfig(this.user);
    },
    getAvatarUrl(url) {
      if (!url) return '/static/logo.png';
      return normalizeFileUrl(url) || '/static/logo.png';
    },
    getImageUrl(url) {
      if (!url) return '';
      return normalizeFileUrl(url);
    },
    getRoleType() {
      if (this.isStudent) return 'primary';
      if (this.isMerchant) return 'success';
      if (this.isSupervisor) return 'warning';
      if (this.isAdmin) return 'error';
      return 'default';
    },
    getOnlineDays() {
      if (!this.user.createTime) return 0;
      const create = new Date(this.user.createTime);
      const now = new Date();
      const diff = now - create;
      return Math.floor(diff / (1000 * 60 * 60 * 24)) + 1;
    },
    chooseAvatar() {
      if (this.avatarUploading) return;
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const tempFile = res.tempFiles && res.tempFiles[0];
          const tempPath = (tempFile && tempFile.path) || (res.tempFilePaths && res.tempFilePaths[0]) || '';
          if (!tempPath) return;
          if (tempFile?.size && tempFile.size > 10 * 1024 * 1024) {
            uni.showToast({ title: '头像需小于10MB', icon: 'none' });
            return;
          }
          this.avatarUploading = true;
          try {
            const remoteUrl = await fileApi.upload(tempPath, 'user_avatar');
            await userApi.updateProfile({ userAvatar: remoteUrl });
            const refreshed = await userApi.fetchCurrentUser();
            this.user = refreshed || { ...this.user, userAvatar: remoteUrl };
            uni.setStorageSync('userInfo', this.user);
            uni.showToast({ title: '头像更新成功', icon: 'success' });
          } catch (error) {
            uni.showToast({ title: error.message || '头像上传失败', icon: 'none' });
          } finally {
            this.avatarUploading = false;
          }
        }
      });
    },
    async loadFavorites() {
      try {
        this.favorites = await canteenApi.myFavorites();
      } catch (error) {
        this.favorites = [];
      }
    },
    async loadComplaints() {
      try {
        const page = await canteenApi.listMyComplaints({ current: 1, size: 20 });
        this.complaints = page.records || [];
      } catch (error) {
        this.complaints = [];
      }
    },
    goDishDetail(item) {
      uni.navigateTo({ 
        url: `/pages/index/dish-detail?id=${item.id}&name=${encodeURIComponent(item.dishName)}`
      });
    },
    goEdit() {
      uni.navigateTo({ url: '/pages/edit-profile/index' });
    },
    goComplaintCreate() {
      uni.navigateTo({ url: '/pages/appointment/index' });
    },
    goComplaintList() {
      uni.switchTab({ url: '/pages/order/index' });
    },
    goMyFavorites() {
      uni.navigateTo({ url: '/pages/favorites/index' });
    },
    goContent() {
      uni.navigateTo({ url: '/pages/content/list' });
    },
    goNotification() {
      uni.navigateTo({ url: '/pages/notification/index' });
    },
    goMerchantProfile() {
      uni.navigateTo({ url: '/pages/merchant/profile' });
    },
    goDishManage() {
      uni.navigateTo({ url: '/pages/merchant/dishes' });
    },
    goPublishDynamic() {
      uni.navigateTo({ url: '/pages/merchant/publish-dynamic' });
    },
    goSupervisorComplaints() {
      uni.navigateTo({ url: '/pages/supervisor/complaints' });
    },
    goSupervisorMerchants() {
      uni.navigateTo({ url: '/pages/supervisor/merchants' });
    },
    goComplaintRanking() {
      uni.navigateTo({ url: '/pages/supervisor/complaints?sort=ranking' });
    },
    goAdminUser() {
      uni.showToast({ title: 'UniApp 管理员用户管理页暂未实现', icon: 'none' });
    },
    goAdminMerchant() {
      uni.navigateTo({ url: '/pages/supervisor/merchants' });
    },
    goAdminAnnouncement() {
      uni.navigateTo({ url: '/pages/content/list?tab=announcement' });
    },
    logout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            uni.removeStorageSync('foodpal_token');
            uni.removeStorageSync('userInfo');
            applyTabbarConfig({});
            uni.reLaunch({ url: '/pages/login/index' });
          }
        }
      });
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.page-content {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40rpx;
}

.user-card {
  margin: 20rpx;
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.user-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.avatar-wrap {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  flex-shrink: 0;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 4rpx solid #e0e0e0;
  background: #f5f5f5;
}

.avatar-edit {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  background: #2f65f9;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
}

.user-main {
  flex: 1;
  min-width: 0;
}

.name {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2f4f;
  display: block;
  margin-bottom: 8rpx;
}

.meta {
  font-size: 24rpx;
  color: #6e7891;
  display: block;
  margin-bottom: 12rpx;
}

.user-tags {
  display: flex;
  gap: 8rpx;
  flex-wrap: wrap;
}

.edit-hint {
  font-size: 32rpx;
  opacity: 0.8;
}

.stat-grid {
  display: flex;
  justify-content: space-around;
  padding: 8rpx 0;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-num {
  font-size: 36rpx;
  font-weight: bold;
  color: #1f2f4f;
  display: block;
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: 22rpx;
  color: #6e7891;
}

.section-extra {
  color: #2f65f9;
  font-size: 24rpx;
}

.favorites-preview {
  display: flex;
  gap: 12rpx;
  padding: 0 20rpx;
  overflow-x: auto;
  margin-bottom: 16rpx;
}

.favorite-card {
  width: 200rpx;
  flex-shrink: 0;
  background: #ffffff;
  border-radius: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.favorite-thumb {
  width: 200rpx;
  height: 140rpx;
  background: #f5f7fa;
}

.favorite-info {
  padding: 12rpx;
}

.favorite-name {
  font-size: 24rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-price {
  font-size: 28rpx;
  font-weight: bold;
  color: #f5576c;
  display: block;
  margin-bottom: 8rpx;
}

.empty-box-small {
  text-align: center;
  padding: 40rpx;
  margin: 0 20rpx;
  background: #ffffff;
  border-radius: 12rpx;
}

.empty-icon {
  font-size: 60rpx;
  display: block;
  margin-bottom: 12rpx;
  opacity: 0.5;
}

.empty-text {
  color: #9aa4b8;
  font-size: 24rpx;
}

.menu-list {
  margin: 0 20rpx;
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
}

.menu-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.menu-icon-img {
  width: 38rpx;
  height: 38rpx;
  margin-right: 16rpx;
  display: block;
  flex-shrink: 0;
}

.logout-wrap {
  margin: 40rpx 20rpx;
}

.logout-btn {
  width: 100%;
  border-radius: 8rpx;
  background: #f5f5f5;
  border: none;
  color: #333;
  font-size: 28rpx;
  padding: 24rpx;
}
</style>
