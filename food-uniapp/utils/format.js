import { BASE_URL, getToken } from '@/utils/request';

const pad = (value) => String(value).padStart(2, '0');

const formatDate = (value) => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
};

const formatDateTime = (value) => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const formatRelative = (value) => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  const diff = Date.now() - date.getTime();
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;

  if (diff < minute) return '刚刚';
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`;
  if (diff < day) return `${Math.floor(diff / hour)}小时前`;
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`;
  return formatDate(date);
};

const normalizeFileUrl = (url) => {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;
  const apiBase = String(BASE_URL || '').replace(/\/+$/, '');
  const hostBase = apiBase.replace(/\/api$/i, '');
  let finalUrl = '';
  if (url.startsWith('/api/')) {
    finalUrl = `${hostBase}${url}`;
  } else if (url.startsWith('/')) {
    finalUrl = `${apiBase}${url}`;
  } else {
    finalUrl = `${apiBase}/${url}`;
  }
  return finalUrl;
};

export { formatDate, formatDateTime, formatRelative, normalizeFileUrl };
