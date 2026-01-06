package com.example.Spot.store.presentation.dto.request;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

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

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime openTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime closeTime;
    private List<String> categoryNames;
}

