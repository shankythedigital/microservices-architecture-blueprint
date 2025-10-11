#!/usr/bin/env bash
set -euo pipefail



# setup-notification-service.sh
# Generates a full Spring Boot 3.x Notification Service skeleton (Jakarta APIs).
# - All Java classes (entities, repos, services, controllers, config, crypto, util, exceptions)
# - Flyway migrations (V1__init.sql + V2__seed_templates.sql)
# - application.yml and pom.xml (Spring Boot 3 parent)
# - .env.notify with generated encryption keys (base64 32 bytes)
#
# Usage: run in an empty dir. The script creates ./notification-service.
#

ROOT_DIR="$(pwd)/notification-service"
PKG_DIR="$ROOT_DIR/src/main/java/com/example/notification"
RES_DIR="$ROOT_DIR/src/main/resources"

echo "Creating project folders at $ROOT_DIR ..."
mkdir -p "$PKG_DIR"/{config,controller,dto,entity,entity/templates,repository,service,crypto,util,exception,security}
mkdir -p "$RES_DIR/db/migration"


KEYS_DIR="$RES_DIR/keys"

echo "Creating project folders at $ROOT_DIR ..."
mkdir -p "$PKG_DIR"/{config,controller,dto,entity,entity/templates,repository,service,crypto,util,exception,security}
mkdir -p "$RES_DIR/db/migration" "$KEYS_DIR"

#########################################
# 1. Copy JWT public key from auth-service
#########################################
AUTH_KEYS_DIR="$(pwd)/auth-service/src/main/resources/keys"

if [ -f "$AUTH_KEYS_DIR/jwt-public.pem" ]; then
  cp "$AUTH_KEYS_DIR/jwt-public.pem" "$KEYS_DIR/jwt-public.pem"
  echo "Linked jwt-public.pem from auth-service into notification-service."
else
  echo "⚠️ WARNING: jwt-public.pem not found in auth-service. Please run:"
  echo "   openssl genrsa -out auth-service/src/main/resources/keys/jwt-private.pem 2048"
  echo "   openssl rsa -in auth-service/src/main/resources/keys/jwt-private.pem -pubout -out auth-service/src/main/resources/keys/jwt-public.pem"
  exit 1
fi



# ---------------------------
# pom.xml (Spring Boot 3 parent + Jakarta)
# ---------------------------
cat > "$ROOT_DIR/pom.xml" <<'XML'
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
    <relativePath/>
  </parent>

  <groupId>com.example</groupId>
  <artifactId>notification-service</artifactId>
  <version>1.0.0</version>
  <name>notification-service</name>

  <properties>
    <java.version>17</java.version>
  </properties>

  <dependencies>
    <!-- Spring Boot starters -->
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
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <!-- spring-dotenv to load .env files automatically -->
    <dependency>
      <groupId>me.paulschwarz</groupId>
      <artifactId>spring-dotenv</artifactId>
      <version>3.0.0</version>
    </dependency>

    <!-- Flyway -->
    <!-- <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency> -->

    <!-- MySQL runtime -->
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- Lombok (optional, IDE support recommended) -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>

    <!-- Jakarta annotation & persistence & servlet API (explicit to avoid IDE compile errors) -->
    <dependency>
      <groupId>jakarta.annotation</groupId>
      <artifactId>jakarta.annotation-api</artifactId>
    </dependency>

    <dependency>
      <groupId>jakarta.persistence</groupId>
      <artifactId>jakarta.persistence-api</artifactId>
    </dependency>

    <dependency>
      <groupId>jakarta.servlet</groupId>
      <artifactId>jakarta.servlet-api</artifactId>
      <scope>provided</scope>
    </dependency>

    <!-- Jackson -->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>

    

    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>

    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
    </dependency>

    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
    </dependency>


    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-oauth2-client</artifactId>
        <version>5.7.0</version> <!-- Use the appropriate version -->
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>


    <dependency>

        <groupId>org.springframework.security</groupId>

        <artifactId>spring-security-oauth2-resource-server</artifactId>

        <version>5.5.0</version> <!-- Use the appropriate version -->

    </dependency>

    <dependency>

        <groupId>org.springframework.security</groupId>

        <artifactId>spring-security-oauth2-core</artifactId>

        <version>5.5.0</version> <!-- Use the appropriate version -->

    </dependency>
    
    <dependency>

        <groupId>io.github.openfeign</groupId>

        <artifactId>feign-core</artifactId>

        <version>11.10</version> <!-- Use the latest version available -->

    </dependency>


    <dependency>

        <groupId>org.springframework.security</groupId>

        <artifactId>spring-security-oauth2-jose</artifactId>

        <version>5.5.0</version> <!-- Use the appropriate version -->

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
XML

# ---------------------------
# Application starter
# ---------------------------
cat > "$PKG_DIR/NotificationServiceApplication.java" <<'JAVA'
package com.example.notification;

// import com.example.notification.crypto.JpaAttributeEncryptor;
import com.example.notification.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import java.util.Base64;

@SpringBootApplication
public class NotificationServiceApplication {

    // @Value("${notify.enc.key}")
    // private String notifyEncKey;

    // @Value("${notify.hmac.key}")
    // private String notifyHmacKey;

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    // @PostConstruct
    // public void initCrypto() {
    //     byte[] enc = Base64.getDecoder().decode(notifyEncKey);
    //     JpaAttributeEncryptor.init(enc);
    //     HashUtil.init(notifyHmacKey); // base64 string
    // }
}

JAVA

# ---------------------------
# application.yml
# ---------------------------
cat > "$RES_DIR/application.yml" <<'YML'
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/notificationdb?useSSL=false&allowPublicKeyRetrieval=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:Snmysql@1110}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: false
  <!-- flyway: 
      enabled: true
      locations: classpath:db/migration -->

notify:
  encryption:
    key: ${NOTIFY_ENC_KEY:ChangeThisTo32ByteKey_ReplaceInProd!}
  hmac:
    key: ${NOTIFY_HMAC_KEY:ChangeThisToAnotherKeyForHMAC_ReplaceInProd!}

security:
  access-token: ${ACCESS_TOKEN:change_this_token}

server:
  port: 8082
  
jwt:
  public-key-path: classpath:keys/jwt-public.pem



auth:
  service:
    url: http://localhost:8081/api/
  client-id: notification-service
  client-secret: notify-secret

