<template>
  <view class="page-content">
    <!-- 搜索和筛选区域 -->
    <uni-card :border="false" padding="16" style="margin: 0 0 16rpx 0;">
      <view class="search-row">
        <uni-search-bar
          v-model="keyword"
          placeholder="搜索菜品名称或商户"
          cancel-button="none"
          @confirm="loadDishes(true)"
          @clear="loadDishes(true)"
          class="search-bar"
        />
        <button class="search-btn" size="mini" type="primary" @tap="loadDishes(true)">搜索</button>
      </view>
      <view style="margin-top: 16rpx;">
        <uni-segmented-control
          :current="categoryIndex"
          :values="categories"
          style-type="button"
          @clickItem="onCategoryChange"
        />
      </view>
      <view style="margin-top: 12rpx;" v-if="categoryIndex === 0">
        <uni-data-checkbox
          v-model="supplyStatusFilter"
          :localdata="supplyStatusOptions"
          multiple
          @change="onSupplyStatusChange"
        />
      </view>
    </uni-card>

    <!-- 统计卡片 -->
    <uni-section title="今日数据" type="line"></uni-section>
    <uni-card :border="false" padding="16" style="margin: 0 0 16rpx 0;">
      <view class="stat-grid">
        <view class="stat-item">
          <text class="stat-num">{{ statistics.totalDishes }}</text>
          <text class="stat-label">菜品总数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.availableDishes }}</text>
          <text class="stat-label">可供应</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.specialDishes }}</text>
          <text class="stat-label">特价中</text>
        </view>
      </view>
    </uni-card>

    <!-- 快捷功能 -->
    <uni-section title="快捷功能" type="line"></uni-section>
    <uni-card :border="false" padding="12" style="margin: 0 0 16rpx 0;">
      <view class="quick-grid" v-if="isStudent">
        <view class="quick-item" @tap="goComplaintCreate">
          <image class="quick-icon" src="/static/icon_med/xunwen.png" mode="aspectFit" />
          <text class="quick-text">发起投诉</text>
        </view>
        <view class="quick-item" @tap="goMyFavorites">
          <image class="quick-icon" src="/static/icon_med/jiangbei.png" mode="aspectFit" />
          <text class="quick-text">我的收藏</text>
        </view>
        <view class="quick-item" @tap="goContent">
          <image class="quick-icon" src="/static/icon_med/zixun.png" mode="aspectFit" />
          <text class="quick-text">资讯公告</text>
        </view>
      </view>
      <view class="quick-grid" v-else-if="isMerchant">
        <view class="quick-item" @tap="goMerchantProfile">
          <image class="quick-icon" src="/static/icon_med/shangdian.png" mode="aspectFit" />
          <text class="quick-text">店铺管理</text>
        </view>
        <view class="quick-item" @tap="goDishManage">
          <image class="quick-icon" src="/static/icon_med/fenlei.png" mode="aspectFit" />
          <text class="quick-text">菜品管理</text>
        </view>
        <view class="quick-item" @tap="goPublishDynamic">
          <image class="quick-icon" src="/static/icon_med/huodong.png" mode="aspectFit" />
          <text class="quick-text">发布动态</text>
        </view>
      </view>
      <view class="quick-grid" v-else-if="isSupervisor">
        <view class="quick-item" @tap="goComplaintManage">
          <image class="quick-icon" src="/static/icon_med/tixing.png" mode="aspectFit" />
          <text class="quick-text">投诉管理</text>
        </view>
        <view class="quick-item" @tap="goMerchantSupervise">
          <image class="quick-icon" src="/static/icon_med/shangdian.png" mode="aspectFit" />
          <text class="quick-text">商户监管</text>
        </view>
        <view class="quick-item" @tap="goContent">
          <image class="quick-icon" src="/static/icon_med/zixun.png" mode="aspectFit" />
          <text class="quick-text">资讯公告</text>
        </view>
      </view>
      <view class="quick-grid" v-else-if="isAdmin">
        <view class="quick-item" @tap="goComplaintManage">
          <image class="quick-icon" src="/static/icon_med/tixing.png" mode="aspectFit" />
          <text class="quick-text">投诉管理</text>
        </view>
        <view class="quick-item" @tap="goMerchantSupervise">
          <image class="quick-icon" src="/static/icon_med/shangdian.png" mode="aspectFit" />
          <text class="quick-text">商户管理</text>
        </view>
        <view class="quick-item" @tap="goContent">
          <image class="quick-icon" src="/static/icon_med/zixun.png" mode="aspectFit" />
          <text class="quick-text">资讯公告</text>
        </view>
      </view>
    </uni-card>

    <!-- 热门推荐 -->
    <uni-section title="热门推荐" type="line"></uni-section>
    <scroll-view scroll-x class="horizontal-scroll" v-if="recommendList.length">
      <view class="row-wrap">
        <view class="recommend-card" v-for="item in recommendList" :key="item.id" @tap="goDishDetail(item)">
          <image 
            class="recommend-image" 
            :src="getImageUrl(item.dishImage)" 
            mode="aspectFill"
            @error="onImageError($event, item)"
          />
          <view class="recommend-badge hot">HOT</view>
          <text class="recommend-name">{{ item.dishName }}</text>
          <text class="dish-meta">{{ item.merchantName || '未知商户' }}</text>
          <view class="dish-price-row">
            <text class="recommend-price">¥{{ item.currentPrice || item.dishPrice }}</text>
            <view class="dish-stats-mini">
              <image src="/static/icon_med/zan.png" class="stat-img" />{{ item.likeCount || 0 }}
              <image src="/static/icon_med/jiangbei.png" class="stat-img" />{{ item.favoriteCount || 0 }}
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    <view v-else class="empty-box">
      <text class="empty-text">暂无推荐</text>
    </view>

    <!-- 投诉排行榜 -->
    <uni-section title="投诉排行榜" type="line"></uni-section>
    <uni-card :border="false" padding="12" style="margin: 0 0 16rpx 0;" v-if="complaintRanking.length">
      <view class="ranking-list">
        <view class="ranking-item" v-for="(item, index) in complaintRanking.slice(0, 5)" :key="item.merchantId" @tap="goMerchantDetail(item)">
          <view class="ranking-badge" :class="{ top3: index < 3 }">{{ index + 1 }}</view>
          <text class="ranking-name">{{ item.merchantName || '未知商户' }}</text>
          <view class="ranking-right">
            <text class="ranking-count">{{ item.complaintCount }}条</text>
            <text class="ranking-badge complaint">投诉</text>
          </view>
        </view>
      </view>
    </uni-card>
    <view v-else class="empty-box" style="margin-bottom: 16rpx;">
      <text class="empty-text">暂无投诉数据</text>
    </view>

    <!-- 菜品列表 -->
    <uni-section title="菜品列表" type="line">
      <template v-slot:right>
        <text style="font-size: 24rpx; color: #2f65f9;" @tap="loadDishes(true)">刷新</text>
      </template>
    </uni-section>
    <view v-if="dishList.length">
      <uni-card v-for="item in dishList" :key="item.id" :border="false" padding="12" class="dish-card" @click="goDishDetail(item)">
        <view class="dish-list-item">
          <image
            class="list-dish-image"
            :src="getImageUrl(item.dishImage)"
            mode="aspectFill"
            @error="onImageError($event, item)"
          />
          <view class="dish-item-info">
            <view class="dish-item-main">
              <view class="dish-item-left">
                <text class="list-dish-name">{{ item.dishName }}</text>
                <view class="dish-item-tags">
                  <text class="dish-tag">{{ item.category || '未分类' }}</text>
                  <text class="dish-tag" :class="{ 'tag-success': item.supplyStatus === 1 }">
                    {{ item.supplyStatus === 1 ? '可供应' : '已售罄' }}
                  </text>
                  <text class="dish-tag tag-danger" v-if="item.isSpecial">特价</text>
                </view>
                <text class="dish-item-merchant">{{ item.merchantName || '未知商户' }}</text>
              </view>
              <view class="dish-item-right">
                <text class="list-dish-price">¥{{ item.currentPrice || item.dishPrice }}</text>
                <view class="dish-item-stats">
                  <text class="stat-text">{{ item.likeCount || 0 }}点赞</text>
                  <text class="stat-text">{{ item.favoriteCount || 0 }}收藏</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </uni-card>
    </view>
    <view v-else class="empty-box">
      <text class="empty-text">{{ searchTip }}</text>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore && dishList.length">
      <text class="load-more-text" @tap="loadMore">
        {{ loading ? '加载中...' : '加载更多' }}
      </text>
    </view>
  </view>
