# Auth Service - Project Review

## 📋 Project Overview

**Service Name:** Authentication & Authorization Service  
**Version:** 0.0.5-SNAPSHOT  
**Port:** 7071  
**Status:** ✅ Production Ready

## 🏗️ Architecture

### Technology Stack
- **Framework:** Spring Boot 3.3.2
- **Java Version:** 17
- **Database:** MySQL
- **ORM:** JPA/Hibernate
- **Security:** Spring Security + JWT
- **API Documentation:** Swagger/OpenAPI 3.0

### Dependencies
- Spring Boot Web
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server
- OAuth2 Client
- JWT (jjwt)
- SpringDoc OpenAPI
- Common Service (shared utilities)
- MySQL Connector

## 📁 Project Structure

```
auth-service/
├── src/main/java/com/example/authservice/
│   ├── config/              # Configuration classes
│   ├── controller/          # REST controllers
│   ├── dto/                 # Data Transfer Objects
│   ├── model/                # Domain models
│   ├── repository/           # Data access layer
│   ├── service/              # Business logic
│   │   └── impl/             # Service implementations
│   ├── security/             # Security configuration
│   ├── crypto/               # Encryption utilities
│   ├── converter/            # Type converters
│   ├── mapper/               # Entity-DTO mappers
│   ├── init/                 # Data initialization
│   └── util/                 # Utility classes
├── src/main/resources/
│   ├── application.yml       # Application configuration
│   └── keys/                 # JWT keys (RSA)
└── pom.xml                   # Maven configuration
```

## 🔌 API Endpoints

### Base URL
- **Local:** http://localhost:7071
- **Swagger UI:** http://localhost:7071/swagger-ui.html

### Main Controllers
1. **AuthController** - Authentication endpoints
   - `/api/auth/register` - User registration
   - `/api/auth/login` - Login (multiple methods)
   - `/api/auth/refresh` - Token refresh
   - `/api/auth/logout` - Logout
   - `/api/auth/forgot-password` - Password reset
   - `/api/auth/change-password` - Change password

2. **UserController** - User profile management
   - `/api/user/me` - Get current user
   - `/api/user/profile` - Update profile

3. **AdminUserController** - Admin user management
   - `/api/admin/users` - List users
   - `/api/admin/users/{id}` - Get user details
   - `/api/admin/users/{id}` - Update user
   - `/api/admin/users/{id}` - Delete user

4. **AdminAuditController** - Audit log access
   - `/api/admin/audit` - Get audit logs

5. **ProjectTypeController** - Project type management
   - `/api/auth/v1/project-types` - CRUD operations

## 🗄️ Database Schema

### Core Entities
- **User** - User accounts
- **Role** - User roles (USER, ADMIN)
- **Credential** - Authentication credentials
- **RefreshToken** - Refresh tokens
- **Session** - Active sessions
- **OtpLog** - OTP verification logs
- **PendingReset** - Password reset requests
- **UserDetailMaster** - User profile details
- **ProjectType** - Project types
- **AuditLog** - Audit trail

### Authentication Methods
1. **Password** - Username/password
2. **Email** - Email/password
3. **Phone** - Phone/OTP
4. **MPIN** - Mobile PIN
5. **RSA** - RSA key-based
6. **WebAuthn** - Web Authentication (FIDO2)

## 🔐 Security

### Authentication Flow
1. User submits credentials
2. Service validates credentials
3. JWT tokens generated (access + refresh)
4. Tokens returned to client
5. Client includes Bearer token in subsequent requests

### JWT Configuration
- **Algorithm:** RS256 (RSA) or HS256 (HMAC)
- **Access Token Validity:** 900 seconds (15 minutes)
- **Refresh Token Validity:** 1209600 seconds (14 days)
- **Key Storage:** RSA keys in `keys/` directory or environment variables

### Security Features
- ✅ Password encryption (BCrypt)
- ✅ JWT token validation
- ✅ Role-based access control (RBAC)
- ✅ Session management
- ✅ OTP verification
- ✅ Rate limiting (via filters)
- ✅ CORS configuration

## 📊 Features

### User Management
- ✅ User registration
- ✅ Multiple login methods
- ✅ Token refresh
- ✅ Password reset
- ✅ Profile management
- ✅ Admin user management

### Authentication Methods
- ✅ Username/Password
- ✅ Email/Password
- ✅ Phone/OTP
- ✅ MPIN
- ✅ RSA key-based
- ✅ WebAuthn (FIDO2)

### Security Features
- ✅ JWT token generation
- ✅ Token refresh mechanism
- ✅ Session tracking
- ✅ Audit logging
- ✅ Password encryption
- ✅ OTP generation and verification

### Project Types
- ✅ CRUD operations
- ✅ Project type management

## 🧪 Testing

### Unit Tests
- Service layer tests
- Repository tests
- Security tests

### Integration Tests
- Controller tests
- Authentication flow tests
- Token validation tests

## 📝 Configuration

### Application Properties
```yaml
server:
  port: 7071

spring:
  datasource:
    url: jdbc:mysql://...
  jpa:
    hibernate:
      ddl-auto: update

JWT_PRIVATE_KEY_PATH: classpath:keys/jwt-private.pem
JWT_PUBLIC_KEY_PATH: classpath:keys/jwt-public.pem
JWT_SECRET: <secret>
JWT_ACCESS_TOKEN_VALIDITY_SECONDS: 900
JWT_REFRESH_TOKEN_VALIDITY_SECONDS: 1209600
```

## 🔧 Build & Deploy

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

### Docker
```bash
docker build -t auth-service .
docker run -p 7071:7071 auth-service
```

## 📈 Performance

### Optimizations
- Connection pooling
- Token caching
- Lazy loading for relationships
- Indexed database queries

### Monitoring
- Actuator endpoints
- Health checks
- Session metrics

## 🐛 Known Issues

1. **Duplicate Feign Dependency:** Warning about duplicate spring-cloud-starter-openfeign (non-critical)

## 🚀 Future Enhancements

1. OAuth2 provider integration
2. Social login (Google, Facebook)
3. Multi-factor authentication (MFA)
4. Account lockout after failed attempts
5. Password strength validation
6. Session management UI
7. User activity tracking

## 📚 Documentation

- **API Documentation:** Swagger UI at `/swagger-ui.html`
- **Setup Guide:** See project README
- **Security Guide:** See security documentation

## ✅ Code Quality

- ✅ All compilation errors resolved
- ✅ Common-service dependency properly configured
- ✅ Security configured
- ✅ Swagger integration complete
- ✅ Multiple authentication methods implemented

## 🔗 Related Services

- **Asset Service** (7073) - Uses auth for asset operations
- **Notification Service** (7072) - Sends auth-related notifications
- **Common Service** - Shared utilities and JWT verification

---

**Last Updated:** 2025-12-11  
**Maintained By:** Development Team

