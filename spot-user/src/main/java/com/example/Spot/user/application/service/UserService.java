package com.example.Spot.user.application.service;

import java.util.List;
import java.util.stream.Collectors;

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
    
    private UserResponseDTO toResponse(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getNickname(),
                user.getEmail(),
                user.getRoadAddress(),
                user.getAddressDetail(),
                user.getAge(),
                user.isMale()
        );
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

        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = "";
        }

        String cognitoUsername = jwt.getClaimAsString("cognito:username");
        if (cognitoUsername == null || cognitoUsername.isBlank()) {
            cognitoUsername = jwt.getClaimAsString("username");
        }
        if (cognitoUsername == null || cognitoUsername.isBlank()) {
            cognitoUsername = cognitoSub;
        }

        UserEntity existing = userRepository.findByCognitoSub(cognitoSub).orElse(null);
        if (existing != null) {
            cognitoAdminService.backfillUserIdAndRole(cognitoUsername, existing.getId(), existing.getRole().name());
            return toResponse(existing);
        }

        Role role = Role.CUSTOMER;

        UserEntity saved = new UserEntity(
                cognitoUsername,
                cognitoSub,
                request.getNickname(),
                request.getRoadAddress(),
                request.getAddressDetail(),
                email,
                role
        );

        saved.setMale(request.isMale());
        saved.setAge(request.getAge());

        UserEntity persisted = userRepository.save(saved);

        cognitoAdminService.backfillUserIdAndRole(cognitoUsername, persisted.getId(), role.name());

        return toResponse(persisted);
    }


}
