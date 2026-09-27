package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class TokenResponse {
    private Long id;
    private String tokenNumber;
    private String queueId;
    private String queueName;
    private String userName;
    private String status;
    private String priority;
    private String priorityReason;
    private int queuePosition;
    private int peopleAhead;
    private int estimatedWaitMinutes;
    private LocalDateTime joinTime;
    private LocalDateTime calledTime;
    private LocalDateTime servingStartTime;
    private LocalDateTime completedTime;
    private LocalDateTime holdStartTime;
    private LocalDateTime holdExpiryTime;
    private int skipCount;

    public TokenResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }
    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
    public int getPeopleAhead() { return peopleAhead; }
    public void setPeopleAhead(int peopleAhead) { this.peopleAhead = peopleAhead; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
    public LocalDateTime getCalledTime() { return calledTime; }
    public void setCalledTime(LocalDateTime calledTime) { this.calledTime = calledTime; }
    public LocalDateTime getServingStartTime() { return servingStartTime; }
    public void setServingStartTime(LocalDateTime servingStartTime) { this.servingStartTime = servingStartTime; }
    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    public LocalDateTime getHoldStartTime() { return holdStartTime; }
    public void setHoldStartTime(LocalDateTime holdStartTime) { this.holdStartTime = holdStartTime; }
    public LocalDateTime getHoldExpiryTime() { return holdExpiryTime; }
    public void setHoldExpiryTime(LocalDateTime holdExpiryTime) { this.holdExpiryTime = holdExpiryTime; }
    public int getSkipCount() { return skipCount; }
    public void setSkipCount(int skipCount) { this.skipCount = skipCount; }
}
