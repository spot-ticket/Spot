package com.example.Spot.domain.login.service;

import com.example.Spot.domain.login.dto.JoinDTO;
import com.example.Spot.domain.user.dto.UserResponseDTO;
import com.example.Spot.domain.user.entity.UserEntity;
import com.example.Spot.domain.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // 회원가입
    public void joinProcess(JoinDTO joinDTO) {
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();

        // 존재하는 ID인지 확인
        Boolean isExist = userRepository.existsByUsername(username);
        if (isExist) {
            return; // 이미 존재하면 종료(원하면 예외로 바꿀 수 있음)
        }

        // 저장
        UserEntity data = new UserEntity();
        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setRole("ROLE_ADMIN"); // 필요하면 ROLE_USER로 변경

        userRepository.save(data);
    }

    // GET API용: username으로 유저 조회 (비밀번호 제외)
    public UserResponseDTO getUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return new UserResponseDTO(user.getId(), user.getUsername(), user.getRole());
    }
}
