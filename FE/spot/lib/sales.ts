import {api} from './api';

export interface SalesSummary {
  totalRevenue: number;
  totalOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  averageOrderAmount: number;
  periodStart: string;
  periodEnd: string;
}

export interface DailySales {
  date: string;
  revenue: number;
  orderCount: number;
}

export interface PopularMenu {
  menuId: string;
  menuName: string;
  orderCount: number;
  totalRevenue: number;
}

export const salesApi = {
  // 매출 요약 조회
  getSalesSummary: async (
    storeId: string,
    startDate?: string,
    endDate?: string
  ): Promise<SalesSummary> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const response = await api.get<{ result: SalesSummary }>(
      `/api/stores/${storeId}/sales/summary?${params.toString()}`
    );
    return response.data.result;
  },

  // 일별 매출 조회
  getDailySales: async (
    storeId: string,
    startDate?: string,
    endDate?: string
  ): Promise<DailySales[]> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const response = await api.get<{ result: DailySales[] }>(
      `/api/stores/${storeId}/sales/daily?${params.toString()}`
    );
    return response.data.result;
  },

  // 인기 메뉴 조회
  getPopularMenus: async (
    storeId: string,
    startDate?: string,
    endDate?: string,
    limit: number = 10
  ): Promise<PopularMenu[]> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    params.append('limit', limit.toString());

    const response = await api.get<{ result: PopularMenu[] }>(
      `/api/stores/${storeId}/sales/popular-menus?${params.toString()}`
    );
    return response.data.result;
  },
};

export default salesApi;
