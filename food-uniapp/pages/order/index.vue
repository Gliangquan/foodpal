<template>
  <view class="page-content">
    <view class="header-card evidence-tip" v-if="isMerchant">
      <text class="title">投诉处理</text>
      <text class="sub">收到整改通知后，建议补充文字说明和现场照片，方便监督员快速复核。</text>
    </view>
    <view class="header-card">
      <text class="title">投诉记录</text>
      <text class="sub">{{ roleText }}查看投诉单及处理进度</text>
      <uni-segmented-control
        :current="statusIndex"
        :values="statusLabels"
        style-type="button"
        active-color="#2f65f9"
        @clickItem="onStatusChange"
      />
    </view>

    <view v-if="list.length">
      <uni-card v-for="item in list" :key="item.id" :border="false" padding="18" class="complaint-card">
        <view class="row-between">
          <text class="no">{{ item.complaintNo || '#' + item.id }}</text>
          <uni-tag :text="statusText(item.status)" :type="statusTag(item.status)" size="small" />
        </view>
        <text class="complaint-title">{{ item.complaintTitle }}</text>
        <text class="meta">商户：{{ item.merchantName || '-' }}</text>
        <text class="meta" v-if="item.studentName">投诉人：{{ item.studentName }}</text>
        <text class="content">{{ item.complaintContent }}</text>
        <view class="evidence-row" v-if="parseEvidenceUrls(item.evidenceUrls).length">
          <image
            v-for="(url, index) in parseEvidenceUrls(item.evidenceUrls)"
            :key="`${item.id}-${index}`"
            class="evidence-image"
            :src="url"
            mode="aspectFill"
            @tap="previewEvidence(item.evidenceUrls, index)"
          />
        </view>
        <text class="progress">处理说明：{{ progressText(item) }}</text>
        <text class="time">提交时间：{{ format(item.createTime) }}</text>

        <view class="op-row">
          <button size="mini" v-if="isMerchant && item.status === 'pending_rectify'" @tap="openRectify(item)">提交整改结果</button>
          <button size="mini" v-if="isStudent && item.status === 'completed' && item.resultRating == null" type="primary" @tap="openEvaluate(item)">评价处理结果</button>
          <uni-tag v-if="item.resultRating != null" :text="'已评价: ' + ratingText(item.resultRating)" type="success" size="small" />
        </view>
      </uni-card>
    </view>

    <view v-else class="empty-box">
      <text class="empty-text">暂无投诉记录</text>
    </view>

    <view class="load-more" v-if="hasMore && list.length">
      <text class="load-more-text" @tap="loadMore">加载更多</text>
    </view>

    <uni-popup ref="rectifyPopup" type="bottom">
      <view class="rectify-sheet">
        <text class="rectify-title">提交整改结果</text>
        <text class="rectify-sub">请说明整改措施，必要时补充现场图片，方便监督员复核。</text>
        <textarea
          v-model="rectifyForm.rectifyResult"
          class="rectify-textarea"
          placeholder="例如：已更换餐品、重新消毒、补充明码标价"
          maxlength="300"
        />
        <view class="evidence-row" v-if="rectifyForm.evidenceFiles.length">
          <image
            v-for="(url, index) in rectifyForm.evidenceFiles"
            :key="`rectify-${index}`"
            class="evidence-image"
            :src="url"
            mode="aspectFill"
            @tap="previewImageList(rectifyForm.evidenceFiles, index)"
          />
        </view>
        <view class="rectify-actions">
          <button size="mini" @tap="chooseRectifyImages">{{ rectifyUploading ? '上传中...' : '补充整改图片' }}</button>
          <view class="rectify-footer-actions">
            <button size="mini" @tap="closeRectify">取消</button>
            <button size="mini" type="primary" @tap="submitRectify">提交整改</button>
          </view>
        </view>
      </view>
    </uni-popup>

    <uni-popup ref="evaluatePopup" type="center">
      <view class="evaluate-popup">
        <text class="evaluate-title">评价处理结果</text>
        <text class="evaluate-sub">请对本次投诉处理结果进行评价</text>
        <view class="rating-row">
          <text
            v-for="n in 3"
            :key="n"
            class="rating-item"
            :class="{ active: evaluateRating === n }"
            @tap="setRating(n)"
          >
            {{ ratingText(n) }}
          </text>
        </view>
        <textarea
          v-model="evaluateFeedback"
          class="evaluate-textarea"
          placeholder="请输入您的反馈意见（可选）"
          maxlength="200"
        />
        <view class="evaluate-actions">
          <button size="mini" @tap="closeEvaluate">取消</button>
          <button size="mini" type="primary" @tap="submitEvaluate">提交评价</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { canteenApi, userApi } from '@/utils/api.js';
import { formatDateTime } from '@/utils/format.js';
import { isMerchantRole, isSupervisorRole, isStudentRole, roleLabel } from '@/utils/permission.js';

