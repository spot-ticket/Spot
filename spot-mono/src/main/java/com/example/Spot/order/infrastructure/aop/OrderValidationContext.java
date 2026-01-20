package com.example.Spot.order.infrastructure.aop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.Spot.menu.domain.entity.MenuEntity;
import com.example.Spot.menu.domain.entity.MenuOptionEntity;
import com.example.Spot.order.domain.entity.OrderEntity;
import com.example.Spot.store.domain.entity.StoreEntity;

public class OrderValidationContext {

    private static final ThreadLocal<ContextData> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<OrderEntity> CURRENT_ORDER = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_STORE_ID = new ThreadLocal<>();

    public static class ContextData {
        private StoreEntity store;
        private Map<UUID, MenuEntity> menuMap = new HashMap<>();
        private Map<UUID, MenuOptionEntity> menuOptionMap = new HashMap<>();

        public StoreEntity getStore() {
            return store;
        }

        public void setStore(StoreEntity store) {
            this.store = store;
        }

        public void addMenu(UUID menuId, MenuEntity menu) {
            menuMap.put(menuId, menu);
        }

        public MenuEntity getMenu(UUID menuId) {
            return menuMap.get(menuId);
        }

        public void addMenuOption(UUID optionId, MenuOptionEntity option) {
            menuOptionMap.put(optionId, option);
        }

        public MenuOptionEntity getMenuOption(UUID optionId) {
            return menuOptionMap.get(optionId);
        }
    }

    public static void set(ContextData data) {
        CONTEXT.set(data);
    }

    public static ContextData get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static StoreEntity getStore() {
        ContextData data = get();
        return data != null ? data.getStore() : null;
    }

    public static MenuEntity getMenu(UUID menuId) {
        ContextData data = get();
        return data != null ? data.getMenu(menuId) : null;
    }

    public static MenuOptionEntity getMenuOption(UUID optionId) {
        ContextData data = get();
        return data != null ? data.getMenuOption(optionId) : null;
    }

    // 주문 상태 변경용 메서드
    public static void setCurrentOrder(OrderEntity order) {
        CURRENT_ORDER.set(order);
    }

    public static OrderEntity getCurrentOrder() {
        return CURRENT_ORDER.get();
    }

    public static void clearCurrentOrder() {
        CURRENT_ORDER.remove();
    }

    // 가게 소유권 검증용 메서드
    public static void setCurrentStoreId(UUID storeId) {
        CURRENT_STORE_ID.set(storeId);
    }

    public static UUID getCurrentStoreId() {
        return CURRENT_STORE_ID.get();
    }

    public static void clearCurrentStoreId() {
        CURRENT_STORE_ID.remove();
    }
}
