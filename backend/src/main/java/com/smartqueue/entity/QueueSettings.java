package com.smartqueue.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queue_settings")
public class QueueSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "queue_id", nullable = false)
    private ServiceQueue queue;

    private int maxCapacity = 50;
    private int avgTimePerPersonMinutes = 5;
    private int holdTimeMinutes = 10;

    @Column(length = 10)
    private String workingHoursStart = "09:00";

    @Column(length = 10)
    private String workingHoursEnd = "17:00";

    private String holdExpiryBehavior = "EXPIRE";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public QueueSettings() {
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

    public ServiceQueue getQueue() { return queue; }
    public void setQueue(ServiceQueue queue) { this.queue = queue; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public int getAvgTimePerPersonMinutes() { return avgTimePerPersonMinutes; }
    public void setAvgTimePerPersonMinutes(int avgTimePerPersonMinutes) { this.avgTimePerPersonMinutes = avgTimePerPersonMinutes; }

    public int getHoldTimeMinutes() { return holdTimeMinutes; }
    public void setHoldTimeMinutes(int holdTimeMinutes) { this.holdTimeMinutes = holdTimeMinutes; }

    public String getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(String workingHoursStart) { this.workingHoursStart = workingHoursStart; }

    public String getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(String workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }

    public String getHoldExpiryBehavior() { return holdExpiryBehavior; }
    public void setHoldExpiryBehavior(String holdExpiryBehavior) { this.holdExpiryBehavior = holdExpiryBehavior; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
