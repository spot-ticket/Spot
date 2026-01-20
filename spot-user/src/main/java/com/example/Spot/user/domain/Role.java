package com.example.Spot.user.domain;

public enum Role {
    CUSTOMER,
    OWNER,
    CHEF,
    MANAGER,
    MASTER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
