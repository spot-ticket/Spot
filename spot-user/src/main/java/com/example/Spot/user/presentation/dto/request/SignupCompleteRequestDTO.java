package com.example.Spot.user.presentation.dto.request;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupCompleteRequestDTO {

    private String nickname;

    private boolean male;

    private int age;

    private String roadAddress;

    private String addressDetail;
}

