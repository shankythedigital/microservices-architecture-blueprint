# Generate private key
openssl genrsa -out private.pem 2048

# Extract public key in X.509 format (Base64 DER)
openssl rsa -in private.pem -pubout -outform PEM > public.pem


#!/usr/bin/env bash
set -euo pipefail

PROJECT="auth-service"
BASE="src/main/java/com/example/authservice"
RES="$PROJECT/src/main/resources"
PKG="com.example.authservice"
KEYS_DIR="$RES/keys"

echo "Creating project: $PROJECT"

# Create directories
mkdir -p $PROJECT/$BASE/{controller,service,service/impl,repository,model,dto,util,converter,config,client,security,init,exception,crypto,mapper}
mkdir -p $RES
mkdir -p $KEYS_DIR



# -------------------------
# generate RSA keypair (2048) if not present
# -------------------------
PRIVATE_PEM="$KEYS_DIR/jwt-private.pem"
PUBLIC_PEM="$KEYS_DIR/jwt-public.pem"

if [ -f "$PRIVATE_PEM" ] && [ -f "$PUBLIC_PEM" ]; then
  echo "RSA keypair already exists in $KEYS_DIR — skipping generation."
else
  echo "Generating RSA keypair (2048 bits) into $KEYS_DIR ..."
  # generate private key
  openssl genrsa -out "$PRIVATE_PEM" 2048
  # generate public key in X.509 PEM format
  openssl rsa -in "$PRIVATE_PEM" -pubout -out "$PUBLIC_PEM"
  chmod 600 "$PRIVATE_PEM"
  chmod 644 "$PUBLIC_PEM"
  echo "Keys generated: $PRIVATE_PEM, $PUBLIC_PEM"
fi


# ---------------------------
# Create .env.auth and symlink/copy as .env
# ---------------------------
ENV_FILE="$PROJECT/.env.auth"
DOTENV_FILE="$PROJECT/.env"

if [ -f "$ENV_FILE" ]; then
  echo ".env.auth already exists. Skipping key generation."
else
  echo "Generating .env.auth with encryption keys and db credentials..."
  ENC_KEY=$(openssl rand -base64 32)
  HMAC_KEY=$(openssl rand -base64 32)
  cat > "$ENV_FILE" <<EOF
AUTH_ENC_KEY=$ENC_KEY
AUTH_HMAC_KEY=$HMAC_KEY
DB_USERNAME=root
DB_PASSWORD=Snmysql@1110
ACCESS_TOKEN=change_this_token
EOF
  echo "Created $ENV_FILE (DO NOT commit to git)."
fi

# Ensure .env points to .env.auth
cp "$ENV_FILE" "$DOTENV_FILE"
echo "Linked $ENV_FILE -> $DOTENV_FILE (Spring Boot will auto-load)."

# Add to .gitignore
if ! grep -q "^.env" "$PROJECT/.gitignore" 2>/dev/null; then
  echo ".env" >> "$PROJECT/.gitignore"
  echo ".env.auth" >> "$PROJECT/.gitignore"
fi


################################################################################
# 1. pom.xml
################################################################################

cat > $PROJECT/pom.xml <<'EOF'
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
 
  <!-- Inherit from the aggregator/parent POM -->
  <parent>
    <groupId>com.example</groupId>
    <artifactId>microservices-architecture-blueprint</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <relativePath>../pom.xml</relativePath>
  </parent>

  <artifactId>auth-service</artifactId>
  <version>0.0.2-SNAPSHOT</version>
  <name>Auth Service</name>
  <description>Authentication microservice with session management and PII encryption</description>

  
  <properties>
    <java.version>17</java.version>
  </properties>
  <dependencies>
    <!-- Web, JPA, Security, OAuth2 -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>

    <!-- feign-->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
      <version>4.0.4</version>
    </dependency>


    <dependency>

        <groupId>org.springframework.boot</groupId>

        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>

    </dependency>

    <!-- JWT (jjwt) -->
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.11.5</version>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>0.11.5</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>0.11.5</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>

    <groupId>javax.validation</groupId>

    <artifactId>validation-api</artifactId>

    <version>2.0.1.Final</version>

</dependency>

    <dependency>

    <groupId>jakarta.validation</groupId>

    <artifactId>jakarta.validation-api</artifactId>

    <version>3.0.0</version>

</dependency>

    <!-- MySQL -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
        <optional>true</optional>
    </dependency>

    <!-- Test -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>

    <dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
    </dependency>
  
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-aop</artifactId>
      </dependency> 
    <!-- Common lib -->
    <dependency>
      <groupId>com.example</groupId>
      <artifactId>common-lib</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>

    <dependency>

        <groupId>javax.annotation</groupId>

        <artifactId>javax.annotation-api</artifactId>

        <version>1.3.2</version>

    </dependency>

    
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>



EOF


cat > .env <<'EOF'

DB_USERNAME=root
DB_PASSWORD=Snmysql@1110
ENCRYPTION_KEY=MySuperSecure32CharRandomKey123!
JWT_SECRET=4aD9#kLp!2zQmN7xYvRtWpShUfBgJcKd

GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
FACEBOOK_CLIENT_ID=your-facebook-client-id
FACEBOOK_CLIENT_SECRET=your-facebook-client-secret
TWITTER_CLIENT_ID=your-twitter-client-id
TWITTER_CLIENT_SECRET=your-twitter-client-secret

EOF

# -------------------------
# application.yml
# -------------------------
cat > $RES/application.yml <<'YML'
server:
  port: 8081

notification:
  service:
    url: http://localhost:8082/api/notifications

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/authdb?useSSL=false&serverTimezone=UTC
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:Snmysql@1110}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    open-in-view: false

encryption:
  key: ${ENCRYPTION_KEY:ChangeThisTo32ByteKey_ReplaceInProd!}
hmac:
  key: ${HMAC_KEY:ChangeThisToAnotherKeyForHMAC_ReplaceInProd!}

jwt:
  private-key-path: classpath:keys/jwt-private.pem
  public-key-path: classpath:keys/jwt-public.pem
  access-token-validity-seconds: 900
  refresh-token-validity-seconds: 1209600
auth:
  enc:
    key: ${AUTH_ENC_KEY:default_auth_enc_key}   # fallback if not provided
  hmac:
    key: ${AUTH_HMAC_KEY:default_auth_hmac_key}



# logging:
#     level:
#         root: INFO
#         com.example.authservice: DEBUG
#         org.springframework.web: INFO
#         org.hibernate.SQL: DEBUG
#         org.hibernate.type.descriptor.sql.BasicBinder: TRACE
#         org.springframework.security: DEBUG
#         org.springframework.cloud.openfeign: DEBUG
#         org.hibernate.tool.hbm2ddl: DEBUG
#         org.hibernate.orm.deprecation: WARN
#         org.springframework.orm.jpa: DEBUG
#         org.springframework.transaction: DEBUG
#         org.springframework.jdbc.core: DEBUG
#         org.springframework.context.support.PostProcessorRegistrationDelegate$BeanPostProcessorChecker: off


YML

# -------------------------
# Main application
# -------------------------
cat > $PROJECT/$BASE/Application.java <<'JAVA'


package com.example.authservice;

import com.example.authservice.crypto.JpaAttributeEncryptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;

import java.util.Base64;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.authservice.client")
public class Application {

    @Value("${auth.enc.key}")
    private String encKey;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void initEncryptor() {
        try {
            byte[] keyBytes;
            try {
                // Try Base64 decode first
                keyBytes = Base64.getDecoder().decode(encKey);
            } catch (IllegalArgumentException e) {
                // If not Base64, fall back to raw bytes (UTF-8)
                keyBytes = encKey.getBytes();
                // Ensure key length is 32 bytes (AES-256 requirement)
                if (keyBytes.length < 32) {
                    byte[] padded = new byte[32];
                    System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
                    keyBytes = padded;
                } else if (keyBytes.length > 32) {
                    byte[] truncated = new byte[32];
                    System.arraycopy(keyBytes, 0, truncated, 0, 32);
                    keyBytes = truncated;
                }
            }

            JpaAttributeEncryptor.init(Base64.getEncoder().encodeToString(keyBytes));
            System.out.println("✅ JpaAttributeEncryptor initialized successfully");

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to initialize JpaAttributeEncryptor", e);
        }
    }
}






JAVA

# -------------------------
# model/BaseEntity
# -------------------------
cat > $PROJECT/$BASE/model/BaseEntity.java <<'JAVA'
package com.example.authservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "active")
    private Boolean active = true;

    public String getCreatedBy(){return createdBy;}
    public void setCreatedBy(String v){this.createdBy=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public String getUpdatedBy(){return updatedBy;}
    public void setUpdatedBy(String v){this.updatedBy=v;}
    public LocalDateTime getUpdatedAt(){return updatedAt;}
    public void setUpdatedAt(LocalDateTime v){this.updatedAt=v;}
    public Boolean getActive(){return active;}
    public void setActive(Boolean v){this.active=v;}
}
JAVA

# -------------------------
# model/Role
# -------------------------
cat > $PROJECT/$BASE/model/Role.java <<'JAVA'
package com.example.authservice.model;

import jakarta.persistence.*;

@Entity
@Table(name="roles")
public class Role extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique=true,nullable=false)
    private String name;

    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public String getName(){return name;}
    public void setName(String v){this.name=v;}
}
JAVA

# -------------------------
# model/User
# -------------------------


cat > $PROJECT/$BASE/model/UserId.java <<'JAVA'
package com.example.authservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * ✅ Embeddable logical composite key data for User entity.
 * Note: user_id is excluded from persistence control (managed by User).
 */
@Embeddable
public class UserId implements Serializable {

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;  // Reference only; not persisted by this embeddable

    @Column(name = "username_hash", length = 512)
    private String usernameHash;

    @Column(name = "email_hash", length = 512)
    private String emailHash;

    @Column(name = "mobile_hash", length = 512)
    private String mobileHash;

    @Column(name = "project_type", length = 50)
    private String projectType;

    public UserId() {}

    // Overloaded constructor without userId (used during registration)
    public UserId(String usernameHash, String emailHash, String mobileHash, String projectType) {
        this.usernameHash = usernameHash;
        this.emailHash = emailHash;
        this.mobileHash = mobileHash;
        this.projectType = projectType;
    }

    // Full constructor
    public UserId(Long userId, String usernameHash, String emailHash, String mobileHash, String projectType) {
        this.userId = userId;
        this.usernameHash = usernameHash;
        this.emailHash = emailHash;
        this.mobileHash = mobileHash;
        this.projectType = projectType;
    }

    // ----------------------
    // Getters & Setters
    // ----------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsernameHash() { return usernameHash; }
    public void setUsernameHash(String usernameHash) { this.usernameHash = usernameHash; }

    public String getEmailHash() { return emailHash; }
    public void setEmailHash(String emailHash) { this.emailHash = emailHash; }

    public String getMobileHash() { return mobileHash; }
    public void setMobileHash(String mobileHash) { this.mobileHash = mobileHash; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserId that)) return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(usernameHash, that.usernameHash)
                && Objects.equals(emailHash, that.emailHash)
                && Objects.equals(mobileHash, that.mobileHash)
                && Objects.equals(projectType, that.projectType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, usernameHash, emailHash, mobileHash, projectType);
    }
}


JAVA

cat > $PROJECT/$BASE/model/User.java <<'JAVA'
package com.example.authservice.model;

import jakarta.persistence.*;
import java.util.Set;

