<template>
  <view class="page-content">
    <view class="header-card">
      <view class="header-left">
        <text class="title">投诉管理</text>
        <text class="sub">查看所有投诉记录，处理学生投诉</text>
      </view>
    </view>

    <!-- 统计卡片 -->
    <uni-card :border="false" padding="16" style="margin-bottom: 16rpx;">
      <view class="stat-grid">
        <view class="stat-item">
          <text class="stat-num">{{ statistics.total }}</text>
          <text class="stat-label">总投诉</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.pending }}</text>
          <text class="stat-label">待处理</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.rectify }}</text>
          <text class="stat-label">待整改</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ statistics.completed }}</text>
          <text class="stat-label">已完成</text>
        </view>
      </view>
    </uni-card>

    <!-- 搜索和筛选 -->
    <uni-card :border="false" padding="16" style="margin-bottom: 16rpx;">
      <uni-search-bar
        v-model="keyword"
        placeholder="搜索投诉编号、标题、商户"
        cancel-button="none"
        @confirm="loadComplaints(true)"
        @clear="loadComplaints(true)"
        class="search-bar"
      />
      <view class="filter-row">
        <uni-data-select 
          v-model="filterStatus" 
          :localdata="statusOptions" 
          placeholder="状态筛选" 
          @change="loadComplaints(true)"
          class="filter-select"
        />
        <uni-data-select 
          v-model="filterMerchant" 
          :localdata="merchantOptions" 
          placeholder="商户筛选"
          @change="loadComplaints(true)"
          class="filter-select"
        />
      </view>
    </uni-card>

    <!-- 投诉列表 -->
    <view v-if="complaintList.length">
      <uni-card 
        v-for="item in complaintList" 
        :key="item.id" 
        :border="false" 
        padding="18" 
        class="complaint-card"
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
            <text class="meta-icon">学生</text>
            <text class="meta-text">{{ item.studentName || '-' }}</text>
          </view>
        </view>

        <view class="content-box">
          <text class="content">{{ item.complaintContent }}</text>
        </view>

        <view class="progress-row" v-if="item.rectifyRequirement">
          <text class="progress-label">整改要求：</text>
          <text class="progress-text">{{ item.rectifyRequirement }}</text>
        </view>

        <view class="progress-row" v-if="item.feedback">
          <text class="progress-label">处理反馈：</text>
          <text class="progress-text">{{ item.feedback }}</text>
        </view>

        <view class="meta-row">
          <text class="time">📅 {{ formatTime(item.createTime) }}</text>
          <text class="status-text">
            {{ getProgressText(item) }}
          </text>
        </view>

        <!-- 证据图片 -->
        <view class="evidence-row" v-if="item.evidenceUrls">
          <text class="evidence-label">证据：</text>
          <scroll-view scroll-x class="evidence-scroll">
            <image
                          v-for="(url, index) in parseEvidenceUrls(item.evidenceUrls)" 
                          :key="index"
                          class="evidence-img"
                          :src="getImageUrl(url)"
                          mode="aspectFill"
                          @tap="previewEvidence(item.evidenceUrls, index)"
                        />          </scroll-view>
        </view>

        <view class="op-row">
          <button 
            class="op-btn primary" 
            size="mini" 
            type="primary" 
            v-if="item.status === 'pending_review'"
            @tap="openProcess(item)"
          >
            <text>处理投诉</text>
          </button>
          <button 
            class="op-btn primary" 
            size="mini" 
            type="primary" 
            v-if="item.status === 'rectified'"
            @tap="openProcess(item)"
          >
            <text>审核整改</text>
          </button>
          <button class="op-btn" size="mini" @tap="viewDetail(item)">
            <text>查看详情</text>
          </button>
        </view>
      </uni-card>
    </view>

    <view v-else class="empty-box">
