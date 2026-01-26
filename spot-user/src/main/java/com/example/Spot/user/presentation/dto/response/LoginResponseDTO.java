// src/main/java/com/example/Spot/user/presentation/dto/response/LoginResponseDTO.java
package com.example.Spot.user.presentation.dto.response;

public class LoginResponseDTO {

    private String accessToken;
    private String idToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(
            String accessToken,
            String idToken,
            String refreshToken,
            Integer expiresIn,
            String tokenType
    ) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
