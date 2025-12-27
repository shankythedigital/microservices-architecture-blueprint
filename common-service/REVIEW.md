# Common Service - Project Review

## 📋 Project Overview

**Service Name:** Common Service (Shared Library)  
**Version:** 0.0.5-SNAPSHOT  
**Type:** JAR Library  
**Status:** ✅ Production Ready

## 🏗️ Architecture

### Technology Stack
- **Framework:** Spring Boot 3.3.2
- **Java Version:** 17
- **Packaging:** JAR (library)

### Purpose
Shared library containing common utilities, base classes, and reusable components used across all microservices.

## 📁 Project Structure

```
common-service/
├── src/main/java/com/example/common/
│   ├── jpa/                 # JPA base classes
│   │   ├── BaseEntity.java  # Base entity with audit fields
│   │   └── AuditRecord.java # Audit record entity
│   ├── security/            # Security utilities
│   │   ├── JwtVerifier.java # JWT token verification
│   │   └── JwtAuthFilter.java # JWT authentication filter
│   ├── service/             # Service utilities
│   │   ├── SafeNotificationHelper.java # Safe notification sending
│   │   └── NotificationHelper.java # Notification helper
│   ├── util/                # Utility classes
│   │   ├── ResponseWrapper.java # API response wrapper
│   │   ├── FileStorageUtil.java # File storage utilities
│   │   ├── HashUtil.java    # Hashing utilities
│   │   ├── HmacUtil.java    # HMAC utilities
│   │   ├── JwtUtil.java     # JWT utilities
│   │   ├── AesGcmEncryptor.java # Encryption utilities
│   │   └── RequestContext.java # Request context
│   ├── client/              # Feign clients
│   │   ├── AdminClient.java # Admin service client
│   │   ├── AssetUserLinkClient.java # Asset user link client
│   │   └── NotificationClient.java # Notification client
│   ├── config/              # Configuration classes
│   │   ├── AsyncConfig.java # Async configuration
│   │   ├── FeignAuthConfig.java # Feign auth configuration
│   │   ├── FeignTokenInterceptor.java # Token interceptor
│   │   └── SchedulerConfig.java # Scheduler configuration
│   ├── exception/           # Exception handlers
│   │   └── GlobalExceptionHandler.java # Global exception handler
│   ├── filter/              # Filters
│   │   └── CorrelationIdFilter.java # Correlation ID filter
│   ├── converter/           # Converters
│   │   └── JpaAttributeEncryptor.java # JPA encryption
│   ├── entity/               # Common entities
│   │   └── NotificationRetryLog.java # Notification retry log
│   └── repository/           # Common repositories
│       └── NotificationRetryLogRepository.java
└── pom.xml                  # Maven configuration
```

## 🔑 Key Components

### 1. BaseEntity
**Location:** `com.example.common.jpa.BaseEntity`

Base class for all JPA entities providing:
- `createdBy` - Creator identifier
- `createdAt` - Creation timestamp
- `updatedBy` - Updater identifier
- `updatedAt` - Update timestamp
- `active` - Soft delete flag

**Usage:**
```java
@Entity
public class MyEntity extends BaseEntity {
    // Entity fields
}
```

### 2. JwtVerifier
**Location:** `com.example.common.security.JwtVerifier`

JWT token verification utility supporting:
- RSA (RS256) token verification
- HMAC (HS256) token verification
- Automatic key loading from files or environment
- Cloud and local environment detection

**Usage:**
```java
@Autowired
private JwtVerifier jwtVerifier;

Claims claims = jwtVerifier.validate(token);
```

### 3. JwtAuthFilter
**Location:** `com.example.common.security.JwtAuthFilter`

Spring Security filter for JWT authentication:
- Validates Bearer tokens
- Extracts user information
- Sets security context

