package com.example.authservice.dto;

import java.time.LocalDateTime;

/**
 * Request body for block (temporary) and permanent-block actions.
 * PDPA/DPDPA: reason documents lawful basis for access restriction.
 */
public class BlockUserRequest {
    /** Reason code or description (e.g. security, compliance, user request). */
    private String reason;
    /** Optional; for temporary block only. After this time, policy may allow manual unblock. */
    private LocalDateTime blockedUntil;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getBlockedUntil() { return blockedUntil; }
    public void setBlockedUntil(LocalDateTime blockedUntil) { this.blockedUntil = blockedUntil; }
}
