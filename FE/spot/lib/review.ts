import { api } from './api';

export interface Review {
  id: string;
  storeId: string;
  storeName: string;
  userId: number;
  userNickname: string;
  rating: number;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface ReviewStats {
  averageRating: number;
  totalReviews: number;
}

export interface ReviewCreateRequest {
  storeId: string;
  rating: number;
  content?: string;
}

export interface ReviewUpdateRequest {
  rating?: number;
  content?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const reviewApi = {
  // 리뷰 작성
  createReview: async (data: ReviewCreateRequest): Promise<Review> => {
    const response = await api.post<{ result: Review }>('/api/reviews', data);
    return response.data.result;
  },

  // 특정 가게의 리뷰 목록 조회
  getStoreReviews: async (
    storeId: string,
    page: number = 0,
    size: number = 10
  ): Promise<PageResponse<Review>> => {
    const response = await api.get<{ result: PageResponse<Review> }>(
      `/api/reviews/stores/${storeId}`,
      {
        params: { page, size },
      }
    );
    return response.data.result;
  },

  // 가게 리뷰 통계 조회
  getStoreReviewStats: async (storeId: string): Promise<ReviewStats> => {
    const response = await api.get<{ result: ReviewStats }>(
      `/api/reviews/stores/${storeId}/stats`
    );
    return response.data.result;
  },

  // 리뷰 수정
  updateReview: async (
    reviewId: string,
    data: ReviewUpdateRequest
  ): Promise<Review> => {
    const response = await api.patch<{ result: Review }>(
      `/api/reviews/${reviewId}`,
      data
    );
    return response.data.result;
  },

  // 리뷰 삭제
  deleteReview: async (reviewId: string): Promise<void> => {
    await api.delete(`/api/reviews/${reviewId}`);
  },
};

export default reviewApi;
