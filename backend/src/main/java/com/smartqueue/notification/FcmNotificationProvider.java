package com.smartqueue.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Firebase Cloud Messaging notification provider.
 * Sends push notifications to mobile/web clients.
 * Only active when firebase.enabled=true and credentials are configured.
 */
@Component
public class FcmNotificationProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(FcmNotificationProvider.class);

    @Value("${firebase.enabled:false}")
    private boolean enabled;

    @Override
    public boolean send(Long userId, String deviceToken, String title, String body) {
        if (!enabled || deviceToken == null || deviceToken.isBlank()) {
            return false;
        }

        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("userId", String.valueOf(userId))
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM notification sent: {}", response);
            return true;
        } catch (Exception e) {
            log.error("Failed to send FCM notification to user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getChannel() {
        return "FCM";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