const STATUS_VALUES = ['', 'pending_review', 'pending_rectify', 'rectified', 'completed', 'rejected'];
const STATUS_LABELS = ['全部', '待审核', '待整改', '已整改', '已完成', '已驳回'];

export default {
  data() {
    return {
      userInfo: {},
      roleText: '',
      isMerchant: false,
      isSupervisor: false,
      isStudent: false,
      currentEvaluateId: null,
      evaluateRating: 3,
      evaluateFeedback: '',
      statusIndex: 0,
      statusLabels: STATUS_LABELS,
      list: [],
      current: 1,
      pageSize: 10,
      hasMore: true,
      loading: false,
      currentRectifyId: null,
      rectifyUploading: false,
      rectifyForm: {
        rectifyResult: '',
        evidenceUrls: '',
        evidenceFiles: []
      }
    };
  },
  async onShow() {
    await this.loadUser();
    await this.loadList(true);
  },
  onPullDownRefresh() {
    this.loadList(true).finally(() => uni.stopPullDownRefresh());
  },
  methods: {
    format(value) {
      return formatDateTime(value);
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
    async loadUser() {
      try {
        this.userInfo = await userApi.fetchCurrentUser();
      } catch (error) {
        this.userInfo = uni.getStorageSync('userInfo') || {};
      }
      this.isMerchant = isMerchantRole(this.userInfo);
      this.isSupervisor = isSupervisorRole(this.userInfo);
      this.isStudent = isStudentRole(this.userInfo);
      this.roleText = roleLabel(this.userInfo);
    },
    async loadList(reset = false) {
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
          status: STATUS_VALUES[this.statusIndex] || undefined
        };
        const page = this.isSupervisor ? await canteenApi.listComplaints(params) : await canteenApi.listMyComplaints(params);
        const records = page.records || [];
        if (reset) {
          this.list = records;
        } else {
          this.list = [...this.list, ...records];
        }
        this.hasMore = records.length === this.pageSize;
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      } finally {
        this.loading = false;
      }
    },
    onStatusChange(e) {
      this.statusIndex = e.currentIndex;
      this.loadList(true);
    },
    loadMore() {
      if (!this.hasMore || this.loading) return;
      this.current += 1;
      this.loadList(false);
    },
    openRectify(item) {
      this.currentRectifyId = item.id;
      this.rectifyForm.rectifyResult = item.rectifyResult || '';
      const files = this.parseEvidenceUrls(item.evidenceUrls);
      this.rectifyForm.evidenceFiles = files;
      this.rectifyForm.evidenceUrls = files.length ? JSON.stringify(files) : '';
      this.$refs.rectifyPopup.open();
    },
    closeRectify() {
      this.currentRectifyId = null;
      this.rectifyUploading = false;
      this.rectifyForm = {
        rectifyResult: '',
        evidenceUrls: '',
        evidenceFiles: []
      };
      this.$refs.rectifyPopup.close();
    },
    async chooseRectifyImages() {
      if (this.rectifyUploading) return;
      uni.chooseImage({
        count: 3,
        success: async (res) => {
          const files = res.tempFilePaths || [];
          if (!files.length) return;
          this.rectifyUploading = true;
          try {
            const uploaded = [...this.rectifyForm.evidenceFiles];
            for (const filePath of files) {
              const result = await canteenApi.uploadFile(filePath, 'complaint');
              if (result?.url) {
                uploaded.push(result.url);
              }
            }
            this.rectifyForm.evidenceFiles = Array.from(new Set(uploaded));
            this.rectifyForm.evidenceUrls = JSON.stringify(this.rectifyForm.evidenceFiles);
            uni.showToast({ title: '整改图片已上传', icon: 'success' });
          } catch (error) {
            uni.showToast({ title: error.message || '上传失败', icon: 'none' });
          } finally {
            this.rectifyUploading = false;
          }
        }
      });
    },
    async submitRectify() {
      if (!this.currentRectifyId) return;
      if (!this.rectifyForm.rectifyResult) {
        uni.showToast({ title: '请填写整改内容', icon: 'none' });
        return;
      }
      try {
        await canteenApi.submitRectify({
          complaintId: this.currentRectifyId,
          rectifyResult: this.rectifyForm.rectifyResult,
          evidenceUrls: this.rectifyForm.evidenceUrls || undefined
        });
        uni.showToast({ title: '整改已提交', icon: 'success' });
        this.closeRectify();
        await this.loadList(true);
      } catch (error) {
        uni.showToast({ title: error.message || '提交失败', icon: 'none' });
      }
    },
    ratingText(rating) {
      const map = { 1: '不满意', 2: '一般', 3: '满意' };
      return map[rating] || '未知';
    },
    openEvaluate(item) {
      this.currentEvaluateId = item.id;
      this.evaluateRating = 3;
      this.evaluateFeedback = '';
      this.$refs.evaluatePopup.open();
    },
    closeEvaluate() {
      this.currentEvaluateId = null;
      this.evaluateRating = 3;
      this.evaluateFeedback = '';
      this.$refs.evaluatePopup.close();
    },
    setRating(rating) {
      this.evaluateRating = rating;
    },
    parseEvidenceUrls(urls) {
      if (!urls) return [];
      const source = String(urls).trim();
      if (!source) return [];
      const normalizeList = (list) => list
        .map((item) => this.getImageUrl(String(item || '').trim()))
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
    getImageUrl(url) {
      if (!url) return '';
      if (url.startsWith('http://') || url.startsWith('https://')) return url;
      if (url.startsWith('/api/')) return url;
      if (url.startsWith('/')) return url;
      return '/api/file/preview/' + url;
    },
    previewEvidence(urls, index) {
      const imageUrls = this.parseEvidenceUrls(urls);
      this.previewImageList(imageUrls, index);
    },
    previewImageList(urls, index) {
      if (!urls || !urls.length) return;
      uni.previewImage({
        urls,
        current: urls[index] || urls[0]
      });
    },
    progressText(item) {
      if (!item) return '待处理';
      if (item.status === 'pending_review') return '等待监督管理员审核';
      if (item.status === 'pending_rectify') return item.rectifyRequirement || '已通知商户整改';
      if (item.status === 'rectified') return item.rectifyResult || '商户已提交整改结果，等待复核';
      if (item.status === 'completed') return item.feedback || item.rectifyResult || '投诉已处理完成';
      if (item.status === 'rejected') return item.feedback || '投诉已驳回';
      return item.processProgress || '待处理';
    },
    async submitEvaluate() {
      if (!this.currentEvaluateId) return;
      try {
        await canteenApi.evaluateComplaint({
          complaintId: this.currentEvaluateId,
          resultRating: this.evaluateRating,
          feedback: this.evaluateFeedback
        });
        uni.showToast({ title: '评价已提交', icon: 'success' });
        this.closeEvaluate();
        await this.loadList(true);
      } catch (error) {
        uni.showToast({ title: error.message || '评价失败', icon: 'none' });
      }
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.header-card {
  background: #fff;
  border-radius: 18rpx;
  padding: 18rpx;
  margin-bottom: 14rpx;
}

.evidence-tip {
  margin-bottom: 12rpx;
}

.title {
  display: block;
  font-size: 30rpx;
  font-weight: 700;
  color: #1e2f4f;
}

.sub {
  display: block;
  margin-top: 6rpx;
  margin-bottom: 10rpx;
  color: #6d7892;
  font-size: 22rpx;
}

.complaint-card {
  margin-bottom: 12rpx;
}

.row-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no {
  color: #617198;
  font-size: 22rpx;
}

.complaint-title {
  display: block;
  margin-top: 6rpx;
  font-size: 28rpx;
  color: #1e2f4f;
  font-weight: 600;
}

.meta,
.content,
.progress,
.time {
  display: block;
  margin-top: 6rpx;
  font-size: 23rpx;
  color: #617198;
}

.evidence-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 10rpx;
}

.evidence-image {
  width: 144rpx;
  height: 144rpx;
  border-radius: 12rpx;
  background: #eef2f7;
}

.op-row {
  margin-top: 10rpx;
  display: flex;
  justify-content: flex-end;
}

.empty-box {
  text-align: center;
  padding: 80rpx 0;
}

.empty-text {
  color: #9aa4b8;
  font-size: 24rpx;
}

.load-more {
  text-align: center;
  margin: 18rpx 0;
}

.load-more-text {
  color: #2f65f9;
  font-size: 24rpx;
}

.rectify-sheet {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 28rpx 24rpx calc(28rpx + env(safe-area-inset-bottom));
}

.rectify-title {
  display: block;
  font-size: 32rpx;
  font-weight: 700;
  color: #1e2f4f;
}

.rectify-sub {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 1.6;
  color: #6d7892;
}

.rectify-textarea {
  width: 100%;
  min-height: 180rpx;
  margin-top: 20rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  background: #f6f8fc;
  box-sizing: border-box;
}

.rectify-actions {
  margin-top: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.rectify-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.evaluate-popup {
  background: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  width: 560rpx;
}

.evaluate-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #1e2f4f;
  text-align: center;
  margin-bottom: 12rpx;
}

.evaluate-sub {
  display: block;
  font-size: 24rpx;
  color: #6d7892;
  text-align: center;
  margin-bottom: 30rpx;
}

.rating-row {
  display: flex;
  justify-content: center;
  gap: 30rpx;
  margin-bottom: 30rpx;
}

.rating-item {
  padding: 16rpx 32rpx;
  border-radius: 8rpx;
  background: #f4f6fb;
  color: #617198;
  font-size: 26rpx;
}

.rating-item.active {
  background: #2f65f9;
  color: #fff;
}

.evaluate-textarea {
  width: 100%;
  height: 160rpx;
  background: #f4f6fb;
  border-radius: 8rpx;
  padding: 16rpx;
  font-size: 26rpx;
  box-sizing: border-box;
  margin-bottom: 30rpx;
}

.evaluate-actions {
  display: flex;
  justify-content: center;
  gap: 30rpx;
}
</style>
