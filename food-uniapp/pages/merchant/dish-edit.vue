<template>
  <view class="page-content">
    <view class="header-card">
      <text class="title">{{ isEdit ? '编辑菜品' : '新增菜品' }}</text>
      <text class="sub">{{ isEdit ? '修改菜品信息' : '添加新菜品到您的店铺' }}</text>
    </view>

    <uni-card :border="false" padding="24">
      <uni-forms :model="dishForm" label-position="top">
        <uni-forms-item label="菜品名称" required>
          <uni-easyinput v-model="dishForm.dishName" placeholder="请输入菜品名称" />
        </uni-forms-item>

        <uni-forms-item label="菜品价格" required>
          <uni-easyinput v-model="dishForm.dishPrice" type="digit" placeholder="请输入菜品价格" />
        </uni-forms-item>

        <uni-forms-item label="菜品分类">
          <uni-data-select v-model="dishForm.category" :localdata="categoryOptions" placeholder="请选择分类" />
        </uni-forms-item>

        <uni-forms-item label="菜品图片">
          <view class="image-upload" @tap="uploadImage">
            <image v-if="dishForm.dishImage" :src="getImageUrl(dishForm.dishImage)" mode="aspectFill" class="preview-img" />
            <view v-else class="upload-placeholder">
              <text class="icon">+</text>
              <text class="text">点击上传图片</text>
            </view>
          </view>
        </uni-forms-item>

        <uni-forms-item label="食材组成">
          <uni-easyinput v-model="dishForm.ingredients" type="textarea" placeholder="请输入食材组成，如：猪肉、白菜、面粉等" />
        </uni-forms-item>

        <uni-forms-item label="营养成分">
          <uni-easyinput v-model="dishForm.nutritionInfo" type="textarea" placeholder="请输入营养成分信息" />
        </uni-forms-item>

        <uni-forms-item label="供应状态">
          <uni-data-checkbox v-model="dishForm.supplyStatus" :localdata="supplyStatusOptions" />
        </uni-forms-item>

        <uni-forms-item label="特价设置">
          <view class="special-section">
            <uni-data-checkbox v-model="isSpecial" :localdata="[{text: '启用特价', value: true}]" />
            <template v-if="isSpecial">
              <uni-easyinput v-model="dishForm.specialPrice" type="digit" placeholder="特价价格" style="margin-top: 10rpx;" />
              <view class="time-row">
                <text class="label">开始时间：</text>
                <uni-datetime-picker v-model="dishForm.specialStartTime" type="datetime" />
              </view>
              <view class="time-row">
                <text class="label">结束时间：</text>
                <uni-datetime-picker v-model="dishForm.specialEndTime" type="datetime" />
              </view>
            </template>
          </view>
        </uni-forms-item>
      </uni-forms>

      <button class="submit-btn" type="primary" @tap="saveDish">{{ isEdit ? '保存修改' : '添加菜品' }}</button>
    </uni-card>
  </view>
</template>

<script>
import { canteenApi } from '@/utils/api.js';
import { normalizeFileUrl } from '@/utils/format.js';

export default {
  data() {
    return {
      defaultImg: '/static/logo.png',
      isEdit: false,
      dishId: null,
      isSpecial: false,
      dishForm: {
        dishName: '',
        dishPrice: '',
        category: '',
        dishImage: '',
        ingredients: '',
        nutritionInfo: '',
        supplyStatus: 1,
        specialPrice: '',
        specialStartTime: '',
        specialEndTime: ''
      },
      categoryOptions: [
        { value: '主食', text: '主食' },
        { value: '面食', text: '面食' },
        { value: '汤类', text: '汤类' },
        { value: '凉菜', text: '凉菜' },
        { value: '小吃', text: '小吃' },
        { value: '饮品', text: '饮品' }
      ],
      supplyStatusOptions: [
        { value: 1, text: '可供应' },
        { value: 0, text: '已售罄' }
      ]
    };
  },
  onLoad(options) {
    if (options.id) {
      this.isEdit = true;
      this.dishId = options.id;
      this.loadDishDetail();
    }
  },
  methods: {
    async loadDishDetail() {
      try {
        uni.showLoading({ title: '加载中...' });
        const dish = await canteenApi.getDishDetail(this.dishId);
        if (dish) {
          this.dishForm = {
            dishName: dish.dishName || '',
            dishPrice: dish.dishPrice || '',
            category: dish.category || '',
            dishImage: dish.dishImage || '',
            ingredients: dish.ingredients || '',
            nutritionInfo: dish.nutritionInfo || '',
            supplyStatus: dish.supplyStatus || 1,
            specialPrice: dish.specialPrice || '',
            specialStartTime: dish.specialStartTime || '',
            specialEndTime: dish.specialEndTime || ''
          };
          this.isSpecial = !!dish.specialPrice;
        }
      } catch (error) {
        uni.showToast({ title: error.message || '加载失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    uploadImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const tempFilePath = res.tempFilePaths[0];
          this.uploadFile(tempFilePath);
        }
      });
    },
    async uploadFile(filePath) {
      try {
        uni.showLoading({ title: '上传中...' });
        const result = await canteenApi.uploadFile(filePath, 'dish');
        if (result && result.url) {
          this.dishForm.dishImage = result.url;
          uni.showToast({ title: '上传成功', icon: 'success' });
        }
      } catch (error) {
        uni.showToast({ title: error.message || '上传失败', icon: 'none' });
      } finally {
        uni.hideLoading();
      }
    },
    getImageUrl(imageUrl) {
      return normalizeFileUrl(imageUrl);
    },
    async saveDish() {
      if (!this.dishForm.dishName) {
        uni.showToast({ title: '请输入菜品名称', icon: 'none' });
        return;
      }
      if (!this.dishForm.dishPrice) {
        uni.showToast({ title: '请输入菜品价格', icon: 'none' });
        return;
      }

      try {
        uni.showLoading({ title: '保存中...' });
        const payload = { ...this.dishForm };
        if (this.isEdit) {
          payload.id = this.dishId;
          await canteenApi.updateDish(payload);
        } else {
          await canteenApi.createDish(payload);
        }
        uni.showToast({ title: '保存成功', icon: 'success' });
        setTimeout(() => {
          uni.navigateBack();
        }, 1000);
      } catch (error) {
        uni.showToast({ title: error.message || '保存失败', icon: 'none' });
      } finally {
        uni.hideLoading();
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

.image-upload {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed #d8e2ff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #9aa4b8;
}

.icon {
  font-size: 60rpx;
  line-height: 1;
}

.text {
  font-size: 24rpx;
  margin-top: 10rpx;
}

.preview-img {
  width: 100%;
  height: 100%;
  background: #f5f7fa;
}

.special-section {
  padding: 20rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
}

.time-row {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
}

.label {
  font-size: 26rpx;
  color: #6d7892;
  width: 160rpx;
}

.submit-btn {
  margin-top: 30rpx;
}
</style>
