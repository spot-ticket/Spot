package com.example.Spot.review.presentation.dto.response;

public record ReviewStatsResponse(
        Double averageRating,
        Long totalReviews
) {
}
