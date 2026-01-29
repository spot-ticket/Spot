package com.example.Spot.user.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

@Service
public class CognitoAdminService {

    private final CognitoIdentityProviderClient cognito;
    private String userPoolId;

    public CognitoAdminService(
            @Value("${aws.region:ap-northeast-2}") String region,
            @Value("${cognito.user-pool-id}") String userPoolId
    ) {
        this.cognito = CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .build();
        this.userPoolId = userPoolId;
    }

    public void backfillUserIdAndRole(String username, Integer userId, String role) {
        cognito.adminUpdateUserAttributes(AdminUpdateUserAttributesRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .userAttributes(
                        AttributeType.builder().name("custom:user_id").value(String.valueOf(userId)).build(),
                        AttributeType.builder().name("custom:role").value(role).build()
                )
                .build());
    }
}
