package com.example.Spot.user.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.store.domain.entity.StoreUserEntity;
import com.example.Spot.user.domain.Role;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends UpdateBaseEntity {

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private final List<StoreUserEntity> staffs = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String username;

    @Column(nullable = false)
    private String nickname;
    private boolean male;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
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


    public static UserEntity forAuthentication(String username, Role role) {
        UserEntity user = new UserEntity();
        user.username = username;
        user.role = role;
        return user;
    }

    public void ismale() {
        this.male = true;
    }

    // Setter methods
    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
