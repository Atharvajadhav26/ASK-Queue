package com.smartqueue.mapper;

import com.smartqueue.dto.response.ProfileResponse;
import com.smartqueue.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public ProfileResponse toProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        if (user != null) {
            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setMobile(user.getMobile());
            response.setPreferredLanguage(user.getPreferredLanguage());
            response.setNotificationEmail(user.isNotificationEmail());
            response.setNotificationSms(user.isNotificationSms());
            response.setNotificationPush(user.isNotificationPush());
        }
        return response;
    }
}
