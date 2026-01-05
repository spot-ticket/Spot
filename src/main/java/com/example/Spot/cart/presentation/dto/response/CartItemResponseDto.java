package com.example.Spot.cart.presentation.dto.response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.Spot.cart.domain.entity.CartItemEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDto {
    
    private UUID cartItemId;
    private UUID menuId;
    private String menuName;
    private Integer menuPrice;
    private String menuImageUrl;
    private Integer quantity;
    private List<CartItemOptionResponseDto> options;
    private Integer subtotal;
    
    private Boolean isAvailable;  
    private Boolean isHidden;     
    private Boolean isOrderable;  
    private String statusMessage; 
    
    public static CartItemResponseDto from(CartItemEntity item) {
        List<CartItemOptionResponseDto> options = item.getOptions().stream()
            .map(CartItemOptionResponseDto::from)
            .collect(Collectors.toList());
        
        boolean isAvailable = item.isMenuAvailable();
        boolean isHidden = item.isMenuHidden();
        boolean isOrderable = item.isOrderable();
        
        // 상태 메시지 생성
        String statusMessage = null;
        if (isHidden) {
            statusMessage = "판매 중지된 상품입니다";
        } else if (!isAvailable) {
            statusMessage = "품절된 상품입니다";
        }
        
        return CartItemResponseDto.builder()
            .cartItemId(item.getId())
            .menuId(item.getMenu().getId())
            .menuName(item.getMenuName())
            .menuPrice(item.getMenuPrice())
            .menuImageUrl(item.getMenu().getImageUrl())
            .quantity(item.getQuantity())
            .options(options)
            .subtotal(isOrderable ? item.getSubtotal() : null)  // 주문 불가면 null
            .isAvailable(isAvailable)
            .isHidden(isHidden)
            .isOrderable(isOrderable)
            .statusMessage(statusMessage)
            .build();
    }
}

