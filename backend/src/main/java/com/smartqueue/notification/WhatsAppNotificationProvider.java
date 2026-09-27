package com.smartqueue.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Twilio WhatsApp notification provider.
 * Sends WhatsApp messages to customers.
 * Only active when twilio.enabled=true and credentials are configured.
 */
@Component
public class WhatsAppNotificationProvider implements NotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationProvider.class);

    @Value("${twilio.enabled:false}")
    private boolean enabled;

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.whatsapp-from:}")
    private String whatsappFrom;

    @PostConstruct
    public void init() {
        if (enabled && accountSid != null && !accountSid.isBlank()) {
            try {
                Twilio.init(accountSid, authToken);
                log.info("Twilio WhatsApp provider initialized");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio: {}", e.getMessage());
                enabled = false;
            }
        }
    }

    @Override
    public boolean send(Long userId, String phoneNumber, String title, String body) {
        if (!enabled || phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }

        try {
            String whatsappTo = "whatsapp:" + phoneNumber;
            String messageBody = title + "\n\n" + body;

            Message message = Message.creator(
                    new PhoneNumber(whatsappTo),
                    new PhoneNumber(whatsappFrom),
                    messageBody
            ).create();

            log.debug("WhatsApp message sent: SID={}", message.getSid());
            return true;
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to {}: {}", phoneNumber, e.getMessage());
            return false;
        }
    }

    @Override
    public String getChannel() {
        return "WHATSAPP";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
