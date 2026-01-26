package com.example.Spot.user.domain.entity;

import com.example.Spot.global.common.Role;
import com.example.Spot.global.common.UpdateBaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_user",
uniqueConstraints = {
@UniqueConstraint(name = "uk_users_cognito_sub", columnNames = "cognito_sub")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends UpdateBaseEntity {

    // MSA 전환으로 StoreUserEntity와의 관계 제거 (userId로 참조)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cognito_sub", nullable = false, unique = true, length = 64)
    private String cognitoSub;

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

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_state", nullable = false, length = 30)
    private SignupState signupState;

    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted;

    @Builder
    public UserEntity(String username, String cognitoSub, String nickname, String roadAddress, String addressDetail, String email, Role role) {
        this.cognitoSub = cognitoSub;
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

    public String getCognitoSub() {
        return cognitoSub;
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
    public void markCompleted() {
        this.signupState = SignupState.COMPLETED;
        this.profileCompleted = true;
    }
}
