<template>
  <view class="page-content">
    <view class="header-card">
      <text class="title">消息中心</text>
      <text class="sub">投诉进度、系统通知与食堂公告</text>
      <uni-segmented-control
        :values="['系统消息', '食堂公告']"
        :current="tabIndex"
        style-type="button"
        active-color="#2f65f9"
        @clickItem="changeTab"
      />
    </view>

    <view v-if="tabIndex === 0">
      <uni-list :border="false" v-if="notifications.length">
        <uni-list-item
          v-for="item in notifications"
          :key="item.id"
          :title="item.title"
          :note="item.content"
          :right-text="format(item.createTime)"
          clickable
          @click="markRead(item)"
        >
          <template v-slot:header>
            <uni-badge v-if="item.status === 'unread'" text="未读" type="error" style="margin-right: 10rpx;" />
          </template>
        </uni-list-item>
      </uni-list>
      <view v-else class="empty-box"><text class="empty-text">暂无系统消息</text></view>
    </view>

    <view v-else>
      <uni-list :border="false" v-if="announcements.length">
        <uni-list-item
          v-for="item in announcements"
          :key="item.id"
          :title="item.title"
          :note="item.content"
          :right-text="format(item.publishTime || item.createTime)"
          clickable
          @click="goDetail(item)"
        />
      </uni-list>
      <view v-else class="empty-box"><text class="empty-text">暂无公告</text></view>
    </view>
  </view>
</template>

<script>
import { canteenApi, notificationApi, userApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      tabIndex: 0,
      notifications: [],
      announcements: [],
      userInfo: {}
    };
  },
  async onShow() {
    await this.loadUser();
    await Promise.all([this.loadNotifications(), this.loadAnnouncements()]);
  },
  methods: {
    format(value) {
      return formatDateTime(value);
    },
    async loadUser() {
      try {
        this.userInfo = await userApi.fetchCurrentUser();
      } catch (error) {
        this.userInfo = uni.getStorageSync('userInfo') || {};
      }
    },
    async loadNotifications() {
      if (!this.userInfo?.id) return;
      try {
        const page = await notificationApi.list({ userId: this.userInfo.id, current: 1, size: 30 });
        this.notifications = page.records || [];
      } catch (error) {
        this.notifications = [];
      }
    },
    async loadAnnouncements() {
      try {
        const page = await canteenApi.listAnnouncements({ current: 1, size: 30, onlyPublished: true });
        this.announcements = page.records || [];
      } catch (error) {
        this.announcements = [];
      }
    },
    changeTab(e) {
      this.tabIndex = e.currentIndex;
    },
    async markRead(item) {
      if (item.status !== 'unread') return;
      try {
        await notificationApi.markAsRead(item.id);
        item.status = 'read';
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' });
      }
    },
    goDetail(item) {
      uni.navigateTo({ url: `/pages/content/detail?id=${item.id}&type=announcement` });
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.header-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin: 20rpx 20rpx 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2f4f;
  line-height: 1.4;
}

.sub {
  display: block;
  margin-top: 6rpx;
  margin-bottom: 10rpx;
  font-size: 22rpx;
  color: #6e7891;
  line-height: 1.5;
}

.empty-box {
  text-align: center;
  padding: 120rpx 0;
}

.empty-text {
  color: #9aa4b8;
  font-size: 26rpx;
}
</style>
