package com.smartqueue.controller;

import com.smartqueue.dto.request.CancelRequest;
import com.smartqueue.dto.request.PriorityRequest;
import com.smartqueue.dto.request.SkipRequest;
import com.smartqueue.entity.Token;
import com.smartqueue.repository.TokenRepository;
import com.smartqueue.security.SecurityUtils;
import com.smartqueue.service.NotificationService;
import com.smartqueue.service.QueueEngine;
import com.smartqueue.service.WebSocketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/tokens")
public class AdminTokenController {

    private final QueueEngine queueEngine;
    private final TokenRepository tokenRepository;
    private final WebSocketService webSocketService;
    private final NotificationService notificationService;

    public AdminTokenController(QueueEngine queueEngine, TokenRepository tokenRepository, WebSocketService webSocketService, NotificationService notificationService) {
        this.queueEngine = queueEngine;
        this.tokenRepository = tokenRepository;
        this.webSocketService = webSocketService;
        this.notificationService = notificationService;
    }

    @PostMapping("/{id}/hold")
    public ResponseEntity<Void> holdToken(@PathVariable Long id) {
        Long adminId = SecurityUtils.getCurrentUserId();
        queueEngine.holdToken(id, adminId, "ADMIN");
        Token token = tokenRepository.findById(id).orElseThrow();
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_HELD_BY_ADMIN", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<Void> resumeToken(@PathVariable Long id) {
        queueEngine.resumeToken(id, SecurityUtils.getCurrentUserId(), "ADMIN");
        Token token = tokenRepository.findById(id).orElseThrow();
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_RESUMED_BY_ADMIN", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/skip")
    public ResponseEntity<Void> skipToken(@PathVariable Long id, @Valid @RequestBody SkipRequest request) {
        queueEngine.skipToken(id, request.getPositions() != null ? request.getPositions() : 1, SecurityUtils.getCurrentUserId(), "ADMIN");
        Token token = tokenRepository.findById(id).orElseThrow();
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_SKIPPED_BY_ADMIN", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelToken(@PathVariable Long id, @RequestBody CancelRequest request) {
        queueEngine.cancelToken(id, SecurityUtils.getCurrentUserId(), "ADMIN", request.getReason());
        Token token = tokenRepository.findById(id).orElseThrow();
        notificationService.notifyTokenCancelled(token);
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_CANCELLED_BY_ADMIN", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeToken(@PathVariable Long id) {
        queueEngine.completeToken(id, SecurityUtils.getCurrentUserId());
        Token token = tokenRepository.findById(id).orElseThrow();
        notificationService.notifyServiceCompleted(token);
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_COMPLETED", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/priority")
    public ResponseEntity<Void> setPriority(@PathVariable Long id, @RequestBody PriorityRequest request) {
        queueEngine.setPriority(id, com.smartqueue.entity.PriorityLevel.valueOf(request.getPriority().toUpperCase()), request.getReason(), SecurityUtils.getCurrentUserId());
        Token token = tokenRepository.findById(id).orElseThrow();
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_PRIORITY_UPDATED", id);
        return ResponseEntity.ok().build();
    }
}
