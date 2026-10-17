
package com.skillforge.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.skillforge.entity.User;
import com.skillforge.repository.UserRepository;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // The refresh endpoint validates its own refresh token
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "/auth/refresh".equals(request.getServletPath());
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
            Claims claims = jwtService.parseToken(token);

            // A refresh token must not authenticate normal API requests
            if (!"access".equals(
                    claims.get("tokenType", String.class))) {
                sendError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Access token required"
                );
                return;
            }

            String email = claims.getSubject();

            if (email == null || email.isBlank()) {
                sendError(
                        response,
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "JWT subject is missing"
                );
                return;
            }

            Optional<User> result =
                    userRepository.findByEmail(email);

            if (result.isEmpty()
                    || !Boolean.TRUE.equals(
                            result.get().getActive())) {

                SecurityContextHolder.clearContext();

                sendError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "User account is inactive or unavailable"
                );
                return;
            }

            User user = result.get();

            if (user.getRole() == null) {
                SecurityContextHolder.clearContext();

                sendError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "User role is missing"
                );
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

        } catch (Exception ex) {
            SecurityContextHolder.clearContext();

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired JWT token"
            );
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

        String safeMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        response.getWriter().write(
                "{\"message\":\"" + safeMessage + "\"}"
        );
    }
}
