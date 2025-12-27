
package com.example.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main application entry. Explicitly scans the common module packages so shared beans
 * (NotificationHelper, repositories, entities, clients) are discovered.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.notification", "com.example.common"})
@EntityScan(basePackages = {"com.example.notification.entity", "com.example.common.entity"})
@EnableJpaRepositories(basePackages = {"com.example.notification.repository", "com.example.common.repository"})
@EnableFeignClients(basePackages = {"com.example.common.client", "com.example.notification.client"})
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