/**
 * ✅ User Entity with embedded composite attributes and auto-generated primary key.
 * Uses logical hashes and project type for uniqueness constraints.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username_hash", "project_type"}),
        @UniqueConstraint(columnNames = {"email_hash", "project_type"}),
        @UniqueConstraint(columnNames = {"mobile_hash", "project_type"})
    }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;  // Primary key

    @Embedded
    private UserId compositeId; // Logical composite identifiers (hashes + projectType)

    @Column(name = "username_enc", length = 2048)
    private String usernameEnc;

    @Column(name = "email_enc", length = 2048)
    private String emailEnc;

    @Column(name = "mobile_enc", length = 2048)
    private String mobileEnc;

    private String password;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"), // uses PK
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserDetailMaster detail;

    // ----------------------
    // Constructors
    // ----------------------
    public User() {}

    public User(UserId compositeId, String usernameEnc, String emailEnc, String mobileEnc, String password, Boolean enabled) {
        this.compositeId = compositeId;
        this.usernameEnc = usernameEnc;
        this.emailEnc = emailEnc;
        this.mobileEnc = mobileEnc;
        this.password = password;
        this.enabled = enabled;
    }

    // ----------------------
    // Getters & Setters
    // ----------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public UserId getCompositeId() { return compositeId; }
    public void setCompositeId(UserId compositeId) { this.compositeId = compositeId; }

    public String getUsernameEnc() { return usernameEnc; }
    public void setUsernameEnc(String usernameEnc) { this.usernameEnc = usernameEnc; }

    public String getEmailEnc() { return emailEnc; }
    public void setEmailEnc(String emailEnc) { this.emailEnc = emailEnc; }

    public String getMobileEnc() { return mobileEnc; }
    public void setMobileEnc(String mobileEnc) { this.mobileEnc = mobileEnc; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public UserDetailMaster getDetail() { return detail; }
    public void setDetail(UserDetailMaster detail) { this.detail = detail; }

    // ----------------------
    // Convenience Accessors
    // ----------------------
    public String getUsernameHash() { return compositeId != null ? compositeId.getUsernameHash() : null; }
    public String getEmailHash() { return compositeId != null ? compositeId.getEmailHash() : null; }
    public String getMobileHash() { return compositeId != null ? compositeId.getMobileHash() : null; }
    public String getProjectType() { return compositeId != null ? compositeId.getProjectType() : null; }
}


JAVA

# -------------------------
# model/UserDetailMaster
# -------------------------
cat > $PROJECT/$BASE/model/UserDetailMaster.java <<'JAVA'
package com.example.authservice.model;

import com.example.authservice.crypto.JpaAttributeEncryptor;
import com.example.authservice.util.HmacUtil;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_detail_master",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username_hash"}),
        @UniqueConstraint(columnNames = {"email_hash"}),
        @UniqueConstraint(columnNames = {"mobile_hash"})
    }
)
public class UserDetailMaster extends BaseEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Encrypted + HMAC username
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "username_enc", nullable = false, length = 2048)
    private String username;

    @Column(name = "username_hash", nullable = false, unique = true, length = 512)
    private String usernameHash;

    // Encrypted + HMAC email
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "email_enc", length = 2048)
    private String email;

    @Column(name = "email_hash", unique = true, length = 512)
    private String emailHash;

    // Encrypted + HMAC mobile
    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "mobile_enc", length = 2048)
    private String mobile;

    @Column(name = "mobile_hash", unique = true, length = 512)
    private String mobileHash;

    @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "employee_id_enc", length = 1024)
    private String employeeId;

    @Column(name = "login_date")
    private LocalDateTime loginDate;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    @Column(name = "login_retry")
    private Integer loginRetry = 0;

    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    // ✅ Auto-compute HMAC values before insert/update
    @PrePersist
    @PreUpdate
    public void computeHashes() {
        if (this.username != null) this.usernameHash = HmacUtil.hmacHex(this.username);
        if (this.email != null) this.emailHash = HmacUtil.hmacHex(this.email);
        if (this.mobile != null) this.mobileHash = HmacUtil.hmacHex(this.mobile);
    }

    // ----------------------
    // Getters & Setters
    // ----------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUsernameHash() { return usernameHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailHash() { return emailHash; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getMobileHash() { return mobileHash; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public LocalDateTime getLoginDate() { return loginDate; }
    public void setLoginDate(LocalDateTime loginDate) { this.loginDate = loginDate; }

    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    public Integer getLoginRetry() { return loginRetry; }
    public void setLoginRetry(Integer loginRetry) { this.loginRetry = loginRetry; }

    public Integer getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(Integer failedAttempts) { this.failedAttempts = failedAttempts; }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
}

JAVA

# -------------------------
# model/Session
# -------------------------
cat > $PROJECT/$BASE/model/Session.java <<'JAVA'
package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="sessions")
public class Session extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name="user_id") private User user;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;
    private Boolean revoked = false;
    private String deviceInfo;

    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public User getUser(){return user;}
    public void setUser(User v){this.user=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public void setCreatedAt(LocalDateTime v){this.createdAt=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;}
    public void setExpiresAt(LocalDateTime v){this.expiresAt=v;}
    public Boolean isRevoked(){return revoked;}
    public void setRevoked(Boolean v){this.revoked=v;}
    public String getDeviceInfo(){return deviceInfo;}
    public void setDeviceInfo(String v){this.deviceInfo=v;}
}
JAVA

# -------------------------
# model/RefreshToken
# -------------------------
cat > $PROJECT/$BASE/model/RefreshToken.java <<'JAVA'

package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="refresh_tokens")
public class RefreshToken extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true) 
    private String tokenHash;
    
    @Column(name ="access_token",nullable=false, columnDefinition = "TEXT") 
    private String accesstoken;
    
    @ManyToOne @JoinColumn(name="session_id") 
    private Session session;
    private LocalDateTime expiryDate;


    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public String getAccessToken(){return accesstoken;}
    public void setAccessToken(String v){this.accesstoken=v;}
    public String getTokenHash(){return tokenHash;}
    public void setTokenHash(String v){this.tokenHash=v;}
    public Session getSession(){return session;}
    public void setSession(Session v){this.session=v;}
    public LocalDateTime getExpiryDate(){return expiryDate;}
    public void setExpiryDate(LocalDateTime v){this.expiryDate=v;}
}




JAVA

cat > $PROJECT/$BASE/model/PendingReset.java <<'JAVA'
package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="pending_resets")
public class PendingReset extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String type; // "MPIN", "EMAIL", "MOBILE"
    private String resetToken;
    private LocalDateTime expiresAt;

    // getters/setters
    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public Long getUserId(){return userId;}
    public void setUserId(Long v){this.userId=v;}
    public String getType(){return type;}
    public void setType(String v){this.type=v;}
    public String getResetToken(){return resetToken;}
    public void setResetToken(String v){this.resetToken=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;}
    public void setExpiresAt(LocalDateTime v){this.expiresAt=v;}
}
JAVA


# -------------------------
# dto/LoginRequest, AuthResponse, RegisterRequest, ChangePwdRequest, ForgotPwdRequest
# -------------------------
cat > $PROJECT/$BASE/dto/LoginRequest.java <<'JAVA'
package com.example.authservice.dto;
public class LoginRequest {
    public String loginType; // PASSWORD, OTP, MPIN, AUTHCODE, RSA, BIOMETRIC, PASSKEY, PASSPHRASE, OAUTH
    public String username;
    public String password;
    public String otp;
    public String mpin;
    public String authCode;
    public String passphrase;
    public String rsaChallenge;
    public String deviceInfo;
    public String signature;       // for RSA or WebAuthn
    public String credentialId;    // for WebAuthn

}
JAVA

cat > $PROJECT/$BASE/dto/AuthResponse.java <<'JAVA'
package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("refreshToken")
    private String refreshToken;

    @JsonProperty("expiresIn")
    private long expiresIn;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("sessionId")
    private Long sessionId;

    @JsonProperty("roles")
    private List<String> roles;

    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public AuthResponse(String accessToken, String refreshToken, long expiresIn,
                        Long userId, Long sessionId, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.sessionId = sessionId;
        this.roles = roles;
    }

    // --- Getters ---
    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public List<String> getRoles() {
        return roles;
    }

    // --- Setters ---
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    // --- Debug-friendly representation ---
    @Override
    public String toString() {
        return "AuthResponse{" +
                "userId=" + userId +
                ", sessionId=" + sessionId +
                ", roles=" + roles +
                ", accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : null) + '\'' +
                ", refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(10, refreshToken.length())) + "..." : null) + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}

JAVA

cat > $PROJECT/$BASE/dto/RegisterRequest.java <<'JAVA'
package com.example.authservice.dto;
public class RegisterRequest {
    public String username;
    public String password;
    public String email;
    public String mobile;
    public String projectType;
}
JAVA

cat > $PROJECT/$BASE/dto/ChangePasswordRequest.java <<'JAVA'
package com.example.authservice.dto;
public class ChangePasswordRequest {
    public String username;
    public String currentPassword;
    public String newPassword;
}
JAVA

cat > $PROJECT/$BASE/dto/ForgotPasswordRequest.java <<'JAVA'
package com.example.authservice.dto;
public class ForgotPasswordRequest {
    public String usernameOrEmail;
}
JAVA

# -------------------------
# util/HmacUtil
# -------------------------
cat > $PROJECT/$BASE/util/HmacUtil.java <<'JAVA'
package com.example.authservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Utility for computing/verifying HMAC-SHA256 hashes.
 * Loads HMAC key from application.yml or environment.
 */
@Component
public class HmacUtil {

    private static final String HMAC_ALGO = "HmacSHA256";

    private static byte[] KEY;

    // Load from application.yml -> hmac.key
    @Value("${hmac.key:}")
    private String configKey;

    @PostConstruct
    private void init() {
        if (configKey == null || configKey.isBlank()) {
            // fallback
            configKey = System.getenv().getOrDefault("HMAC_KEY",
                    System.getProperty("hmac.key", "ChangeThisToAnotherKeyForHMAC_ReplaceInProd!"));
        }

        if (configKey.length() < 16) {
            throw new IllegalArgumentException("❌ HMAC key must be at least 16 characters (configured in application.yml or env HMAC_KEY)");
        }

        KEY = configKey.getBytes(StandardCharsets.UTF_8);
        System.out.println("🔑 HmacUtil initialized with key length=" + KEY.length);
    }

    /** Generate HMAC (hex-encoded) */
    public static String hmacHex(String data) {
        if (data == null) return null;
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(KEY, HMAC_ALGO));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    /** Verify if provided hex HMAC matches the computed value */
    public static boolean verifyHmac(String data, String expectedHex) {
        String actual = hmacHex(data);
        return actual != null && actual.equalsIgnoreCase(expectedHex);
    }
}

JAVA

cat > $PROJECT/$BASE/util/RequestContext.java <<'JAVA'
package com.example.authservice.util;

public class RequestContext {
    private static final ThreadLocal<String> ipHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> uaHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> urlHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> methodHolder = new ThreadLocal<>();

    public static void setIp(String ip) { ipHolder.set(ip); }
    public static String getIp() { return ipHolder.get(); }
    public static void clearIp() { ipHolder.remove(); }

    public static void setUserAgent(String ua) { uaHolder.set(ua); }
    public static String getUserAgent() { return uaHolder.get(); }
    public static void clearUserAgent() { uaHolder.remove(); }

    public static void setUrl(String url) { urlHolder.set(url); }
    public static String getUrl() { return urlHolder.get(); }
    public static void clearUrl() { urlHolder.remove(); }

    public static void setMethod(String method) { methodHolder.set(method); }
    public static String getMethod() { return methodHolder.get(); }
    public static void clearMethod() { methodHolder.remove(); }

    public static void clearAll() {
        clearIp();
        clearUserAgent();
        clearUrl();
        clearMethod();
    }
}
JAVA




