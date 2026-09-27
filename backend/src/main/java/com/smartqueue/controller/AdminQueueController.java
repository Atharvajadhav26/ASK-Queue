package com.smartqueue.controller;

import com.smartqueue.dto.request.CreateQueueRequest;
import com.smartqueue.dto.request.UpdateQueueRequest;
import com.smartqueue.dto.response.QueueDetailResponse;
import com.smartqueue.dto.response.QueueManagementResponse;
import com.smartqueue.dto.response.QueueResponse;
import com.smartqueue.dto.response.TokenResponse;
import com.smartqueue.entity.ServiceQueue;
import com.smartqueue.entity.Token;
import com.smartqueue.mapper.QueueMapper;
import com.smartqueue.mapper.TokenMapper;
import com.smartqueue.repository.TokenRepository;
import com.smartqueue.security.SecurityUtils;
import com.smartqueue.service.NotificationService;
import com.smartqueue.service.QrCodeService;
import com.smartqueue.service.QueueEngine;
import com.smartqueue.service.QueueService;
import com.smartqueue.service.WebSocketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/queues")
public class AdminQueueController {

    private final QueueService queueService;
    private final QueueEngine queueEngine;
    private final TokenRepository tokenRepository;
    private final QrCodeService qrCodeService;
    private final WebSocketService webSocketService;
    private final NotificationService notificationService;
    private final QueueMapper queueMapper;
    private final TokenMapper tokenMapper;

    public AdminQueueController(QueueService queueService, QueueEngine queueEngine, TokenRepository tokenRepository, QrCodeService qrCodeService, WebSocketService webSocketService, NotificationService notificationService, QueueMapper queueMapper, TokenMapper tokenMapper) {
        this.queueService = queueService;
        this.queueEngine = queueEngine;
        this.tokenRepository = tokenRepository;
        this.qrCodeService = qrCodeService;
        this.webSocketService = webSocketService;
        this.notificationService = notificationService;
        this.queueMapper = queueMapper;
        this.tokenMapper = tokenMapper;
    }

    @GetMapping("/")
    public ResponseEntity<List<QueueResponse>> getAdminQueues() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ServiceQueue> queues = queueService.getAdminQueues(userId);
        
