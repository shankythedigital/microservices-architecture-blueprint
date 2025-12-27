// // // // package com.example.authservice.repository;

// // // // import com.example.authservice.model.Session;
// // // // import org.springframework.data.jpa.repository.JpaRepository;

// // // // public interface SessionRepository extends JpaRepository<Session, Long> {

// // // // }

package com.example.authservice.repository;

import com.example.authservice.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {

    // Get all active (non-revoked) sessions for a user
    List<Session> findByUser_UserIdAndRevokedFalse(Long userId);
}

