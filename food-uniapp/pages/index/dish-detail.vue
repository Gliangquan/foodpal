<template>
  <view class="page-content">
    <image class="cover-image" :src="getImageUrl(dish.dishImage)" mode="aspectFill" />

    <view class="info-card">
      <view class="title-row">
        <text class="dish-name">{{ dish.dishName || dishName || '菜品详情' }}</text>
        <view class="price-wrap">
          <text class="price-main">¥{{ displayPrice }}</text>
          <text v-if="showOriginPrice" class="price-origin">¥{{ dish.dishPrice }}</text>
        </view>
      </view>

      <view class="tag-row">
        <uni-tag :text="dish.category || '未分类'" size="mini" />
        <uni-tag :text="dish.supplyStatus === 1 ? '可供应' : '已售罄'" :type="dish.supplyStatus === 1 ? 'success' : 'warning'" size="mini" />
        <uni-tag v-if="isSpecialActive" text="特价中" type="error" size="mini" />
      </view>

      <text class="meta-text">商户：{{ dish.merchantName || (dish.merchantId ? `ID ${dish.merchantId}` : '未知商户') }}</text>
      <text class="meta-text">点赞：{{ dish.likeCount || 0 }} · 收藏：{{ dish.favoriteCount || 0 }}</text>

      <view class="section">
        <text class="section-title">食材组成</text>
        <text class="section-content">{{ dish.ingredients || '暂无' }}</text>
      </view>

      <view class="section">
        <text class="section-title">营养信息</text>
        <text class="section-content">{{ dish.nutritionInfo || '暂无' }}</text>
      </view>
    </view>

    <view class="action-bar">
      <button class="action-btn" :class="{ active: dish.liked }" :disabled="liking" @tap="toggleLike">
        {{ dish.liked ? '已点赞' : '点赞' }} {{ dish.likeCount || 0 }}
      </button>
      <button class="action-btn" :class="{ active: dish.favorited }" :disabled="favoriting" @tap="toggleFavorite">
        {{ dish.favorited ? '已收藏' : '收藏' }} {{ dish.favoriteCount || 0 }}
      </button>
    </view>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';

const parseNumber = (value, defaultValue = 0) => {
  const num = Number(value);
  return Number.isFinite(num) ? num : defaultValue;
};

