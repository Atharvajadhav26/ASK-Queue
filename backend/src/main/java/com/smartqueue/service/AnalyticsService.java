package com.smartqueue.service;

import com.smartqueue.entity.*;
import com.smartqueue.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics service — computes real-time statistics from database data.
 * No fake or hardcoded values.
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final QueueRepository queueRepository;
    private final TokenRepository tokenRepository;
    private final QueueSettingsRepository queueSettingsRepository;

    public AnalyticsService(QueueRepository queueRepository,
                            TokenRepository tokenRepository,
                            QueueSettingsRepository queueSettingsRepository) {
        this.queueRepository = queueRepository;
        this.tokenRepository = tokenRepository;
        this.queueSettingsRepository = queueSettingsRepository;
    }

    /**
     * Get admin dashboard statistics.
     */
    public Map<String, Object> getDashboardStats(Long adminId) {
        Map<String, Object> stats = new LinkedHashMap<>();

        List<ServiceQueue> adminQueues = queueRepository.findByCreatedBy(adminId);
        List<ServiceQueue> activeQueues = adminQueues.stream()
                .filter(q -> q.getStatus() == QueueStatus.OPEN)
                .collect(Collectors.toList());

        int totalWaiting = 0;
        String currentToken = null;
        double totalAvgWait = 0;
        int queueCount = 0;

        for (ServiceQueue queue : activeQueues) {
            int waiting = (int) tokenRepository.countByQueueAndStatus(queue, TokenStatus.WAITING);
            totalWaiting += waiting;

            if (queue.getCurrentServingTokenId() != null) {
                Token serving = tokenRepository.findById(queue.getCurrentServingTokenId()).orElse(null);
                if (serving != null && currentToken == null) {
                    currentToken = serving.getTokenNumber();
                }
            }

            QueueSettings settings = queueSettingsRepository.findByQueueId(queue.getId()).orElse(null);
            if (settings != null && waiting > 0) {
                totalAvgWait += (double) waiting * settings.getAvgTimePerPersonMinutes();
                queueCount++;
            }
        }

        stats.put("activeQueues", activeQueues.size());
        stats.put("customersWaiting", totalWaiting);
        stats.put("currentToken", currentToken != null ? currentToken : "—");
        stats.put("avgWaitTime", queueCount > 0 ? String.format("%.0f min", totalAvgWait / queueCount) : "0 min");

        // Recent queues for dashboard
        List<Map<String, Object>> recentQueues = new ArrayList<>();
        for (ServiceQueue queue : adminQueues) {
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("id", queue.getId());
            qMap.put("queueId", queue.getQueueId());
            qMap.put("name", queue.getName());
            qMap.put("serviceType", queue.getServiceType());
            qMap.put("status", queue.getStatus().name());
            qMap.put("waitingCount", tokenRepository.countByQueueAndStatus(queue, TokenStatus.WAITING));
            recentQueues.add(qMap);
        }
        stats.put("recentQueues", recentQueues);

        return stats;
    }

    /**
     * Get detailed analytics data.
     */
    public Map<String, Object> getAnalytics(Long adminId) {
        Map<String, Object> analytics = new LinkedHashMap<>();

        List<ServiceQueue> adminQueues = queueRepository.findByCreatedBy(adminId);
        List<Token> allTokens = new ArrayList<>();
        for (ServiceQueue queue : adminQueues) {
            allTokens.addAll(tokenRepository.findByQueue(queue));
        }

        // Basic counts
        int activeQueues = (int) adminQueues.stream()
                .filter(q -> q.getStatus() == QueueStatus.OPEN).count();
        int totalWaiting = (int) allTokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.WAITING).count();
        int totalServed = (int) allTokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.COMPLETED).count();
        int totalCancelled = (int) allTokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.CANCELLED).count();
        int totalSkipped = (int) allTokens.stream()
                .filter(t -> t.getSkipCount() > 0).count();
        int totalHeld = (int) allTokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.HELD).count();
        int totalExpired = (int) allTokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.EXPIRED).count();
        int priorityCustomers = (int) allTokens.stream()
                .filter(t -> t.getPriority() != PriorityLevel.NORMAL).count();

        analytics.put("activeQueues", activeQueues);
        analytics.put("totalCustomersWaiting", totalWaiting);
        analytics.put("totalCustomersServed", totalServed);
        analytics.put("customersServed", totalServed); // frontend alias
        analytics.put("completedTokens", totalServed);
        analytics.put("cancelledTokens", totalCancelled);
        analytics.put("skippedTokens", totalSkipped);
        analytics.put("heldTokens", totalHeld);
        analytics.put("expiredTokens", totalExpired);
        analytics.put("priorityCustomers", priorityCustomers);

        // Average wait time (from completed tokens)
        double avgWait = allTokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.COMPLETED && t.getCalledTime() != null && t.getJoinTime() != null)
                .mapToLong(t -> java.time.Duration.between(t.getJoinTime(), t.getCalledTime()).toMinutes())
                .average()
                .orElse(0);
        analytics.put("avgWaitTimeMinutes", Math.round(avgWait * 10.0) / 10.0);
        analytics.put("avgWaitTime", String.format("%.0f min", avgWait)); // frontend alias

        // Average service time
        double avgService = allTokens.stream()
                .filter(t -> t.getStatus() == TokenStatus.COMPLETED && t.getServingStartTime() != null && t.getCompletedTime() != null)
                .mapToLong(t -> java.time.Duration.between(t.getServingStartTime(), t.getCompletedTime()).toMinutes())
                .average()
                .orElse(0);
        analytics.put("avgServiceTimeMinutes", Math.round(avgService * 10.0) / 10.0);
        analytics.put("avgServiceTime", String.format("%.0f min", avgService)); // frontend alias

        // Customers served per hour (last 24 hours) — Chart.js Bar format
        List<String> hourLabels = new ArrayList<>();
        List<Integer> hourValues = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 23; i >= 0; i--) {
            LocalDateTime hourStart = now.minusHours(i).withMinute(0).withSecond(0);
            LocalDateTime hourEnd = hourStart.plusHours(1);
            String hourLabel = String.format("%02d:00", hourStart.getHour());
            int count = (int) allTokens.stream()
                    .filter(t -> t.getStatus() == TokenStatus.COMPLETED && t.getCompletedTime() != null)
                    .filter(t -> !t.getCompletedTime().isBefore(hourStart) && t.getCompletedTime().isBefore(hourEnd))
                    .count();
            hourLabels.add(hourLabel);
            hourValues.add(count);
        }
        // Chart.js-ready format: { labels: [...], datasets: [{ label, data, backgroundColor }] }
        Map<String, Object> barChart = new LinkedHashMap<>();
        barChart.put("labels", hourLabels);
        Map<String, Object> barDataset = new LinkedHashMap<>();
        barDataset.put("label", "Customers Served");
        barDataset.put("data", hourValues);
        barDataset.put("backgroundColor", "rgba(99, 102, 241, 0.6)");
        barDataset.put("borderColor", "rgba(99, 102, 241, 1)");
        barDataset.put("borderWidth", 1);
        barChart.put("datasets", List.of(barDataset));
        analytics.put("customersServedPerHour", barChart);

        // Token status distribution — Chart.js Doughnut format
        Map<String, Integer> statusDist = new LinkedHashMap<>();
        statusDist.put("WAITING", totalWaiting);
        int servingCount = (int) allTokens.stream().filter(t -> t.getStatus() == TokenStatus.SERVING).count();
        statusDist.put("SERVING", servingCount);
        statusDist.put("COMPLETED", totalServed);
        statusDist.put("CANCELLED", totalCancelled);
        statusDist.put("HELD", totalHeld);
        statusDist.put("EXPIRED", totalExpired);
        // Chart.js-ready format
        Map<String, Object> doughnutChart = new LinkedHashMap<>();
        doughnutChart.put("labels", new ArrayList<>(statusDist.keySet()));
        Map<String, Object> doughnutDataset = new LinkedHashMap<>();
        doughnutDataset.put("data", new ArrayList<>(statusDist.values()));
        doughnutDataset.put("backgroundColor", List.of(
                "#3B82F6", "#F59E0B", "#10B981", "#EF4444", "#8B5CF6", "#6B7280"));
        doughnutChart.put("datasets", List.of(doughnutDataset));
        analytics.put("tokenStatusDistribution", doughnutChart);

        // Queue volume by day (last 7 days)
        Map<String, Integer> volumeByDay = new TreeMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateLabel = date.toString();
            int count = (int) allTokens.stream()
                    .filter(t -> t.getJoinTime() != null && t.getJoinTime().toLocalDate().equals(date))
                    .count();
            volumeByDay.put(dateLabel, count);
        }
        analytics.put("queueVolumeByDay", volumeByDay);

        // Peak hour
        String peakHour = "N/A";
        int maxServed = 0;
        for (int i = 0; i < hourLabels.size(); i++) {
            if (hourValues.get(i) > maxServed) {
                maxServed = hourValues.get(i);
                peakHour = hourLabels.get(i);
            }
        }
        analytics.put("peakHour", peakHour);

        // Average wait time by hour
        Map<String, Double> avgWaitByHour = new TreeMap<>();
        for (int i = 23; i >= 0; i--) {
            LocalDateTime hourStart = now.minusHours(i).withMinute(0).withSecond(0);
            LocalDateTime hourEnd = hourStart.plusHours(1);
            String hourLabel = String.format("%02d:00", hourStart.getHour());
            double avg = allTokens.stream()
                    .filter(t -> t.getStatus() == TokenStatus.COMPLETED && t.getCalledTime() != null && t.getJoinTime() != null)
                    .filter(t -> !t.getCalledTime().isBefore(hourStart) && t.getCalledTime().isBefore(hourEnd))
                    .mapToLong(t -> java.time.Duration.between(t.getJoinTime(), t.getCalledTime()).toMinutes())
                    .average()
                    .orElse(0);
            avgWaitByHour.put(hourLabel, Math.round(avg * 10.0) / 10.0);
        }
        analytics.put("avgWaitTimeByHour", avgWaitByHour);

        return analytics;
    }
}
