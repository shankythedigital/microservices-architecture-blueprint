
package com.example.authservice.repository;

import com.example.authservice.model.UserDetailMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDetailMasterRepository extends JpaRepository<UserDetailMaster, Long> {

    // Lookup by username hash
    Optional<UserDetailMaster> findByUsernameHash(String usernameHash);

    // Lookup by email hash
    Optional<UserDetailMaster> findByEmailHash(String emailHash);

    // Lookup by mobile hash
    Optional<UserDetailMaster> findByMobileHash(String mobileHash);
    
    Optional<UserDetailMaster> findByUserId(Long userId);

    // Exists checks (for uniqueness validation before insert/update)
    boolean existsByUsernameHash(String usernameHash);
    boolean existsByEmailHash(String emailHash);
    boolean existsByMobileHash(String mobileHash);
    boolean existsByUserId(Long userId);
}


