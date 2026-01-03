package com.example.Spot.user.domain.entity;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.example.Spot.global.common.UpdateBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "p_user_auth",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_auth_user_id",
                columnNames = "user_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthEntity extends UpdateBaseEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String hashedPassword;

    @Builder
    public UserAuthEntity(UserEntity user, String hashedPassword) {
        this.user = user;
        this.hashedPassword = hashedPassword;
    }

    public void changePassword(String newHashedPassword) {
        this.hashedPassword = newHashedPassword;
    }
}
