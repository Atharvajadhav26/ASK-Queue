package com.smartqueue.service;

import com.smartqueue.entity.*;
import com.smartqueue.exception.*;
import com.smartqueue.repository.*;
import com.smartqueue.util.TimeCalculator;
import com.smartqueue.util.TokenNumberGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * QueueEngine — the core business logic controller for all queue operations.
 * This is the single source of truth for queue state.
 *
 * All queue-mutating operations MUST go through this service.
 * Frontend must NEVER be the source of truth for queue state.
 */
@Service
public class QueueEngine {

    private static final Logger log = LoggerFactory.getLogger(QueueEngine.class);

    private static final List<TokenStatus> ACTIVE_STATUSES = List.of(
            TokenStatus.WAITING, TokenStatus.HELD, TokenStatus.CALLED, TokenStatus.SERVING
    );

    private final QueueRepository queueRepository;
    private final QueueSettingsRepository queueSettingsRepository;
    private final TokenRepository tokenRepository;
    private final TokenHistoryRepository tokenHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public QueueEngine(QueueRepository queueRepository,
                       QueueSettingsRepository queueSettingsRepository,
                       TokenRepository tokenRepository,
                       TokenHistoryRepository tokenHistoryRepository,
                       UserRepository userRepository,
                       @org.springframework.context.annotation.Lazy NotificationService notificationService) {
        this.queueRepository = queueRepository;
        this.queueSettingsRepository = queueSettingsRepository;
        this.tokenRepository = tokenRepository;
        this.tokenHistoryRepository = tokenHistoryRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ═══════════════════════════════════════════════════════════════
    // TOKEN GENERATION — Join Queue
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token joinQueue(Long queueId, Long userId) {
        // Acquire pessimistic lock on the queue
        ServiceQueue queue = queueRepository.findByIdWithLock(queueId)
                .orElseThrow(() -> new QueueNotFoundException("Queue not found with id: " + queueId));

        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new QueueClosedException("Queue '" + queue.getName() + "' is currently closed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        QueueSettings settings = queueSettingsRepository.findByQueueId(queue.getId())
                .orElseThrow(() -> new BadRequestException("Queue settings not configured"));

        // Check if user already has an active token in this queue
        List<Token> existingTokens = tokenRepository
                .findByQueueAndUserAndStatusIn(queue, user, ACTIVE_STATUSES);
        if (!existingTokens.isEmpty()) {
            throw new BadRequestException("You already have an active token in this queue");
        }

        // Check capacity
        int currentCount = (int) tokenRepository.countByQueueAndStatusIn(queue, ACTIVE_STATUSES);
        if (currentCount >= settings.getMaxCapacity()) {
            throw new QueueFullException("Queue '" + queue.getName() + "' has reached maximum capacity");
        }

        // Generate token
        int newSeq = queue.getCurrentTokenSeq() + 1;
        queue.setCurrentTokenSeq(newSeq);

        String tokenNumber = TokenNumberGenerator.generate(queue.getPrefix(), newSeq);

        int maxPosition = getMaxActivePosition(queue);
        int newPosition = maxPosition + 1;

        Token token = new Token();
        token.setTokenNumber(tokenNumber);
        token.setQueue(queue);
        token.setUser(user);
        token.setStatus(TokenStatus.WAITING);
        token.setPriority(PriorityLevel.NORMAL);
        token.setQueuePosition(newPosition);
        token.setJoinTime(LocalDateTime.now());

        token = tokenRepository.save(token);
        queueRepository.save(queue);

        recalculatePositions(queue);
        token = tokenRepository.findById(token.getId()).orElse(token);

        recordHistory(token, null, TokenStatus.WAITING, "JOIN", "CUSTOMER", userId,
                "Joined queue", null, token.getQueuePosition());

        log.info("Token {} generated for user {} in queue {}", tokenNumber, userId, queue.getQueueId());
        return token;
    }

    // ═══════════════════════════════════════════════════════════════
    // NEXT TOKEN — Admin calls next customer
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token callNextToken(Long queueId, Long adminId) {
        // Pessimistic lock to prevent concurrent next operations
        ServiceQueue queue = queueRepository.findByIdWithLock(queueId)
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        // Complete current serving token if exists
        if (queue.getCurrentServingTokenId() != null) {
            Token currentToken = tokenRepository.findById(queue.getCurrentServingTokenId())
                    .orElse(null);
            if (currentToken != null && currentToken.getStatus() == TokenStatus.SERVING) {
                completeTokenInternal(currentToken, adminId);
            }
        }

        recalculatePositions(queue);

        List<Token> waitingTokens = tokenRepository
                .findByQueueAndStatusOrderByQueuePositionAsc(queue, TokenStatus.WAITING);

        if (waitingTokens.isEmpty()) {
            throw new BadRequestException("No waiting customers in queue");
        }

        Token nextToken = waitingTokens.get(0);
        int oldPosition = nextToken.getQueuePosition();

        nextToken.setStatus(TokenStatus.SERVING);
        nextToken.setCalledTime(LocalDateTime.now());
        nextToken.setServingStartTime(LocalDateTime.now());
        nextToken = tokenRepository.save(nextToken);

        queue.setCurrentServingTokenId(nextToken.getId());
        queueRepository.save(queue);

        recalculatePositions(queue);

        recordHistory(nextToken, TokenStatus.WAITING, TokenStatus.SERVING, "NEXT", "ADMIN", adminId,
                "Called to serve", oldPosition, 0);

        log.info("Next token called: {} in queue {}", nextToken.getTokenNumber(), queue.getQueueId());
        return nextToken;
    }

    // ═══════════════════════════════════════════════════════════════
    // PREVIOUS TOKEN — Revert to previous customer
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token previousToken(Long queueId, Long adminId) {
        ServiceQueue queue = queueRepository.findByIdWithLock(queueId)
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        Token currentServingToken = null;
        if (queue.getCurrentServingTokenId() != null) {
            currentServingToken = tokenRepository.findById(queue.getCurrentServingTokenId()).orElse(null);
        }

        if (currentServingToken != null && currentServingToken.getStatus() == TokenStatus.SERVING) {
            currentServingToken.setStatus(TokenStatus.WAITING);
            currentServingToken.setCalledTime(null);
            currentServingToken.setServingStartTime(null);
            currentServingToken.setQueuePosition(0);
            tokenRepository.save(currentServingToken);

            recordHistory(currentServingToken, TokenStatus.SERVING, TokenStatus.WAITING,
                    "PREVIOUS", "ADMIN", adminId, "Reverted from SERVING to WAITING via Previous", 0, 1);
        }

        List<Token> completedTokens = tokenRepository.findByQueue(queue).stream()
                .filter(t -> t.getStatus() == TokenStatus.COMPLETED)
                .filter(t -> t.getCompletedTime() != null)
                .sorted((a, b) -> b.getCompletedTime().compareTo(a.getCompletedTime()))
                .collect(Collectors.toList());

        Token restoredToken = null;
        if (!completedTokens.isEmpty()) {
            restoredToken = completedTokens.get(0);
            restoredToken.setStatus(TokenStatus.SERVING);
            restoredToken.setCompletedTime(null);
            restoredToken = tokenRepository.save(restoredToken);

            queue.setCurrentServingTokenId(restoredToken.getId());

            recordHistory(restoredToken, TokenStatus.COMPLETED, TokenStatus.SERVING,
                    "PREVIOUS_RESTORE", "ADMIN", adminId, "Restored from COMPLETED to SERVING via Previous", null, 0);
        } else {
            queue.setCurrentServingTokenId(null);
        }

        queueRepository.save(queue);
        recalculatePositions(queue);

        log.info("Previous operation executed in queue {}", queue.getQueueId());
        return restoredToken != null ? restoredToken : currentServingToken;
    }

    // ═══════════════════════════════════════════════════════════════
    // HOLD TOKEN
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token holdToken(Long tokenId, Long actorId, String actorType) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        if ("CUSTOMER".equals(actorType) && !token.getUser().getId().equals(actorId)) {
            throw new UnauthorizedException("You are not authorized to modify this token");
        }

        if (token.getStatus() != TokenStatus.WAITING) {
            throw new InvalidTokenStateException(
                    "Token can only be held from WAITING state. Current: " + token.getStatus());
        }

        ServiceQueue queue = queueRepository.findByIdWithLock(token.getQueue().getId())
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        QueueSettings settings = queueSettingsRepository.findByQueueId(queue.getId())
                .orElseThrow(() -> new BadRequestException("Queue settings not found"));

        int oldPosition = token.getQueuePosition();

        token.setStatus(TokenStatus.HELD);
        token.setHoldStartTime(LocalDateTime.now());
        token.setHoldExpiryTime(TimeCalculator.calculateHoldExpiry(settings.getHoldTimeMinutes()));
        token = tokenRepository.save(token);

        recalculatePositions(queue);

        recordHistory(token, TokenStatus.WAITING, TokenStatus.HELD, "HOLD", actorType, actorId,
                "Token held, expires at " + token.getHoldExpiryTime(), oldPosition, 0);

        log.info("Token {} held by {} {}", token.getTokenNumber(), actorType, actorId);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════
    // RESUME TOKEN
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token resumeToken(Long tokenId, Long actorId, String actorType) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        if ("CUSTOMER".equals(actorType) && !token.getUser().getId().equals(actorId)) {
            throw new UnauthorizedException("You are not authorized to modify this token");
        }

        if (token.getStatus() != TokenStatus.HELD) {
            throw new InvalidTokenStateException(
                    "Token can only be resumed from HELD state. Current: " + token.getStatus());
        }

        ServiceQueue queue = queueRepository.findByIdWithLock(token.getQueue().getId())
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        int maxPosition = getMaxActivePosition(queue);
        int newPosition = maxPosition + 1;

        token.setStatus(TokenStatus.WAITING);
        token.setQueuePosition(newPosition);
        token.setHoldStartTime(null);
        token.setHoldExpiryTime(null);
        token = tokenRepository.save(token);

        recalculatePositions(queue);
        token = tokenRepository.findById(tokenId).orElse(token);

        recordHistory(token, TokenStatus.HELD, TokenStatus.WAITING, "RESUME", actorType, actorId,
                "Token resumed, placed at position " + token.getQueuePosition(), null, token.getQueuePosition());

        log.info("Token {} resumed by {} {}", token.getTokenNumber(), actorType, actorId);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════
    // SKIP TOKEN (Customer voluntarily moves back)
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token skipToken(Long tokenId, int positions, Long actorId, String actorType) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        if ("CUSTOMER".equals(actorType) && !token.getUser().getId().equals(actorId)) {
            throw new UnauthorizedException("You are not authorized to modify this token");
        }

        if (token.getStatus() != TokenStatus.WAITING) {
            throw new InvalidTokenStateException(
                    "Token can only be skipped from WAITING state. Current: " + token.getStatus());
        }

        if (positions < 1 || positions > 10) {
            throw new BadRequestException("Skip positions must be between 1 and 10");
        }

        ServiceQueue queue = queueRepository.findByIdWithLock(token.getQueue().getId())
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        recalculatePositions(queue);
        token = tokenRepository.findById(tokenId).orElseThrow();

        List<Token> waitingTokens = tokenRepository
                .findByQueueAndStatusOrderByQueuePositionAsc(queue, TokenStatus.WAITING);

        int currentPos = token.getQueuePosition();
        int targetPos = currentPos + positions;

        for (Token t : waitingTokens) {
            if (t.getQueuePosition() > currentPos && t.getQueuePosition() <= targetPos) {
                t.setQueuePosition(t.getQueuePosition() - 1);
                tokenRepository.save(t);
            }
        }

        token.setQueuePosition(targetPos);
        token.setSkipCount(token.getSkipCount() + 1);
        token = tokenRepository.save(token);

        recalculatePositions(queue);
        token = tokenRepository.findById(tokenId).orElse(token);

        recordHistory(token, TokenStatus.WAITING, TokenStatus.WAITING, "SKIP", actorType, actorId,
                "Skipped " + positions + " positions", currentPos, token.getQueuePosition());

        log.info("Token {} skipped {} positions by {} {}", token.getTokenNumber(), positions, actorType, actorId);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════
    // CANCEL TOKEN (Leave Queue)
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token cancelToken(Long tokenId, Long actorId, String actorType, String reason) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        if ("CUSTOMER".equals(actorType) && !token.getUser().getId().equals(actorId)) {
            throw new UnauthorizedException("You are not authorized to modify this token");
        }

        if (token.getStatus() == TokenStatus.COMPLETED || token.getStatus() == TokenStatus.CANCELLED
                || token.getStatus() == TokenStatus.EXPIRED) {
            throw new InvalidTokenStateException(
                    "Token cannot be cancelled from " + token.getStatus() + " state");
        }

        ServiceQueue queue = queueRepository.findByIdWithLock(token.getQueue().getId())
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        TokenStatus oldStatus = token.getStatus();
        int oldPosition = token.getQueuePosition();

        token.setStatus(TokenStatus.CANCELLED);
        token.setCancelledTime(LocalDateTime.now());
        token.setCancelledBy(actorId);
        token.setCancelReason(reason);
        token = tokenRepository.save(token);

        if (queue.getCurrentServingTokenId() != null && queue.getCurrentServingTokenId().equals(tokenId)) {
            queue.setCurrentServingTokenId(null);
            queueRepository.save(queue);
        }

        recalculatePositions(queue);

        recordHistory(token, oldStatus, TokenStatus.CANCELLED, "CANCEL", actorType, actorId,
                reason != null ? reason : "Token cancelled", oldPosition, 0);

        log.info("Token {} cancelled by {} {}", token.getTokenNumber(), actorType, actorId);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════
    // COMPLETE TOKEN
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token completeToken(Long tokenId, Long adminId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        if (token.getStatus() != TokenStatus.SERVING) {
            throw new InvalidTokenStateException(
                    "Token can only be completed from SERVING state. Current: " + token.getStatus());
        }

        ServiceQueue queue = queueRepository.findByIdWithLock(token.getQueue().getId())
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        completeTokenInternal(token, adminId);

        if (queue.getCurrentServingTokenId() != null && queue.getCurrentServingTokenId().equals(tokenId)) {
            queue.setCurrentServingTokenId(null);
            queueRepository.save(queue);
        }

        recalculatePositions(queue);

        log.info("Token {} completed by admin {}", token.getTokenNumber(), adminId);
        return token;
    }

    private void completeTokenInternal(Token token, Long adminId) {
        TokenStatus oldStatus = token.getStatus();
        token.setStatus(TokenStatus.COMPLETED);
        token.setCompletedTime(LocalDateTime.now());
        tokenRepository.save(token);

        recordHistory(token, oldStatus, TokenStatus.COMPLETED, "COMPLETE", "ADMIN", adminId,
                "Service completed", null, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    // SET PRIORITY
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public Token setPriority(Long tokenId, PriorityLevel priority, String reason, Long adminId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new TokenNotFoundException("Token not found"));

        if (token.getStatus() != TokenStatus.WAITING) {
            throw new InvalidTokenStateException(
                    "Priority can only be set for WAITING tokens. Current: " + token.getStatus());
        }

        ServiceQueue queue = queueRepository.findByIdWithLock(token.getQueue().getId())
                .orElseThrow(() -> new QueueNotFoundException("Queue not found"));

        PriorityLevel oldPriority = token.getPriority();
        token.setPriority(priority);
        token.setPriorityReason(reason);
        token.setPriorityAssignedBy(adminId);
        token = tokenRepository.save(token);

        recalculatePositions(queue);
        token = tokenRepository.findById(tokenId).orElse(token);

        recordHistory(token, TokenStatus.WAITING, TokenStatus.WAITING, "PRIORITY", "ADMIN", adminId,
                "Priority changed from " + oldPriority + " to " + priority + ": " + reason,
                token.getQueuePosition(), token.getQueuePosition());

        log.info("Token {} priority set to {} by admin {}", token.getTokenNumber(), priority, adminId);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════
    // HOLD EXPIRY — Called by scheduler
    // ═══════════════════════════════════════════════════════════════

    @Transactional
    public void processExpiredHolds() {
        List<Token> expiredTokens = tokenRepository
                .findByStatusAndHoldExpiryTimeBefore(TokenStatus.HELD, LocalDateTime.now());

        for (Token token : expiredTokens) {
            ServiceQueue queue = queueRepository.findByIdWithLock(token.getQueue().getId())
                    .orElse(null);

            if (queue == null) continue;

            QueueSettings settings = queueSettingsRepository.findByQueueId(queue.getId())
                    .orElse(null);

            String expiryBehavior = settings != null ? settings.getHoldExpiryBehavior() : "EXPIRE";

            if ("MOVE_TO_END".equals(expiryBehavior)) {
                int maxPosition = getMaxActivePosition(queue);
                token.setStatus(TokenStatus.WAITING);
                token.setQueuePosition(maxPosition + 1);
                token.setHoldStartTime(null);
                token.setHoldExpiryTime(null);
                tokenRepository.save(token);

                recordHistory(token, TokenStatus.HELD, TokenStatus.WAITING, "HOLD_EXPIRY_MOVE",
                        "SYSTEM", null, "Hold expired, moved to end of queue", null, maxPosition + 1);
            } else {
                token.setStatus(TokenStatus.EXPIRED);
                tokenRepository.save(token);

                recordHistory(token, TokenStatus.HELD, TokenStatus.EXPIRED, "HOLD_EXPIRY",
                        "SYSTEM", null, "Hold expired", null, 0);
            }

            recalculatePositions(queue);
            log.info("Hold expired for token {}: behavior={}", token.getTokenNumber(), expiryBehavior);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // POSITION & METRICS CALCULATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get the number of people ahead of a given token.
     */
    public int getPeopleAhead(Token token) {
        if (token == null || token.getStatus() != TokenStatus.WAITING) {
            return 0;
        }
        return Math.max(0, token.getQueuePosition() - 1);
    }

    /**
     * Get estimated wait time for a token.
     */
    public int getEstimatedWaitMinutes(Token token) {
        if (token == null) return 0;
        QueueSettings settings = queueSettingsRepository.findByQueueId(token.getQueue().getId())
                .orElse(null);
        int avgTime = settings != null ? settings.getAvgTimePerPersonMinutes() : 5;
        int peopleAhead = getPeopleAhead(token);
        return TimeCalculator.estimateWaitMinutes(peopleAhead, avgTime);
    }

    /**
     * Get the current serving token number for a queue.
     */
    public String getCurrentServingTokenNumber(ServiceQueue queue) {
        if (queue == null || queue.getCurrentServingTokenId() == null) {
            return null;
        }
        return tokenRepository.findById(queue.getCurrentServingTokenId())
                .map(Token::getTokenNumber)
                .orElse(null);
    }

    /**
     * Count people currently waiting in a queue.
     */
    public int getWaitingCount(ServiceQueue queue) {
        if (queue == null) return 0;
        return (int) tokenRepository.countByQueueAndStatus(queue, TokenStatus.WAITING);
    }

    /**
     * Get estimated wait for the next person joining.
     */
    public int getEstimatedWaitForNewJoiner(ServiceQueue queue) {
        if (queue == null) return 0;
        QueueSettings settings = queueSettingsRepository.findByQueueId(queue.getId())
                .orElse(null);
        int avgTime = settings != null ? settings.getAvgTimePerPersonMinutes() : 5;
        int waiting = getWaitingCount(queue);
        return TimeCalculator.estimateWaitMinutes(waiting, avgTime);
    }

    // ═══════════════════════════════════════════════════════════════
    // INTERNAL HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Recalculate dense-rank positions for all WAITING tokens in a queue.
     * Primary order: Priority DESC (EMERGENCY > VIP > NORMAL)
     * Secondary order: queuePosition / joinTime ASC
     * Also triggers 5/3/1 approaching notifications at position thresholds.
     */
    private void recalculatePositions(ServiceQueue queue) {
        List<Token> waitingTokens = tokenRepository
                .findByQueueAndStatusOrderByQueuePositionAsc(queue, TokenStatus.WAITING);

        waitingTokens.sort((a, b) -> {
            int priorityCompare = getPriorityWeight(b.getPriority()) - getPriorityWeight(a.getPriority());
            if (priorityCompare != 0) return priorityCompare;
            return Integer.compare(a.getQueuePosition(), b.getQueuePosition());
        });

        int position = 1;
        for (Token token : waitingTokens) {
            int oldPosition = token.getQueuePosition();
            if (oldPosition != position) {
                token.setQueuePosition(position);
                tokenRepository.save(token);
            }
            // 5/3/1 approaching notifications — fire when crossing threshold downward
            // peopleAhead = position - 1 (position 1 means 0 ahead)
            int peopleAhead = position - 1;
            if ((peopleAhead == 5 || peopleAhead == 3 || peopleAhead == 1) && oldPosition > position) {
                try {
                    notificationService.notifyTurnApproaching(token, peopleAhead);
                } catch (Exception e) {
                    log.warn("Failed to send approaching notification for token {}: {}",
                            token.getTokenNumber(), e.getMessage());
                }
            }
            position++;
        }
    }

    /**
     * Get the maximum position among active (WAITING) tokens.
     */
    private int getMaxActivePosition(ServiceQueue queue) {
        List<Token> waitingTokens = tokenRepository
                .findByQueueAndStatusOrderByQueuePositionAsc(queue, TokenStatus.WAITING);

        return waitingTokens.stream()
                .mapToInt(Token::getQueuePosition)
                .max()
                .orElse(0);
    }

    /**
     * Get numeric weight for priority comparison.
     * Higher weight = higher priority.
     */
    private int getPriorityWeight(PriorityLevel priority) {
        if (priority == null) return 1;
        switch (priority) {
            case EMERGENCY: return 3;
            case VIP: return 2;
            case NORMAL: return 1;
            default: return 1;
        }
    }

    /**
     * Record a token state transition in history.
     */
    private void recordHistory(Token token, TokenStatus from, TokenStatus to,
                               String action, String actionBy, Long actionById,
                               String details, Integer positionBefore, Integer positionAfter) {
        TokenHistory history = new TokenHistory();
        history.setToken(token);
        history.setFromStatus(from);
        history.setToStatus(to);
        history.setAction(action);
        history.setActionBy(actionBy);
        history.setActionById(actionById);
        history.setDetails(details);
        history.setPositionBefore(positionBefore);
        history.setPositionAfter(positionAfter);
        history.setCreatedAt(LocalDateTime.now());
        tokenHistoryRepository.save(history);
    }
}
