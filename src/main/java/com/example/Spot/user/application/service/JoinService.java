package com.example.Spot.user.application.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Spot.global.presentation.advice.DuplicateUserException;
import com.example.Spot.user.domain.entity.UserAuthEntity;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserAuthRepository;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.user.presentation.dto.request.JoinDTO;

@Service
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserAuthRepository userAuthRepository;

    public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, UserAuthRepository userAuthRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userAuthRepository = userAuthRepository;
    }

    // 회원가입
    public void joinProcess(JoinDTO joinDTO) {

        // 존재하는 ID인지 확인
        if (userRepository.existsByUsername(joinDTO.getUsername())) {
            throw new DuplicateUserException("USERNAME_ALREADY_EXISTS");
        }

        // User Entity 저장
        UserEntity user = new UserEntity(
                joinDTO.getUsername(),
                joinDTO.getNickname(),
                joinDTO.getRoadAddress(),
                joinDTO.getAddressDetail(),
                joinDTO.getEmail(),
                joinDTO.getRole()
        );
        user.setMale(joinDTO.isMale());
        user.setAge(joinDTO.getAge());

        userRepository.save(user);

        // 3) UserAuthEntity 생성/저장 (p_user_auth)
        String hashedPassword = bCryptPasswordEncoder.encode(joinDTO.getPassword());
        UserAuthEntity auth = new UserAuthEntity(user, hashedPassword);

        userAuthRepository.save(auth);

    }

}
