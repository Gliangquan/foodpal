<template>
  <view class="avatar-wrapper" :style="{ width: sizeValue, height: sizeValue }">
    <image
      v-if="imageUrl"
      class="avatar-image"
      :style="{ width: sizeValue, height: sizeValue, borderRadius: radiusValue }"
      mode="aspectFill"
      :src="imageUrl"
      @error="onImageError"
      @load="onImageLoad"
    />
    <view
      v-else
      class="avatar-fallback"
      :style="{ width: sizeValue, height: sizeValue, borderRadius: radiusValue }"
    >
      <text class="avatar-text">{{ initial }}</text>
    </view>
  </view>
</template>

<script>
import { normalizeFileUrl } from '@/utils/format.js';
import { getToken } from '@/utils/request.js';

export default {
  props: {
    src: {
      type: String,
      default: ''
    },
    name: {
      type: String,
      default: '用户'
    },
    size: {
      type: [String, Number],
      default: 'md'
    },
    radius: {
      type: [String, Number],
      default: 'md'
    }
  },
  data() {
    return {
      imageError: false
    };
  },
  computed: {
    imageUrl() {
      if (this.imageError || !this.src) return '';
      let url = normalizeFileUrl(this.src);
      // 在URL中添加token作为查询参数，用于认证
      const token = getToken();
      if (token) {
        const separator = url.includes('?') ? '&' : '?';
        url = `${url}${separator}token=${encodeURIComponent(token)}`;
      }
      console.log('Avatar URL:', { src: this.src, normalized: url });
      return url;
    },
    initial() {
      return (this.name || '用户').slice(0, 1);
    },
    sizeValue() {
      const sizeMap = {
        xs: '32rpx',
        sm: '48rpx',
        md: '64rpx',
        lg: '80rpx',
        xl: '120rpx'
      };
      return sizeMap[this.size] || this.size;
    },
    radiusValue() {
      const radiusMap = {
        xs: '4rpx',
        sm: '8rpx',
        md: '16rpx',
        lg: '24rpx',
        full: '50%'
      };
      return radiusMap[this.radius] || this.radius;
    }
  },
  methods: {
    onImageError(e) {
      console.warn('Avatar image load error:', {
        src: this.src,
        url: this.imageUrl,
        error: e
      });
      this.imageError = true;
    },
    onImageLoad() {
      console.log('Avatar image loaded successfully:', this.imageUrl);
    }
  }
};
</script>

<style scoped lang="scss">
.avatar-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-image {
  display: block;
  object-fit: cover;
}

.avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #dce6ff 0%, #eef2ff 100%);
  color: #2f65f9;
}

.avatar-text {
  font-size: 24rpx;
  font-weight: 700;
  line-height: 1;
}
</style>