YML

# ---------------------------
# Crypto utilities
# ---------------------------
cat > "$PKG_DIR/crypto/AesGcmEncryptor.java" <<'JAVA'
package com.example.notification.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AesGcmEncryptor {
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_BIT_LENGTH = 128;
    private final byte[] key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmEncryptor(byte[] key) {
        if (key == null || key.length != 32) throw new IllegalArgumentException("Key must be 32 bytes");
        this.key = key;
    }

    public String encrypt(String plain) {
        if (plain == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec ks = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, ks, new GCMParameterSpec(TAG_BIT_LENGTH, iv));
            byte[] ct = cipher.doFinal(plain.getBytes("UTF-8"));
            byte[] combined = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ct, 0, combined, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String data) {
        if (data == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(data);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            byte[] ct = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, ct, 0, ct.length);
            Cipher cipher = Cipher.getInstance(ALGO);
            SecretKeySpec ks = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, ks, new GCMParameterSpec(TAG_BIT_LENGTH, iv));
            byte[] plain = cipher.doFinal(ct);
            return new String(plain, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
JAVA

cat > "$PKG_DIR/crypto/JpaAttributeEncryptor.java" <<'JAVA'
package com.example.notification.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JpaAttributeEncryptor implements AttributeConverter<String, String> {
    private static AesGcmEncryptor encryptor;

    public static void init(byte[] key) {
        encryptor = new AesGcmEncryptor(key);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return encryptor.decrypt(dbData);
    }
}
JAVA

# ---------------------------
# Util: HashUtil (HMAC-SHA256 fingerprint, base64)
# ---------------------------
cat > "$PKG_DIR/util/HashUtil.java" <<'JAVA'
package com.example.notification.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HashUtil {
    private static String base64Key;

    public static void init(String base64KeyIn) {
        base64Key = base64KeyIn;
    }

    public static String fingerprint(String value) {
        if (value == null) return null;
        try {
            byte[] key = Base64.getDecoder().decode(base64Key);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] out = mac.doFinal(value.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }
}
JAVA

# ---------------------------
# BaseEntity
# ---------------------------
cat > "$PKG_DIR/entity/BaseEntity.java" <<'JAVA'
package com.example.notification.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Column;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "active")
    private Boolean active = true;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // getters & setters
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
JAVA

# ---------------------------
# Template Entities
# ---------------------------
cat > "$PKG_DIR/entity/templates/NotificationTemplateMaster.java" <<'JAVA'
package com.example.notification.entity.templates;

import com.example.notification.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "notification_template_master")
public class NotificationTemplateMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", unique = true, nullable = false)
    private String templateCode;

    private String name;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "JSON")
    private String placeholders;

    private Boolean active;
    private String projectType;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getPlaceholders() { return placeholders; }
    public void setPlaceholders(String placeholders) { this.placeholders = placeholders; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
}
JAVA

cat > "$PKG_DIR/entity/templates/SmsTemplateMaster.java" <<'JAVA'
package com.example.notification.entity.templates;

import com.example.notification.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "sms_template_master")
public class SmsTemplateMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", unique = true, nullable = false)
    private String templateCode;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "JSON")
    private String placeholders;

    private Boolean active;
    private String projectType;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getPlaceholders() { return placeholders; }
    public void setPlaceholders(String placeholders) { this.placeholders = placeholders; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
}
JAVA

cat > "$PKG_DIR/entity/templates/WhatsappTemplateMaster.java" <<'JAVA'
package com.example.notification.entity.templates;

import com.example.notification.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "whatsapp_template_master")
public class WhatsappTemplateMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", unique = true, nullable = false)
    private String templateCode;

    private String name;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "JSON")
    private String placeholders;

    private Boolean active;
    private String projectType;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getPlaceholders() { return placeholders; }
    public void setPlaceholders(String placeholders) { this.placeholders = placeholders; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
}
JAVA

cat > "$PKG_DIR/entity/templates/InappTemplateMaster.java" <<'JAVA'
package com.example.notification.entity.templates;

import com.example.notification.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "inapp_template_master")
public class InappTemplateMaster extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_code", unique = true, nullable = false)
    private String templateCode;

    private String name;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "JSON")
    private String placeholders;

    private Boolean active;
    private String projectType;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getPlaceholders() { return placeholders; }
    public void setPlaceholders(String placeholders) { this.placeholders = placeholders; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
}
JAVA

# ---------------------------
# Log Entities (with encrypted fields via JpaAttributeEncryptor)
# ---------------------------
cat > "$PKG_DIR/entity/SmsLog.java" <<'JAVA'
package com.example.notification.entity;

// import com.example.notification.crypto.JpaAttributeEncryptor;
import jakarta.persistence.*;

