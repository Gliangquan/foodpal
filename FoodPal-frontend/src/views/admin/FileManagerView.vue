<template>
  <div class="page-container">
    <a-page-header
      title="文件管理"
      sub-title="直接管理 MinIO 文件：上传、建文件夹、查看、下载"
      @back="() => $router.back()"
    />

    <a-card :bordered="false" style="margin-top: 16px">
      <div class="toolbar">
        <div class="path-row">
          <a-breadcrumb>
            <a-breadcrumb-item>
              <a @click.prevent="goToRoot">根目录</a>
            </a-breadcrumb-item>
            <a-breadcrumb-item v-for="(segment, index) in pathSegments" :key="`${index}-${segment}`">
              <a @click.prevent="goToSegment(index)">{{ segment }}</a>
            </a-breadcrumb-item>
          </a-breadcrumb>
        </div>

        <a-space wrap>
          <a-button :disabled="!currentPrefix" @click="goParent">上一级</a-button>
          <a-button :loading="loading" @click="fetchObjects">刷新</a-button>
          <a-button type="primary" @click="folderModalOpen = true">新建文件夹</a-button>
          <a-upload
            :show-upload-list="false"
            :multiple="true"
            :before-upload="handleBeforeUpload"
          >
            <a-button type="primary" ghost :loading="uploading">上传文件</a-button>
          </a-upload>
        </a-space>
      </div>

      <a-table
        :columns="columns"
        :data-source="objects"
        :loading="loading"
        :pagination="false"
        :row-key="(record: MinioObjectItem) => record.objectPath"
        :scroll="{ x: 900 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a-space>
              <folder-open-outlined v-if="record.directory" style="color: #faad14" />
              <file-outlined v-else style="color: #1890ff" />
              <a v-if="record.directory" @click.prevent="enterDirectory(record)">
                {{ record.name }}
              </a>
              <span v-else>{{ record.name }}</span>
            </a-space>
          </template>

          <template v-else-if="column.key === 'type'">
            <a-tag :color="record.directory ? 'gold' : 'blue'">
              {{ record.directory ? '文件夹' : '文件' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'size'">
            {{ record.directory ? '-' : formatFileSize(record.size || 0) }}
          </template>

          <template v-else-if="column.key === 'lastModified'">
            {{ formatDate(record.lastModified) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="record.directory"
                type="link"
                size="small"
                @click="enterDirectory(record)"
              >
                打开
              </a-button>
              <a-button
                v-else
                type="link"
                size="small"
                @click="handlePreview(record)"
              >
                查看
              </a-button>
              <a-button
                v-if="!record.directory"
                type="link"
                size="small"
                @click="handleDownload(record)"
              >
                下载
              </a-button>
              <a-popconfirm
                :title="record.directory ? '删除该文件夹及其内容？' : '删除该文件？'"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="folderModalOpen"
      title="新建文件夹"
      :confirm-loading="creatingFolder"
      @ok="handleCreateFolder"
      @cancel="folderName = ''"
    >
      <a-form layout="vertical">
        <a-form-item label="文件夹名称" required>
          <a-input v-model:value="folderName" placeholder="请输入文件夹名称（不含 /）" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="imagePreviewOpen"
      :title="previewTitle"
      :footer="null"
      width="900px"
      @cancel="closeImagePreview"
    >
      <div class="image-preview-wrap">
        <img :src="previewUrl" :alt="previewTitle" class="preview-image" />
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { message } from 'ant-design-vue';
import { FileOutlined, FolderOpenOutlined } from '@ant-design/icons-vue';
import {
  type MinioObjectItem,
  createMinioFolder,
  deleteMinioObject,
  getMinioDownloadUrl,
  getMinioPreviewUrl,
  listMinioObjects,
  uploadMinioFile,
} from '../../api';
import { formatDateTime, formatFileSize } from '../../utils/format';

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name', width: 260 },
  { title: '类型', key: 'type', width: 110 },
  { title: '大小', dataIndex: 'size', key: 'size', width: 140 },
  { title: '修改时间', dataIndex: 'lastModified', key: 'lastModified', width: 220 },
  { title: '对象路径', dataIndex: 'objectPath', key: 'objectPath' },
  { title: '操作', key: 'action', width: 220, fixed: 'right' as const },
];

const loading = ref(false);
const uploading = ref(false);
const creatingFolder = ref(false);
const objects = ref<MinioObjectItem[]>([]);
const currentPrefix = ref('');

const folderModalOpen = ref(false);
const folderName = ref('');

const imagePreviewOpen = ref(false);
const previewUrl = ref('');
const previewTitle = ref('');

const pathSegments = computed(() =>
  currentPrefix.value
    .split('/')
    .map((item) => item.trim())
    .filter(Boolean)
);

const normalizePrefix = (prefix?: string) => {
  if (!prefix) return '';
  const normalized = prefix.replace(/\\/g, '/').replace(/^\/+|\/+$/g, '');
  if (!normalized) return '';
  return `${normalized}/`;
};

const formatDate = (value?: string) => {
  if (!value) return '-';
  return formatDateTime(value);
};

const fetchObjects = async () => {
  loading.value = true;
  try {
    const response = await listMinioObjects(currentPrefix.value || undefined);
    if (response.code === 0) {
      objects.value = response.data || [];
    } else {
      message.error(response.message || '获取文件列表失败');
    }
  } catch (error: any) {
    message.error(error?.message || '获取文件列表失败');
  } finally {
    loading.value = false;
  }
};

const goToRoot = () => {
  currentPrefix.value = '';
  fetchObjects();
};

const goToSegment = (index: number) => {
  const prefix = pathSegments.value.slice(0, index + 1).join('/');
  currentPrefix.value = normalizePrefix(prefix);
  fetchObjects();
};

const goParent = () => {
  if (!currentPrefix.value) return;
  const segments = [...pathSegments.value];
  segments.pop();
  currentPrefix.value = normalizePrefix(segments.join('/'));
  fetchObjects();
};

const enterDirectory = (record: MinioObjectItem) => {
  if (!record.directory) return;
  currentPrefix.value = normalizePrefix(record.objectPath);
  fetchObjects();
};

const handleCreateFolder = async () => {
  const name = folderName.value.trim();
  if (!name) {
    message.warning('请输入文件夹名称');
    return;
  }
  creatingFolder.value = true;
  try {
    const response = await createMinioFolder(currentPrefix.value, name);
    if (response.code === 0) {
      message.success('文件夹创建成功');
      folderModalOpen.value = false;
      folderName.value = '';
      fetchObjects();
    } else {
      message.error(response.message || '文件夹创建失败');
    }
  } catch (error: any) {
    message.error(error?.message || '文件夹创建失败');
  } finally {
    creatingFolder.value = false;
  }
};

const handleBeforeUpload = async (file: File) => {
  uploading.value = true;
  try {
    const response = await uploadMinioFile(file, currentPrefix.value || undefined);
    if (response.code === 0) {
      message.success(`上传成功：${file.name}`);
      fetchObjects();
    } else {
      message.error(response.message || '上传失败');
    }
  } catch (error: any) {
    message.error(error?.message || `上传失败：${file.name}`);
  } finally {
    uploading.value = false;
  }
  return false;
};

const isImageFile = (fileName: string) => /\.(png|jpg|jpeg|gif|webp|bmp|svg)$/i.test(fileName);

const handlePreview = (record: MinioObjectItem) => {
  if (record.directory) return;
  const url = getMinioPreviewUrl(record.objectPath);
  if (isImageFile(record.name)) {
    previewUrl.value = url;
    previewTitle.value = record.name;
    imagePreviewOpen.value = true;
    return;
  }
  window.open(url, '_blank');
};

const closeImagePreview = () => {
  imagePreviewOpen.value = false;
  previewUrl.value = '';
  previewTitle.value = '';
};

const handleDownload = (record: MinioObjectItem) => {
  if (record.directory) return;
  window.open(getMinioDownloadUrl(record.objectPath), '_blank');
};

const handleDelete = async (record: MinioObjectItem) => {
  try {
    const response = await deleteMinioObject(record.objectPath, record.directory);
    if (response.code === 0) {
      message.success('删除成功');
      fetchObjects();
    } else {
      message.error(response.message || '删除失败');
    }
  } catch (error: any) {
    message.error(error?.message || '删除失败');
  }
};

onMounted(() => {
  fetchObjects();
});
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.path-row {
  min-width: 260px;
}

.image-preview-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  max-height: 70vh;
  overflow: auto;
}

.preview-image {
  max-width: 100%;
  max-height: 68vh;
  object-fit: contain;
}
</style>
