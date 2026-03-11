package com.thd.ordermanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.keep-alive.url:}")
    private String keepAliveUrl;

    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void keepAlive() {
        if (keepAliveUrl == null || keepAliveUrl.isBlank()) {
            return;
        }
        try {
            var response = restTemplate.getForEntity(keepAliveUrl, String.class);
            logger.debug("Keep-alive ping: status={}", response.getStatusCode());
        } catch (Exception e) {
            logger.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
