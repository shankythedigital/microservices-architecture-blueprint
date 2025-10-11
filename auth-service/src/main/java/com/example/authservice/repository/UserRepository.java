

package com.example.authservice.repository;

import com.example.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ Existence checks using compositeId fields (Spring Data will auto-resolve)
    boolean existsByCompositeId_UsernameHash(String usernameHash);
    boolean existsByCompositeId_EmailHash(String emailHash);
    boolean existsByCompositeId_MobileHash(String mobileHash);

    // ✅ Standard finders for deterministic hash lookups
    Optional<User> findByCompositeId_UsernameHash(String usernameHash);
    Optional<User> findByCompositeId_EmailHash(String emailHash);
    Optional<User> findByCompositeId_MobileHash(String mobileHash);

    // ✅ Lookup by numeric user ID (native PK)
    Optional<User> findByUserId(Long userId);

    // ✅ Multi-tenant lookup: username + projectType
    Optional<User> findByCompositeId_UsernameHashAndCompositeId_ProjectType(String usernameHash, String projectType);

    // ✅ Multi-tenant lookup: email + projectType
    Optional<User> findByCompositeId_EmailHashAndCompositeId_ProjectType(String emailHash, String projectType);

    // ✅ Multi-tenant lookup: mobile + projectType
    Optional<User> findByCompositeId_MobileHashAndCompositeId_ProjectType(String mobileHash, String projectType);
}


