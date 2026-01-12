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
    console.log(data);
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

// OWNER용 주문 관리 API
export const ownerOrderApi = {
  // 내 가게 주문 목록 조회 (페이징)
  getMyStoreOrders: async (
    customerId?: number,
    date?: string,
    status?: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<OrderResponse>> => {
    const params: any = { page, size };
    if (customerId) params.customerId = customerId;
    if (date) params.date = date;
    if (status) params.status = status;

    const response = await api.get<ApiResponse<PageResponse<OrderResponse>>>(
      '/api/orders/my-store',
      { params }
    );
    return response.data.result;
  },

  // 내 가게 활성 주문 조회
  getMyStoreActiveOrders: async (): Promise<OrderResponse[]> => {
    const response = await api.get<ApiResponse<OrderResponse[]>>(
      '/api/orders/my-store/active'
    );
    return response.data.result;
  },

  // 주문 수락
  acceptOrder: async (
    orderId: string,
    estimatedTime: number
  ): Promise<OrderResponse> => {
    const response = await api.patch<ApiResponse<OrderResponse>>(
      `/api/orders/${orderId}/accept`,
      { estimatedTime }
    );
    return response.data.result;
  },

  // 주문 거절
  rejectOrder: async (orderId: string, reason: string): Promise<OrderResponse> => {
    const response = await api.patch<ApiResponse<OrderResponse>>(
      `/api/orders/${orderId}/reject`,
      { reason }
    );
    return response.data.result;
  },

  // 주문 취소 (가게 측)
  storeCancelOrder: async (orderId: string, reason: string): Promise<OrderResponse> => {
    const response = await api.patch<ApiResponse<OrderResponse>>(
      `/api/orders/${orderId}/store-cancel`,
      { reason }
    );
    return response.data.result;
  },

  // 주문 완료
  completeOrder: async (orderId: string): Promise<OrderResponse> => {
    const response = await api.patch<ApiResponse<OrderResponse>>(
      `/api/orders/${orderId}/complete`
    );
    return response.data.result;
  },
};

export default orderApi;
