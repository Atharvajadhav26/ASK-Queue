package com.smartqueue.mapper;

import com.smartqueue.dto.response.NotificationResponse;
import com.smartqueue.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        if (notification != null) {
            response.setId(notification.getId());
            response.setTitle(notification.getTitle());
            response.setMessage(notification.getMessage());
            response.setType(notification.getType() != null ? notification.getType().name() : null);
            response.setRead(notification.isRead());
            response.setCreatedAt(notification.getCreatedAt());
        }
        return response;
    }
}
