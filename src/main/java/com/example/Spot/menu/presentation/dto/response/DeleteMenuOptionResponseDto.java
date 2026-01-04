package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeleteMenuOptionResponseDto {

    @JsonProperty("option_id")
    private UUID optionId;   // ✅ 필수 추가

    @JsonProperty("menu_id")
    private UUID menuId;

    private String name;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public DeleteMenuOptionResponseDto(MenuOptionEntity option) {
        this.optionId = option.getId();
        this.menuId = option.getMenu().getId();
        this.name = option.getName();
        this.isDeleted = option.getIsDeleted(); // 엔티티에 필드/메서드 추가 필요!
        this.updatedAt = option.getUpdatedAt();
    }
}
