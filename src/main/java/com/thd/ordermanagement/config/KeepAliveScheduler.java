package com.thd.ordermanagement.config;

import com.thd.ordermanagement.service.HealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KeepAliveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final HealthService healthService;

    public KeepAliveScheduler(HealthService healthService) {
        this.healthService = healthService;
    }

    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void keepAlive() {
        var status = healthService.getHealthStatus();
        logger.debug("Keep-alive ping: status={}", status.status());
    }
}