<text class="empty-text">{{ loading ? '加载中...' : '暂无投诉记录' }}</text>
      <text class="empty-tip" v-if="!loading">暂无投诉数据</text>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="hasMore && complaintList.length">
      <text class="load-more-text" @tap="loadMore">
        {{ loading ? '加载中...' : '加载更多' }}
      </text>
    </view>

    <!-- 处理弹窗 -->
    <uni-popup ref="processPopup" type="center">
      <view class="process-popup">
        <view class="popup-header">
          <text class="popup-title">处理投诉</text>
          <text class="popup-sub">投诉编号：{{ currentComplaint?.complaintNo }}</text>
        </view>
        <uni-forms :model="processForm" label-position="top">
          <uni-forms-item label="处理状态" required>
            <uni-data-select 
              v-model="processForm.status" 
              :localdata="getProcessOptions()" 
              placeholder="请选择处理状态"
            />
          </uni-forms-item>
          <uni-forms-item label="整改要求" v-if="processForm.status === 'pending_rectify'">
            <uni-easyinput 
              v-model="processForm.rectifyRequirement" 
              type="textarea" 
              placeholder="请输入整改要求"
              :maxlength="500"
            />
          </uni-forms-item>
          <uni-forms-item label="处理反馈" required>
            <uni-easyinput 
              v-model="processForm.feedback" 
              type="textarea" 
              placeholder="请输入处理反馈（将通知学生）"
              :maxlength="500"
            />
          </uni-forms-item>
        </uni-forms>
        <view class="popup-actions">
          <button class="popup-btn cancel" size="mini" @tap="closeProcess">取消</button>
          <button class="popup-btn confirm" size="mini" type="primary" @tap="submitProcess">确认处理</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';

