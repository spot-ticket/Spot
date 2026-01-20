package com.example.Spot.store.infrastructure.aop;

import com.example.Spot.store.domain.entity.StoreEntity;

public class StoreValidationContext {

    private static final ThreadLocal<StoreEntity> CURRENT_STORE = new ThreadLocal<>();
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
        IS_ADMIN.remove();
    }
}
