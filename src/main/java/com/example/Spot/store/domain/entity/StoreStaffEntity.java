package com.example.Spot.store.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.user.domain.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
