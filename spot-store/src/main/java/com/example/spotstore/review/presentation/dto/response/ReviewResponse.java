package com.example.spotstore.review.presentation.dto.response;

import com.example.Spot.review.domain.entity.ReviewEntity;

import java.time.LocalDateTime;
import java.util.UUID;

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
