package com.example.Spot.store.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Spot.store.application.service.CategoryServiceImpl;
import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.repository.CategoryRepository;
import com.example.Spot.store.domain.repository.StoreCategoryRepository;
import com.example.Spot.store.presentation.dto.request.CategoryRequestDTO;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock private StoreCategoryRepository storeCategoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategorySuccess() {
        // given
        CategoryRequestDTO.Create request = new CategoryRequestDTO.Create("한식");
        given(categoryRepository.existsByNameAndIsDeletedFalse("한식")).willReturn(false);
        given(categoryRepository.saveAndFlush(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        var result = categoryService.createCategory(request);

        // then
        assertThat(result.name()).isEqualTo("한식");
        verify(categoryRepository).saveAndFlush(any());
    }

    @Test
    @DisplayName("카테고리 이름으로 매장 목록 조회")
    void getStoresByCategoryNameSuccess() {
        // given
        String catName = "치킨";
        UUID catId = UUID.randomUUID();
        CategoryEntity mockCategory = CategoryEntity.builder().name(catName).build();
        // Reflection을 사용해 가짜 ID 주입 (Entity에 @Id가 있으므로)
        ReflectionTestUtils.setField(mockCategory, "id", catId);

        given(categoryRepository.findByNameAndIsDeletedFalse(catName)).willReturn(mockCategory);
        given(storeCategoryRepository.findAllActiveByCategoryIdWithStore(catId)).willReturn(List.of());

        // when
        var result = categoryService.getStoresByCategoryName(catName);

        // then
        assertThat(result).isNotNull();
        verify(storeCategoryRepository).findAllActiveByCategoryIdWithStore(catId);
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategorySuccess() {
        // given
        UUID catId = UUID.randomUUID();
        CategoryEntity category = spy(CategoryEntity.builder().name("중식").build());
        given(categoryRepository.findByIdAndIsDeletedFalseWithLock(catId)).willReturn(Optional.of(category));

        CategoryRequestDTO.Update updateRequest = new CategoryRequestDTO.Update("중식요리");

        // when
        categoryService.updateCategory(catId, updateRequest);

        // then
        assertThat(category.getName()).isEqualTo("중식요리");
    }

    @Test
    @DisplayName("카테고리 삭제(Soft Delete) 성공")
    void deleteCategorySuccess() {
        // given
        UUID catId = UUID.randomUUID();
        CategoryEntity category = spy(CategoryEntity.builder().name("일식").build());
        given(categoryRepository.findByIdAndIsDeletedFalseWithLock(catId)).willReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(catId, 1);

        // then
        verify(category).softDelete(1); // softDelete 메서드가 호출되었는지 확인
    }
}