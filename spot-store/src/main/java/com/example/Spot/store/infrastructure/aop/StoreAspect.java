package com.example.Spot.store.infrastructure.aop;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.example.Spot.global.common.Role;
import com.example.Spot.global.infrastructure.config.security.CustomUserDetails;
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

    private final StoreRepository storeRepository;

    @Around("@annotation(adminOnly)")
    public Object handleAdminOnly(
            ProceedingJoinPoint joinPoint,
            AdminOnly adminOnly) throws Throwable {

        CustomUserDetails principal = extractPrincipal(joinPoint);
        if (principal == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }

        Role role = principal.getRole();
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
        CustomUserDetails principal = extractPrincipal(joinPoint);
        if (principal == null) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }

        Integer userId = (Integer) joinPoint.getArgs()[0];
        Role role = principal.getRole();
        boolean isAdmin = "MASTER".equals(role) || "MANAGER".equals(role);


        log.debug("[매장 권한 검증] StoreId: {}, UserId: {}", storeId, userId);

        StoreEntity store = storeRepository.findByIdWithDetailsWithLock(storeId, isAdmin)
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
    private CustomUserDetails extractPrincipal(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof CustomUserDetails cud) {
                return cud;
            }
        }
        return null;
    }

}
