# Epics, Features & User Stories
## Keeply — Complete Asset Lifecycle Management Platform

**Version:** 1.0.0  
**Last Updated:** July 2026  
**Document Type:** Product Backlog & Requirements Specification  
**Related Documents:**
- [BUSINESS_REQUIREMENTS_DOCUMENT.md](./BUSINESS_REQUIREMENTS_DOCUMENT.md) — Business requirements (BRD)
- [Technical Functionality Documentation](../Technical%20Functionality%20Documentation.md)
- [TECHNICAL_FUNCTIONAL_DOCUMENT.md](./TECHNICAL_FUNCTIONAL_DOCUMENT.md)
- [COMMUNICATION_OPT_OUT.md](./COMMUNICATION_OPT_OUT.md)
- [BLOCK_UNBLOCK_PDPA_COMPLIANCE.md](./BLOCK_UNBLOCK_PDPA_COMPLIANCE.md)

---

## Table of Contents

1. [Document Purpose & Scope](#1-document-purpose--scope)
2. [Product Vision & Personas](#2-product-vision--personas)
3. [Architecture Context](#3-architecture-context)
4. [Backlog Conventions](#4-backlog-conventions)
5. [Epic 0 — Platform Foundation (common-service)](#epic-0--platform-foundation-common-service)
6. [Epic 1 — Identity & Access Management (auth-service)](#epic-1--identity--access-management-auth-service)
7. [Epic 2 — Multi-Channel Notifications (notification-service)](#epic-2--multi-channel-notifications-notification-service)
8. [Epic 3 — Asset Lifecycle Management (asset-service)](#epic-3--asset-lifecycle-management-asset-service)
9. [Epic 4 — Customer Support & Helpdesk (helpdesk-service)](#epic-4--customer-support--helpdesk-helpdesk-service)
10. [Epic 5 — Keeply Mobile Experience (keeply_flutter_app)](#epic-5--keeply-mobile-experience-keeply_flutter_app)
11. [Epic 6 — Cross-Service Integration Journeys](#epic-6--cross-service-integration-journeys)
12. [Implementation Roadmap](#12-implementation-roadmap)
13. [Traceability Matrix](#13-traceability-matrix)
14. [Known Gaps & Future Backlog](#14-known-gaps--future-backlog)

---

## 1. Document Purpose & Scope

### 1.1 Purpose

This document defines the **product backlog** for the Keeply microservices platform in a structured hierarchy:

```
Program
 └── Epic (business outcome spanning weeks/months)
      └── Feature (cohesive capability)
           └── User Story (deliverable unit of value)
                └── Acceptance Criteria (testable conditions)
```

It is intended for product owners, business analysts, developers, QA engineers, and project managers to plan sprints, estimate work, and trace requirements to implemented services.

### 1.2 Scope

| In Scope | Out of Scope |
|----------|--------------|
| common-service (shared library) | keeply_react_app (web — separate backlog) |
| auth-service | Third-party payment / e-commerce checkout |
| notification-service | External LLM provider hosting (Ollama is client-side) |
| asset-service | |
| helpdesk-service | |
| keeply_flutter_app | |

### 1.3 Service Inventory

| Component | Type | Default Port | Database Schema |
|-----------|------|-------------|-----------------|
| common-service | Shared JAR library | N/A | `notification_retry_log` (when used) |
| auth-service | Microservice | 7071 | `authdb` |
| notification-service | Microservice | 7072 | `notificationdb` |
| helpdesk-service | Microservice | 7074 | `helpdeskdb` |
| asset-service | Microservice | 7075 | `assetdb` |
| keeply_flutter_app | Flutter mobile client | N/A | Local secure storage / Hive |

**Technology Stack:** Java 17, Spring Boot 3.4.x, PostgreSQL (Supabase-compatible), JWT (RS256/HS256), OpenFeign, Flutter 3+, BLoC, Dio.

---

## 2. Product Vision & Personas

### 2.1 Product Vision

**Keeply** empowers homeowners and small businesses to **register, track, and maintain** physical assets (appliances, electronics, vehicles, equipment) throughout their lifecycle — from purchase and warranty through maintenance, compliance, and support — via a secure microservices backend and an intuitive mobile experience.

### 2.2 Personas

| Persona | Description | Primary Touchpoints |
|---------|-------------|---------------------|
| **Homeowner (End User)** | Registers personal assets, uploads invoices, tracks warranty expiry | Flutter app, auth-service, asset-service, notification inbox |
| **Asset Administrator** | Manages catalog master data, bulk uploads, compliance rules | asset-service admin APIs, React/admin tooling |
| **Support Agent (L1/L2/L3)** | Handles tickets, answers queries, escalates per SLA | helpdesk-service |
| **Platform Administrator** | Manages users, audit logs, T&C, blocks accounts | auth-service admin APIs |
| **Compliance Officer** | Ensures PII encryption, opt-out, audit, field-level decrypt policies | auth-service, common-service, asset/helpdesk encrypted fields |
| **Developer / DevOps** | Deploys services, configures env, monitors health | common-service config, actuator, Render/Supabase |
| **Integration Agent** | Programmatic asset linking, master data, audit via agent APIs | asset-service agent controllers |

### 2.3 Business Goals

1. Reduce loss of warranty coverage due to missed expiry dates.
2. Centralize asset documentation (invoices, photos, AMC contracts).
3. Provide compliant identity and PII handling (DPDPA/PDPA).
4. Enable self-service support (FAQ, chatbot) before human escalation.
5. Deliver a unified mobile experience across four backend microservices.

---

## 3. Architecture Context

### 3.1 System Context Diagram

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         CLIENT APPLICATIONS                               │
│   keeply_flutter_app  │  keeply_react_app (web)  │  Postman / Integrations │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                    HTTP/REST + JWT Bearer Token
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                          MICROSERVICES LAYER                              │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │auth-service │ │notification │ │helpdesk-    │ │asset-service│        │
│  │   :7071     │ │  -service   │ │  service    │ │   :7075     │        │
│  │             │ │   :7072     │ │   :7074     │ │             │        │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘        │
│         │               │               │               │                 │
│         └───────────────┴───────────────┴───────────────┘                 │
│                                 │                                         │
│                    ┌────────────▼────────────┐                          │
│                    │     common-service       │                          │
│                    │  JWT, Encryption, Feign  │                          │
│                    │  ResponseWrapper, CORS   │                          │
│                    └─────────────────────────┘                          │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  PostgreSQL: authdb │ notificationdb │ helpdeskdb │ assetdb              │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Inter-Service Communication

| From | To | Mechanism | Purpose |
|------|-----|-----------|---------|
| All services | common-service | Maven dependency | Shared security, utilities |
| auth-service, asset-service | notification-service | Feign `NotificationClient` | OTP, alerts, lifecycle notifications |
| notification-service | auth-service | Feign `AuthServiceClient` | Communication opt-out preferences |
| asset-service | auth-service | JWT validation | Authenticate API requests |
| helpdesk-service | auth-service | JWT validation | User identity for tickets |
| keeply_flutter_app | All four services | Dio HTTP per-service URL | Mobile UX |

---

## 4. Backlog Conventions

### 4.1 Story ID Prefixes

| Prefix | Epic |
|--------|------|
| `CS-` | common-service |
| `AUTH-` | auth-service |
| `NOTIF-` | notification-service |
| `ASSET-` | asset-service |
| `HELP-` | helpdesk-service |
| `APP-` | keeply_flutter_app |
| `X-` | Cross-cutting integration |

### 4.2 Priority Levels

| Priority | Meaning |
|----------|---------|
| **P0** | Must-have for MVP / release blocker |
| **P1** | Important for core user journey |
| **P2** | Valuable enhancement |
| **P3** | Future / nice-to-have |

### 4.3 Story Format

Each user story includes:
- **As a** [persona]
- **I want** [goal]
- **So that** [benefit]
- **Acceptance Criteria** (Given / When / Then)
- **API Reference** (where applicable)
- **Priority** and **Dependencies**

---

## Epic 0 — Platform Foundation (common-service)

| Attribute | Value |
|-----------|-------|
| **Epic ID** | EPIC-0 |
| **Component** | common-service |
| **Business Value** | Eliminates duplicated security, encryption, and API patterns; ensures regulatory consistency |
| **Status** | Implemented (library) |

### Feature 0.1 — Cross-Service Security & JWT

**Description:** Centralized JWT creation, verification, and Spring Security filters consumed by all deployable microservices.

---

#### CS-01: Consistent JWT validation across services

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | Platform Architect |

**User Story:**  
As a **platform architect**, I want all microservices to validate JWTs using the same shared verifier so that a single login grants access to auth, asset, notification, and helpdesk APIs.

**Acceptance Criteria:**
- **Given** a valid access token issued by auth-service  
  **When** any downstream service receives a request with `Authorization: Bearer <token>`  
  **Then** `JwtVerifier` validates signature (RS256 with PEM keys or HS256 `JWT_SECRET` fallback) and extracts user claims.
- **Given** an expired or tampered token  
  **When** a protected endpoint is called  
  **Then** the service returns HTTP 401 with a structured error via `GlobalExceptionHandler`.

**Technical Reference:** `JwtUtil`, `JwtVerifier`, `JwtAuthFilter`, `AccessTokenFilter`

---

#### CS-02: Automatic Bearer token propagation on Feign calls

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | Service Developer |

**User Story:**  
As a **service developer**, I want Feign outbound calls to automatically include the current user's Bearer token so I do not reimplement auth headers in every client.

**Acceptance Criteria:**
- **Given** an authenticated inbound request with a JWT  
  **When** a service invokes a Feign client (e.g., `NotificationClient`)  
  **Then** `FeignTokenInterceptor` attaches `Authorization: Bearer <token>` to the outbound request.
- **Given** no security context  
  **When** a system-level call is made  
  **Then** the interceptor does not inject a stale or invalid token.

**Technical Reference:** `FeignAuthConfig`, `FeignTokenInterceptor`

---

#### CS-03: Reusable Spring Security filter chain integration

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | Security Engineer |

**User Story:**  
As a **security engineer**, I want shared JWT filters available to each service's `SecurityConfig` so authentication behavior is uniform.

**Acceptance Criteria:**
- Each service can register public paths (Swagger, actuator health, auth endpoints) and protect all others.
- Invalid credentials raise `BadCredentialsException`; locked accounts raise `LockedException`; disabled accounts raise `DisabledException`.

---

### Feature 0.2 — PII Encryption & Compliance Utilities

**Description:** AES-256-GCM encryption at the JPA layer, HMAC lookup hashes, masking, and validation utilities for DPDPA/PDPA compliance.

---

#### CS-04: Encrypt PII at rest in database columns

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | Compliance Officer |

**User Story:**  
As a **compliance officer**, I want sensitive user fields encrypted before database persistence so plaintext PII is never stored on disk.

**Acceptance Criteria:**
- **Given** an entity field annotated with `@Convert(converter = JpaAttributeEncryptor.class)`  
  **When** the entity is saved  
  **Then** the column stores AES-256-GCM ciphertext, not plaintext.
- **Given** a read operation  
  **When** JPA loads the entity  
  **Then** the application layer receives decrypted values transparently.
- Encryption key is loaded via `EncryptionKeyProvider` from `AUTH_ENC_KEY` or cloud configuration.

**Entities Using Encryption:** `User` (username, email, mobile), `AssetUserLink`, helpdesk `Issue` reporter/assignee fields, `InappLog`

---

#### CS-05: HMAC-based uniqueness lookups without decryption

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | Developer |

**User Story:**  
As a **developer**, I want to check email/mobile/username uniqueness using HMAC hashes so lookups do not require decrypting all rows.

**Acceptance Criteria:**
- Registration checks `emailHash`, `mobileHash`, `usernameHash` in composite `UserId` embeddable.
- Hash algorithm is consistent via `EncryptDecryptUtil.hmac()` with `AUTH_HMAC_KEY`.

---

#### CS-06: PII masking and field-level validation

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Persona** | API Consumer |

**User Story:**  
As an **API consumer**, I want masked PII in list responses and validators on input so accidental exposure is minimized.

**Acceptance Criteria:**
- `PiiMaskingUtil` masks email/mobile in non-admin responses.
- `PiiDataValidator` rejects malformed mobile numbers per country code rules.

---

### Feature 0.3 — Standard API Response & Error Handling

**Description:** Uniform `{ success, message, data }` wrapper and global exception mapping.

---

#### CS-07: Standardized API response envelope

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | Mobile Developer |

**User Story:**  
As a **Flutter developer**, I want every API to return the same JSON envelope so I can parse responses with one utility.

**Acceptance Criteria:**
- Success: `{ "success": true, "message": "...", "data": { ... } }`
- Failure: `{ "success": false, "message": "...", "data": null }`
- Flutter `ResponseWrapper.fromJson` and `jsonMapOf` unwrap `data` consistently.

**Technical Reference:** `ResponseWrapper<T>`

---

#### CS-08: Global exception handler for all services

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | API Consumer |

**User Story:**  
As an **API consumer**, I want predictable HTTP status codes and error messages for validation, auth, and server errors.

**Acceptance Criteria:**
- JWT errors → 401 with clear message.
- `@Valid` constraint violations → 400 with field-level details.
- Unhandled exceptions → 500 with sanitized message (no stack trace in production).

**Technical Reference:** `GlobalExceptionHandler` (`@ControllerAdvice`)

---

#### CS-09: Correlation ID propagation

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **Persona** | DevOps Engineer |

**User Story:**  
As an **ops engineer**, I want a correlation ID on every request so I can trace a user action across auth → asset → notification.

**Acceptance Criteria:**
- `CorrelationIdFilter` reads or generates `X-Correlation-Id`.
- ID appears in service logs for the request lifecycle.

---

### Feature 0.4 — Notification Delivery Abstraction

**Description:** Helpers and Feign client for reliable cross-service notification dispatch.

---

#### CS-10: Simple notification send helper

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | Service Developer |
| **Dependencies** | notification-service running |

**User Story:**  
As a **service developer**, I want to send a notification with template code and placeholders in one method call.

**Acceptance Criteria:**
- `NotificationHelper` builds `NotificationRequest` with channel, templateCode, placeholders, userId.
- Calls `NotificationClient` → `POST ${notification.service.url}/api/notifications`.
- Respects `common.notification.enabled=false` to no-op in test environments.

---

#### CS-11: Safe notification with retry and failure persistence

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Persona** | Reliability Engineer |

**User Story:**  
As a **reliability engineer**, I want failed notification calls retried and logged so OTP and alerts are not silently lost.

**Acceptance Criteria:**
- `SafeNotificationHelper` retries up to 3 times with backoff.
- After final failure, persists to `NotificationRetryLog` entity.
- Async variant available for non-blocking sends.

---

### Feature 0.5 — Shared Infrastructure Utilities

---

#### CS-12: JPA audit base entity

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Persona** | Developer |

**User Story:**  
As a **developer**, I want all domain entities to extend a base class with audit fields so created/updated metadata is automatic.

**Acceptance Criteria:** `BaseEntity` provides `createdBy`, `createdAt`, `updatedBy`, `updatedAt`, `active`.

---

#### CS-13: File storage utility

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Persona** | End User |

**User Story:**  
As a **user**, I want profile photos and documents stored in organized folders so they can be served and deleted reliably.

**Acceptance Criteria:**
- Files saved under `uploads/{entityType}/{filename}`.
- `FileStorageUtil` supports save, retrieve, delete.
- Auth registration and profile update use this for profile photos.

---

#### CS-14: Cloud datasource environment post-processors

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Persona** | DevOps Engineer |

**User Story:**  
As a **DevOps engineer**, I want JDBC URLs auto-configured from Supabase/Render environment variables.

**Acceptance Criteria:** Post-processors map `SUPABASE_*` / Render vars to Spring datasource properties per service schema.

---

#### CS-15: Shared CORS configuration

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Persona** | Web/Mobile Developer |

**User Story:**  
As a **client developer**, I want CORS preflight to succeed for Flutter web and React apps.

**Acceptance Criteria:** `KeeplyCorsConfiguration` applied consistently; allowed origins configurable.

---

### Feature 0.6 — Inter-Service Feign Clients

---

#### CS-16: Admin client for auth-service

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **Persona** | Asset Administrator |

**User Story:**  
As an **asset administrator**, I want to fetch admin users by project type for assignment workflows.

**Acceptance Criteria:** `AdminClient` calls `GET /api/auth/v1/admins?projectType=` on auth-service.

**Note:** Endpoint referenced in common-service; verify implementation in auth-service controllers.

---

#### CS-17: Asset user link client

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **Persona** | Auth Service |

**User Story:**  
As **auth-service**, I want to query user-asset links by subcategory for cross-domain reporting.

**Acceptance Criteria:** `AssetUserLinkClient` → `GET /api/asset/v1/userlinks/by-subcategory?subCategoryId=`

---

#### CS-18: Notification Feign client

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Persona** | Any Consumer Service |

**User Story:**  
As any **consumer service**, I want a typed Feign interface to notification-service.

**Acceptance Criteria:** `NotificationClient` posts to configurable `notification.service.url` (default `http://localhost:7072`).

---

## Epic 1 — Identity & Access Management (auth-service)

| Attribute | Value |
|-----------|-------|
| **Epic ID** | EPIC-1 |
| **Component** | auth-service |
| **Port** | 7071 |
| **Schema** | `authdb` (PostgreSQL) |
| **Business Value** | Secure, compliant identity for entire platform |
| **Status** | Implemented |

### Feature 1.1 — User Registration & Onboarding

---

#### AUTH-01: Standard user registration

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/auth/register` |

**User Story:**  
As a **new homeowner**, I want to register with my email, mobile, and password so I can start tracking my assets in Keeply.

**Acceptance Criteria:**
- **Given** valid registration payload with accepted T&C version  
  **When** I submit registration  
  **Then** a `User` and `UserDetailMaster` record is created with encrypted PII and HMAC hashes.
- **Given** duplicate email or mobile for same `projectType`  
  **When** I register  
  **Then** HTTP 409 or validation error is returned.
- User receives default `ROLE_USER`.

---

#### AUTH-02: Multipart registration with profile photo

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/auth/register` (multipart) |

**User Story:**  
As a **new user**, I want to upload a profile photo during registration so my account feels personal.

**Acceptance Criteria:**
- Multipart form supports JSON user fields + image file.
- Photo stored via `FileStorageUtil`; URL saved on user profile.

---

#### AUTH-03: Admin user registration

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/auth/adminregister` |

**User Story:**  
As a **platform administrator**, I want to create admin accounts with elevated privileges.

**Acceptance Criteria:** Created user has `ROLE_ADMIN`; only authorized callers can invoke endpoint.

---

#### AUTH-04: Project type master data management

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/auth/v1/project-types` |

**User Story:**  
As a **product owner**, I want project types (ECOM, ASSET, etc.) managed as reference data so users and templates are scoped correctly.

**Acceptance Criteria:**
- CRUD operations on `ProjectType` entity.
- `GET /validate/{code}` returns whether code is active.

---

#### AUTH-05: Versioned Terms & Conditions

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `/api/auth/terms-and-conditions` |

**User Story:**  
As a **legal stakeholder**, I want versioned T&C per project type so registration records which version the user accepted.

**Acceptance Criteria:**
- Create, list, get by version, activate/deactivate endpoints.
- Registration rejected if T&C not accepted or version inactive.

---

### Feature 1.2 — Multi-Method Authentication

---

#### AUTH-06: Password login

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/auth/login` (`loginType: PASSWORD`) |

**User Story:**  
As a **returning user**, I want to log in with username/email/mobile and password.

**Acceptance Criteria:**
- Returns `accessToken`, `refreshToken`, expiry metadata.
- BCrypt password verification.
- Failed attempts increment counter; lockout per policy.
- Audit log entry created.

---

#### AUTH-07: OTP login

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/auth/otp/send`, `POST /api/auth/login` (`loginType: OTP`) |

**User Story:**  
As a **mobile-first user**, I want to log in with a one-time password sent to my phone or email.

**Acceptance Criteria:**
- OTP hashed in `OtpLog` with expiry timestamp.
- OTP delivered via `SafeNotificationHelper` → notification-service.
- Expired or wrong OTP returns 401.

---

#### AUTH-08: MPIN login

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/auth/mpin/register`, `/api/auth/mpin/verify`, login `loginType: MPIN` |

**User Story:**  
As a **frequent app user**, I want a short PIN for quick repeat access.

**Acceptance Criteria:**
- MPIN stored hashed in `Credential` entity.
- Register, verify, and login flows functional.
- Reset via request/confirm with `PendingReset` token.

---

#### AUTH-09: RSA key-based login

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `/api/auth/credential/rsa/challenge/{userId}`, `/credential/rsa/verify` |

**User Story:**  
As a **security-conscious user**, I want to authenticate using my RSA key pair.

**Acceptance Criteria:** Challenge-response flow; signature verified against stored public key in `Credential`.

---

#### AUTH-10: WebAuthn / Passkey login

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `/api/auth/credential/webauthn/challenge/{userId}`, `/credential/webauthn/verify` |

**User Story:**  
As a **modern mobile user**, I want passkey/biometric login via WebAuthn.

**Acceptance Criteria:** WebAuthn registration and verification; credential type `WEBAUTHN` in `credentials` table.

---

#### AUTH-11: Auth code login

| Field | Detail |
|-------|--------|
| **Priority** | P3 |
| **API** | `POST /api/auth/login` (`loginType: AUTHCODE`) |

**User Story:**  
As an **enterprise integrator**, I want alternate auth code based login for delegated auth scenarios.

**Acceptance Criteria:** Valid auth code exchanges for JWT pair; code single-use and time-limited.

---

### Feature 1.3 — Session & Token Lifecycle

---

#### AUTH-12: JWT access and refresh token issuance

| Field | Detail |
|-------|--------|
| **Priority** | P0 |

**User Story:**  
As a **security architect**, I want short-lived access tokens and longer refresh tokens to balance security and UX.

**Acceptance Criteria:**
- Access token TTL: ~900 seconds (15 minutes).
- Refresh token TTL: ~14 days.
- Refresh token stored hashed in `refresh_tokens`; linked to `sessions`.

---

#### AUTH-13: Token refresh

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/auth/refresh` |

**User Story:**  
As a **mobile app**, I want to obtain a new access token using my refresh token without re-login.

**Acceptance Criteria:**
- Valid refresh token returns new access token.
- Revoked or expired refresh token returns 401.

---

#### AUTH-14: Logout and session revocation

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/auth/logout` |

**User Story:**  
As a **user**, I want to log out and invalidate my session on this device.

**Acceptance Criteria:** Session marked revoked; refresh token invalidated; subsequent API calls with old access token fail after expiry.

---

### Feature 1.4 — Profile & Account Management

---

#### AUTH-15: View and update profile

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET/PUT /api/auth/profile/me`, `/profile/{userId}` |

**User Story:**  
As a **user**, I want to manage my address, bio, demographics, social links, skills, and timezone.

**Acceptance Criteria:**
- JSON and multipart (photo) update supported.
- Non-owner cannot update another user's profile (unless admin).

---

#### AUTH-16: Change email or mobile with OTP

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/auth/contact/change/request`, `/contact/change/confirm` |

**User Story:**  
As a **user**, I want to change my contact details only after verifying the new value via OTP.

**Acceptance Criteria:** Two-step flow; old contact notified if configured; hashes updated on confirm.

---

#### AUTH-17: Communication opt-out preferences

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET/PUT .../communication-preferences` |

**User Story:**  
As a **user**, I want to opt out of SMS, email, WhatsApp, in-app, or push notifications independently.

**Acceptance Criteria:**
- Flags on `UserDetailMaster`: `optOutSms`, `optOutEmail`, `optOutWhatsapp`, `optOutInapp`, `optOutPush`.
- notification-service reads these before send (see NOTIF-06).

---

#### AUTH-18: Self-service account soft delete

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `DELETE /api/users/me` |

**User Story:**  
As a **user exercising data rights**, I want to delete my account while the platform retains audit records per policy.

**Acceptance Criteria:** User marked inactive/soft-deleted; login disabled; audit trail preserved.

---

#### AUTH-19: Self-service decrypted profile access

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `GET /api/users/me/details-decrypted`, `/me/decrypt/{field}` |

**User Story:**  
As a **user**, I want to view my own full decrypted PII when permitted by regulation.

**Acceptance Criteria:** Only self or admin can access; single-field decrypt limits blast radius.

---

### Feature 1.5 — Admin User Management & Audit

---

#### AUTH-20: Admin list and inspect users

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `GET /api/admin/users`, `GET /api/admin/users/{userId}/details-decrypted` |
| **Role** | `ROLE_ADMIN` |

**User Story:**  
As an **administrator**, I want to list users and view decrypted profiles for support investigations.

---

#### AUTH-21: Block, unblock, and permanent block

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/admin/users/{userId}/block`, `/unblock`, `/permanent-block` |

**User Story:**  
As an **administrator**, I want to temporarily or permanently block abusive accounts.

**Acceptance Criteria:** Blocked users cannot login; temporary block has expiry; permanent block requires elevated action.

---

#### AUTH-22: Admin field-level and bulk decryption

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `GET /api/admin/users/{userId}/decrypt/{field}`, `POST /api/admin/decrypt` |

**User Story:**  
As a **compliance-approved admin**, I want controlled decryption for lawful investigations.

**Acceptance Criteria:** All decrypt actions audit-logged.

---

#### AUTH-23: Audit log export

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `GET /api/admin/audit/logs`, `/logs/paged`, `/logs/csv`, `/logs/excel` |

**User Story:**  
As an **auditor**, I want to export authentication and admin action logs.

**Acceptance Criteria:** Paged JSON and CSV/Excel download; includes actor, action, timestamp, IP if captured.

---

### Feature 1.6 — OTP & Alternative Credentials

---

#### AUTH-24: Send OTP for multiple purposes

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/auth/otp/send` |

**User Story:**  
As a **user**, I want OTP for login, registration verification, and contact change.

**Acceptance Criteria:** Rate limiting per mobile/email; OTP expiry configurable; channel respects opt-out except security-critical flows.

---

#### AUTH-25: Register RSA, WebAuthn, MPIN credentials

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/auth/credential/register` |

**User Story:**  
As a **user**, I want to register alternative login methods alongside password.

**Acceptance Criteria:** Credentials stored in `credentials` with type enum; multiple credentials per user allowed per type policy.

---

## Epic 2 — Multi-Channel Notifications (notification-service)

| Attribute | Value |
|-----------|-------|
| **Epic ID** | EPIC-2 |
| **Component** | notification-service |
| **Port** | 7072 |
| **Schema** | `notificationdb` |
| **Business Value** | Timely,user-respecting communication across channels |
| **Status** | Implemented (log/queue model; external providers pending) |

### Feature 2.1 — Template-Based Notification Sending

---

#### NOTIF-01: Send notification by template

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/notifications` |

**User Story:**  
As **auth-service or asset-service**, I want to enqueue a notification using a template code and placeholder map.

**Acceptance Criteria:**
- Returns HTTP 202 Accepted.
- Resolves template from channel-specific master table.
- Renders `{{placeholder}}` via `TemplateEngineUtil`.
- Persists rendered message to appropriate log table.

**Request Body (`NotificationRequest`):** channel, username, mobile, email, subject, templateCode, placeholders, userId

---

#### NOTIF-02: Channel-specific template masters

| Field | Detail |
|-------|--------|
| **Priority** | P0 |

**User Story:**  
As a **product manager**, I want independent templates for SMS, WhatsApp, Email, and In-App per project type.

**Acceptance Criteria:**
- Tables: `notification_template_master`, `sms_template_master`, `whatsapp_template_master`, `inapp_template_master`.
- Fields: templateCode, name, subject/title, body, placeholders (JSON), projectType, active.
- Seeded templates for ASSET_MGMT (assign, return, maintenance, warranty, bulk upload) and ECOM (OTP, order, shipment).

---

#### NOTIF-03: Email channel alias

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **developer**, I want both `EMAIL` and `NOTIFICATION` channel codes to resolve to email templates.

**Acceptance Criteria:** Both codes use `NotificationTemplateMaster` and `notification_log`.

---

### Feature 2.2 — Communication Opt-Out Enforcement

---

#### NOTIF-04: Respect user opt-out before send

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Dependencies** | AUTH-17 |

**User Story:**  
As a **user**, I want notifications suppressed on channels I opted out of.

**Acceptance Criteria:**
- **Given** `notification.opt-out-check.enabled=true` and valid Bearer token  
  **When** send is requested  
  **Then** service calls `GET /api/auth/profile/{userId}/communication-preferences` and skips channel if opted out.
- Skipped sends logged or silently dropped per configuration.

---

### Feature 2.3 — In-App Notification Center

---

#### NOTIF-05: List in-app notifications

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET /api/notifications/list`, `/list/{userId}` |

**User Story:**  
As a **mobile user**, I want to see my in-app notification history.

**Acceptance Criteria:**
- Default: last 30 days (`notification.list.display-days`).
- Max 100 results (`notification.list.max-results`).
- Returns `NotificationListResponse` with id, title, body, read flag, timestamp.

---

#### NOTIF-06: Unread count for badge

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET /api/notifications/count`, `/count/{userId}` |

**User Story:**  
As a **mobile user**, I want a badge showing unread alert count on the Alerts tab.

**Acceptance Criteria:** Count reflects `read=false` in `inapp_log` for user.

---

#### NOTIF-07: Mark read, unread, toggle, bulk operations

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `PUT /read/{id}`, `/read-all`, `PATCH /toggle-read/{id}`, `PUT /unread/{id}`, etc. |

**User Story:**  
As a **user**, I want full control over read state of my notifications.

**Acceptance Criteria:** All single and bulk variants work for JWT user and optional explicit userId paths.

---

#### NOTIF-08: Encrypt in-app notification content at rest

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **compliance officer**, I want in-app message bodies encrypted in the database.

**Acceptance Criteria:** `InappLog` body/title fields use `JpaAttributeEncryptor`.

---

### Feature 2.4 — Notification Audit & Future Delivery

---

#### NOTIF-09: Per-channel delivery logs

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As an **ops engineer**, I want every notification attempt recorded for audit and replay.

**Acceptance Criteria:** Inserts into `sms_log`, `whatsapp_log`, `notification_log`, or `inapp_log` with timestamp, userId, rendered content.

---

#### NOTIF-10: Provider integration readiness

| Field | Detail |
|-------|--------|
| **Priority** | P3 |
| **Status** | Future |

**User Story:**  
As a **platform owner**, I want to plug in Twilio, SMTP, and FCM without schema changes.

**Acceptance Criteria:** `provider_message_id` and `provider_response` populated when external dispatch implemented.

---

#### NOTIF-11: Push notification channel

| Field | Detail |
|-------|--------|
| **Priority** | P3 |
| **Status** | Future |

**User Story:**  
As a **mobile user**, I want native push notifications with opt-out honored via `optOutPush`.

**Acceptance Criteria:** New `PUSH` channel handler; FCM/APNs integration; respects auth preferences.

---

## Epic 3 — Asset Lifecycle Management (asset-service)

| Attribute | Value |
|-----------|-------|
| **Epic ID** | EPIC-3 |
| **Component** | asset-service |
| **Port** | 7075 |
| **Schema** | `assetdb` |
| **Business Value** | Core asset registry, coverage tracking, compliance |
| **Status** | Implemented |

### Feature 3.1 — Product Catalog & Master Data

---

#### ASSET-01: Category hierarchy CRUD

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `/api/asset/v1/categories`, `/subcategories`, `/makes`, `/models` |

**User Story:**  
As an **administrator**, I want a four-level product hierarchy (Category → SubCategory → Make → Model) so assets are classified consistently.

**Acceptance Criteria:** Full CRUD; parent-child integrity enforced; inactive records soft-disabled via `active` flag.

---

#### ASSET-02: Bulk JSON and Excel upload for masters

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `.../bulk`, `.../bulk/excel` on master controllers |

**User Story:**  
As an **administrator**, I want to import hundreds of catalog rows from Excel templates.

**Acceptance Criteria:** Template format documented in `asset-service/docs/EXCEL_BULK_UPLOAD_FORMAT.md`; validation errors returned per row.

---

#### ASSET-03: Category images and display ordering

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | Favourite/most-like/sequence on categories |

**User Story:**  
As a **user**, I want room/category tiles with images and personalized ordering.

**Acceptance Criteria:** Image upload; `isFavourite`, `isMostLike`, `sequenceOrder` fields; image classification endpoint on categories.

---

#### ASSET-04: Vendor and purchase outlet masters

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/vendors`, `/outlets` |

**User Story:**  
As a **user**, I want to record where I purchased an asset and from which vendor.

**Acceptance Criteria:** VendorMaster and PurchaseOutlet CRUD with bulk support.

---

#### ASSET-05: Reference lookups (status, entity type, document type)

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/statuses`, `/entity-types`, `/document-types` |

**User Story:**  
As a **developer**, I want lookup APIs for dropdowns and validation.

---

#### ASSET-06: Master data agent API

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `/api/asset/v1/masters` |

**User Story:**  
As an **integration agent**, I want programmatic CRUD and validation across all master entities.

**Acceptance Criteria:** Summary endpoint returns counts per entity; validation endpoint checks referential integrity.

---

### Feature 3.2 — Asset CRUD & Complete Creation

---

#### ASSET-07: Create individual asset

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/asset/v1/assets` |

**User Story:**  
As a **homeowner**, I want to register an asset with name, serial number, purchase date, and status.

**Acceptance Criteria:** Creates `AssetMaster` linked to category hierarchy; returns wrapped response with asset ID.

---

#### ASSET-08: Complete asset creation (single transaction)

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/asset/v1/assets/complete` (multipart) |

**User Story:**  
As a **mobile user**, I want to add an asset with warranty, invoice, photo, and self-assignment in one submit.

**Acceptance Criteria:**
- **Given** multipart form with asset JSON, warranty dates, invoice file, optional photo  
  **When** submitted  
  **Then** asset, warranty, documents, and `AssetUserLink` created atomically.
- Notification sent on success if configured.

---

#### ASSET-09: Search, update, delete assets

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET /assets/search`, `PUT /assets/{id}`, `DELETE /assets/{id}` |

**User Story:**  
As a **user**, I want to find and maintain my asset records over time.

---

#### ASSET-10: Favourite and display ordering

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | PATCH favourite/most-like/sequence on assets |

**User Story:**  
As a **user**, I want to pin favourite assets on my dashboard.

---

#### ASSET-11: Bulk asset import

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | Bulk JSON and Excel on `/assets` |

**User Story:**  
As an **administrator**, I want to import many assets at once for migration or enterprise onboarding.

---

### Feature 3.3 — Warranty & AMC Management

---

#### ASSET-12: Warranty CRUD with documents

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `/api/asset/v1/warranty` |

**User Story:**  
As an **asset owner**, I want to record warranty start/end dates and attach the warranty card or certificate.

**Acceptance Criteria:** One-to-one `AssetWarranty` per asset; document linkage via `AssetDocument`.

---

#### ASSET-13: AMC CRUD with documents

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/asset/v1/amc` |

**User Story:**  
As an **asset owner**, I want to track annual maintenance contract dates and service agreements.

---

#### ASSET-14: Need-your-attention coverage reminders

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET /api/asset/v1/userlinks/need-your-attention` |

**User Story:**  
As a **user**, I want a consolidated list of assets with warranty or AMC expiring soon so I can act before coverage lapses.

**Acceptance Criteria:**
- Returns assets/links where warranty or AMC end date within configured threshold (e.g., 14 days).
- Used by Flutter dashboard and may trigger notification templates.

---

### Feature 3.4 — Document Management

---

#### ASSET-15: Upload asset documents

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/asset/v1/documents/upload` |

**User Story:**  
As a **user**, I want to attach invoices, photos, and PDFs to my assets.

**Acceptance Criteria:** Document type validated against `DocumentTypeMaster`; file size/type limits enforced.

---

#### ASSET-16: Download documents

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET /documents/download/{id}`, `/files` |

**User Story:**  
As a **user**, I want to open or download documents from asset detail view in the app.

---

#### ASSET-17: Bulk document upload

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | Bulk upload on documents controller |

**User Story:**  
As an **administrator**, I want to attach documents to many assets in one operation.

---

### Feature 3.5 — User–Asset Linking

---

#### ASSET-18: Link and delink assets/components to users

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `/api/asset/v1/userlinks` |

**User Story:**  
As a **user**, I want assets and components assigned to me so my dashboard shows only my items.

**Acceptance Criteria:** Single and multi link/delink; supports entity types (asset, component, etc.).

---

#### ASSET-19: List assets assigned to user

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET /api/asset/v1/user-asset-links/user/{id}/assets` |

**User Story:**  
As a **mobile user**, I want my assigned assets loaded for dashboard and browse views.

---

#### ASSET-20: Assignment history and statistics

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `/api/asset/v1/user-asset-links` agent endpoints |

**User Story:**  
As an **administrator**, I want link history and stats for operational reporting.

---

#### ASSET-21: Encrypt assignee PII in links

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **compliance officer**, I want user identifying fields in `AssetUserLink` encrypted at rest.

---

### Feature 3.6 — Scanning & Identification

---

#### ASSET-22: Scan barcode/QR to find asset

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/asset/v1/scan` |

**User Story:**  
As a **user**, I want to scan a barcode or QR code to quickly pull up an existing asset.

**Acceptance Criteria:** Matches asset ID, UDV name, or serial number; operation audit-logged.

---

#### ASSET-23: Scan QR from image upload

| Field | Detail |
|-------|--------|
| **Priority** | P2 |

**User Story:**  
As a **user**, I want to decode a QR code from a photo in my gallery.

---

#### ASSET-24: Save scanned product as new asset

| Field | Detail |
|-------|--------|
| **Priority** | P2 |

**User Story:**  
As a **user**, I want to create an asset from scan results when no match exists.

---

### Feature 3.7 — Components & Purchase Info

---

#### ASSET-25: Asset components

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/asset/v1/components` |

**User Story:**  
As a **user**, I want to track attachable components (filters, remotes, chargers) linked to parent assets.

---

#### ASSET-26: Purchase information

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **user**, I want purchase price, date, and outlet stored with each asset.

**Acceptance Criteria:** `AssetPurchaseInfo` linked to `AssetMaster` and `PurchaseOutlet`.

---

### Feature 3.8 — Compliance Engine

---

#### ASSET-27: Compliance rules management

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `/api/asset/v1/compliance/rules` |

**User Story:**  
As a **compliance manager**, I want rules per entity type with severity and rule type masters.

**Acceptance Criteria:** Rule CRUD, templates, initialize defaults; linked to `ComplianceRuleTypeMaster`, `ComplianceSeverityMaster`.

---

#### ASSET-28: Validate assets and track violations

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `/api/asset/v1/compliance` |

**User Story:**  
As a **compliance manager**, I want assets validated against rules with resolvable violations.

**Acceptance Criteria:** Validate endpoint returns violations; resolve workflow updates `ComplianceViolation` status.

---

#### ASSET-29: Compliance metrics and reports

| Field | Detail |
|-------|--------|
| **Priority** | P3 |
| **API** | Compliance metrics and report endpoints |

**User Story:**  
As an **executive**, I want compliance dashboards exported from the API.

---

### Feature 3.9 — OCR, LLM & Intelligent Extraction

---

#### ASSET-30: Product label OCR

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `/api/asset/v1/products/ocr` |

**User Story:**  
As a **user**, I want to photograph a product label and extract make, model, and serial automatically.

**Acceptance Criteria:** Tesseract OCR; correct and train endpoints; patterns stored in `OcrLearnedPattern`.

---

#### ASSET-31: Intelligent document extraction

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `/api/asset/v1/intelligent-extraction` |

**User Story:**  
As a **user**, I want invoice and warranty card fields extracted from uploaded PDFs/images.

---

#### ASSET-32: LLM-based extraction

| Field | Detail |
|-------|--------|
| **Priority** | P3 |
| **API** | `/api/asset/v1/llm-extraction` |

**User Story:**  
As a **power user**, I want unstructured document parsing via LLM for complex layouts.

---

### Feature 3.10 — Audit & Lifecycle Notifications

---

#### ASSET-33: Asset operation audit trail

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/asset/v1/audit` |

**User Story:**  
As an **auditor**, I want searchable logs of asset CRUD, scans, and assignments.

**Acceptance Criteria:** Log, search, statistics, cleanup agent endpoints.

---

#### ASSET-34: Trigger notifications on lifecycle events

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Dependencies** | NOTIF-01 |

**User Story:**  
As an **asset owner**, I want in-app alerts when warranty nears expiry or bulk upload completes.

**Acceptance Criteria:** `SafeNotificationHelper` invoked with appropriate template codes on configured events.

---

## Epic 4 — Customer Support & Helpdesk (helpdesk-service)

| Attribute | Value |
|-----------|-------|
| **Epic ID** | EPIC-4 |
| **Component** | helpdesk-service |
| **Port** | 7074 |
| **Schema** | `helpdeskdb` |
| **Business Value** | Structured support with SLA accountability |
| **Status** | Implemented |

### Feature 4.1 — Issue / Ticket Management

---

#### HELP-01: Create custom support ticket

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `POST /api/helpdesk/issues` |

**User Story:**  
As a **user**, I want to open a ticket with title and description when I have a problem.

**Acceptance Criteria:**
- Issue created with status `OPEN`, priority default or supplied.
- `loginUserId` from JWT stored as reporter.
- `relatedService` enum: AUTH_SERVICE, NOTIFICATION_SERVICE, ASSET_SERVICE, HELPDESK_SERVICE, UPCOMING_PROJECT.

---

#### HELP-02: Create ticket from issue master

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/helpdesk/issues` with `issueMasterId` |

**User Story:**  
As a **user**, I want to pick a predefined issue type (e.g., "AC not cooling") linked to my appliance category.

**Acceptance Criteria:** Title/description/category inherited from `IssueMaster`; optional assetId, componentId, sparePartId.

---

#### HELP-03: View my tickets

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET /api/helpdesk/issues/my-issues` |

**User Story:**  
As a **user**, I want to see all tickets I have raised.

---

#### HELP-04: Agent assign, status, resolve, close

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `PATCH /{id}/assign`, `/status`, `POST /{id}/resolve`, `PATCH /{id}/close` |

**User Story:**  
As a **support agent**, I want to own and progress tickets through their lifecycle.

**Acceptance Criteria:**
- Status enum: OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED.
- Resolve requires resolution text; SLA resolution timestamp updated.

---

#### HELP-05: Filter tickets by status and service

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `GET /status/{status}`, `/service/{service}` |

**User Story:**  
As a **support manager**, I want queues filtered by status and originating microservice.

---

### Feature 4.2 — Issue Master Catalog

---

#### HELP-06: Manage predefined issue types

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/helpdesk/issue-master` |

**User Story:**  
As an **administrator**, I want issue templates tied to asset taxonomy (category, subcategory, component).

**Acceptance Criteria:** CRUD; filter by categoryId, subCategoryId, componentId; seeded repair issues via Flyway.

---

### Feature 4.3 — User Queries

---

#### HELP-07: Submit user query

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/helpdesk/queries` |

**User Story:**  
As a **user**, I want to ask a question when FAQs are insufficient.

**Acceptance Criteria:** Query status `PENDING`; askedBy encrypted.

---

#### HELP-08: Agent answer query

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/helpdesk/queries/{id}/answer` |

**User Story:**  
As a **support agent**, I want to post an official answer to a user query.

**Acceptance Criteria:** Status → `ANSWERED`; answeredBy encrypted.

---

#### HELP-09: Close query

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `PATCH /api/helpdesk/queries/{id}/close` |

**User Story:**  
As a **user**, I want to close my query when satisfied.

---

### Feature 4.4 — FAQ & Knowledge Base

---

#### HELP-10: Search and browse FAQs

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **API** | `GET /api/helpdesk/faqs`, `/search`, `/service/{service}`, `/category/{category}` |

**User Story:**  
As a **user**, I want to find answers before opening a ticket.

---

#### HELP-11: Mark FAQ helpful

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `POST /api/helpdesk/faqs/{id}/helpful` |

**User Story:**  
As a **user**, I want to indicate which FAQs solved my problem.

---

#### HELP-12: Admin FAQ curation

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | PUT favourite, most-like, sequence-order; DELETE |
| **Role** | `ROLE_ADMIN` |

**User Story:**  
As an **administrator**, I want to feature and order FAQs for better self-service.

---

#### HELP-13: Service knowledge articles

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/helpdesk/knowledge` |

**User Story:**  
As a **user**, I want detailed articles about platform features per service.

**Acceptance Criteria:** CRUD (admin write); search by keyword and service.

---

### Feature 4.5 — SLA & Escalation

---

#### HELP-14: Escalation matrix configuration

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `/api/helpdesk/escalation-matrix` |

**User Story:**  
As a **support manager**, I want SLA targets and escalation paths per service, priority, and level (L1/L2/L3).

**Acceptance Criteria:** `EscalationMatrix` defines response SLA, resolution SLA, escalation target, auto-escalation delay.

---

#### HELP-15: Auto-escalation on SLA breach

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/helpdesk/escalations/issue/{id}/auto-escalate` |

**User Story:**  
As a **support manager**, I want tickets automatically escalated when response or resolution SLA is breached.

**Acceptance Criteria:**
- Scheduled job runs every 60 seconds.
- `IssueEscalation` history records from/to level, reason, escalatedBy.
- `SLATracking` updated with breach timestamps.

---

#### HELP-16: SLA breach reporting

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `GET /api/helpdesk/sla/breaches` |

**User Story:**  
As a **support manager**, I want a list of all SLA breaches for weekly review.

---

#### HELP-17: Record first response

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/helpdesk/sla/issue/{issueId}/first-response` |

**User Story:**  
As a **system**, I want first agent response time captured for response SLA metrics.

---

#### HELP-18: Manual escalation

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/helpdesk/escalations/issue/{issueId}` |

**User Story:**  
As a **support lead**, I want to manually escalate complex tickets to L2/L3.

---

### Feature 4.6 — Rule-Based Chatbot

---

#### HELP-19: Chatbot message exchange

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **API** | `POST /api/helpdesk/chatbot/message` |

**User Story:**  
As a **user**, I want instant answers from a chatbot before creating a ticket.

**Acceptance Criteria:**
- Keyword match against FAQs and `ServiceKnowledge`.
- Canned responses for auth, asset, notification topics.
- Session persisted in `ChatbotSession` / `ChatbotMessage`.

---

#### HELP-20: Retrieve chat session history

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **API** | `GET /api/helpdesk/chatbot/session/{sessionId}` |

**User Story:**  
As a **user**, I want to resume and review my chatbot conversation.

---

### Feature 4.7 — Helpdesk Integrations

---

#### HELP-21: JWT-authenticated helpdesk APIs

| Field | Detail |
|-------|--------|
| **Priority** | P0 |

**User Story:**  
As a **helpdesk service**, I want all APIs protected by the same JWT as other services.

**Acceptance Criteria:** Shared `JwtAuthFilter`; reporter/assignee PII encrypted.

---

#### HELP-22: Ticket event notifications (planned)

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **Status** | Backlog |

**User Story:**  
As a **user**, I want in-app notification when my ticket is assigned, updated, or resolved.

**Acceptance Criteria:** Integrate `SafeNotificationHelper` on create/assign/resolve; config already has `notification.service.url`.

---

## Epic 5 — Keeply Mobile Experience (keeply_flutter_app)

| Attribute | Value |
|-----------|-------|
| **Epic ID** | EPIC-5 |
| **Component** | keeply_flutter_app |
| **Stack** | Flutter 3+, BLoC, Dio, FlutterSecureStorage |
| **Business Value** | Primary end-user channel for asset management |
| **Status** | Implemented |

### Feature 5.1 — App Shell & Navigation

---

#### APP-01: Splash and auth routing

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screen** | `SplashPage` |

**User Story:**  
As a **user**, I want the app to open to splash and route me to home or welcome based on stored session.

**Acceptance Criteria:**
- Valid tokens → `KeeplyMobileShell`.
- No/expired session → `WelcomePage`.

---

#### APP-02: Five-tab bottom navigation shell

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screen** | `KeeplyMobileShell` |

**User Story:**  
As a **user**, I want persistent navigation across Dashboard, Rooms, Add Asset, Tips, and Alerts.

**Acceptance Criteria:** Tabs: (0) Dashboard, (1) Browse Rooms, (2) Create Asset, (3) Tips Hub, (4) Alerts Hub.

---

#### APP-03: Account hub and deep links

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screen** | `AccountHubPage` |

**User Story:**  
As a **user**, I want one place for profile, settings, helpdesk, master data browse, and sign out.

---

### Feature 5.2 — Authentication UX

---

#### APP-04: Welcome and registration

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screens** | `WelcomePage`, `RegisterPage` |

**User Story:**  
As a **new user**, I want a guided path from welcome to registration.

**Acceptance Criteria:** Calls `POST /api/auth/register`; stores tokens on success; shows validation errors.

---

#### APP-05: Login with password or OTP

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screen** | `LoginPage` |

**User Story:**  
As a **returning user**, I want flexible login methods matching auth-service capabilities.

---

#### APP-06: Automatic token refresh

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Component** | `AuthInterceptor` |

**User Story:**  
As a **user**, I want seamless API access without frequent re-login.

**Acceptance Criteria:**
- **Given** API returns 401  
  **When** refresh token is valid  
  **Then** interceptor calls `POST /api/auth/refresh` and retries original request once.

---

#### APP-07: Profile edit with photo

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Screen** | `AccountProfilePage` |

**User Story:**  
As a **user**, I want to update my profile and photo from the app.

**Acceptance Criteria:** Multipart PUT to auth profile API; `AuthBloc` reflects updates.

---

#### APP-08: Secure logout

| Field | Detail |
|-------|--------|
| **Priority** | P0 |

**User Story:**  
As a **user**, I want logout to clear local tokens and revoke server session.

**Acceptance Criteria:** `FlutterSecureStorage` cleared; `POST /api/auth/logout` called; navigate to welcome.

---

### Feature 5.3 — Dashboard & Home

---

#### APP-09: Asset dashboard with room filter

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screen** | `DashboardPage` |

**User Story:**  
As a **homeowner**, I want a visual dashboard of my assets with room-based filtering.

**Acceptance Criteria:**
- Loads assigned assets via `user-asset-links` or fallback.
- Room filter narrows displayed asset strip.
- `AppDataRefreshCubit` triggers reload on asset changes.

---

#### APP-10: Coverage reminder cards

| Field | Detail |
|-------|--------|
| **Priority** | P0 |

**User Story:**  
As a **user**, I want warranty/AMC expiry highlighted within 14 days on the dashboard.

**Acceptance Criteria:** Parses `need-your-attention` response into reminder UI cards.

---

#### APP-11: Browse assets by room

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Screen** | `BrowseRoomsPage` |

**User Story:**  
As a **user**, I want a dedicated view to browse all my assets organized by room/category.

---

#### APP-12: Open issues count on dashboard

| Field | Detail |
|-------|--------|
| **Priority** | P2 |

**User Story:**  
As a **user**, I want to see how many helpdesk tickets I have open from the dashboard.

**Acceptance Criteria:** Calls helpdesk `my-issues`; displays count badge or card.

---

#### APP-13: Unread alerts badge in shell header

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **user**, I want unread notification count visible without opening Alerts tab.

**Acceptance Criteria:** `GET /api/notifications/count` on shell load and refresh.

---

### Feature 5.4 — Asset Creation & Management

---

#### APP-14: Guided multi-step asset creation

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screen** | `CreateAssetPage` |

**User Story:**  
As a **user**, I want step-by-step asset creation with catalog pickers (category → subcategory → make → model).

**Acceptance Criteria:** Pickers load from asset-service master APIs; form validates required fields.

---

#### APP-15: Complete creation with documents

| Field | Detail |
|-------|--------|
| **Priority** | P0 |

**User Story:**  
As a **user**, I want to attach warranty dates, invoice, and photo in one submission.

**Acceptance Criteria:** `AssetRemoteDataSource.createAssetComplete(FormData)` → `POST /assets/complete`.

---

#### APP-16: Asset detail and document download

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screen** | `AssetDetailPage` |

**User Story:**  
As a **user**, I want to view asset details and open/download attached documents.

**Acceptance Criteria:** `KeeplyDocumentsApi` download; open with platform file handler.

---

#### APP-17: Update and delete assets

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Component** | `AssetBloc` |

**User Story:**  
As a **user**, I want to correct or remove asset records from the app.

---

#### APP-18: Search assets

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **user**, I want to search my assets by name or serial.

**Acceptance Criteria:** `GET /assets/search` with query parameter.

---

### Feature 5.5 — Scanning

---

#### APP-19: Camera barcode scan

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Screen** | `AssetScanPage` |
| **Package** | `mobile_scanner` |

**User Story:**  
As a **user**, I want to scan barcodes with my device camera to find products.

---

#### APP-20: Manual barcode entry

| Field | Detail |
|-------|--------|
| **Priority** | P2 |

**User Story:**  
As a **user**, I want to type a barcode when camera scan is unavailable.

---

#### APP-21: OpenFoodFacts fallback

| Field | Detail |
|-------|--------|
| **Priority** | P2 |

**User Story:**  
As a **user**, I want unknown grocery/product barcodes looked up externally for prefilled name/brand.

**Acceptance Criteria:** When asset-service `/scan` returns no match, query OpenFoodFacts API.

---

### Feature 5.6 — Alerts & Notifications Inbox

---

#### APP-22: Alerts hub with filters

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Screen** | `AlertsHubPage` |

**User Story:**  
As a **user**, I want to view notifications in All, Unread, and Read tabs.

**Acceptance Criteria:** `KeeplyNotificationsApi.list`; pull-to-refresh.

---

#### APP-23: Mark notifications read

| Field | Detail |
|-------|--------|
| **Priority** | P0 |

**User Story:**  
As a **user**, I want to mark individual or all notifications as read.

**Acceptance Criteria:** Read, toggle, read-all APIs; badge count updates.

---

### Feature 5.7 — Helpdesk & Support

---

#### APP-24: Helpdesk hub

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Screen** | `HelpdeskHubPage` |

**User Story:**  
As a **user**, I want access to FAQs, knowledge search, my issues, and new ticket creation.

**Acceptance Criteria:** Integrates `KeeplyHelpdeskApi` for issues, FAQs, knowledge, queries.

---

#### APP-25: Create support ticket from app

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **user**, I want to raise a ticket linked to an asset when something breaks.

**Acceptance Criteria:** Optional `assetId` in issue payload; appears in `my-issues`.

---

#### APP-26: Ollama AI support chat

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **Screen** | `SupportChatPage` |

**User Story:**  
As a **user**, I want AI-assisted answers using local Ollama when configured.

**Acceptance Criteria:** Calls `AppConfig.ollamaBaseUrl`; graceful fallback if Ollama unavailable.

---

#### APP-27: WhatsApp click-to-chat support

| Field | Detail |
|-------|--------|
| **Priority** | P3 |

**User Story:**  
As a **user**, I want optional escalation to human support via WhatsApp.

**Acceptance Criteria:** `WHATSAPP_SUPPORT_E164` dart-define opens WhatsApp deep link.

---

### Feature 5.8 — Tips, AI & Master Data

---

#### APP-28: Tips hub with category-based guidance

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **Screen** | `TipsHubPage` |

**User Story:**  
As a **user**, I want maintenance tips relevant to my asset categories.

**Acceptance Criteria:** May combine static tips with Ollama-generated content from user categories.

---

#### APP-29: Voice catalog assist

| Field | Detail |
|-------|--------|
| **Priority** | P3 |
| **Screen** | `VoiceCatalogAssistPage` |

**User Story:**  
As a **user**, I want voice input to navigate catalog selection.

---

#### APP-30: Master data catalog browser

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **Screen** | `MasterDataCatalogPage` |

**User Story:**  
As a **user**, I want read-only browse of categories, subcategories, makes, and models.

---

#### APP-31: Offline awareness

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **user**, I want clear feedback when the network is unavailable.

**Acceptance Criteria:** `AssetBloc` connectivity checks; connectivity banner widgets displayed.

---

### Feature 5.9 — Settings & Configuration

---

#### APP-32: Theme and accessibility preferences

| Field | Detail |
|-------|--------|
| **Priority** | P2 |
| **Screen** | `AccountSettingsPage` |

**User Story:**  
As a **user**, I want light/dark theme, compact UI, and reduce motion options.

**Acceptance Criteria:** Preferences persisted locally; applied on app restart.

---

#### APP-33: Environment-specific service URLs

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Component** | `AppConfig` |

**User Story:**  
As a **developer**, I want to point the app at local, staging, or production backends.

**Acceptance Criteria:**
- Android emulator: `10.0.2.2`
- Physical device: `--dart-define=KEEPLY_DEV_HOST=<ip>`
- Per-service: `ASSET_SERVICE_URL`, `AUTH_SERVICE_URL`, etc.

---

#### APP-34: Cross-feature data refresh

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Component** | `AppDataRefreshCubit` |

**User Story:**  
As a **user**, I want dashboard and shell to refresh automatically after I add or edit an asset.

---

## Epic 6 — Cross-Service Integration Journeys

| Attribute | Value |
|-----------|-------|
| **Epic ID** | EPIC-6 |
| **Business Value** | End-to-end user journeys spanning multiple services |

### Feature 6.1 — Unified Authentication

---

#### X-01: Single sign-on across four backends

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Services** | auth, asset, notification, helpdesk |

**User Story:**  
As a **Flutter user**, I want one login to access all microservice APIs.

**Acceptance Criteria:**
- Single JWT from auth-service.
- `keeplyApiUrl(KeeplyApiService, path)` routes to correct host:port per service.
- All services validate token via shared `JwtVerifier`.

---

#### X-02: Mobile dev networking

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **developer testing on device**, I want the Flutter app to reach locally running Docker services.

**Acceptance Criteria:** Documented dart-define and host resolution for emulator vs physical device.

---

### Feature 6.2 — Registration & OTP Journey

---

#### X-03: End-to-end registration with OTP

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Flow** | Flutter → auth → notification → Flutter |

**User Story:**  
As a **new user**, I want OTP delivered during registration and login.

**Acceptance Criteria:**
1. User submits register/login on Flutter.
2. auth-service generates OTP, calls `SafeNotificationHelper`.
3. notification-service resolves OTP template, writes to SMS/email/in-app log.
4. User enters OTP; auth validates against `otp_log`.

---

### Feature 6.3 — Asset Lifecycle Alert Journey

---

#### X-04: Warranty expiry dashboard to in-app alert

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Flow** | asset → notification → Flutter |

**User Story:**  
As an **asset owner**, I want warranty expiry surfaced on dashboard and in alerts inbox.

**Acceptance Criteria:**
1. asset-service `need-your-attention` returns expiring items.
2. Scheduled or event-driven notification enqueued with warranty template.
3. Flutter dashboard shows reminder cards; Alerts tab lists in-app notification.

---

### Feature 6.4 — Support Journey

---

#### X-05: Self-service to ticket escalation

| Field | Detail |
|-------|--------|
| **Priority** | P1 |
| **Flow** | Flutter → helpdesk (FAQ/chatbot → issue) |

**User Story:**  
As a **user with a broken appliance**, I want to try FAQ/chatbot first, then open a ticket linked to my asset.

**Acceptance Criteria:**
1. User searches FAQ in `HelpdeskHubPage`.
2. Optional chatbot session via helpdesk or Ollama.
3. Issue created with `assetId`; visible in `my-issues` and dashboard count.

---

### Feature 6.5 — Privacy & Compliance Journey

---

#### X-06: Opt-out honored end-to-end

| Field | Detail |
|-------|--------|
| **Priority** | P0 |
| **Flow** | Flutter → auth → notification |

**User Story:**  
As a **user**, I want communication preferences set in the app respected by all notification sends.

**Acceptance Criteria:**
1. User disables SMS in auth communication preferences.
2. Subsequent OTP or alert on SMS channel skipped by notification-service.
3. In-app channel still works unless `optOutInapp` is true.

---

#### X-07: Account deletion stops future contact

| Field | Detail |
|-------|--------|
| **Priority** | P1 |

**User Story:**  
As a **user who deleted their account**, I want no further notifications or login.

**Acceptance Criteria:** Soft delete disables auth; opt-out implicit; services reject requests for deleted user ID.

---

#### X-08: Encrypted PII across services

| Field | Detail |
|-------|--------|
| **Priority** | P0 |

**User Story:**  
As a **compliance officer**, I want consistent encryption for PII in auth, asset links, helpdesk, and in-app notifications.

**Acceptance Criteria:** All listed entities use `JpaAttributeEncryptor` from common-service with shared key management.

---

## 12. Implementation Roadmap

### Phase 1 — Foundation (MVP)

| Deliverable | Stories | Exit Criteria |
|-------------|---------|---------------|
| Shared library integrated | CS-01–CS-08, CS-10, CS-18 | All services build with common-service |
| Core auth | AUTH-01, 05–07, 12–15, 24 | Register, login, OTP, profile, JWT refresh |
| Basic asset CRUD | ASSET-01, 07–09, 15–16, 18–19 | Create/list/edit assets with documents |
| Flutter shell + auth | APP-01–08, 14–15 | User can register, login, add one asset |

### Phase 2 — Core Mobile Experience

| Deliverable | Stories | Exit Criteria |
|-------------|---------|---------------|
| Complete asset flow | ASSET-08, 12–14 | Multipart complete creation + warranty |
| Dashboard | APP-09–11, 16 | Dashboard with reminders and browse |
| User linking | ASSET-18–19 | Assigned assets only on dashboard |

### Phase 3 — Notifications

| Deliverable | Stories | Exit Criteria |
|-------------|---------|---------------|
| In-app center | NOTIF-01–08 | Inbox, count, read state |
| Opt-out | NOTIF-04, AUTH-17, X-06 | Preferences enforced |
| Lifecycle alerts | ASSET-34, X-04 | Warranty reminders in app |

### Phase 4 — Helpdesk & Support

| Deliverable | Stories | Exit Criteria |
|-------------|---------|---------------|
| Tickets & FAQ | HELP-01–05, 10, 19–21 | Create/view issues; FAQ search |
| SLA | HELP-14–18 | Auto-escalation job running |
| Flutter helpdesk | APP-24–25 | Helpdesk hub in app |

### Phase 5 — Advanced Asset Capabilities

| Deliverable | Stories | Exit Criteria |
|-------------|---------|---------------|
| Scanning | ASSET-22–24, APP-19–21 | Camera scan flow |
| Compliance | ASSET-27–29 | Rules and violations API |
| OCR/AI | ASSET-30–32 | OCR scan endpoint operational |
| Bulk admin | ASSET-02, 11, 17 | Excel import documented and tested |

### Phase 6 — Hardening & External Integrations

| Deliverable | Stories | Exit Criteria |
|-------------|---------|---------------|
| Real SMS/email dispatch | NOTIF-10 | Twilio/SMTP integrated |
| Push notifications | NOTIF-11 | FCM channel live |
| Helpdesk notifications | HELP-22 | Ticket events notify user |
| Admin client gap | CS-16 | Admin list endpoint in auth-service |

---

## 13. Traceability Matrix

| Business Goal | Epic | Key Stories |
|---------------|------|-------------|
| Secure identity | E1 | AUTH-01, 06–07, 12–14, 17 |
| Don't miss warranty | E3, E5, E6 | ASSET-12–14, APP-10, X-04 |
| Centralized documents | E3, E5 | ASSET-15–16, APP-15–16 |
| Regulatory compliance | E0, E1, E6 | CS-04–06, AUTH-17–19, X-06–08 |
| Self-service support | E4, E5 | HELP-10, 19, APP-24–25, X-05 |
| Unified mobile UX | E5, E6 | APP-01–13, X-01 |
| Reliable notifications | E0, E2 | CS-10–11, NOTIF-01–07 |

### Story Count Summary

| Epic | Features | User Stories |
|------|----------|--------------|
| E0 — common-service | 6 | 18 |
| E1 — auth-service | 6 | 25 |
| E2 — notification-service | 4 | 11 |
| E3 — asset-service | 10 | 34 |
| E4 — helpdesk-service | 7 | 22 |
| E5 — keeply_flutter_app | 9 | 34 |
| E6 — Cross-cutting | 5 | 8 |
| **Total** | **47** | **152** |

---

## 14. Known Gaps & Future Backlog

| ID | Gap | Recommended Story | Priority |
|----|-----|-------------------|----------|
| GAP-01 | Notification delivery is log-only (no Twilio/SMTP/FCM) | NOTIF-10 | P2 |
| GAP-02 | No PUSH channel in notification-service | NOTIF-11 | P3 |
| GAP-03 | Helpdesk does not send ticket notifications | HELP-22 | P2 |
| GAP-04 | `AdminClient` endpoint may be missing in auth-service | CS-16 | P2 |
| GAP-05 | Flutter category Excel path may not match backend | Verify `/categories/bulk/excel` | P3 |
| GAP-06 | Technical doc lists MySQL; runtime uses PostgreSQL | Update Technical Functionality Documentation ports/schemas | P3 |
| GAP-07 | helpdesk-service README references MySQL | Align README with PostgreSQL/Supabase | P3 |
| GAP-08 | LLM/Ollama in Flutter is client-side only | Document boundary; optional server-side LLM epic | P3 |
| GAP-09 | Web app (keeply_react_app) not in this backlog | Create separate EPIC for React parity | P3 |
| GAP-10 | API Gateway not implemented | Future epic for unified gateway/routing | P3 |

---

## Appendix A — API Base Paths Quick Reference

| Service | Base Path | Swagger UI |
|---------|-----------|------------|
| auth-service | `/api/auth`, `/api/users`, `/api/admin` | `/swagger-ui.html` |
| notification-service | `/api/notifications` | `/swagger-ui.html` |
| helpdesk-service | `/api/helpdesk` | `/swagger-ui.html` |
| asset-service | `/api/asset/v1` | `/swagger-ui.html` |

## Appendix B — Key Entity Reference

| Service | Core Entities |
|---------|---------------|
| auth-service | User, UserDetailMaster, Session, RefreshToken, Credential, OtpLog, Role, ProjectType, TermsAndConditions, AuditLog |
| notification-service | *TemplateMaster (×4), SmsLog, WhatsappLog, NotificationLog, InappLog |
| asset-service | AssetMaster, AssetWarranty, AssetAmc, AssetDocument, AssetUserLink, ProductCategory*, VendorMaster, ComplianceRule, ComplianceViolation |
| helpdesk-service | Issue, IssueMaster, IssueEscalation, EscalationMatrix, SLATracking, Query, FAQ, ServiceKnowledge, ChatbotSession |

## Appendix C — Flutter Feature Module Map

| Module | Path | Backend Service |
|--------|------|-----------------|
| Auth | `lib/features/auth/` | auth-service :7071 |
| Asset | `lib/features/asset/` | asset-service :7075 |
| Home/Shell | `lib/features/home/`, `shell/` | asset, notification, helpdesk |
| Notification | `lib/features/notification/` | notification-service :7072 |
| Helpdesk | `lib/features/helpdesk/` | helpdesk-service :7074 |
| Master Data | `lib/features/master_data/` | asset-service :7075 |

---

**Document Owner:** Product / Engineering  
**Review Cycle:** Quarterly or per major release  
**Next Review:** October 2026
