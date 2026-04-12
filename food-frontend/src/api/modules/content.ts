import request from '../request';
import { BaseResponse, PageData, PageParams } from '../types';

export type ContentType = 'article' | 'announcement';
export type ContentStatus = 'draft' | 'published' | 'unpublished';

export interface ContentItem {
  id?: number;
  title: string;
  type: ContentType;
  content: string;
  summary?: string;
  coverImage?: string;
  tags?: string;
  priority?: number;
  status?: ContentStatus;
  viewCount?: number;
  publishTime?: string;
  createTime?: string;
  updateTime?: string;
}

export interface ContentListParams extends PageParams {
  type?: ContentType;
  status?: ContentStatus;
  keyword?: string;
}

export function createContent(data: ContentItem): Promise<BaseResponse<ContentItem>> {
  return request.post('/content/create', data) as Promise<BaseResponse<ContentItem>>;
}

export function updateContent(id: number, data: Partial<ContentItem>): Promise<BaseResponse<string>> {
  return request.post(`/content/update/${id}`, data) as Promise<BaseResponse<string>>;
}

export function deleteContent(id: number): Promise<BaseResponse<string>> {
  return request.post(`/content/delete/${id}`) as Promise<BaseResponse<string>>;
}

export function publishContent(id: number): Promise<BaseResponse<string>> {
  return request.post(`/content/publish/${id}`) as Promise<BaseResponse<string>>;
}

export function unpublishContent(id: number): Promise<BaseResponse<string>> {
  return request.post(`/content/unpublish/${id}`) as Promise<BaseResponse<string>>;
}

export function getContentById(id: number): Promise<BaseResponse<ContentItem>> {
  return request.get(`/content/${id}`) as Promise<BaseResponse<ContentItem>>;
}

export function listContentByPage(params: ContentListParams): Promise<BaseResponse<PageData<ContentItem>>> {
  return request.get('/content/list', {
    params,
  }) as Promise<BaseResponse<PageData<ContentItem>>>;
}

