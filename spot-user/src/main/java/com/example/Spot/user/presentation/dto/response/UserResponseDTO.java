package com.example.Spot.user.presentation.dto.response;

import com.example.Spot.global.common.Role;
import com.example.Spot.user.domain.entity.UserEntity;

import lombok.Getter;

@Getter
public class UserResponseDTO {

    private final Integer id;
    private final String username;
    private final Role role;
    private final String nickname;
    private final String email;
    private final String roadAddress;
    private final String addressDetail;
    private final int age;
    private final boolean male;

    public UserResponseDTO(
            Integer id,
            String username,
            Role role,
            String nickname,
            String email,
            String roadAddress,
            String addressDetail,
            int age,
            boolean male
    ) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.nickname = nickname;
        this.email = email;
        this.roadAddress = roadAddress;
        this.addressDetail = addressDetail;
        this.age = age;
        this.male = male;
    }

    public static UserResponseDTO from(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getNickname(),
                user.getEmail(),
                user.getRoadAddress(),
                user.getAddressDetail(),
                user.getAge(),
                user.isMale()
        );
    }
}
