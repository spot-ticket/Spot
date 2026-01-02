package com.example.Spot.user.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDTO {
    private int id;
    private String username;
    private String role;
}
@Getter
public class LogoutRequest {
    private String refreshToken;
}
@Getter
public class RefreshRequest {
    private String refreshToken;
}

public record TokenResponse(String accessToken, String refreshToken) {}