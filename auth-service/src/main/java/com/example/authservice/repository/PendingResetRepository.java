package com.example.authservice.repository;

import com.example.authservice.model.PendingReset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PendingResetRepository extends JpaRepository<PendingReset,Long> {
    Optional<PendingReset> findByResetToken(String resetToken);
}