@Entity
@Table(name = "sms_log")
public class SmsLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Convert(converter = JpaAttributeEncryptor.class)
    private String username;

    // @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "mobile_enc", length = 2048)
    private String mobile;

    private String mobileFingerprint;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String templateCode;
    private String providerMessageId;

    @Column(columnDefinition = "TEXT")
    private String providerResponse;

    private Integer retries = 0;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getMobileFingerprint() { return mobileFingerprint; }
    public void setMobileFingerprint(String mobileFingerprint) { this.mobileFingerprint = mobileFingerprint; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    public Integer getRetries() { return retries; }
    public void setRetries(Integer retries) { this.retries = retries; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
JAVA

cat > "$PKG_DIR/entity/NotificationLog.java" <<'JAVA'
package com.example.notification.entity;

// import com.example.notification.crypto.JpaAttributeEncryptor;
import jakarta.persistence.*;

@Entity
@Table(name = "notification_log")
public class NotificationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Convert(converter = JpaAttributeEncryptor.class)
    private String username;

    // @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "email_enc", length = 2048)
    private String email;

    private String emailFingerprint;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String channel;
    private String templateCode;

    @Column(columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEmailFingerprint() { return emailFingerprint; }
    public void setEmailFingerprint(String emailFingerprint) { this.emailFingerprint = emailFingerprint; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
JAVA

cat > "$PKG_DIR/entity/WhatsappLog.java" <<'JAVA'
package com.example.notification.entity;

// import com.example.notification.crypto.JpaAttributeEncryptor;
import jakarta.persistence.*;

@Entity
@Table(name = "whatsapp_log")
public class WhatsappLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Convert(converter = JpaAttributeEncryptor.class)
    private String username;

    // @Convert(converter = JpaAttributeEncryptor.class)
    @Column(name = "mobile_enc", length = 2048)
    private String mobile;

    private String mobileFingerprint;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String templateCode;

    @Column(columnDefinition = "TEXT")
    private String providerResponse;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getMobileFingerprint() { return mobileFingerprint; }
    public void setMobileFingerprint(String mobileFingerprint) { this.mobileFingerprint = mobileFingerprint; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
JAVA

cat > "$PKG_DIR/entity/InappLog.java" <<'JAVA'
package com.example.notification.entity;

// import com.example.notification.crypto.JpaAttributeEncryptor;
import jakarta.persistence.*;

@Entity
@Table(name = "inapp_log")
public class InappLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Convert(converter = com.example.notification.crypto.JpaAttributeEncryptor.class)
    private String username;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String templateCode;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
JAVA

cat > "$PKG_DIR/entity/AuditLog.java" <<'JAVA'
package com.example.notification.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "entity_name")
    private String entityName;

    @Column(name = "entity_id")
    private Long entityId;

    private String action; // CREATE, UPDATE, DELETE, READ

    @Column(columnDefinition = "JSON")
    private String oldValue;

    @Column(columnDefinition = "JSON")
    private String newValue;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    private String url;

    private String httpMethod;

    @Column(name = "user_id", nullable = false)
    private String userId;

    // getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
JAVA

# ---------------------------
# Repositories
# ---------------------------
cat > "$PKG_DIR/repository/NotificationTemplateRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.templates.NotificationTemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplateMaster, Long> {
    Optional<NotificationTemplateMaster> findByTemplateCode(String templateCode);
}
JAVA

cat > "$PKG_DIR/repository/SmsTemplateRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.templates.SmsTemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SmsTemplateRepository extends JpaRepository<SmsTemplateMaster, Long> {
    Optional<SmsTemplateMaster> findByTemplateCode(String templateCode);
}
JAVA

cat > "$PKG_DIR/repository/WhatsappTemplateRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.templates.WhatsappTemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WhatsappTemplateRepository extends JpaRepository<WhatsappTemplateMaster, Long> {
    Optional<WhatsappTemplateMaster> findByTemplateCode(String templateCode);
}
JAVA

cat > "$PKG_DIR/repository/InappTemplateRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.templates.InappTemplateMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InappTemplateRepository extends JpaRepository<InappTemplateMaster, Long> {
    Optional<InappTemplateMaster> findByTemplateCode(String templateCode);
}
JAVA

cat > "$PKG_DIR/repository/SmsLogRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {
}
JAVA

cat > "$PKG_DIR/repository/NotificationLogRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}
JAVA

cat > "$PKG_DIR/repository/WhatsappLogRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.WhatsappLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhatsappLogRepository extends JpaRepository<WhatsappLog, Long> {
}
JAVA

cat > "$PKG_DIR/repository/InappLogRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.InappLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InappLogRepository extends JpaRepository<InappLog, Long> {
}
JAVA

cat > "$PKG_DIR/repository/AuditLogRepository.java" <<'JAVA'
package com.example.notification.repository;

import com.example.notification.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
JAVA

# ---------------------------
# DTOs
# ---------------------------
cat > "$PKG_DIR/dto/NotificationRequest.java" <<'JAVA'
package com.example.notification.dto;

import jakarta.persistence.*;

import java.util.Map;

/**
 * DTO for accepting notification requests across multiple channels.
 * Supports SMS, WhatsApp, Email/Notification, and In-App notifications.
 */
public class NotificationRequest {

    /** Target channel: SMS, WHATSAPP, EMAIL, NOTIFICATION, INAPP */
    private String channel;

    /** Username of the recipient (optional but recommended for personalization) */
    private String username;

    /** Mobile number (used for SMS/WhatsApp) */
    @Column(name = "mobile_enc", length = 2048)
    private String mobile;

    /** Email address (used for Email/Notification) */
    @Column(name = "email_enc", length = 2048)
    private String email;

    /** Subject or title (used for Email/InApp) */
    private String subject;

    /** Code of the template being used (e.g., OTP, ORDER_CONFIRM) */
    private String templateCode;

    /** Map of placeholder keys -> values for rendering templates */
    private Map<String, Object> placeholders;

    /** ID of the user who triggered the notification (for audit/logging) */
    private String userId;

