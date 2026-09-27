package com.smartqueue.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mock notification provider for development/testing.
 * Logs notifications to console instead of sending to external services.
 * Always enabled when no external providers are configured.
 */
@Component
public class MockNotificationProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationProvider.class);

    @Override
    public boolean send(Long userId, String deviceToken, String title, String body) {
        log.info("═══════════════════════════════════════════════");
        log.info("  MOCK NOTIFICATION");
        log.info("  User ID:  {}", userId);
        log.info("  Title:    {}", title);
        log.info("  Body:     {}", body);
        log.info("  Token:    {}", deviceToken != null ? deviceToken.substring(0, Math.min(20, deviceToken.length())) + "..." : "N/A");
        log.info("═══════════════════════════════════════════════");
        return true;
    }

    @Override
    public String getChannel() {
        return "MOCK";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
