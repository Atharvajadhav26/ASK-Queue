package com.smartqueue.dto.response;

public class QueueDetailResponse extends QueueResponse {
    private int avgTimePerPerson;
    private int holdTimeMinutes;
    private String workingHoursStart;
    private String workingHoursEnd;
    private String holdExpiryBehavior;
    private String qrCodeUrl;
    private String queueLink;

    public QueueDetailResponse() { super(); }

    public int getAvgTimePerPerson() { return avgTimePerPerson; }
    public void setAvgTimePerPerson(int avgTimePerPerson) { this.avgTimePerPerson = avgTimePerPerson; }
    public int getHoldTimeMinutes() { return holdTimeMinutes; }
    public void setHoldTimeMinutes(int holdTimeMinutes) { this.holdTimeMinutes = holdTimeMinutes; }
    public String getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(String workingHoursStart) { this.workingHoursStart = workingHoursStart; }
    public String getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(String workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }
    public String getHoldExpiryBehavior() { return holdExpiryBehavior; }
    public void setHoldExpiryBehavior(String holdExpiryBehavior) { this.holdExpiryBehavior = holdExpiryBehavior; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public String getQueueLink() { return queueLink; }
    public void setQueueLink(String queueLink) { this.queueLink = queueLink; }
}
