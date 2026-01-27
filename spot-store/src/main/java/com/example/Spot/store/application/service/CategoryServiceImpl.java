package com.example.Spot.store.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
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

    // ************* //
    // 매장 카테고리 조회 //
    // ************* //
    @Override
    public List<CategoryResponseDTO.CategoryItem> getAllCategory() {
        return categoryRepository.findAllByIsDeletedFalse()
                .stream()
                .map(c -> new CategoryResponseDTO.CategoryItem(c.getId(), c.getName()))
                .toList();
    }


    @Override
    public List<CategoryResponseDTO.StoreSummary> getStoresByCategoryId(UUID categoryId) {
        CategoryEntity category = categoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        List<StoreEntity> stores = getDistinctStoresByCategoryId(category.getId());

        return stores.stream()
                .map(this::toStoreSummary)
                .toList();
    }

    @Override
    public List<CategoryResponseDTO.StoreSummary> getStoresByCategoryName(String categoryName) {
        CategoryEntity category = categoryRepository.findByNameAndIsDeletedFalse(categoryName);

        if (category == null) {
            throw new IllegalArgumentException("Category not found: " + categoryName);
        }
        
        List<StoreEntity> stores = getDistinctStoresByCategoryId(category.getId());

        return stores.stream()
                .map(this::toStoreSummary)
                .toList();
    }

    // 카테고리 ID로 중복 제거된 Store 목록 조회
    private List<StoreEntity> getDistinctStoresByCategoryId(UUID categoryId) {
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
                .toList();
    }


    // ************** //
    // 매장 카테고리 생성 //
    // ************** //
    @Override
    @Transactional
    public CategoryResponseDTO.CategoryDetail createCategory(CategoryRequestDTO.Create request) {
        
        if (categoryRepository.existsByNameAndIsDeletedFalse(request.name())) {
            throw new IllegalArgumentException("Category name already exists: " + request.name());
        }

        try {
            CategoryEntity category = CategoryEntity.builder()
                                                    .name(request.name())
                                                    .build();

            CategoryEntity saved = categoryRepository.saveAndFlush(category);

            return new CategoryResponseDTO.CategoryDetail(saved.getId(), saved.getName());
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("매장 카테고리가 이미 생성되었습니다.", e);
        }
    }


    // ************** //
    // 매장 카테고리 변경 //
    // ************** //
    @Override
    @Transactional
    public CategoryResponseDTO.CategoryDetail updateCategory(UUID categoryId, CategoryRequestDTO.Update request) {
        CategoryEntity category = categoryRepository.findByIdAndIsDeletedFalseWithLock(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        category.updateName(request.name());
        return new CategoryResponseDTO.CategoryDetail(category.getId(), category.getName());
    }


    // ************** //
    // 매장 카테고리 삭제 //
    // ************** //
    @Override
    @Transactional
    public void deleteCategory(UUID categoryId, Integer userId) {
        CategoryEntity category = categoryRepository.findByIdAndIsDeletedFalseWithLock(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        category.softDelete(userId);
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
