package com.example.spotstore.store.infrastructure.aop;

import com.example.Spot.store.domain.entity.StoreEntity;
import com.example.Spot.user.domain.entity.UserEntity;


public class StoreValidationContext {

    private static final ThreadLocal<StoreEntity> CURRENT_STORE = new ThreadLocal<>();
    private static final ThreadLocal<UserEntity> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_ADMIN = new ThreadLocal<>();

    // Store 관련 메서드
    public static void setCurrentStore(StoreEntity store) {
        CURRENT_STORE.set(store);
    }

    public static StoreEntity getCurrentStore() {
        return CURRENT_STORE.get();
    }

    public static void clearCurrentStore() {
        CURRENT_STORE.remove();
    }

    // User 관련 메서드
    public static void setCurrentUser(UserEntity user) {
        CURRENT_USER.set(user);
    }

    public static UserEntity getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clearCurrentUser() {
        CURRENT_USER.remove();
    }

    // Admin 플래그 관련 메서드
    public static void setIsAdmin(boolean isAdmin) {
        IS_ADMIN.set(isAdmin);
    }

    public static Boolean getIsAdmin() {
        return IS_ADMIN.get();
    }

    public static void clearIsAdmin() {
        IS_ADMIN.remove();
    }

    // 모든 컨텍스트 클리어
    public static void clearAll() {
        CURRENT_STORE.remove();
        CURRENT_USER.remove();
        IS_ADMIN.remove();
    }
}
