package com.example.Spot.store.presentation.dto.request;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Builder
public class StoreUpdateRequest {

    private String name;
    private String address;
    private String detailAddress;
    private String phoneNumber;
    private LocalTime openTime;
    private LocalTime closeTime;

    private List<UUID> categoryIds;
    
    private List<Integer> staffUserIds;
}

