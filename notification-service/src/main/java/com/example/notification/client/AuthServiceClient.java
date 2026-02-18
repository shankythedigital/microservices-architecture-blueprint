package com.example.notification.client;

import com.example.notification.dto.CommunicationPreferencesDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Calls auth-service to fetch user communication opt-out preferences.
 * If auth is unavailable or the call fails, returns empty (caller proceeds with send).
 */
@Component
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestTemplate restTemplate;
    private final String authBaseUrl;

    public AuthServiceClient(
            RestTemplate restTemplate,
            @Value("${auth.service.url:}") String authBaseUrl) {
        this.restTemplate = restTemplate;
        this.authBaseUrl = authBaseUrl != null ? authBaseUrl.trim() : "";
    }

    /**
     * Fetch communication preferences for a user. Returns empty if URL not configured, no token, or call fails.
     */
    public Optional<CommunicationPreferencesDto> getCommunicationPreferences(String userId, String bearerToken) {
        if (userId == null || userId.isBlank() || bearerToken == null || bearerToken.isBlank()) {
            return Optional.empty();
        }
        if (authBaseUrl.isEmpty()) {
            log.trace("Auth service URL not configured — skipping opt-out check");
            return Optional.empty();
        }
        String url = authBaseUrl + "auth/profile/" + userId + "/communication-preferences";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken);
        try {
            ResponseEntity<CommunicationPreferencesDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    CommunicationPreferencesDto.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            }
        } catch (Exception e) {
            log.debug("Could not fetch communication preferences for user {}: {} — proceeding with send", userId, e.getMessage());
        }
        return Optional.empty();
    }
}
