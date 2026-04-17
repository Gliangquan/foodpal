<template>
  <div class="home-page">
    <a-page-header title="高校食堂通管理台" sub-title="学生、商户、投诉与资讯协同管理" />

    <a-row :gutter="16" style="margin-top: 12px">
      <a-col :xs="24" :md="8">
        <a-card class="stat-card merchant">
          <div class="stat-content">
            <div class="stat-icon"><ShopOutlined /></div>
            <div class="stat-info">
              <a-statistic title="商户数量" :value="summary.merchantCount" />
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card class="stat-card dish">
          <div class="stat-content">
            <div class="stat-icon"><CoffeeOutlined /></div>
            <div class="stat-info">
              <a-statistic title="菜品数量" :value="summary.dishCount" />
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="8">
        <a-card class="stat-card complaint">
          <div class="stat-content">
            <div class="stat-icon"><FileTextOutlined /></div>
            <div class="stat-info">
              <a-statistic title="待处理投诉" :value="summary.pendingComplaintCount" />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-top: 16px">
      <a-col :xs="24" :lg="14">
        <a-card title="智能推荐菜品 Top10">
          <template #extra>
            <a-button type="link" size="small" @click="loadDashboard">刷新</a-button>
          </template>

          <div v-if="recommendedDishes.length" class="recommend-grid">
            <div
              v-for="(item, index) in recommendedDishes"
              :key="item.id"
              class="recommend-card"
              :class="{ top3: index < 3 }"
            >
              <div class="rank-badge" :class="{ top3: index < 3 }">
                {{ index + 1 }}
              </div>
              <img
                :src="getImageUrl(item.dishImage)"
                :alt="item.dishName"
                class="dish-image"
                @error="handleImageError"
              />
              <div class="dish-info">
                <div class="dish-header">
                  <span class="dish-name" :title="item.dishName">{{ item.dishName }}</span>
                  <a-tag v-if="item.isSpecial" color="red" size="small">特价</a-tag>
                </div>
                <div class="dish-merchant" :title="item.merchantName">{{ item.merchantName }}</div>
                <div class="dish-meta">
                  <span class="dish-category">{{ item.category || '未分类' }}</span>
                  <span class="dish-price">
                    <span class="current">¥{{ item.currentPrice ?? item.dishPrice }}</span>
                    <span v-if="item.isSpecial" class="original">¥{{ item.dishPrice }}</span>
                  </span>
                </div>
                <div class="dish-stats">
                  <span class="stat-item">
                    <span class="stat-icon"><LikeOutlined /></span>
                    <span class="stat-count">{{ item.likeCount || 0 }}</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-icon"><StarOutlined /></span>
                    <span class="stat-count">{{ item.favoriteCount || 0 }}</span>
                  </span>
                  <span class="stat-item">
                    <span class="stat-icon"><LineChartOutlined /></span>
                    <span class="stat-count score">{{ item.score }}</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无推荐菜品" />
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card title="商户投诉排行榜 Top10">
          <template #extra>
            <a-button type="link" size="small" @click="loadDashboard">刷新</a-button>
          </template>

          <div v-if="complaintRanking.length" class="ranking-list">
            <div
              v-for="(item, index) in complaintRanking"
              :key="item.merchantId"
              class="ranking-item"
              :class="{ top3: index < 3 }"
            >
              <div class="rank-number" :class="{ top3: index < 3 }">
                {{ index + 1 }}
              </div>
              <div class="ranking-info">
                <div class="merchant-name" :title="item.merchantName">{{ item.merchantName }}</div>
                <div class="complaint-count">
                  <span class="count-label">投诉数</span>
                  <span class="count-value" :class="getCountClass(item.complaintCount)">
                    {{ item.complaintCount }}
                  </span>
                </div>
              </div>
              <div class="ranking-chart">
                <div
                  class="chart-bar"
                  :style="{ width: getBarWidth(item.complaintCount), background: getBarColor(index) }"
                ></div>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无投诉数据" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { message } from 'ant-design-vue';
