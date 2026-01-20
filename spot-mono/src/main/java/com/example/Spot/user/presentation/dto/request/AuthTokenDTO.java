package com.example.Spot.user.presentation.dto.request;

import lombok.Getter;

public class AuthTokenDTO {

    @Getter
    public static class LogoutRequest {
        private String refreshToken;
    }

    @Getter
    public static class RefreshRequest {
        private String refreshToken;
    }
}