</template>

<script>
import { canteenApi, userApi } from '@/utils/api.js';
import { BASE_URL } from '@/utils/request.js';
import { isStudentRole, isMerchantRole, isSupervisorRole, isAdminRole } from '@/utils/permission.js';

export default {
  data() {
    return {
      userInfo: {},
      isStudent: false,
      isMerchant: false,
      isSupervisor: false,
      isAdmin: false,
      defaultImg: '/static/logo.png',
      categories: ['全部', '主食', '面食', '汤类', '凉菜', '小吃'],
      categoryIndex: 0,
      keyword: '',
      searchFocus: false,
      dishList: [],
      recommendList: [],
      complaintRanking: [],
      current: 1,
      pageSize: 10,
      hasMore: true,
      loading: false,
      supplyStatusFilter: [1],
      supplyStatusOptions: [
        { value: 1, text: '可供应' },
        { value: 0, text: '已售罄' }
      ],
      statistics: {
        totalDishes: 0,
        availableDishes: 0,
        specialDishes: 0
      }
    };
  },
  computed: {
    searchTip() {
      if (this.keyword) {
        return `换个关键词试试`;
      }
      if (this.categoryIndex > 0) {
        return `试试其他分类`;
      }
      return `暂无菜品数据`;
    }
  },
  onLoad() {
    this.loadUserInfo();
    this.loadDishes(true);
    this.loadRecommend();
    this.loadComplaintRanking();
    this.loadStatistics();
  },
  onPullDownRefresh() {
    Promise.all([
      this.loadDishes(true),
      this.loadRecommend(),
      this.loadComplaintRanking(),
      this.loadStatistics()
    ]).finally(() => uni.stopPullDownRefresh());
  },
  onReachBottom() {
    if (this.hasMore && !this.loading) {
      this.loadMore();
    }
  },
  methods: {
    async loadUserInfo() {
      try {
        this.userInfo = await userApi.fetchCurrentUser();
        uni.setStorageSync('userInfo', this.userInfo);
      } catch (error) {
        this.userInfo = uni.getStorageSync('userInfo') || {};
      }
      this.isStudent = isStudentRole(this.userInfo);
      this.isMerchant = isMerchantRole(this.userInfo);
      this.isSupervisor = isSupervisorRole(this.userInfo);
      this.isAdmin = isAdminRole(this.userInfo);
    },
    getImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:') || url.startsWith('blob:')) {
        return url;
      }
      if (url.startsWith('/static/')) {
        return url;
      }
      const apiBase = String(BASE_URL || '').replace(/\/+$/, '');
      const originBase = apiBase.endsWith('/api') ? apiBase.slice(0, -4) : apiBase;
      if (url.startsWith('/api/')) {
        return `${originBase}${url}`;
      }
      if (url.startsWith('/file/')) {
        return `${apiBase}${url}`;
      }
      if (url.startsWith('api/')) {
        return `${originBase}/${url}`;
      }
      if (url.startsWith('/')) {
        return `${originBase}${url}`;
      }
      return `${apiBase}/file/preview/${url}`;
    },
    onImageError(event, item) {
      // 图片加载失败时不做处理，保持布局
    },
    onCategoryChange(e) {
      this.categoryIndex = e.currentIndex;
      this.loadDishes(true);
      this.loadStatistics();
    },
    onSupplyStatusChange() {
      this.loadDishes(true);
      this.loadStatistics();
    },
    async loadDishes(reset = false) {
      if (this.loading) return;
      this.loading = true;
      
      if (reset) {
        this.current = 1;
        this.hasMore = true;
      }
      
      try {
        const category = this.categoryIndex === 0 ? undefined : this.categories[this.categoryIndex];
        const page = await canteenApi.listDishes({
          current: this.current,
          size: this.pageSize,
          keyword: this.keyword || undefined,
          category: category,
          supplyStatus: this.supplyStatusFilter.length === 1 ? this.supplyStatusFilter[0] : undefined
        });

        const records = page.records || [];

        if (reset) {
          this.dishList = records;
          // 重置时更新统计数据
          this.updateStatistics(page);
        } else {
          this.dishList = [...this.dishList, ...records];
        }
        this.hasMore = records.length === this.pageSize;
      } catch (error) {
        uni.showToast({ title: '加载菜品失败', icon: 'none' });
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    updateStatistics(page) {
      const dishes = page.records || [];
      this.statistics.totalDishes = page.total || 0;
      this.statistics.availableDishes = dishes.filter(d => d.supplyStatus === 1).length;
      this.statistics.specialDishes = dishes.filter(d => d.isSpecial).length;
    },
    async loadRecommend() {
      try {
        const page = await canteenApi.recommendDishes(10);
        const list = Array.isArray(page) ? page : [];
        const uniqueList = [];
        const idSet = new Set();
        for (const item of list) {
          if (!item || !item.id || idSet.has(item.id)) continue;
          idSet.add(item.id);
          uniqueList.push(item);
        }
        uniqueList.sort((a, b) => {
          const interactionA = Number(a.likeCount || 0) + Number(a.favoriteCount || 0);
          const interactionB = Number(b.likeCount || 0) + Number(b.favoriteCount || 0);
          if (interactionB !== interactionA) return interactionB - interactionA;
          return Number(b.likeCount || 0) - Number(a.likeCount || 0);
        });
        this.recommendList = uniqueList;
      } catch (error) {
        console.error(error);
        this.recommendList = [];
      }
    },
    async loadComplaintRanking() {
      try {
        const data = await canteenApi.getComplaintRanking(5);
        this.complaintRanking = data || [];
      } catch (error) {
        console.error(error);
        this.complaintRanking = [];
      }
    },
    async loadStatistics() {
      try {
        const category = this.categoryIndex === 0 ? undefined : this.categories[this.categoryIndex];
        const page = await canteenApi.listDishes({
          current: 1,
          size: 10000,
          keyword: this.keyword || undefined,
          category: category,
          supplyStatus: this.supplyStatusFilter.length === 1 ? this.supplyStatusFilter[0] : undefined
        });
        const dishes = page.records || [];
        this.statistics.totalDishes = page.total || 0;
        this.statistics.availableDishes = dishes.filter(d => d.supplyStatus === 1).length;
        this.statistics.specialDishes = dishes.filter(d => d.isSpecial).length;
      } catch (error) {
        console.error(error);
      }
    },
    async loadMore() {
      if (this.hasMore && !this.loading) {
        this.current++;
        await this.loadDishes(false);
      }
    },
    async toggleLike(item) {
      try {
        await canteenApi.toggleLike(item.id);
        item.liked = !item.liked;
        item.likeCount = (item.likeCount || 0) + (item.liked ? 1 : -1);
        uni.showToast({ 
          title: item.liked ? '已点赞' : '已取消点赞', 
          icon: 'none',
          duration: 1000
        });
      } catch (error) {
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },
    async toggleFavorite(item) {
      try {
        await canteenApi.toggleFavorite(item.id);
        item.favorited = !item.favorited;
        item.favoriteCount = (item.favoriteCount || 0) + (item.favorited ? 1 : -1);
        uni.showToast({ 
          title: item.favorited ? '已收藏' : '已取消收藏', 
          icon: 'none',
          duration: 1000
        });
      } catch (error) {
        uni.showToast({ title: '操作失败', icon: 'none' });
      }
    },
    goDishDetail(item) {
      const query = [
        `id=${item.id}`,
        `name=${encodeURIComponent(item.dishName || '')}`,
        `dishImage=${encodeURIComponent(item.dishImage || '')}`,
        `merchantName=${encodeURIComponent(item.merchantName || '')}`,
        `likeCount=${item.likeCount || 0}`,
        `favoriteCount=${item.favoriteCount || 0}`,
        `liked=${item.liked ? 1 : 0}`,
        `favorited=${item.favorited ? 1 : 0}`
      ].join('&');
      uni.navigateTo({ 
        url: `/pages/index/dish-detail?${query}`
      });
    },
    goComplaintCreate() {
      uni.navigateTo({ url: '/pages/appointment/index' });
    },
    goMyFavorites() {
      uni.navigateTo({ url: '/pages/favorites/index' });
    },
    goContent() {
      uni.navigateTo({ url: '/pages/content/list' });
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
    goComplaintManage() {
      uni.navigateTo({ url: '/pages/supervisor/complaints' });
    },
    goMerchantSupervise() {
      uni.navigateTo({ url: '/pages/supervisor/merchants' });
    },
    goUserManage() {
      uni.navigateTo({ url: '/pages/supervisor/complaints' });
    },
    goMerchantManage() {
      uni.navigateTo({ url: '/pages/supervisor/merchants' });
    },
    goAnnouncementManage() {
      uni.navigateTo({ url: '/pages/content/list?tab=announcement' });
    },
    goMerchantDetail(item) {
      const merchantId = item?.merchantId || item?.id;
      const merchantName = encodeURIComponent(item?.merchantName || '');
      if (!merchantId) return;
      uni.navigateTo({
        url: `/pages/complaint/list?merchantId=${merchantId}&merchantName=${merchantName}`
      });
    }
  }
};
</script>

<style scoped>
.page-content {
  min-height: 100vh;
  background: #f5f5f5;
  box-sizing: border-box;
  width: 100%;
  max-width: 100vw;
  padding: calc(20rpx + env(safe-area-inset-top)) 20rpx calc(40rpx + env(safe-area-inset-bottom));
  overflow-x: hidden;
  overscroll-behavior-x: none;
}

.quick-grid {
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 8rpx;
}

.quick-item {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 14rpx 8rpx;
  border-radius: 12rpx;
  background: #f8f9fb;
}

.quick-icon {
  width: 44rpx;
  height: 44rpx;
  margin-bottom: 8rpx;
}

.quick-text {
  font-size: 22rpx;
  color: #1f2f4f;
  line-height: 1.3;
  text-align: center;
}

.search-container {
  margin-bottom: 12rpx;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.search-bar {
  flex: 1;
  background: #f5f5f5;
  border-radius: 50rpx;
}

.search-btn {
  flex-shrink: 0;
  height: 72rpx;
  line-height: 72rpx;
  padding: 0 28rpx;
  border-radius: 36rpx;
  background: #2f65f9;
  color: #fff;
}

.filter-row {
  display: flex;
  justify-content: center;
  align-items: center;
}

.segment-control {
  width: 100%;
  background: #f5f5f5;
  border-radius: 50rpx;
}

.checkbox-filter {
  margin: 12rpx 0;
  display: flex;
  justify-content: center;
  gap: 32rpx;
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
  color: #999;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12rpx 16rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  position: relative;
}

.ranking-badge {
  width: 48rpx;
  height: 48rpx;
  line-height: 48rpx;
  text-align: center;
  border-radius: 50%;
  background: #999;
  color: #ffffff;
  font-weight: bold;
  margin-right: 16rpx;
  font-size: 24rpx;
}

.ranking-badge.top3 {
  background: #f5576c;
}

.ranking-badge.complaint {
  background: #ff6b6b;
}

.ranking-name {
  flex: 1;
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
}

.ranking-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4rpx;
}

.ranking-count {
  font-size: 24rpx;
  color: #666;
}

.horizontal-scroll {
  white-space: nowrap;
  width: 100%;
  box-sizing: border-box;
  display: block;
  margin: 0 0 20rpx 0;
  padding: 0 4rpx;
}

.row-wrap {
  display: inline-flex;
  gap: 16rpx;
  padding-right: 4rpx;
}

.recommend-card {
  position: relative;
  display: inline-block;
  width: 280rpx;
}

.recommend-image {
  width: 280rpx;
  height: 200rpx;
  border-radius: 12rpx;
  display: block;
  margin-bottom: 12rpx;
  background: #f0f0f0;
}

.recommend-badge {
  position: absolute;
  top: 8rpx;
  left: 8rpx;
  padding: 4rpx 12rpx;
  background: #ff6b6b;
  color: #ffffff;
  border-radius: 8rpx;
  font-size: 20rpx;
  font-weight: bold;
}

.recommend-badge.hot {
  background: #f5576c;
}

.recommend-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 4rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-meta {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-bottom: 8rpx;
}

.dish-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recommend-price {
  font-size: 32rpx;
  font-weight: bold;
  color: #ff6b00;
}

.dish-stats-mini {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 20rpx;
  color: #999;
}

.stat-img {
  width: 20rpx;
  height: 20rpx;
  margin-right: 4rpx;
  vertical-align: middle;
  flex-shrink: 0;
}

.dish-card {
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0.06);
}

.dish-row {
  display: flex;
  gap: 16rpx;
}

.dish-thumb {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
  background: #f0f0f0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.dish-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8rpx;
}

.dish-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
  margin-right: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-tags {
  display: flex;
  gap: 8rpx;
}

.dish-merchant {
  font-size: 24rpx;
  color: #666;
  margin-bottom: 6rpx;
}

.dish-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.dish-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.dish-current {
  font-size: 32rpx;
  font-weight: bold;
  color: #f5576c;
}

.dish-origin {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 8rpx;
}

.dish-stats {
  display: flex;
  gap: 20rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.stat-icon {
  font-size: 24rpx;
}

.stat-count {
  font-size: 24rpx;
  color: #666;
}

.op-row {
  display: flex;
  gap: 12rpx;
  margin-top: 8rpx;
}

.op-btn {
  padding: 8rpx 16rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 50rpx;
  background: #ffffff;
  font-size: 24rpx;
  color: #666;
}

.op-btn.active {
  background: #f0f0f0;
  color: #333;
}

.op-btn.detail {
  background: #f5f5f5;
  color: #333;
}

.empty-box {
  text-align: center;
  padding: 60rpx 32rpx;
  margin-bottom: 100rpx;
}

.empty-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 16rpx;
  opacity: 0.5;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  display: block;
  margin-bottom: 8rpx;
}

.empty-tip {
  font-size: 24rpx;
  color: #bbb;
}

.load-more {
  text-align: center;
  padding: 20rpx;
  margin-bottom: 100rpx;
}

.load-more-text {
  color: #666;
  font-size: 28rpx;
  padding: 16rpx 32rpx;
  background: #f5f5f5;
  border-radius: 8rpx;
}

.dish-card {
  margin-bottom: 12rpx;
  cursor: pointer;
}

.dish-list-item {
  display: flex;
  width: 100%;
  align-items: flex-start;
  gap: 16rpx;
  padding: 16rpx 0;
}

.list-dish-image {
  width: 160rpx;
  height: 160rpx;
  flex-shrink: 0;
  border-radius: 8rpx;
  background: #f0f0f0;
}

.dish-item-info {
  flex: 1;
  width: 0;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.dish-item-main {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16rpx;
}

.dish-item-left {
  flex: 1;
  min-width: 0;
  max-width: calc(100% - 216rpx);
}

.dish-item-right {
  margin-left: auto;
  min-width: 200rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 20rpx;
}

.list-dish-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2f4f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 10rpx;
}

.list-dish-price {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #ff6b00;
  text-align: right;
  width: 100%;
}

.dish-item-tags {
  display: flex;
  gap: 8rpx;
  margin-bottom: 10rpx;
  flex-wrap: wrap;
}

.dish-tag {
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  background: #f0f0f0;
  color: #666;
}

.tag-success {
  background: #e8f5e9;
  color: #2e7d32;
}

.tag-danger {
  background: #ffebee;
  color: #c62828;
}

.dish-item-merchant {
  display: block;
  font-size: 24rpx;
  color: #6e7891;
}

.dish-item-stats {
  display: flex;
  gap: 12rpx;
  justify-content: flex-end;
  width: 100%;
  white-space: nowrap;
}

.stat-text {
  font-size: 22rpx;
  color: #9aa4b8;
  white-space: nowrap;
  text-align: right;
}
</style>
