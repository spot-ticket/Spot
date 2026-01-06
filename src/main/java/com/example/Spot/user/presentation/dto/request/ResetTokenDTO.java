package com.example.Spot.user.presentation.dto.request;

public class ResetTokenDTO {
    public record EmailRequest(String email) {}

    public record ResetPasswordRequest(String token, String newPassword) {}

}
