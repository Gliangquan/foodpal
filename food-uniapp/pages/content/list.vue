<template>
  <view class="page-content">
    <view class="header-card">
      <text class="title">商户动态 / 系统公告</text>
      <text class="sub">查看商户发布内容与平台公告，不展示互动数据</text>
    </view>

    <!-- 标签切换 -->
    <uni-segmented-control
      :values="tabLabels"
      :current="tabIndex"
      style-type="button"
      active-color="#2f65f9"
      @clickItem="onTabChange"
      style="margin: 0 20rpx 16rpx;"
    />

    <!-- 统计卡片 -->
    <uni-card 
      :border="false" 
      padding="16" 
      style="margin: 0 20rpx 16rpx;" 
      :style="{ background: tabIndex === 0 ? '#f0f0f0' : '#f5f5f5' }"
    >
      <view class="stat-grid">
        <view class="stat-item">
          <text class="stat-num">{{ tabIndex === 0 ? statistics.dynamics.total : statistics.announcements.total }}</text>
          <text class="stat-label">总数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ tabIndex === 0 ? statistics.dynamics.published : statistics.announcements.published }}</text>
          <text class="stat-label">已发布</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ tabIndex === 0 ? statistics.dynamics.merchants : statistics.announcements.pinned }}</text>
          <text class="stat-label">{{ tabIndex === 0 ? '涉及商户' : '置顶' }}</text>
        </view>
      </view>
    </uni-card>

    <!-- 搜索 -->
    <uni-card :border="false" padding="16" style="margin: 0 20rpx 16rpx;">
      <uni-search-bar
        v-model="keyword"
        :placeholder="tabIndex === 0 ? '搜索动态标题' : '搜索公告标题'"
        cancel-button="none"
        @confirm="fetchList(true)"
        @clear="fetchList(true)"
        class="search-bar"
      />
    </uni-card>

    <!-- 列表 -->
    <view v-if="list.length" class="list-wrap">
      <uni-card
        v-for="item in list"
        :key="`${item.type}-${item.id}`"
        :border="false"
        padding="20"
        class="article-card"
        @tap="goDetail(item)"
      >
        <view class="card-header">
          <view class="card-head">
            <text class="card-title">{{ item.title }}</text>
            <uni-tag 
              :text="item.type === 'announcement' ? '公告' : '动态'" 
              :type="item.type === 'announcement' ? 'warning' : 'primary'" 
              size="small" 
            />
          </view>
          <uni-tag 
            v-if="item.type === 'announcement' && item.isPinned" 
            text="置顶" 
            type="error" 
            size="mini"
          />
        </view>

        <!-- 封面图片 -->
        <image 
          v-if="item.coverImage" 
          class="cover-image" 
          :src="getImageUrl(item.coverImage)" 
          mode="aspectFill"
          @error="item.coverImage = ''"
        />

        <text class="card-summary">{{ item.summary }}</text>

        <view class="card-foot">
          <text class="time-text">{{ formatTime(item.publishTime || item.createTime) }}</text>
          <view class="card-footer-right">
            <text class="merchant-text" v-if="item.merchantName">商户：{{ item.merchantName }}</text>
            <text class="merchant-text" v-if="item.expireTime">截止：{{ formatTime(item.expireTime) }}</text>
            <text class="view-link">查看详情</text>
          </view>
        </view>
      </uni-card>
    </view>

    <view v-else class="empty-box">
      <text class="empty-text">{{ loading ? '加载中...' : (tabIndex === 0 ? '暂无动态' : '暂无公告') }}</text>
      <text class="empty-tip" v-if="!loading">{{ keyword ? '换个关键词试试' : '暂时没有新内容' }}</text>
    </view>

    <view v-if="hasMore && list.length" class="load-more">
      <text class="load-more-text" @tap="loadMore">
        {{ loading ? '加载中...' : '加载更多' }}
      </text>
    </view>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      keyword: '',
      tabLabels: ['商户动态', '系统公告'],
      tabIndex: 0,
      list: [],
      current: 1,
      pageSize: 10,
      hasMore: true,
      loading: false,
      statistics: {
        dynamics: {
          total: 0,
          published: 0,
          merchants: 0
        },
        announcements: {
          total: 0,
          published: 0,
          pinned: 0
        }
      }
    };
  },
  onLoad() {
    Promise.all([this.fetchList(true), this.loadStatistics()]);
  },
  onPullDownRefresh() {
    Promise.all([this.fetchList(true), this.loadStatistics()]).finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    formatTime(time) {
      const date = new Date(time);
      const now = new Date();
      const diff = now - date;
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      
      if (days === 0) {
        const hours = Math.floor(diff / (1000 * 60 * 60));
        if (hours === 0) {
          const minutes = Math.floor(diff / (1000 * 60));
          return minutes === 0 ? '刚刚' : `${minutes}分钟前`;
        }
        return `${hours}小时前`;
      }
      if (days === 1) return '昨天';
      if (days < 7) return `${days}天前`;
      return formatDateTime(time);
    },
    buildSummary(content) {
      if (!content) return '暂无摘要';
      return String(content)
        .replace(/<[^>]*>/g, '')
        .replace(/\s+/g, ' ')
        .trim()
        .slice(0, 100);
    },
    getImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) return url;
      return '/api/file/preview/' + url;
    },
    async loadStatistics() {
      try {
        const [dynamicPage, announcementPage] = await Promise.all([
          canteenApi.listDynamics({ current: 1, size: 1000, onlyPublished: true }),
          canteenApi.listAnnouncements({ current: 1, size: 1000, onlyPublished: true })
        ]);
        
        const dynamics = dynamicPage.records || [];
        const announcements = announcementPage.records || [];
        
        this.statistics.dynamics.total = dynamics.length;
        this.statistics.dynamics.published = dynamics.filter(d => d.status === 'published').length;
        this.statistics.dynamics.merchants = new Set(dynamics.map(d => d.merchantName).filter(Boolean)).size;
        
        this.statistics.announcements.total = announcements.length;
        this.statistics.announcements.published = announcements.filter(a => a.status === 'published').length;
        this.statistics.announcements.pinned = announcements.filter(a => a.isTop === 1 || a.isPinned).length;
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },
    async fetchList(reset = false) {
      if (this.loading) return;
      this.loading = true;
      
      if (reset) {
        this.current = 1;
        this.hasMore = true;
      }
      
      try {
        const params = {
          current: this.current,
          size: this.pageSize,
          onlyPublished: true,
          keyword: this.keyword || undefined
        };

        let page;
        if (this.tabIndex === 0) {
          page = await canteenApi.listDynamics(params);
          const records = (page.records || []).map((item) => ({
            ...item,
            type: 'dynamic',
            summary: this.buildSummary(item.content)
          }));
          if (reset) {
            this.list = records;
          } else {
            this.list = [...this.list, ...records];
          }
          this.hasMore = records.length === this.pageSize;
        } else {
          page = await canteenApi.listAnnouncements(params);
          const records = (page.records || []).map((item) => ({
            ...item,
            type: 'announcement',
            summary: this.buildSummary(item.content)
          }));
          if (reset) {
            this.list = records;
          } else {
            this.list = [...this.list, ...records];
          }
          this.hasMore = records.length === this.pageSize;
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    onTabChange(e) {
      this.tabIndex = e.currentIndex;
      this.fetchList(true);
    },
    loadMore() {
      if (!this.hasMore || this.loading) return;
      this.current += 1;
      this.fetchList(false);
    },
    goDetail(item) {
      uni.navigateTo({ url: `/pages/content/detail?id=${item.id}&type=${item.type}` });
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.page-content {
  min-height: 100vh;
  background: #f5f5f5;
  padding: calc(20rpx + env(safe-area-inset-top)) 20rpx calc(40rpx + env(safe-area-inset-bottom));
}

.header-card {
  background: #ffffff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2f4f;
  display: block;
  line-height: 1.4;
  margin-bottom: 8rpx;
}

.sub {
  color: #6e7891;
  font-size: 22rpx;
  display: block;
  line-height: 1.5;
}

.stat-grid {
  display: flex;
  justify-content: space-around;
  padding: 8rpx 0;
}

.stat-item {
  text-align: center;
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

.search-bar {
  margin-bottom: 0;
}

.list-wrap {
  margin: 0 20rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.article-card {
  margin: 0;
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.card-head {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-right: 12rpx;
}

.card-title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #1c2b4a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cover-image {
  width: 100%;
  height: 300rpx;
  border-radius: 12rpx;
  margin: 12rpx 0;
  background: #f5f7fa;
}

.card-summary {
  display: block;
  margin: 12rpx 0;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-foot {
  margin-top: 12rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12rpx;
  border-top: 1rpx solid #f0f0f0;
}

.time-text {
  font-size: 22rpx;
  color: #999;
}

.card-footer-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.merchant-text {
  font-size: 22rpx;
  color: #999;
}

.view-link {
  font-size: 24rpx;
  color: #2f65f9;
  font-weight: 500;
}

.empty-box {
  margin: 80rpx 20rpx;
  text-align: center;
  padding: 80rpx 32rpx;
}

.empty-text {
  color: #9aa4b8;
  font-size: 28rpx;
  display: block;
  margin-bottom: 12rpx;
}

.empty-tip {
  color: #bbb;
  font-size: 24rpx;
}

.load-more {
  margin: 20rpx;
  text-align: center;
}

.load-more-text {
  color: #2f65f9;
  font-size: 26rpx;
  padding: 16rpx 32rpx;
  background: rgba(47, 101, 249, 0.1);
  border-radius: 50rpx;
}
</style>
