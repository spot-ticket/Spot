package com.example.Spot.user.presentation.dto.request;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

import com.example.Spot.user.domain.Role;

@Setter
@Getter
public class JoinDTO {



    private String username;
    private String password;

    private String nickname;

    private String email;

    private boolean male;
    private int age;
    private String address;
    private Role role;
}

