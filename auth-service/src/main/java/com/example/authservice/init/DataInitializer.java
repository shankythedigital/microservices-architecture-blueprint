package com.example.authservice.init;

import com.example.authservice.model.*;
import com.example.authservice.repository.*;
import com.example.common.util.HmacUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * DataInitializer
 * 
 * Seeds initial data for the application:
 * - Project types (ECOM, ASSET, PORTAL, ADMIN_CONSOLE)
 * - Default roles (ROLE_ADMIN, ROLE_USER, ROLE_MANAGER, ROLE_AUDITOR, ROLE_SUPPORT)
 * - Default admin users for each project type
 * 
 * This runs automatically on application startup and is idempotent -
 * it will not create duplicates if data already exists.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ProjectTypeRepository projectTypeRepo;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ============================================================
    // Constants
    // ============================================================

    private static final List<String> PROJECT_TYPES = Arrays.asList(
            "ECOM", "ASSET", "PORTAL", "ADMIN_CONSOLE"
    );

    private static final List<String> DEFAULT_ROLES = Arrays.asList(
            "ROLE_ADMIN",
            "ROLE_USER",
            "ROLE_MANAGER",
            "ROLE_AUDITOR",
            "ROLE_SUPPORT"
    );

    private static final Map<String, String> PROJECT_TYPE_DESCRIPTIONS = Map.of(
            "ECOM", "E-Commerce Platform",
            "ASSET", "Asset Management System",
            "PORTAL", "Customer Portal",
            "ADMIN_CONSOLE", "Administration Console"
    );

    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    private static final String SYSTEM_USER = "system";

    // ============================================================
    // Main Entry Point
    // ============================================================

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🚀 Starting data initialization...");
        
        try {
            seedProjectTypes();
            seedRoles();
            seedDefaultAdmins();
            
            log.info("✅ Data initialization completed successfully");
        } catch (Exception e) {
            log.error("❌ Data initialization failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize application data", e);
        }
    }

    // ============================================================
    // Project Type Seeding
    // ============================================================

    /**
     * Seeds project types if they don't already exist
     */
    private void seedProjectTypes() {
        log.info("📋 Seeding project types...");
        
        int displayOrder = 1;
        for (String projectTypeCode : PROJECT_TYPES) {
            try {
                Optional<ProjectType> existing = projectTypeRepo.findByCodeIgnoreCase(projectTypeCode);
                
                if (existing.isPresent()) {
                    log.debug("ℹ️ Project type '{}' already exists, skipping", projectTypeCode);
                    continue;
                }

                ProjectType projectType = createProjectType(projectTypeCode, displayOrder);
                projectTypeRepo.save(projectType);
                
                log.info("✅ Created project type: {} (Order: {})", projectTypeCode, displayOrder);
                displayOrder++;
                
            } catch (Exception e) {
                log.error("❌ Failed to create project type '{}': {}", projectTypeCode, e.getMessage());
                throw e;
            }
        }
        
        log.info("✅ Project type seeding completed");
    }

    /**
     * Creates a new ProjectType entity
     */
    private ProjectType createProjectType(String code, int displayOrder) {
        ProjectType projectType = new ProjectType();
        projectType.setCode(code);
        projectType.setName(code.replace("_", " "));
        projectType.setDescription(
                PROJECT_TYPE_DESCRIPTIONS.getOrDefault(code, "Project type: " + code)
        );
        projectType.setDisplayOrder(displayOrder);
        projectType.setCreatedBy(SYSTEM_USER);
        projectType.setActive(true);
        return projectType;
    }

    // ============================================================
    // Role Seeding
    // ============================================================

    /**
     * Seeds default roles if they don't already exist
     */
    private void seedRoles() {
        log.info("📋 Seeding default roles...");
        
        for (String roleName : DEFAULT_ROLES) {
            try {
                Optional<Role> existing = roleRepo.findByName(roleName);
                
                if (existing.isPresent()) {
                    log.debug("ℹ️ Role '{}' already exists, skipping", roleName);
                    continue;
                }

                Role role = createRole(roleName);
                roleRepo.save(role);
                
                log.info("✅ Created role: {}", roleName);
                
            } catch (Exception e) {
                log.error("❌ Failed to create role '{}': {}", roleName, e.getMessage());
                throw e;
            }
        }
        
        log.info("✅ Role seeding completed");
    }

    /**
     * Creates a new Role entity
     */
    private Role createRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        role.setCreatedBy(SYSTEM_USER);
        return role;
    }

    // ============================================================
    // Default Admin User Seeding
    // ============================================================

    /**
     * Seeds default admin users for each project type
     */
    private void seedDefaultAdmins() {
        log.info("📋 Seeding default admin users...");
        
        for (String projectType : PROJECT_TYPES) {
            try {
                String adminUsername = "admin_" + projectType.toLowerCase();
                
                // Check if admin already exists
                String usernameHash = HmacUtil.hmacHex(adminUsername);
                boolean exists = userRepo.existsByCompositeId_UsernameHash(usernameHash);
                
                if (exists) {
                    log.info("ℹ️ Admin user for project type '{}' already exists, skipping", projectType);
                    continue;
                }

                createAdminUserForProjectType(projectType, adminUsername);
                
            } catch (Exception e) {
                log.error("❌ Failed to create admin user for project type '{}': {}", 
                        projectType, e.getMessage());
                // Continue with other project types even if one fails
            }
        }
        
        log.info("✅ Admin user seeding completed");
    }

    /**
     * Creates an admin user for a specific project type
     */
    private void createAdminUserForProjectType(String projectType, String username) {
        try {
            // Generate user details
            String email = username + "@example.com";
            String mobile = generateMobileNumber();
            String employeeId = "EMP_ADMIN_" + projectType.toUpperCase();

            // Compute HMACs for composite key
            String usernameHash = HmacUtil.hmacHex(username);
            String emailHash = HmacUtil.hmacHex(email);
            String mobileHash = HmacUtil.hmacHex(mobile);

            // Create composite user ID
            UserId compositeId = new UserId(null, usernameHash, emailHash, mobileHash, projectType);

            // Create user entity
            User user = createUser(compositeId, username, email, mobile);

            // Assign admin and user roles
            assignRolesToUser(user);

            // Create user detail master
            UserDetailMaster detail = createUserDetail(user, username, email, mobile, employeeId);

            // Link user and detail
            user.setDetail(detail);

            // Persist user (cascade will save detail)
            userRepo.save(user);

            log.info("✅ Created admin user for project type [{}]: username='{}', password='{}'",
                    projectType, username, DEFAULT_ADMIN_PASSWORD);

        } catch (Exception e) {
            log.error("❌ Error creating admin user for project type '{}': {}", 
                    projectType, e.getMessage(), e);
            throw new RuntimeException("Failed to create admin user for " + projectType, e);
        }
    }

    /**
     * Creates a User entity with encrypted credentials
     */
    private User createUser(UserId compositeId, String username, String email, String mobile) {
        User user = new User();
        user.setCompositeId(compositeId);
        user.setUsernameEnc(username);
        user.setEmailEnc(email);
        user.setMobileEnc(mobile);
        user.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        user.setEnabled(true);
        user.setCreatedBy(SYSTEM_USER);
        return user;
    }

    /**
     * Assigns ROLE_ADMIN and ROLE_USER to the user
     */
    private void assignRolesToUser(User user) {
        Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_ADMIN not found. Ensure roles are seeded before creating admin users."));

        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_USER not found. Ensure roles are seeded before creating admin users."));

        user.setRoles(Set.of(adminRole, userRole));
    }

    /**
     * Creates a UserDetailMaster entity
     */
    private UserDetailMaster createUserDetail(
            User user, String username, String email, String mobile, String employeeId) {
        UserDetailMaster detail = new UserDetailMaster();
        detail.setUser(user);
        detail.setUsername(username);
        detail.setEmail(email);
        detail.setMobile(mobile);
        detail.setEmployeeId(employeeId);
        detail.setCreatedBy(SYSTEM_USER);
        detail.setActive(true);
        return detail;
    }

    /**
     * Generates a random mobile number for admin users
     */
    private String generateMobileNumber() {
        Random random = new Random();
        int suffix = 10 + random.nextInt(89); // Random number between 10 and 98
        return "+9112345678" + suffix;
    }
}
