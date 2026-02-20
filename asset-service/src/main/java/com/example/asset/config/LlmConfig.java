package com.example.asset.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate for LLM API calls with timeout.
 */
@Configuration
public class LlmConfig {

    @Bean(name = "llmRestTemplate")
    public RestTemplate llmRestTemplate(LlmProperties llmProperties) {
        int timeout = Math.max(10, llmProperties.getTimeoutSeconds());
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(timeout))
                .build();
    }
}
