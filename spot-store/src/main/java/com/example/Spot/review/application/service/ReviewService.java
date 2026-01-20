package com.example.Spot.review.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.review.domain.entity.ReviewEntity;
import com.example.Spot.review.domain.repository.ReviewRepository;
import com.example.Spot.review.presentation.dto.request.ReviewCreateRequest;
import com.example.Spot.review.presentation.dto.request.ReviewUpdateRequest;
import com.example.Spot.review.presentation.dto.response.ReviewResponse;
import com.example.Spot.review.presentation.dto.response.ReviewStatsResponse;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, Integer userId) {
        // 가게 존재 확인
        StoreEntity store = storeRepository.findByIdAndIsDeletedFalse(request.storeId())
                .orElseThrow(() -> new EntityNotFoundException("가게를 찾을 수 없습니다."));

        // 사용자 존재 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 리뷰 생성
        ReviewEntity review = ReviewEntity.builder()
                .store(store)
                .user(user)
                .rating(request.rating())
                .content(request.content())
                .createdBy(userId)
                .build();

        review.validateRating();
        ReviewEntity savedReview = reviewRepository.save(review);

        return ReviewResponse.fromEntity(savedReview);
    }

    public Page<ReviewResponse> getStoreReviews(UUID storeId, Pageable pageable) {
        Page<ReviewEntity> reviews = reviewRepository.findByStoreIdAndIsDeletedFalse(storeId, pageable);
        return reviews.map(ReviewResponse::fromEntity);
    }

    public ReviewStatsResponse getStoreReviewStats(UUID storeId) {
        Double averageRating = reviewRepository.getAverageRatingByStoreId(storeId);
        Long totalReviews = reviewRepository.countByStoreIdAndIsDeletedFalse(storeId);

        return new ReviewStatsResponse(
                averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0,
                totalReviews
        );
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, ReviewUpdateRequest request, Integer userId) {
        ReviewEntity review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 작성자 본인만 수정 가능
        if (!review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        review.updateReview(request.rating(), request.content(), userId);
        review.validateRating();

        return ReviewResponse.fromEntity(review);
    }

    @Transactional
    public void deleteReview(UUID reviewId, Integer userId, boolean isAdmin) {
        ReviewEntity review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        // 작성자 본인 또는 관리자만 삭제 가능
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        review.softDelete(userId);
    }
}
