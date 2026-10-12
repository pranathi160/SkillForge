package com.skillforge.dto;

import com.skillforge.entity.Role;

public class LoginResponse {

    private String token;
    private Long id;
    private String fullName;
    private String email;
    private Role role;

    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            Long id,
            String fullName,
            String email,
            Role role) {

        this.token = token;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}