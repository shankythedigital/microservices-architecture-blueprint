
package com.example.authservice.repository;

import com.example.authservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  

     /**
     * Delete all tokens for a given session ID.
     */
    int deleteBySession_Id(Long sessionId);

    /**
     * Find refresh token by its hash value.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * 🔹 Find the latest valid (non-expired, active) token for a given session.
     */
    Optional<RefreshToken> findTopBySession_IdAndActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(
            Long sessionId, LocalDateTime now);

    /**
     * 🔹 Find the latest valid (non-expired, active) token for a given user ID.
     */
    Optional<RefreshToken> findTopBySession_User_UserIdAndActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime now);

    /**
     * 🔹 Find the latest valid (non-expired, active) token for a given username.
     *    Navigates via: RefreshToken → Session → User → UserDetailMaster
     */
    Optional<RefreshToken> findTopBySession_User_Detail_UsernameIgnoreCaseAndActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(
            String username, LocalDateTime now);

    /**
     * 🔹 Find the latest valid (non-expired, active) global access token.
     */
    Optional<RefreshToken> findTopByActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(LocalDateTime now);

    /**
     * 🔹 Fallback for any token that is active (ignores expiry).
     */
    Optional<RefreshToken> findTopByActiveIsTrueOrderByCreatedAtDesc();
    
    // Find all refresh tokens belonging to a user (via session → user)
    List<RefreshToken> findBySession_User_UserId(Long userId);

}





