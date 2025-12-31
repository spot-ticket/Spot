package com.example.Spot.user.domain.entity;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.user.domain.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
//@Table(name="p_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends UpdateBaseEntity {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String username;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "sex")
    private boolean male;

    @Column(name = "age")
    private int age;

    @Column(name = "address")
    private String address;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;


    @Builder
    public UserEntity(String username, String nickname, String address, String email, Role role) {
        this.username = username;
        this.nickname = nickname;
        this.address = address;
        this.email = email;
        this.role = role;
    }


    @Builder
    public static UserEntity forAuthentication(String username, Role role) {
        UserEntity user = new UserEntity(); // protected 생성자 → 같은 클래스라서 가능
        user.username = username;
        user.role = role;
        return user;
    }

    @Builder
    public void ismale() {
        this.male = true;
    }


}

