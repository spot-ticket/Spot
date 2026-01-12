package com.example.Spot.store.domain.entity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.review.domain.entity.ReviewEntity;
import com.example.Spot.store.domain.StoreStatus;
import com.example.Spot.user.domain.entity.UserEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Table(name = "p_store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "road_address", nullable = false)
    private String roadAddress; // 예: 서울특별시 종로구 사직로 161 //

    @Column(name = "address_detail", nullable = false)
    private String addressDetail;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status;

    @OneToMany(
            mappedBy = "store",
            cascade = CascadeType.ALL,  // Store가 저장/수정될 때 연결 정보도 함께 저장/수정 //
            orphanRemoval = true,       // 리스트에서 제거하면 DB에서도 hardDelete //
            fetch = FetchType.LAZY
    )
    private List<StoreUserEntity> users = new ArrayList<>();

    @OneToMany(
            mappedBy = "store",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<StoreCategoryEntity> storeCategoryMaps = new HashSet<>();

    @OneToMany(
            mappedBy = "store",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<ReviewEntity> reviews = new ArrayList<>();

    @Builder
    public StoreEntity(
            String name,
            String roadAddress,
            String addressDetail,
            String phoneNumber,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        this.name = name;
        this.roadAddress = roadAddress;
        this.addressDetail = addressDetail;
        this.phoneNumber = phoneNumber;
        this.openTime = openTime;
        this.closeTime = closeTime;

        this.status = StoreStatus.PENDING;
    }

    public void addStoreUser(UserEntity user) {
        StoreUserEntity storeUser = StoreUserEntity.builder()
                .store(this) // 현재 매장
                .user(user) // 전달받은 유저
                .build();
        this.users.add(storeUser);
    }

    public void addCategory(CategoryEntity category) {
        if (this.storeCategoryMaps.size() >= 3) {
            throw new IllegalArgumentException("카테고리는 최대 3개까지만 등록 가능합니다.");
        }
        StoreCategoryEntity storeCategory = StoreCategoryEntity.builder()
                .store(this)
                .category(category)
                .build();
        this.storeCategoryMaps.add(storeCategory);
    }

    public void updateStoreDetails(
            String name,
            String roadAddress,
            String addressDetail,
            String phoneNumber,
            LocalTime openTime,
            LocalTime closeTime,
            List<CategoryEntity> categories
    ) {
        if (name != null) {
            this.name = name;
        }
        if (roadAddress != null) {
            this.roadAddress = roadAddress;
        }
        if (addressDetail != null) {
            this.addressDetail = addressDetail;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (openTime != null) {
            this.openTime = openTime;
        }
        if (closeTime != null) {
            this.closeTime = closeTime;
        }

        if (categories != null) {
            this.storeCategoryMaps.clear(); // 기존 연결 해제(orphanRemova l=true 작동)
            for (CategoryEntity category : categories) {
                this.addCategory(category); // 새로운 카테고리 연결
            }
        }
    }

    public boolean isOpenNow() {
        if (this.openTime == null || this.closeTime == null) {
            return true; // 영업시간이 설정되지 않은 경우 24시간 영업으로 간주
        }

        LocalTime now = LocalTime.now();

        if (this.openTime.isBefore(this.closeTime)) {
            return !now.isBefore(this.openTime) && !now.isAfter(this.closeTime);
        } else {
            return !now.isBefore(this.openTime) || !now.isAfter(this.closeTime);
        }
    }

    public boolean isOpenAt(LocalTime time) {
        if (this.openTime == null || this.closeTime == null) {
            return true;
        }

        if (this.openTime.isBefore(this.closeTime)) {
            return !time.isBefore(this.openTime) && !time.isAfter(this.closeTime);
        } else {
            return !time.isBefore(this.openTime) || !time.isAfter(this.closeTime);
        }
    }

    public void updateStatus(StoreStatus status) {
        this.status = status;
    }
}
