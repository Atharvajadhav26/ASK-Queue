package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class HistoryResponse {
    private String tokenNumber;
    private String queueName;
    private String serviceType;
    private LocalDateTime date;
    private LocalDateTime joinTime;
    private LocalDateTime calledTime;
    private LocalDateTime completedTime;
    private Long waitingTimeMinutes;
    private Long serviceTimeMinutes;
    private String status;

    public HistoryResponse() {}

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
    public LocalDateTime getCalledTime() { return calledTime; }
    public void setCalledTime(LocalDateTime calledTime) { this.calledTime = calledTime; }
    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    public Long getWaitingTimeMinutes() { return waitingTimeMinutes; }
    public void setWaitingTimeMinutes(Long waitingTimeMinutes) { this.waitingTimeMinutes = waitingTimeMinutes; }
    public Long getServiceTimeMinutes() { return serviceTimeMinutes; }
    public void setServiceTimeMinutes(Long serviceTimeMinutes) { this.serviceTimeMinutes = serviceTimeMinutes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
