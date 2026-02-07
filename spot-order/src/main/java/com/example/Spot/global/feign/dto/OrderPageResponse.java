package com.example.Spot.global.feign.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPageResponse {

    private List<OrderResponse> content;
    private int totalPages;
    private long totalElements;
    private int size;
    private int number;
    private boolean first;
    private boolean last;

    public static OrderPageResponse from(Page<OrderResponse> pageData) {
        return OrderPageResponse.builder()
                .content(pageData.getContent())
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .size(pageData.getSize())
                .number(pageData.getNumber())
                .first(pageData.isFirst())
                .last(pageData.isLast())
                .build();
    }
}
