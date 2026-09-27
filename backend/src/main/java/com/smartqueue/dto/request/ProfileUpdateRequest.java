package com.smartqueue.dto.request;

public class ProfileUpdateRequest {
    private String name;
    private String mobile;
    private String preferredLanguage;
    private Boolean notificationEmail;
    private Boolean notificationSms;
    private Boolean notificationPush;
    private String currentPassword;
    private String newPassword;

    public ProfileUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public Boolean getNotificationEmail() { return notificationEmail; }
    public void setNotificationEmail(Boolean notificationEmail) { this.notificationEmail = notificationEmail; }
    public Boolean getNotificationSms() { return notificationSms; }
    public void setNotificationSms(Boolean notificationSms) { this.notificationSms = notificationSms; }
    public Boolean getNotificationPush() { return notificationPush; }
    public void setNotificationPush(Boolean notificationPush) { this.notificationPush = notificationPush; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
