package com.example.Spot.store.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.Spot.store.application.service.CategoryService;
import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;
import com.example.Spot.store.presentation.dto.response.CategoryResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 전체 조회
    @GetMapping
    public List<CategoryResponseDTO.CategoryItem> getAll() {
        return categoryService.getAll();
    }

    // 카테고리별 매장 조회
    @GetMapping("/{categoryId}/stores")
    public List<CategoryResponseDTO.StoreSummary> getStores(@PathVariable UUID categoryId) {
        return categoryService.getStoresByCategory(categoryId);
    }

    // 카테고리 생성
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO.CategoryDetail create(@RequestBody @Valid CategoryRequestDTO.Create request) {
        return categoryService.create(request);
    }

    // 카테고리 수정
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{categoryId}")
    public CategoryResponseDTO.CategoryDetail update(
            @PathVariable UUID categoryId,
            @RequestBody @Valid CategoryRequestDTO.Update request
    ) {
        return categoryService.update(categoryId, request);
    }

    // 카테고리 삭제(soft delete)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID categoryId) {
        categoryService.delete(categoryId);
    }
}
