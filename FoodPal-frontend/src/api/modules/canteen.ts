import request from '../request';
import type { BaseResponse, PageData, PageParams } from '../types';

export interface MerchantProfile {
  id?: number;
  userId?: number;
  merchantName: string;
  contactName?: string;
  contactPhone?: string;
  avatar?: string;
  businessHours?: string;
  location?: string;
  description?: string;
  status?: number;
  auditStatus?: string;
  createTime?: string;
  updateTime?: string;
}

export interface DishItem {
  id?: number;
  merchantId?: number;
  merchantName?: string;
  dishName: string;
  dishPrice: number;
  ingredients?: string;
  nutritionInfo?: string;
  dishImage?: string;
  category?: string;
  supplyStatus?: number;
  specialPrice?: number;
  specialStartTime?: string;
  specialEndTime?: string;
  purchaseLimit?: number;
  likeCount?: number;
  favoriteCount?: number;
  liked?: boolean;
  favorited?: boolean;
  isSpecial?: boolean;
  currentPrice?: number;
  createTime?: string;
  updateTime?: string;
}

export interface ComplaintItem {
  id?: number;
  complaintNo?: string;
  userId?: number;
  studentName?: string;
  merchantId?: number;
  merchantName?: string;
  complaintTitle: string;
  complaintContent: string;
  evidenceUrls?: string;
  status?: string;
  processProgress?: string;
  rectifyRequirement?: string;
  rectifyResult?: string;
  feedback?: string;
  resultRating?: number;
  processBy?: number;
  processByName?: string;
  processTime?: string;
  createTime?: string;
  updateTime?: string;
}

export interface DynamicItem {
  id?: number;
  publisherId?: number;
  publisherType?: string;
  merchantId?: number;
  title: string;
  content: string;
  coverImage?: string;
  mediaUrls?: string;
  newsType?: string;
  auditStatus?: string;
  status?: string;
  publishTime?: string;
  expireTime?: string;
  createTime?: string;
  updateTime?: string;
}

export interface AnnouncementItem {
  id?: number;
  title: string;
  content: string;
  coverImage?: string;
  announcementType?: string;
  status?: string;
  publishTime?: string;
  expireTime?: string;
  isTop?: number;
  sortNo?: number;
  createTime?: string;
  updateTime?: string;
}

export interface ComplaintProcessRequest {
  complaintId: number;
  status: string;
  processProgress?: string;
  rectifyRequirement?: string;
  rectifyResult?: string;
  feedback?: string;
  resultRating?: number;
}

export interface ComplaintCreateRequest {
  merchantId: number;
  complaintTitle: string;
  complaintContent: string;
  evidenceUrls?: string;
}

export function getCanteenSummary(): Promise<BaseResponse<Record<string, number>>> {
  return request.get('/canteen/dashboard/summary') as Promise<BaseResponse<Record<string, number>>>;
}

export function listMerchants(params: PageParams & {
  keyword?: string;
  status?: number;
  auditStatus?: string;
}): Promise<BaseResponse<PageData<MerchantProfile>>> {
  return request.get('/canteen/merchant/list', { params }) as Promise<BaseResponse<PageData<MerchantProfile>>>;
}

export function saveMerchant(data: MerchantProfile): Promise<BaseResponse<number>> {
  return request.post('/canteen/merchant/save', data) as Promise<BaseResponse<number>>;
}

export function updateMerchant(data: MerchantProfile): Promise<BaseResponse<boolean>> {
  return request.post('/canteen/merchant/update', data) as Promise<BaseResponse<boolean>>;
}

export function deleteMerchant(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/merchant/delete/${id}`) as Promise<BaseResponse<boolean>>;
}

export function listDishes(params: PageParams & {
  keyword?: string;
  category?: string;
  merchantId?: number;
  supplyStatus?: number;
  onlySpecial?: boolean;
}): Promise<BaseResponse<PageData<DishItem>>> {
  return request.get('/canteen/dish/list', { params }) as Promise<BaseResponse<PageData<DishItem>>>;
}

export function recommendDishes(limit = 10): Promise<BaseResponse<DishItem[]>> {
  return request.get('/canteen/dish/recommend', { params: { limit } }) as Promise<BaseResponse<DishItem[]>>;
}

export function saveDish(data: DishItem): Promise<BaseResponse<number>> {
  return request.post('/canteen/dish/save', data) as Promise<BaseResponse<number>>;
}

export function updateDish(data: DishItem): Promise<BaseResponse<boolean>> {
  return request.post('/canteen/dish/update', data) as Promise<BaseResponse<boolean>>;
}

export function deleteDish(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/dish/delete/${id}`) as Promise<BaseResponse<boolean>>;
}

export function setSpecialDish(data: {
  dishId: number;
  specialPrice: number;
  specialStartTime: string;
  specialEndTime: string;
  purchaseLimit?: number;
}): Promise<BaseResponse<boolean>> {
  return request.post('/canteen/dish/special/set', data) as Promise<BaseResponse<boolean>>;
}

export function cancelSpecialDish(dishId: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/dish/special/cancel/${dishId}`) as Promise<BaseResponse<boolean>>;
}

export function toggleDishLike(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/dish/like/toggle/${id}`) as Promise<BaseResponse<boolean>>;
}

