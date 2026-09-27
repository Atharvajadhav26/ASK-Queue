package com.smartqueue.dto.request;

import jakarta.validation.constraints.NotNull;

public class PriorityRequest {
    @NotNull
    private String priority;
    private String reason;

    public PriorityRequest() {}

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
