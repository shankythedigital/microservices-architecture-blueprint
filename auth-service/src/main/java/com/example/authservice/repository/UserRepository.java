
package com.example.authservice.repository;

import com.example.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Simple existence checks
    boolean existsByCompositeId_UsernameHash(String usernameHash);
    boolean existsByCompositeId_EmailHash(String emailHash);
    boolean existsByCompositeId_MobileHash(String mobileHash);

    // Single field finders
    Optional<User> findByCompositeId_UsernameHash(String usernameHash);
    Optional<User> findByCompositeId_EmailHash(String emailHash);
    Optional<User> findByCompositeId_MobileHash(String mobileHash);

    // Primary key lookup
    Optional<User> findByUserId(Long userId);

    // Multi-tenant lookups
    Optional<User> findByCompositeId_UsernameHashAndCompositeId_ProjectType(String usernameHash, String projectType);
    Optional<User> findByCompositeId_EmailHashAndCompositeId_ProjectType(String emailHash, String projectType);
    Optional<User> findByCompositeId_MobileHashAndCompositeId_ProjectType(String mobileHash, String projectType);

    // ✅ Corrected: composite lookups (username + mobile/email + projectType)
    Optional<User> findByCompositeId_UsernameHashAndCompositeId_MobileHashAndCompositeId_ProjectType(
            String usernameHash, String mobileHash, String projectType);

    Optional<User> findByCompositeId_UsernameHashAndCompositeId_EmailHashAndCompositeId_ProjectType(
            String usernameHash, String emailHash, String projectType);

    // Existence checks per tenant
    boolean existsByCompositeId_UsernameHashAndCompositeId_ProjectType(String usernameHash, String projectType);
    boolean existsByCompositeId_EmailHashAndCompositeId_ProjectType(String emailHash, String projectType);
    boolean existsByCompositeId_MobileHashAndCompositeId_ProjectType(String mobileHash, String projectType);
    
    boolean existsByCompositeId_UsernameHashAndCompositeId_MobileHashAndCompositeId_ProjectType(String usernameHash,String mobileHash, String projectType);
    boolean existsByCompositeId_UsernameHashAndCompositeId_EmailHashAndCompositeId_ProjectType(String usernameHash,String mobileHash, String projectType);
}





