package com.example.Spot.store.presentation.controller;
import java.util.List;
import java.util.UUID;

import com.example.Spot.global.presentation.ApiResponse;
import com.example.Spot.global.presentation.code.GeneralSuccessCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.infra.auth.security.CustomUserDetails;
import com.example.Spot.store.application.service.CategoryService;
import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;
import com.example.Spot.store.presentation.dto.response.CategoryResponseDTO;
import com.example.Spot.user.domain.entity.UserEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 전체 조회
    @GetMapping
    public ApiResponse<List<CategoryResponseDTO.CategoryItem>> getAll() {
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, categoryService.getAll());
    }

    // 카테고리별 매장 조회
    @GetMapping("/{categoryName}/stores")
    public ApiResponse<List<CategoryResponseDTO.StoreSummary>> getStores(@PathVariable String categoryName) {
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, categoryService.getStoresByCategoryName(categoryName));
    }

    // 카테고리 생성
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    @PostMapping
    public ApiResponse<CategoryResponseDTO.CategoryDetail> create(
            @RequestBody @Valid CategoryRequestDTO.Create request
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, categoryService.create(request));
    }

    // 카테고리 수정
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    @PatchMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDTO.CategoryDetail> update(
            @PathVariable UUID categoryId,
            @RequestBody @Valid CategoryRequestDTO.Update request
    ) {
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, categoryService.update(categoryId, request));
    }

    // 카테고리 삭제(soft delete)
    @PreAuthorize("hasAnyRole('MASTER','MANAGER')")
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> delete(
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        categoryService.delete(categoryId, principal.getUserEntity());
        return ApiResponse.onSuccess(GeneralSuccessCode.GOOD_REQUEST, null);
    }
}
