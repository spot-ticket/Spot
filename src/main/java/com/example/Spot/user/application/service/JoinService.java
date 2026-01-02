package com.example.Spot.user.application.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Spot.user.presentation.dto.request.JoinDTO;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.entity.UserAuthEntity;
import com.example.Spot.user.domain.repository.UserRepository;
import com.example.Spot.user.domain.repository.UserAuthRepository;
import com.example.Spot.global.presentation.advice.DuplicateUserException;

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
                joinDTO.getAddress(),
                joinDTO.getEmail(),
                joinDTO.getRole()
        );

        // male/age가 생성자에 없으면 여기서 반영
        // (UserEntity에 setter가 없다면 updateProfile 같은 메서드를 만들어서 쓰는 걸 추천)
        user.setMale(joinDTO.isMale());
        user.setAge(joinDTO.getAge());

        userRepository.save(user);

        // 3) UserAuthEntity 생성/저장 (p_user_auth)
        String hashedPassword = bCryptPasswordEncoder.encode(joinDTO.getPassword());
        UserAuthEntity auth = new UserAuthEntity(user, hashedPassword);

        userAuthRepository.save(auth);


//        String username = joinDTO.getUsername();
//        String password = joinDTO.getPassword();
//        String nickname = joinDTO.getNickname();
//        String email = joinDTO.getEmail();
//        boolean male = joinDTO.isMale();
//        int age = joinDTO.getAge();
//        String address = joinDTO.getAddress();
//        Role role = joinDTO.getRole();
//
//
//
//        // userEntity 저장
//        UserEntity data = new UserEntity();
//        data.setUsername(username);
//        data.setPassword(bCryptPasswordEncoder.encode(password));
//        data.setNickname(nickname);
//        data.setEmail(email);
//        data.setMale(male);
//        data.setAge(age);
//        data.setAddress(address);
//        data.setRole(role);
//
//        userRepository.save(data);
    }

}
