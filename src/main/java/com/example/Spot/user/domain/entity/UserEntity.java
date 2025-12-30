package com.example.Spot.user.domain.entity;

import com.example.Spot.user.presentation.dto.request.JoinDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter

@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String username;
    private String password;
    private String nickname;
    private String email;

    private boolean male;
    private int age;
    private String address;


    @Enumerated(EnumType.STRING)
    private JoinDTO.Role role;

    // created at
}

