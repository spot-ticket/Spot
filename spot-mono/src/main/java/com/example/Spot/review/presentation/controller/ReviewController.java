package com.example.Spot.review.presentation.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.review.application.service.ReviewService;
import com.example.Spot.review.presentation.dto.request.ReviewCreateRequest;
import com.example.Spot.review.presentation.dto.request.ReviewUpdateRequest;
import com.example.Spot.review.presentation.dto.response.ReviewResponse;
import com.example.Spot.review.presentation.dto.response.ReviewStatsResponse;
import com.example.Spot.user.domain.Role;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ReviewResponse review = reviewService.createReview(request, principal.getUserId());

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.CREATE, review)
        );
    }

    // 특정 가게의 리뷰 목록 조회 (공개)
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getStoreReviews(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewResponse> reviews = reviewService.getStoreReviews(storeId, pageable);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, reviews)
        );
    }

    // 가게 리뷰 통계 조회 (평균 별점, 리뷰 개수)
    @GetMapping("/stores/{storeId}/stats")
    public ResponseEntity<ApiResponse<ReviewStatsResponse>> getStoreReviewStats(
            @PathVariable UUID storeId) {

        ReviewStatsResponse stats = reviewService.getStoreReviewStats(storeId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, stats)
        );
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails principal) {

        ReviewResponse review = reviewService.updateReview(reviewId, request, principal.getUserId());

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, review)
        );
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal CustomUserDetails principal) {

        boolean isAdmin = principal.getUserRole() == Role.MASTER || principal.getUserRole() == Role.MANAGER;
        reviewService.deleteReview(reviewId, principal.getUserId(), isAdmin);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null)
        );
    }
}
