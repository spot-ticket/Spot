package com.example.Spot.cart.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.cart.domain.entity.CartEntity;
import com.example.Spot.cart.domain.entity.CartItemEntity;
import com.example.Spot.cart.domain.entity.CartItemOptionEntity;
import com.example.Spot.cart.domain.repository.CartItemRepository;
import com.example.Spot.cart.domain.repository.CartRepository;
import com.example.Spot.cart.presentation.dto.request.AddCartItemRequestDto;
import com.example.Spot.cart.presentation.dto.request.CartItemOptionRequestDto;
import com.example.Spot.cart.presentation.dto.request.UpdateCartItemQuantityRequestDto;
import com.example.Spot.cart.presentation.dto.response.CartResponseDto;
import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.menu.domain.repository.MenuOptionRepository;
import com.example.Spot.menu.domain.repository.MenuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    @Override
    public CartResponseDto getCart(Integer userId) {
        CartEntity cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> CartEntity.builder()
                        .userId(userId)
                        .build());
        
        return CartResponseDto.from(cart);
    }

    @Override
    @Transactional
    public CartResponseDto addCartItem(Integer userId, AddCartItemRequestDto requestDto) {
        // 장바구니 조회 또는 생성
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    CartEntity newCart = CartEntity.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart);
                });

        MenuEntity menu = menuRepository.findById(requestDto.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        if (menu.getIsHidden()) {
            throw new IllegalArgumentException("판매 중지된 메뉴입니다: " + menu.getName());
        }

        // 가게 검증 (다른 가게의 상품인지)
        if (menu.getStore() != null) {
            cart.validateStore(menu.getStore().getId());
        }

        CartItemEntity cartItem = CartItemEntity.builder()
                .menu(menu)
                .quantity(requestDto.getQuantity())
                .build();

        for (CartItemOptionRequestDto optionDto : requestDto.getOptions()) {
            MenuOptionEntity menuOption = menuOptionRepository.findById(optionDto.getMenuOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 옵션입니다."));

            if (!menuOption.getIsAvailable()) {
                throw new IllegalArgumentException("선택할 수 없는 옵션입니다: " + menuOption.getName());
            }

            if (!menuOption.getMenu().getId().equals(menu.getId())) {
                throw new IllegalArgumentException("해당 메뉴의 옵션이 아닙니다.");
            }

            CartItemOptionEntity cartItemOption = CartItemOptionEntity.builder()
                    .menuOption(menuOption)
                    .build();

            cartItem.addOption(cartItemOption);
        }

        cart.addItem(cartItem);
        cartRepository.save(cart);

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponseDto updateCartItemQuantity(Integer userId, UUID cartItemId, 
                                                   UpdateCartItemQuantityRequestDto requestDto) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템이 존재하지 않습니다."));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("해당 사용자의 장바구니 아이템이 아닙니다.");
        }

        cartItem.updateQuantity(requestDto.getQuantity());

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponseDto removeCartItem(Integer userId, UUID cartItemId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템이 존재하지 않습니다."));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("해당 사용자의 장바구니 아이템이 아닙니다.");
        }

        cartItemRepository.delete(cartItem);

        return getCart(userId);
    }

    @Override
    @Transactional
    public void clearCart(Integer userId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        cart.clear();
        cartRepository.save(cart);
    }
}

