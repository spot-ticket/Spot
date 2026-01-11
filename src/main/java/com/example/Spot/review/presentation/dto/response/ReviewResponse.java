package com.example.Spot.review.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.review.domain.entity.ReviewEntity;

public record ReviewResponse(
        UUID id,
        UUID storeId,
        String storeName,
        Integer userId,
        String userNickname,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse fromEntity(ReviewEntity review) {
        return new ReviewResponse(
                review.getId(),
                review.getStore().getId(),
                review.getStore().getName(),
                review.getUser().getId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
