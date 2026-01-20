package com.example.Spot.store.infrastructure.aop;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.example.Spot.global.feign.UserClient;
import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.store.domain.repository.StoreRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class StoreAspect {

    // User Roll을 확인하는 로직이 너무 많음
    
    private final UserClient userClient;
    private final StoreRepository storeRepository;

    @Around("@annotation(adminOnly)")
    public Object handleAdminOnly(
            ProceedingJoinPoint joinPoint,
            AdminOnly adminOnly) throws Throwable {

        Integer userId = (Integer) joinPoint.getArgs()[0];

        log.debug("[관리자 권한 검증] UserId: {}", userId);

        String role = userClient.getUser(userId).getRole();
        boolean isAdmin = "MASTER".equals(role) || "MANAGER".equals(role);

        if (!isAdmin) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }

        StoreValidationContext.setIsAdmin(true);

        try {
            return joinPoint.proceed();
        } finally {
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

        String role = userClient.getUser(userId).getRole();
        boolean isAdmin = "MANAGER".equals(role) || "MASTER".equals(role);

        StoreEntity store = storeRepository.findByIdWithDetails(storeId, isAdmin)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없거나 접근 권한이 없습니다."));

        if (!isAdmin) {
            boolean isRealOwner = store.getUsers().stream()
                    .anyMatch(su -> su.getUserId().equals(userId));

            if (!isRealOwner) {
                throw new AccessDeniedException("해당 매장에 대한 관리 권한이 없습니다.");
            }
        }

        StoreValidationContext.setCurrentStore(store);
        StoreValidationContext.setIsAdmin(isAdmin);

        try {
            return joinPoint.proceed();
        } finally {
            StoreValidationContext.clearAll();
        }
    }
}
