package com.smartqueue.controller;

import com.smartqueue.dto.request.SkipRequest;
import com.smartqueue.dto.response.ActiveQueueResponse;
import com.smartqueue.dto.response.HistoryResponse;
import com.smartqueue.entity.Token;
import com.smartqueue.entity.TokenStatus;
import com.smartqueue.mapper.TokenMapper;
import com.smartqueue.repository.TokenRepository;
import com.smartqueue.security.SecurityUtils;
import com.smartqueue.service.NotificationService;
import com.smartqueue.service.QueueEngine;
import com.smartqueue.service.WebSocketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {
    
    private final TokenRepository tokenRepository;
    private final QueueEngine queueEngine;
    private final WebSocketService webSocketService;
    private final NotificationService notificationService;
    private final TokenMapper tokenMapper;

    public TokenController(TokenRepository tokenRepository, QueueEngine queueEngine, WebSocketService webSocketService, NotificationService notificationService, TokenMapper tokenMapper) {
        this.tokenRepository = tokenRepository;
        this.queueEngine = queueEngine;
        this.webSocketService = webSocketService;
        this.notificationService = notificationService;
        this.tokenMapper = tokenMapper;
    }

    @GetMapping("/active")
    public ResponseEntity<List<ActiveQueueResponse>> getActiveTokens() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<TokenStatus> activeStatuses = Arrays.asList(TokenStatus.WAITING, TokenStatus.HELD, TokenStatus.CALLED, TokenStatus.SERVING);
        List<Token> tokens = tokenRepository.findByUserIdAndStatusIn(userId, activeStatuses);
        
        List<ActiveQueueResponse> responses = tokens.stream().map(token -> {
            String currentToken = queueEngine.getCurrentServingTokenNumber(token.getQueue());
            int peopleAhead = queueEngine.getPeopleAhead(token);
            int estimatedWait = queueEngine.getEstimatedWaitMinutes(token);
            return tokenMapper.toActiveQueueResponse(token, currentToken, peopleAhead, estimatedWait);
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/history")
    public ResponseEntity<List<HistoryResponse>> getTokenHistory() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<TokenStatus> historyStatuses = Arrays.asList(TokenStatus.COMPLETED, TokenStatus.CANCELLED);
        List<Token> tokens = tokenRepository.findByUserIdAndStatusIn(userId, historyStatuses);
        
        List<HistoryResponse> responses = tokens.stream()
                .map(tokenMapper::toHistoryResponse)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{tokenId}/hold")
    public ResponseEntity<Void> holdToken(@PathVariable Long tokenId) {
        Long userId = SecurityUtils.getCurrentUserId();
        queueEngine.holdToken(tokenId, userId, "CUSTOMER");
        Token token = tokenRepository.findById(tokenId).orElseThrow();
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_HELD", token.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{tokenId}/resume")
    public ResponseEntity<Void> resumeToken(@PathVariable Long tokenId) {
        queueEngine.resumeToken(tokenId, SecurityUtils.getCurrentUserId(), "CUSTOMER");
        Token token = tokenRepository.findById(tokenId).orElseThrow();
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_RESUMED", token.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{tokenId}/skip")
    public ResponseEntity<Void> skipToken(@PathVariable Long tokenId, @Valid @RequestBody SkipRequest request) {
        queueEngine.skipToken(tokenId, request.getPositions() != null ? request.getPositions() : 1, SecurityUtils.getCurrentUserId(), "CUSTOMER");
        Token token = tokenRepository.findById(tokenId).orElseThrow();
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_SKIPPED", token.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{tokenId}/leave")
    public ResponseEntity<Void> leaveQueue(@PathVariable Long tokenId) {
        queueEngine.cancelToken(tokenId, SecurityUtils.getCurrentUserId(), "CUSTOMER", "Customer left the queue");
        Token token = tokenRepository.findById(tokenId).orElseThrow();
        notificationService.notifyTokenCancelled(token);
        webSocketService.broadcastQueueEvent(token.getQueue().getId(), "TOKEN_CANCELLED", token.getId());
        return ResponseEntity.ok().build();
    }
}