# -------------------------
# repository interfaces
# -------------------------
cat > $PROJECT/$BASE/repository/UserRepository.java <<'JAVA'


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


JAVA

cat > $PROJECT/$BASE/repository/UserDetailMasterRepository.java <<'JAVA'

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


JAVA

cat > $PROJECT/$BASE/repository/RoleRepository.java <<'JAVA'
package com.example.authservice.repository;
import com.example.authservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(String name);
}
JAVA

cat > $PROJECT/$BASE/repository/SessionRepository.java <<'JAVA'
package com.example.authservice.repository;

import com.example.authservice.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

}
JAVA

cat > $PROJECT/$BASE/repository/RefreshTokenRepository.java <<'JAVA'
package com.example.authservice.repository;

import com.example.authservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    int deleteBySessionId(Long sessionId);
    Optional<RefreshToken> findByTokenHash(String tokenHash); // ✅ use tokenHash instead of token

      /**
     * Find the latest valid (non-expired and active) access_token for a given session.
     * Automatically sorts by createdAt descending and limits to one record.
     */
    Optional<RefreshToken> findTopBySession_IdAndActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(Long sessionId, LocalDateTime now);

    /**
     * Fallback: find latest valid global access_token (any session).
     * Returns the most recently created, active and non-expired token.
     */
    Optional<RefreshToken> findTopByActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(LocalDateTime now);

}

JAVA

cat > $PROJECT/$BASE/repository/OtpLogRepository.java <<'JAVA'
package com.example.authservice.repository;

import com.example.authservice.model.OtpLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpLogRepository extends JpaRepository<OtpLog, Long> {

    // ✅ use the actual entity field: mobileHash
    Optional<OtpLog> findTopByMobileHashAndUsedFalseOrderByCreatedAtDesc(String mobileHash);
}

JAVA

cat > $PROJECT/$BASE/repository/PendingResetRepository.java <<'JAVA'
package com.example.authservice.repository;

import com.example.authservice.model.PendingReset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PendingResetRepository extends JpaRepository<PendingReset,Long> {
    Optional<PendingReset> findByResetToken(String resetToken);
}
JAVA

# -------------------------
# model/OtpLog
# -------------------------
cat > $PROJECT/$BASE/model/OtpLog.java <<'JAVA'
package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="otp_log")
public class OtpLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="mobile_hash", length=512) private String mobileHash;
    @Column(name="otp_hash", length=512) private String otpHash;
    private LocalDateTime expiresAt;
    private Boolean used = false;

    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public String getMobileHash(){return mobileHash;}
    public void setMobileHash(String v){this.mobileHash=v;}
    public String getOtpHash(){return otpHash;}
    public void setOtpHash(String v){this.otpHash=v;}
    public LocalDateTime getExpiresAt(){return expiresAt;}
    public void setExpiresAt(LocalDateTime v){this.expiresAt=v;}
    public boolean isUsed() { return used; }
    public void setUsed(Boolean v){this.used=v;}
}
JAVA

# -------------------------
# service/impl: AuthServiceImpl (main flows implemented)
# -------------------------

