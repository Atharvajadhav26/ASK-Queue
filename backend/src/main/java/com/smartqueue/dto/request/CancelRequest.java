package com.smartqueue.dto.request;

public class CancelRequest {
    private String reason;

    public CancelRequest() {}

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
