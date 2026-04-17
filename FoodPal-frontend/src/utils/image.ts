const HTTP_URL_RE = /^https?:\/\//i;

/**
 * 统一前端可访问的图片地址格式
 */
export function normalizeImageUrl(url?: string): string {
  if (!url) return '';
  const value = url.trim();
  if (!value) return '';
  if (HTTP_URL_RE.test(value) || value.startsWith('data:') || value.startsWith('blob:')) {
    return value;
  }
  if (value.startsWith('/api/')) {
    return value;
  }
  if (value.startsWith('/file/')) {
    return `/api${value}`;
  }
  if (value.startsWith('api/')) {
    return `/${value}`;
  }
  if (value.startsWith('/')) {
    return value;
  }
  return `/api/file/preview/${value}`;
}

/**
 * 兼容函数保留：当前仅做标准化，具体兼容逻辑由后端 /file/preview 处理。
 */
export function resolveDishImageForAdmin(url?: string): string {
  return normalizeImageUrl(url);
}
