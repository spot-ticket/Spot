package com.example.Spot.store.presentation.dto.request;

import java.time.LocalTime;
import java.util.List;

import com.example.Spot.store.domain.entity.CategoryEntity;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StoreCreateRequest {
    
    @NotBlank(message = "매장 이름은 필수입니다.")
    private String name;
    
    @NotBlank(message = "주소는 필수입니다.")
    private String address;
    private String detailAddress;
    private String phoneNumber; 
    
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime openTime;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime closeTime;
    
    @NotNull(message = "오너 ID는 필수입니다.")
    private Integer ownerId;
    
    @NotNull(message = "셰프 ID는 필수입니다.")
    private Integer chefId;
    
    @NotEmpty(message = "최소 하나 이상의 카테고리를 선택해야 합니다.")
    private List<String> categoryNames;
    
    public StoreEntity toEntity(List<CategoryEntity>categories) {
        StoreEntity store = StoreEntity.builder()
                .name(this.name)
                .address(this.address)
                .detailAddress(this.detailAddress)
                .phoneNumber(this.phoneNumber)
                .openTime(this.openTime)
                .closeTime(this.closeTime)
                .build();
        
        if (categories != null) {
            categories.forEach(store::addCategory);
        }
        
        return store;
    }
}
