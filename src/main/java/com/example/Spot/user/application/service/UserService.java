package com.example.Spot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.user.presentation.dto.request.UserUpdateRequestDTO;
import com.example.Spot.user.presentation.dto.response.UserResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDTO getByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return toResponse(user);
    }

    @Transactional
    public UserResponseDTO updateByUsername(String username, UserUpdateRequestDTO req) {
        UserEntity user = userRepository.findByUsername(username)
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
    public void deleteByUsername(String targetUsername, String loginUsername) {
        if (!targetUsername.equals(loginUsername)) {
            throw new AccessDeniedException("사용자 본인만 삭제할 수 있습니다.");
        }

        UserEntity user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.softDelete();
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
}
