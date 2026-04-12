// 通用响应类型
export interface BaseResponse<T = any> {
  code: number;
  data: T;
  message: string;
}

// 分页数据
export interface PageData<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

// 分页请求参数
export interface PageParams {
  current?: number;
  size?: number;
}
