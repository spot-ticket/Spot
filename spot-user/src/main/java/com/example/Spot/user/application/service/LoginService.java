package com.example.Spot.user.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Spot.user.presentation.dto.request.LoginDTO;
import com.example.Spot.user.presentation.dto.response.LoginResponseDTO;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginService {

    private final CognitoIdentityProviderClient cognito;

    @Value("${cognito.client-id}")
    private String clientId;

    public LoginService(CognitoIdentityProviderClient cognito) {
        this.cognito = cognito;
    }

    public LoginResponseDTO loginProcess(LoginDTO dto) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", dto.getUsername());
        authParams.put("PASSWORD", dto.getPassword());

        InitiateAuthRequest request = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParams)
                .build();

        InitiateAuthResponse response = cognito.initiateAuth(request);
        AuthenticationResultType result = response.authenticationResult();

        return new LoginResponseDTO(
                result.accessToken(),
                result.idToken(),
                result.refreshToken(),
                result.expiresIn(),
                result.tokenType()
        );
    }
}
