package com.example.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for notification list API
 * Registered via @EnableConfigurationProperties in NotificationServiceApplication
 */
@ConfigurationProperties(prefix = "notification.list")
public class NotificationListProperties {

    /**
     * Number of days to display notifications in notification icons
     * Default: 30 days
     */
    private int displayDays = 30;

    /**
     * Maximum number of notifications to return
     * Default: 100
     */
    private int maxResults = 100;

    // Getters and Setters
    public int getDisplayDays() {
        return displayDays;
    }

    public void setDisplayDays(int displayDays) {
        this.displayDays = displayDays;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}

