package com.smartqueue.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Scheduling enabled via @EnableScheduling
    // HoldExpiryScheduler will be picked up automatically
}
