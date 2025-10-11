package com.example.authservice.repository;

import com.example.authservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    int deleteBySessionId(Long sessionId);
    Optional<RefreshToken> findByTokenHash(String tokenHash); // ✅ use tokenHash instead of token

      /**
     * Find the latest valid (non-expired and active) access_token for a given session.
     * Automatically sorts by createdAt descending and limits to one record.
     */
    Optional<RefreshToken> findTopBySession_IdAndActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(Long sessionId, LocalDateTime now);

    /**
     * Fallback: find latest valid global access_token (any session).
     * Returns the most recently created, active and non-expired token.
     */
    Optional<RefreshToken> findTopByActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(LocalDateTime now);

}

