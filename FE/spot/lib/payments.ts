import api from './api';
import type { ApiResponse, PaymentConfirmRequest, PaymentConfirmResponse } from '@/types';

export const paymentApi = {
  // 결제 승인
  confirmPayment: async (
    orderId: string,
    data: PaymentConfirmRequest
  ): Promise<PaymentConfirmResponse> => {
    const response = await api.post<ApiResponse<PaymentConfirmResponse>>(
      `/api/payments/${orderId}/confirm`,
      data
    );
    return response.data.result;
  },

  // 결제 취소
  cancelPayment: async (
    orderId: string,
    paymentId: string,
    cancelReason: string
  ): Promise<void> => {
    await api.post(`/api/payments/${orderId}/cancel`, {
      paymentId,
      cancelReason,
    });
  },

  // 결제 상세 조회
  getPaymentDetail: async (paymentId: string) => {
    const response = await api.get(`/api/payments/${paymentId}`);
    return response.data.result;
  },
};

export default paymentApi;
