'use client';

import {useEffect, useState} from 'react';
import {type Review, reviewApi, type ReviewStats} from '@/lib/review';
import {useAuth} from '@/store/authStore';
import Button from '@/components/ui/Button';

interface ReviewListProps {
  storeId: string;
}

export function ReviewList({ storeId }: ReviewListProps) {
  const { user } = useAuth();
  const [reviews, setReviews] = useState<Review[]>([]);
  const [stats, setStats] = useState<ReviewStats | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [editingReviewId, setEditingReviewId] = useState<string | null>(null);
  const [editRating, setEditRating] = useState(0);
  const [editContent, setEditContent] = useState('');

  useEffect(() => {
    loadReviews();
    loadStats();
  }, [storeId, page]);

  const loadReviews = async () => {
    try {
      setIsLoading(true);
      const data = await reviewApi.getStoreReviews(storeId, page, 10);
      setReviews(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('리뷰 목록 로드 실패:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const loadStats = async () => {
    try {
      const data = await reviewApi.getStoreReviewStats(storeId);
      setStats(data);
    } catch (error) {
      console.error('리뷰 통계 로드 실패:', error);
    }
  };

  const handleEdit = (review: Review) => {
    setEditingReviewId(review.id);
    setEditRating(review.rating);
    setEditContent(review.content || '');
  };

  const handleCancelEdit = () => {
    setEditingReviewId(null);
    setEditRating(0);
    setEditContent('');
  };

  const handleSaveEdit = async (reviewId: string) => {
    try {
      await reviewApi.updateReview(reviewId, {
        rating: editRating,
        content: editContent,
      });
      alert('리뷰가 수정되었습니다.');
      setEditingReviewId(null);
      loadReviews();
      loadStats();
    } catch (error) {
      console.error('리뷰 수정 실패:', error);
      alert('리뷰 수정에 실패했습니다.');
    }
  };

  const handleDelete = async (reviewId: string) => {
    if (!confirm('리뷰를 삭제하시겠습니까?')) return;

    try {
      await reviewApi.deleteReview(reviewId);
      alert('리뷰가 삭제되었습니다.');
      loadReviews();
      loadStats();
    } catch (error) {
      console.error('리뷰 삭제 실패:', error);
      alert('리뷰 삭제에 실패했습니다.');
    }
  };

  const renderStars = (rating: number, interactive: boolean = false, onRatingChange?: (rating: number) => void) => {
    return (
      <div className="flex gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            onClick={() => interactive && onRatingChange?.(star)}
            disabled={!interactive}
            className={`text-2xl ${
              star <= rating ? 'text-yellow-400' : 'text-gray-300'
            } ${interactive ? 'cursor-pointer hover:scale-110 transition-transform' : 'cursor-default'}`}
          >
            ★
          </button>
        ))}
      </div>
    );
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* 리뷰 통계 */}
      {stats && (
        <div className="bg-gray-50 p-6 rounded-lg">
          <div className="flex items-center gap-4">
            <div>
              <div className="text-4xl font-bold text-blue-600">
                {stats.averageRating.toFixed(1)}
              </div>
              <div className="text-sm text-gray-600 mt-1">평균 별점</div>
            </div>
            <div className="flex-1">
              {renderStars(Math.round(stats.averageRating))}
              <div className="text-sm text-gray-600 mt-2">
                전체 {stats.totalReviews}개의 리뷰
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 리뷰 목록 */}
      <div className="space-y-4">
        {reviews.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            아직 작성된 리뷰가 없습니다.
          </div>
        ) : (
          reviews.map((review) => (
            <div
              key={review.id}
              className="bg-white border border-gray-200 rounded-lg p-6"
            >
              {editingReviewId === review.id ? (
                // 수정 모드
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      별점
                    </label>
                    {renderStars(editRating, true, setEditRating)}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      리뷰 내용
                    </label>
                    <textarea
                      value={editContent}
                      onChange={(e) => setEditContent(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md"
                      rows={4}
                    />
                  </div>
                  <div className="flex gap-2">
                    <Button
                      size="sm"
                      onClick={() => handleSaveEdit(review.id)}
                    >
                      저장
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={handleCancelEdit}
                    >
                      취소
                    </Button>
                  </div>
                </div>
              ) : (
                // 일반 모드
                <>
                  <div className="flex items-start justify-between mb-3">
                    <div>
                      <div className="font-medium text-gray-900">
                        {review.userNickname}
                      </div>
                      <div className="text-sm text-gray-500 mt-1">
                        {new Date(review.createdAt).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric',
                        })}
                      </div>
                    </div>
                    {user && user.id === review.userId && (
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleEdit(review)}
                        >
                          수정
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleDelete(review.id)}
                          className="text-red-600 border-red-600 hover:bg-red-50"
                        >
                          삭제
                        </Button>
                      </div>
                    )}
                  </div>
                  <div className="mb-3">{renderStars(review.rating)}</div>
                  {review.content && (
                    <p className="text-gray-700 whitespace-pre-wrap">
                      {review.content}
                    </p>
                  )}
                </>
              )}
            </div>
          ))
        )}
      </div>

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="flex justify-center gap-2 mt-6">
          <Button
            variant="outline"
            disabled={page === 0}
            onClick={() => setPage(page - 1)}
          >
            이전
          </Button>
          <span className="px-4 py-2 text-sm text-gray-700">
            {page + 1} / {totalPages}
          </span>
          <Button
            variant="outline"
            disabled={page >= totalPages - 1}
            onClick={() => setPage(page + 1)}
          >
            다음
          </Button>
        </div>
      )}
    </div>
  );
}
