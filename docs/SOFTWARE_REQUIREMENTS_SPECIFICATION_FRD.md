# Software Requirements Specification (SRS) & Functional Requirements Document (FRD)
## Keeply — Complete Asset Lifecycle Management Platform

| Field | Value |
|-------|-------|
| **Document Title** | SRS / FRD (Combined) |
| **Product Name** | Keeply |
| **Program** | Complete Asset Lifecycle Management |
| **Version** | 1.0.0 |
| **Status** | Approved for Planning |
| **Last Updated** | July 2026 |
| **Document Owner** | Product & Engineering |
| **Classification** | Internal |

**Companion Documents:**
- [BUSINESS_REQUIREMENTS_DOCUMENT.md](./BUSINESS_REQUIREMENTS_DOCUMENT.md) — Full BRD
- [EPICS_FEATURES_USER_STORIES.md](./EPICS_FEATURES_USER_STORIES.md) — Detailed backlog (152 user stories)
- [TECHNICAL_FUNCTIONAL_DOCUMENT.md](./TECHNICAL_FUNCTIONAL_DOCUMENT.md) — Technical specification

---

## Table of Contents

**Part A — Introduction & System Overview**
1. [Document Purpose](#part-a--introduction)
2. [System Overview](#2-system-overview)
3. [Architecture Summary](#3-architecture-summary)

**Part B — Business Requirements (BRD Summary)**
4. [Business Objectives & KPIs](#4-business-objectives--kpis)
5. [Scope](#5-scope)
6. [Personas](#6-personas)
7. [Business Requirements Register](#7-business-requirements-register)

**Part C — Functional Requirements (FRD)**
8. [common-service Functional Requirements](#8-common-service-functional-requirements)
9. [auth-service Functional Requirements](#9-auth-service-functional-requirements)
10. [notification-service Functional Requirements](#10-notification-service-functional-requirements)
11. [asset-service Functional Requirements](#11-asset-service-functional-requirements)
12. [helpdesk-service Functional Requirements](#12-helpdesk-service-functional-requirements)
13. [keeply_flutter_app Functional Requirements](#13-keeply_flutter_app-functional-requirements)

**Part D — System & Non-Functional Requirements**
14. [Interface Requirements](#14-interface-requirements)
15. [Data Requirements](#15-data-requirements)
16. [Security Requirements](#16-security-requirements)
17. [Non-Functional Requirements](#17-non-functional-requirements)
18. [Compliance Requirements](#18-compliance-requirements)

**Part E — Traceability & Delivery**
19. [Requirements Traceability Matrix](#19-requirements-traceability-matrix)
20. [User Story Index by Epic](#20-user-story-index-by-epic)
21. [Implementation Phases](#21-implementation-phases)
22. [Glossary](#22-glossary)

---

# Part A — Introduction

## 1. Document Purpose

This combined **SRS/FRD** serves as the single reference for:

| Audience | Use |
|----------|-----|
| **Business stakeholders** | Objectives, scope, KPIs, business rules |
| **Business analysts** | Numbered BR-xxx and FR-xxx requirements |
| **Developers & architects** | Service boundaries, APIs, data schemas |
| **QA** | Testable requirements and acceptance traceability |
| **Compliance** | Privacy, opt-out, audit obligations |

**Document hierarchy:**

```
BRD (business WHY)  →  SRS/FRD (this document)  →  Epics/User Stories (delivery HOW)
     BR-xxx                    FR-xxx + NFR-xxx              AUTH-01, ASSET-07, APP-14
```

---

## 2. System Overview

### 2.1 Product Description

**Keeply** is a microservices-based asset lifecycle management platform. Users register physical assets, attach warranty and purchase documents, receive expiry reminders, and access support — primarily through the **keeply_flutter_app** mobile client.

### 2.2 System Components

| Component | Type | Port | Schema | Role |
|-----------|------|------|--------|------|
| common-service | Shared library | — | — | JWT, encryption, Feign, API standards |
| auth-service | Microservice | 7071 | authdb | Identity, sessions, profiles, admin |
| notification-service | Microservice | 7072 | notificationdb | Templates, in-app inbox, channel logs |
| helpdesk-service | Microservice | 7074 | helpdeskdb | Tickets, FAQ, SLA, chatbot |
| asset-service | Microservice | 7075 | assetdb | Assets, warranty, documents, compliance |
| keeply_flutter_app | Mobile client | — | Local | Consumer UX (Flutter, BLoC, Dio) |

### 2.3 Technology Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17, Spring Boot 3.4.x, JPA/Hibernate |
| Database | PostgreSQL (Supabase-compatible) |
| Security | Spring Security, JWT (RS256/HS256), AES-256-GCM PII encryption |
| Integration | REST, OpenFeign |
| Mobile | Flutter 3+, BLoC, Dio, FlutterSecureStorage |
| API Docs | OpenAPI 3 / Swagger UI per service |

---

## 3. Architecture Summary

```
┌─────────────────────────────────────────────────────────────┐
│  keeply_flutter_app  │  keeply_react_app  │  Integrations   │
└──────────────────────────────┬──────────────────────────────┘
                               │ HTTPS + JWT
       ┌───────────────────────┼───────────────────────┐
       ▼                       ▼                       ▼
 auth-service          asset-service          notification-service
  :7071                   :7075                    :7072
       │                       │                       │
       └───────────────────────┼───────────────────────┘
                               ▼
                      helpdesk-service :7074
                               │
                      common-service (shared)
                               ▼
              PostgreSQL: authdb | assetdb | notificationdb | helpdeskdb
```

### 3.1 Key Integration Flows

| Flow | Path |
|------|------|
| Login | Flutter → auth-service → JWT stored |
| Add asset | Flutter → asset-service `/assets/complete` |
| OTP | auth-service → SafeNotificationHelper → notification-service |
| Opt-out check | notification-service → auth-service preferences |
| Support ticket | Flutter → helpdesk-service (JWT user as reporter) |
| Warranty alert | asset-service → notification-service → Flutter inbox |

---

# Part B — Business Requirements

## 4. Business Objectives & KPIs

### 4.1 Objectives

| ID | Objective |
|----|-----------|
| BO-01 | Increase warranty utilization before expiry |
| BO-02 | Centralize asset documentation (invoices, AMC, photos) |
| BO-03 | Build trust through privacy-by-design (DPDPA/PDPA) |
| BO-04 | Reduce support cost via self-service |
| BO-05 | Enable platform extensibility (ECOM, ASSET project types) |
| BO-06 | Deliver mobile-first adoption |

### 4.2 Key Performance Indicators

| KPI | Target (12 mo) |
|-----|----------------|
| KPI-01 Monthly active users | 10,000+ |
| KPI-02 Assets per active user | ≥ 3 |
| KPI-03 Complete creation (warranty + invoice) | ≥ 70% |
| KPI-04 Reminder engagement (7-day view) | ≥ 40% |
| KPI-05 Self-service resolution (no ticket) | ≥ 50% |
| KPI-06 Ticket SLA compliance | ≥ 90% |
| KPI-07 Opt-out compliance | 100% |
| KPI-08 Unauthorized access rate | < 0.1% |
| KPI-09 D30 retention | ≥ 25% |

---

## 5. Scope

### 5.1 In Scope

Identity & access, asset lifecycle, documents, catalog master data, notifications (in-app + templates), helpdesk, Flutter mobile app, PII encryption, communication opt-out, shared platform library.

### 5.2 Out of Scope (v1)

Web app (keeply_react_app), payments/checkout, manufacturer RMA APIs, enterprise SSO, live SMS/email/push providers (queued first), IoT monitoring.

---

## 6. Personas

| Persona | Need |
|---------|------|
| **Priya (Homeowner)** | Warranty reminders, invoice storage, mobile simplicity |
| **Rajesh (Small Business)** | Bulk import, assignment, compliance reports |
| **Anita (Support Agent)** | Ticket queues, SLA, escalation |
| **Vikram (Admin)** | User management, audit, catalog |
| **Meera (Compliance)** | Encryption, opt-out, audit export |

---

## 7. Business Requirements Register

| ID | Priority | Requirement |
|----|----------|-------------|
| BR-101 | Must | User registration with T&C acceptance |
| BR-102 | Must | Login via password and OTP |
| BR-103 | Should | Login via MPIN, RSA, WebAuthn, auth code |
| BR-104 | Must | JWT access + refresh tokens |
| BR-105 | Must | Logout and session revocation |
| BR-106 | Must | Profile view/update with photo |
| BR-107 | Must | Contact change with OTP |
| BR-108 | Must | Soft-delete account |
| BR-109 | Must | Admin role and admin registration |
| BR-110 | Must | Admin block/unblock/permanent-block with audit |
| BR-111 | Must | Exportable audit logs |
| BR-201 | Must | Asset registration with classification |
| BR-202 | Must | Single-flow asset + warranty + invoice + assignment |
| BR-203 | Must | Search, update, delete assigned assets |
| BR-204 | Must | Category → SubCategory → Make → Model hierarchy |
| BR-205 | Must | Warranty and AMC with dates |
| BR-206 | Must | Document upload and download |
| BR-207 | Must | User–asset assignment |
| BR-208 | Must | "Needs attention" for expiring coverage |
| BR-301 | Must | Template-based notifications |
| BR-302 | Must | Multi-channel templates (in-app MVP) |
| BR-303 | Must | In-app inbox with read/unread |
| BR-305 | Must | Honor communication opt-out |
| BR-401 | Must | Create and track support tickets |
| BR-407 | Must | FAQ search and browse |
| BR-411 | Must | SLA per service/priority/level |
| BR-501 | Must | Mobile registration and login |
| BR-502 | Must | Dashboard with reminders and badges |
| BR-503 | Must | Guided asset creation in app |
| BR-601 | Must | Consistent API response envelope |
| BR-602 | Must | JWT validation on all protected APIs |
| BR-603 | Must | PII encrypted at rest |

*Full BR-101 through BR-607: see [BUSINESS_REQUIREMENTS_DOCUMENT.md](./BUSINESS_REQUIREMENTS_DOCUMENT.md)*

---

# Part C — Functional Requirements (FRD)

Functional requirements are numbered **FR-xxx** by service. Each maps to user stories in [EPICS_FEATURES_USER_STORIES.md](./EPICS_FEATURES_USER_STORIES.md).

---

## 8. common-service Functional Requirements

| ID | Requirement | Acceptance Summary | Stories |
|----|-------------|-------------------|---------|
| FR-CS-01 | Shared JWT verification | All services validate tokens consistently | CS-01 |
| FR-CS-02 | Feign Bearer propagation | Outbound calls include JWT | CS-02 |
| FR-CS-03 | JPA PII encryption | AES-256-GCM on annotated fields | CS-04 |
| FR-CS-04 | HMAC lookup hashes | Uniqueness without plaintext | CS-05 |
| FR-CS-05 | ResponseWrapper envelope | `{success, message, data}` | CS-07 |
| FR-CS-06 | Global exception handler | Structured HTTP errors | CS-08 |
| FR-CS-07 | NotificationHelper | Single-call notification send | CS-10 |
| FR-CS-08 | SafeNotificationHelper | 3 retries + failure log | CS-11 |
| FR-CS-09 | FileStorageUtil | `uploads/{entityType}/` storage | CS-13 |
| FR-CS-10 | Feign clients | Notification, Admin, AssetUserLink | CS-16–18 |
| FR-CS-11 | BaseEntity audit fields | created/updated by/at, active | CS-12 |
| FR-CS-12 | CORS configuration | Shared KeeplyCorsConfiguration | CS-15 |

---

## 9. auth-service Functional Requirements

**Base path:** `/api/auth`, `/api/users`, `/api/admin`  
**Port:** 7071 | **Schema:** authdb

| ID | Requirement | API / Behavior | Stories |
|----|-------------|----------------|---------|
| FR-AUTH-01 | User registration | `POST /api/auth/register` | AUTH-01 |
| FR-AUTH-02 | Multipart registration + photo | Multipart register | AUTH-02 |
| FR-AUTH-03 | Admin registration | `POST /api/auth/adminregister` | AUTH-03 |
| FR-AUTH-04 | Password login | `POST /api/auth/login` PASSWORD | AUTH-06 |
| FR-AUTH-05 | OTP login | OTP send + login OTP | AUTH-07, 24 |
| FR-AUTH-06 | MPIN lifecycle | register/verify/reset | AUTH-08, 28 |
| FR-AUTH-07 | RSA login | challenge/verify | AUTH-09 |
| FR-AUTH-08 | WebAuthn login | challenge/verify | AUTH-10 |
| FR-AUTH-09 | Token refresh | `POST /api/auth/refresh` | AUTH-13 |
| FR-AUTH-10 | Logout | `POST /api/auth/logout` | AUTH-14 |
| FR-AUTH-11 | Profile CRUD | `GET/PUT /api/auth/profile/me` | AUTH-15–16 |
| FR-AUTH-12 | Communication preferences | opt-out flags GET/PUT | AUTH-17 |
| FR-AUTH-13 | Contact change OTP | request/confirm | AUTH-16 |
| FR-AUTH-14 | Soft delete | `DELETE /api/users/me` | AUTH-18 |
| FR-AUTH-15 | Self decrypt PII | `/me/details-decrypted` | AUTH-19 |
| FR-AUTH-16 | Admin user list | `GET /api/admin/users` | AUTH-20–21 |
| FR-AUTH-17 | Block/unblock | admin block APIs | AUTH-22 |
| FR-AUTH-18 | Audit export | CSV/Excel logs | AUTH-23 |
| FR-AUTH-19 | Project types CRUD | `/api/auth/v1/project-types` | AUTH-04 |
| FR-AUTH-20 | Terms & Conditions | versioned T&C | AUTH-05 |
| FR-AUTH-21 | Account lockout | failed attempt policy | AUTH-25 |

**Key entities:** User, UserDetailMaster, Session, RefreshToken, Credential, OtpLog, Role, ProjectType, TermsAndConditions, AuditLog

---

## 10. notification-service Functional Requirements

**Base path:** `/api/notifications`  
**Port:** 7072 | **Schema:** notificationdb

| ID | Requirement | API / Behavior | Stories |
|----|-------------|----------------|---------|
| FR-NOTIF-01 | Send notification | `POST /api/notifications` → 202 | NOTIF-01 |
| FR-NOTIF-02 | Template resolution | Per-channel template masters | NOTIF-02 |
| FR-NOTIF-03 | Placeholder rendering | `{{key}}` substitution | NOTIF-03 |
| FR-NOTIF-04 | Opt-out enforcement | Fetch auth preferences; skip channel | NOTIF-04, 06 |
| FR-NOTIF-05 | In-app list | `GET /list` — 30 days, max 100 | NOTIF-05 |
| FR-NOTIF-06 | Unread count | `GET /count` | NOTIF-06 |
| FR-NOTIF-07 | Read/unread management | read, toggle, read-all, unread | NOTIF-07 |
| FR-NOTIF-08 | Channel logging | sms/whatsapp/email/inapp logs | NOTIF-09 |
| FR-NOTIF-09 | Seeded templates | OTP, warranty, assign, bulk upload | NOTIF-04–05 |
| FR-NOTIF-10 | In-app encryption | JpaAttributeEncryptor on InappLog | NOTIF-08 |

**Channels:** SMS, WHATSAPP, EMAIL/NOTIFICATION, INAPP  
**Note:** External provider dispatch is future (FR-NOTIF-11); current model logs/queues messages.

---

## 11. asset-service Functional Requirements

**Base path:** `/api/asset/v1`  
**Port:** 7075 | **Schema:** assetdb

| ID | Requirement | API / Behavior | Stories |
|----|-------------|----------------|---------|
| FR-ASSET-01 | Asset CRUD | `/assets` | ASSET-07–09 |
| FR-ASSET-02 | Complete creation | `POST /assets/complete` multipart | ASSET-08 |
| FR-ASSET-03 | Catalog hierarchy | categories, subcategories, makes, models | ASSET-01 |
| FR-ASSET-04 | Bulk/Excel import | bulk JSON and Excel endpoints | ASSET-02, 11 |
| FR-ASSET-05 | Warranty management | `/warranty` + documents | ASSET-12 |
| FR-ASSET-06 | AMC management | `/amc` + documents | ASSET-13 |
| FR-ASSET-07 | Needs attention | `/userlinks/need-your-attention` | ASSET-14 |
| FR-ASSET-08 | Document upload/download | `/documents`, `/files` | ASSET-15–16 |
| FR-ASSET-09 | User linking | `/userlinks`, `/user-asset-links` | ASSET-18–20 |
| FR-ASSET-10 | Barcode/QR scan | `/scan` | ASSET-22–24 |
| FR-ASSET-11 | Components | `/components` | ASSET-25 |
| FR-ASSET-12 | Vendors & outlets | `/vendors`, `/outlets` | ASSET-04 |
| FR-ASSET-13 | Compliance engine | `/compliance`, `/compliance/rules` | ASSET-27–29 |
| FR-ASSET-14 | OCR extraction | `/products/ocr` | ASSET-30 |
| FR-ASSET-15 | Intelligent/LLM extraction | `/intelligent-extraction`, `/llm-extraction` | ASSET-31–32 |
| FR-ASSET-16 | Audit trail | `/audit` agent APIs | ASSET-33 |
| FR-ASSET-17 | Lifecycle notifications | SafeNotificationHelper on events | ASSET-34 |
| FR-ASSET-18 | Master data agent | `/masters` | ASSET-06 |
| FR-ASSET-19 | Favourite/ordering | favourite, sequence on assets/categories | ASSET-10, 03 |

**Hub entity:** AssetMaster (links warranty, AMC, documents, userLinks, components, purchaseInfo)

---

## 12. helpdesk-service Functional Requirements

**Base path:** `/api/helpdesk`  
**Port:** 7074 | **Schema:** helpdeskdb

| ID | Requirement | API / Behavior | Stories |
|----|-------------|----------------|---------|
| FR-HELP-01 | Create issue | `POST /issues` | HELP-01 |
| FR-HELP-02 | Issue from master | issueMasterId + asset links | HELP-02 |
| FR-HELP-03 | My issues | `GET /issues/my-issues` | HELP-03 |
| FR-HELP-04 | Agent workflow | assign, status, resolve, close | HELP-04 |
| FR-HELP-05 | Filter issues | by status, service | HELP-05 |
| FR-HELP-06 | Issue master CRUD | `/issue-master` | HELP-06–07 |
| FR-HELP-07 | User queries | submit, answer, close | HELP-07–09 |
| FR-HELP-08 | FAQ management | CRUD, search, helpful vote | HELP-10–12 |
| FR-HELP-09 | Service knowledge | `/knowledge` CRUD + search | HELP-13 |
| FR-HELP-10 | Escalation matrix | `/escalation-matrix` | HELP-14 |
| FR-HELP-11 | SLA tracking | `/sla` — breaches, first response | HELP-15–17 |
| FR-HELP-12 | Auto-escalation | 60s scheduled job | HELP-15 |
| FR-HELP-13 | Manual escalation | `/escalations/issue/{id}` | HELP-18 |
| FR-HELP-14 | Chatbot | `/chatbot/message`, session history | HELP-19–20 |
| FR-HELP-15 | JWT protection | All endpoints except swagger/health | HELP-21 |

**Statuses:** OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED  
**Support levels:** L1, L2, L3

---

## 13. keeply_flutter_app Functional Requirements

| ID | Requirement | Screen / Component | Stories |
|----|-------------|-------------------|---------|
| FR-APP-01 | Splash auth routing | SplashPage | APP-01 |
| FR-APP-02 | 5-tab shell | KeeplyMobileShell | APP-02 |
| FR-APP-03 | Welcome & register | WelcomePage, RegisterPage | APP-04 |
| FR-APP-04 | Login password/OTP | LoginPage | APP-05 |
| FR-APP-05 | Token auto-refresh | AuthInterceptor | APP-06 |
| FR-APP-06 | Dashboard | DashboardPage — assets, reminders, badges | APP-09–13 |
| FR-APP-07 | Browse by room | BrowseRoomsPage | APP-11 |
| FR-APP-08 | Asset creation wizard | CreateAssetPage → `/assets/complete` | APP-14–15 |
| FR-APP-09 | Asset detail + docs | AssetDetailPage | APP-16 |
| FR-APP-10 | Asset CRUD/search | AssetBloc | APP-17–18 |
| FR-APP-11 | Barcode scan | AssetScanPage, OpenFoodFacts fallback | APP-19–21 |
| FR-APP-12 | Alerts inbox | AlertsHubPage | APP-22–23 |
| FR-APP-13 | Helpdesk hub | HelpdeskHubPage | APP-24–25 |
| FR-APP-14 | Account hub | AccountHubPage, profile, settings | APP-03, 07, 10 |
| FR-APP-15 | Per-service API URLs | AppConfig, keeplyApiUrl() | APP-33 |
| FR-APP-16 | Connectivity awareness | AssetBloc, banners | APP-31 |
| FR-APP-17 | Cross-tab refresh | AppDataRefreshCubit | APP-34 |
| FR-APP-18 | Ollama AI assist | TipsHubPage, SupportChatPage | APP-26, 28 |

**HTTP stack:** Dio + AuthInterceptor + ResponseWrapper parsing

---

# Part D — System & Non-Functional Requirements

## 14. Interface Requirements

### 14.1 External Interfaces

| Interface | Protocol | Consumer |
|-----------|----------|----------|
| Mobile app → microservices | HTTPS REST JSON | keeply_flutter_app |
| Service → service | HTTPS REST + Feign | auth, asset, notification, helpdesk |
| Future SMS/email/push | Provider APIs | notification-service |

### 14.2 API Standards

| Requirement | Specification |
|-------------|---------------|
| Authentication | `Authorization: Bearer <JWT>` |
| Success response | `{ "success": true, "message": "...", "data": {} }` |
| Error response | `{ "success": false, "message": "...", "data": null }` |
| Documentation | Swagger UI at `/swagger-ui.html` per service |
| Versioning | asset-service: `/api/asset/v1`; auth: `/api/auth` |

### 14.3 Service Endpoint Summary

| Service | Port | Primary Paths |
|---------|------|---------------|
| auth-service | 7071 | `/api/auth/*`, `/api/users/*`, `/api/admin/*` |
| notification-service | 7072 | `/api/notifications/*` |
| helpdesk-service | 7074 | `/api/helpdesk/*` |
| asset-service | 7075 | `/api/asset/v1/*` |

---

## 15. Data Requirements

### 15.1 Database Schemas

| Schema | Service | Core Tables |
|--------|---------|-------------|
| authdb | auth-service | users, user_detail_master, sessions, refresh_tokens, credentials, otp_log, audit_log |
| assetdb | asset-service | asset_master, asset_warranty, asset_amc, asset_document, asset_user_link, product_category* |
| notificationdb | notification-service | *_template_master, sms_log, inapp_log, notification_log |
| helpdeskdb | helpdesk-service | issue, issue_master, faq, query, escalation_matrix, sla_tracking |

### 15.2 Data Retention (Minimum)

| Data Type | Retention |
|-----------|-----------|
| Auth audit logs | 12–24 months |
| Asset audit logs | 12 months |
| Notification logs | 6 months |
| Soft-deleted user | Audit retained per policy |
| Ticket history | 12 months |

### 15.3 PII Fields Requiring Encryption

User email/mobile/username (auth), assignee fields (asset links), reporter/assignee (helpdesk), in-app notification body (notification).

---

## 16. Security Requirements

| ID | Requirement |
|----|-------------|
| SEC-01 | TLS for all API traffic in production |
| SEC-02 | AES-256-GCM encryption for PII at rest |
| SEC-03 | BCrypt for passwords |
| SEC-04 | JWT RS256 in production (HS256 dev fallback) |
| SEC-05 | RBAC: ROLE_ADMIN for admin endpoints |
| SEC-06 | Account lockout after failed logins |
| SEC-07 | Audit all admin decrypt and block actions |
| SEC-08 | Tokens in FlutterSecureStorage only |
| SEC-09 | Field-level decrypt (not bulk exposure) |
| SEC-10 | CORS restricted to configured origins |

---

## 17. Non-Functional Requirements

| ID | Category | Requirement | Target |
|----|----------|-------------|--------|
| NFR-01 | Availability | Core API uptime | 99.5% / month |
| NFR-02 | Performance | Dashboard API p95 | < 2s |
| NFR-03 | Performance | Complete asset creation p95 | < 10s |
| NFR-04 | Scalability | Registered users (Y1) | 50,000 |
| NFR-05 | Usability | First asset in 5 min | Usability benchmark |
| NFR-06 | Maintainability | Flyway migrations | All services |
| NFR-07 | Observability | Health checks | `/actuator/health` |
| NFR-08 | Portability | PostgreSQL 14+ Supabase | Production |
| NFR-09 | Mobile | Android + iOS | Flutter 3+ |
| NFR-10 | Reliability | Notification retry | 3 attempts + log |

---

## 18. Compliance Requirements

| ID | Regulation Principle | Implementation |
|----|---------------------|----------------|
| COMP-01 | Lawful basis | T&C at registration |
| COMP-02 | Data minimization | Required fields only |
| COMP-03 | Purpose limitation | Scoped decrypt APIs |
| COMP-04 | Storage limitation | Soft delete + retention policy |
| COMP-05 | Right to access | Self-service profile/decrypt |
| COMP-06 | Right to erasure | Soft delete (hard delete process separate) |
| COMP-07 | Restrict processing | Permanent block |
| COMP-08 | Communication consent | Per-channel opt-out (BUS-R14–17) |
| COMP-09 | Security safeguards | Encryption, RBAC, audit |
| COMP-10 | Accountability | Exportable audit logs |

---

# Part E — Traceability & Delivery

## 19. Requirements Traceability Matrix

| Business Req | Functional Req | Service | User Story |
|--------------|----------------|---------|------------|
| BR-101 | FR-AUTH-01 | auth | AUTH-01 |
| BR-102 | FR-AUTH-04, 05 | auth | AUTH-06, 07 |
| BR-202 | FR-ASSET-02 | asset | ASSET-08 |
| BR-208 | FR-ASSET-07 | asset | ASSET-14 |
| BR-305 | FR-NOTIF-04 | notification | NOTIF-04 |
| BR-401 | FR-HELP-01 | helpdesk | HELP-01 |
| BR-501 | FR-APP-03, 04 | flutter | APP-04, 05 |
| BR-503 | FR-APP-08 | flutter | APP-14, 15 |
| BR-601 | FR-CS-05 | common | CS-07 |
| BR-603 | FR-CS-03 | common | CS-04 |

---

## 20. User Story Index by Epic

| Epic | ID | Stories | Focus |
|------|-----|---------|-------|
| E0 Platform | EPIC-0 | CS-01 – CS-18 | common-service |
| E1 Identity | EPIC-1 | AUTH-01 – AUTH-28 | auth-service |
| E2 Notifications | EPIC-2 | NOTIF-01 – NOTIF-14 | notification-service |
| E3 Assets | EPIC-3 | ASSET-01 – ASSET-36 | asset-service |
| E4 Helpdesk | EPIC-4 | HELP-01 – HELP-26 | helpdesk-service |
| E5 Mobile | EPIC-5 | APP-01 – APP-34 | keeply_flutter_app |
| E6 Integration | EPIC-6 | X-01 – X-10 | Cross-service |

**Total:** 47 features, 152 user stories — full detail in [EPICS_FEATURES_USER_STORIES.md](./EPICS_FEATURES_USER_STORIES.md)

---

## 21. Implementation Phases

| Phase | Business Outcome | Key FR IDs |
|-------|------------------|------------|
| 1 Foundation | Register + first asset | FR-AUTH-01–05, FR-ASSET-01–02, FR-APP-03–08 |
| 2 Daily Value | Dashboard reminders | FR-ASSET-07, FR-APP-06–07 |
| 3 Stay Informed | In-app alerts + opt-out | FR-NOTIF-01–07, FR-AUTH-12 |
| 4 Support | Tickets + SLA | FR-HELP-01–14, FR-APP-13 |
| 5 Power | Scan, OCR, compliance | FR-ASSET-10–15 |
| 6 Market Ready | SMS/push providers | FR-NOTIF-11, HELP-22 |

---

## 22. Glossary

| Term | Definition |
|------|------------|
| AMC | Annual Maintenance Contract |
| BRD | Business Requirements Document |
| FRD | Functional Requirements Document (this document Part C) |
| JWT | JSON Web Token for API authentication |
| Keeply | Consumer brand for the platform |
| MVP | Minimum Viable Product |
| PII | Personally Identifiable Information |
| SLA | Service Level Agreement for support tickets |
| SRS | Software Requirements Specification |
| T&C | Terms and Conditions |

---

## Document Approval

| Role | Name | Date |
|------|------|------|
| Product Owner | | |
| Engineering Lead | | |
| QA Lead | | |
| Compliance / Legal | | |

**Revision History**

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | July 2026 | Initial combined SRS/FRD |

---

*End of Document*
