package com.example.Spot.infra.auth.security;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserAuthEntity;
import com.example.Spot.user.domain.entity.UserEntity;


public class CustomUserDetails implements UserDetails {

    private final UserEntity userEntity;
    private final UserAuthEntity userAuthEntity;

    // 로그인(UserDetailsService 용)
    public CustomUserDetails(UserEntity userEntity, UserAuthEntity userAuthEntity) {
        this.userEntity = userEntity;
        this.userAuthEntity = userAuthEntity;
    }

    //JWT filter용
    public CustomUserDetails(UserEntity userEntity) {
        this.userEntity = userEntity;
        this.userAuthEntity = null;
    }

    // API(Controller)에서 사용 - id, role
    public UserEntity getUserEntity() {
        return userEntity;
    }

    // role값 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(() -> "ROLE_" + userEntity.getRole().name());

        return collection;
    }

    @Override
    public String getPassword() {
        return (userAuthEntity == null) ? "" : userAuthEntity.getHashedPassword();
    }

    @Override
    public String getUsername() {

        return userEntity.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }

    public Integer getUserId() {
        return userEntity.getId();
    }

    public Role getUserRole() {
        return userEntity.getRole();
    }
}
