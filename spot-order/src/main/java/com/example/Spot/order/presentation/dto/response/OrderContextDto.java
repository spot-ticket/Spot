package com.example.Spot.order.presentation.dto.response;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import com.example.Spot.global.feign.dto.MenuOptionResponse;
import com.example.Spot.global.feign.dto.MenuResponse;
import com.example.Spot.global.feign.dto.StoreResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderContextDto implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private StoreResponse store;
    private Map<UUID, MenuResponse> menuMap;
    private Map<UUID, MenuOptionResponse> optionMap;
}
