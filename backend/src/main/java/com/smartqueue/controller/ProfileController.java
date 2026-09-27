package com.smartqueue.controller;

import com.smartqueue.dto.request.ProfileUpdateRequest;
import com.smartqueue.dto.response.ProfileResponse;
import com.smartqueue.entity.User;
import com.smartqueue.mapper.UserMapper;
import com.smartqueue.repository.UserRepository;
import com.smartqueue.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public ResponseEntity<ProfileResponse> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow();
        ProfileResponse response = userMapper.toProfileResponse(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/")
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody ProfileUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow();
        
        if (request.getName() != null) user.setName(request.getName());
        if (request.getMobile() != null) user.setMobile(request.getMobile());
        if (request.getPreferredLanguage() != null) user.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getNotificationEmail() != null) user.setNotificationEmail(request.getNotificationEmail());
        if (request.getNotificationSms() != null) user.setNotificationSms(request.getNotificationSms());
        if (request.getNotificationPush() != null) user.setNotificationPush(request.getNotificationPush());
        
        // Handle password update if any logic needed
        
        userRepository.save(user);
        
        ProfileResponse response = userMapper.toProfileResponse(user);
        return ResponseEntity.ok(response);
    }
}
