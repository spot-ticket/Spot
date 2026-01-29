package com.example.Spot.global.common;

public enum Role {
    ADMIN,
    CUSTOMER,
    OWNER,
    CHEF,
    MANAGER,
    MASTER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
