
package com.example.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.asset", "com.example.common"})
@EntityScan(basePackages = {"com.example.asset.entity", "com.example.common.entity"})
@EnableJpaRepositories(basePackages = {"com.example.asset.repository", "com.example.common.repository"})
@EnableFeignClients(basePackages = {"com.example.common.client", "com.example.asset.client"})
public class AssetServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetServiceApplication.class, args);
    }
}

