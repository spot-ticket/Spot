package com.example.Spot.user.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Spot.global.presentation.advice.DuplicateResourceException;
import com.example.Spot.user.presentation.dto.request.JoinDTO;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

@Service
public class JoinService {

    private final CognitoIdentityProviderClient cognito;

    @Value("${cognito.client-id}")
    private String clientId;

    public JoinService(CognitoIdentityProviderClient cognito) {
        this.cognito = cognito;
    }

    public void joinProcess(JoinDTO joinDTO) {
        try {
            SignUpRequest request = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(joinDTO.getUsername())
                    .password(joinDTO.getPassword())
                    .userAttributes(
                            AttributeType.builder().name("email").value(joinDTO.getEmail()).build()
                    )
                    .build();

            cognito.signUp(request);
        } catch (UsernameExistsException e) {
            throw new DuplicateResourceException("USERNAME_ALREADY_EXISTS");
        }
    }
}
