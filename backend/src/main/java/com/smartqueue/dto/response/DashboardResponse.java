package com.smartqueue.dto.response;

import java.util.List;

public class DashboardResponse {
    private int activeQueues;
    private int customersWaiting;
    private String currentToken;
    private String avgWaitTime;
    private List<QueueResponse> recentQueues;

    public DashboardResponse() {}

    public int getActiveQueues() { return activeQueues; }
    public void setActiveQueues(int activeQueues) { this.activeQueues = activeQueues; }
    public int getCustomersWaiting() { return customersWaiting; }
    public void setCustomersWaiting(int customersWaiting) { this.customersWaiting = customersWaiting; }
    public String getCurrentToken() { return currentToken; }
    public void setCurrentToken(String currentToken) { this.currentToken = currentToken; }
    public String getAvgWaitTime() { return avgWaitTime; }
    public void setAvgWaitTime(String avgWaitTime) { this.avgWaitTime = avgWaitTime; }
    public List<QueueResponse> getRecentQueues() { return recentQueues; }
    public void setRecentQueues(List<QueueResponse> recentQueues) { this.recentQueues = recentQueues; }
}
