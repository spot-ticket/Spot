package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.user.domain.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuAdminResponseDto(
        @JsonProperty("menu_id")
        UUID id,

        @JsonProperty("store_id")
        UUID storeId,

        String name,
        String category,
        Integer price,
        String description,

        @JsonProperty("image_url")
        String imageUrl,

        List<MenuOptionAdminResponseDto> options,

        @JsonProperty("is_available")
        Boolean isAvailable,

        @JsonProperty("is_deleted")
        Boolean isDeleted,

        @JsonProperty("is_hidden")
        Boolean isHidden,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("created_by")
        Integer createdBy,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt,

        @JsonProperty("updated_by")
        Integer updatedBy,

        @JsonProperty("deleted_at")
        LocalDateTime deletedAt,

        @JsonProperty("deleted_by")
        Integer deletedBy

) implements MenuResponseDto {

    // 정적 팩토리 메서드
    public static MenuAdminResponseDto of(MenuEntity menu, List<MenuOptionEntity> options, Role userRole) {

        // 받아온 options 리스트를 그대로 DTO로 변환
        List<MenuOptionAdminResponseDto> optionDtos = (options != null)
                ? options.stream()
                .map(option -> MenuOptionAdminResponseDto.of(option, userRole))
                .toList()
                : Collections.emptyList();

        // 2. 권한 체크 로직 (여기로 이동)
        LocalDateTime createdAt = null;
        Integer createdBy = null;

        LocalDateTime updatedAt = null;
        Integer updatedBy = null;

        LocalDateTime deletedAt = null;
        Integer deletedBy = null;

        if (userRole == Role.MASTER || userRole == Role.MANAGER) {
            createdBy = menu.getCreatedBy();
            createdAt = menu.getCreatedAt(); // 추가됨

            updatedBy = menu.getUpdatedBy();
            updatedAt = menu.getUpdatedAt(); // 추가됨

            deletedBy = menu.getDeletedBy();
            deletedAt = menu.getDeletedAt();
        }

        return new MenuAdminResponseDto(
                menu.getId(),
                menu.getStore().getId(),
                menu.getName(),
                menu.getCategory(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                optionDtos,
                menu.getIsAvailable(),
                menu.getIsDeleted(),
                menu.getIsHidden(),

                createdAt,
                createdBy,

                updatedAt,
                updatedBy,

                deletedAt,
                deletedBy
        );
    }
}
