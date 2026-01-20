package com.example.spotstore.store.infrastructure.aop;

import java.util.UUID;

import com.example.Spot.store.infrastructure.aop.StoreValidationContext;
import com.example.Spot.store.infrastructure.aop.ValidateStoreAuthority;
import com.example.Spot.store.infrastructure.aop.ValidateUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;
import com.example.Spot.user.domain.Role;
import com.example.Spot.user.domain.entity.UserEntity;
import com.example.Spot.user.domain.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class StoreAspect {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Around("@annotation(validateUser)")
    public Object handleValidateUser(
            ProceedingJoinPoint joinPoint,
            ValidateUser validateUser) throws Throwable {

        Integer userId = (Integer) joinPoint.getArgs()[0];

        log.debug("[User 검증] UserId: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        boolean isAdmin = user.getRole() == Role.MASTER || user.getRole() == Role.MANAGER;

        StoreValidationContext.setCurrentUser(user);
        StoreValidationContext.setIsAdmin(isAdmin);

        try {
            return joinPoint.proceed();
        } finally {
            StoreValidationContext.clearCurrentUser();
            StoreValidationContext.clearIsAdmin();
        }
    }

    @Around("@annotation(adminOnly)")
    public Object handleAdminOnly(
            ProceedingJoinPoint joinPoint,
            AdminOnly adminOnly) throws Throwable {

        Integer userId = (Integer) joinPoint.getArgs()[0];

        log.debug("[관리자 권한 검증] UserId: {}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        boolean isAdmin = user.getRole() == Role.MASTER || user.getRole() == Role.MANAGER;

        if (!isAdmin) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }

        StoreValidationContext.setCurrentUser(user);
        StoreValidationContext.setIsAdmin(true);

        try {
            return joinPoint.proceed();
        } finally {
            StoreValidationContext.clearCurrentUser();
            StoreValidationContext.clearIsAdmin();
        }
    }

    @Around("@annotation(validateStoreAuthority)")
    public Object handleValidateStoreAuthority(
            ProceedingJoinPoint joinPoint,
            ValidateStoreAuthority validateStoreAuthority) throws Throwable {

        UUID storeId = (UUID) joinPoint.getArgs()[0];
        Integer userId = (Integer) joinPoint.getArgs()[1];

        log.debug("[매장 권한 검증] StoreId: {}, UserId: {}", storeId, userId);

        // User 조회 및 관리자 여부 확인
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        boolean isAdmin = user.getRole() == Role.MASTER || user.getRole() == Role.MANAGER;

        // Store 조회
        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없거나 접근 권한이 없습니다."));

        // 관리자가 아닌 경우 소유권 검증 (MSA 전환으로 userId로 직접 비교)
        if (!isAdmin) {
            boolean isRealOwner = store.getUsers().stream()
                    .anyMatch(su -> su.getUserId().equals(user.getId()));

            if (!isRealOwner) {
                throw new AccessDeniedException("해당 매장에 대한 관리 권한이 없습니다.");
            }
        }

        // ThreadLocal에 저장
        StoreValidationContext.setCurrentStore(store);
        StoreValidationContext.setCurrentUser(user);
        StoreValidationContext.setIsAdmin(isAdmin);

        try {
            return joinPoint.proceed();
        } finally {
            StoreValidationContext.clearAll();
        }
    }
}
