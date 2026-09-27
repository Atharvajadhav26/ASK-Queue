package com.smartqueue.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String tokenNumber;

    @ManyToOne
    @JoinColumn(name = "queue_id", nullable = false)
    private ServiceQueue queue;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private TokenStatus status = TokenStatus.WAITING;

    @Enumerated(EnumType.STRING)
    private PriorityLevel priority = PriorityLevel.NORMAL;

    @Column(length = 255)
    private String priorityReason;

    private Long priorityAssignedBy;

    private int queuePosition = 0;

    private LocalDateTime joinTime;
    private LocalDateTime calledTime;
    private LocalDateTime servingStartTime;
    private LocalDateTime completedTime;
    private LocalDateTime cancelledTime;
    private LocalDateTime holdStartTime;
    private LocalDateTime holdExpiryTime;

    private Long cancelledBy;

    @Column(length = 255)
    private String cancelReason;

    private int skipCount = 0;

    @Version
    private int version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Token() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.joinTime == null) {
            this.joinTime = this.createdAt;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public ServiceQueue getQueue() { return queue; }
    public void setQueue(ServiceQueue queue) { this.queue = queue; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public TokenStatus getStatus() { return status; }
    public void setStatus(TokenStatus status) { this.status = status; }

    public PriorityLevel getPriority() { return priority; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }

    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }

    public Long getPriorityAssignedBy() { return priorityAssignedBy; }
    public void setPriorityAssignedBy(Long priorityAssignedBy) { this.priorityAssignedBy = priorityAssignedBy; }

    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }

    public LocalDateTime getJoinTime() { return joinTime; }
    public void setJoinTime(LocalDateTime joinTime) { this.joinTime = joinTime; }

    public LocalDateTime getCalledTime() { return calledTime; }
    public void setCalledTime(LocalDateTime calledTime) { this.calledTime = calledTime; }

    public LocalDateTime getServingStartTime() { return servingStartTime; }
    public void setServingStartTime(LocalDateTime servingStartTime) { this.servingStartTime = servingStartTime; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }

    public LocalDateTime getCancelledTime() { return cancelledTime; }
    public void setCancelledTime(LocalDateTime cancelledTime) { this.cancelledTime = cancelledTime; }

    public LocalDateTime getHoldStartTime() { return holdStartTime; }
    public void setHoldStartTime(LocalDateTime holdStartTime) { this.holdStartTime = holdStartTime; }

    public LocalDateTime getHoldExpiryTime() { return holdExpiryTime; }
    public void setHoldExpiryTime(LocalDateTime holdExpiryTime) { this.holdExpiryTime = holdExpiryTime; }

    public Long getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(Long cancelledBy) { this.cancelledBy = cancelledBy; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public int getSkipCount() { return skipCount; }
    public void setSkipCount(int skipCount) { this.skipCount = skipCount; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
