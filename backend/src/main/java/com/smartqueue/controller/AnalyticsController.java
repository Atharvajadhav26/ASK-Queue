package com.smartqueue.controller;

import com.smartqueue.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.smartqueue.security.SecurityUtils;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardStats(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/charts")
    public ResponseEntity<Map<String, Object>> getCharts() {
        return ResponseEntity.ok(analyticsService.getAnalytics(SecurityUtils.getCurrentUserId()));
    }
}