export function toggleDishFavorite(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/dish/favorite/toggle/${id}`) as Promise<BaseResponse<boolean>>;
}

export function listMyFavoriteDishes(): Promise<BaseResponse<DishItem[]>> {
  return request.get('/canteen/dish/favorite/list') as Promise<BaseResponse<DishItem[]>>;
}

export function createComplaint(data: ComplaintCreateRequest): Promise<BaseResponse<number>> {
  return request.post('/canteen/complaint/create', data) as Promise<BaseResponse<number>>;
}

export function listMyComplaints(params: PageParams & {
  status?: string;
}): Promise<BaseResponse<PageData<ComplaintItem>>> {
  return request.get('/canteen/complaint/my/list', { params }) as Promise<BaseResponse<PageData<ComplaintItem>>>;
}

export function listComplaints(params: PageParams & {
  status?: string;
  merchantId?: number;
}): Promise<BaseResponse<PageData<ComplaintItem>>> {
  return request.get('/canteen/complaint/list', { params }) as Promise<BaseResponse<PageData<ComplaintItem>>>;
}

export function processComplaint(data: ComplaintProcessRequest): Promise<BaseResponse<boolean>> {
  return request.post('/canteen/complaint/process', data) as Promise<BaseResponse<boolean>>;
}

export function submitRectify(data: { complaintId: number; rectifyResult: string; evidenceUrls?: string }): Promise<BaseResponse<boolean>> {
  return request.post('/canteen/complaint/rectify/submit', data) as Promise<BaseResponse<boolean>>;
}

export function getComplaintRanking(limit = 10): Promise<BaseResponse<Array<{ merchantId: number; merchantName: string; complaintCount: number }>>> {
  return request.get('/canteen/complaint/ranking', { params: { limit } }) as Promise<BaseResponse<Array<{ merchantId: number; merchantName: string; complaintCount: number }>>>;
}

export function exportComplaints(params?: { status?: string; merchantId?: number }): Promise<Blob> {
  return request.get('/canteen/complaint/export', {
    params,
    responseType: 'blob'
  }) as Promise<Blob>;
}

export function listDynamics(params: PageParams & {
  newsType?: string;
  merchantId?: number;
  onlyPublished?: boolean;
}): Promise<BaseResponse<PageData<DynamicItem>>> {
  return request.get('/canteen/dynamic/list', { params }) as Promise<BaseResponse<PageData<DynamicItem>>>;
}

export function saveDynamic(data: DynamicItem): Promise<BaseResponse<number>> {
  return request.post('/canteen/dynamic/save', data) as Promise<BaseResponse<number>>;
}

export function updateDynamic(data: DynamicItem): Promise<BaseResponse<boolean>> {
  return request.post('/canteen/dynamic/update', data) as Promise<BaseResponse<boolean>>;
}

export function auditDynamic(
  id: number,
  auditStatus: 'approved' | 'rejected',
  auditReason?: string
): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/dynamic/audit/${id}`, { auditStatus, auditReason }) as Promise<BaseResponse<boolean>>;
}

export function deleteDynamic(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/dynamic/delete/${id}`) as Promise<BaseResponse<boolean>>;
}

export function withdrawDynamic(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/dynamic/withdraw/${id}`) as Promise<BaseResponse<boolean>>;
}

export function sendViolationNotice(merchantId: number, content: string): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/merchant/violation/${merchantId}`, { content }) as Promise<BaseResponse<boolean>>;
}

export function listAnnouncements(params: PageParams & {
  announcementType?: string;
  onlyPublished?: boolean;
}): Promise<BaseResponse<PageData<AnnouncementItem>>> {
  return request.get('/canteen/announcement/list', { params }) as Promise<BaseResponse<PageData<AnnouncementItem>>>;
}

export function saveAnnouncement(data: AnnouncementItem): Promise<BaseResponse<number>> {
  return request.post('/canteen/announcement/save', data) as Promise<BaseResponse<number>>;
}

export function updateAnnouncement(data: AnnouncementItem): Promise<BaseResponse<boolean>> {
  return request.post('/canteen/announcement/update', data) as Promise<BaseResponse<boolean>>;
}

export function deleteAnnouncement(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/announcement/delete/${id}`) as Promise<BaseResponse<boolean>>;
}

export function pinAnnouncement(id: number, top = true): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/announcement/pin/${id}`, null, { params: { top } }) as Promise<BaseResponse<boolean>>;
}

export function publishAnnouncement(id: number, status: 'published' | 'unpublished'): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/announcement/publish/${id}`, null, { params: { status } }) as Promise<BaseResponse<boolean>>;
}

export function withdrawAnnouncement(id: number): Promise<BaseResponse<boolean>> {
  return request.post(`/canteen/announcement/withdraw/${id}`) as Promise<BaseResponse<boolean>>;
}

// Merchant Profile APIs
export function getMerchantProfile(): Promise<BaseResponse<any>> {
  return request.get('/canteen/merchant/my') as Promise<BaseResponse<any>>;
}

export function updateMerchantProfile(data: any): Promise<BaseResponse<boolean>> {
  return request.post('/canteen/merchant/my/update', data) as Promise<BaseResponse<boolean>>;
}
