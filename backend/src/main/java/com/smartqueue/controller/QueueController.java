package com.smartqueue.controller;

import com.smartqueue.dto.response.QueueDetailResponse;
import com.smartqueue.dto.response.TokenResponse;
import com.smartqueue.entity.ServiceQueue;
import com.smartqueue.entity.Token;
import com.smartqueue.mapper.QueueMapper;
import com.smartqueue.mapper.TokenMapper;
import com.smartqueue.security.SecurityUtils;
import com.smartqueue.service.NotificationService;
import com.smartqueue.service.QueueEngine;
import com.smartqueue.service.QueueService;
import com.smartqueue.service.WebSocketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;
    private final QueueEngine queueEngine;
    private final NotificationService notificationService;
    private final WebSocketService webSocketService;
    private final QueueMapper queueMapper;
    private final TokenMapper tokenMapper;

    public QueueController(QueueService queueService, QueueEngine queueEngine, NotificationService notificationService, WebSocketService webSocketService, QueueMapper queueMapper, TokenMapper tokenMapper) {
        this.queueService = queueService;
        this.queueEngine = queueEngine;
        this.notificationService = notificationService;
        this.webSocketService = webSocketService;
        this.queueMapper = queueMapper;
        this.tokenMapper = tokenMapper;
    }

    @GetMapping("/{queueId}")
    public ResponseEntity<QueueDetailResponse> getQueue(@PathVariable String queueId) {
        ServiceQueue queue = queueService.getQueueByQueueId(queueId);
        com.smartqueue.entity.QueueSettings settings = queueService.getQueueSettings(queue.getId()).orElse(null);
        int waitingCount = queueEngine.getWaitingCount(queue);
        String currentTokenNumber = queueEngine.getCurrentServingTokenNumber(queue);
        int estimatedWait = queueEngine.getEstimatedWaitForNewJoiner(queue);
        
        QueueDetailResponse response = queueMapper.toQueueDetailResponse(queue, settings, waitingCount, currentTokenNumber, estimatedWait, null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{queueId}/join")
    public ResponseEntity<TokenResponse> joinQueue(@PathVariable String queueId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ServiceQueue queue = queueService.getQueueByQueueId(queueId);
        Token token = queueEngine.joinQueue(queue.getId(), userId);
        
        notificationService.notifyQueueJoined(token);
        
        int peopleAhead = queueEngine.getPeopleAhead(token);
        int estimatedWait = queueEngine.getEstimatedWaitMinutes(token);
        
        TokenResponse response = tokenMapper.toTokenResponse(token, peopleAhead, estimatedWait);
        webSocketService.broadcastQueueEvent(queue.getId(), "TOKEN_JOINED", response);
        return ResponseEntity.ok(response);
    }
}
