package com.example.Spot.store.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreCategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreCategoryRepository;
import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;
import com.example.Spot.store.presentation.dto.response.CategoryResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final StoreCategoryRepository storeCategoryRepository;

    @Override
    public List<CategoryResponseDTO.CategoryItem> getAll() {
        return categoryRepository.findAllByIsDeletedFalse()
                .stream()
                .map(c -> new CategoryResponseDTO.CategoryItem(c.getId(), c.getName()))
                .toList();
    }


    // get
    @Override
    public List<CategoryResponseDTO.StoreSummary> getStoresByCategory(UUID categoryId) {
        CategoryEntity category = categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        List<StoreCategoryEntity> maps =
                storeCategoryRepository.findAllActiveByCategoryIdWithStore(category.getId());

        return maps.stream()
                .map(StoreCategoryEntity::getStore)
                .distinct() // 중복 매핑 방지
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
    public void delete(UUID categoryId) {
        CategoryEntity category = categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        // soft delete
        category.softDelete();
    }



    private CategoryResponseDTO.StoreSummary toStoreSummary(StoreEntity s) {
        return new CategoryResponseDTO.StoreSummary(
                s.getId(),
                s.getName(),
                s.getAddress(),
                s.getPhoneNumber(),
                s.getOpenTime(),
                s.getCloseTime()
        );
    }
}
