package com.smartqueue.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for broadcasting queue events via WebSocket/STOMP.
 */
@Service
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast a queue update to all subscribers watching a specific queue.
     * Customers and admins subscribe to /topic/queue/{queueId}
     */
    public void broadcastQueueUpdate(Long queueId, Object payload) {
        String destination = "/topic/queue/" + queueId;
        log.debug("Broadcasting queue update to {}", destination);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Broadcast an admin-specific queue update.
     * Admins subscribe to /topic/admin/queue/{queueId}
     */
    public void broadcastAdminQueueUpdate(Long queueId, Object payload) {
        String destination = "/topic/admin/queue/" + queueId;
        log.debug("Broadcasting admin queue update to {}", destination);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Send a notification to a specific user.
     * Uses a user-specific topic since SockJS connections don't carry JWT principal.
     * Frontend subscribes to /topic/user/{userId}/notifications
     */
    public void sendToUser(Long userId, Object payload) {
        String destination = "/topic/user/" + userId + "/notifications";
        log.debug("Sending notification to user {} at {}", userId, destination);
        messagingTemplate.convertAndSend(destination, payload);
    }

    /**
     * Broadcast a generic queue event with action type.
     */
    public void broadcastQueueEvent(Long queueId, String action, Object data) {
        Map<String, Object> event = new java.util.HashMap<>();
        event.put("action", action);
        event.put("queueId", queueId != null ? queueId.toString() : "");
        event.put("data", data != null ? data : java.util.Collections.emptyMap());
        event.put("timestamp", System.currentTimeMillis());
        broadcastQueueUpdate(queueId, event);
        broadcastAdminQueueUpdate(queueId, event);
    }
}
