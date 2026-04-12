import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import { message } from 'ant-design-vue';

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 允许携带 cookies
});

// 请求拦截器
request.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 可以在这里添加 token 等认证信息
    const user = localStorage.getItem('user');
    if (user) {
      const userObj = JSON.parse(user);
      if (userObj.token) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${userObj.token}`;
      }
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response;
    
    // 如果是 Blob 类型（文件下载），直接返回 data
    if (response.config.responseType === 'blob') {
      return data;
    }
    
    // 处理后端统一响应格式
    if (data && typeof data.code !== 'undefined') {
      if (data.code !== 0) {
        // 业务错误
        message.error(data.message || '请求失败');
        return Promise.reject(new Error(data.message || '请求失败'));
      }
      // 成功时直接返回 data
      return data;
    }
    
    return response;
  },
  (error: AxiosError) => {
    // HTTP 错误处理
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          message.error('未登录或登录已过期');
          // 可以在这里处理登出逻辑
          localStorage.removeItem('user');
          window.location.href = '/login';
          break;
        case 403:
          message.error('没有权限访问');
          break;
        case 404:
          message.error('请求的资源不存在');
          break;
        case 500:
          message.error(data?.message || '服务器错误');
          break;
        default:
          message.error(data?.message || `请求失败 (${status})`);
      }
    } else if (error.request) {
      message.error('网络请求失败，请检查网络连接');
    } else {
      message.error(error.message || '请求出错');
    }
    
    return Promise.reject(error);
  }
);

export default request;
