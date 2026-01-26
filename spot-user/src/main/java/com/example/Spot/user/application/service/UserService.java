package com.example.Spot.user.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.global.common.Role;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.user.presentation.dto.request.SignupCompleteRequestDTO;
import com.example.Spot.user.presentation.dto.request.UserUpdateRequestDTO;
import com.example.Spot.user.presentation.dto.response.UserResponseDTO;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CognitoAdminService cognitoAdminService;

    public UserResponseDTO getByUserId(Integer userid) {
        UserEntity user = userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return toResponse(user);
    }

    @Transactional
    public UserResponseDTO updateById(Integer userid, UserUpdateRequestDTO req) {
        UserEntity user = userRepository.findById(userid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (req.nickname() != null) {
            user.setNickname(req.nickname());
        }
        if (req.email() != null) {
            user.setEmail(req.email());
        }
        if (req.roadAddress() != null || req.addressDetail() != null) {
            user.setAddress(
                    req.roadAddress() != null ? req.roadAddress() : user.getRoadAddress(),
                    req.addressDetail() != null ? req.addressDetail() : user.getAddressDetail()
            );
        }

        return toResponse(user);
    }

    @Transactional
    public void deleteMe(Integer loginUserId) {
        UserEntity user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.softDelete(user.getId());
    }
    

    
    public List<UserResponseDTO> searchUsersByNickname(String nickname) {
        List<UserEntity> users = userRepository.findByNicknameContaining(nickname);
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDTO completeSignup(Jwt jwt, SignupCompleteRequestDTO request) {
        String cognitoSub = jwt.getSubject();

        if (cognitoSub == null || cognitoSub.isBlank()) {
            throw new IllegalArgumentException("Missing sub");
        }

        Role role = resolveRole(jwt.getClaimAsString("custom:role"));
        String cognitoUsername = resolveCognitoUsername(jwt, cognitoSub);

        UserEntity user = userRepository.findByCognitoSub(cognitoSub)
                .orElseGet(() -> createUserSafely(cognitoUsername, cognitoSub, role, request));

        if (user.getRole() == null) {
            user.setRole(role);
        }

        user.setNickname(request.getNickname());
        user.setAddress(request.getRoadAddress(), request.getAddressDetail());
        user.setMale(request.isMale());
        user.setAge(request.getAge());

        user.markCompleted();

        UserEntity persisted = userRepository.save(user);

        try {
            cognitoAdminService.backfillUserIdAndRole(
                    cognitoUsername,
                    persisted.getId(),
                    persisted.getRole().name()
            );
        } catch (RuntimeException ignored) {
        }

        return toResponse(persisted);
    }

    private UserEntity createUserSafely(
            String cognitoUsername,
            String cognitoSub,
            Role role,
            SignupCompleteRequestDTO request
    ) {
        try {
            UserEntity user = new UserEntity(
                    cognitoUsername,
                    cognitoSub,
                    request.getNickname(),
                    request.getRoadAddress(),
                    request.getAddressDetail(),
                    "",
                    role
            );
            user.setMale(request.isMale());
            user.setAge(request.getAge());
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByCognitoSub(cognitoSub)
                    .orElseThrow(() -> e);
        }
    }
    private String resolveCognitoUsername(Jwt jwt, String fallback) {
        String cognitoUsername = jwt.getClaimAsString("cognito:username");
        if (cognitoUsername == null || cognitoUsername.isBlank()) {
            cognitoUsername = jwt.getClaimAsString("username");
        }
        if (cognitoUsername == null || cognitoUsername.isBlank()) {
            cognitoUsername = fallback;
        }
        return cognitoUsername;
    }

    private Role resolveRole(String roleClaim) {
        if (roleClaim == null || roleClaim.isBlank()) {
            return Role.CUSTOMER;
        }
        try {
            return Role.valueOf(roleClaim);
        } catch (IllegalArgumentException e) {
            return Role.CUSTOMER;
        }
    }

    private UserResponseDTO toResponse(UserEntity user) {
        return UserResponseDTO.from(user);
    }




}
