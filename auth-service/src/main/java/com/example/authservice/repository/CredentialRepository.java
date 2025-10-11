
package com.example.authservice.repository;

import com.example.authservice.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Optional<Credential> findByCredentialId(String credentialId);
    Optional<Credential> findByUser_UserIdAndTypeAndMetadata(Long userId, String type, String metadata);
}


