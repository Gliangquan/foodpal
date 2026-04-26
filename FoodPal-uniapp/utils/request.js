const DEFAULT_BASE_URL = '/api';
const LOCAL_DEV_BASE_URL = '/api';
// 微信小程序不支持代理，必须使用完整的后端地址
// TODO: 部署时替换为实际的后端域名，如 https://your-domain.com/api
const MP_WEIXIN_BASE_URL = 'http://localhost:19941/api';
const TOKEN_KEY = 'foodpal_token';
const BASE_URL_KEY = 'foodpal_base_url';

const normalizeBaseUrl = (baseUrl) => {
  const url = String(baseUrl || '').trim();
  if (!url) return '';
  return url.replace(/\/+$/, '');
};

const resolveBaseUrl = () => {
  const runtimeBaseUrl = normalizeBaseUrl(uni.getStorageSync(BASE_URL_KEY));
  if (runtimeBaseUrl) {
    return runtimeBaseUrl;
  }

  // #ifdef MP-WEIXIN
  return MP_WEIXIN_BASE_URL;
  // #endif

  // #ifdef H5
  if (typeof window !== 'undefined') {
    const host = String(window.location.hostname || '').toLowerCase();
    if (host === 'localhost' || host === '127.0.0.1') {
      return LOCAL_DEV_BASE_URL;
    }
  }

  const envBaseUrl = normalizeBaseUrl(
    import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_BASE_URL
  );
  if (envBaseUrl) {
    return envBaseUrl;
  }
  // #endif

  return DEFAULT_BASE_URL;
};

const BASE_URL = resolveBaseUrl();

const showLoading = (show = true) => {
  if (show) {
    uni.showLoading({ title: '加载中...' });
  }
};

const hideLoading = (show = true) => {
  if (show) {
    uni.hideLoading();
  }
};

const buildQuery = (params = {}) => {
  const entries = Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '');
  if (!entries.length) return '';
  const query = entries.map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`).join('&');
  return `?${query}`;
};

const request = ({ url, method = 'GET', data = {}, params = {}, header = {}, showLoading: loading = true }) => {
  return new Promise((resolve, reject) => {
    showLoading(loading);
    const token = uni.getStorageSync(TOKEN_KEY);
    const finalUrl = `${BASE_URL}${url}${buildQuery(params)}`;
    const body = method === 'GET' ? undefined : data;
    uni.request({
      url: finalUrl,
      method,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...header
      },
      data: body,
      success: (res) => {
        hideLoading(loading);
        const { statusCode, data: response } = res;
        if (statusCode === 401 || response?.code === 40100) {
          uni.clearStorageSync();
          uni.showToast({ title: '登录过期，请重新登录', icon: 'none' });
          setTimeout(() => {
            uni.reLaunch({ url: '/pages/login/index' });
          }, 800);
          reject(new Error('未登录'));
          return;
        }
        if (statusCode >= 200 && statusCode < 300 && response) {
          if (response.code === 0) {
            resolve(response.data);
          } else {
            reject(new Error(response.message || '请求失败'));
          }
        } else {
          reject(new Error(response?.message || `请求失败 ${statusCode}`));
        }
      },
      fail: (error) => {
        hideLoading(loading);
        reject(error);
      }
    });
  });
};

const setToken = (token) => {
  if (token) {
    uni.setStorageSync(TOKEN_KEY, token);
  } else {
    uni.removeStorageSync(TOKEN_KEY);
  }
};

const getToken = () => uni.getStorageSync(TOKEN_KEY);

const setBaseUrl = (baseUrl) => {
  const normalized = normalizeBaseUrl(baseUrl);
  if (normalized) {
    uni.setStorageSync(BASE_URL_KEY, normalized);
  } else {
    uni.removeStorageSync(BASE_URL_KEY);
  }
};

export { request, setToken, getToken, BASE_URL, setBaseUrl };
