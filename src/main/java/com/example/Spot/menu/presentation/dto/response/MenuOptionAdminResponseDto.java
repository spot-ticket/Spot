package com.example.Spot.menu.presentation.dto.response;

import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuOptionAdminResponseDto {

    @JsonProperty("option_id")
    private UUID id;

    @JsonProperty("menu_id")
    private UUID menuId;

    private String name;
    private String detail;
    private Integer price;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("is_hidden")
    private Boolean isHidden;

    @JsonProperty("is_deleted")
    private Boolean isDeleted;

    @JsonProperty("created_by")
    private Integer createdBy;

    @JsonProperty("updated_by")
    private Integer updatedBy;

    @JsonProperty("deleted_by")
    private Integer deletedBy;

    public MenuOptionAdminResponseDto(MenuOptionEntity menuOption) {
        this.id = menuOption.getId();
        this.menuId = menuOption.getMenu().getId();
        this.name = menuOption.getName();
        this.detail = menuOption.getDetail();
        this.price = menuOption.getPrice();
        this.isAvailable = menuOption.getIsAvailable();
        this.isHidden = menuOption.getIsHidden();
        this.isDeleted = menuOption.getIsDeleted();
        this.createdBy = menuOption.getCreatedBy();
        this.updatedBy = menuOption.getUpdatedBy();
        this.deletedBy = menuOption.getDeletedBy();
    }
}
