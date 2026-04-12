import request from '../request';
import { BaseResponse, PageData, PageParams } from '../types';

// 用户登录请求
export interface UserLoginRequest {
  loginType: 'phone' | 'account';
  userPhone?: string;
  userAccount?: string;
  userPassword: string;
}

// 用户注册请求
export interface UserRegisterRequest {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
  userPhone: string;
  userName?: string;
  userRole?: string;
  userEmail?: string;
}

// 用户信息
export interface User {
  id: number;
  userAccount: string;
  userName?: string;
  userAvatar?: string;
  userPhone?: string;
  userEmail?: string;
  userRole: 'user' | 'student' | 'merchant' | 'supervisor' | 'admin';
  userProfile?: string;
  qualificationStatus?: string;
  workYears?: number;
  specialties?: string;
  serviceArea?: string;
  createTime: string;
  updateTime: string;
}

// 登录用户信息
export interface LoginUserVO extends User {
  token?: string;
}

// 创建用户请求
export interface UserAddRequest {
  userAccount: string;
  userName?: string;
  userAvatar?: string;
  userPhone?: string;
  userEmail?: string;
  userRole?: string;
  status?: number;
  userProfile?: string;
}

// 更新用户请求
export interface UserUpdateRequest {
  id: number;
  userName?: string;
  userAvatar?: string;
  userPhone?: string;
  userEmail?: string;
  userRole?: string;
  status?: number;
  userProfile?: string;
}

// 更新当前用户信息请求
export interface UserUpdateMyRequest {
  userName?: string;
  userAvatar?: string;
  userProfile?: string;
  userPhone?: string;
  userEmail?: string;
  gender?: string;
  age?: number;
  workYears?: number;
  specialties?: string;
  serviceArea?: string;
}

// 用户查询请求
export interface UserQueryRequest extends PageParams {
  id?: number;
  keyword?: string;
  userAccount?: string;
  userPhone?: string;
  userName?: string;
  userRole?: string;
  qualificationStatus?: string;
}

// 批量删除请求
export interface UserBatchDeleteRequest {
  ids: number[];
  softDelete?: boolean;
}

// 批量更新请求
export interface UserBatchUpdateRequest {
  ids: number[];
  userName?: string;
  userRole?: string;
  status?: number;
  userProfile?: string;
}

// 删除请求
export interface DeleteRequest {
  id: number;
}

export interface UserAdminPasswordResetRequest {
  id: number;
}

// 用户统计信息
export interface UserStatisticsVO {
  totalUsers: number;
  activeUsers: number;
  newUsers?: number;
  adminCount?: number;
  studentCount?: number;
  merchantCount?: number;
  supervisorCount?: number;
  enabledCount?: number;
  disabledCount?: number;
  todayLoginCount?: number;
  weekLoginCount?: number;
  monthLoginCount?: number;
}

/**
 * 用户登录
 */
export function userLogin(data: UserLoginRequest): Promise<BaseResponse<LoginUserVO>> {
  return request.post('/user/login', data) as Promise<BaseResponse<LoginUserVO>>;
}

/**
 * 用户注册
 */
export function userRegister(data: UserRegisterRequest): Promise<BaseResponse<number>> {
  return request.post('/user/register', data) as Promise<BaseResponse<number>>;
}

/**
 * 用户注销
 */
export function userLogout(): Promise<BaseResponse<boolean>> {
  return request.post('/user/logout') as Promise<BaseResponse<boolean>>;
}

/**
 * 获取当前登录用户
 */
export function getLoginUser(): Promise<BaseResponse<LoginUserVO>> {
  return request.get('/user/get/login') as Promise<BaseResponse<LoginUserVO>>;
}

/**
 * 创建用户（管理员）
 */
export function addUser(data: UserAddRequest): Promise<BaseResponse<number>> {
  return request.post('/user/add', data) as Promise<BaseResponse<number>>;
}

/**
 * 删除用户（管理员）
 */
export function deleteUser(data: DeleteRequest): Promise<BaseResponse<boolean>> {
  return request.post('/user/delete', data) as Promise<BaseResponse<boolean>>;
}

/**
 * 更新用户（管理员）
 */
export function updateUser(data: UserUpdateRequest): Promise<BaseResponse<boolean>> {
  return request.post('/user/update', data) as Promise<BaseResponse<boolean>>;
}

/**
 * 管理员重置用户密码
 */
export function adminResetUserPassword(data: UserAdminPasswordResetRequest): Promise<BaseResponse<boolean>> {
  return request.post('/user/password/reset/admin', data) as Promise<BaseResponse<boolean>>;
}

/**
 * 根据 ID 获取用户（管理员）
 */
export function getUserById(id: number): Promise<BaseResponse<User>> {
  return request.get('/user/get', { params: { id } }) as Promise<BaseResponse<User>>;
}

/**
 * 根据 ID 获取用户包装类
 */
export function getUserVOById(id: number): Promise<BaseResponse<User>> {
  return request.get('/user/get/vo', { params: { id } }) as Promise<BaseResponse<User>>;
}

/**
 * 分页获取用户列表（管理员）
 */
export function listUserByPage(data: UserQueryRequest): Promise<BaseResponse<PageData<User>>> {
  return request.post('/user/list/page', data) as Promise<BaseResponse<PageData<User>>>;
}

/**
 * 分页获取用户封装列表
 */
export function listUserVOByPage(data: UserQueryRequest): Promise<BaseResponse<PageData<User>>> {
  return request.post('/user/list/page/vo', data) as Promise<BaseResponse<PageData<User>>>;
}

/**
 * 更新个人信息
 */
export function updateMyUser(data: UserUpdateMyRequest): Promise<BaseResponse<boolean>> {
  return request.post('/user/update/my', data) as Promise<BaseResponse<boolean>>;
}

/**
 * 批量删除用户
 */
export function batchDeleteUser(data: UserBatchDeleteRequest): Promise<BaseResponse<number>> {
  return request.post('/user/batch-delete', data) as Promise<BaseResponse<number>>;
}

/**
 * 批量更新用户
 */
export function batchUpdateUser(data: UserBatchUpdateRequest): Promise<BaseResponse<number>> {
  return request.post('/user/batch-update', data) as Promise<BaseResponse<number>>;
}

/**
 * 获取用户统计信息
 */
export function getUserStatistics(userRole?: string): Promise<BaseResponse<UserStatisticsVO>> {
  return request.get('/user/statistics', { params: { userRole } }) as Promise<BaseResponse<UserStatisticsVO>>;
}