cat > $PROJECT/$BASE/service/impl/AuthServiceImpl.java <<'JAVA'
package com.example.authservice.service.impl;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.model.*;
import com.example.authservice.repository.*;
import com.example.authservice.security.JwtUtil;
import com.example.authservice.util.HmacUtil;
import com.example.authservice.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl {

    @Autowired private UserRepository userRepo;
    @Autowired private UserDetailMasterRepository udmRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private SessionRepository sessionRepo;
    @Autowired private RefreshTokenRepository refreshRepo;
    @Autowired private OtpLogRepository otpRepo;
    @Autowired private CredentialRepository credRepo;
    @Autowired private PendingResetRepository resetRepo;
    @Autowired private OtpServiceImpl otpService;
    @Autowired private AuditService auditService;
    @Autowired private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // =====================================================
    // USER REGISTRATION
    // =====================================================
    public User register(String usernamePlain, String password, String emailPlain, String mobilePlain, String projectType) {
        if (usernamePlain == null || usernamePlain.isBlank())
            throw new IllegalArgumentException("Username is required");

        // Hash PII
        String usernameHash = HmacUtil.hmacHex(usernamePlain);
        String emailHash = emailPlain != null ? HmacUtil.hmacHex(emailPlain) : null;
        String mobileHash = mobilePlain != null ? HmacUtil.hmacHex(mobilePlain) : null;

        // Duplicate checks
        if (userRepo.existsByCompositeId_UsernameHash(usernameHash))
            throw new DuplicateKeyException("Username already exists");
        if (emailHash != null && udmRepo.existsByEmailHash(emailHash))
            throw new DuplicateKeyException("Email already exists");
        if (mobileHash != null && udmRepo.existsByMobileHash(mobileHash))
            throw new DuplicateKeyException("Mobile already exists");

        // Build composite key
        UserId compositeId = new UserId(null, usernameHash, emailHash, mobileHash, projectType);

        User user = new User();
        user.setCompositeId(compositeId);
        user.setUsernameEnc(usernamePlain);
        user.setPassword(password != null ? encoder.encode(password) : null);
        user.setEnabled(true);
        user.setCreatedBy("system");

        // Default role
        Role role = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));
        user.setRoles(Set.of(role));

        // Create UserDetailMaster
        UserDetailMaster detail = new UserDetailMaster();
        detail.setUsername(usernamePlain);
        detail.setEmail(emailPlain);
        detail.setMobile(mobilePlain);
        detail.setCreatedBy("system");
        detail.setActive(true);
        detail.setUser(user);
        user.setDetail(detail);

        return userRepo.save(user);
    }

    public User adminregister(String usernamePlain, String password, String emailPlain, String mobilePlain, String projectType) {
        if (usernamePlain == null || usernamePlain.isBlank())
            throw new IllegalArgumentException("Username is required");

        String usernameHash = HmacUtil.hmacHex(usernamePlain);
        if (userRepo.existsByCompositeId_UsernameHash(usernameHash))
            throw new DuplicateKeyException("Username already exists");

        UserId compositeId = new UserId(null, usernameHash,
                emailPlain != null ? HmacUtil.hmacHex(emailPlain) : null,
                mobilePlain != null ? HmacUtil.hmacHex(mobilePlain) : null,
                projectType);

        User user = new User();
        user.setCompositeId(compositeId);
        user.setUsernameEnc(usernamePlain);
        user.setPassword(password != null ? encoder.encode(password) : null);
        user.setEnabled(true);
        user.setCreatedBy("system");

        Role role = roleRepo.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));
        user.setRoles(Set.of(role));

        UserDetailMaster detail = new UserDetailMaster();
        detail.setUsername(usernamePlain);
        detail.setEmail(emailPlain);
        detail.setMobile(mobilePlain);
        detail.setCreatedBy("system");
        detail.setActive(true);
        detail.setUser(user);
        user.setDetail(detail);

        return userRepo.save(user);
    }

    // =====================================================
    // MPIN MANAGEMENT
    // =====================================================
    public void registerMpin(Long userId, String mpin, String deviceInfo) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String hash = encoder.encode(mpin);
        Credential c = credRepo.findByUser_UserIdAndTypeAndMetadata(userId, "MPIN", deviceInfo)
                .orElse(new Credential());
        c.setUser(user);
        c.setType("MPIN");
        c.setCredentialId("mpin-" + userId + "-" + deviceInfo);
        c.setPublicKey(hash);
        c.setMetadata(deviceInfo);
        c.setCreatedBy("system");
        credRepo.save(c);

        auditService.log(userId, "MPIN_REGISTER", "Credential", null, "****",
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    public String requestMpinReset(Long userId, String mobilePlain, String otp) {
        if (!otpService.validateOtp(mobilePlain, otp))
            throw new RuntimeException("Invalid OTP");

        String token = UUID.randomUUID().toString();
        PendingReset pr = new PendingReset();
        pr.setUserId(userId);
        pr.setType("MPIN");
        pr.setResetToken(token);
        pr.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        resetRepo.save(pr);

        auditService.log(userId, "MPIN_RESET_REQUEST", "Credential", null, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return token;
    }

    public void confirmMpinReset(String resetToken, String newMpin, String deviceInfo) {
        PendingReset pr = resetRepo.findByResetToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        if (pr.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Reset token expired");

        registerMpin(pr.getUserId(), newMpin, deviceInfo);
        resetRepo.delete(pr);

        auditService.log(pr.getUserId(), "MPIN_RESET_CONFIRM", "Credential", null, "****",
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    // =====================================================
    // EMAIL / MOBILE CHANGE
    // =====================================================
    public String requestEmailMobileChange(Long userId, String oldValue, String otp, String type) {
        if (!otpService.validateOtp(oldValue, otp))
            throw new RuntimeException("Invalid OTP for " + type);

        String token = UUID.randomUUID().toString();
        PendingReset pr = new PendingReset();
        pr.setUserId(userId);
        pr.setType(type);
        pr.setResetToken(token);
        pr.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        resetRepo.save(pr);

        auditService.log(userId, type + "_CHANGE_REQUEST", "UserDetailMaster", oldValue, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return token;
    }

    public void confirmEmailMobileChange(String resetToken, String newValue, String otp) {
        PendingReset pr = resetRepo.findByResetToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        if (pr.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Reset token expired");
        if (!otpService.validateOtp(newValue, otp))
            throw new RuntimeException("Invalid OTP for new value");

        UserDetailMaster udm = udmRepo.findById(pr.getUserId())
                .orElseThrow(() -> new RuntimeException("User detail not found"));
        if ("EMAIL".equalsIgnoreCase(pr.getType()))
            udm.setEmail(newValue);
        else if ("MOBILE".equalsIgnoreCase(pr.getType()))
            udm.setMobile(newValue);
        udmRepo.save(udm);
        resetRepo.delete(pr);

        auditService.log(pr.getUserId(), "CONTACT_CHANGE", "UserDetailMaster", null, newValue,
                RequestContext.getIp(), RequestContext.getUserAgent());
    }

    // =====================================================
    // LOGIN METHODS
    // =====================================================
    public AuthResponse loginWithPassword(String usernamePlain, String password, String deviceInfo) {
        if (usernamePlain == null || password == null)
            throw new IllegalArgumentException("Username and password are required");

        String usernameHash = HmacUtil.hmacHex(usernamePlain);
        User user = userRepo.findByCompositeId_UsernameHash(usernameHash)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getPassword() == null || !encoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid password");

        UserDetailMaster udm = udmRepo.findByUserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User detail not found"));

        udm.setLastLoginDate(udm.getLoginDate());
        udm.setLoginDate(LocalDateTime.now());
        udm.setFailedAttempts(0);
        udmRepo.save(udm);

        AuthResponse resp = createSessionAndTokens(user, deviceInfo);
        auditService.log(user.getUserId(), "PASSWORD_LOGIN", "User", null, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return resp;
    }

    public AuthResponse loginWithOtp(String mobilePlain, String otp, String deviceInfo, String projectType) {
        String mobileHash = HmacUtil.hmacHex(mobilePlain);
        boolean ok = otpRepo.findAll().stream()
                .anyMatch(log -> mobileHash.equals(log.getMobileHash())
                        && !Boolean.TRUE.equals(log.isUsed())
                        && log.getExpiresAt().isAfter(LocalDateTime.now())
                        && HmacUtil.hmacHex(otp).equals(log.getOtpHash()));
        if (!ok) throw new RuntimeException("Invalid OTP");

        Optional<UserDetailMaster> od = udmRepo.findByMobileHash(mobileHash);
        User user = od.map(d -> userRepo.findById(d.getUserId()).orElseThrow())
                .orElseGet(() -> register(mobilePlain, null, null, mobilePlain, projectType));

        AuthResponse resp = createSessionAndTokens(user, deviceInfo);
        auditService.log(user.getUserId(), "OTP_LOGIN", "User", null, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return resp;
    }

    public AuthResponse loginWithMpin(Long userId, String mpin, String deviceInfo) {
        Credential cred = credRepo.findByUser_UserIdAndTypeAndMetadata(userId, "MPIN", deviceInfo)
                .orElseThrow(() -> new RuntimeException("MPIN not registered"));
        if (!encoder.matches(mpin, cred.getPublicKey()))
            throw new RuntimeException("Invalid MPIN");

        User user = userRepo.findById(userId).orElseThrow();
        AuthResponse resp = createSessionAndTokens(user, deviceInfo);
        auditService.log(userId, "MPIN_LOGIN", "User", null, null,
                RequestContext.getIp(), RequestContext.getUserAgent());
        return resp;
    }

    // =====================================================
    // RSA + PASSKEY
    // =====================================================
    private final Map<Long, String> rsaChallenges = new HashMap<>();
    private final Map<Long, String> webauthnChallenges = new HashMap<>();

    public String createRsaChallenge(Long userId) {
        String challenge = UUID.randomUUID().toString();
        rsaChallenges.put(userId, challenge);
        return challenge;
    }

    public boolean verifyRsaSignature(Long userId, String challenge, String signatureBase64) {
        String expected = rsaChallenges.get(userId);
        if (expected == null || !expected.equals(challenge))
            return false;

        Optional<Credential> credOpt = credRepo.findByCredentialId("rsa-" + userId);
        if (credOpt.isEmpty()) return false;

        try {
            byte[] keyBytes = Base64.getDecoder().decode(credOpt.get().getPublicKey().replaceAll("\\n", ""));
            PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(pubKey);
            sig.update(expected.getBytes(StandardCharsets.UTF_8));
            boolean ok = sig.verify(Base64.getDecoder().decode(signatureBase64));
            if (ok) rsaChallenges.remove(userId);
            return ok;
        } catch (Exception e) {
            return false;
        }
    }

    public AuthResponse loginWithRsa(Long userId, String challenge, String signature, String deviceInfo) {
        if (!verifyRsaSignature(userId, challenge, signature))
            throw new RuntimeException("RSA verification failed");
        User user = userRepo.findById(userId).orElseThrow();
        return createSessionAndTokens(user, deviceInfo);
    }

    public Map<String, Object> webauthnChallenge(Long userId) {
        String challenge = UUID.randomUUID().toString();
        webauthnChallenges.put(userId, challenge);
        return Map.of("challenge", challenge, "userId", userId);
    }

    public boolean verifyWebauthnResponse(Long userId, String credentialId, String signature) {
        String challenge = webauthnChallenges.get(userId);
        if (challenge == null) return false;
        Optional<Credential> credOpt = credRepo.findByCredentialId(credentialId);
        if (credOpt.isEmpty()) return false;
        webauthnChallenges.remove(userId);
        return true;
    }

    public AuthResponse loginWithPasskey(Long userId, String credentialId, String signature, String deviceInfo) {
        if (!verifyWebauthnResponse(userId, credentialId, signature))
            throw new RuntimeException("Passkey verification failed");
        User user = userRepo.findById(userId).orElseThrow();
        return createSessionAndTokens(user, deviceInfo);
    }

    // =====================================================
    // CREDENTIAL MANAGEMENT
    // =====================================================
    public void registerCredential(Long userId, String type, String credentialId, String publicKey) {
        User user = userRepo.findById(userId).orElseThrow();
        Credential c = new Credential();
        c.setUser(user);
        c.setType(type);
        c.setCredentialId(credentialId);
        c.setPublicKey(publicKey);
        c.setCreatedBy("system");
        credRepo.save(c);
    }

    // =====================================================
    // TOKEN HANDLING
    // =====================================================
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null)
            throw new RuntimeException("Missing refresh token");

        String hash = HmacUtil.hmacHex(refreshToken);
        RefreshToken rt = refreshRepo.findByTokenHash(hash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (rt.getExpiryDate().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Expired refresh token");

        Session s = rt.getSession();
        if (s == null || Boolean.TRUE.equals(s.isRevoked()))
            throw new RuntimeException("Invalid session");

        refreshRepo.delete(rt);

        String newRefresh = UUID.randomUUID().toString();
        RefreshToken nrt = new RefreshToken();
        nrt.setTokenHash(HmacUtil.hmacHex(newRefresh));
        nrt.setSession(s);
        nrt.setExpiryDate(LocalDateTime.now().plusDays(14));
        refreshRepo.save(nrt);

        List<String> roles = s.getUser().getRoles().stream().map(Role::getName).toList();
        String access = jwtUtil.generateAccessToken(s.getUser().getUserId(), s.getId(), roles);
        return new AuthResponse(access, newRefresh, jwtUtil.getAccessTokenValiditySeconds());
    }

    private AuthResponse createSessionAndTokens(User user, String deviceInfo) {
        Session session = new Session();
        session.setUser(user);
        session.setDeviceInfo(deviceInfo);
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        sessionRepo.save(session);

        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String access = jwtUtil.generateAccessToken(user.getUserId(), session.getId(), roles);
        String refresh = UUID.randomUUID().toString();

        RefreshToken rt = new RefreshToken();
        rt.setTokenHash(HmacUtil.hmacHex(refresh));
        rt.setAccessToken(access);
        rt.setSession(session);
        rt.setExpiryDate(LocalDateTime.now().plusDays(14));
        refreshRepo.save(rt);

        return new AuthResponse(access, refresh, jwtUtil.getAccessTokenValiditySeconds());
    }

    // =====================================================
    // AUTH CODE
    // =====================================================
    public boolean validateAuthCode(Long userId, String authCode) {
        return "AUTH1234".equals(authCode);
    }
}


JAVA


cat > $PROJECT/$BASE/service/impl/AuditService.java <<'JAVA'
package com.example.authservice.service.impl;

import com.example.authservice.model.AuditLog;
import com.example.authservice.repository.AuditLogRepository;
import com.example.authservice.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    @Autowired private AuditLogRepository repo;

    public void log(Long userId, String action, String entity,
                    String oldValue, String newValue,
                    String ip, String ua) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setEntityName(entity);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);

        // If null passed from caller, fallback to RequestContext
        log.setIpAddress(ip != null ? ip : RequestContext.getIp());
        log.setUserAgent(ua != null ? ua : RequestContext.getUserAgent());
        log.setUrl(RequestContext.getUrl());
        log.setMethod(RequestContext.getMethod());

        repo.save(log);
    }
}
JAVA


cat > $PROJECT/$BASE/service/impl/AuditLogService.java <<'JAVA'

package com.example.authservice.service.impl;

import com.example.authservice.model.AuditLog;
import com.example.authservice.repository.AuditLogRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {
    @Autowired
    private AuditLogRepository repo;

    /**
     * DB-level filtered list
     */
    public List<AuditLog> findLogs(Long userId, String action, String url, String method,
                                   LocalDateTime from, LocalDateTime to) {
        return repo.searchLogs(userId, action, url, method, from, to);
    }

    /**
     * DB-level paged search
     */
    public Page<AuditLog> findLogsPaged(Long userId, String action, String url, String method,
                                        LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return repo.searchLogsPaged(userId, action, url, method, from, to, pageable);
    }

    public String exportToCsv(List<AuditLog> logs) {
        String header = "ID,UserId,Action,Entity,OldValue,NewValue,IP,UserAgent,URL,Method,Timestamp\n";
        StringBuilder sb = new StringBuilder(header);
        for (AuditLog l : logs) {
            sb.append(nullSafe(l.getId())).append(",")
              .append(nullSafe(l.getUserId())).append(",")
              .append(escapeCSV(l.getAction())).append(",")
              .append(escapeCSV(l.getEntityName())).append(",")
              .append(escapeCSV(l.getOldValue())).append(",")
              .append(escapeCSV(l.getNewValue())).append(",")
              .append(escapeCSV(l.getIpAddress())).append(",")
              .append(escapeCSV(l.getUserAgent())).append(",")
              .append(escapeCSV(l.getUrl())).append(",")
              .append(escapeCSV(l.getMethod())).append(",")
              .append(nullSafe(l.getTimestamp()))
              .append("\n");
        }
        return sb.toString();
    }

    public byte[] exportToExcel(List<AuditLog> logs) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("AuditLogs");
            String[] columns = {"ID","UserId","Action","Entity","OldValue","NewValue","IP","UserAgent","URL","Method","Timestamp"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) headerRow.createCell(i).setCellValue(columns[i]);

            int rowIdx = 1;
            for (AuditLog l : logs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(l.getId() != null ? l.getId() : -1);
                row.createCell(1).setCellValue(l.getUserId() != null ? l.getUserId() : -1);
                row.createCell(2).setCellValue(nullSafeString(l.getAction()));
                row.createCell(3).setCellValue(nullSafeString(l.getEntityName()));
                row.createCell(4).setCellValue(nullSafeString(l.getOldValue()));
                row.createCell(5).setCellValue(nullSafeString(l.getNewValue()));
                row.createCell(6).setCellValue(nullSafeString(l.getIpAddress()));
                row.createCell(7).setCellValue(nullSafeString(l.getUserAgent()));
                row.createCell(8).setCellValue(nullSafeString(l.getUrl()));
                row.createCell(9).setCellValue(nullSafeString(l.getMethod()));
                row.createCell(10).setCellValue(l.getTimestamp() != null ? l.getTimestamp().toString() : "");
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel export", e);
        }
    }

    private String escapeCSV(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s + "\"";
        }
        return s;
    }

    private String nullSafeString(String v) { return v == null ? "" : v; }
    private String nullSafe(Object o) { return o == null ? "" : o.toString(); }
}

JAVA






# -------------------------
# service: OtpService (generate + validate)
# -------------------------
cat > $PROJECT/$BASE/service/impl/OtpServiceImpl.java <<'JAVA'
package com.example.authservice.service.impl;

import com.example.authservice.client.NotificationClient;
import com.example.authservice.model.OtpLog;
import com.example.authservice.repository.OtpLogRepository;
import com.example.authservice.security.JwtUtil;
import com.example.authservice.service.UserService;
import com.example.authservice.util.HmacUtil;
import com.example.authservice.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ✅ OTP Service Implementation
 * Handles OTP generation, persistence, validation, and cross-channel notification dispatch.
 */
@Service
public class OtpServiceImpl {

    private static final int EXPIRY_MINUTES = 3;

    @Autowired
    private OtpLogRepository otpLogRepository;

    @Autowired
    private NotificationClient notificationClient;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * ✅ Generate OTP, persist in DB, and send via notification channels
     *
     * @param userId       User ID (can be null or 0 for change requests)
     * @param username     Username or identifier
     * @param mobile       Mobile number
     * @param email        Email address
     * @param type         Primary type of OTP trigger (SMS/EMAIL/WHATSAPP)
     * @param channel      Notification channel
     * @param purpose      OTP purpose (LOGIN, CHANGE, etc.)
     * @return Generated OTP code
     */
    public String generateOtp(String userId, String username, String mobile,
                              String email, String type, String channel, String purpose) {

        StringBuilder errorMessage = new StringBuilder();
        String otpStr = String.format("%06d", new Random().nextInt(1_000_000));

        // -----------------------------------
        // 1️⃣ Persist OTP in DB
        // -----------------------------------
        OtpLog log = new OtpLog();
        log.setMobileHash(HmacUtil.hmacHex(mobile == null ? username : mobile));
        log.setOtpHash(HmacUtil.hmacHex(otpStr));
        log.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        log.setUsed(false);
        log.setCreatedBy(username != null ? username : "system");
        otpLogRepository.save(log);

        // -----------------------------------
        // 2️⃣ Prepare placeholders & audit
        // -----------------------------------
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("otp", otpStr);
        placeholders.put("purpose", purpose);

        Map<String, Object> audit = new HashMap<>();
        audit.put("ipAddress", RequestContext.getIp());
        audit.put("userAgent", RequestContext.getUserAgent());
        audit.put("url", "/api/notifications");
        audit.put("httpMethod", "POST");

        // -----------------------------------
        // 3️⃣ Fetch Access Token (if available)
        // -----------------------------------
        Long uid = null;
        try {
            uid = (userId != null) ? Long.valueOf(userId) : null;
        } catch (Exception ignored) {}

        String token = null;
        if (uid != null && uid > 0) {
            token = userService.getLatestAccessTokenByUserId(uid).orElse(null);
        }

        // -----------------------------------
        // 4️⃣ Determine Template Code Dynamically
        // -----------------------------------
        String templateCode = "OTP_" + (channel != null ? channel.toUpperCase() : "SMS");

        // -----------------------------------
        // 5️⃣ Send OTP Notification(s)
        // -----------------------------------
        try {
            // Primary channel notification (e.g., SMS / EMAIL)
            sendNotification(token, userId, username, mobile, email, channel, templateCode, placeholders, audit);

            // If channel supports secondary fallback (optional logic)
            if (!"INAPP".equalsIgnoreCase(channel)) {
                if ("SMS".equalsIgnoreCase(channel) && email != null) {
                    sendNotification(token, userId, username, mobile, email, "EMAIL", "OTP_EMAIL", placeholders, audit);
                } else if ("EMAIL".equalsIgnoreCase(channel) && mobile != null) {
                    sendNotification(token, userId, username, mobile, email, "SMS", "OTP_SMS", placeholders, audit);
                }
            }

        } catch (Exception ex) {
            errorMessage.append("\n❌ Notification send failed: ").append(ex.getMessage());
        }

        // -----------------------------------
        // 6️⃣ Error Handling
        // -----------------------------------
        if (errorMessage.length() > 0) {
            throw new RuntimeException(errorMessage.toString());
        }

        System.out.println("✅ OTP " + otpStr + " generated for " + username + " (" + channel + ")");
        return otpStr;
    }

    /**
     * ✅ Send notification through Feign client
     */
    private void sendNotification(String token,
                                  String userId,
                                  String username,
                                  String mobile,
                                  String email,
                                  String channel,
                                  String templateCode,
                                  Map<String, Object> placeholders,
                                  Map<String, Object> audit) {
        try {
            Map<String, Object> req = new LinkedHashMap<>();
            req.put("userId", userId);
            req.put("username", username);
            req.put("mobile", mobile);
            req.put("email", email);
            req.put("channel", channel.toUpperCase());
            req.put("templateCode", templateCode);
            req.put("placeholders", placeholders);
            req.put("audit", audit);

            String bearer = (token != null && !token.isBlank())
                    ? (token.startsWith("Bearer ") ? token : "Bearer " + token)
                    : null;

            notificationClient.sendNotification(req, bearer);
            System.out.println("📤 Notification sent via " + channel + " → " + username);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to send " + channel + " notification: " + ex.getMessage(), ex);
        }
    }

    /**
     * ✅ Validate OTP input against latest unused DB entry
     */
    public boolean validateOtp(String mobile, String otpInput) {
        String mobileHash = HmacUtil.hmacHex(mobile);

        OtpLog log = otpLogRepository.findTopByMobileHashAndUsedFalseOrderByCreatedAtDesc(mobileHash)
                .orElseThrow(() -> new RuntimeException("No OTP found or expired"));

        if (log.isUsed() || log.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        boolean valid = HmacUtil.hmacHex(otpInput).equals(log.getOtpHash());
        if (valid) {
            log.setUsed(true);
            otpLogRepository.save(log);
        }
        return valid;
    }
}



JAVA


# -------------------------
# controllers: AuthController + AdminController
# -------------------------
cat > $PROJECT/$BASE/controller/AuthController.java <<'JAVA'


package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.service.impl.AuthServiceImpl;
import com.example.authservice.service.impl.OtpServiceImpl;

import jakarta.servlet.http.HttpServletRequest;

import com.example.authservice.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;
    @Autowired
    private OtpServiceImpl otpService;
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            authService.register(req.username, req.password, req.email, req.mobile, req.projectType);
            return ResponseEntity.ok("registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/adminregister")
    public ResponseEntity<?> adminregister(@RequestBody RegisterRequest req) {
        try {
            authService.adminregister(req.username, req.password, req.email, req.mobile, req.projectType);
            return ResponseEntity.ok("registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            switch (req.loginType) {
                case "PASSWORD":
                    return ResponseEntity.ok(authService.loginWithPassword(req.username, req.password, req.deviceInfo));
                case "OTP":
                    return ResponseEntity.ok(authService.loginWithOtp(req.username, req.otp, req.deviceInfo, "ECOM"));
                case "MPIN":
                    return ResponseEntity
                            .ok(authService.loginWithMpin(Long.parseLong(req.username), req.mpin, req.deviceInfo));
                case "RSA":
                    return ResponseEntity.ok(authService.loginWithRsa(Long.parseLong(req.username), req.rsaChallenge,
                            req.signature, req.deviceInfo));
                case "PASSKEY":
                    return ResponseEntity.ok(authService.loginWithPasskey(Long.parseLong(req.username),
                            req.credentialId, req.signature, req.deviceInfo));
                case "AUTHCODE":
                    if (authService.validateAuthCode(Long.parseLong(req.username), req.authCode)) {
                        return ResponseEntity.ok(authService.loginWithPassword(req.username, "dummy", req.deviceInfo));
                    } else {
                        return ResponseEntity.status(401).body("Invalid auth code");
                    }
                default:
                    return ResponseEntity.badRequest().body("Unsupported loginType");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // POST /credential/rsa/verify
    @PostMapping("/credential/rsa/verify")
    public ResponseEntity<?> rsaVerify(@Valid @RequestBody RsaVerifyRequest req) {
        boolean ok = authService.verifyRsaSignature(req.userId, req.challenge, req.signature);
        if (ok) {
            return ResponseEntity.ok("RSA signature verified successfully");
        } else {
            return ResponseEntity.status(401).body("Invalid RSA signature");
        }
    }

    // WebAuthn: verify (keeps using generic DTO)
    @PostMapping("/credential/webauthn/verify")
    public ResponseEntity<?> webauthnVerify(@Valid @RequestBody CredentialChallengeResponse req) {
        boolean ok = authService.verifyWebauthnResponse(req.userId, req.credentialId, req.signature);
        if (ok) {
            return ResponseEntity.ok(Map.of("userId", req.userId, "verified", true));
        } else {
            return ResponseEntity.status(401)
                    .body(Map.of("userId", req.userId, "verified", false, "message", "Invalid WebAuthn response"));
        }
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            // -----------------------------------
            // 1️⃣ Extract request body fields
            // -----------------------------------
            Long userId = payload.get("userId") != null ? Long.parseLong(payload.get("userId").toString()) : 0L;
            String channel = (String) payload.getOrDefault("channel", "SMS");
            String templateCode = (String) payload.getOrDefault("templateCode", "OTP_SMS");
            String mobile = payload.get("mobile") != null ? payload.get("mobile").toString() : null;
            String email = payload.get("email") != null ? payload.get("email").toString() : null;
            String type = (String) payload.getOrDefault("type", "SMS");
            String purpose = (String) payload.getOrDefault("purpose", "LOGIN");

            // -----------------------------------
            // 2️⃣ Validation of required fields
            // -----------------------------------
            if (type == null || type.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Type is required (SMS, EMAIL, WHATSAPP)"));
            }

            if (("SMS".equalsIgnoreCase(type) || "WHATSAPP".equalsIgnoreCase(type))
                    && (mobile == null || mobile.isBlank())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mobile number is required for " + type));
            }

            if ("EMAIL".equalsIgnoreCase(type) && (email == null || email.isBlank())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required for EMAIL type"));
            }

            // -----------------------------------
            // 3️⃣ Resolve username based on userId or identifier
            // -----------------------------------
            String identifier = (mobile != null && !mobile.isBlank()) ? mobile : email;
            String username;

            if ("LOGIN".equalsIgnoreCase(purpose)) {
                username = userService.getUsernameFromIdentifier(identifier, type);
                if (username == null) {
                    return ResponseEntity.status(404)
                            .body(Map.of("error", "User not found for identifier: " + identifier));
                }
            } else if ("CHANGE".equalsIgnoreCase(purpose)) {
                username = "change-" + identifier;
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Unsupported purpose. Use LOGIN or CHANGE."));
            }

            // -----------------------------------
            // 4️⃣ Validate formats
            // -----------------------------------
            if (mobile != null && !mobile.matches("^[0-9]{10,15}$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid mobile number format"));
            }
            if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email address format"));
            }

            // -----------------------------------
            // 5️⃣ Generate and send OTP (delegates to OtpServiceImpl)
            // -----------------------------------
            String otp = otpService.generateOtp(
                    userId.toString(),
                    username,
                    mobile,
                    email,
                    type,
                    channel,
                    templateCode // Notice: templateCode instead of purpose
            );

            // -----------------------------------
            // 6️⃣ Build and return response
            // -----------------------------------
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "OTP sent successfully via " + type);
            response.put("userId", userId);
            response.put("mobile", mobile);
            response.put("email", email);
            response.put("channel", channel);
            response.put("templateCode", templateCode);
            response.put("purpose", purpose.toUpperCase());
            response.put("otp", otp); // ⚠️ For dev/debug only — remove in production
            response.put("expiresInMinutes", 3);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestParam String refreshToken) {
        try {
            return ResponseEntity.ok(authService.refresh(refreshToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/credential/register")
    public ResponseEntity<?> registerCredential(@RequestBody CredentialRegisterRequest req) {
        try {
            authService.registerCredential(req.userId, req.type, req.credentialId, req.publicKey);
            return ResponseEntity.ok("Credential registered");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/credential/rsa/challenge/{userId}")
    public ResponseEntity<?> rsaChallenge(@PathVariable Long userId) {
        String challenge = authService.createRsaChallenge(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "challenge", challenge));
    }

    @GetMapping("/credential/webauthn/challenge/{userId}")
    public ResponseEntity<?> webauthnChallenge(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.webauthnChallenge(userId));
    }

    @PostMapping("/mpin/register")
    public ResponseEntity<?> registerMpin(@RequestBody MpinRegisterRequest req) {
        try {
            authService.registerMpin(req.userId, req.mpin, req.deviceInfo);
            return ResponseEntity.ok("MPIN registered/reset");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/mpin/verify")
    public ResponseEntity<?> verifyMpin(@RequestBody MpinVerifyRequest req) {
        try {
            return ResponseEntity.ok(authService.loginWithMpin(req.userId, req.mpin, req.deviceInfo));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/mpin/reset/request")
    public ResponseEntity<?> requestMpinReset(@RequestBody MpinResetRequest req) {
        try {
            String token = authService.requestMpinReset(req.userId, req.mobile, req.otp);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/mpin/reset/confirm")
    public ResponseEntity<?> confirmMpinReset(@RequestBody MpinResetConfirmRequest req) {
        try {
            authService.confirmMpinReset(req.resetToken, req.newMpin, req.deviceInfo);
            return ResponseEntity.ok("MPIN reset successful");
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/contact/change/request")
    public ResponseEntity<?> requestChange(@RequestBody EmailMobileChangeRequest req) {
        try {
            String token = authService.requestEmailMobileChange(req.userId, req.oldValue, req.otp, req.type);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @PostMapping("/contact/change/confirm")
    public ResponseEntity<?> confirmChange(@RequestBody EmailMobileChangeConfirmRequest req) {
        try {
            authService.confirmEmailMobileChange(req.resetToken, req.newValue, req.otp);
            return ResponseEntity.ok("Change successful");
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}


JAVA

cat > $PROJECT/$BASE/controller/AdminAuditController.java <<'JAVA'
package com.example.authservice.controller;

import com.example.authservice.model.AuditLog;
import com.example.authservice.service.impl.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    @Autowired
    private AuditLogService auditLogService;

    // List logs (all)
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLog>> getLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(auditLogService.findLogs(userId, action, url, method, from, to));
    }

    // Paged logs
    @GetMapping("/logs/paged")
    public ResponseEntity<Page<AuditLog>> getLogsPaged(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            Pageable pageable) {
        return ResponseEntity.ok(auditLogService.findLogsPaged(userId, action, url, method, from, to, pageable));
    }

    // CSV export
    @GetMapping("/logs/csv")
    public ResponseEntity<byte[]> getLogsCsv(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<AuditLog> logs = auditLogService.findLogs(userId, action, url, method, from, to);
        String csv = auditLogService.exportToCsv(logs);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }

    // Excel export
    @GetMapping("/logs/excel")
    public ResponseEntity<byte[]> getLogsExcel(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<AuditLog> logs = auditLogService.findLogs(userId, action, url, method, from, to);
        byte[] bytes = auditLogService.exportToExcel(logs);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=audit-logs.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}

JAVA


# -------------------------
# security: JwtFilter and SecurityConfig (stateless)
# -------------------------
cat > $PROJECT/$BASE/security/JwtFilter.java <<'JAVA'
package com.example.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                Jws<Claims> claimsJws = jwtUtil.parseToken(token);
                Claims claims = claimsJws.getBody();

                String uid = claims.get("uid").toString();

                List<String> roles = claims.get("roles", List.class);
                if (roles == null) {
                    roles = Collections.emptyList();
                }

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(uid, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}


JAVA

cat > $PROJECT/$BASE/security/SecurityConfig.java <<'JAVA'
package com.example.authservice.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


JAVA
cat > $PROJECT/$BASE/security/RequestContextFilter.java <<'JAVA'
package com.example.authservice.security;

import com.example.authservice.util.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RequestContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");
            String url = request.getRequestURI();
            String method = request.getMethod();

            RequestContext.setIp(ip);
            RequestContext.setUserAgent(ua);
            RequestContext.setUrl(url);
            RequestContext.setMethod(method);

            filterChain.doFilter(request, response);
        } finally {
            RequestContext.clearAll();
        }
    }
}
JAVA



# -------------------------
# init/DataInitializer
# -------------------------
cat > $PROJECT/$BASE/init/DataInitializer.java <<'JAVA'

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

JAVA


cat > $PROJECT/$BASE/model/Credential.java <<'JAVA'

package com.example.authservice.model;

import jakarta.persistence.*;

@Entity
@Table(name="credentials")
public class Credential extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable=false)
    private String type; // RSA, WEBAUTHN, MPIN, etc.

    @Column(nullable=false, length=4096)
    private String publicKey; // PEM for RSA, base64 for WebAuthn, or hashed mpin

    @Column(nullable=false, unique=true)
    private String credentialId; // for WebAuthn credential IDs, RSA key fingerprint, or mpin-id

    @Column(name="metadata", length=1024)
    private String metadata; // optional device info, etc.

    public Long getId(){ return id; }
    public void setId(Long id){ this.id = id; }

    public User getUser(){ return user; }
    public void setUser(User user){ this.user = user; }

    public String getType(){ return type; }
    public void setType(String type){ this.type = type; }

    public String getPublicKey(){ return publicKey; }
    public void setPublicKey(String publicKey){ this.publicKey = publicKey; }

    public String getCredentialId(){ return credentialId; }
    public void setCredentialId(String credentialId){ this.credentialId = credentialId; }

    public String getMetadata(){ return metadata; }
    public void setMetadata(String metadata){ this.metadata = metadata; }
}



JAVA


cat > $PROJECT/$BASE/model/AuditLog.java <<'JAVA'
package com.example.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="audit_log")
public class AuditLog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String action;
    private String entityName;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private String url;
    private String method;
    private LocalDateTime timestamp = LocalDateTime.now();

    // getters and setters
    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public Long getUserId(){return userId;}
    public void setUserId(Long v){this.userId=v;}
    public String getAction(){return action;}
    public void setAction(String v){this.action=v;}
    public String getEntityName(){return entityName;}
    public void setEntityName(String v){this.entityName=v;}
    public String getOldValue(){return oldValue;}
    public void setOldValue(String v){this.oldValue=v;}
    public String getNewValue(){return newValue;}
    public void setNewValue(String v){this.newValue=v;}
    public String getIpAddress(){return ipAddress;}
    public void setIpAddress(String v){this.ipAddress=v;}
    public String getUserAgent(){return userAgent;}
    public void setUserAgent(String v){this.userAgent=v;}
    public String getUrl(){return url;}
    public void setUrl(String v){this.url=v;}
    public String getMethod(){return method;}
    public void setMethod(String v){this.method=v;}
    public LocalDateTime getTimestamp(){return timestamp;}
    public void setTimestamp(LocalDateTime v){this.timestamp=v;}
}
JAVA



cat > $PROJECT/$BASE/repository/CredentialRepository.java <<'JAVA'

package com.example.authservice.repository;

import com.example.authservice.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, Long> {
    Optional<Credential> findByCredentialId(String credentialId);
    Optional<Credential> findByUser_UserIdAndTypeAndMetadata(Long userId, String type, String metadata);
}


JAVA


cat > $PROJECT/$BASE/dto/MpinRegisterRequest.java <<'JAVA'
package com.example.authservice.dto;

public class MpinRegisterRequest {
    public Long userId;
    public String mpin;       // raw mpin, will be hashed
    public String deviceInfo; // optional, to differentiate devices
}
JAVA

cat > $PROJECT/$BASE/dto/MpinVerifyRequest.java <<'JAVA'
package com.example.authservice.dto;

public class MpinVerifyRequest {
    public Long userId;
    public String mpin;
    public String deviceInfo;
}
JAVA

cat > $PROJECT/$BASE/dto/CredentialRegisterRequest.java <<'JAVA'
package com.example.authservice.dto;
public class CredentialRegisterRequest {
    public Long userId;
    public String type; // RSA or WEBAUTHN
    public String credentialId;
    public String publicKey;
}
JAVA

cat > $PROJECT/$BASE/dto/CredentialChallengeResponse.java <<'JAVA'

package com.example.authservice.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Generic credential challenge response used by WebAuthn and other flows.
 * credentialId is optional for RSA verification (we use "rsa-{userId}" by convention).
 */
public class CredentialChallengeResponse {

    @NotNull(message = "userId is required")
    public Long userId;

    // optional for RSA verify; used for WebAuthn / Passkey flows
    public String credentialId;

    @NotNull(message = "challenge is required")
    public String challenge;

    @NotNull(message = "signature is required")
    public String signature;
}


JAVA

cat > $PROJECT/$BASE/dto/MpinResetRequest.java <<'JAVA'
package com.example.authservice.dto;

public class MpinResetRequest {
    public Long userId;
    public String mobile;
    public String otp;
}
JAVA

cat > $PROJECT/$BASE/dto/MpinResetConfirmRequest.java <<'JAVA'
package com.example.authservice.dto;

public class MpinResetConfirmRequest {
    public String resetToken;
    public String newMpin;
    public String deviceInfo;
}
JAVA

cat > $PROJECT/$BASE/dto/EmailMobileChangeRequest.java <<'JAVA'
package com.example.authservice.dto;

public class EmailMobileChangeRequest {
    public Long userId;
    public String oldValue;
    public String otp;   // OTP sent to old value
    public String type;  // "EMAIL" or "MOBILE"
}
JAVA

cat > $PROJECT/$BASE/dto/EmailMobileChangeConfirmRequest.java <<'JAVA'
package com.example.authservice.dto;

public class EmailMobileChangeConfirmRequest {
    public String resetToken;
    public String newValue;
    public String otp;   // OTP sent to new value
}
JAVA

cat > $PROJECT/$BASE/repository/AuditLogRepository.java <<'JAVA'
package com.example.authservice.repository;

import com.example.authservice.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:userId IS NULL OR a.userId = :userId) " +
           "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
           "AND (:from IS NULL OR a.timestamp >= :from) " +
           "AND (:to IS NULL OR a.timestamp <= :to)")
    List<AuditLog> searchLogs(@Param("userId") Long userId,
                              @Param("action") String action,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to);

    @Query("SELECT a FROM AuditLog a " +
           "WHERE (:userId IS NULL OR a.userId = :userId) " +
           "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
           "AND (:from IS NULL OR a.timestamp >= :from) " +
           "AND (:to IS NULL OR a.timestamp <= :to)")
    Page<AuditLog> searchLogsPaged(@Param("userId") Long userId,
                                   @Param("action") String action,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to,
                                   Pageable pageable);

@Query("SELECT a FROM AuditLog a " +
       "WHERE (:userId IS NULL OR a.userId = :userId) " +
       "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
       "AND (:url IS NULL OR a.url LIKE %:url%) " +
       "AND (:method IS NULL OR UPPER(a.method) = UPPER(:method)) " +
       "AND (:from IS NULL OR a.timestamp >= :from) " +
       "AND (:to IS NULL OR a.timestamp <= :to)")
List<AuditLog> searchLogs(@Param("userId") Long userId,
                          @Param("action") String action,
                          @Param("url") String url,
                          @Param("method") String method,
                          @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to);

@Query("SELECT a FROM AuditLog a " +
       "WHERE (:userId IS NULL OR a.userId = :userId) " +
       "AND (:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
       "AND (:url IS NULL OR a.url LIKE %:url%) " +
       "AND (:method IS NULL OR UPPER(a.method) = UPPER(:method)) " +
       "AND (:from IS NULL OR a.timestamp >= :from) " +
       "AND (:to IS NULL OR a.timestamp <= :to)")
Page<AuditLog> searchLogsPaged(@Param("userId") Long userId,
                               @Param("action") String action,
                               @Param("url") String url,
                               @Param("method") String method,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to,
                               Pageable pageable);

}
JAVA


cat > $PROJECT/$BASE/exception/GlobalExceptionHandler.java <<'JAVA'
package com.example.authservice.exception;

import com.example.authservice.dto.ErrorResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException; // Import AccessDeniedException

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.NoSuchElementException; // Import NoSuchElementException

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    // 🔑 Authentication & Security exceptions

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLocked(LockedException ex, HttpServletRequest req) {
        return build(HttpStatus.LOCKED, ex.getMessage(), req);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpired(CredentialsExpiredException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    // 🔑 Data integrity exceptions

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateKeyException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), req);
    }

    // 🔑 Fallback for all other exceptions

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage(), req);
    }

    // ✅ Validation errors -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest req) {
        StringBuilder sb = new StringBuilder("Validation failed: ");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            sb.append(fieldError.getField())
            .append(" ")
            .append(fieldError.getDefaultMessage())
            .append("; ");
        }
        return build(HttpStatus.BAD_REQUEST, sb.toString(), req);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

}



JAVA


cat > $PROJECT/$BASE/dto/ErrorResponse.java <<'JAVA'
package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}

JAVA


cat > $PROJECT/$BASE/dto/UserDetailDto.java <<'JAVA'

package com.example.authservice.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDetailDto {
    public Long userId;
    public String username;   // decrypted by JPA converter if used on entity
    public String email;
    public String mobile;
    public String employeeId;
    public LocalDateTime loginDate;
    public LocalDateTime lastLoginDate;
    public Integer failedAttempts;
    public Boolean accountLocked;
}
JAVA


cat > $PROJECT/$BASE/dto/UserDto.java <<'JAVA'
package com.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * ✅ UserDto
 * Safe data transfer object for exposing user information.
 * Used for:
 *  - GET /users/me
 *  - GET /users/{id}
 *  - GET /admin/users
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long userId;             // System-generated user ID
    private String username;         // Decrypted username
    private String email;            // Decrypted email
    private String mobile;           // Decrypted mobile
    private String projectType;      // ECOM / ASSET / etc.
    private Boolean enabled;         // Account active?
    private Set<String> roles;       // ROLE_USER / ROLE_ADMIN
    private LocalDateTime lastLoginDate; // Last login timestamp

    public UserDto() {}

    public UserDto(Long userId, String username, String email, String mobile,
                   String projectType, Boolean enabled, Set<String> roles, LocalDateTime lastLoginDate) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.mobile = mobile;
        this.projectType = projectType;
        this.enabled = enabled;
        this.roles = roles;
        this.lastLoginDate = lastLoginDate;
    }

    // -------------------------
    // Getters & Setters
    // -------------------------
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public LocalDateTime getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }

    // -------------------------
    // Builder Pattern (Optional)
    // -------------------------
    public static UserDtoBuilder builder() {
        return new UserDtoBuilder();
    }

    public static class UserDtoBuilder {
        private final UserDto dto = new UserDto();

        public UserDtoBuilder userId(Long id) { dto.setUserId(id); return this; }
        public UserDtoBuilder username(String v) { dto.setUsername(v); return this; }
        public UserDtoBuilder email(String v) { dto.setEmail(v); return this; }
        public UserDtoBuilder mobile(String v) { dto.setMobile(v); return this; }
        public UserDtoBuilder projectType(String v) { dto.setProjectType(v); return this; }
        public UserDtoBuilder enabled(Boolean v) { dto.setEnabled(v); return this; }
        public UserDtoBuilder roles(Set<String> v) { dto.setRoles(v); return this; }
        public UserDtoBuilder lastLoginDate(LocalDateTime v) { dto.setLastLoginDate(v); return this; }

        public UserDto build() { return dto; }
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", projectType='" + projectType + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles +
                ", lastLoginDate=" + lastLoginDate +
                '}';
    }
}


