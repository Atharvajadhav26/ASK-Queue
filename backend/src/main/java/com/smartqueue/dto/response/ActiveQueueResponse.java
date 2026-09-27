package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class ActiveQueueResponse {
    private Long tokenId;
    private String tokenNumber;
    private String queueId;
    private String queueName;
    private String queueDescription;
    private String currentToken;
    private String status;
    private String priority;
    private int queuePosition;
    private int peopleAhead;
    private int estimatedWaitMinutes;
    private LocalDateTime holdStartTime;
    private LocalDateTime holdExpiryTime;
    private LocalDateTime joinTime;

    public ActiveQueueResponse() {}

    public Long getTokenId() { return tokenId; }
    public void setTokenId(Long tokenId) { this.tokenId = tokenId; }
    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }
    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getQueueDescription() { return queueDescription; }
    public void setQueueDescription(String queueDescription) { this.queueDescription = queueDescription; }
    public String getCurrentToken() { return currentToken; }
    public void setCurrentToken(String currentToken) { this.currentToken = currentToken; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
    public int getPeopleAhead() { return peopleAhead; }
    public void setPeopleAhead(int peopleAhead) { this.peopleAhead = peopleAhead; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }
    public LocalDateTime getHoldStartTime() { return holdStartTime; }
    public void setHoldStartTime(LocalDateTime holdStartTime) { this.holdStartTime = holdStartTime; }
    public LocalDateTime getHoldExpiryTime() { return holdExpiryTime; }
    public void setHoldExpiryTime(LocalDateTime holdExpiryTime) { this.holdExpiryTime = holdExpiryTime; }
    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }
}
