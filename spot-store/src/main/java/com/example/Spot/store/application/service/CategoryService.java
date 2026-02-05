package com.example.Spot.store.application.service;

import java.util.List;
import java.util.UUID;

import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;
import com.example.Spot.store.presentation.dto.response.CategoryResponseDTO;

public interface CategoryService {

    // ************* //
    // 매장 카테고리 조회 //
    // ************* //
    List<CategoryResponseDTO.CategoryItem> getAllCategory();
    List<CategoryResponseDTO.StoreSummary> getStoresByCategoryId(UUID categoryId);
    List<CategoryResponseDTO.StoreSummary> getStoresByCategoryName(String name);

    // ************** //
    // 매장 카테고리 생성 //
    // ************** //
    CategoryResponseDTO.CategoryDetail createCategory(CategoryRequestDTO.Create request);

    // ************** //
    // 매장 카테고리 변경 //
    // ************** //
    CategoryResponseDTO.CategoryDetail updateCategory(UUID categoryId, CategoryRequestDTO.Update request);

    // ************** //
    // 매장 카테고리 삭제 //
    // ************** //
    void deleteCategory(UUID categoryId, Integer userId);
}
