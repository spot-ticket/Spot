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
public class MenuOptionResponse {

    private UUID id;
    private UUID menuId;
    private String name;
    private String detail;
    private Integer price;
    private boolean isDeleted;
}
