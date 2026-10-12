
package com.skillforge.controller;

import com.skillforge.dto.ChangePasswordRequest;
import com.skillforge.dto.UpdateProfileRequest;
import com.skillforge.dto.UserProfileResponse;
import com.skillforge.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get profile
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            Authentication authentication) {

        return ResponseEntity.ok(
                userService.getUserProfile(authentication.getName()));
    }

    // Update profile
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                userService.updateProfile(
                        authentication.getName(), request));
    }

    // Change password
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        userService.changePassword(
                authentication.getName(), request);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully"));
    }

    // Deactivate account
    @PutMapping("/deactivate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deactivateAccount(
            Authentication authentication) {

        userService.deactivateAccount(authentication.getName());

        return ResponseEntity.ok(
                Map.of("message", "Account deactivated successfully"));
    }

    // Admin-only endpoint
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> adminOnly(
            Authentication authentication) {

        return ResponseEntity.ok(
                "Welcome Admin! Logged in as "
                        + authentication.getName());
    }
}
