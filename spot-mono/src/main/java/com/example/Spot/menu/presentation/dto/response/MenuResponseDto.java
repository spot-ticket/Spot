package com.example.Spot.menu.presentation.dto.response;

import java.util.UUID;

public interface MenuResponseDto {
    UUID id();         // getId() -> id()
    UUID storeId();    // getStoreId() -> storeId()
    String name();
    String category();
    Integer price();
    String description();
    String imageUrl();
    Boolean isAvailable();
}
