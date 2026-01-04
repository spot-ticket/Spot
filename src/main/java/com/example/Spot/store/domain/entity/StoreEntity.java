package com.example.Spot.store.domain.entity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.user.domain.entity.UserEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(nullable = false)
    private String address;

    private String detailAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @OneToMany(
            mappedBy = "store",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
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

    @Builder
    public StoreEntity(
            String name,
            String address,
            String phoneNumber,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public void addStoreUser(UserEntity user) {
        StoreUserEntity storeUser = StoreUserEntity.builder()
                .store(this)
                .user(user)
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
            String address,
            String phoneNumber,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        if (name != null) {
            this.name = name;
        }
        if (address != null) {
            this.address = address;
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
    }
}