export default {
  data() {
    return {
      keyword: '',
      filterStatus: '',
      filterMerchant: '',
      presetMerchantName: '',
      statusOptions: [
        { value: '', text: '全部状态' },
        { value: 'pending_review', text: '待审核' },
        { value: 'pending_rectify', text: '待整改' },
        { value: 'rectified', text: '已整改' },
        { value: 'completed', text: '已完成' },
        { value: 'rejected', text: '已驳回' }
      ],
      merchantOptions: [],
      processOptions: [
        { value: 'pending_rectify', text: '下达整改' },
        { value: 'completed', text: '处理完成' },
        { value: 'rejected', text: '驳回投诉' }
      ],
      complaintList: [],
      current: 1,
      pageSize: 10,
      hasMore: true,
      loading: false,
      currentComplaint: null,
      processForm: {
        status: 'pending_rectify',
        rectifyRequirement: '',
        feedback: ''
      },
      statistics: {
        total: 0,
        pending: 0,
        rectify: 0,
        completed: 0
      }
    };
  },
  onLoad(options = {}) {
    const merchantId = Number(options.merchantId);
    if (Number.isFinite(merchantId) && merchantId > 0) {
      this.filterMerchant = merchantId;
    }
    if (options.merchantName) {
      try {
        this.presetMerchantName = decodeURIComponent(options.merchantName);
      } catch (e) {
        this.presetMerchantName = options.merchantName;
      }
      uni.setNavigationBarTitle({
        title: `${this.presetMerchantName} - 投诉管理`
      });
    }
  },
  onShow() {
    this.loadComplaints(true);
    this.loadMerchants();
    this.loadStatistics();
  },
  onPullDownRefresh() {
    Promise.all([
      this.loadComplaints(true),
      this.loadStatistics()
    ]).finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    formatTime(value) {
      const date = new Date(value);
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
      return formatDateTime(value);
    },
    getPriorityText(createTime) {
      if (!createTime) return '普通';
      const date = new Date(createTime);
      const now = new Date();
      const diffHours = (now - date) / (1000 * 60 * 60);
      if (diffHours < 24) return '紧急';
      if (diffHours < 72) return '重要';
      return '普通';
    },
    getPriorityType(createTime) {
      if (!createTime) return 'default';
      const date = new Date(createTime);
      const now = new Date();
      const diffHours = (now - date) / (1000 * 60 * 60);
      if (diffHours < 24) return 'error';
      if (diffHours < 72) return 'warning';
      return 'default';
    },
    statusText(status) {
      const map = {
        pending_review: '待审核',
        pending_rectify: '待整改',
        rectified: '已整改',
        completed: '已完成',
        rejected: '已驳回'
      };
      return map[status] || status || '未知';
    },
    statusTag(status) {
      if (status === 'pending_review') return 'warning';
      if (status === 'pending_rectify') return 'error';
      if (status === 'rectified') return 'primary';
      if (status === 'completed') return 'success';
      if (status === 'rejected') return 'default';
      return 'default';
    },
    getProgressText(item) {
      if (!item) return '待处理';
      if (item.status === 'pending_review') return '等待监督管理员审核';
      if (item.status === 'pending_rectify') return item.rectifyRequirement || '已通知商户整改';
      if (item.status === 'rectified') return item.rectifyResult || '商户已提交整改结果，等待复核';
      if (item.status === 'completed') return item.feedback || item.rectifyResult || '投诉已处理完成';
      if (item.status === 'rejected') return item.feedback || '投诉已驳回';
      return item.processProgress || '待处理';
    },
    getProcessOptions() {
      if (!this.currentComplaint) return this.processOptions;
      if (this.currentComplaint.status === 'rectified') {
        return [
          { value: 'completed', text: '通过整改' },
          { value: 'pending_rectify', text: '重新整改' }
        ];
      }
      return this.processOptions;
    },
    async loadMerchants() {
      try {
        const page = await canteenApi.listMerchants({ current: 1, size: 200, auditStatus: 'approved' });
        const records = page.records || [];
        const merchantOptions = [
          { value: '', text: '全部商户' },
          ...records.map(item => ({
            value: item.id,
            text: item.merchantName
          }))
        ];
        if (this.filterMerchant) {
          const existed = merchantOptions.some(item => Number(item.value) === Number(this.filterMerchant));
          if (!existed) {
            merchantOptions.push({
              value: this.filterMerchant,
              text: this.presetMerchantName || `商户#${this.filterMerchant}`
            });
          }
        }
        this.merchantOptions = merchantOptions;
      } catch (error) {
        console.error('加载商户失败', error);
      }
    },
    async loadStatistics() {
      try {
        const page = await canteenApi.listAllComplaints({ current: 1, size: 1000 });
        const complaints = page.records || [];
        this.statistics.total = complaints.length;
        this.statistics.pending = complaints.filter(c => c.status === 'pending_review').length;
        this.statistics.rectify = complaints.filter(c => c.status === 'pending_rectify').length;
        this.statistics.completed = complaints.filter(c => c.status === 'completed').length;
      } catch (error) {
        console.error('加载统计失败', error);
      }
    },
    async loadComplaints(reset = false) {
      if (this.loading) return;
      this.loading = true;
      
      if (reset) {
        this.current = 1;
        this.hasMore = true;
      }
      
      try {
        const page = await canteenApi.listAllComplaints({
          current: this.current,
          size: this.pageSize,
          status: this.filterStatus || undefined,
          keyword: this.keyword || undefined,
          merchantId: this.filterMerchant || undefined
        });

        const records = page.records || [];
        if (reset) {
          this.complaintList = records;
        } else {
          this.complaintList = [...this.complaintList, ...records];
        }
        this.hasMore = records.length === this.pageSize;
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    loadMore() {
      if (!this.hasMore || this.loading) return;
      this.current += 1;
      this.loadComplaints(false);
    },
    openProcess(item) {
      this.currentComplaint = item;
      this.processForm.status = item.status === 'rectified' ? 'completed' : 'pending_rectify';
      this.processForm.rectifyRequirement = item.rectifyRequirement || '';
      this.processForm.feedback = item.feedback || '';
      this.$refs.processPopup.open();
    },
    closeProcess() {
      this.$refs.processPopup.close();
      this.currentComplaint = null;
    },
    async submitProcess() {
      if (!this.currentComplaint) return;
      
      if (!this.processForm.feedback) {
        return uni.showToast({ title: '请输入处理反馈', icon: 'none' });
      }
      
      if (this.processForm.status === 'pending_rectify' && !this.processForm.rectifyRequirement) {
        return uni.showToast({ title: '请输入整改要求', icon: 'none' });
      }
      
      try {
        uni.showLoading({ title: '处理中...' });
        await canteenApi.processComplaint({
          complaintId: this.currentComplaint.id,
          status: this.processForm.status,
          rectifyRequirement: this.processForm.rectifyRequirement,
          feedback: this.processForm.feedback
        });
        uni.showToast({ title: '处理成功', icon: 'success' });
        this.closeProcess();
        await Promise.all([
          this.loadComplaints(true),
          this.loadStatistics()
        ]);
      } catch (error) {
        uni.showToast({ title: error.message || '处理失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    viewDetail(item) {
      const content = [
        `编号：${item.complaintNo || item.id}`,
        `商户：${item.merchantName || '-'}`,
        `学生：${item.studentName || '-'}`,
        `状态：${this.statusText(item.status)}`,
        `标题：${item.complaintTitle || '-'}`,
        `内容：${item.complaintContent || '-'}`
      ].join('\n');
      uni.showModal({
        title: '投诉详情',
        content,
        showCancel: false
      });
    },
    parseEvidenceUrls(urls) {
      if (!urls) return [];
      const source = String(urls).trim();
      if (!source) return [];
      const normalizeList = (list) => list
        .map(u => this.getImageUrl(String(u || '').trim()))
        .filter(u => u);

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
    getImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) return url;
      if (url.startsWith('/api/')) return url;
      if (url.startsWith('/')) return url;
      return '/api/file/preview/' + url;
    },
    previewEvidence(urls, index) {
      const imageUrls = this.parseEvidenceUrls(urls);
      uni.previewImage({
        urls: imageUrls,
        current: imageUrls[index] || imageUrls[0]
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
  margin-bottom: 12rpx;
}

.filter-row {
  display: flex;
  gap: 12rpx;
}

.filter-select {
  flex: 1;
}

.complaint-card {
  margin: 0 20rpx 16rpx;
  background: #ffffff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.no-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.no {
  font-size: 24rpx;
  color: #666;
  font-weight: 500;
}

.complaint-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1c2b4a;
  margin-bottom: 12rpx;
  display: block;
}

.meta-row {
  display: flex;
  gap: 24rpx;
  margin-bottom: 12rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.meta-icon {
  font-size: 24rpx;
}

.meta-text {
  font-size: 24rpx;
  color: #666;
}

.content-box {
  background: #f8f9fa;
  padding: 16rpx;
  border-radius: 12rpx;
  margin-bottom: 12rpx;
}

.content {
  font-size: 26rpx;
  color: #333;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.progress-row {
  display: flex;
  margin-bottom: 8rpx;
}

.progress-label {
  font-size: 24rpx;
  color: #6e7891;
  flex-shrink: 0;
  margin-right: 8rpx;
}

.progress-text {
  font-size: 24rpx;
  color: #333;
  flex: 1;
  word-break: break-all;
}

.time {
  font-size: 22rpx;
  color: #999;
}

.status-text {
  font-size: 22rpx;
  color: #2f65f9;
}

.evidence-row {
  margin-bottom: 16rpx;
}

.evidence-label {
  font-size: 22rpx;
  color: #666;
  margin-bottom: 8rpx;
  display: block;
}

.evidence-scroll {
  white-space: nowrap;
}

.evidence-img {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  margin-right: 12rpx;
  display: inline-block;
  background: #f5f7fa;
}

.op-row {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
}

.op-btn {
  padding: 8rpx 16rpx;
  border-radius: 50rpx;
  font-size: 24rpx;
}

.op-btn.primary {
  background: #f5f5f5;
  border-color: #f5f5f5;
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
  color: #f5576c;
  font-size: 26rpx;
  padding: 16rpx 32rpx;
  background: rgba(245, 87, 108, 0.1);
  border-radius: 50rpx;
}

.process-popup {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  width: 600rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.popup-header {
  text-align: center;
  margin-bottom: 24rpx;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.popup-sub {
  font-size: 24rpx;
  color: #999;
}

.popup-actions {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.popup-btn {
  flex: 1;
  padding: 16rpx;
  border-radius: 50rpx;
}

.popup-btn.cancel {
  background: #f0f0f0;
  color: #666;
}

.popup-btn.confirm {
  background: #f5f5f5;
  color: #333;
}
</style>