    // ----------------- Getters & Setters -----------------

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Map<String, Object> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(Map<String, Object> placeholders) {
        this.placeholders = placeholders;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

JAVA



# ---------------------------
# Controller
# ---------------------------
cat > "$PKG_DIR/controller/NotificationController.java" <<'JAVA'
package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> send(@Valid @RequestBody NotificationRequest req) {
        service.enqueue(req);
        
        return ResponseEntity.accepted().body(req.getChannel() + " Notification accepted");
    }
}
JAVA

# ---------------------------
# Security: Access Token Filter + Config
# ---------------------------
cat > "$PKG_DIR/security/AccessTokenFilter.java" <<'JAVA'
package com.example.notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AccessTokenFilter extends OncePerRequestFilter {

    private final String accessToken;

    public AccessTokenFilter(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing Authorization header");
            return;
        }
        String token = auth.substring(7);
        if (!accessToken.equals(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid access token");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
JAVA

cat > "$PKG_DIR/config/SecurityConfig.java" <<'JAVA'
package com.example.notification.config;

import com.example.notification.security.JwtAuthFilter;
import com.example.notification.security.JwtVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Value("${jwt.public-key-path}")
    private String publicKeyPath;

    @Bean
    public JwtVerifier jwtVerifier() {
        return new JwtVerifier(publicKeyPath);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtVerifier());
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
JAVA

# ---------------------------
# Exceptions + Global Handler
# ---------------------------
cat > "$PKG_DIR/exception/BadCredentialsException.java" <<'JAVA'
package com.example.notification.exception;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException() { super("Invalid username, password, or OTP."); }
    public BadCredentialsException(String msg) { super(msg); }
}
JAVA

cat > "$PKG_DIR/exception/LockedException.java" <<'JAVA'
package com.example.notification.exception;

public class LockedException extends RuntimeException {
    public LockedException() { super("Account is locked. Contact support."); }
}
JAVA

cat > "$PKG_DIR/exception/CredentialsExpiredException.java" <<'JAVA'
package com.example.notification.exception;

public class CredentialsExpiredException extends RuntimeException {
    public CredentialsExpiredException() { super("Credentials expired. Please regenerate OTP or refresh token."); }
}
JAVA

cat > "$PKG_DIR/exception/DisabledException.java" <<'JAVA'
package com.example.notification.exception;

public class DisabledException extends RuntimeException {
    public DisabledException() { super("Account is disabled."); }
}
JAVA

cat > "$PKG_DIR/exception/GlobalExceptionHandler.java" <<'JAVA'
package com.example.notification.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> bad(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<String> locked(LockedException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<String> expired(CredentialsExpiredException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> disabled(DisabledException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> other(Exception ex) {
        return ResponseEntity.status(500).body("Internal error: " + ex.getMessage());
    }
}
JAVA

# ---------------------------
# DataInitializer
# ---------------------------
cat > "$PKG_DIR/config/DataInitializer.java" <<'JAVA'
package com.example.notification.config;

import com.example.notification.entity.templates.NotificationTemplateMaster;
import com.example.notification.entity.templates.SmsTemplateMaster;
import com.example.notification.entity.templates.WhatsappTemplateMaster;
import com.example.notification.entity.templates.InappTemplateMaster;
import com.example.notification.repository.NotificationTemplateRepository;
import com.example.notification.repository.SmsTemplateRepository;
import com.example.notification.repository.WhatsappTemplateRepository;
import com.example.notification.repository.InappTemplateRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private final NotificationTemplateRepository notificationRepo;
    private final SmsTemplateRepository smsRepo;
    private final WhatsappTemplateRepository waRepo;
    private final InappTemplateRepository inappRepo;

    public DataInitializer(NotificationTemplateRepository notificationRepo,
                           SmsTemplateRepository smsRepo,
                           WhatsappTemplateRepository waRepo,
                           InappTemplateRepository inappRepo) {
        this.notificationRepo = notificationRepo;
        this.smsRepo = smsRepo;
        this.waRepo = waRepo;
        this.inappRepo = inappRepo;
    }

    @PostConstruct
    public void seed() {
        if (notificationRepo.count() == 0) {
            NotificationTemplateMaster t = new NotificationTemplateMaster();
            t.setTemplateCode("WELCOME_EMAIL");
            t.setName("Welcome Email");
            t.setSubject("Welcome to Our Store");
            t.setBody("Hello {{name}}, thank you for registering with us!");
            t.setPlaceholders("{\"name\":\"Customer Name\"}");
            t.setActive(true);
            t.setProjectType("ECOM");
            notificationRepo.save(t);
        }

        if (smsRepo.count() == 0) {
            SmsTemplateMaster s = new SmsTemplateMaster();
            s.setTemplateCode("OTP_SMS");
            s.setName("OTP SMS");
            s.setBody("Your OTP is {{otp}}. Do not share it.");
            s.setPlaceholders("{\"otp\":\"One-Time Password\"}");
            s.setActive(true);
            s.setProjectType("ECOM");
            smsRepo.save(s);
        }

        if (waRepo.count() == 0) {
            WhatsappTemplateMaster w = new WhatsappTemplateMaster();
            w.setTemplateCode("WELCOME_WA");
            w.setName("Welcome WhatsApp");
            w.setSubject("Welcome");
            w.setBody("Hi {{name}}, welcome to Our Store!");
            w.setPlaceholders("{\"name\":\"Customer Name\"}");
            w.setActive(true);
            w.setProjectType("ECOM");
            waRepo.save(w);
        }

        if (inappRepo.count() == 0) {
            InappTemplateMaster i = new InappTemplateMaster();
            i.setTemplateCode("WELCOME_INAPP");
            i.setName("Welcome InApp");
            i.setTitle("Welcome");
            i.setBody("Hi {{name}}, thanks for registering!");
            i.setPlaceholders("{\"name\":\"Customer Name\"}");
            i.setActive(true);
            i.setProjectType("ECOM");
            inappRepo.save(i);
        }
    }
}
JAVA



echo "Enhancing Notification Service for Template-based messaging..."

# ---------------------------
# Template Engine Util
# ---------------------------
cat > "$PKG_DIR/util/TemplateEngineUtil.java" <<'JAVA'
package com.example.notification.util;

import java.util.Map;

public class TemplateEngineUtil {
    public static String render(String template, Map<String, Object> placeholders) {
        if (template == null) return null;
        String rendered = template;
        if (placeholders != null) {
            for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
                String key = "{{" + entry.getKey() + "}}";
                rendered = rendered.replace(key, String.valueOf(entry.getValue()));
            }
        }
        return rendered;
    }
}
JAVA

# ---------------------------
# Template Resolver Service
# ---------------------------
cat > "$PKG_DIR/service/TemplateResolverService.java" <<'JAVA'
package com.example.notification.service;

import com.example.notification.entity.templates.NotificationTemplateMaster;
import com.example.notification.entity.templates.SmsTemplateMaster;
import com.example.notification.entity.templates.WhatsappTemplateMaster;
import com.example.notification.entity.templates.InappTemplateMaster;
import com.example.notification.repository.NotificationTemplateRepository;
import com.example.notification.repository.SmsTemplateRepository;
import com.example.notification.repository.WhatsappTemplateRepository;
import com.example.notification.repository.InappTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TemplateResolverService {

    private final NotificationTemplateRepository notificationRepo;
    private final SmsTemplateRepository smsRepo;
    private final WhatsappTemplateRepository waRepo;
    private final InappTemplateRepository inappRepo;

    public TemplateResolverService(NotificationTemplateRepository notificationRepo,
                                   SmsTemplateRepository smsRepo,
                                   WhatsappTemplateRepository waRepo,
                                   InappTemplateRepository inappRepo) {
        this.notificationRepo = notificationRepo;
        this.smsRepo = smsRepo;
        this.waRepo = waRepo;
        this.inappRepo = inappRepo;
    }

    public String resolveBody(String channel, String templateCode) {
        switch (channel.toUpperCase()) {
            case "SMS":
                Optional<SmsTemplateMaster> sms = smsRepo.findByTemplateCode(templateCode);
                return sms.map(SmsTemplateMaster::getBody).orElse(null);
            case "WHATSAPP":
                Optional<WhatsappTemplateMaster> wa = waRepo.findByTemplateCode(templateCode);
                return wa.map(WhatsappTemplateMaster::getBody).orElse(null);
            case "INAPP":
                Optional<InappTemplateMaster> inapp = inappRepo.findByTemplateCode(templateCode);
                return inapp.map(InappTemplateMaster::getBody).orElse(null);
            case "EMAIL":
            case "NOTIFICATION":
                Optional<NotificationTemplateMaster> n = notificationRepo.findByTemplateCode(templateCode);
                return n.map(NotificationTemplateMaster::getBody).orElse(null);
            default:
                throw new IllegalArgumentException("Unsupported channel: " + channel);
        }
    }

    public String resolveSubject(String channel, String templateCode) {
        switch (channel.toUpperCase()) {
            case "WHATSAPP":
                return waRepo.findByTemplateCode(templateCode).map(WhatsappTemplateMaster::getSubject).orElse(null);
            case "EMAIL":
            case "NOTIFICATION":
                return notificationRepo.findByTemplateCode(templateCode).map(NotificationTemplateMaster::getSubject).orElse(null);
            default:
                return null; // SMS/INAPP usually don’t have subjects
        }
    }
}
JAVA

# ---------------------------
# Modify NotificationService
# ---------------------------
cat > "$PKG_DIR/service/NotificationService.java" <<'JAVA'
package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.entity.*;
import com.example.notification.repository.*;
import com.example.notification.util.HashUtil;
import com.example.notification.util.TemplateEngineUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final SmsLogRepository smsRepo;
    private final NotificationLogRepository notificationRepo;
    private final WhatsappLogRepository whatsappRepo;
    private final InappLogRepository inappRepo;
    private final TemplateResolverService templateResolver;

    public NotificationService(
            SmsLogRepository smsRepo,
            NotificationLogRepository notificationRepo,
            WhatsappLogRepository whatsappRepo,
            InappLogRepository inappRepo,
            TemplateResolverService templateResolver) {
        this.smsRepo = smsRepo;
        this.notificationRepo = notificationRepo;
        this.whatsappRepo = whatsappRepo;
        this.inappRepo = inappRepo;
        this.templateResolver = templateResolver;
    }

    @Transactional
    public void enqueue(NotificationRequest req) {
        // Step 1: Resolve template subject + body
        String rawBody = templateResolver.resolveBody(req.getChannel(), req.getTemplateCode());
        String subject = templateResolver.resolveSubject(req.getChannel(), req.getTemplateCode());

        // Step 2: Render body with placeholders
        String renderedBody = TemplateEngineUtil.render(rawBody, req.getPlaceholders());

        // Step 3: Persist based on channel
        switch (req.getChannel().toUpperCase()) {
            case "SMS" -> saveSms(req, renderedBody);
            case "WHATSAPP" -> saveWhatsapp(req, renderedBody);
            case "EMAIL", "NOTIFICATION" -> saveNotification(req, renderedBody, subject);
            case "INAPP" -> saveInapp(req, renderedBody);
            default -> throw new IllegalArgumentException("Unsupported channel: " + req.getChannel());
        }
    }

    private void saveSms(NotificationRequest req, String body) {
        SmsLog sms = new SmsLog();
        sms.setUsername(req.getUsername());
        sms.setMobile(req.getMobile());
        sms.setMessage(body);
        sms.setTemplateCode(req.getTemplateCode());
        sms.setUserId(req.getUserId());
        sms.setCreatedAt(LocalDateTime.now());

        // if (sms.getMobile() != null) {
        //     sms.setMobileFingerprint(HashUtil.fingerprint(sms.getMobile()));
        // }

        smsRepo.save(sms);
    }

    private void saveWhatsapp(NotificationRequest req, String body) {
        WhatsappLog wa = new WhatsappLog();
        wa.setUsername(req.getUsername());
        wa.setMobile(req.getMobile());
        wa.setMessage(body);
        wa.setTemplateCode(req.getTemplateCode());
        wa.setUserId(req.getUserId());
        wa.setCreatedAt(LocalDateTime.now());

        // if (wa.getMobile() != null) {
        //     wa.setMobileFingerprint(HashUtil.fingerprint(wa.getMobile()));
        // }

        whatsappRepo.save(wa);
    }

    private void saveNotification(NotificationRequest req, String body, String subject) {
        NotificationLog n = new NotificationLog();
        n.setUsername(req.getUsername());
        n.setEmail(req.getEmail());
        n.setSubject(subject != null ? subject : req.getSubject());
        n.setMessage(body);
        n.setChannel(req.getChannel());
        n.setTemplateCode(req.getTemplateCode());
        n.setUserId(req.getUserId());
        n.setCreatedAt(LocalDateTime.now());

        // if (n.getEmail() != null) {
        //     n.setEmailFingerprint(HashUtil.fingerprint(n.getEmail()));
        // }

        notificationRepo.save(n);
    }

    private void saveInapp(NotificationRequest req, String body) {
        InappLog in = new InappLog();
        in.setUsername(req.getUsername());
        in.setTitle(req.getSubject()); // in-app often uses a title
        in.setMessage(body);
        in.setTemplateCode(req.getTemplateCode());
        in.setUserId(req.getUserId());
        in.setCreatedAt(LocalDateTime.now());

        inappRepo.save(in);
    }
}


JAVA



cat > "$PKG_DIR/security/JwtVerifier.java" <<'JAVA'
package com.example.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class JwtVerifier {

    private final PublicKey publicKey;

    public JwtVerifier(String publicKeyPath) {
        this.publicKey = loadPublicKey(publicKeyPath);
    }

    private PublicKey loadPublicKey(String path) {
        try {
            byte[] keyBytes;

            if (path.startsWith("classpath:")) {
                String resourcePath = path.replace("classpath:", "");
                try (InputStream in = new ClassPathResource(resourcePath).getInputStream()) {
                    keyBytes = in.readAllBytes();
                }
            } else {
                keyBytes = Files.readAllBytes(Paths.get(path));
            }

            String key = new String(keyBytes)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to load public key from: " + path, e);
        }
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey((RSAPublicKey) publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("❌ Invalid or expired JWT", e);
        }
    }
}


JAVA


cat > "$PKG_DIR/security/JwtAuthFilter.java" <<'JAVA'
package com.example.notification.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtVerifier jwtVerifier;

    public JwtAuthFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtVerifier.validateToken(token);
                String userId = claims.getSubject();

                // Create Spring Security Authentication
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store authentication in context
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token: " + e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}


JAVA


cat > "$PKG_DIR/service/AuthTokenService.java" <<'JAVA'

package com.example.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthTokenService {

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${auth.client-id}")
    private String clientId;

    @Value("${auth.client-secret}")
    private String clientSecret;

    /**
     * Try to get the user’s JWT from SecurityContextHolder.
     * If not present (background job), fetch service token from auth-service.
     */
    public String getAccessToken() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        return getServiceToken();
    }

    private String getServiceToken() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", "client_credentials");

        ResponseEntity<Map> response = restTemplate.postForEntity(
            authServiceUrl + "/auth/token", params, Map.class);

        return (String) response.getBody().get("access_token");
    }
}
JAVA

cat > "$PKG_DIR/config/FeignConfig.java" <<'JAVA'
package com.example.notification.config;

import com.example.notification.service.AuthTokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private final AuthTokenService tokenService;

