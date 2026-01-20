package com.example.Spot.store.presentation.dto.request;

import java.util.List;

public record StoreUserUpdateRequest(
        List<UserChange> changes
) {
   public record UserChange(
           Integer userId,
           String role,
           Action action
   ) {}
    
    public enum Action {
       ADD, REMOVE
    }
}
