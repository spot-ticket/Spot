package com.example.Spot.store.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreCategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreCategoryRepository;
import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;
import com.example.Spot.store.presentation.dto.response.CategoryResponseDTO;
import com.example.Spot.user.domain.entity.UserEntity;


import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreCategoryRepository storeCategoryRepository;

    // 카테고리 전체 조회
    @Override
    public List<CategoryResponseDTO.CategoryItem> getAll() {
        return categoryRepository.findAllByIsDeletedFalse()
                .stream()
                .map(c -> new CategoryResponseDTO.CategoryItem(c.getId(), c.getName()))
                .toList();
    }


    // 카테고리 별 매장 조회
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO.StoreSummary> getStoresByCategoryId(UUID categoryId) {
        CategoryEntity category = categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        return getStoresByCategoryIdInternal(category.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO.StoreSummary> getStoresByCategoryName(String categoryName) {
        CategoryEntity category = categoryRepository.findByNameAndIsDeletedFalse(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));
        return getStoresByCategoryIdInternal(category.getId());
    }

    private List<CategoryResponseDTO.StoreSummary> getStoresByCategoryIdInternal(UUID categoryId) {
        List<StoreCategoryEntity> maps =
                storeCategoryRepository.findAllActiveByCategoryIdWithStore(categoryId);

        return maps.stream()
                .map(StoreCategoryEntity::getStore)
                .collect(Collectors.toMap(
                        StoreEntity::getId,
                        s -> s,
                        (a, b) -> a
                ))
                .values().stream()
                .map(this::toStoreSummary)
                .toList();
    }



    // create
    @Override
    @Transactional
    public CategoryResponseDTO.CategoryDetail create(CategoryRequestDTO.Create request) {
        if (categoryRepository.existsByNameAndIsDeletedFalse(request.name())) {
            throw new IllegalArgumentException("Category name already exists: " + request.name());
        }

        CategoryEntity saved = categoryRepository.save(
                CategoryEntity.builder()
                        .name(request.name())
                        .build()
        );

        return new CategoryResponseDTO.CategoryDetail(saved.getId(), saved.getName());
    }


    // update
    @Override
    @Transactional
    public CategoryResponseDTO.CategoryDetail update(UUID categoryId, CategoryRequestDTO.Update request) {
        CategoryEntity category = categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        // 이름 중복 방지
        if (categoryRepository.existsByNameAndIsDeletedFalse(request.name())
                && !category.getName().equals(request.name())) {
            throw new IllegalArgumentException("Category name already exists: " + request.name());
        }

        category.updateName(request.name());
        return new CategoryResponseDTO.CategoryDetail(category.getId(), category.getName());
    }


    // delete
    @Override
    @Transactional
    public void delete(UUID categoryId, UserEntity user) {
        CategoryEntity category = categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        // soft delete
        category.softDelete(user.getId());
    }



    private CategoryResponseDTO.StoreSummary toStoreSummary(StoreEntity s) {
        return new CategoryResponseDTO.StoreSummary(
                s.getId(),
                s.getName(),
                s.getRoadAddress(),
                s.getPhoneNumber(),
                s.getOpenTime(),
                s.getCloseTime()
        );
    }
}
