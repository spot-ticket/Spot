package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeleteMenuResponseDto {

    @JsonProperty("menu_id")
    private UUID menuId;

    @JsonProperty("store_id")
    private UUID storeId;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public DeleteMenuResponseDto(MenuEntity menu) {
        this.menuId = menu.getId();
        this.storeId = menu.getStore().getId();
        this.isDeleted = menu.getIsDeleted();
        this.updatedAt = menu.getUpdatedAt();
    }
}
