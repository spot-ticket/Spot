import api from './api';
import type { ApiResponse, Menu } from '@/types';

export const menuApi = {
  // 가게의 메뉴 목록 조회
  getMenus: async (storeId: string): Promise<Menu[]> => {
    const response = await api.get<ApiResponse<Menu[]>>(`/api/stores/${storeId}/menus`);
    return response.data.result;
  },

  // 메뉴 상세 조회
  getMenu: async (storeId: string, menuId: string): Promise<Menu> => {
    const response = await api.get<ApiResponse<Menu>>(
      `/api/stores/${storeId}/menus/${menuId}`
    );
    return response.data.result;
  },
};

export default menuApi;
