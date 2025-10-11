
package com.example.authservice.init;

import com.example.authservice.model.*;
import com.example.authservice.repository.*;
import com.example.authservice.util.HmacUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ✅ DataInitializer
 * Seeds base roles and creates default admin users per project type.
 * Now fully aligned with composite UserId structure.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RoleRepository roleRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private UserDetailMasterRepository udmRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Supported project types (for multi-tenant systems)
    private static final List<String> PROJECT_TYPES = List.of("ECOM", "ASSET", "PORTAL", "ADMIN_CONSOLE");

    // Default roles
    private static final List<String> DEFAULT_ROLES = List.of(
            "ROLE_ADMIN",
            "ROLE_USER",
            "ROLE_MANAGER",
            "ROLE_AUDITOR",
            "ROLE_SUPPORT"
    );

    @Override
    public void run(String... args) {
        seedRoles();
        seedDefaultAdmins();
    }

    // =====================================================
    // ROLE SEEDING
    // =====================================================
    private void seedRoles() {
        for (String roleName : DEFAULT_ROLES) {
            roleRepo.findByName(roleName).or(() -> {
                Role role = new Role();
                role.setName(roleName);
                role.setCreatedBy("system");
                roleRepo.save(role);
                System.out.println("✅ Role created: " + roleName);
                return Optional.of(role);
            });
        }
    }

    // =====================================================
    // ADMIN CREATION PER PROJECT TYPE
    // =====================================================
    private void seedDefaultAdmins() {
        for (String projectType : PROJECT_TYPES) {
            String adminUsername = "admin_" + projectType.toLowerCase();
            String email = adminUsername + "@example.com";
            String mobile = "+9112345678" + (10 + new Random().nextInt(89));

            // Compute HMACs
            String usernameHash = HmacUtil.hmacHex(adminUsername);
            String emailHash = HmacUtil.hmacHex(email);
            String mobileHash = HmacUtil.hmacHex(mobile);

            // Check if already exists
            boolean exists = userRepo.existsByCompositeId_UsernameHash(usernameHash);
            if (exists) {
                System.out.printf("ℹ️ Admin already exists for project %s%n", projectType);
                continue;
            }

            createAdminUser(adminUsername, email, mobile, projectType, usernameHash, emailHash, mobileHash);
        }
    }

    // =====================================================
    // CREATE ADMIN USER FOR GIVEN PROJECT TYPE
    // =====================================================
    private void createAdminUser(
            String username,
            String email,
            String mobile,
            String projectType,
            String usernameHash,
            String emailHash,
            String mobileHash
    ) {
        try {
            // Create composite key
            UserId compositeId = new UserId(null, usernameHash, emailHash, mobileHash, projectType);

            // Create user
            User user = new User();
            user.setCompositeId(compositeId);
            user.setUsernameEnc(username);
            user.setEmailEnc(email);
            user.setMobileEnc(mobile);
            user.setPassword(encoder.encode("Admin@123"));
            user.setEnabled(true);
            user.setCreatedBy("system");

            // Assign roles
            Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));
            Role userRole = roleRepo.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
            user.setRoles(Set.of(adminRole, userRole));

            // Create user detail master
            UserDetailMaster detail = new UserDetailMaster();
            detail.setUser(user);
            detail.setUsername(username);
            detail.setEmail(email);
            detail.setMobile(mobile);
            detail.setEmployeeId("EMP_ADMIN_" + projectType.toUpperCase());
            detail.setCreatedBy("system");
            detail.setActive(true);

            // Link and persist
            user.setDetail(detail);
            userRepo.save(user);

            System.out.printf(
                    "✅ Admin user created for project [%s]: username='%s', password='Admin@123'%n",
                    projectType, username
            );
        } catch (Exception e) {
            System.err.printf("❌ Failed to create admin for [%s]: %s%n", projectType, e.getMessage());
        }
    }
}

