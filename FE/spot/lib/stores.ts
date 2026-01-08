import api from './api';
import type { ApiResponse, Store, Category } from '@/types';

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export const storeApi = {
  // 가게 목록 조회
  getStores: async (page = 0, size = 20): Promise<PageResponse<Store>> => {
    const response = await api.get<PageResponse<Store>>('/api/stores', {
      params: { page, size },
    });
    return response.data;
  },

  // 가게 상세 조회
  getStore: async (storeId: string): Promise<Store> => {
    const response = await api.get<ApiResponse<Store>>(`/api/stores/${storeId}`);
    return response.data.result;
  },

  // 가게 검색
  searchStores: async (keyword: string, page = 0, size = 20): Promise<PageResponse<Store>> => {
    const response = await api.get<PageResponse<Store>>('/api/stores/search', {
      params: { keyword, page, size },
    });
    return response.data;
  },

  // 카테고리 목록 조회
  getCategories: async (): Promise<Category[]> => {
    const response = await api.get<ApiResponse<Category[]>>('/api/categories');
    return response.data.result;
  },

  // 카테고리별 가게 조회
  getStoresByCategory: async (
    categoryName: string,
    page = 0,
    size = 20
  ): Promise<PageResponse<Store>> => {
    const response = await api.get<PageResponse<Store>>(
      `/api/categories/${categoryName}/stores`,
      {
        params: { page, size },
      }
    );
    return response.data;
  },
};

export default storeApi;
