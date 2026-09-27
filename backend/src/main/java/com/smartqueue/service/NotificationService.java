package com.smartqueue.service;

import com.smartqueue.entity.*;
import com.smartqueue.notification.NotificationDispatcher;
import com.smartqueue.repository.NotificationRepository;
import com.smartqueue.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for creating, storing, and dispatching notifications.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationDispatcher dispatcher;
    private final WebSocketService webSocketService;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               NotificationDispatcher dispatcher,
                               WebSocketService webSocketService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.dispatcher = dispatcher;
        this.webSocketService = webSocketService;
    }

    /**
     * Create and send a notification to a user.
     */
    @Transactional
    public Notification sendNotification(Long userId, Long tokenId, Long queueId,
                                          String title, String message, NotificationType type) {
        // Save to database
        Notification notification = new Notification();
        notification.setUser(userRepository.findById(userId).orElse(null));
        notification.setTokenId(tokenId);
        notification.setQueueId(queueId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setChannel("IN_APP");
        notification.setCreatedAt(LocalDateTime.now());
        notification = notificationRepository.save(notification);

        // Send via WebSocket for real-time delivery
        try {
            webSocketService.sendToUser(userId, notification);
        } catch (Exception e) {
            log.warn("Failed to send WebSocket notification to user {}: {}", userId, e.getMessage());
        }

        // Dispatch to external providers (FCM, WhatsApp)
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                dispatcher.dispatch(userId, user.getFcmToken(), user.getMobile(), title, message);
            }
        } catch (Exception e) {
            log.warn("Failed to dispatch external notification for user {}: {}", userId, e.getMessage());
        }

        return notification;
    }

    /**
     * Send queue joined notification.
     */
    public void notifyQueueJoined(Token token) {
        sendNotification(
                token.getUser().getId(),
                token.getId(),
                token.getQueue().getId(),
                "Queue Joined",
                "Your queue token " + token.getTokenNumber() + " has been generated successfully for " + token.getQueue().getName(),
                NotificationType.QUEUE_JOINED
        );
    }

    /**
     * Send turn approaching notification with people remaining count.
     */
    public void notifyTurnApproaching(Token token, int peopleAhead) {
        String message;
        if (peopleAhead == 1) {
            message = "Your turn is approaching. 1 customer remains ahead of you.";
        } else {
            message = "Only " + peopleAhead + " customers are ahead of you.";
        }
        sendNotification(
                token.getUser().getId(),
                token.getId(),
                token.getQueue().getId(),
                "Turn Approaching",
                message,
                NotificationType.TURN_APPROACHING
        );
    }

    /**
     * Send your-turn notification.
     */
    public void notifyYourTurn(Token token) {
        sendNotification(
                token.getUser().getId(),
                token.getId(),
                token.getQueue().getId(),
                "Your Turn!",
                "Your turn has arrived. Please proceed to the counter.",
                NotificationType.YOUR_TURN
        );
    }

    /**
     * Send queue closed notification to a user.
     */
    public void notifyQueueClosed(Token token) {
        sendNotification(
                token.getUser().getId(),
                token.getId(),
                token.getQueue().getId(),
                "Queue Closed",
                "Queue '" + token.getQueue().getName() + "' has been closed.",
                NotificationType.QUEUE_CLOSED
        );
    }

    /**
     * Send hold expiring notification.
     */
    public void notifyHoldExpiring(Token token) {
        sendNotification(
                token.getUser().getId(),
                token.getId(),
                token.getQueue().getId(),
                "Hold Expiring",
                "Your hold on token " + token.getTokenNumber() + " will expire in 2 minutes. Please resume your position.",
                NotificationType.HOLD_EXPIRING
        );
    }

    /**
     * Send token cancelled notification.
     */
    public void notifyTokenCancelled(Token token) {
        sendNotification(
                token.getUser().getId(),
                token.getId(),
                token.getQueue().getId(),
                "Token Cancelled",
                "Your token " + token.getTokenNumber() + " has been cancelled.",
                NotificationType.TOKEN_CANCELLED
        );
    }

    /**
     * Send service completed notification.
     */
    public void notifyServiceCompleted(Token token) {
        sendNotification(
                token.getUser().getId(),
                token.getId(),
                token.getQueue().getId(),
                "Service Completed",
                "Your service for token " + token.getTokenNumber() + " has been completed. Thank you!",
                NotificationType.TOKEN_COMPLETED
        );
    }

    /**
     * Get all notifications for a user.
     */
    public List<Notification> getUserNotifications(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get unread notification count.
     */
    public int getUnreadCount(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0;
        return (int) notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Mark a notification as read.
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getUser().getId().equals(userId)) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    /**
     * Mark all notifications as read for a user.
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        for (Notification notification : unread) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
}
