
package com.skillforge.service;

import com.skillforge.dto.RegisterRequest;
import com.skillforge.dto.UpdateProfileRequest;
import com.skillforge.dto.UserProfileResponse;
import com.skillforge.dto.ChangePasswordRequest;
import com.skillforge.entity.User;
import com.skillforge.entity.Role;
import com.skillforge.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {

        String fullName = request.getFullName().trim();
        String email = request.getEmail().trim();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        String encryptedPassword =
                passwordEncoder.encode(request.getPassword());

        User user = new User(
                null,
                fullName,
                email,
                encryptedPassword,
                Role.LEARNER,
                true,
                null
        );

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return convertToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(
            String currentEmail,
            UpdateProfileRequest request) {

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String newEmail = request.getEmail().trim();

        if (!newEmail.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("Email already registered");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(newEmail);

        User savedUser = userRepository.save(user);
        return convertToProfileResponse(savedUser);
    }

    @Transactional
    public void changePassword(
            String email,
            ChangePasswordRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    @Transactional
    public void deactivateAccount(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setActive(false);
        userRepository.save(user);
    }

    private UserProfileResponse convertToProfileResponse(User user) {

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt()
        );
    }
}
