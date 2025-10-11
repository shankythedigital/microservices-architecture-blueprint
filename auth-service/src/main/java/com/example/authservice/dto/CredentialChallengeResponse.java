
package com.example.authservice.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Generic credential challenge response used by WebAuthn and other flows.
 * credentialId is optional for RSA verification (we use "rsa-{userId}" by convention).
 */
public class CredentialChallengeResponse {

    @NotNull(message = "userId is required")
    public Long userId;

    // optional for RSA verify; used for WebAuthn / Passkey flows
    public String credentialId;

    @NotNull(message = "challenge is required")
    public String challenge;

    @NotNull(message = "signature is required")
    public String signature;
}


