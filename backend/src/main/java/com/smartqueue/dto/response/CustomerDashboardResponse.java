package com.smartqueue.dto.response;

import java.util.List;

public class CustomerDashboardResponse {
    private boolean hasActiveQueue;
    private ActiveQueueResponse activeQueue;
    private int unreadNotifications;
    private List<HistoryResponse> recentHistory;

    public CustomerDashboardResponse() {}

    public boolean isHasActiveQueue() { return hasActiveQueue; }
    public void setHasActiveQueue(boolean hasActiveQueue) { this.hasActiveQueue = hasActiveQueue; }
    public ActiveQueueResponse getActiveQueue() { return activeQueue; }
    public void setActiveQueue(ActiveQueueResponse activeQueue) { this.activeQueue = activeQueue; }
    public int getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(int unreadNotifications) { this.unreadNotifications = unreadNotifications; }
    public List<HistoryResponse> getRecentHistory() { return recentHistory; }
    public void setRecentHistory(List<HistoryResponse> recentHistory) { this.recentHistory = recentHistory; }
}
