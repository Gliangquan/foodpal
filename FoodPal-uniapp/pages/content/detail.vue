<template>
  <view class="page-content">
    <uni-card :border="false" padding="24">
      <view class="head-row">
        <uni-tag :text="contentType === 'announcement' ? '系统公告' : '商户动态'" :type="contentType === 'announcement' ? 'warning' : 'primary'" size="small" />
        <text class="time">{{ format(detail.publishTime || detail.createTime) }}</text>
      </view>
      <text class="title">{{ detail.title || '内容详情' }}</text>
      <image 
        :src="getImageUrl(detail.coverImage)" 
        mode="widthFix" 
        class="cover"
        @error="onImageError"
      />
      <text class="content">{{ detail.content || '暂无内容' }}</text>
      <view class="share-row">
        <button size="mini" @tap="shareContent">分享</button>
      </view>
    </uni-card>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { formatDateTime, normalizeFileUrl } from '@/utils/format.js';

export default {
  data() {
    return {
      contentId: null,
      contentType: 'dynamic',
      detail: {},
      shareChannels: ['微信好友', '朋友圈', '复制链接'],
      sharePopupOpen: false
    };
  },
  onLoad(options) {
    this.contentId = Number(options.id || 0);
    this.contentType = options.type || 'dynamic';
    if (!this.contentId) {
      uni.showToast({ title: '参数错误', icon: 'none' });
      return;
    }
    this.loadDetail();
  },
  onShareAppMessage() {
    return {
      title: this.detail.title || '高校食堂通',
      path: `/pages/content/detail?id=${this.contentId}&type=${this.contentType}`,
      imageUrl: this.detail.coverImage || '/static/logo.png'
    };
  },
  onShareTimeline() {
    return {
      title: this.detail.title || '高校食堂通',
      query: `id=${this.contentId}&type=${this.contentType}`,
      imageUrl: this.detail.coverImage || '/static/logo.png'
    };
  },
  methods: {
    format(value) {
      return formatDateTime(value);
    },
    async loadDetail() {
      try {
        if (this.contentType === 'announcement') {
          const page = await canteenApi.listAnnouncements({ current: 1, size: 100, onlyPublished: true });
          const records = page.records || [];
          const found = records.find((item) => Number(item.id) === this.contentId);
          this.detail = found || {};
        } else {
          const page = await canteenApi.listDynamics({ current: 1, size: 100, onlyPublished: true });
          const records = page.records || [];
          const found = records.find((item) => Number(item.id) === this.contentId);
          this.detail = found || {};
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      }
    },
    getImageUrl(imageUrl) {
      return normalizeFileUrl(imageUrl);
    },
    // 图片加载失败处理
    onImageError() {
      console.warn('封面图片加载失败:', this.detail.coverImage);
      this.detail.coverImage = '';
    },
    shareContent() {
      uni.showActionSheet({
        title: '分享到',
        itemList: this.shareChannels,
        success: async (res) => {
          const channel = this.shareChannels[res.tapIndex];
          if (channel === '复制链接') {
            const path = `pages/content/detail?id=${this.contentId}&type=${this.contentType}`;
            uni.setClipboardData({
              data: path,
              success: () => {
                uni.showToast({ title: '链接已复制', icon: 'success' });
              }
            });
          } else if (channel === '微信好友' || channel === '朋友圈') {
            // 调用微信小程序分享
            uni.showShareMenu({
              withShareTicket: true,
              menus: ['shareAppMessage', 'shareTimeline']
            });
            uni.showToast({ title: '请点击右上角分享按钮', icon: 'none' });
          }
          
          // 记录分享行为（仅针对动态）
          if (this.contentType === 'dynamic' && this.contentId) {
            try {
              await canteenApi.shareDynamic(this.contentId, channel);
            } catch (e) {
              // 分享记录失败不影响用户体验
            }
          }
        }
      });
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.head-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  color: #8b95ab;
  font-size: 22rpx;
}

.title {
  display: block;
  margin-top: 16rpx;
  color: #1f2f4f;
  font-size: 34rpx;
  font-weight: 700;
  line-height: 1.4;
}

.cover {
  width: 100%;
  border-radius: 14rpx;
  margin-top: 14rpx;
  background-color: #f5f7fa;
  min-height: 200rpx;
}

.content {
  margin-top: 16rpx;
  display: block;
  color: #4e5a76;
  font-size: 28rpx;
  line-height: 1.8;
  white-space: pre-wrap;
}

.share-row {
  margin-top: 24rpx;
  display: flex;
  justify-content: center;
}
</style>
