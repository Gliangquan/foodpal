import { request, setToken, getToken, BASE_URL } from '@/utils/request';

const userApi = {
  login(payload) {
    return request({
      url: '/user/login',
      method: 'POST',
      data: payload
    });
  },
  register(payload) {
    return request({
      url: '/user/register',
      method: 'POST',
      data: payload
    });
  },
  fetchCurrentUser() {
    return request({
      url: '/user/get/login',
      method: 'GET'
    });
  },
  updateProfile(payload) {
    return request({
      url: '/user/update/my',
      method: 'POST',
      data: payload
    });
  },
  logout() {
    return request({
      url: '/user/logout',
      method: 'POST'
    });
  }
};

const notificationApi = {
  list(params) {
    return request({
      url: '/notification/list',
      method: 'GET',
      params
    });
  },
  unreadCount(userId) {
    return request({
      url: '/notification/unread-count',
      method: 'GET',
      params: { userId }
    });
  },
  markAsRead(id) {
    return request({
      url: `/notification/read/${id}`,
      method: 'POST'
    });
  },
  markAllAsRead(userId) {
    return request({
      url: '/notification/read-all',
      method: 'POST',
      params: { userId }
    });
  }
};

const contentApi = {
  list(params) {
    return request({
      url: '/content/published/list',
      method: 'GET',
      params
    });
  },
  detail(id) {
    return request({
      url: `/content/published/${id}`,
      method: 'GET'
    });
  }
};

