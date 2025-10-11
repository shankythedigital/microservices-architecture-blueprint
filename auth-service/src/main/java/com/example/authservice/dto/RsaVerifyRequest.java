
package com.example.authservice.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Dedicated request DTO for RSA verification - no credentialId required.
 */
public class RsaVerifyRequest {
    @NotNull(message = "userId is required")
    public Long userId;

    @NotNull(message = "challenge is required")
    public String challenge;

    @NotNull(message = "signature is required")
    public String signature;
}

