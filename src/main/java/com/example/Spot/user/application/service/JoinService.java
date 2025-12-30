package com.example.Spot.user.application.service;

import com.example.Spot.user.presentation.dto.request.JoinDTO;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.global.exception.DuplicateUserException;
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
        String nickname = joinDTO.getNickname();
        String email = joinDTO.getEmail();
        boolean male = joinDTO.isMale();
        int age = joinDTO.getAge();
        String address = joinDTO.getAddress();
        JoinDTO.Role role = joinDTO.getRole();

        // 존재하는 ID인지 확인
        if (userRepository.existsByUsername(joinDTO.getUsername())) {
            throw new DuplicateUserException("USERNAME_ALREADY_EXISTS");
        }

        // userEntity 저장
        UserEntity data = new UserEntity();
        data.setUsername(username);
        data.setPassword(bCryptPasswordEncoder.encode(password));
        data.setNickname(nickname);
        data.setEmail(email);
        data.setMale(male);
        data.setAge(age);
        data.setAddress(address);
        data.setRole(role);

        userRepository.save(data);
    }

}
