import api from './api';
import type { ApiResponse, OrderCreateRequest, OrderResponse } from '@/types';

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export const orderApi = {
  // 주문 생성
  createOrder: async (data: OrderCreateRequest): Promise<OrderResponse> => {
    const response = await api.post<ApiResponse<OrderResponse>>('/api/orders', data);
    return response.data.result;
  },

  // 내 주문 목록 조회
  getMyOrders: async (page = 0, size = 20): Promise<PageResponse<OrderResponse>> => {
    const response = await api.get<ApiResponse<PageResponse<OrderResponse>>>('/api/orders/my', {
      params: { page, size },
    });
    return response.data.result;
  },

  // 진행 중인 주문 조회
  getActiveOrders: async (): Promise<OrderResponse[]> => {
    const response = await api.get<ApiResponse<OrderResponse[]>>('/api/orders/my/active');
    return response.data.result;
  },

  // 주문 취소
  cancelOrder: async (orderId: string, reason: string): Promise<OrderResponse> => {
    const response = await api.patch<ApiResponse<OrderResponse>>(
      `/api/orders/${orderId}/customer-cancel`,
      { reason }
    );
    return response.data.result;
  },
};

export default orderApi;
