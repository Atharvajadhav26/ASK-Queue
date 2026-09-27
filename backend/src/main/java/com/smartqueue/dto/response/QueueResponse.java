package com.smartqueue.dto.response;

import java.time.LocalDateTime;

public class QueueResponse {
    private Long id;
    private String queueId;
    private String name;
    private String serviceType;
    private String description;
    private String status;
    private String currentTokenNumber;
    private int peopleWaiting;
    private int maxCapacity;
    private int estimatedWaitMinutes;
    private LocalDateTime createdAt;

    public QueueResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQueueId() { return queueId; }
    public void setQueueId(String queueId) { this.queueId = queueId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentTokenNumber() { return currentTokenNumber; }
    public void setCurrentTokenNumber(String currentTokenNumber) { this.currentTokenNumber = currentTokenNumber; }
    public int getPeopleWaiting() { return peopleWaiting; }
    public void setPeopleWaiting(int peopleWaiting) { this.peopleWaiting = peopleWaiting; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
