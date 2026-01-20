package com.example.Spot.global.feign.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponse {

    private UUID id;
    private UUID storeId;
    private String name;
    private String description;
    private Integer price;
    private String imageUrl;
    private boolean isHidden;
    private boolean isDeleted;
}
