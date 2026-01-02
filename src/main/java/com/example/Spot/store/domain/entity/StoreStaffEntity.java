package com.example.Spot.store.domain.entity;

import java.util.UUID;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.user.domain.entity.UserEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_store_staff")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreStaffEntity extends UpdateBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity store;

    @Builder
    public StoreStaffEntity(UserEntity user, StoreEntity store) {
        this.user = user;
        this.store = store;
    }
}