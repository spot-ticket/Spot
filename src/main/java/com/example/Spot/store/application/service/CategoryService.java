package com.example.Spot.store.application.service;

import java.util.List;
import java.util.UUID;

import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;
import com.example.Spot.store.presentation.dto.response.CategoryResponseDTO;
import com.example.Spot.user.domain.entity.UserEntity;

public interface CategoryService {

    List<CategoryResponseDTO.CategoryItem> getAll();

    List<CategoryResponseDTO.StoreSummary> getStoresByCategoryId(UUID categoryId);

    List<CategoryResponseDTO.StoreSummary> getStoresByCategoryName(String name);

    CategoryResponseDTO.CategoryDetail create(CategoryRequestDTO.Create request);

    CategoryResponseDTO.CategoryDetail update(UUID categoryId, CategoryRequestDTO.Update request);

    void delete(UUID categoryId, UserEntity user);
}
