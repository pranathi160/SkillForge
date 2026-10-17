
package com.skillforge.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.skillforge.dto.LoginRequest;
import com.skillforge.dto.RegisterRequest;
import com.skillforge.dto.RegisterResponse;
import com.skillforge.entity.User;
import com.skillforge.service.AuthServices;
import com.skillforge.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthServices authService;

    public AuthController(
            UserService userService,
            AuthServices authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // Register
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.registerUser(request);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            User user = authService.login(request);

            String accessToken = authService.generateToken(user);
            String refreshToken =
                    authService.generateRefreshToken(user);

            Map<String, Object> response = new HashMap<>();

            response.put("token", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("userId", user.getId());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {

            Map<String, String> error =
                    Map.of("message", ex.getMessage());

            if ("User account is inactive".equals(ex.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(error);
            }

            if ("Invalid email or password".equals(ex.getMessage())) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(error);
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Login failed"));
        }
    }

    // Refresh access token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Refresh token is required"
                    ));
        }

        try {
            return ResponseEntity.ok(
                    authService.refreshAccessToken(refreshToken)
            );

        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Refresh token is invalid or expired"
                    ));
        }
    }
}
