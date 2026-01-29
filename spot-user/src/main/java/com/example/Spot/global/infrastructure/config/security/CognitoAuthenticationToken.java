package com.example.Spot.global.infrastructure.config.security;

import java.util.Collection;
import java.util.Objects;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class CognitoAuthenticationToken extends AbstractAuthenticationToken {

    private final Jwt jwt;
    private final CustomUserDetails principal;

    public CognitoAuthenticationToken(
            Jwt jwt,
            CustomUserDetails principal,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.jwt = Objects.requireNonNull(jwt, "jwt");
        this.principal = Objects.requireNonNull(principal, "principal");
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return jwt.getTokenValue();
    }

    //  principal: CustomUserDetails
    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.getUsername();
    }

    public Jwt getJwt() {
        return jwt;
    }
}
