package com.example.Spot.store.presentation.dto.request;

import java.time.LocalTime;
import java.util.List;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record StoreCreateRequest (
        @NotBlank String name,
        @NotBlank String roadAddress,
        String addressDetail,
        @NotBlank String phoneNumber,
        @NotNull LocalTime openTime,
        @NotNull LocalTime closeTime,
        @NotEmpty List<String> categoryNames,
        @NotNull Integer ownerId,
        @NotNull Integer chefId
) {
    public StoreEntity toEntity(List<CategoryEntity> categories) {
        StoreEntity store = StoreEntity.builder()
                .name(name)
                .roadAddress(roadAddress)
                .addressDetail(addressDetail)
                .phoneNumber(phoneNumber)
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
        
        if (categories != null) {
            categories.forEach(store::addCategory);
        }
        
        return store;
    }
}
