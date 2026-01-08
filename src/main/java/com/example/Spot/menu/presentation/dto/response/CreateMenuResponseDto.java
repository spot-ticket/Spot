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
//public class CreateMenuResponseDto {
//
//    @JsonProperty("menu_id")
//    private UUID id;
//
//    @JsonProperty("store_id")
//    private UUID storeId;
//
//    private String name;
//
//    @JsonProperty("created_at")
//    private LocalDateTime createdAt;
//
//    public CreateMenuResponseDto(MenuEntity menu) {
//        this.id = menu.getId();
//        this.storeId = menu.getStore().getId();
//        this.name = menu.getName();
//        this.createdAt = menu.getCreatedAt();
//    }
//
//}