JAVA


cat > $PROJECT/$BASE/service/UserService.java <<'JAVA'

package com.example.authservice.service;

import com.example.authservice.dto.UserDto;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.model.User;
import com.example.authservice.model.UserDetailMaster;
import com.example.authservice.repository.RefreshTokenRepository;
import com.example.authservice.repository.UserDetailMasterRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.util.HmacUtil;
import com.example.authservice.util.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired private UserRepository userRepo;
    @Autowired private UserDetailMasterRepository udmRepo;
    @Autowired private RefreshTokenRepository refreshRepo;
    @Autowired private UserMapper userMapper;

    public UserDto getMyProfile(Long currentUserId) {
        if (currentUserId == null) throw new RuntimeException("Unauthorized: No active user context");

        User user = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found for ID: " + currentUserId));

        UserDetailMaster udm = udmRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User details not found for ID: " + currentUserId));

        return userMapper.toDto(user, udm);
    }

    public UserDto getUserProfile(Long targetUserId, Long currentUserId) {
        if (targetUserId == null || currentUserId == null) throw new RuntimeException("Invalid request");

        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin && !targetUserId.equals(currentUserId)) {
            throw new RuntimeException("Access denied: not authorized to view another user's profile");
        }

        User target = userRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        UserDetailMaster udm = udmRepo.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User details not found"));

        return userMapper.toDto(target, udm);
    }

    public java.util.List<com.example.authservice.dto.UserDto> listUsers(Long currentUserId) {
        User current = userRepo.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isAdmin = current.getRoles().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase("ROLE_ADMIN"));

        if (!isAdmin) throw new RuntimeException("Access denied: only admins can list all users");

        var users = userRepo.findAll();
        var out = new java.util.ArrayList<UserDto>();
        for (User u : users) {
            UserDetailMaster udm = udmRepo.findByUserId(u.getUserId()).orElse(null);
            out.add(userMapper.toDto(u, udm));
        }
        return out;
    }

    public Long resolveUserId(String identifier, String type) {
        if (identifier == null || identifier.isBlank()) return null;
        String hash = HmacUtil.hmacHex(identifier);
        if (type != null) {
            switch (type.toUpperCase()) {
                case "USERNAME":
                    Optional<UserDetailMaster> optU = udmRepo.findByUsernameHash(hash);
                    return optU.map(UserDetailMaster::getUserId).orElse(null);
                case "EMAIL":
                    Optional<UserDetailMaster> optE = udmRepo.findByEmailHash(hash);
                    return optE.map(UserDetailMaster::getUserId).orElse(null);
                case "SMS":
                    Optional<UserDetailMaster> optM = udmRepo.findByMobileHash(hash);
                    return optM.map(UserDetailMaster::getUserId).orElse(null);
                default:
                    throw new RuntimeException("Invalid type parameter. Must be one of: username, email, mobile");
            }
        }
        return null; // Ensure a Long is returned
        // Optional<UserDetailMaster> opt = udmRepo.findByUsernameHash(hash);
        // if (opt.isPresent()) return opt.get().getUserId();
        // opt = udmRepo.findByEmailHash(hash);
        // if (opt.isPresent()) return opt.get().getUserId();
        // opt = udmRepo.findByMobileHash(hash);
        // if (opt.isPresent()) return opt.get().getUserId();
        // return null;
    }

    public String getUsernameFromIdentifier(String identifier,String type) {
        if (identifier == null || identifier.isBlank()) return null;
        String hash = HmacUtil.hmacHex(identifier);

        if (type != null) {
            switch (type.toUpperCase()) {
                case "USERNAME":
                    Optional<UserDetailMaster> optU = udmRepo.findByUsernameHash(hash);
                    return optU.map(UserDetailMaster::getUsername).orElse(null);
                case "EMAIL":
                    Optional<UserDetailMaster> optE = udmRepo.findByEmailHash(hash);
                    return optE.map(UserDetailMaster::getUsername).orElse(null);
                case "SMS":
                    Optional<UserDetailMaster> optM = udmRepo.findByMobileHash(hash);
                    return optM.map(UserDetailMaster::getUsername).orElse(null);
                default:
                    throw new RuntimeException("Invalid type parameter. Must be one of: username, email, mobile");
            }
        }
        // Optional<UserDetailMaster> opt = udmRepo.findByUsernameHash(hash);
        // if (opt.isPresent()) return opt.get().getUsername();
        // opt = udmRepo.findByEmailHash(hash);
        // if (opt.isPresent()) return opt.get().getUsername();
        // opt = udmRepo.findByMobileHash(hash);
        // if (opt.isPresent()) return opt.get().getUsername();
        return null;
    }

    /**
     * Best-effort: return the latest non-expired access token found in refresh tokens table.
     * This is a helper for notification/OTP flows when a token is required to call other services.
     */
    public java.util.Optional<String> getLatestAccessTokenByUserId(Long userId) {
        try {
            // 1) attempt to find any refresh token whose session belongs to the user and is active
            var now = LocalDateTime.now();
            var optional = refreshRepo.findTopByActiveIsTrueAndExpiryDateAfterOrderByCreatedAtDesc(now);
            if (optional.isPresent()) {
                var rt = optional.get();
                if (rt.getSession() != null && rt.getSession().getUser() != null
                        && rt.getSession().getUser().getUserId().equals(userId)) {
                    return java.util.Optional.ofNullable(rt.getAccessToken());
                }
            }
        } catch (Exception e) {
            // swallow and return empty
        }
        return java.util.Optional.empty();
    }
}




