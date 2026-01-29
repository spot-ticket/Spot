package com.example.Spot.order.infrastructure.aop;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.example.Spot.global.feign.dto.MenuOptionResponse;
import com.example.Spot.global.feign.dto.MenuResponse;
import com.example.Spot.global.feign.dto.StoreResponse;
import com.example.Spot.order.domain.entity.OrderEntity;

public class OrderValidationContext {

    private static final ThreadLocal<ContextData> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<OrderEntity> CURRENT_ORDER = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_STORE_ID = new ThreadLocal<>();

    public static class ContextData {
        private StoreResponse storeResponse;
        private Map<UUID, MenuResponse> menuResponseMap = new HashMap<>();
        private Map<UUID, MenuOptionResponse> menuOptionResponseMap = new HashMap<>();

        public StoreResponse getStoreResponse() {
            return storeResponse;
        }

        public void setStoreResponse(StoreResponse storeResponse) {
            this.storeResponse = storeResponse;
        }

        public void addMenuResponse(UUID menuId, MenuResponse menu) {
            menuResponseMap.put(menuId, menu);
        }

        public MenuResponse getMenuResponse(UUID menuId) {
            return menuResponseMap.get(menuId);
        }

        public void addMenuOptionResponse(UUID optionId, MenuOptionResponse option) {
            menuOptionResponseMap.put(optionId, option);
        }

        public MenuOptionResponse getMenuOptionResponse(UUID optionId) {
            return menuOptionResponseMap.get(optionId);
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

    public static StoreResponse getStoreResponse() {
        ContextData data = get();
        return data != null ? data.getStoreResponse() : null;
    }

    public static MenuResponse getMenuResponse(UUID menuId) {
        ContextData data = get();
        return data != null ? data.getMenuResponse(menuId) : null;
    }

    public static MenuOptionResponse getMenuOptionResponse(UUID optionId) {
        ContextData data = get();
        return data != null ? data.getMenuOptionResponse(optionId) : null;
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
