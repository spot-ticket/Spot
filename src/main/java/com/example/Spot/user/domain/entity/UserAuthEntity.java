package com.example.Spot.user.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_user_auth",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_auth_user_id", columnNames = "user_id"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    // p_user_auth.user_id -> p_user.id (1:1)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public UserAuthEntity(UserEntity user, String hashedPassword) {
        this.user = user;
        this.hashedPassword = hashedPassword;
    }

    public void changePassword(String newHashedPassword) {
        this.hashedPassword = newHashedPassword;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
