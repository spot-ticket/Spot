package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;

public record CreateMenuResponseDto(

        UUID id,
        String name,
        String category,
        Integer price,
        String description,
        String imageUrl,
        LocalDateTime createdAt
) {
    // Entity를 DTO로 변환하는 생성자
    public CreateMenuResponseDto(MenuEntity menu) {
        this(
                menu.getId(),
                menu.getName(),
                menu.getCategory(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.getCreatedAt()
        );
    }
}