**Usage:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.addFilterBefore(
        new JwtAuthFilter(jwtVerifier),
        UsernamePasswordAuthenticationFilter.class
    );
}
```

### 4. ResponseWrapper
**Location:** `com.example.common.util.ResponseWrapper`

Standardized API response wrapper:
```java
ResponseWrapper<T> {
    boolean success;
    String message;
    T data;
}
```

**Usage:**
```java
return ResponseEntity.ok(
    new ResponseWrapper<>(true, "Success", data)
);
```

### 5. SafeNotificationHelper
**Location:** `com.example.common.service.SafeNotificationHelper`

Safe notification sending with:
- Automatic retry on failure
- Failure logging
- Persistence for manual retry
- Async processing

**Usage:**
```java
@Autowired
private SafeNotificationHelper notificationHelper;

notificationHelper.safeNotify(
    token, userId, username, email, mobile,
    channel, templateCode, variables
);
```

### 6. FileStorageUtil
**Location:** `com.example.common.util.FileStorageUtil`

File storage utilities:
- Save files with organized structure
- Retrieve files
- Delete files
- File validation

**Usage:**
```java
@Autowired
private FileStorageUtil fileStorageUtil;

String path = fileStorageUtil.storeFile(file, "ASSET");
```

### 7. Feign Clients
**Location:** `com.example.common.client.*`

Feign clients for inter-service communication:
- `AdminClient` - Admin service operations
- `AssetUserLinkClient` - Asset user link operations
- `NotificationClient` - Notification operations

## 🔐 Security Features

### JWT Support
- RSA public key verification
- HMAC secret verification
- Token parsing and validation
- Claims extraction

### Encryption
- AES-GCM encryption
- HMAC for message authentication
- Key management utilities

## 📊 Features

### Utilities
- ✅ Response wrapper for consistent API responses
- ✅ File storage management
- ✅ JWT token verification
- ✅ Encryption utilities
- ✅ Hashing utilities
- ✅ Request context management

### Services
- ✅ Safe notification sending
- ✅ Notification retry mechanism
- ✅ Inter-service communication (Feign)

### Base Classes
- ✅ BaseEntity for audit fields
- ✅ Global exception handler
- ✅ Common filters

## 🧪 Testing

### Unit Tests
- Utility class tests
- Service tests
- Security tests

## 📝 Configuration

### Maven Dependency
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>common-service</artifactId>
    <version>0.0.5-SNAPSHOT</version>
</dependency>
```

### Properties
Common properties used across services:
- `JWT_PUBLIC_KEY_PATH` - JWT public key path
- `JWT_SECRET` - JWT HMAC secret
- `common.notification.enabled` - Enable/disable notifications

## 🔧 Build & Deploy

### Build
```bash
mvn clean install
```

### Install to Local Repository
```bash
mvn clean install -DskipTests
```

## 📈 Usage Across Services

### Auth Service
- Uses `JwtVerifier` for token validation
- Uses `BaseEntity` for audit fields
- Uses `ResponseWrapper` for API responses

### Asset Service
- Uses `BaseEntity` for all entities
- Uses `ResponseWrapper` for API responses
- Uses `SafeNotificationHelper` for notifications
- Uses `FileStorageUtil` for file management
- Uses `JwtAuthFilter` for security

### Notification Service
- Uses `JwtVerifier` for token validation
- Uses `BaseEntity` for audit fields
- Uses `ResponseWrapper` for API responses

## 🚀 Future Enhancements

1. Additional utility classes
2. More Feign clients
3. Enhanced encryption utilities
4. Caching utilities
5. Validation utilities
6. Logging utilities
7. Metrics utilities

## 📚 Documentation

- **BaseEntity:** See JPA documentation
- **JWT:** See security documentation
- **Notifications:** See notification documentation

## ✅ Code Quality

- ✅ All classes properly structured
- ✅ Comprehensive utilities
- ✅ Well-documented
- ✅ Reusable across services

## 🔗 Used By

- **Auth Service** - JWT, BaseEntity, ResponseWrapper
- **Asset Service** - All common utilities
- **Notification Service** - JWT, BaseEntity, ResponseWrapper

---

**Last Updated:** 2025-12-11  
**Maintained By:** Development Team

