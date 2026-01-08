package com.example.Spot.user.domain.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.store.domain.entity.StoreUserEntity;
import com.example.Spot.user.domain.Role;

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

    @Column(nullable = false)
    private boolean male;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private String roadAddress;

    @Column(nullable = false)
    private String addressDetail;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    public UserEntity(String username, String nickname, String roadAddress, String addressDetail, String email, Role role) {
        this.username = username;
        this.nickname = nickname;
        this.roadAddress = roadAddress;
        this.addressDetail = addressDetail;
        this.email = email;
        this.role = role;
    }

    public static UserEntity forAuthentication(Integer id, Role role) {
        UserEntity user = new UserEntity();
        user.id = id;
        user.role = role;
        return user;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String roadAddress, String addressDetail) {
        this.roadAddress = roadAddress;
        this.addressDetail = addressDetail;
    }
}
