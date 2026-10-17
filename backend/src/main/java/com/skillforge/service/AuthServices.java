
package com.skillforge.service;

import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.skillforge.dto.LoginRequest;
import com.skillforge.entity.User;
import com.skillforge.repository.UserRepository;
import com.skillforge.security.JwtService;

import io.jsonwebtoken.Claims;

@Service
public class AuthServices {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthServices(
            UserRepository userRepository,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    // Validate login credentials
    public User login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        ));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException(
                    "User account is inactive"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        if (user.getRole() == null) {
            throw new RuntimeException(
                    "User role is missing"
            );
        }

        return user;
    }

    // Generate access token
    public String generateToken(User user) {
        return jwtService.generateToken(user);
    }

    // Generate refresh token
    public String generateRefreshToken(User user) {
        return jwtService.generateRefreshToken(user);
    }

    // Validate refresh token and generate a new access token
    public Map<String, String> refreshAccessToken(
            String refreshToken) {

        try {
            Claims claims = jwtService.parseToken(refreshToken);

            if (!"refresh".equals(
                    claims.get("tokenType", String.class))) {
                throw new RuntimeException("Invalid token type");
            }

            String email = claims.getSubject();

            if (email == null || email.isBlank()) {
                throw new RuntimeException("Invalid token subject");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));

            if (!Boolean.TRUE.equals(user.getActive())) {
                throw new RuntimeException("User account is inactive");
            }

            if (user.getRole() == null) {
                throw new RuntimeException("User role is missing");
            }

            String newAccessToken = jwtService.generateToken(user);

            return Map.of("token", newAccessToken);

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Refresh token is invalid or expired"
            );
        }
    }
}
