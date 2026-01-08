//package com.example.Spot.menu.presentation.dto.response;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//import com.example.Spot.menu.domain.entity.MenuEntity;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Getter
//@NoArgsConstructor
//public class UpdateMenuResponseDto {
//    @JsonProperty("menu_id")
//    private UUID menuId;
//
//    @JsonProperty("store_id")
//    private UUID storeId;
//
//    private String name;
//
//    private String category;
//    private Integer price;
//    private String description;
//
//    @JsonProperty("image_url")
//    private String imageUrl;
//
//    @JsonProperty("is_available")
//    private Boolean isAvailable;
//
//    @JsonProperty("is_hidden")
//    private Boolean isHidden;
//
//    @JsonProperty("updated_at")
//    private LocalDateTime updatedAt;
//
//    @JsonProperty("created_by")
//    private Integer createdBy;
//
//    @JsonProperty("updated_by")
//    private Integer updatedBy;
//
//    @JsonProperty("deleted_by")
//    private Integer deletedBy;
//
//    public UpdateMenuResponseDto(MenuEntity menu) {
//        this.menuId = menu.getId();
//        this.storeId = menu.getStore().getId();
//        this.name = menu.getName();
//        this.category = menu.getCategory();
//        this.price = menu.getPrice();
//        this.description = menu.getDescription();
//        this.imageUrl = menu.getImageUrl();
//        this.isAvailable = menu.getIsAvailable();
//        this.isHidden = menu.getIsHidden();
//        this.updatedAt = menu.getUpdatedAt();
//        this.createdBy = menu.getCreatedBy();
//        this.updatedBy = menu.getUpdatedBy();
//        this.deletedBy = menu.getDeletedBy();
//    }
//}
