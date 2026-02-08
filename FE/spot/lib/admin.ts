import {api} from './api';
import type {ApiResponse, OrderResponse, PageResponse, Store, User} from '@/types';

// 관리자용 통계 타입
export interface AdminStats {
  totalUsers: number;
  totalOrders: number;
  totalStores: number;
  totalRevenue: number;
  recentOrders: OrderResponse[];
  userGrowth: Array<{ date: string; count: number }>;
  orderStats: Array<{ status: string; count: number }>;
}

// 사용자 관리 API
export const adminUserApi = {
  // 전체 사용자 조회 (페이징)
  getAllUsers: async (page = 0, size = 20): Promise<PageResponse<User>> => {
    const response = await api.get<ApiResponse<PageResponse<User>>>(
      `/api/admin/users?page=${page}&size=${size}`
    );
    return response.data.result;
  },

  // 특정 사용자 조회
  getUser: async (userId: number): Promise<User> => {
    const response = await api.get<ApiResponse<User>>(`/api/users/${userId}`);
    return response.data.result;
  },

  // 사용자 역할 변경
  updateUserRole: async (userId: number, role: string): Promise<void> => {
    await api.patch(`/api/admin/users/${userId}/role`, { role });
  },

  // 사용자 삭제
  deleteUser: async (userId: number): Promise<void> => {
    await api.delete(`/api/admin/users/${userId}`);
  },
};

// 주문 관리 API
export const adminOrderApi = {
  // 전체 주문 조회 (페이징)
  getAllOrders: async (page = 0, size = 20): Promise<PageResponse<OrderResponse>> => {
    const response = await api.get<ApiResponse<PageResponse<OrderResponse>>>(
      `/api/admin/orders?page=${page}&size=${size}`
    );
    return response.data.result;
  },

  // 특정 주문 상세 조회
  getOrder: async (orderId: string): Promise<OrderResponse> => {
    const response = await api.get<ApiResponse<OrderResponse>>(`/api/orders/${orderId}`);
    return response.data.result;
  },

  // 주문 상태 변경
  updateOrderStatus: async (orderId: string, status: string): Promise<void> => {
    await api.patch(`/api/orders/${orderId}/status`, { status });
  },

  // 주문 취소
  cancelOrder: async (orderId: string): Promise<void> => {
    await api.delete(`/api/orders/${orderId}`);
  },
};

// 가게 관리 API
export const adminStoreApi = {
  // 전체 가게 조회 (페이징)
  getAllStores: async (page = 0, size = 20): Promise<PageResponse<Store>> => {
    const response = await api.get<ApiResponse<PageResponse<Store>>>(
      `/api/admin/stores?page=${page}&size=${size}`
    );
    return response.data.result;
  },

  // 특정 가게 조회
  getStore: async (storeId: number): Promise<Store> => {
    const response = await api.get<ApiResponse<Store>>(`/api/stores/${storeId}`);
    return response.data.result;
  },

  // 가게 상태 변경
  updateStoreStatus: async (storeId: string, status: 'PENDING' | 'APPROVED' | 'REJECTED'): Promise<void> => {
    await api.patch(`/api/stores/${storeId}/status?status=${status}`);
  },

  // 가게 승인
  approveStore: async (storeId: string): Promise<void> => {
    await api.patch(`/api/stores/${storeId}/status?status=APPROVED`);
  },

  // 가게 거절
  rejectStore: async (storeId: string): Promise<void> => {
    await api.patch(`/api/stores/${storeId}/status?status=REJECTED`);
  },

  // 가게 정보 수정
  updateStore: async (storeId: number, storeData: Partial<Store>): Promise<void> => {
    await api.put(`/api/stores/${storeId}`, storeData);
  },

  // 가게 삭제
  deleteStore: async (storeId: string): Promise<void> => {
    await api.delete(`/api/admin/stores/${storeId}`);
  },
};

// 통계 및 대시보드 API
export const adminStatsApi = {
  // 전체 통계 조회
  getStats: async (): Promise<AdminStats> => {
    const response = await api.get<ApiResponse<AdminStats>>('/api/admin/stats/dashboard');
    return response.data.result;
  },

  // 매출 통계 조회
  getRevenueStats: async (startDate: string, endDate: string): Promise<any> => {
    const response = await api.get<ApiResponse<any>>(
      `/api/admin/stats/revenue?startDate=${startDate}&endDate=${endDate}`
    );
    return response.data.result;
  },

  // 사용자 증가 통계
  getUserGrowthStats: async (period: 'daily' | 'weekly' | 'monthly'): Promise<any> => {
    const response = await api.get<ApiResponse<any>>(
      `/api/admin/stats/user-growth?period=${period}`
    );
    return response.data.result;
  },
};
