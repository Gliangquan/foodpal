<template>
  <view class="page-content">
    <view class="header-card">
      <text class="title">商户监管</text>
      <text class="sub">查看所有商户信息，监督商户运营情况</text>
    </view>

    <uni-card :border="false" padding="12" style="margin: 0 0 20rpx 0;">
      <uni-search-bar v-model="keyword" placeholder="搜索商户名称" @confirm="loadMerchants(true)" @clear="loadMerchants(true)" />
    </uni-card>

    <view v-if="merchantList.length">
      <uni-card v-for="item in merchantList" :key="item.id" :border="false" padding="18" class="merchant-card">
        <view class="merchant-header">
          <image class="merchant-avatar" :src="item.avatar" mode="aspectFill" />
          <view class="merchant-info">
            <text class="merchant-name">{{ item.merchantName }}</text>
            <text class="merchant-contact">{{ item.contactName }} · {{ item.contactPhone }}</text>
            <view class="merchant-tags">
              <uni-tag :text="item.status === 1 ? '营业中' : '已停业'" :type="item.status === 1 ? 'success' : 'default'" size="small" />
              <uni-tag text="查看详情" type="primary" size="small" @tap="viewMerchantDetail(item)" />
            </view>
          </view>
        </view>

        <view class="merchant-stats">
          <view class="stat-item">
            <text class="stat-num">{{ item.dishCount || 0 }}</text>
            <text class="stat-label">菜品数</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ item.complaintCount || 0 }}</text>
            <text class="stat-label">投诉数</text>
          </view>
          <view class="stat-item">
            <text class="stat-num">{{ item.pendingComplaintCount || 0 }}</text>
            <text class="stat-label">待处理</text>
          </view>
        </view>

        <view class="merchant-location">
          <text class="label">地址：</text>
          <text class="value">{{ item.location || '暂无地址信息' }}</text>
        </view>

        <view class="merchant-hours">
          <text class="label">营业时间：</text>
          <text class="value">{{ item.businessHours || '暂无信息' }}</text>
        </view>

        <view class="op-row">
          <button size="mini" @tap="viewComplaints(item)">查看投诉</button>
          <button size="mini" type="warn" v-if="item.status === 1" @tap="sendViolationNotice(item)">违规提醒</button>
        </view>
      </uni-card>
    </view>

    <view v-else class="empty-box">
      <text class="empty-text">暂无商户数据</text>
    </view>

    <view class="load-more" v-if="hasMore && merchantList.length">
      <text class="load-more-text" @tap="loadMore">加载更多</text>
    </view>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';

export default {
  data() {
    return {
      defaultImg: '/static/logo.png',
      keyword: '',
      merchantList: [],
      current: 1,
      pageSize: 10,
      hasMore: true,
      loading: false
    };
  },
  onShow() {
    this.loadMerchants(true);
  },
  onPullDownRefresh() {
    this.loadMerchants(true).finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    async loadMerchants(reset = false) {
      if (this.loading) return;
      this.loading = true;
      if (reset) {
        this.current = 1;
        this.hasMore = true;
      }
      try {
        const page = await canteenApi.listAllMerchants({
          current: this.current,
          size: this.pageSize,
          keyword: this.keyword.trim() || undefined
        });

        const records = page.records || [];
        // 为每个商户加载统计信息
        for (let merchant of records) {
          await this.loadMerchantStats(merchant);
        }

        if (reset) {
          this.merchantList = records;
        } else {
          this.merchantList = [...this.merchantList, ...records];
        }
        this.hasMore = records.length === this.pageSize;
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    async loadMerchantStats(merchant) {
      try {
        // 获取菜品数
        const dishRes = await canteenApi.listDishes({
          current: 1,
          size: 1,
          merchantId: merchant.id
        });
        merchant.dishCount = dishRes.total || 0;

        // 获取投诉数
        const complaintRes = await canteenApi.listAllComplaints({
          current: 1,
          size: 1,
          merchantId: merchant.id
        });
        merchant.complaintCount = complaintRes.total || 0;
      } catch (error) {
        console.error('加载商户统计失败:', error);
      }
    },
    loadMore() {
      if (!this.hasMore || this.loading) return;
      this.current += 1;
      this.loadMerchants(false);
    },
    viewMerchantDetail(item) {
      uni.navigateTo({
        url: `/pages/supervisor/complaints?merchantId=${item.id}&merchantName=${encodeURIComponent(item.merchantName || '')}`
      });
    },
    viewComplaints(item) {
      uni.navigateTo({
        url: `/pages/supervisor/complaints?merchantId=${item.id}&merchantName=${encodeURIComponent(item.merchantName || '')}`
      });
    },
    sendViolationNotice(item) {
      uni.showModal({
        title: '发送违规提醒',
        content: '确定要向该商户发送违规提醒吗？',
        editable: true,
        placeholderText: '请输入违规提醒内容',
        success: async (res) => {
          if (res.confirm && res.content) {
            try {
              await canteenApi.sendViolationNotice({
                merchantId: item.id,
                content: res.content
              });
              uni.showToast({ title: '提醒已发送', icon: 'success' });
            } catch (error) {
              uni.showToast({ title: error.message || '发送失败', icon: 'none' });
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

.merchant-card {
  margin-bottom: 14rpx;
}

.merchant-header {
  display: flex;
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.merchant-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background-color: #f5f7fa;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
}

.merchant-name {
  display: block;
  font-size: 30rpx;
  color: #1c2b4a;
  font-weight: 700;
}

.merchant-contact {
  display: block;
  margin-top: 6rpx;
  font-size: 24rpx;
  color: #6e7891;
}

.merchant-tags {
  margin-top: 10rpx;
  display: flex;
  gap: 12rpx;
}

.merchant-stats {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 0;
  border-top: 1rpx solid #f0f2f5;
  border-bottom: 1rpx solid #f0f2f5;
  margin-bottom: 16rpx;
}

.stat-item {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #2f65f9;
}

.stat-label {
  display: block;
  font-size: 22rpx;
  color: #6d7892;
  margin-top: 6rpx;
}

.merchant-location,
.merchant-hours {
  display: flex;
  margin-bottom: 8rpx;
}

.label {
  font-size: 24rpx;
  color: #6d7892;
  width: 140rpx;
}

.value {
  font-size: 24rpx;
  color: #1c2b4a;
  flex: 1;
}

.op-row {
  margin-top: 16rpx;
  display: flex;
  gap: 12rpx;
}

.empty-box {
  text-align: center;
  padding: 80rpx 0;
}

.empty-text {
  color: #9aa4b8;
  font-size: 26rpx;
}

.load-more {
  text-align: center;
  margin: 20rpx 0;
}

.load-more-text {
  color: #2f65f9;
  font-size: 24rpx;
}
</style>
