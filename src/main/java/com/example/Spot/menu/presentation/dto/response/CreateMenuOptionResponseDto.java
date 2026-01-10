//package com.example.Spot.menu.presentation.dto.response;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//import com.example.Spot.menu.domain.entity.MenuOptionEntity;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Getter
//@NoArgsConstructor
//public class CreateMenuOptionResponseDto {
//
//    @JsonProperty("option_id")
//    private UUID optionId;
//
//    @JsonProperty("menu_id")
//    private UUID menuId;
//
//    private String name;
//
//    @JsonProperty("created_at")
//    private LocalDateTime createdAt;
//
//    public CreateMenuOptionResponseDto(MenuOptionEntity option) {
//        this.optionId = option.getId();
//        this.menuId = option.getMenu().getId();
//        this.name = option.getName();
//        this.createdAt = option.getCreatedAt();
//    }
//}
