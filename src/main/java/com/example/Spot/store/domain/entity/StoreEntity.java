package com.example.Spot.store.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "p_stores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @OneToMany(mappedBy = "store", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<StoreStaffEntity> staffs = new ArrayList<>();

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    private Set<StoreViewEntity> storeCategoryMaps = new HashSet<>();


    @Builder
    public StoreEntity(String name, String address, String phoneNumber,
                       LocalTime openTime, LocalTime closeTime) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
}
