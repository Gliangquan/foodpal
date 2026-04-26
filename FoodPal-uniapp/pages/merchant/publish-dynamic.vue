<template>
  <view class="page-content">
    <view class="header-card">
      <text class="title">发布动态</text>
      <text class="sub">提交店铺动态、活动或通知，等待监督员审核</text>
    </view>

    <uni-card :border="false" padding="24">
      <uni-forms :model="dynamicForm" label-position="top">
        <uni-forms-item label="动态标题" required>
          <uni-easyinput v-model="dynamicForm.title" placeholder="请输入动态标题" maxlength="50" />
        </uni-forms-item>

        <uni-forms-item label="动态类型">
          <uni-data-checkbox v-model="dynamicForm.newsType" :localdata="newsTypeOptions" />
        </uni-forms-item>

        <uni-forms-item label="封面图片">
          <view class="image-upload" @tap="uploadCover">
            <image v-if="dynamicForm.coverImage" :src="getImageUrl(dynamicForm.coverImage)" mode="aspectFill" class="preview-img" />
            <view v-else class="upload-placeholder">
              <text class="icon">+</text>
              <text class="text">点击上传封面</text>
            </view>
          </view>
        </uni-forms-item>

        <uni-forms-item label="动态内容" required>
          <uni-easyinput v-model="dynamicForm.content" type="textarea" placeholder="请输入动态内容，介绍您的店铺活动、新品上市等信息" maxlength="500" />
        </uni-forms-item>

        <uni-forms-item label="展示截止时间">
          <view class="expire-tip">商户可自行设置截止时间，监督员审核时会判断是否合理；不填则默认长期展示。</view>
          <uni-datetime-picker v-model="dynamicForm.expireTime" type="datetime" />
        </uni-forms-item>
      </uni-forms>

      <button class="submit-btn" type="primary" @tap="publishDynamic">提交审核</button>
    </uni-card>

    <view class="history-section">
      <text class="section-title">我的动态记录</text>
      <view v-if="historyList.length">
        <uni-card v-for="item in historyList" :key="item.id" :border="false" padding="16" class="history-card">
          <view class="history-row">
            <image v-if="item.coverImage" :src="getImageUrl(item.coverImage)" mode="aspectFill" class="history-img" />
            <view class="history-info">
              <text class="history-title">{{ item.title }}</text>
              <text class="history-time">{{ format(item.createTime) }}</text>
              <uni-tag :text="getAuditStatusText(item)" :type="getAuditStatusType(item)" size="small" />
            </view>
          </view>
        </uni-card>
      </view>
      <view v-else class="empty-box">
        <text class="empty-text">暂无动态记录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { formatDateTime, normalizeFileUrl } from '@/utils/format.js';

export default {
  data() {
    return {
      defaultImg: '/static/logo.png',
      dynamicForm: {
        title: '',
        content: '',
        coverImage: '',
        newsType: 'notice',
        expireTime: ''
      },
      newsTypeOptions: [
        { text: '新品上市', value: 'new_dish' },
        { text: '优惠活动', value: 'activity' },
        { text: '店铺通知', value: 'notice' }
      ],
      historyList: []
    };
  },
  onShow() {
    this.loadHistory();
  },
  methods: {
    format(value) {
      return formatDateTime(value);
    },
    getAuditStatusText(item) {
      if (item?.auditStatus === 'rejected') return '已驳回';
      if (item?.auditStatus === 'approved' || item?.status === 'published') return '已发布';
      return '待审核';
    },
    getAuditStatusType(item) {
      if (item?.auditStatus === 'rejected') return 'error';
      if (item?.auditStatus === 'approved' || item?.status === 'published') return 'success';
      return 'warning';
    },
    uploadCover() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          this.uploadFile(tempFilePath);
        }
      });
    },
    async uploadFile(filePath) {
      try {
        uni.showLoading({ title: '上传中...' });
        const result = await canteenApi.uploadFile(filePath, 'dynamic');
        const uploadedUrl = result?.url || result?.raw?.url || (typeof result?.raw === 'string' ? result.raw : '');
        if (uploadedUrl) {
          this.dynamicForm.coverImage = uploadedUrl;
          uni.showToast({ title: '上传成功', icon: 'success' });
        } else {
          uni.showToast({ title: '上传成功但地址为空', icon: 'none' });
        }
      } catch (error) {
        uni.showToast({ title: error.message || '上传失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    async publishDynamic() {
      if (!this.dynamicForm.title) {
        uni.showToast({ title: '请输入动态标题', icon: 'none' });
        return;
      }
      if (!this.dynamicForm.content) {
        uni.showToast({ title: '请输入动态内容', icon: 'none' });
        return;
      }

      try {
        uni.showLoading({ title: '提交中...' });
        await canteenApi.publishDynamic(this.dynamicForm);
        uni.showToast({ title: '已提交，等待审核', icon: 'success' });
        // 重置表单
        this.dynamicForm = {
          title: '',
          content: '',
          coverImage: '',
          newsType: 'notice',
          expireTime: ''
        };
        // 刷新历史记录
        await this.loadHistory();
      } catch (error) {
        uni.showToast({ title: error.message || '发布失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    async loadHistory() {
      try {
        const result = await canteenApi.listMyDynamics({ current: 1, size: 10 });
        this.historyList = result.records || [];
      } catch (error) {
        console.error('加载历史记录失败:', error);
        this.historyList = [];
        uni.showToast({ title: error.message || '加载动态记录失败', icon: 'none' });
      }
    },
    getImageUrl(imageUrl) {
      if (!imageUrl || imageUrl.trim() === '') {
        return '';
      }
      return normalizeFileUrl(imageUrl);
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

.image-upload {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed #d8e2ff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #9aa4b8;
}

.icon {
  font-size: 60rpx;
  line-height: 1;
}

.text {
  font-size: 24rpx;
  margin-top: 10rpx;
}

.preview-img {
  width: 100%;
  height: 100%;
}

.expire-tip {
  margin-bottom: 12rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6d7892;
}

.submit-btn {
  margin-top: 30rpx;
}

.history-section {
  margin-top: 30rpx;
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #1e2f4f;
  margin-bottom: 20rpx;
  padding: 0 24rpx;
}

.history-card {
  margin-bottom: 14rpx;
}

.history-row {
  display: flex;
  gap: 16rpx;
}

.history-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 10rpx;
  background-color: #f5f7fa;
}

.history-info {
  flex: 1;
}

.history-title {
  display: block;
  font-size: 28rpx;
  color: #1c2b4a;
  font-weight: 600;
}

.history-time {
  display: block;
  font-size: 22rpx;
  color: #6d7892;
  margin-top: 8rpx;
}

.empty-box {
  text-align: center;
  padding: 60rpx 0;
}

.empty-text {
  color: #9aa4b8;
  font-size: 26rpx;
}
</style>
