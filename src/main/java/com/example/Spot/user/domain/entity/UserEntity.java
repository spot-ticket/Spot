package com.example.Spot.user.domain.entity;
import com.example.Spot.global.common.UpdateBaseEntity;
import com.example.Spot.store.domain.entity.StoreStaffEntity;
import com.example.Spot.user.domain.Role;
import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(name="p_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends UpdateBaseEntity {

    @Id
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<StoreStaffEntity> staffs = new ArrayList<>();

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
        UserEntity user = new UserEntity();
        user.username = username;
        user.role = role;
        return user;
    }

    @Builder
    public void ismale() {
        this.male = true;
    }


}

