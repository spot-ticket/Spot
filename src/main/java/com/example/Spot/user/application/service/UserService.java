package com.example.Spot.user.application.service;

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
        if (req.address() != null) {
            user.setAddress(req.address());
        }

        return toResponse(user);
    }


    @Transactional
    public void deleteMe(Integer loginUserId) {
        UserEntity user = userRepository.findById(loginUserId)
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
                user.getAddress(),
                user.getAge(),
                user.isMale()
        );
    }
}
