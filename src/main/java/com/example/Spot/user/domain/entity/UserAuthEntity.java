package com.example.Spot.user.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_user_auth",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_auth_user_id", columnNames = "user_id"))
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

    @Builder
    public void changePassword(String newHashedPassword) {
        this.hashedPassword = newHashedPassword;
    }

}
