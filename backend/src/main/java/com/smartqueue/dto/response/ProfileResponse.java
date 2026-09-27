package com.smartqueue.dto.response;

public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String preferredLanguage;
    private boolean notificationEmail;
    private boolean notificationSms;
    private boolean notificationPush;

    public ProfileResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public boolean isNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(boolean notificationEmail) { this.notificationEmail = notificationEmail; }
    public boolean isNotificationSms() { return notificationSms; }
    public void setNotificationSms(boolean notificationSms) { this.notificationSms = notificationSms; }
    public boolean isNotificationPush() { return notificationPush; }
    public void setNotificationPush(boolean notificationPush) { this.notificationPush = notificationPush; }
}
