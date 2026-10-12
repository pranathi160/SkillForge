
package com.skillforge.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.skillforge.entity.User;
import com.skillforge.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7).trim();

            Claims claims = Jwts.parser()
                .verifyWith(
                    Keys.hmacShaKeyFor(
                        secretKey.getBytes(StandardCharsets.UTF_8)
                    )
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String email = claims.getSubject();

            if (email == null || email.isBlank()) {
                sendError(response, 401, "JWT subject is missing");
                return;
            }

            Optional<User> result = userRepository.findByEmail(email);

            if (result.isEmpty()
                    || !Boolean.TRUE.equals(result.get().getActive())) {
                SecurityContextHolder.clearContext();
                sendError(response, 403,
                    "User account is inactive or unavailable");
                return;
            }

            User user = result.get();

            if (user.getRole() == null) {
                SecurityContextHolder.clearContext();
                sendError(response, 403, "User role is missing");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    Collections.singletonList(
                        new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name()
                        )
                    )
                );

            SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        } catch (Exception exception) {
            SecurityContextHolder.clearContext();
            System.err.println(
                "JWT validation failed: " + exception.getMessage()
            );

            sendError(response, 401, "Invalid or expired JWT token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            "{\"message\":\"" + message + "\"}"
        );
    }
}
