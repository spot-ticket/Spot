package com.example.spotstore.review.presentation.dto.response;

public record ReviewStatsResponse(
        Double averageRating,
        Long totalReviews
) {
}
