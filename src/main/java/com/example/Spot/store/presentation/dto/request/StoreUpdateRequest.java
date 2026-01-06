package com.example.Spot.store.presentation.dto.request;

import java.time.LocalTime;
import java.util.List;

public record StoreUpdateRequest(
        
    String name,
    String address,
    String detailAddress,
    String phoneNumber,
    LocalTime openTime,
    LocalTime closeTime,
    List<String> categoryNames
) {}

