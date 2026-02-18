# Block, Unblock & Permanent Block — Security, Compliance & PDPA/DPDPA

This document describes how **block**, **unblock**, and **permanent block** are implemented and how they align with **security and compliance rules** and **PDPA/DPDPA** (Digital Personal Data Protection Act) principles.

---

## 1. Definitions

| Action | Meaning | When to use |
|--------|--------|-------------|
| **Block (temporary)** | User cannot log in or use the system for a limited time. Can be reversed by **Unblock**. | Security incident, policy violation, compliance remediation, manual review. |
| **Unblock** | Restore access for a previously blocked user. | After review, expiry of temporary block, or corrective action. |
| **Permanent block** | User access is permanently revoked. Reversal typically requires a separate process (e.g. data protection request, legal). | Serious policy/legal violation, user request to restrict processing (PDPA), or final decision after compliance. |

---

## 2. Security & Compliance Rules

- **Block/unblock decisions** should be driven by policy and, where applicable, by the same **compliance rule** framework used for assets (e.g. `ComplianceRule`, `blocks_operation`, severity).
- **Who can block/unblock**: Only roles with admin/security privileges (e.g. `ROLE_ADMIN`) should call block/unblock/permanent-block APIs.
- **Audit**: Every block, unblock, and permanent block must be **audited** (who, when, reason, target user) for security and compliance.
- **Reasons**: Store a **reason code or free-text reason** and, for temporary block, **blocked_until** (optional). This supports compliance reviews and PDPA lawful basis.

---

## 3. PDPA/DPDPA Alignment

- **Lawful basis**: Blocking/restricting access is a **legitimate interest** (security, compliance) or **legal obligation**. Document the reason for each action.
- **Purpose limitation**: Use block/unblock only for access control and security/compliance; do not use stored reasons for unrelated purposes.
- **Data minimization**: Store only necessary fields: block type, reason, timestamps, and who performed the action.
- **Right to restrict processing**: A **permanent block** can implement the data subject’s request to restrict processing (no further access to the system); ensure process is documented and audited.
- **Audit trail**: Retain an audit log of block/unblock/permanent-block events (with masked PII where appropriate) for accountability and regulatory response.

---

## 4. Implementation in This Codebase

### 4.1 Data model (auth-service)

- **`users.enabled`**: `false` = account must not be allowed to log in (covers both “blocked” and “permanent block” at login).
- **`user_detail_master.account_locked`**: Typically used for **temporary** lock (e.g. after failed attempts); when set by admin, treat as temporary block until unblock.
- **Block metadata** (for audit and PDPA):
  - `block_type`: `NONE` | `TEMPORARY` | `PERMANENT`
  - `block_reason`: reason code or short description (e.g. security, compliance, user request)
  - `blocked_at`, `blocked_by`: timestamp and actor
  - `blocked_until`: optional; for temporary block, after this time the system may auto-unblock or require manual unblock depending on policy.

These can live on `user_detail_master` or a small `user_block_history` table; the design below uses columns on `user_detail_master` for simplicity.

### 4.2 Login enforcement

Before issuing tokens or creating a session:

1. **`user.getEnabled() == false`** → reject login (e.g. “Account disabled”).
2. **`udm.getAccountLocked() == true`** → reject login (e.g. “Account temporarily locked”).
3. **`block_type == PERMANENT`** → reject login (e.g. “Account permanently blocked”).

Use clear, non-leaking error messages (e.g. generic “Account is not allowed to access the system”) while logging the actual reason server-side for audit.

### 4.3 Admin APIs (auth-service)

- **POST** `/api/admin/users/{userId}/block`  
  - Body: `{ "reason": "...", "blockedUntil": "ISO8601" }`  
  - Sets `enabled = false` or uses a dedicated “blocked” state, sets `account_locked = true` for temporary, sets `block_type = TEMPORARY`, stores reason and `blocked_until`, and audits.

- **POST** `/api/admin/users/{userId}/unblock`  
  - Clears block: `account_locked = false`, `block_type = NONE`, clears reason and `blocked_until`, sets `enabled = true`, and audits. **Permanently blocked users can be unblocked only by an administrator** (ROLE_ADMIN); a dedicated error message is returned if a non-admin attempts to unblock a permanently blocked account.

- **POST** `/api/admin/users/{userId}/permanent-block`  
  - Body: `{ "reason": "..." }`  
  - Sets `enabled = false`, `block_type = PERMANENT`, stores reason and audit fields; does not set `blocked_until`. Only reversible via a separate, documented process (e.g. data protection request). Audit the action.

All three must:
- Require admin (or equivalent) role.
- Write to the audit log (who, when, target userId, action, reason).

### 4.4 Compliance rules (asset-service) — optional link

- You can define **USER** as an entity type in the compliance engine and add rules that “recommend” or mandate block (e.g. “User must be blocked if critical violation not resolved”).
- The actual **enforcement** of “user cannot log in” remains in **auth-service** (using `enabled`, `account_locked`, and `block_type` as above). Asset-service compliance can call auth-service admin APIs or set a flag that auth-service reads, depending on your architecture.

---

## 5. Summary

- **Block** = temporary no-access; store reason and optional `blocked_until`; enforce at login; allow **Unblock** to restore access.
- **Unblock** = clear temporary block and restore access; audit.
- **Permanent block** = revoke access indefinitely with reason and audit; reversal via a separate, compliant process.
- **Security & compliance**: Admin-only APIs, audit every action, optional link to compliance rules.
- **PDPA/DPDPA**: Lawful basis, purpose limitation, data minimization, right to restrict processing, and full audit trail.

Implementing the data model, login checks, and admin APIs as above gives you block/unblock/permanent-block behavior that is consistent with security, compliance, and PDPA/DPDPA principles.
