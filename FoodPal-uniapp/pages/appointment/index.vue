<template>
  <view class="page-content">
    <uni-card :border="false" padding="20">
      <view class="title">发起投诉</view>
      <view class="sub">请选择投诉商户并填写详细情况，可上传图片/视频链接作为证据。</view>

      <uni-forms ref="formRef" :modelValue="form" label-position="top">
        <uni-forms-item label="投诉商户" required>
          <uni-data-select v-model="form.merchantId" :localdata="merchantOptions" placeholder="请选择商户" />
        </uni-forms-item>

        <uni-forms-item label="投诉标题" required>
          <uni-easyinput v-model="form.complaintTitle" placeholder="例如：餐品与描述不符" />
        </uni-forms-item>

        <uni-forms-item label="投诉内容" required>
          <uni-easyinput type="textarea" v-model="form.complaintContent" :maxlength="500" placeholder="请描述问题经过、时间、地点等" />
        </uni-forms-item>

        <uni-forms-item label="证据图片(可选)">
          <view class="evidence-list" v-if="evidenceFiles.length">
            <image
              v-for="(item, index) in evidenceFiles"
              :key="`${item}-${index}`"
              class="evidence-image"
              :src="item"
              mode="aspectFill"
            />
          </view>
          <button class="btn-secondary" size="mini" type="default" @tap="chooseEvidenceImages">
            {{ uploadingEvidence ? '上传中...' : '上传证据图片' }}
          </button>
        </uni-forms-item>
      </uni-forms>

      <button class="btn-primary" :disabled="submitting" @tap="submitComplaint">
        {{ submitting ? '提交中...' : '提交投诉' }}
      </button>
    </uni-card>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';

export default {
  data() {
    return {
      submitting: false,
      presetMerchantId: '',
      presetMerchantName: '',
      merchantOptions: [],
      uploadingEvidence: false,
      evidenceFiles: [],
      form: {
        merchantId: undefined,
        complaintTitle: '',
        complaintContent: '',
        evidenceUrls: ''
      }
    };
  },
  async onLoad(options = {}) {
    const merchantId = Number(options.merchantId);
    this.presetMerchantId = Number.isFinite(merchantId) && merchantId > 0 ? merchantId : '';
    if (options.merchantName) {
      try {
        this.presetMerchantName = decodeURIComponent(options.merchantName);
      } catch (e) {
        this.presetMerchantName = options.merchantName;
      }
    }
    await this.loadMerchants();
  },
  methods: {
    async loadMerchants() {
      try {
        const page = await canteenApi.listMerchants({ current: 1, size: 200, status: 1, auditStatus: 'approved' });
        const records = page.records || [];
        this.merchantOptions = records.map((item) => ({
          value: item.id,
          text: item.merchantName
        }));

        if (this.presetMerchantId) {
          const existed = this.merchantOptions.some((item) => Number(item.value) === Number(this.presetMerchantId));
          if (!existed) {
            this.merchantOptions.unshift({
              value: this.presetMerchantId,
              text: this.presetMerchantName || `商户#${this.presetMerchantId}`
            });
          }
          this.form.merchantId = this.presetMerchantId;
          if (!this.form.complaintTitle && this.presetMerchantName) {
            this.form.complaintTitle = `关于${this.presetMerchantName}的就餐投诉`;
          }
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载商户失败', icon: 'none' });
      }
    },
    async chooseEvidenceImages() {
      if (this.uploadingEvidence) return;
      uni.chooseImage({
        count: 3,
        success: async (res) => {
          const files = res.tempFilePaths || [];
          if (!files.length) return;
          this.uploadingEvidence = true;
          try {
            const uploaded = [];
            for (const filePath of files) {
              const result = await canteenApi.uploadFile(filePath, 'complaint');
              if (result?.url) {
                uploaded.push(result.url);
              }
            }
            this.evidenceFiles = uploaded;
            this.form.evidenceUrls = JSON.stringify(uploaded);
            uni.showToast({ title: '证据上传成功', icon: 'success' });
          } catch (error) {
            uni.showToast({ title: error.message || '证据上传失败', icon: 'none' });
          } finally {
            this.uploadingEvidence = false;
          }
        }
      });
    },
    async submitComplaint() {
      if (this.submitting) return;
      if (!this.form.merchantId || !this.form.complaintTitle || !this.form.complaintContent) {
        return uni.showToast({ title: '请填写完整信息', icon: 'none' });
      }

      this.submitting = true;
      try {
        await canteenApi.createComplaint({
          merchantId: this.form.merchantId,
          complaintTitle: this.form.complaintTitle,
          complaintContent: this.form.complaintContent,
          evidenceUrls: this.form.evidenceUrls || undefined
        });
        uni.showToast({ title: '投诉提交成功', icon: 'success' });
        setTimeout(() => {
          uni.switchTab({ url: '/pages/order/index' });
        }, 600);
      } catch (error) {
        uni.showToast({ title: error.message || '投诉提交失败', icon: 'none' });
      } finally {
        this.submitting = false;
      }
    }
  }
};
</script>

<style scoped lang="scss">
@import "@/styles/common.scss";

.title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1f2f4f;
}

.sub {
  font-size: 22rpx;
  color: #6f7890;
  margin: 8rpx 0 16rpx;
  line-height: 1.6;
}

.evidence-list {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-bottom: 12rpx;
}

.evidence-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #f5f5f5;
}
</style>
