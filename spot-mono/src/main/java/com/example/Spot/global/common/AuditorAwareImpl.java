package com.example.Spot.global.common;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class AuditorAwareImpl implements AuditorAware<Integer> {

    @Override
    public Optional<Integer> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();


        // 회원가입: null이므로 0 (SYSTEM)
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return Optional.of(0); // SYSTEM
        }

        Object principal = auth.getPrincipal();


        // 1) principal을 Integer로
        if (principal instanceof Integer id) {
            return Optional.of(id);
        }

        // if... CustomUserDetails
//        if (principal instanceof CustomUserDetails cud) {
//            return Optional.ofNullable(cud.getUserId()).or(() -> Optional.of(0));
//        }

        // 2) 그 외는 SYSTEM
        return Optional.of(0);
    }
}