import {
  ShopOutlined,
  CoffeeOutlined,
  FileTextOutlined,
  LikeOutlined,
  StarOutlined,
  LineChartOutlined,
} from '@ant-design/icons-vue';
import { getCanteenSummary, getComplaintRanking, recommendDishes as fetchRecommendDishes, type DishItem } from '../api';
import { normalizeImageUrl } from '../utils/image';

const summary = reactive({
  merchantCount: 0,
  dishCount: 0,
  pendingComplaintCount: 0
});

const recommendedDishes = ref<DishItem[]>([]);
const complaintRanking = ref<Array<{ merchantId: number; merchantName: string; complaintCount: number }>>([]);

const defaultImage = 'https://via.placeholder.com/200x150?text=暂无图片';

const getImageUrl = (url?: string) => {
  if (!url) return defaultImage;
  return normalizeImageUrl(url) || defaultImage;
};

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement;
  img.src = defaultImage;
};

const getCountClass = (count: number) => {
  if (count >= 10) return 'high';
  if (count >= 5) return 'medium';
  return 'low';
};

const getBarWidth = (count: number) => {
  const maxCount = Math.max(...complaintRanking.value.map(r => r.complaintCount), 1);
  return `${(count / maxCount) * 100}%`;
};

const getBarColor = (index: number) => {
  if (index === 0) return '#ff4d4f';
  if (index === 1) return '#ff7a45';
  if (index === 2) return '#ffa940';
  return '#52c41a';
};

const loadDashboard = async () => {
  try {
    const [summaryRes, dishRes, rankRes] = await Promise.all([
      getCanteenSummary(),
      fetchRecommendDishes(10),
      getComplaintRanking(10)
    ]);

    Object.assign(summary, summaryRes.data || {});
    recommendedDishes.value = dishRes.data || [];
    complaintRanking.value = rankRes.data || [];
  } catch (error: any) {
    message.error(error?.message || '加载首页数据失败');
  }
};

onMounted(loadDashboard);
</script>

<style scoped>
.home-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card.merchant,
.stat-card.dish,
.stat-card.complaint {
  background: #ffffff;
  border: 1px solid #e8e8e8;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.9;
  color: #333333;
}

.stat-info {
  flex: 1;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.recommend-card {
  position: relative;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 14px;
  overflow: hidden;
}

.rank-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 2;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #8c8c8c;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.rank-badge.top3 {
  background: #ff4d4f;
}

.dish-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  background: #f5f5f5;
}

.dish-info {
  padding: 14px;
}

.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.dish-name {
  font-weight: 600;
  color: #111;
}

.dish-merchant,
.dish-category {
  color: #666;
  font-size: 13px;
}

.dish-meta,
.dish-stats {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current {
  color: #ff4d4f;
  font-weight: 700;
}

.original {
  margin-left: 6px;
  color: #999;
  text-decoration: line-through;
}

.stat-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #666;
}

.stat-count.score {
  color: #1677ff;
  font-weight: 600;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #f0f0f0;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #d9d9d9;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.rank-number.top3 {
  background: #ff4d4f;
  color: #fff;
}

.ranking-info {
  min-width: 0;
  flex: 1;
}

.merchant-name {
  font-weight: 600;
  color: #111;
}

.complaint-count {
  margin-top: 4px;
  font-size: 13px;
  color: #666;
}

.count-value {
  margin-left: 6px;
  font-weight: 700;
}

.count-value.high {
  color: #ff4d4f;
}

.count-value.medium {
  color: #fa8c16;
}

.count-value.low {
  color: #52c41a;
}

.ranking-chart {
  width: 110px;
  height: 10px;
  background: #f5f5f5;
  border-radius: 999px;
  overflow: hidden;
}

.chart-bar {
  height: 100%;
  border-radius: 999px;
}
</style>