JAVA

cat > $PROJECT/$BASE/controller/UserController.java <<'JAVA'

package com.example.authservice.controller;

import com.example.authservice.dto.UserDto;
import com.example.authservice.service.UserService;
import com.example.authservice.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    // GET /api/users/me  -> returns current user's profile
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        UserDto dto = userService.getMyProfile(currentUserId);
        return ResponseEntity.ok(dto);
    }

    // GET /api/users/{id} -> admin or self
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long id) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        UserDto dto = userService.getUserProfile(id, currentUserId);
        return ResponseEntity.ok(dto);
    }
}

JAVA

cat > $PROJECT/$BASE/controller/AdminUserController.java <<'JAVA'
package com.example.authservice.controller;

import com.example.authservice.dto.UserDto;
import com.example.authservice.service.UserService;
import com.example.authservice.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired private UserService userService;

    @GetMapping("")
    public ResponseEntity<List<UserDto>> listUsers() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) return ResponseEntity.status(401).build();
        List<UserDto> users = userService.listUsers(currentUserId);
        return ResponseEntity.ok(users);
    }
}

JAVA


cat > $PROJECT/$BASE/util/SecurityUtil.java <<'JAVA'

package com.example.authservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    /**
     * Reads the authenticated principal name and tries to parse it as Long userId.
     * This assumes your JwtFilter sets Authentication.getName() to the user's id string.
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        try {
            return Long.parseLong(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}


JAVA



cat > $PROJECT/$BASE/security/JwtUtil.java <<'EOF'
package com.example.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

@Component
public class JwtUtil {

    private PrivateKey privateKey; // optional for signing
    private PublicKey publicKey;   // required for verifying
    private Key hmacKey;           // fallback if no RSA provided
    private boolean useRsa = false;

    @Value("${jwt.private-key-path:}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-path:}")
    private Resource publicKeyResource;

    @Value("${jwt.secret:}") // fallback secret if no RSA
    private String hmacSecret;

    @Value("${jwt.access-token-validity-seconds:900}")
    private long accessTokenValiditySeconds;

    @Value("${jwt.refresh-token-validity-seconds:1209600}")
    private long refreshTokenValiditySeconds;

    @PostConstruct
    private void init() {
        try {
            if (publicKeyResource != null && publicKeyResource.exists()) {
                this.publicKey = loadPublicKey(publicKeyResource);
                System.out.println("🔒 Loaded RSA public key");
                useRsa = true;
            }

            if (privateKeyResource != null && privateKeyResource.exists()) {
                this.privateKey = loadPrivateKey(privateKeyResource);
                System.out.println("🔑 Loaded RSA private key");
                useRsa = true;
            }

            if (!useRsa) {
                if (hmacSecret == null || hmacSecret.length() < 32) {
                    throw new IllegalArgumentException("❌ jwt.secret must be at least 32 characters if RSA keys not provided");
                }
                this.hmacKey = Keys.hmacShaKeyFor(hmacSecret.getBytes());
                System.out.println("⚡ Using HMAC (HS256) for JWT");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JwtUtil: " + e.getMessage(), e);
        }
    }

     public long getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public long getRefreshTokenValiditySeconds() {
        return refreshTokenValiditySeconds;
    }
    private PrivateKey loadPrivateKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            String keyPem = new String(is.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(keyPem);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
        }
    }

    private PublicKey loadPublicKey(Resource resource) throws Exception {
        try (InputStream is = resource.getInputStream()) {
            String keyPem = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(keyPem);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        }
    }

    // ----------------------------
    // Token Generation
    // ----------------------------
    public String generateAccessToken(Long userId, Long sessionId, List<String> roles) {
        Instant now = Instant.now();
        Date expiry = Date.from(now.plusSeconds(accessTokenValiditySeconds));

        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("uid", userId, "sid", sessionId, "roles", roles))
                .setIssuedAt(Date.from(now))
                .setExpiration(expiry);

        if (useRsa && privateKey != null) {
            builder.signWith(privateKey, SignatureAlgorithm.RS256);
        } else {
            builder.signWith(hmacKey, SignatureAlgorithm.HS256);
        }
        return builder.compact();
    }

    public String generateRefreshToken(Long userId, Long sessionId) {
        Instant now = Instant.now();
        Date expiry = Date.from(now.plusSeconds(refreshTokenValiditySeconds));

        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .addClaims(Map.of("uid", userId, "sid", sessionId))
                .setIssuedAt(Date.from(now))
                .setExpiration(expiry);

        if (useRsa && privateKey != null) {
            builder.signWith(privateKey, SignatureAlgorithm.RS256);
        } else {
            builder.signWith(hmacKey, SignatureAlgorithm.HS256);
        }
        return builder.compact();
    }

    // ----------------------------
    // Token Validation
    // ----------------------------
    public Jws<Claims> parseToken(String token) {
        JwtParserBuilder parser = Jwts.parserBuilder();
        if (useRsa) {
            parser.setSigningKey(publicKey);
        } else {
            parser.setSigningKey(hmacKey);
        }
        return parser.build().parseClaimsJws(token);
    }

    public String getUsernameFromToken(String token) {
        return parseToken(token).getBody().getSubject();
    }

    public Long getSessionIdFromToken(String token) {
        Object sid = parseToken(token).getBody().get("sid");
        return sid != null ? Long.valueOf(sid.toString()) : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Object roles = parseToken(token).getBody().get("roles");
        return roles instanceof List ? (List<String>) roles : List.of();
    }
}


EOF


cat > $PROJECT/$BASE/dto/RegisterResponse.java <<'EOF'

package com.example.authservice.dto;

public class RegisterResponse {
    private String username;
    private String email;
    private String mobile;
    private String employeeId;

    public RegisterResponse(String username, String email, String mobile, String employeeId) {
        this.username = username;
        this.email = email;
        this.mobile = mobile;
        this.employeeId = employeeId;
    }

    // getters & setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}


EOF



cat > $PROJECT/$BASE/client/NotificationClient.java <<'EOF'





package com.example.authservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;


@FeignClient(
    name = "notificationClient", 
    url = "${notification.service.url}"  // ✅ property must exist
    )
public interface NotificationClient {

    @PostMapping
    // void sendNotification (@RequestBody Map<String, Object> payload); 
    void sendNotification (@RequestBody Map<String, Object> payload,
    @RequestHeader("Authorization") String bearerToken); 

    // void sendNotification(
    // @RequestParam("mobile") String mobile, 
    // @RequestParam("username") String username,
    // @RequestParam("email") String email,
    // @RequestParam("templateCode") String templateCode,
    // @RequestParam Map<String, String> placeholders);

    
}








EOF




# # -------------------------
# # util/JwtUtil
# # -------------------------
# cat > $PROJECT/$BASE/util/JwtUtil.java <<'JAVA'
# package com.example.authservice.util;

# import io.jsonwebtoken.*;
# import io.jsonwebtoken.security.Keys;
# import org.springframework.beans.factory.annotation.Value;
# import org.springframework.stereotype.Component;

# import javax.annotation.PostConstruct;
# import java.security.Key;
# import java.util.*;

# @Component
# public class JwtUtil {

#     private Key key;

#     @Value("${jwt.secret:}")
#     private String configSecret;

#     @Value("${jwt.access-token-validity-seconds:900}")
#     private long accessTokenValiditySeconds;

#     @Value("${jwt.refresh-token-validity-seconds:1209600}")
#     private long refreshTokenValiditySeconds;

#     @PostConstruct
#     private void init() {
#         // Fallback to env/system if not set in application.yml
#         if (configSecret == null || configSecret.isBlank()) {
#             configSecret = System.getenv().getOrDefault("JWT_SECRET",
#                     System.getProperty("jwt.secret", "ChangeThisJwtSecret_ReplaceInProd!"));
#         }

#         if (configSecret.length() < 32) {
#             throw new IllegalArgumentException("❌ jwt.secret must be at least 32 characters (configured in application.yml or env)");
#         }

#         this.key = Keys.hmacShaKeyFor(configSecret.getBytes());
#         System.out.println("🔑 JwtUtil initialized with secret length=" + configSecret.length());
#     }

#     /**
#      * Generate a signed JWT access token
#      */
#     public String generateAccessToken(Long userId, Long sessionId, List<String> roles) {
#         return generateToken(userId, sessionId, roles, accessTokenValiditySeconds * 1000);
#     }

