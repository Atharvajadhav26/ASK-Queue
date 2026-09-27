package com.smartqueue.service;

import com.smartqueue.entity.*;
import com.smartqueue.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User profile service for profile retrieval and updates.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateProfile(Long userId, String name, String mobile, String preferredLanguage,
                               Boolean notificationEmail, Boolean notificationSms, Boolean notificationPush,
                               String currentPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (mobile != null && !mobile.isBlank()) {
            // Check if mobile is already taken by another user
            Optional<User> existing = userRepository.findByMobile(mobile);
            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                throw new RuntimeException("Mobile number already in use");
            }
            user.setMobile(mobile);
        }
        if (preferredLanguage != null) {
            user.setPreferredLanguage(preferredLanguage);
        }
        if (notificationEmail != null) {
            user.setNotificationEmail(notificationEmail);
        }
        if (notificationSms != null) {
            user.setNotificationSms(notificationSms);
        }
        if (notificationPush != null) {
            user.setNotificationPush(notificationPush);
        }

        // Password change
        if (newPassword != null && !newPassword.isBlank()) {
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        user = userRepository.save(user);
        log.info("Profile updated for user {}", userId);
        return user;
    }
}
