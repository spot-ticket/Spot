package com.example.Spot.cart.presentation.dto.response;

import java.util.UUID;

import com.example.Spot.cart.domain.entity.CartItemOptionEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemOptionResponseDto {
    
    private UUID menuOptionId;
    private String optionName;
    private String optionDetail;
    private Integer optionPrice;
    private Boolean isAvailable;
    
    public static CartItemOptionResponseDto from(CartItemOptionEntity option) {
        return CartItemOptionResponseDto.builder()
            .menuOptionId(option.getMenuOption().getId())
            .optionName(option.getOptionName())
            .optionDetail(option.getOptionDetail())
            .optionPrice(option.getOptionPrice())
            .isAvailable(option.getMenuOption().getIsAvailable())
            .build();
    }
}