#     /**
#      * Generate a signed JWT refresh token
#      */
#     public String generateRefreshToken(Long userId, Long sessionId) {
#         return generateToken(userId, sessionId, List.of(), refreshTokenValiditySeconds * 1000);
#     }

#     private String generateToken(Long userId, Long sessionId, List<String> roles, long ttlMillis) {
#         long now = System.currentTimeMillis();

#         Map<String, Object> claims = new HashMap<>();
#         claims.put("uid", userId);
#         claims.put("sid", sessionId);
#         claims.put("roles", roles);

#         JwtBuilder builder = Jwts.builder()
#                 .setClaims(claims)
#                 .setSubject(String.valueOf(userId))
#                 .setIssuedAt(new Date(now))
#                 .signWith(key, SignatureAlgorithm.HS256);

#         if (ttlMillis > 0) {
#             builder.setExpiration(new Date(now + ttlMillis));
#         }

#         return builder.compact();
#     }

#     /**
#      * Parse and validate a JWT token. Throws if invalid/expired.
#      */
#     public Jws<Claims> parseToken(String token) {
#         return Jwts.parserBuilder()
#                 .setSigningKey(key)
#                 .build()
#                 .parseClaimsJws(token);
#     }

#     public String getUsernameFromToken(String token) {
#         return parseToken(token).getBody().getSubject();
#     }

