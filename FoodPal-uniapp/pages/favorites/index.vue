<template>
  <view class="page-content">
    <uni-card :border="false" padding="16" style="margin-bottom: 16rpx;">
      <view class="header-row">
        <text class="title">我的收藏</text>
        <text class="count">共 {{ favorites.length }} 个菜品</text>
      </view>
    </uni-card>

    <view v-if="favorites.length">
      <uni-card 
        v-for="item in favorites" 
        :key="item.id" 
        :border="false" 
        padding="16" 
        class="favorite-card"
        @click="goDishDetail(item)"
      >
        <view class="card-row">
          <image class="dish-image" :src="getImageUrl(item.dishImage)" mode="aspectFill" />
          <view class="dish-info">
            <text class="dish-name">{{ item.dishName }}</text>
            <text class="dish-merchant">{{ item.merchantName || '未知商户' }}</text>
            <view class="price-row">
              <text class="price">¥{{ item.currentPrice || item.dishPrice }}</text>
              <uni-tag 
                v-if="item.isSpecial" 
                text="特价" 
                type="error" 
                size="mini"
              />
              <uni-tag 
                :text="item.supplyStatus === 1 ? '可供应' : '已售罄'" 
                :type="item.supplyStatus === 1 ? 'success' : 'warning'" 
                size="mini"
              />
            </view>
            <view class="stats-row">
              <text class="stat-item">👍 {{ item.likeCount || 0 }}</text>
              <text class="stat-item">⭐ {{ item.favoriteCount || 0 }}</text>
            </view>
          </view>
        </view>
      </uni-card>
    </view>

    <view v-else class="empty-box">
      <text class="empty-icon">⭐</text>
      <text class="empty-text">还没有收藏任何菜品</text>
      <button class="empty-btn" @tap="goIndex">去首页看看</button>
    </view>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';

export default {
  data() {
    return {
      favorites: []
    };
  },
  onShow() {
    this.loadFavorites();
  },
  onPullDownRefresh() {
    this.loadFavorites().finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    async loadFavorites() {
      try {
        this.favorites = await canteenApi.myFavorites();
      } catch (error) {
        uni.showToast({ title: '加载失败', icon: 'none' });
        console.error(error);
        this.favorites = [];
      }
    },
    getImageUrl(url) {
      return normalizeFileUrl(url) || '/static/logo.png';
    },
    goDishDetail(item) {
      uni.navigateTo({ 
        url: `/pages/index/dish-detail?id=${item.id}&name=${encodeURIComponent(item.dishName)}`
      });
    },
    goIndex() {
      uni.switchTab({ url: '/pages/index/index' });
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
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  color: #1f2f4f;
}

.count {
  font-size: 24rpx;
  color: #6e7891;
}

.favorite-card {
  margin: 0 20rpx 16rpx;
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.card-row {
  display: flex;
  gap: 16rpx;
}

.dish-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background: #f5f7fa;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.dish-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2f4f;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-merchant {
  font-size: 22rpx;
  color: #9aa4b8;
  display: block;
  margin-top: 4rpx;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 8rpx;
}

.price {
  font-size: 32rpx;
  font-weight: bold;
  color: #f5576c;
}

.stats-row {
  display: flex;
  gap: 24rpx;
  margin-top: 8rpx;
}

.stat-item {
  font-size: 22rpx;
  color: #6e7891;
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
  display: block;
  margin-bottom: 32rpx;
}

.empty-btn {
  width: 240rpx;
  height: 72rpx;
  line-height: 72rpx;
  background: #2f65f9;
  color: #ffffff;
  border-radius: 36rpx;
  font-size: 26rpx;
  margin: 0 auto;
}
</style>