    public FeignConfig(AuthTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String token = tokenService.getAccessToken();
                if (token != null) {
                    template.header("Authorization", "Bearer " + token);
                }
            }
        };
    }
}
JAVA

echo "Template-based Notification Service enhancements generated successfully."
# ---------------------------
# Flyway migrations V1 + V2 (seed)
# ---------------------------
cat > "$RES_DIR/db/migration/V1__init.sql" <<'SQL'
-- Templates
CREATE TABLE IF NOT EXISTS notification_template_master (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_code VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(255),
  subject VARCHAR(255),
  body TEXT NOT NULL,
  placeholders JSON,
  active BOOLEAN DEFAULT TRUE,
  project_type ENUM('ECOM','ASSET_MGMT') NOT NULL DEFAULT 'ECOM',
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS sms_template_master (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_code VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(255),
  body TEXT NOT NULL,
  placeholders JSON,
  active BOOLEAN DEFAULT TRUE,
  project_type ENUM('ECOM','ASSET_MGMT') NOT NULL DEFAULT 'ECOM',
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS whatsapp_template_master (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_code VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(255),
  subject VARCHAR(255),
  body TEXT NOT NULL,
  placeholders JSON,
  active BOOLEAN DEFAULT TRUE,
  project_type ENUM('ECOM','ASSET_MGMT') NOT NULL DEFAULT 'ECOM',
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS inapp_template_master (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_code VARCHAR(100) UNIQUE NOT NULL,
  name VARCHAR(255),
  title VARCHAR(255),
  body TEXT NOT NULL,
  placeholders JSON,
  active BOOLEAN DEFAULT TRUE,
  project_type VARCHAR(50) NOT NULL DEFAULT 'ECOM',
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

-- Logs
CREATE TABLE IF NOT EXISTS sms_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255),
  mobile VARCHAR(255),
  mobileFingerprint VARCHAR(255),
  message TEXT,
  template_code VARCHAR(100),
  provider_message_id VARCHAR(255),
  provider_response TEXT,
  retries INT DEFAULT 0,
  user_id VARCHAR(64),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS notification_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255),
  email VARCHAR(255),
  emailFingerprint VARCHAR(255),
  subject VARCHAR(255),
  message TEXT,
  channel VARCHAR(50),
  template_code VARCHAR(100),
  provider_response TEXT,
  user_id VARCHAR(64),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS whatsapp_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255),
  mobile VARCHAR(255),
  mobileFingerprint VARCHAR(255),
  message TEXT,
  template_code VARCHAR(100),
  provider_response TEXT,
  user_id VARCHAR(64),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS inapp_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255),
  title VARCHAR(255),
  message TEXT,
  template_code VARCHAR(100),
  user_id VARCHAR(64),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255),
  entity_name VARCHAR(100) NOT NULL,
  entity_id BIGINT NOT NULL,
  action ENUM('CREATE','UPDATE','DELETE','READ') NOT NULL,
  old_value JSON,
  new_value JSON,
  ip_address VARCHAR(100),
  user_agent VARCHAR(512),
  url VARCHAR(1024),
  http_method VARCHAR(10),
  user_id VARCHAR(64),
  created_by VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_by VARCHAR(255),
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE
);
SQL

