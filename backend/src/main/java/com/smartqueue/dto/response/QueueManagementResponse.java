package com.smartqueue.dto.response;

import java.util.List;

public class QueueManagementResponse {
    private String queueId;
    private String queueName;
    private String status;
    private String currentServingToken;
    private int totalWaiting;
    private int totalServed;
    private int totalCancelled;
    private List<TokenResponse> tokens;

    public QueueManagementResponse() {}

    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentServingToken() { return currentServingToken; }
    public void setCurrentServingToken(String currentServingToken) { this.currentServingToken = currentServingToken; }
    public int getTotalWaiting() { return totalWaiting; }
    public void setTotalWaiting(int totalWaiting) { this.totalWaiting = totalWaiting; }
    public int getTotalServed() { return totalServed; }
    public void setTotalServed(int totalServed) { this.totalServed = totalServed; }
    public int getTotalCancelled() { return totalCancelled; }
    public void setTotalCancelled(int totalCancelled) { this.totalCancelled = totalCancelled; }
    public List<TokenResponse> getTokens() { return tokens; }
    public void setTokens(List<TokenResponse> tokens) { this.tokens = tokens; }
}
