package com.smartqueue.scheduler;

import com.smartqueue.entity.Token;
import com.smartqueue.entity.TokenStatus;
import com.smartqueue.repository.TokenRepository;
import com.smartqueue.service.NotificationService;
import com.smartqueue.service.QueueEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task to process expired holds and send hold-expiring notifications.
 * Runs every 30 seconds.
 */
@Component
public class HoldExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(HoldExpiryScheduler.class);

    private final QueueEngine queueEngine;
    private final TokenRepository tokenRepository;
    private final NotificationService notificationService;

    public HoldExpiryScheduler(QueueEngine queueEngine,
                               TokenRepository tokenRepository,
                               NotificationService notificationService) {
        this.queueEngine = queueEngine;
        this.tokenRepository = tokenRepository;
        this.notificationService = notificationService;
    }

    /**
     * Process expired holds every 30 seconds.
     */
    @Scheduled(fixedRate = 30000)
    public void processExpiredHolds() {
        try {
            queueEngine.processExpiredHolds();
        } catch (Exception e) {
            log.error("Error processing expired holds: {}", e.getMessage());
        }
    }

    /**
     * Send hold-expiring notifications for tokens expiring within 2 minutes.
     * Runs every 60 seconds.
     */
    @Scheduled(fixedRate = 60000)
    public void sendHoldExpiringNotifications() {
        try {
            LocalDateTime twoMinutesFromNow = LocalDateTime.now().plusMinutes(2);
            List<Token> expiringTokens = tokenRepository
                    .findByStatusAndHoldExpiryTimeBefore(TokenStatus.HELD, twoMinutesFromNow);

            for (Token token : expiringTokens) {
                // Only send if expiry is in the future (not already expired)
                if (token.getHoldExpiryTime() != null && token.getHoldExpiryTime().isAfter(LocalDateTime.now())) {
                    notificationService.notifyHoldExpiring(token);
                }
            }
        } catch (Exception e) {
            log.error("Error sending hold expiry notifications: {}", e.getMessage());
        }
    }
}
