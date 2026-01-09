package com.example.Spot.cart.application.service;

import java.util.UUID;

import com.example.Spot.cart.presentation.dto.request.AddCartItemRequestDto;
import com.example.Spot.cart.presentation.dto.request.UpdateCartItemQuantityRequestDto;
import com.example.Spot.cart.presentation.dto.response.CartResponseDto;

public interface CartService {

    CartResponseDto getCart(Integer userId);

    CartResponseDto addCartItem(Integer userId, AddCartItemRequestDto requestDto);

    CartResponseDto updateCartItemQuantity(Integer userId, UUID cartItemId, UpdateCartItemQuantityRequestDto requestDto);

    CartResponseDto removeCartItem(Integer userId, UUID cartItemId);
    
    void clearCart(Integer userId);
}

