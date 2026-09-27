package com.smartqueue.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_history")
public class TokenHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "token_id", nullable = false)
    private Token token;

    @Enumerated(EnumType.STRING)
    private TokenStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus toStatus;

    @Column(length = 50, nullable = false)
    private String action;

    @Column(length = 20)
    private String actionBy = "SYSTEM";

    private Long actionById;

    @Column(length = 500)
    private String details;

    private Integer positionBefore;
    private Integer positionAfter;

    private LocalDateTime createdAt;

    public TokenHistory() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Token getToken() { return token; }
    public void setToken(Token token) { this.token = token; }

    public TokenStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(TokenStatus fromStatus) { this.fromStatus = fromStatus; }

    public TokenStatus getToStatus() { return toStatus; }
    public void setToStatus(TokenStatus toStatus) { this.toStatus = toStatus; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActionBy() { return actionBy; }
    public void setActionBy(String actionBy) { this.actionBy = actionBy; }

    public Long getActionById() { return actionById; }
    public void setActionById(Long actionById) { this.actionById = actionById; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Integer getPositionBefore() { return positionBefore; }
    public void setPositionBefore(Integer positionBefore) { this.positionBefore = positionBefore; }

    public Integer getPositionAfter() { return positionAfter; }
    public void setPositionAfter(Integer positionAfter) { this.positionAfter = positionAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
