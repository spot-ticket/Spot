package com.example.Spot.menu.presentation.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.user.domain.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuOptionAdminResponseDto(

    UUID id,
    UUID menuId,
    String name,
    Integer price,
    String detail,
    Boolean isAvailable,
    Boolean isHidden,
    Boolean isDeleted,
    LocalDateTime createdAt,
    Integer createdBy,
    LocalDateTime updatedAt,
    Integer updatedBy,
    LocalDateTime deletedAt,
    Integer deletedBy
) {
    public static MenuOptionAdminResponseDto of(MenuOptionEntity option, Role userRole) {

        LocalDateTime createdAt = null;
        Integer createdBy = null;

        LocalDateTime updatedAt = null;
        Integer updatedBy = null;

        LocalDateTime deletedAt = null;
        Integer deletedBy = null;

        // 권한 체크: 관리자(MASTER, MANAGER)인 경우에만 실제 값을 엔티티에서 꺼내옴
        if (userRole == Role.MASTER || userRole == Role.MANAGER) {
            createdAt = option.getCreatedAt();
            createdBy = option.getCreatedBy();

            updatedAt = option.getUpdatedAt();
            updatedBy = option.getUpdatedBy();

            deletedAt = option.getDeletedAt();
            deletedBy = option.getDeletedBy();
        }

        // Return 할 때 위에서 만든 '지역 변수'를 넣어줍니다.
        return new MenuOptionAdminResponseDto(
                option.getId(),
                option.getMenu().getId(),
                option.getName(),
                option.getPrice(),
                option.getDetail(),
                option.isAvailable(),
                option.isHidden(),
                option.getIsDeleted(),

                createdAt, // 위에서 선언한 지역 변수 사용
                createdBy,

                updatedAt,
                updatedBy,

                deletedAt,
                deletedBy
        );
    }
}
