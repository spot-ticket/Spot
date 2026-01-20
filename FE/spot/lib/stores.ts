import api from './api';
import type {Category, Store} from '@/types';

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

    console.log("==");
    console.log(response);
    return response.data;
  },

  // 가게 상세 조회
  getStore: async (storeId: string): Promise<Store> => {
    const response = await api.get<Store>(`/api/stores/${storeId}`);
    return response.data;
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
    const response = await api.get<Category[]>('/api/categories');
    return response.data;
  },

  // 카테고리별 가게 조회
  getStoresByCategory: async (categoryName: string): Promise<PageResponse<Store>> => {
    const response = await api.get<Store[]>(
      `/api/categories/${categoryName}/stores`
    );
    // 백엔드가 List로 반환하므로 PageResponse 형태로 변환
    const stores = response.data;
    return {
      content: stores,
      totalPages: 1,
      totalElements: stores.length,
      size: stores.length,
      number: 0,
    };
  },
};

export default storeApi;
