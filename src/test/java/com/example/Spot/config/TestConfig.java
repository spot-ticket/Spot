package com.example.Spot.config;

import java.util.Optional;

import com.example.Spot.user.infrastructure.respository.EmailSender;
import com.example.Spot.user.infrastructure.repository.FakeEmailSender;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@EnableJpaAuditing
@ActiveProfiles("test")
public class TestConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuditorAware<Integer> auditorProvider() {
        return () -> Optional.of(1);
    }

    @Bean
    @Primary
    public FakeEmailSender fakeEmailSender() {
        return new FakeEmailSender();
    }

    @Bean
    @Primary
    public EmailSender emailSender(FakeEmailSender fake) {
        return fake; // EmailSender 주입 자리에 fake로 대체
    }
}