        List<QueueResponse> responses = queues.stream().map(queue -> {
            com.smartqueue.entity.QueueSettings settings = queueService.getQueueSettings(queue.getId()).orElse(null);
            int waitingCount = queueEngine.getWaitingCount(queue);
            String currentTokenNumber = queueEngine.getCurrentServingTokenNumber(queue);
            int estimatedWait = queueEngine.getEstimatedWaitForNewJoiner(queue);
            return queueMapper.toQueueResponse(queue, settings, waitingCount, currentTokenNumber, estimatedWait);
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/")
    public ResponseEntity<QueueDetailResponse> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        ServiceQueue queue = queueService.createQueue(request);
        com.smartqueue.entity.QueueSettings settings = queueService.getQueueSettings(queue.getId()).orElse(null);
        QueueDetailResponse response = queueMapper.toQueueDetailResponse(queue, settings, 0, null, 0, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QueueResponse> updateQueue(@PathVariable Long id, @RequestBody UpdateQueueRequest request) {
        ServiceQueue queue = queueService.updateQueue(id, request);
        com.smartqueue.entity.QueueSettings settings = queueService.getQueueSettings(queue.getId()).orElse(null);
        int waitingCount = queueEngine.getWaitingCount(queue);
        String currentTokenNumber = queueEngine.getCurrentServingTokenNumber(queue);
        int estimatedWait = queueEngine.getEstimatedWaitForNewJoiner(queue);
        QueueResponse response = queueMapper.toQueueResponse(queue, settings, waitingCount, currentTokenNumber, estimatedWait);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQueue(@PathVariable Long id) {
        webSocketService.broadcastQueueEvent(id, "QUEUE_DELETED", java.util.Map.of("queueId", id, "message", "Queue has been deleted"));
        queueService.deleteQueue(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<QueueResponse> openQueue(@PathVariable Long id) {
        ServiceQueue queue = queueService.openQueue(id);
        com.smartqueue.entity.QueueSettings settings = queueService.getQueueSettings(queue.getId()).orElse(null);
        int waitingCount = queueEngine.getWaitingCount(queue);
        String currentTokenNumber = queueEngine.getCurrentServingTokenNumber(queue);
        int estimatedWait = queueEngine.getEstimatedWaitForNewJoiner(queue);
        QueueResponse response = queueMapper.toQueueResponse(queue, settings, waitingCount, currentTokenNumber, estimatedWait);
        webSocketService.broadcastQueueEvent(queue.getId(), "QUEUE_OPENED", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<QueueResponse> closeQueue(@PathVariable Long id) {
        ServiceQueue queue = queueService.closeQueue(id);
        List<Token> activeTokens = tokenRepository.findByQueue(queue);
        for (Token t : activeTokens) {
            if (t.getStatus() == com.smartqueue.entity.TokenStatus.WAITING || t.getStatus() == com.smartqueue.entity.TokenStatus.HELD) {
                notificationService.notifyQueueClosed(t);
            }
        }
        
        com.smartqueue.entity.QueueSettings settings = queueService.getQueueSettings(queue.getId()).orElse(null);
        int waitingCount = queueEngine.getWaitingCount(queue);
        String currentTokenNumber = queueEngine.getCurrentServingTokenNumber(queue);
        int estimatedWait = queueEngine.getEstimatedWaitForNewJoiner(queue);
        QueueResponse response = queueMapper.toQueueResponse(queue, settings, waitingCount, currentTokenNumber, estimatedWait);
        
        webSocketService.broadcastQueueEvent(queue.getId(), "QUEUE_CLOSED", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/tokens")
    public ResponseEntity<QueueManagementResponse> getQueueTokens(@PathVariable Long id) {
        ServiceQueue queue = queueService.getQueueById(id);
        List<Token> tokens = tokenRepository.findByQueue(queue);

        int totalWaiting = (int) tokens.stream().filter(t -> t.getStatus() == com.smartqueue.entity.TokenStatus.WAITING).count();
        int totalServed = (int) tokens.stream().filter(t -> t.getStatus() == com.smartqueue.entity.TokenStatus.COMPLETED).count();
        int totalCancelled = (int) tokens.stream().filter(t -> t.getStatus() == com.smartqueue.entity.TokenStatus.CANCELLED).count();
        String currentServingToken = queueEngine.getCurrentServingTokenNumber(queue);

        List<TokenResponse> tokenResponses = tokens.stream().map(token -> {
            int peopleAhead = queueEngine.getPeopleAhead(token);
            int estimatedWait = queueEngine.getEstimatedWaitMinutes(token);
            return tokenMapper.toTokenResponse(token, peopleAhead, estimatedWait);
        }).collect(Collectors.toList());

        QueueManagementResponse response = new QueueManagementResponse();
        response.setQueueId(queue.getQueueId());
        response.setQueueName(queue.getName());
        response.setStatus(queue.getStatus() != null ? queue.getStatus().name() : null);
        response.setCurrentServingToken(currentServingToken);
        response.setTotalWaiting(totalWaiting);
        response.setTotalServed(totalServed);
        response.setTotalCancelled(totalCancelled);
        response.setTokens(tokenResponses);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/next")
    public ResponseEntity<TokenResponse> callNextToken(@PathVariable Long id) {
        Token nextToken = queueEngine.callNextToken(id, SecurityUtils.getCurrentUserId());
        if (nextToken != null) {
            notificationService.notifyYourTurn(nextToken);
            int peopleAhead = queueEngine.getPeopleAhead(nextToken);
            int estimatedWait = queueEngine.getEstimatedWaitMinutes(nextToken);
            TokenResponse response = tokenMapper.toTokenResponse(nextToken, peopleAhead, estimatedWait);
            webSocketService.broadcastQueueEvent(id, "NEXT_TOKEN_CALLED", response);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping("/{id}/previous")
    public ResponseEntity<TokenResponse> previousToken(@PathVariable Long id) {
        Token prevToken = queueEngine.previousToken(id, SecurityUtils.getCurrentUserId());
        if (prevToken != null) {
            int peopleAhead = queueEngine.getPeopleAhead(prevToken);
            int estimatedWait = queueEngine.getEstimatedWaitMinutes(prevToken);
            TokenResponse response = tokenMapper.toTokenResponse(prevToken, peopleAhead, estimatedWait);
            webSocketService.broadcastQueueEvent(id, "PREVIOUS_TOKEN_CALLED", response);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("/{id}/qrcode")
    public ResponseEntity<byte[]> getQrCode(@PathVariable Long id) {
        ServiceQueue queue = queueService.getQueueById(id);
        String link = qrCodeService.getQueueLink(queue.getQueueId());
        byte[] qrCode = qrCodeService.generateQrCode(link);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrCode);
    }
}
