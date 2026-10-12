
package com.skillforge.controller;

import com.skillforge.dto.LoginRequest;
import com.skillforge.dto.LoginResponse;
import com.skillforge.dto.RegisterRequest;
import com.skillforge.dto.RegisterResponse;
import com.skillforge.entity.User;
import com.skillforge.service.AuthServices;
import com.skillforge.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            String token = authService.generateToken(user);

            LoginResponse response = new LoginResponse(
                    token,
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {

            Map<String, String> error = Map.of(
                    "message", ex.getMessage()
            );

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
}
