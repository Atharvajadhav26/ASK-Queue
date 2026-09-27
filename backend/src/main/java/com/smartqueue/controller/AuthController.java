package com.smartqueue.controller;

import com.smartqueue.dto.request.LoginRequest;
import com.smartqueue.dto.request.RegisterRequest;
import com.smartqueue.dto.response.AuthResponse;
import com.smartqueue.security.SecurityUtils;
import com.smartqueue.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        String email = SecurityUtils.getCurrentUserEmail();
        String role = SecurityUtils.getCurrentUserRole();

        Map<String, Object> response = new HashMap<>();
        response.put("id", userId);
        response.put("email", email);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }
}
