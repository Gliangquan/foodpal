<template>
  <view class="page-content">
    <view class="header-card">
      <view class="header-left">
        <text class="title">菜品管理</text>
        <text class="sub">管理您的菜品信息、价格和供应状态</text>
      </view>
      <button class="add-btn" type="primary" size="mini" @tap="goAddDish">
        <text>➕ 新增菜品</text>
      </button>
    </view>

    <!-- 统计卡片 -->
    <uni-card :border="false" padding="16" style="margin-bottom: 16rpx;">
      <view class="stat-grid">
        <view class="stat-item">
          <text class="stat-num">{{ statistics.total }}</text>
          <text class="stat-label">总菜品</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.available }}</text>
          <text class="stat-label">可供应</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.soldOut }}</text>
          <text class="stat-label">已售罄</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.special }}</text>
          <text class="stat-label">特价中</text>
        </view>
      </view>
    </uni-card>

    <!-- 搜索和筛选 -->
    <uni-card :border="false" padding="16" style="margin-bottom: 16rpx;">
      <view class="search-row">
        <uni-search-bar
          v-model="keyword"
          placeholder="搜索菜品名称"
          cancel-button="none"
          @confirm="loadDishes(true)"
          @clear="loadDishes(true)"
          class="search-bar"
        />
        <button class="search-btn" size="mini" type="primary" @tap="loadDishes(true)">搜索</button>
      </view>
      <uni-segmented-control
        :current="statusIndex"
        :values="['全部', '可供应', '已售罄']"
        style-type="button"
        active-color="#2f65f9"
        @clickItem="onStatusChange"
        style="margin-top: 12rpx;"
      />
    </uni-card>

    <!-- 批量操作 -->
    <view class="batch-bar" v-if="selectedItems.length > 0">
      <text class="selected-count">已选择 {{ selectedItems.length }} 项</text>
      <button size="mini" type="warn" @tap="batchDelete">批量删除</button>
    </view>

    <!-- 菜品列表 -->
    <uni-list :border="false" v-if="dishList.length">
      <uni-list-item
        v-for="item in dishList"
        :key="item.id"
        clickable
      >
        <template v-slot:body>
          <view class="dish-list-item">
            <view class="select-box" @tap.stop="toggleSelect(item)">
              <uni-icons 
                v-if="isItemSelected(item)" 
                type="checkbox-filled" 
                size="20" 
                color="#2f65f9" 
              />
              <uni-icons 
                v-else 
                type="circle" 
                size="20" 
                color="#ddd" 
              />
            </view>
            <image
              class="dish-image"
              :src="getImageUrl(item.dishImage)"
              mode="aspectFill"
            />
            <view class="dish-item-info">
              <view class="dish-item-header">
                <text class="dish-name">{{ item.dishName }}</text>
                <text class="dish-price">¥{{ getCurrentPrice(item) }}</text>
              </view>
              <view class="dish-item-tags">
                <text class="dish-tag">{{ item.category || '未分类' }}</text>
                <text class="dish-tag" :class="{ 'tag-success': item.supplyStatus === 1 }">
                  {{ item.supplyStatus === 1 ? '可供应' : '已售罄' }}
                </text>
                <text class="dish-tag tag-danger" v-if="isSpecialNow(item)">特价</text>
              </view>
              <view class="dish-item-stats">
                <text class="stat-text">{{ item.likeCount || 0 }}点赞</text>
                <text class="stat-text">{{ item.favoriteCount || 0 }}收藏</text>
              </view>
              <view class="dish-item-actions">
                <button size="mini" @tap.stop="editDish(item)">编辑</button>
                <button 
                  size="mini" 
                  :type="item.supplyStatus === 1 ? 'default' : 'primary'"
                  @tap.stop="toggleSupply(item)"
                >
                  {{ item.supplyStatus === 1 ? '售罄' : '供应' }}
                </button>
                <button size="mini" type="warn" @tap.stop="deleteDish(item)">删除</button>
              </view>
            </view>
          </view>
        </template>
      </uni-list-item>
    </uni-list>

    <view v-else class="empty-box">
      <text class="empty-text">{{ loading ? '加载中...' : '暂无菜品' }}</text>
      <text class="empty-tip" v-if="!loading">点击上方「新增菜品」添加您的第一个菜品</text>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore && dishList.length">
      <text class="load-more-text" @tap="loadMore">
        {{ loading ? '加载中...' : '加载更多' }}
      </text>
    </view>

    <!-- 特价设置弹窗 -->
    <uni-popup ref="specialPopup" type="center">
      <view class="special-popup">
        <text class="popup-title">设置特价活动</text>
        <text class="popup-sub">为菜品设置特价，吸引更多学生</text>
        
        <view class="form-item">
          <text class="form-label">菜品名称</text>
          <text class="form-value">{{ specialForm.dishName }}</text>
        </view>
        
        <view class="form-item">
          <text class="form-label">原价</text>
          <text class="form-value">¥{{ specialForm.dishPrice }}</text>
        </view>
        
        <view class="form-item">
          <text class="form-label">特价价格</text>
          <uni-easyinput 
            v-model="specialForm.specialPrice" 
            type="digit" 
            placeholder="请输入特价价格"
            :clearable="false"
          />
        </view>
        
        <view class="discount-hint" v-if="specialForm.specialPrice && specialForm.dishPrice">
          <text>折扣：{{ ((Number(specialForm.specialPrice) / specialForm.dishPrice) * 10).toFixed(1) }}折</text>
        </view>
        
        <view class="form-item">
          <text class="form-label">开始时间</text>
          <uni-datetime-picker 
            v-model="specialForm.specialStartTime" 
            type="datetime" 
            placeholder="选择开始时间"
          />
        </view>
        
        <view class="form-item">
          <text class="form-label">结束时间</text>
          <uni-datetime-picker 
            v-model="specialForm.specialEndTime" 
            type="datetime" 
            placeholder="选择结束时间"
          />
        </view>
        
        <view class="form-item">
          <text class="form-label">限购数量（可选）</text>
          <uni-easyinput 
            v-model="specialForm.purchaseLimit" 
            type="number" 
            placeholder="每人限购数量，不填则不限购"
            :clearable="true"
          />
        </view>
        
        <view class="popup-actions">
          <button class="popup-btn cancel" size="mini" @tap="closeSpecialModal">取消</button>
          <button class="popup-btn confirm" size="mini" type="primary" @tap="submitSpecial">确认设置</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';

