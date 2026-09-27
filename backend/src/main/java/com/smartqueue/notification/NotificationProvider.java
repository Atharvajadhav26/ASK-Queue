package com.smartqueue.notification;

/**
 * Abstraction for notification delivery channels.
 * Implementations handle specific delivery mechanisms (FCM, Twilio, etc.)
 */
public interface NotificationProvider {

    /**
     * Send a notification to a user.
     * @param userId Target user ID
     * @param deviceToken Device/channel-specific token (FCM token, phone number, etc.)
     * @param title Notification title
     * @param body Notification body/message
     * @return true if sent successfully
     */
    boolean send(Long userId, String deviceToken, String title, String body);

    /**
     * Get the channel name for this provider.
     */
    String getChannel();

    /**
     * Check if this provider is enabled/configured.
     */
    boolean isEnabled();
}
