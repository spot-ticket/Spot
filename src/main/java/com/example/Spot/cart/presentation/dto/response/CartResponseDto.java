package com.example.Spot.cart.presentation.dto.response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.cart.domain.entity.CartEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDto {
    
    private UUID cartId;
    private UUID storeId;
    private String storeName;
    private List<CartItemResponseDto> items;
    private Integer totalAmount;
    private Integer orderableItemCount;  // 주문 가능한 아이템 수
    private Integer unavailableItemCount;  // 품절/숨김 아이템 수
    
    public static CartResponseDto from(CartEntity cart) {
        List<CartItemResponseDto> items = cart.getItems().stream()
            .map(CartItemResponseDto::from)
            .collect(Collectors.toList());
        
        // 주문 가능한 아이템만 합산
        int totalAmount = cart.getItems().stream()
            .filter(item -> item.isOrderable())
            .mapToInt(item -> item.getSubtotal())
            .sum();
        
        long orderableCount = cart.getItems().stream()
            .filter(item -> item.isOrderable())
            .count();
        
        long unavailableCount = cart.getItems().stream()
            .filter(item -> !item.isOrderable())
            .count();
        
        return CartResponseDto.builder()
            .cartId(cart.getId())
            .storeId(cart.getStore() != null ? cart.getStore().getId() : null)
            .storeName(cart.getStore() != null ? cart.getStore().getName() : null)
            .items(items)
            .totalAmount(totalAmount)
            .orderableItemCount((int) orderableCount)
            .unavailableItemCount((int) unavailableCount)
            .build();
    }
}