cat > "$RES_DIR/db/migration/V2__seed_templates.sql" <<'SQL'

-- ========================================
-- Seed Asset Management Templates
-- ========================================
--  truncate table sms_template_master; truncate table whatsapp_template_master; truncate table notification_template_master; truncate table inapp_template_master; 

-- WhatsApp Templates
INSERT INTO whatsapp_template_master 
(template_code, name, subject, body, placeholders, active, project_type)
VALUES
 -- Asset Management
 ('ASSET_ASSIGN_WA', 'Asset Assignment', 'Asset Assigned',
  '📌 Asset {{assetId}} has been assigned to you, {{name}}.',
  '{"assetId":"Asset Identifier","name":"Employee Name"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_RETURN_WA', 'Asset Return', 'Asset Returned',
  '↩️ Asset {{assetId}} returned successfully by {{name}}.',
  '{"assetId":"Asset Identifier","name":"Employee Name"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_MAINT_WA', 'Maintenance Alert', 'Asset Maintenance Scheduled',
  '⚙️ Asset {{assetId}} scheduled for maintenance on {{date}}.',
  '{"assetId":"Asset Identifier","date":"Maintenance Date"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_ERROR_WA', 'Asset Error', 'Asset Error Notification',
  '⚠️ Asset {{assetId}} error {{errorCode}} at {{timestamp}}.',
  '{"assetId":"Asset Identifier","errorCode":"Error Code","timestamp":"Error Time"}', TRUE, 'ASSET_MGMT'),

 -- E-commerce
 ('WELCOME_WA', 'Welcome WhatsApp', 'Welcome',
  '👋 Hi {{name}}, welcome to Our Store!',
  '{"name":"Customer Name"}', TRUE, 'ECOM'),

 ('ORDER_CONFIRM_WA', 'Order Confirmation', 'Order Confirmed',
  '✅ Order {{orderId}} confirmed for {{name}}.',
  '{"orderId":"Order ID","name":"Customer Name"}', TRUE, 'ECOM'),

 ('SHIPMENT_WA', 'Shipment Update', 'Order Shipped',
  '📦 Order {{orderId}} has been shipped. Track here: {{trackingLink}}',
  '{"orderId":"Order ID","trackingLink":"Tracking URL"}', TRUE, 'ECOM'),

 ('DELIVERY_WA', 'Delivery Notification', 'Order Delivered',
  '🎉 Order {{orderId}} delivered successfully.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('ALERT_WA', 'System Alert', 'System Alert',
  '⚠️ Alert: {{alertMessage}}',
  '{"alertMessage":"Alert Details"}', TRUE, 'ECOM'),


 ('OTP_WA', 'OTP Verification', 'OTP Verification',
  'Your OTP is {{otp}}. Do not share it with anyone.',
  '{"otp":"One-Time Password"}', TRUE, 'ECOM');



