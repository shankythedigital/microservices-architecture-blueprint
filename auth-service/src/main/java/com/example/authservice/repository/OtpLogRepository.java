package com.example.authservice.repository;

import com.example.authservice.model.OtpLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpLogRepository extends JpaRepository<OtpLog, Long> {

    // ✅ use the actual entity field: mobileHash
    Optional<OtpLog> findTopByMobileHashAndUsedFalseOrderByCreatedAtDesc(String mobileHash);
}

