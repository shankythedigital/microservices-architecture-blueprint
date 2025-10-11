
package com.example.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${auth.client-id}")
    private String clientId;

    @Value("${auth.client-secret}")
    private String clientSecret;

    /**
     * Try to get the user’s JWT from SecurityContextHolder.
     * If not present (background job), fetch service token from auth-service.
     */
    public String getAccessToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return getServiceToken();
    }

    private String getServiceToken() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", "client_credentials");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            authServiceUrl + "/auth/token", params, Map.class);

        return (String) response.getBody().get("access_token");
    }
}