-- SMS Templates
INSERT INTO sms_template_master 
(template_code, name, body, placeholders, active, project_type)
VALUES
 -- Asset Management
 ('ASSET_ASSIGN_SMS', 'Asset Assignment', 'Asset {{assetId}} has been assigned to you.',
  '{"assetId":"Asset Identifier"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_RETURN_SMS', 'Asset Return', 'Return logged for asset {{assetId}}.',
  '{"assetId":"Asset Identifier"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_MAINT_SMS', 'Maintenance Alert', 'Maintenance scheduled for asset {{assetId}} on {{date}}.',
  '{"assetId":"Asset Identifier","date":"Maintenance Date"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_ERROR_SMS', 'Asset Error', 'Asset {{assetId}} error: {{errorCode}}.',
  '{"assetId":"Asset Identifier","errorCode":"Error Code"}', TRUE, 'ASSET_MGMT'),

 -- E-commerce
 ('OTP_SMS', 'OTP Verification', 'Your OTP is {{otp}}. Do not share it with anyone.',
  '{"otp":"One-Time Password"}', TRUE, 'ECOM'),

 ('ORDER_CONFIRM_SMS', 'Order Confirmation', 'Your order {{orderId}} has been confirmed.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('SHIPMENT_SMS', 'Shipment Update', 'Your order {{orderId}} has been shipped.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('DELIVERY_SMS', 'Delivery Notification', 'Your order {{orderId}} has been delivered.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('ERROR_SMS', 'Error Alert', 'System error occurred: {{errorCode}}',
  '{"errorCode":"Error Code"}', TRUE, 'ECOM');

-- Notification / Email Templates
INSERT INTO notification_template_master 
(template_code, name, subject, body, placeholders, active, project_type)
VALUES
 -- Asset Management
 ('ASSET_ASSIGN_EMAIL', 'Asset Assignment', 'Asset Assigned: {{assetId}}',
  'Hello {{name}}, asset {{assetId}} has been assigned to you.',
  '{"name":"Employee Name","assetId":"Asset Identifier"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_RETURN_EMAIL', 'Asset Return', 'Asset {{assetId}} Returned',
  'Hi {{name}}, your return for asset {{assetId}} has been logged.',
  '{"name":"Employee Name","assetId":"Asset Identifier"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_MAINT_EMAIL', 'Maintenance Alert', 'Maintenance Scheduled for Asset {{assetId}}',
  'Asset {{assetId}} is scheduled for maintenance on {{date}}.',
  '{"assetId":"Asset Identifier","date":"Maintenance Date"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_ERROR_EMAIL', 'Asset System Error', 'Asset Error: {{errorCode}}',
  'Asset {{assetId}} encountered error {{errorCode}} at {{timestamp}}.',
  '{"assetId":"Asset Identifier","errorCode":"Error Code","timestamp":"Error Time"}', TRUE, 'ASSET_MGMT'),

 -- E-commerce
 ('WELCOME_EMAIL', 'Welcome Email', 'Welcome to Our Store',
  'Hello {{name}}, thank you for registering with us! Enjoy shopping 🎉',
  '{"name":"Customer Name"}', TRUE, 'ECOM'),

 ('ORDER_CONFIRM_EMAIL', 'Order Confirmation', 'Order #{{orderId}} Confirmed',
  'Hi {{name}}, your order {{orderId}} has been successfully confirmed.',
  '{"name":"Customer Name","orderId":"Order ID"}', TRUE, 'ECOM'),

 ('SHIPMENT_EMAIL', 'Shipment Notification', 'Your Order #{{orderId}} is Shipped',
  'Hi {{name}}, your order {{orderId}} has been shipped. Track it here: {{trackingLink}}',
  '{"name":"Customer Name","orderId":"Order ID","trackingLink":"Tracking URL"}', TRUE, 'ECOM'),

 ('DELIVERY_EMAIL', 'Delivery Notification', 'Your Order #{{orderId}} Delivered',
  'Hi {{name}}, your order {{orderId}} has been delivered. We hope you enjoy your purchase 😊',
  '{"name":"Customer Name","orderId":"Order ID"}', TRUE, 'ECOM'),

 ('PASSWORD_RESET_EMAIL', 'Password Reset', 'Reset Your Password',
  'Hello {{name}}, we received a request to reset your password. Click here: {{resetLink}}',
  '{"name":"Customer Name","resetLink":"Password Reset Link"}', TRUE, 'ECOM'),

 ('ERROR_EMAIL', 'System Error Notification', 'Error Code: {{errorCode}}',
  'Dear Admin, error {{errorCode}} occurred at {{timestamp}}. Details: {{details}}',
  '{"errorCode":"Error Code","timestamp":"Error Time","details":"Error Details"}', TRUE, 'ECOM'),


 ('OTP_EMAIL', 'OTP Verification', 'OTP Verification',
  'Your OTP is {{otp}}. Do not share it with anyone.',
  '{"otp":"One-Time Password"}', TRUE, 'ECOM');

-- In-App Notification Templates
INSERT INTO inapp_template_master 
(template_code, name, title, body, placeholders, active, project_type)
VALUES
 -- Asset Management
 ('ASSET_ASSIGN_INAPP', 'Asset Assignment', 'Asset Assigned',
  '📌 Asset {{assetId}} has been assigned to you, {{name}}.',
  '{"assetId":"Asset Identifier","name":"Employee Name"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_RETURN_INAPP', 'Asset Return', 'Asset Returned',
  '↩️ Asset {{assetId}} returned successfully by {{name}}.',
  '{"assetId":"Asset Identifier","name":"Employee Name"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_MAINT_INAPP', 'Maintenance Alert', 'Maintenance Scheduled',
  '⚙️ Asset {{assetId}} is scheduled for maintenance on {{date}}.',
  '{"assetId":"Asset Identifier","date":"Maintenance Date"}', TRUE, 'ASSET_MGMT'),

 ('ASSET_ERROR_INAPP', 'Asset Error', 'Asset Error Notification',
  '⚠️ Asset {{assetId}} error {{errorCode}} at {{timestamp}}.',
  '{"assetId":"Asset Identifier","errorCode":"Error Code","timestamp":"Error Time"}', TRUE, 'ASSET_MGMT'),

 -- E-commerce
 ('WELCOME_INAPP', 'Welcome Notification', 'Welcome to Our Store',
  '👋 Hi {{name}}, thanks for registering! Enjoy shopping 🎉',
  '{"name":"Customer Name"}', TRUE, 'ECOM'),

 ('ORDER_CONFIRM_INAPP', 'Order Confirmation', 'Order Confirmed',
  '✅ Your order {{orderId}} has been confirmed.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('SHIPMENT_INAPP', 'Shipment Notification', 'Order Shipped',
  '📦 Your order {{orderId}} has been shipped. Track here: {{trackingLink}}',
  '{"orderId":"Order ID","trackingLink":"Tracking URL"}', TRUE, 'ECOM'),

 ('DELIVERY_INAPP', 'Delivery Notification', 'Order Delivered',
  '🎉 Your order {{orderId}} has been delivered successfully.',
  '{"orderId":"Order ID"}', TRUE, 'ECOM'),

 ('PASSWORD_RESET_INAPP', 'Password Reset', 'Password Reset Requested',
  'Hello {{name}}, a password reset was requested. Reset it here: {{resetLink}}',
  '{"name":"Customer Name","resetLink":"Password Reset Link"}', TRUE, 'ECOM'),

 ('ERROR_INAPP', 'System Error Notification', 'System Error',
  '⚠️ Error {{errorCode}} occurred at {{timestamp}}. Details: {{details}}',
  '{"errorCode":"Error Code","timestamp":"Error Time","details":"Error Details"}', TRUE, 'ECOM'),

 ('OTP_INAPP', 'OTP Verification', 'OTP Verification',
  'Your OTP is {{otp}}. Do not share it with anyone.',
  '{"otp":"One-Time Password"}', TRUE, 'ECOM');

-- Select * from  sms_template_master;  Select * from  whatsapp_template_master;  Select * from notification_template_master;
SQL



# ---------------------------
# Create .env.notify with keys (if not exists)
# ---------------------------
ENV_FILE="$ROOT_DIR/.env.notify"
# ---------------------------
# Create .env.notify and symlink/copy as .env
# ---------------------------
ENV_FILE="$ROOT_DIR/.env.notify"
DOTENV_FILE="$ROOT_DIR/.env"

if [ -f "$ENV_FILE" ]; then
  echo ".env.notify already exists. Skipping key generation."
else
  echo "Generating .env.notify with encryption keys and db credentials..."
  ENC_KEY=$(openssl rand -base64 32)
  HMAC_KEY=$(openssl rand -base64 32)
  cat > "$ENV_FILE" <<EOF
NOTIFY_ENC_KEY=$ENC_KEY
NOTIFY_HMAC_KEY=$HMAC_KEY
DB_USERNAME=root
DB_PASSWORD=Snmysql@1110
ACCESS_TOKEN=change_this_token
EOF
  echo "Created $ENV_FILE (DO NOT commit to git)."
fi

# Ensure .env points to .env.notify
cp "$ENV_FILE" "$DOTENV_FILE"
echo "Linked $ENV_FILE -> $DOTENV_FILE (Spring Boot will auto-load)."

# Add to .gitignore
if ! grep -q "^.env" "$ROOT_DIR/.gitignore" 2>/dev/null; then
  echo ".env" >> "$ROOT_DIR/.gitignore"
  echo ".env.notify" >> "$ROOT_DIR/.gitignore"
fi


# debug-run.sh
# Runs Notification Service in debug mode with JDWP enabled.
# Attaches a debugger at localhost:56517 (change if needed).

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Compile first (if not already built)
echo "Building project..."
(cd "$ROOT_DIR" && mvn -DskipTests compile)

# Run in debug mode
echo "Starting Notification Service in debug mode..."
java \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:56517 \
  -cp "$ROOT_DIR/target/classes:$ROOT_DIR/target/dependency/*" \
  com.example.notification.NotificationServiceApplication
  




# ---------------------------
# Final notes & optional build
# ---------------------------
echo "Project skeleton generated at: $ROOT_DIR"
echo ""
echo "Next steps:"
echo "1) Create database: notificationdb (charset utf8mb4 recommended)."
echo "   Example: CREATE DATABASE notificationdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "2) Export environment variables (or configure in your run environment):"
echo "   export NOTIFY_ENC_KEY=\$(grep NOTIFY_ENC_KEY $ROOT_DIR/.env.notify | cut -d'=' -f2-)"
echo "   export NOTIFY_HMAC_KEY=\$(grep NOTIFY_HMAC_KEY $ROOT_DIR/.env.notify | cut -d'=' -f2-)"
echo "   export DB_USERNAME=\$(grep DB_USERNAME $ROOT_DIR/.env.notify | cut -d'=' -f2-)"
echo "   export DB_PASSWORD=\$(grep DB_PASSWORD $ROOT_DIR/.env.notify | cut -d'=' -f2-)"
echo "   export ACCESS_TOKEN=\$(grep ACCESS_TOKEN $ROOT_DIR/.env.notify | cut -d'=' -f2-)"
echo ""
if command -v mvn >/dev/null 2>&1; then
  echo "Attempting to build the project with Maven (skip if you prefer to build later)..."
  (cd "$ROOT_DIR" && mvn -DskipTests package) || echo "Maven build failed (check logs)."
  echo ""
  echo "If build succeeded, run with:"
  echo "  java -jar $ROOT_DIR/target/notification-service-1.0.0.jar"
else
  echo "Maven not found. Install Maven or run mvn package later in $ROOT_DIR"
fi

echo ""
echo "API endpoints are protected by ACCESS_TOKEN. Add header: Authorization: Bearer <ACCESS_TOKEN>"
echo ""
echo "Script completed."