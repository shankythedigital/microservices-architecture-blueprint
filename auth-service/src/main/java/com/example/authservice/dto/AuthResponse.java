package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("refreshToken")
    private String refreshToken;

    @JsonProperty("expiresIn")
    private long expiresIn;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("sessionId")
    private Long sessionId;

    @JsonProperty("roles")
    private List<String> roles;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public AuthResponse(String accessToken, String refreshToken, long expiresIn,
                        Long userId, Long sessionId, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.sessionId = sessionId;
        this.roles = roles;
    }

    // --- Getters ---
    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public List<String> getRoles() {
        return roles;
    }

    // --- Setters ---
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    // --- Debug-friendly representation ---
    @Override
    public String toString() {
        return "AuthResponse{" +
                "userId=" + userId +
                ", sessionId=" + sessionId +
                ", roles=" + roles +
                ", accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : null) + '\'' +
                ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(10, refreshToken.length())) + "..." : null) + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}

