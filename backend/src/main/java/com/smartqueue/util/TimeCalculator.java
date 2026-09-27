package com.smartqueue.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility for calculating estimated wait times and durations.
 */
public final class TimeCalculator {

    private TimeCalculator() {
        // Utility class
    }

    /**
     * Calculate estimated wait time in minutes.
     * @param peopleAhead Number of people ahead in queue
     * @param avgTimePerPersonMinutes Average service time per person
     * @return Estimated wait time in minutes
     */
    public static int estimateWaitMinutes(int peopleAhead, int avgTimePerPersonMinutes) {
        if (peopleAhead <= 0 || avgTimePerPersonMinutes <= 0) {
            return 0;
        }
        return peopleAhead * avgTimePerPersonMinutes;
    }

    /**
     * Calculate duration between two times in minutes.
     * Returns null if either time is null.
     */
    public static Long durationMinutes(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return null;
        }
        return Duration.between(start, end).toMinutes();
    }

    /**
     * Calculate hold expiry time from now.
     * @param holdTimeMinutes Configured hold duration
     * @return Expiry timestamp
     */
    public static LocalDateTime calculateHoldExpiry(int holdTimeMinutes) {
        return LocalDateTime.now().plusMinutes(holdTimeMinutes);
    }

    /**
     * Check if a hold has expired.
     */
    public static boolean isHoldExpired(LocalDateTime holdExpiryTime) {
        if (holdExpiryTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(holdExpiryTime);
    }

    /**
     * Format duration in minutes to a human-readable string.
     */
    public static String formatMinutes(int minutes) {
        if (minutes < 1) {
            return "< 1 min";
        }
        if (minutes < 60) {
            return minutes + " min";
        }
        int hours = minutes / 60;
        int remaining = minutes % 60;
        if (remaining == 0) {
            return hours + " hr";
        }
        return hours + " hr " + remaining + " min";
    }
}
