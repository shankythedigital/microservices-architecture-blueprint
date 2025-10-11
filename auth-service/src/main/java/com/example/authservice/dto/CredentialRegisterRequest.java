package com.example.authservice.dto;
public class CredentialRegisterRequest {
    public Long userId;
    public String type; // RSA or WEBAUTHN
    public String credentialId;
    public String publicKey;
}
