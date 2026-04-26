<template>
  <view class="page-content">
    <uni-card :border="false" padding="24" style="margin-bottom: 16rpx;">
      <view class="header-row">
        <view class="no-row">
          <uni-tag :text="getPriorityText(complaint.createTime)" :type="getPriorityType(complaint.createTime)" size="mini" />
          <text class="no">{{ complaint.complaintNo || '#' + complaint.id }}</text>
        </view>
        <uni-tag :text="statusText(complaint.status)" :type="statusTag(complaint.status)" size="small" />
      </view>
      
      <text class="title">{{ complaint.complaintTitle }}</text>
      
      <view class="meta-grid">
        <view class="meta-item">
          <text class="meta-label">商户</text>
          <text class="meta-value">{{ complaint.merchantName || '-' }}</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">投诉人</text>
          <text class="meta-value">{{ complaint.studentName || '-' }}</text>
        </view>
        <view class="meta-item">
          <text class="meta-label">投诉时间</text>
          <text class="meta-value">{{ formatDateTime(complaint.createTime) }}</text>
        </view>
      </view>
    </uni-card>

    <uni-card :border="false" padding="24" style="margin-bottom: 16rpx;">
      <view class="section-title">投诉内容</view>
      <text class="content-text">{{ complaint.complaintContent || '-' }}</text>
      <view class="evidence-list" v-if="parseEvidenceUrls(complaint.evidenceUrls).length">
        <image
          v-for="(url, index) in parseEvidenceUrls(complaint.evidenceUrls)"
          :key="`${complaint.id || 'complaint'}-${index}`"
          class="evidence-image"
          :src="url"
          mode="aspectFill"
          @tap="previewEvidence(complaint.evidenceUrls, index)"
        />
      </view>
    </uni-card>

    <uni-card v-if="complaint.rectifyRequirement" :border="false" padding="24" style="margin-bottom: 16rpx;">
      <view class="section-title">整改要求</view>
      <text class="content-text">{{ complaint.rectifyRequirement }}</text>
    </uni-card>

    <uni-card v-if="complaint.feedback" :border="false" padding="24" style="margin-bottom: 16rpx;">
      <view class="section-title">处理反馈</view>
      <text class="content-text">{{ complaint.feedback }}</text>
    </uni-card>

    <uni-card v-if="complaint.status === 'completed' && complaint.processTime" :border="false" padding="24">
      <view class="section-title">完成信息</view>
      <text class="content-text">该投诉已于 {{ formatDateTime(complaint.processTime) }} 完成处理</text>
    </uni-card>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';

export default {
  data() {
    return {
      complaintId: null,
      complaint: {}
    };
  },
  onLoad(options) {
    if (options.id) {
      this.complaintId = Number(options.id);
      this.loadDetail();
    }
  },
  methods: {
    async loadDetail() {
      try {
        const data = await canteenApi.getComplaintDetail(this.complaintId);
        this.complaint = data || {};
      } catch (error) {
        uni.showToast({ title: '加载失败', icon: 'none' });
        console.error(error);
      }
    },
    statusText(status) {
      const map = {
        pending_review: '待审核',
        pending_rectify: '待整改',
        rectified: '待复核',
        completed: '已完成',
        rejected: '已驳回'
      };
      return map[status] || status || '未知';
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
          if (Array.isArray(parsed)) {
            return normalizeList(parsed);
          }
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
    formatDateTime(dateStr) {
      if (!dateStr) return '-';
      const date = new Date(dateStr);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hour = String(date.getHours()).padStart(2, '0');
      const minute = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hour}:${minute}`;
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

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
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

.title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2f4f;
  display: block;
  margin-bottom: 24rpx;
  line-height: 1.5;
}

.meta-grid {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-label {
  font-size: 24rpx;
  color: #9aa4b8;
}

.meta-value {
  font-size: 26rpx;
  color: #4a5568;
  font-weight: 500;
}

.section-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2f4f;
  display: block;
  margin-bottom: 16rpx;
}

.content-text {
  font-size: 26rpx;
  color: #4a5568;
  line-height: 1.8;
  display: block;
}

.evidence-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.evidence-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background: #f5f7fa;
}

.deadline-box {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #e8ecf0;
  display: flex;
  align-items: center;
}

.deadline-label {
  font-size: 24rpx;
  color: #9aa4b8;
}

.deadline-value {
  font-size: 24rpx;
  color: #f5576c;
  font-weight: 500;
}

.feedback-time {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #e8ecf0;
}

.time-text {
  font-size: 22rpx;
  color: #9aa4b8;
}
</style>
