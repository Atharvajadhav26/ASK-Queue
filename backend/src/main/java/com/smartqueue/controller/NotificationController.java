package com.smartqueue.controller;

import com.smartqueue.dto.response.NotificationResponse;
import com.smartqueue.entity.Notification;
import com.smartqueue.mapper.NotificationMapper;
import com.smartqueue.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartqueue.security.SecurityUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping("/")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        List<Notification> notifications = notificationService.getUserNotifications(SecurityUtils.getCurrentUserId());
        List<NotificationResponse> responses = notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount() {
        int count = notificationService.getUnreadCount(SecurityUtils.getCurrentUserId());
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok().build();
    }
}
