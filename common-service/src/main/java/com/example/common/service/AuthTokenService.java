package com.example.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${services.auth.base-url:http://localhost:$AUTH_SERVER_PORT}")
    private String authBaseUrl;

    @Value("${auth.client-id:asset-service}")
    private String clientId;

    @Value("${auth.client-secret:asset-secret}")
    private String clientSecret;

    public String getAccessToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String token) {
            return token;
        }
        // fallback: client credentials (ensure your auth-service exposes this)
        RestTemplate rt = new RestTemplate();
        try {
            Map<String,Object> res = rt.postForObject(authBaseUrl + "/oauth/token",
                    Map.of("client_id", clientId, "client_secret", clientSecret, "grant_type", "client_credentials"),
                    Map.class);
            if (res != null) return (String)res.get("access_token");
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
