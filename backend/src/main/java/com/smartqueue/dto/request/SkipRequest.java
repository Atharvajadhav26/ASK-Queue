package com.smartqueue.dto.request;

import jakarta.validation.constraints.NotNull;

public class SkipRequest {
    @NotNull
    private Integer positions;

    public SkipRequest() {}

    public Integer getPositions() { return positions; }
    public void setPositions(Integer positions) { this.positions = positions; }
}
