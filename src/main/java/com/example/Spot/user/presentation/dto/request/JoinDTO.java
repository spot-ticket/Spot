package com.example.Spot.user.presentation.dto.request;


import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter


public class JoinDTO {
    public enum Role {
        CUSTOMER,
        OWNER,
        CHEF,
        ADMIN;

        public String getAuthority(){
            return "ROLE_" +this.name();
        }
    }


    private String username;
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    private boolean male;
    private int age;
    private String address;
    private Role role;
}

