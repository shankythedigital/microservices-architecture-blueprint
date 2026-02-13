
package com.example.asset.repository;

import com.example.asset.entity.VendorMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<VendorMaster, Long> {
    boolean existsByVendorNameIgnoreCase(String vendorName);
    Optional<VendorMaster> findByVendorNameIgnoreCase(String vendorName);
}


