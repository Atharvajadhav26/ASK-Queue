package com.smartqueue.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dispatches notifications to all enabled providers.
 * Routes notifications through FCM, WhatsApp, and/or Mock providers
 * based on their availability and the user's preferences.
 */
@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final List<NotificationProvider> providers;

    public NotificationDispatcher(List<NotificationProvider> providers) {
        this.providers = providers;
        log.info("Notification dispatcher initialized with {} providers", providers.size());
        for (NotificationProvider provider : providers) {
            log.info("  Provider: {} (enabled={})", provider.getChannel(), provider.isEnabled());
        }
    }

    /**
     * Dispatch a notification to all enabled providers.
     *
     * @param userId      Target user ID
     * @param deviceToken FCM device token (can be null)
     * @param phoneNumber Phone number for WhatsApp (can be null)
     * @param title       Notification title
     * @param body        Notification body
     */
    public void dispatch(Long userId, String deviceToken, String phoneNumber, String title, String body) {
        for (NotificationProvider provider : providers) {
            if (!provider.isEnabled()) {
                continue;
            }

            try {
                String token;
                switch (provider.getChannel()) {
                    case "FCM":
                        token = deviceToken;
                        break;
                    case "WHATSAPP":
                        token = phoneNumber;
                        break;
                    default:
                        token = deviceToken;
                        break;
                }

                boolean sent = provider.send(userId, token, title, body);
                if (sent) {
                    log.debug("Notification sent via {} to user {}", provider.getChannel(), userId);
                }
            } catch (Exception e) {
                log.error("Failed to dispatch notification via {}: {}", provider.getChannel(), e.getMessage());
            }
        }
    }
}
