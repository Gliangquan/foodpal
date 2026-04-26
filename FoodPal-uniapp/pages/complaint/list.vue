<template>
  <view class="page-content">
    <uni-card v-if="merchantName" :border="false" padding="16" style="margin-bottom: 16rpx;">
      <view class="merchant-header">
        <text class="merchant-name">{{ decodeURIComponent(merchantName) }}</text>
        <text class="merchant-subtitle">投诉记录</text>
      </view>
    </uni-card>

    <uni-card :border="false" padding="16" style="margin-bottom: 16rpx;">
      <view class="stat-grid">
        <view class="stat-item">
          <text class="stat-num">{{ statistics.total }}</text>
          <text class="stat-label">总投诉</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.pending }}</text>
          <text class="stat-label">待审核</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.rectify }}</text>
          <text class="stat-label">整改中/待复核</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.completed }}</text>
          <text class="stat-label">已完成</text>
        </view>
      </view>
    </uni-card>

    <uni-card :border="false" padding="16" style="margin-bottom: 16rpx;">
      <view class="filter-row">
        <picker :value="statusIndex" :range="statusOptions" range-key="text" @change="onStatusChange">
          <view class="picker-view">
            <text>{{ statusOptions[statusIndex].text }}</text>
            <text class="picker-arrow">▼</text>
          </view>
        </picker>
      </view>
    </uni-card>

    <view v-if="complaintList.length">
      <uni-card
        v-for="item in complaintList"
        :key="item.id"
        :border="false"
        padding="18"
        class="complaint-card"
        @click="goDetail(item)"
      >
        <view class="card-header">
          <view class="no-row">
            <uni-tag :text="getPriorityText(item.createTime)" :type="getPriorityType(item.createTime)" size="mini" />
            <text class="no">{{ item.complaintNo || '#' + item.id }}</text>
          </view>
          <uni-tag :text="statusText(item.status)" :type="statusTag(item.status)" size="small" />
        </view>

        <text class="complaint-title">{{ item.complaintTitle }}</text>

        <view class="meta-row">
          <view class="meta-item">
            <text class="meta-icon">商户</text>
            <text class="meta-text">{{ item.merchantName || '-' }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon">时间</text>
            <text class="meta-text">{{ formatDate(item.createTime) }}</text>
          </view>
        </view>

        <view class="content-box">
          <text class="content">{{ item.complaintContent }}</text>
        </view>

        <view class="evidence-row" v-if="parseEvidenceUrls(item.evidenceUrls).length">
          <image
            v-for="(url, index) in parseEvidenceUrls(item.evidenceUrls)"
            :key="`${item.id}-${index}`"
            class="evidence-image"
            :src="url"
            mode="aspectFill"
            @tap.stop="previewEvidence(item.evidenceUrls, index)"
          />
        </view>

        <view class="progress-row" v-if="item.rectifyRequirement">
          <text class="progress-label">整改要求：</text>
          <text class="progress-text">{{ item.rectifyRequirement }}</text>
        </view>

        <view class="progress-row" v-if="item.feedback">
          <text class="progress-label">处理反馈：</text>
          <text class="progress-text">{{ item.feedback }}</text>
        </view>
      </uni-card>
    </view>

    <view v-else class="empty-box">
      <text class="empty-icon">📋</text>
      <text class="empty-text">暂无投诉记录</text>
    </view>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';

export default {
  data() {
    return {
      merchantId: null,
      merchantName: '',
      complaintList: [],
      statistics: {
        total: 0,
        pending: 0,
        rectify: 0,
        completed: 0
      },
      statusIndex: 0,
      statusOptions: [
        { text: '全部状态', value: '' },
        { text: '待审核', value: 'pending_review' },
        { text: '待整改', value: 'pending_rectify' },
        { text: '待复核', value: 'rectified' },
        { text: '已完成', value: 'completed' },
        { text: '已驳回', value: 'rejected' }
      ],
      current: 1,
      pageSize: 20
    };
  },
  onLoad(options) {
    if (options.merchantId) {
      this.merchantId = Number(options.merchantId);
    }
    if (options.merchantName) {
      this.merchantName = options.merchantName;
    }
    this.loadComplaints();
  },
  onPullDownRefresh() {
    this.loadComplaints().finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    async loadComplaints() {
      try {
        const params = {
          current: this.current,
          size: this.pageSize,
          merchantId: this.merchantId || undefined,
          status: this.statusOptions[this.statusIndex].value || undefined
        };

        const page = await canteenApi.listComplaints(params);
        this.complaintList = page.records || [];
        this.statistics.total = page.total || 0;
        this.statistics.pending = this.complaintList.filter(c => c.status === 'pending_review').length;
        this.statistics.rectify = this.complaintList.filter(c => c.status === 'pending_rectify' || c.status === 'rectified').length;
        this.statistics.completed = this.complaintList.filter(c => c.status === 'completed').length;
      } catch (error) {
        uni.showToast({ title: '加载失败', icon: 'none' });
        console.error(error);
      }
    },
    onStatusChange(e) {
      this.statusIndex = Number(e.detail.value || 0);
      this.loadComplaints();
    },
    statusText(status) {
      const map = {
        pending_review: '待审核',
        pending_rectify: '待整改',
        rectified: '待复核',
        completed: '已完成',
        rejected: '已驳回'
      };
      return map[status] || status;
    },
    statusTag(status) {
      const map = {
        pending_review: 'warning',
        pending_rectify: 'error',
        rectified: 'primary',
        completed: 'success',
        rejected: 'default'
      };
      return map[status] || 'default';
    },
    getPriorityText(createTime) {
      if (!createTime) return '普通';
      const now = new Date();
      const create = new Date(createTime);
      const days = Math.floor((now - create) / (1000 * 60 * 60 * 24));
      if (days >= 7) return '紧急';
      if (days >= 3) return '重要';
      return '普通';
    },
    getPriorityType(createTime) {
      if (!createTime) return 'default';
      const now = new Date();
      const create = new Date(createTime);
      const days = Math.floor((now - create) / (1000 * 60 * 60 * 24));
      if (days >= 7) return 'error';
      if (days >= 3) return 'warning';
      return 'default';
    },
    formatDate(dateStr) {
      if (!dateStr) return '-';
      const date = new Date(dateStr);
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hour = String(date.getHours()).padStart(2, '0');
      const minute = String(date.getMinutes()).padStart(2, '0');
      return `${month}-${day} ${hour}:${minute}`;
    },
    parseEvidenceUrls(urls) {
      if (!urls) return [];
      const source = String(urls).trim();
      if (!source) return [];
      const normalizeList = (list) => list
        .map((item) => normalizeFileUrl(String(item || '').trim()))
        .filter(Boolean);
      if (source.startsWith('[') && source.endsWith(']')) {
        try {
          const parsed = JSON.parse(source);
          if (Array.isArray(parsed)) return normalizeList(parsed);
        } catch (error) {
        }
      }
      return normalizeList(source.split(','));
    },
    previewEvidence(urls, index) {
      const imageUrls = this.parseEvidenceUrls(urls);
      if (!imageUrls.length) return;
      uni.previewImage({
        urls: imageUrls,
        current: imageUrls[index] || imageUrls[0]
      });
    },
    goDetail(item) {
      uni.navigateTo({
        url: `/pages/complaint/detail?id=${item.id}`
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

.merchant-header {
  text-align: center;
}

.merchant-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2f4f;
  display: block;
  margin-bottom: 8rpx;
}

.merchant-subtitle {
  font-size: 24rpx;
  color: #6e7891;
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

.filter-row {
  display: flex;
  gap: 12rpx;
}

.picker-view {
  flex: 1;
  padding: 16rpx 24rpx;
  background: #f5f7fa;
  border-radius: 8rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
  color: #333;
}

.picker-arrow {
  font-size: 20rpx;
  color: #999;
}

.complaint-card {
  margin: 0 20rpx 16rpx;
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.no-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.no {
  font-size: 24rpx;
  color: #6e7891;
  font-weight: 500;
}

.complaint-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2f4f;
  display: block;
  margin-bottom: 16rpx;
  line-height: 1.5;
}

.meta-row {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 22rpx;
}

.meta-icon {
  color: #9aa4b8;
}

.meta-text {
  color: #6e7891;
}

.content-box {
  background: #f5f7fa;
  padding: 16rpx;
  border-radius: 8rpx;
  margin-bottom: 12rpx;
}

.content {
  font-size: 24rpx;
  color: #4a5568;
  line-height: 1.6;
  display: block;
}

.evidence-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.evidence-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 10rpx;
  background: #f5f7fa;
}

.progress-row {
  margin-top: 12rpx;
  padding-top: 12rpx;
  border-top: 1rpx solid #e8ecf0;
}

.progress-label {
  font-size: 22rpx;
  color: #9aa4b8;
  display: block;
  margin-bottom: 4rpx;
}

.progress-text {
  font-size: 24rpx;
  color: #4a5568;
  line-height: 1.5;
  display: block;
}

.empty-box {
  text-align: center;
  padding: 120rpx 40rpx;
  background: #ffffff;
  margin: 0 20rpx;
  border-radius: 16rpx;
}

.empty-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 16rpx;
  opacity: 0.5;
}

.empty-text {
  color: #9aa4b8;
  font-size: 26rpx;
}
</style>
