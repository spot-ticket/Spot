package com.example.Spot.store.presentation.dto.request;

import java.util.List;

import com.example.Spot.user.domain.Role;

public record StoreUserUpdateRequest(
        List<UserChange> changes
) {
   public record UserChange(
           Integer userId,
           Role role,
           Action action
   ) {}
    
    public enum Action {
       ADD, REMOVE
    }
}
