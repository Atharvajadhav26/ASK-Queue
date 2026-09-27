package com.smartqueue.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_queues")
public class ServiceQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, unique = true, nullable = false)
    private String queueId;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String serviceType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 10)
    private String prefix = "Q";

    @Enumerated(EnumType.STRING)
    private QueueStatus status = QueueStatus.CLOSED;

    private int currentTokenSeq = 0;
    
    private Long currentServingTokenId;

    @Column(nullable = false)
    private Long createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ServiceQueue() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public QueueStatus getStatus() { return status; }
    public void setStatus(QueueStatus status) { this.status = status; }

    public int getCurrentTokenSeq() { return currentTokenSeq; }
    public void setCurrentTokenSeq(int currentTokenSeq) { this.currentTokenSeq = currentTokenSeq; }

    public Long getCurrentServingTokenId() { return currentServingTokenId; }
    public void setCurrentServingTokenId(Long currentServingTokenId) { this.currentServingTokenId = currentServingTokenId; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
