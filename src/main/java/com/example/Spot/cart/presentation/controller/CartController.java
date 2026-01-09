package com.example.Spot.cart.presentation.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.cart.application.service.CartService;
import com.example.Spot.cart.presentation.dto.request.AddCartItemRequestDto;
import com.example.Spot.cart.presentation.dto.request.UpdateCartItemQuantityRequestDto;
import com.example.Spot.cart.presentation.dto.response.CartResponseDto;
import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import com.example.Spot.infra.auth.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        CartResponseDto response = cartService.getCart(userId);
        
        return ResponseEntity
                .status(GeneralSuccessCode.GOOD_REQUEST.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponseDto>> addCartItem(
            @Valid @RequestBody AddCartItemRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        CartResponseDto response = cartService.addCartItem(userId, requestDto);
        
        return ResponseEntity
                .status(GeneralSuccessCode.CREATE.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.CREATE, response));
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> updateCartItemQuantity(
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemQuantityRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        CartResponseDto response = cartService.updateCartItemQuantity(userId, cartItemId, requestDto);
        
        return ResponseEntity
                .status(GeneralSuccessCode.GOOD_REQUEST.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> removeCartItem(
            @PathVariable UUID cartItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        CartResponseDto response = cartService.removeCartItem(userId, cartItemId);
        
        return ResponseEntity
                .status(GeneralSuccessCode.GOOD_REQUEST.getStatus())
                .body(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Integer userId = userDetails.getUserId();
        cartService.clearCart(userId);
        
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null));
    }
}

