package com.example.helpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.helpdesk", "com.example.common"})
@EntityScan(basePackages = {"com.example.helpdesk.entity", "com.example.common.entity"})
@EnableJpaRepositories(basePackages = {"com.example.helpdesk.repository", "com.example.common.repository"})
@EnableFeignClients(basePackages = {"com.example.common.client", "com.example.helpdesk.client"})
public class HelpdeskServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelpdeskServiceApplication.class, args);
    }
}

