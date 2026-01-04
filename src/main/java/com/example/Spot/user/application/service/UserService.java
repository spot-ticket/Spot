package com.example.Spot.user.application.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.user.presentation.dto.response.UserResponseDTO;
import com.example.Spot.user.presentation.dto.request.UserUpdateRequestDTO;

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


    // USER UPDATE
    @Transactional
    public UserResponseDTO updateByUsername(String targetUsername, String loginUsername, UserUpdateRequestDTO req) {
        // 권한 체크: 본인만 수정 가능
        if (!targetUsername.equals(loginUsername)) {
            throw new AccessDeniedException("사용자 본인만 수정할 수 있습니다.");
        }

        UserEntity user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));


        if (req.nickname() != null) user.setNickname(req.nickname());
        if (req.email() != null) user.setEmail(req.email());
        if (req.address() != null) user.setAddress(req.address());

        return toResponse(user);
    }


    // USER DELETE
    @Transactional
    public void deleteByUsername(String targetUsername, String loginUsername) {
        // 권한 체크: 본인만 삭제 가능
        if (!targetUsername.equals(loginUsername)) {
            throw new AccessDeniedException("사용자 본인만 삭제할 수 있습니다.");
        }

        UserEntity user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.softDelete(); // UpdateBaseEntity의 softDelete()
    }


    private UserResponseDTO toResponse(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getNickname(),
                user.getEmail(),
                user.getAddress(),
                user.getAge(),
                user.isMale()
        );
    }


}