const canteenApi = {
  summary() {
    return request({
      url: '/canteen/dashboard/summary',
      method: 'GET'
    });
  },
  listDishes(params) {
    return request({
      url: '/canteen/dish/list',
      method: 'GET',
      params
    });
  },
  recommendDishes(limit = 10) {
    return request({
      url: '/canteen/dish/recommend',
      method: 'GET',
      params: { limit }
    });
  },
  toggleLike(dishId) {
    return request({
      url: `/canteen/dish/like/toggle/${dishId}`,
      method: 'POST'
    });
  },
  toggleFavorite(dishId) {
    return request({
      url: `/canteen/dish/favorite/toggle/${dishId}`,
      method: 'POST'
    });
  },
  myFavorites() {
    return request({
      url: '/canteen/dish/favorite/list',
      method: 'GET'
    });
  },
  listMerchants(params) {
    return request({
      url: '/canteen/merchant/list',
      method: 'GET',
      params
    });
  },
  createComplaint(payload) {
    return request({
      url: '/canteen/complaint/create',
      method: 'POST',
      data: payload
    });
  },
  listMyComplaints(params) {
    return request({
      url: '/canteen/complaint/my/list',
      method: 'GET',
      params
    });
  },
  listComplaints(params) {
    return request({
      url: '/canteen/complaint/list',
      method: 'GET',
      params
    });
  },
  listAllComplaints(params) {
    return request({
      url: '/canteen/complaint/list',
      method: 'GET',
      params
    });
  },
  getComplaintDetail(id) {
    return request({
      url: `/canteen/complaint/detail/${id}`,
      method: 'GET'
    });
  },
  processComplaint(payload) {
    return request({
      url: '/canteen/complaint/process',
      method: 'POST',
      data: payload
    });
  },
  evaluateComplaint(payload) {
    return request({
      url: '/canteen/complaint/evaluate',
      method: 'POST',
      data: payload
    });
  },
  submitRectify(payload) {
    return request({
      url: '/canteen/complaint/rectify/submit',
      method: 'POST',
      data: payload
    });
  },
  listDynamics(params) {
    return request({
      url: '/canteen/dynamic/list',
      method: 'GET',
      params
    });
  },
  listAnnouncements(params) {
    return request({
      url: '/canteen/announcement/list',
      method: 'GET',
      params
    });
  },
  getComplaintRanking(limit = 5) {
    return request({
      url: '/canteen/complaint/ranking',
      method: 'GET',
      params: { limit }
    });
  },
  shareDynamic(dynamicId, channel = 'wechat') {
    return request({
      url: `/canteen/dynamic/share/${dynamicId}`,
      method: 'POST',
      data: { shareChannel: channel }
    });
  },
  getMyMerchantProfile() {
    return request({
      url: '/canteen/merchant/my',
      method: 'GET'
    });
  },
  updateMerchantProfile(payload) {
    return request({
      url: '/canteen/merchant/my/update',
      method: 'POST',
      data: payload
    });
  },
  listMyDishes(params) {
    return request({
      url: '/canteen/dish/my/list',
      method: 'GET',
      params
    });
  },
  getDishDetail(dishId) {
    return request({
      url: `/canteen/dish/detail/${dishId}`,
      method: 'GET'
    });
  },
  createDish(payload) {
    return request({
      url: '/canteen/dish/save',
      method: 'POST',
      data: payload
    });
  },
  updateDish(payload) {
    return request({
      url: '/canteen/dish/update',
      method: 'POST',
      data: payload
    });
  },
  deleteDish(dishId) {
    return request({
      url: `/canteen/dish/delete/${dishId}`,
      method: 'POST'
    });
  },
  setSpecialDish(payload) {
    return request({
      url: '/canteen/dish/special/set',
      method: 'POST',
      data: payload
    });
  },
  cancelSpecialDish(dishId) {
    return request({
      url: `/canteen/dish/special/cancel/${dishId}`,
      method: 'POST'
    });
  },
  publishDynamic(payload) {
    return request({
      url: '/canteen/dynamic/save',
      method: 'POST',
      data: payload
    });
  },
  listMyDynamics(params) {
    return request({
      url: '/canteen/dynamic/my/list',
      method: 'GET',
      params
    });
  },
  listAllMerchants(params) {
    return request({
      url: '/canteen/merchant/list',
      method: 'GET',
      params
    });
  },
  sendViolationNotice(payload) {
    return request({
      url: `/canteen/merchant/violation/${payload.merchantId}`,
      method: 'POST',
      data: { content: payload.content }
    });
  },
  uploadFile(filePath, type = 'dynamic') {
    return new Promise((resolve, reject) => {
      const token = getToken();
      const bizMap = {
        avatar: 'user_avatar',
        dynamic: 'dynamic_image',
        dish: 'dish_image',
        complaint: 'complaint_evidence'
      };
      uni.uploadFile({
        url: `${BASE_URL}/file/upload`,
        filePath,
        name: 'file',
        formData: { biz: bizMap[type] || 'user_avatar' },
        header: {
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        success: (res) => {
          try {
            const data = typeof res.data === 'string' ? JSON.parse(res.data || '{}') : (res.data || {});
            if (res.statusCode >= 200 && res.statusCode < 300 && data.code === 0) {
              const url = typeof data.data === 'string' ? data.data : (data.data?.url || '');
              resolve({ url, raw: data.data });
              return;
            }
            reject(new Error(data.message || '上传失败'));
          } catch (error) {
            reject(new Error('上传响应解析失败'));
          }
        },
        fail: (error) => reject(error)
      });
    });
  }
};

const fileApi = {
  upload(filePath, biz = 'user_avatar', name = 'file') {
    return new Promise((resolve, reject) => {
      const token = getToken();
      uni.uploadFile({
        url: `${BASE_URL}/file/upload`,
        filePath,
        name,
        formData: { biz },
        header: {
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        success: (res) => {
          try {
            const data = typeof res.data === 'string' ? JSON.parse(res.data || '{}') : (res.data || {});
            if (res.statusCode >= 200 && res.statusCode < 300 && data.code === 0) {
              resolve(data.data);
              return;
            }
            reject(new Error(data.message || '上传失败'));
          } catch (error) {
            reject(new Error('上传响应解析失败'));
          }
        },
        fail: (error) => reject(error)
      });
    });
  }
};

export {
  userApi,
  notificationApi,
  contentApi,
  canteenApi,
  fileApi,
  setToken
};