#     public Long getSessionIdFromToken(String token) {
#         Object sid = parseToken(token).getBody().get("sid");
#         return sid != null ? Long.valueOf(sid.toString()) : null;
#     }

#     @SuppressWarnings("unchecked")
#     public List<String> getRolesFromToken(String token) {
#         Object roles = parseToken(token).getBody().get("roles");
#         return roles instanceof List ? (List<String>) roles : List.of();
#     }
# }


# JAVA

cat > $PROJECT/$BASE/config/EncryptionProperties.java <<'JAVA'

package com.example.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {
    /**
     * 32-byte secret key for AES encryption.
     * Must be exactly 32 chars for AES-256.
     */
    private String key = "ChangeThisTo32ByteKey_ReplaceInProd!";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

JAVA



cat > $PROJECT/$BASE/crypto/JpaAttributeEncryptor.java <<'JAVA'

package com.example.authservice.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Base64;

/**
 * JPA AttributeConverter that encrypts/decrypts String values transparently.
 * 
 * Note: Since AttributeConverter is managed by JPA (not Spring),
 * we cannot directly use @Autowired or @Value here.
 * Instead, we initialize the key once from environment variables.
 */
@Converter(autoApply = false)
public class JpaAttributeEncryptor implements AttributeConverter<String, String> {

    private static AesGcmEncryptor encryptor;

    // Initialize the encryptor once at application startup
    public static void init(String base64Key) {
        byte[] key = Base64.getDecoder().decode(base64Key);
        encryptor = new AesGcmEncryptor(key);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return attribute == null ? null : encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData == null ? null : encryptor.decrypt(dbData);
    }
}


JAVA
cat > $PROJECT/$BASE/crypto/AesGcmEncryptor.java <<'JAVA'

package com.example.authservice.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 GCM encryption/decryption utility.
 * Key must be 32 bytes (256-bit).
 */
public class AesGcmEncryptor {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;       // 96 bits
    private static final int TAG_LENGTH = 128;     // 128-bit auth tag

    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmEncryptor(byte[] key) {
        if (key == null || key.length != 32) {
            throw new IllegalArgumentException("AES-GCM key must be 32 bytes (256-bit). Provided: " + (key != null ? key.length : 0));
        }
        this.key = key;
    }

    /**
     * Encrypt plain text into Base64 encoded cipher text
     */
    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // prepend IV for decryption
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encryption failed", e);
        }
    }

    /**
     * Decrypt Base64 encoded cipher text back to plain text
     */
    public String decrypt(String base64Cipher) {
        if (base64Cipher == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(base64Cipher);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] cipherText = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decryption failed", e);
        }
    }
}


JAVA

cat > $PROJECT/$BASE/mapper/UserMapper.java <<'JAVA'

package com.example.authservice.mapper;

import com.example.authservice.dto.UserDto;
import com.example.authservice.model.Role;
import com.example.authservice.model.User;
import com.example.authservice.model.UserDetailMaster;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ✅ UserMapper
 * Safely converts between entity models and DTOs.
 * Now aligned with the updated User model where projectType is inside compositeId.
 */
@Component
public class UserMapper {

    /**
     * Convert entity → DTO (safe for API responses)
     */
    public UserDto toDto(User user, UserDetailMaster detail) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(detail != null ? detail.getUsername() : null);
        dto.setEmail(detail != null ? detail.getEmail() : null);
        dto.setMobile(detail != null ? detail.getMobile() : null);

        // ✅ Access projectType from compositeId
        dto.setProjectType(user.getCompositeId() != null ? user.getCompositeId().getProjectType() : null);

        dto.setEnabled(user.getEnabled());
        dto.setRoles(mapRoles(user.getRoles()));
        dto.setLastLoginDate(detail != null ? detail.getLastLoginDate() : null);

        return dto;
    }

    /**
     * Convert list of Role entities → set of role names
     */
    private Set<String> mapRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) return Set.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Optional: Convert DTO → entity (used for admin updates or registration)
     */
    public void updateEntityFromDto(UserDto dto, User user, UserDetailMaster detail) {
        if (dto == null || user == null || detail == null) return;

        // Basic safe updates
        Optional.ofNullable(dto.getUsername()).ifPresent(detail::setUsername);
        Optional.ofNullable(dto.getEmail()).ifPresent(detail::setEmail);
        Optional.ofNullable(dto.getMobile()).ifPresent(detail::setMobile);
        Optional.ofNullable(dto.getEnabled()).ifPresent(user::setEnabled);

        // ✅ Safely update projectType in the embedded key
        if (dto.getProjectType() != null && user.getCompositeId() != null) {
            user.getCompositeId().setProjectType(dto.getProjectType());
        }

        // Roles are handled via RoleRepository in the service layer
    }
}


JAVA


cat > $PROJECT/$BASE/dto/RsaVerifyRequest.java <<'JAVA'

package com.example.authservice.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Dedicated request DTO for RSA verification - no credentialId required.
 */
public class RsaVerifyRequest {
    @NotNull(message = "userId is required")
    public Long userId;

    @NotNull(message = "challenge is required")
    public String challenge;

    @NotNull(message = "signature is required")
    public String signature;
}

JAVA
# -------------------------
# README
# -------------------------
cat > $PROJECT/README.md <<'MD'
Auth Service scaffold created by setup-auth-service.sh

Steps:
1. Update application.yml or set env: DB credentials, ENCRYPTION_KEY(32 bytes), HMAC_KEY, JWT_SECRET
2. Create DB: CREATE DATABASE authdb;
3. mvn -f auth-service/pom.xml clean package
4. java -jar auth-service/target/auth-service-0.0.1-SNAPSHOT.jar

Notes:
- Replace secrets with secure values and use a secrets manager (Vault/KMS).
- Implement SMS/email provider for OTP.
- WebAuthn/passkey requires front-end and a FIDO2 server library.

echo "✔️ setup-auth-service.sh finished. Project created at ./auth-service"
echo "Next steps: edit application.yml to provide real secrets and DB credentials, then build."

