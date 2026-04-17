<template>
  <div class="image-upload-container">
    <a-upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :max-count="maxCount"
      :before-upload="beforeUpload"
      :custom-request="handleUpload"
      :remove="handleRemove"
      :accept="accept"
      :disabled="disabled"
      @preview="handlePreview"
    >
      <div v-if="fileList.length < maxCount && !disabled">
        <loading-outlined v-if="uploading" />
        <plus-outlined v-else />
        <div class="ant-upload-text">上传图片</div>
      </div>
    </a-upload>

    <a-modal :open="previewVisible" :title="previewTitle" :footer="null" @cancel="handleCancel">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue';
import type { UploadFile, UploadProps } from 'ant-design-vue';
import { uploadDishImage, uploadMerchantImage, uploadUserAvatar } from '../api';
import { normalizeImageUrl } from '../utils/image';

interface Props {
  modelValue?: string;
  type?: 'dish' | 'merchant' | 'user';
  maxCount?: number;
  accept?: string;
  maxSize?: number;
  disabled?: boolean;
}

interface Emits {
  (e: 'update:modelValue', value: string): void;
  (e: 'change', value: string): void;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  type: 'dish',
  maxCount: 1,
  accept: 'image/*',
  maxSize: 10,
  disabled: false
});

const emit = defineEmits<Emits>();

const fileList = ref<UploadFile[]>([]);
const uploading = ref(false);
const previewVisible = ref(false);
const previewImage = ref('');
const previewTitle = ref('');
const getDisplayUrl = (url?: string) => normalizeImageUrl(url);

const updateFileList = () => {
  if (props.modelValue) {
    fileList.value = [{
      uid: '-1',
      name: 'image.png',
      status: 'done',
      url: getDisplayUrl(props.modelValue),
    }];
  } else {
    fileList.value = [];
  }
};

watch(() => props.modelValue, updateFileList, { immediate: true });

const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    message.error('只能上传图片文件!');
    return false;
  }
  const isLtSize = file.size / 1024 / 1024 < props.maxSize;
  if (!isLtSize) {
    message.error(`图片大小不能超过 ${props.maxSize}MB!`);
    return false;
  }
  return true;
};

const handleUpload: UploadProps['customRequest'] = async (options) => {
  const { file, onSuccess, onError } = options;
  uploading.value = true;

  try {
    let imageUrl = '';
    
    switch (props.type) {
      case 'dish':
        imageUrl = await uploadDishImage(file as File);
        break;
      case 'merchant':
        imageUrl = await uploadMerchantImage(file as File);
        break;
      case 'user':
        imageUrl = await uploadUserAvatar(file as File);
        break;
      default:
        imageUrl = await uploadDishImage(file as File);
    }

    const url = imageUrl.data;
    
    onSuccess?.(url, file);
    emit('update:modelValue', url);
    emit('change', url);
    message.success('上传成功');
  } catch (error: any) {
    onError?.(error);
    message.error(error?.message || '上传失败');
  } finally {
    uploading.value = false;
  }
};

const handleRemove = () => {
  emit('update:modelValue', '');
  emit('change', '');
  return true;
};

const handlePreview = async (file: UploadFile) => {
  if (!file.url && !file.preview) {
    file.preview = (await getBase64(file.originFileObj as File)) as string;
  }
  previewImage.value = file.url ? getDisplayUrl(file.url as string) : (file.preview || '');
  previewVisible.value = true;
  previewTitle.value = file.name || file.url?.substring(file.url.lastIndexOf('/') + 1) || '';
};

const handleCancel = () => {
  previewVisible.value = false;
  previewTitle.value = '';
};

const getBase64 = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = (error) => reject(error);
  });
};
</script>

<style scoped>
.image-upload-container {
  display: inline-block;
}

.ant-upload-select-picture-card i {
  font-size: 32px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
  font-size: 14px;
}
</style>
