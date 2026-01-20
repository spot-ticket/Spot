import {api} from './api';

export interface SaveBillingKeyRequest {
  userId: number;
  customerKey: string;
  authKey: string;
}

export interface SaveBillingKeyResponse {
  userId: number;
  customerKey: string;
  billingKey: string;
  savedAt: string;
}

export const billingApi = {
  // 빌링키 저장
  saveBillingKey: async (data: SaveBillingKeyRequest): Promise<SaveBillingKeyResponse> => {
    const response = await api.post<SaveBillingKeyResponse>('/api/payments/billing-key', data);
    return response.data;
  },
};
