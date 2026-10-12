package com.skillforge.dto;

import com.skillforge.entity.Role;

import java.time.LocalDateTime;

public class RegisterResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;

    public RegisterResponse() {
    }

    public RegisterResponse(
            Long id,
            String fullName,
            String email,
            Role role,
            Boolean active,
            LocalDateTime createdAt) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
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

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}