export default {
  data() {
    return {
      dishId: 0,
      dishName: '',
      dish: {
        id: 0,
        dishName: '',
        dishImage: '',
        merchantId: null,
        merchantName: '',
        category: '',
        dishPrice: 0,
        currentPrice: 0,
        specialPrice: null,
        specialStartTime: '',
        specialEndTime: '',
        supplyStatus: 1,
        likeCount: 0,
        favoriteCount: 0,
        liked: false,
        favorited: false,
        ingredients: '',
        nutritionInfo: ''
      },
      liking: false,
      favoriting: false
    };
  },
  computed: {
    isSpecialActive() {
      if (!this.dish.specialPrice || !this.dish.specialStartTime || !this.dish.specialEndTime) return false;
      const now = Date.now();
      const start = new Date(this.dish.specialStartTime).getTime();
      const end = new Date(this.dish.specialEndTime).getTime();
      return Number.isFinite(start) && Number.isFinite(end) && start <= now && now < end;
    },
    displayPrice() {
      if (this.isSpecialActive) {
        return this.dish.specialPrice || this.dish.currentPrice || this.dish.dishPrice || 0;
      }
      return this.dish.currentPrice || this.dish.dishPrice || 0;
    },
    showOriginPrice() {
      return this.isSpecialActive && Number(this.dish.specialPrice) < Number(this.dish.dishPrice);
    }
  },
  onLoad(options = {}) {
    this.dishId = parseNumber(options.id, 0);
    this.dishName = options.name ? decodeURIComponent(options.name) : '';
    this.dish = {
      ...this.dish,
      id: this.dishId || this.dish.id,
      dishName: this.dishName || this.dish.dishName,
      dishImage: options.dishImage ? decodeURIComponent(options.dishImage) : this.dish.dishImage,
      merchantName: options.merchantName ? decodeURIComponent(options.merchantName) : this.dish.merchantName,
      likeCount: parseNumber(options.likeCount, this.dish.likeCount),
      favoriteCount: parseNumber(options.favoriteCount, this.dish.favoriteCount),
      liked: options.liked === '1' || options.liked === 'true',
      favorited: options.favorited === '1' || options.favorited === 'true'
    };
    this.loadDetail();
    this.syncFavoriteState();
    this.syncLikeState();
  },
  methods: {
    getImageUrl(url) {
      return normalizeFileUrl(url) || '/static/logo.png';
    },
    async loadDetail() {
      if (!this.dishId) {
        uni.showToast({ title: '缺少菜品ID', icon: 'none' });
        return;
      }
      try {
        const detail = await canteenApi.getDishDetail(this.dishId);
        if (detail) {
          this.dish = { ...this.dish, ...detail };
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载详情失败', icon: 'none' });
      }
    },
    async syncFavoriteState() {
      try {
        const favorites = await canteenApi.myFavorites();
        const matched = (favorites || []).find(item => Number(item.id) === Number(this.dishId));
        if (matched) {
          this.dish.favorited = true;
          if (matched.favoriteCount !== undefined) this.dish.favoriteCount = matched.favoriteCount;
        } else {
          this.dish.favorited = false;
        }
      } catch (error) {
        // 忽略收藏状态同步失败，不阻塞详情展示
      }
    },
    async syncLikeState() {
      try {
        const page = await canteenApi.listDishes({
          current: 1,
          size: 20,
          keyword: this.dish.dishName || undefined
        });
        const records = page?.records || [];
        const matched = records.find(item => Number(item.id) === Number(this.dishId));
        if (matched) {
          this.dish.liked = !!matched.liked;
          if (matched.likeCount !== undefined) this.dish.likeCount = matched.likeCount;
          if (matched.favoriteCount !== undefined) this.dish.favoriteCount = matched.favoriteCount;
        }
      } catch (error) {
        // 忽略点赞状态同步失败，不阻塞详情展示
      }
    },
    async toggleLike() {
      if (!this.dishId || this.liking) return;
      this.liking = true;
      try {
        await canteenApi.toggleLike(this.dishId);
        const next = !this.dish.liked;
        this.dish.liked = next;
        this.dish.likeCount = Math.max(0, (this.dish.likeCount || 0) + (next ? 1 : -1));
        uni.showToast({ title: next ? '已点赞' : '已取消点赞', icon: 'none' });
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' });
      } finally {
        this.liking = false;
      }
    },
    async toggleFavorite() {
      if (!this.dishId || this.favoriting) return;
      this.favoriting = true;
      try {
        await canteenApi.toggleFavorite(this.dishId);
        const next = !this.dish.favorited;
        this.dish.favorited = next;
        this.dish.favoriteCount = Math.max(0, (this.dish.favoriteCount || 0) + (next ? 1 : -1));
        uni.showToast({ title: next ? '已收藏' : '已取消收藏', icon: 'none' });
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' });
      } finally {
        this.favoriting = false;
      }
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.page-content {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.cover-image {
  width: 100%;
  height: 420rpx;
  background: #eef2f7;
}

.info-card {
  background: #ffffff;
  margin-top: -24rpx;
  border-radius: 24rpx 24rpx 0 0;
  padding: 28rpx 24rpx;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.dish-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2f4f;
  flex: 1;
  line-height: 1.4;
}

.price-wrap {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.price-main {
  font-size: 40rpx;
  font-weight: 700;
  color: #ff6b00;
}

.price-origin {
  font-size: 24rpx;
  color: #9aa4b8;
  text-decoration: line-through;
}

.tag-row {
  display: flex;
  gap: 10rpx;
  margin-top: 16rpx;
  flex-wrap: wrap;
}

.meta-text {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  color: #617198;
}

.section {
  margin-top: 24rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2f4f;
  margin-bottom: 10rpx;
}

.section-content {
  display: block;
  font-size: 26rpx;
  color: #4f5f7d;
  line-height: 1.6;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  gap: 16rpx;
  padding: 16rpx 24rpx calc(16rpx + env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1rpx solid #e8edf5;
}

.action-btn {
  flex: 1;
  border-radius: 999rpx;
  background: #eef3ff;
  color: #2f65f9;
  font-size: 28rpx;
  font-weight: 600;
  border: none;
}

.action-btn.active {
  background: #2f65f9;
  color: #ffffff;
}
</style>

