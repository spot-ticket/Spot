package com.example.Spot.menu.presentation.dto.response;

import java.util.UUID;

public interface MenuResponseDto {
    UUID getId();
    UUID getStoreId();
    String getName();
    String getCategory();
    Integer getPrice();
    String getDescription();
    String getImageUrl();
    Boolean getIsAvailable();
}
