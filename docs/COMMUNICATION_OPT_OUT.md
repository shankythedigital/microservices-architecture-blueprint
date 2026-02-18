# Communication Opt-Out (SMS, Email, WhatsApp, In-App, Push)

Users and admins can opt out of receiving communications per channel. Opt-out preferences are stored in **auth-service** and exposed via profile APIs.

## Channels

| Channel   | Flag (DB)       | Meaning when `true`     |
|----------|-----------------|-------------------------|
| SMS      | `opt_out_sms`   | User has opted out of SMS |
| Email    | `opt_out_email` | User has opted out of email |
| WhatsApp | `opt_out_whatsapp` | User has opted out of WhatsApp |
| In-App   | `opt_out_inapp` | User has opted out of in-app notifications |
| Push     | `opt_out_push`  | User has opted out of push notifications |

Default for all is **`false`** (user receives notifications). Setting to **`true`** means the user has opted out of that channel.

## Who Can Update

- **Normal user:** Can update only their own opt-out preferences (via profile update for their own profile).
- **Admin:** Can update any user’s opt-out preferences (via profile update for any `userId`).

Same access rules as profile update: `PUT /api/auth/profile/me` (self) or `PUT /api/auth/profile/{userId}` (admin only for other users).

## API

### Get preferences

- **GET** `/api/auth/profile/me` or **GET** `/api/auth/profile/{userId}`  
  Full profile; response includes: `optOutSms`, `optOutEmail`, `optOutWhatsapp`, `optOutInapp`, `optOutPush` (boolean).
- **GET** `/api/auth/profile/me/communication-preferences` or **GET** `/api/auth/profile/{userId}/communication-preferences`  
  Returns only the five opt-out flags (lightweight; use when sending notifications).

### Update preferences

- **PUT** `/api/auth/profile/me` or **PUT** `/api/auth/profile/{userId}`  
  Body (JSON or form-data) can include:
  - `optOutSms` (boolean)
  - `optOutEmail` (boolean)
  - `optOutWhatsapp` (boolean)
  - `optOutInapp` (boolean)
  - `optOutPush` (boolean)

Only include fields you want to change. Omitted fields are left unchanged.

**Example (opt out of SMS and WhatsApp):**

```json
{
  "optOutSms": true,
  "optOutWhatsapp": true
}
```

## Respecting Opt-Out When Sending (notification-service)

**Notification-service** respects opt-out when all of the following are true:

- Request includes **userId** and the **Authorization** header (Bearer token).
- Config **notification.opt-out-check.enabled** is `true` (default).
- Auth-service is reachable at **auth.service.url** and returns preferences for that user.

If any of these are missing, the notification is sent as before (no behaviour change). When the check runs and the user has opted out of the channel (SMS, Email, WhatsApp, In-App), the notification is **not** persisted and the request still returns **202 Accepted**.

Callers (e.g. asset-service, auth-service) should send **POST /api/notifications** with the same **Bearer token** and **userId** in the body so that notification-service can call auth-service to fetch preferences. No change is required in callers beyond ensuring the token and userId are present.

## Database (auth-service)

Table: `user_detail_master`  
Columns: `opt_out_sms`, `opt_out_email`, `opt_out_whatsapp`, `opt_out_inapp`, `opt_out_push` (BOOLEAN, default FALSE).  
Migration: `V5__add_communication_opt_out.sql`.
