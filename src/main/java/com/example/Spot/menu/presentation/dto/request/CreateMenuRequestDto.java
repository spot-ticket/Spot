package com.example.Spot.menu.presentation.dto.request;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.store.domain.entity.StoreEntity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMenuRequestDto {

    @NotBlank(message = "메뉴명은 필수입니다.")
    private String name;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value =  0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;

    private String description;

    private String imageUrl;

    public MenuEntity toEntity(StoreEntity store) {
        // imageUrl이 null이거나 빈 문자열이면 기본 이미지 사용
        String finalImageUrl = (imageUrl == null || imageUrl.isBlank())
            ? "https://via.placeholder.com/300x200?text=Menu+Image"
            : imageUrl;

        return MenuEntity.builder()
                .store(store)
                .name(name)
                .category(category)
                .price(price)
                .description(description)
                .imageUrl(finalImageUrl)
                .build();
    }
}