export default {
  data() {
    return {
      defaultImg: '/static/logo.png',
      keyword: '',
      statusIndex: 0,
      dishList: [],
      selectedItems: [],
      current: 1,
      pageSize: 10,
      hasMore: true,
      loading: false,
      specialForm: {
        dishId: 0,
        dishName: '',
        dishPrice: 0,
        specialPrice: '',
        specialStartTime: '',
        specialEndTime: '',
        purchaseLimit: ''
      },
      statistics: {
        total: 0,
        available: 0,
        soldOut: 0,
        special: 0
      }
    };
  },
  onShow() {
    this.loadDishes(true);
    this.loadStatistics();
  },
  onPullDownRefresh() {
    Promise.all([this.loadDishes(true), this.loadStatistics()]).finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    async loadDishes(reset = false) {
      if (this.loading) return;
      this.loading = true;
      
      if (reset) {
        this.current = 1;
        this.hasMore = true;
        this.selectedItems = [];
      }
      
      try {
        const supplyStatus = this.statusIndex === 0 ? undefined : (this.statusIndex === 1 ? 1 : 0);
        const page = await canteenApi.listMyDishes({
          current: this.current,
          size: this.pageSize,
          supplyStatus,
          keyword: this.keyword.trim() || undefined
        });

        const records = page.records || [];
        if (reset) {
          this.dishList = records;
        } else {
          this.dishList = [...this.dishList, ...records];
        }
        this.hasMore = records.length === this.pageSize;
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    async loadStatistics() {
      try {
        const page = await canteenApi.listMyDishes({ current: 1, size: 1000 });
        const dishes = page.records || [];
        this.statistics.total = dishes.length;
        this.statistics.available = dishes.filter(d => d.supplyStatus === 1).length;
        this.statistics.soldOut = dishes.filter(d => d.supplyStatus === 0).length;
        this.statistics.special = dishes.filter(d => this.isSpecialNow(d)).length;
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },
    onStatusChange(e) {
      this.statusIndex = e.currentIndex;
      this.loadDishes(true);
    },
    loadMore() {
      if (!this.hasMore || this.loading) return;
      this.current += 1;
      this.loadDishes(false);
    },
    goAddDish() {
      uni.navigateTo({ url: '/pages/merchant/dish-edit' });
    },
    editDish(item) {
      uni.navigateTo({ url: `/pages/merchant/dish-edit?id=${item.id}` });
    },
    async toggleSupply(item) {
      const newStatus = item.supplyStatus === 1 ? 0 : 1;
      try {
        await canteenApi.updateDish({
          id: item.id,
          supplyStatus: newStatus
        });
        uni.showToast({ 
          title: newStatus === 1 ? '已恢复供应' : '已设为售罄', 
          icon: 'success' 
        });
        await Promise.all([this.loadDishes(true), this.loadStatistics()]);
      } catch (error) {
        uni.showToast({ title: error.message || '操作失败', icon: 'none' });
      }
    },
    deleteDish(item) {
      uni.showModal({
        title: '确认删除',
        content: `确定要删除菜品「${item.dishName}」吗？此操作不可恢复。`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await canteenApi.deleteDish(item.id);
              uni.showToast({ title: '删除成功', icon: 'success' });
              await Promise.all([this.loadDishes(true), this.loadStatistics()]);
            } catch (error) {
              uni.showToast({ title: error.message || '删除失败', icon: 'none' });
            }
          }
        }
      });
    },
    toggleSelect(item) {
      const index = this.selectedItems.findIndex(i => i.id === item.id);
      if (index > -1) {
        this.selectedItems.splice(index, 1);
      } else {
        this.selectedItems.push(item);
      }
    },
    isItemSelected(item) {
      return this.selectedItems.some(i => i.id === item.id);
    },
    batchDelete() {
      uni.showModal({
        title: '批量删除',
        content: `确定要删除选中的 ${this.selectedItems.length} 个菜品吗？此操作不可恢复。`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await Promise.all(this.selectedItems.map(item => canteenApi.deleteDish(item.id)));
              uni.showToast({ title: '批量删除成功', icon: 'success' });
              this.selectedItems = [];
              await Promise.all([this.loadDishes(true), this.loadStatistics()]);
            } catch (error) {
              uni.showToast({ title: error.message || '删除失败', icon: 'none' });
            }
          }
        }
      });
    },
    isSpecialNow(item) {
      if (!item.specialStartTime || !item.specialEndTime) return false;
      const now = new Date();
      const start = new Date(item.specialStartTime);
      const end = new Date(item.specialEndTime);
      return start <= now && end > now;
    },
    getCurrentPrice(item) {
      return this.isSpecialNow(item) ? (item.specialPrice || item.dishPrice) : item.dishPrice;
    },
    getImageUrl(imageUrl) {
      if (!imageUrl || imageUrl.trim() === '') {
        return '';
      }
      if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
        return imageUrl;
      }
      if (imageUrl.startsWith('/')) {
        return imageUrl;
      }
      return '/api/file/preview/' + imageUrl;
    },
    openSpecialModal(item) {
      this.specialForm.dishId = item.id;
      this.specialForm.dishName = item.dishName;
      this.specialForm.dishPrice = item.dishPrice;
      this.specialForm.specialPrice = '';
      this.specialForm.specialStartTime = '';
      this.specialForm.specialEndTime = '';
      this.specialForm.purchaseLimit = '';
      this.$refs.specialPopup.open();
    },
    closeSpecialModal() {
      this.$refs.specialPopup.close();
    },
    async submitSpecial() {
      if (!this.specialForm.specialPrice || Number(this.specialForm.specialPrice) <= 0) {
        return uni.showToast({ title: '请输入有效的特价价格', icon: 'none' });
      }
      if (Number(this.specialForm.specialPrice) >= this.specialForm.dishPrice) {
        return uni.showToast({ title: '特价价格必须低于原价', icon: 'none' });
      }
      if (!this.specialForm.specialStartTime || !this.specialForm.specialEndTime) {
        return uni.showToast({ title: '请选择特价开始和结束时间', icon: 'none' });
      }
      if (this.specialForm.specialStartTime >= this.specialForm.specialEndTime) {
        return uni.showToast({ title: '开始时间必须早于结束时间', icon: 'none' });
      }

      try {
        uni.showLoading({ title: '设置中...' });
        await canteenApi.setSpecialDish({
          dishId: this.specialForm.dishId,
          specialPrice: Number(this.specialForm.specialPrice),
          specialStartTime: this.specialForm.specialStartTime,
          specialEndTime: this.specialForm.specialEndTime,
          purchaseLimit: this.specialForm.purchaseLimit ? Number(this.specialForm.purchaseLimit) : undefined
        });
        uni.showToast({ title: '特价设置成功', icon: 'success' });
        this.closeSpecialModal();
        await Promise.all([this.loadDishes(true), this.loadStatistics()]);
      } catch (error) {
        uni.showToast({ title: error.message || '设置失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    async confirmCancelSpecial(item) {
      uni.showModal({
        title: '取消特价',
        content: `确定要取消菜品「${item.dishName}」的特价吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await canteenApi.cancelSpecialDish(item.id);
              uni.showToast({ title: '特价已取消', icon: 'success' });
              await Promise.all([this.loadDishes(true), this.loadStatistics()]);
            } catch (error) {
              uni.showToast({ title: error.message || '取消失败', icon: 'none' });
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.header-left {
  flex: 1;
}

.title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1f2f4f;
  display: block;
  margin-bottom: 8rpx;
}

.sub {
  color: #6e7891;
  font-size: 24rpx;
  display: block;
}

.add-btn {
  background: #2f65f9;
  color: #ffffff;
  border: none;
  border-radius: 50rpx;
  padding: 16rpx 32rpx;
  font-size: 26rpx;
  box-shadow: 0 4rpx 12rpx rgba(47, 101, 249, 0.2);
  min-height: 44rpx;
  min-width: 44rpx;
  touch-action: manipulation;
  transition: all 0.2s ease;

  &:active {
    opacity: 0.8;
    transform: scale(0.98);
  }
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

.search-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.search-bar {
  flex: 1;
  margin-bottom: 0;
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

.batch-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16rpx 24rpx;
  margin: 0 20rpx 16rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.selected-count {
  font-size: 26rpx;
  color: #667eea;
  font-weight: 600;
}

.dish-card {
  margin: 0 20rpx 16rpx;
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.dish-card.selected {
  background: #f0f4ff;
  border: 2rpx solid #667eea;
}

.dish-list-item {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 0;
  min-height: 88rpx;
}

.select-box {
  width: 44rpx;
  min-height: 44rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8rpx;
  touch-action: manipulation;
}

.dish-image {
  width: 160rpx;
  height: 160rpx;
  flex-shrink: 0;
  border-radius: 8rpx;
  background: #f0f0f0;
}

.dish-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-item-tags {
  display: flex;
  gap: 8rpx;
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

.dish-item-stats {
  display: flex;
  gap: 16rpx;
}

.stat-text {
  font-size: 22rpx;
  color: #9aa4b8;
}

.dish-item-actions {
  display: flex;
  gap: 8rpx;
}

/* 旧的卡片样式（保留用于参考） */

.dish-thumb {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  background-color: #f5f7fa;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  min-width: 0;
}

.dish-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8rpx;
}

.dish-name {
  font-size: 30rpx;
  color: #1c2b4a;
  font-weight: 700;
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

.dish-price-row {
  display: flex;
  align-items: baseline;
  margin-bottom: 8rpx;
}

.dish-price {
  color: #f5576c;
  font-weight: 700;
  font-size: 32rpx;
}

.dish-origin {
  color: #999;
  font-size: 22rpx;
  text-decoration: line-through;
  margin-left: 12rpx;
}

.dish-category {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 6rpx;
}

.dish-desc {
  display: block;
  font-size: 22rpx;
  color: #999;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-stats {
  display: flex;
  gap: 20rpx;
  margin-bottom: 12rpx;
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
  border-radius: 50rpx;
  font-size: 24rpx;
  display: flex;
  align-items: center;
}

.op-btn.warn {
  background: #fff1f0;
  color: #f5576c;
  border: 1rpx solid #ffa39e;
}

.empty-box {
  text-align: center;
  padding: 100rpx 40rpx;
}

.empty-icon {
  font-size: 120rpx;
  display: block;
  margin-bottom: 20rpx;
  opacity: 0.5;
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
  text-align: center;
  padding: 20rpx;
}

.load-more-text {
  color: #2f65f9;
  font-size: 26rpx;
  padding: 16rpx 32rpx;
  background: rgba(47, 101, 249, 0.1);
  border-radius: 50rpx;
}
</style>