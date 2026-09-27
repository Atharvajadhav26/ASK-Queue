package com.smartqueue.dto.response;

import java.util.Map;

public class AnalyticsResponse {
    private int activeQueues;
    private int totalCustomersWaiting;
    private int totalCustomersServed;
    private double avgWaitTimeMinutes;
    private double avgServiceTimeMinutes;
    private int completedTokens;
    private int cancelledTokens;
    private int skippedTokens;
    private int heldTokens;
    private int expiredTokens;
    private int priorityCustomers;
    private String peakHour;
    private Map<String, Integer> customersServedPerHour;
    private Map<String, Integer> tokenStatusDistribution;
    private Map<String, Integer> queueVolumeByDay;
    private Map<String, Double> avgWaitTimeByHour;

    public AnalyticsResponse() {}

    public int getActiveQueues() { return activeQueues; }
    public void setActiveQueues(int activeQueues) { this.activeQueues = activeQueues; }
    public int getTotalCustomersWaiting() { return totalCustomersWaiting; }
    public void setTotalCustomersWaiting(int totalCustomersWaiting) { this.totalCustomersWaiting = totalCustomersWaiting; }
    public int getTotalCustomersServed() { return totalCustomersServed; }
    public void setTotalCustomersServed(int totalCustomersServed) { this.totalCustomersServed = totalCustomersServed; }
    public double getAvgWaitTimeMinutes() { return avgWaitTimeMinutes; }
    public void setAvgWaitTimeMinutes(double avgWaitTimeMinutes) { this.avgWaitTimeMinutes = avgWaitTimeMinutes; }
    public double getAvgServiceTimeMinutes() { return avgServiceTimeMinutes; }
    public void setAvgServiceTimeMinutes(double avgServiceTimeMinutes) { this.avgServiceTimeMinutes = avgServiceTimeMinutes; }
    public int getCompletedTokens() { return completedTokens; }
    public void setCompletedTokens(int completedTokens) { this.completedTokens = completedTokens; }
    public int getCancelledTokens() { return cancelledTokens; }
    public void setCancelledTokens(int cancelledTokens) { this.cancelledTokens = cancelledTokens; }
    public int getSkippedTokens() { return skippedTokens; }
    public void setSkippedTokens(int skippedTokens) { this.skippedTokens = skippedTokens; }
    public int getHeldTokens() { return heldTokens; }
    public void setHeldTokens(int heldTokens) { this.heldTokens = heldTokens; }
    public int getExpiredTokens() { return expiredTokens; }
    public void setExpiredTokens(int expiredTokens) { this.expiredTokens = expiredTokens; }
    public int getPriorityCustomers() { return priorityCustomers; }
    public void setPriorityCustomers(int priorityCustomers) { this.priorityCustomers = priorityCustomers; }
    public String getPeakHour() { return peakHour; }
    public void setPeakHour(String peakHour) { this.peakHour = peakHour; }
    public Map<String, Integer> getCustomersServedPerHour() { return customersServedPerHour; }
    public void setCustomersServedPerHour(Map<String, Integer> customersServedPerHour) { this.customersServedPerHour = customersServedPerHour; }
    public Map<String, Integer> getTokenStatusDistribution() { return tokenStatusDistribution; }
    public void setTokenStatusDistribution(Map<String, Integer> tokenStatusDistribution) { this.tokenStatusDistribution = tokenStatusDistribution; }
    public Map<String, Integer> getQueueVolumeByDay() { return queueVolumeByDay; }
    public void setQueueVolumeByDay(Map<String, Integer> queueVolumeByDay) { this.queueVolumeByDay = queueVolumeByDay; }
    public Map<String, Double> getAvgWaitTimeByHour() { return avgWaitTimeByHour; }
    public void setAvgWaitTimeByHour(Map<String, Double> avgWaitTimeByHour) { this.avgWaitTimeByHour = avgWaitTimeByHour; }
}
