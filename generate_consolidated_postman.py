#!/usr/bin/env python3
"""
Generate consolidated Postman collection and environment for all microservices.
Includes: asset-service, auth-service, notification-service, helpdesk-service
With Swagger/OpenAPI examples in request bodies.
"""

import json
import uuid
from datetime import datetime

# Base URLs (localhost for local dev)
BASE_URLS = {
    "auth": "http://localhost:8080",
    "asset": "http://localhost:7072",
    "notification": "http://localhost:7071",
    "helpdesk": "http://localhost:7074",
}

def create_env_value(key, value, desc="", secret=False):
    return {
        "key": key,
        "value": value,
        "type": "secret" if secret else "default",
        "description": desc,
        "enabled": True
    }

def create_consolidated_environment():
    """Create consolidated environment with variables for all services."""
    values = [
        # Service base URLs
        create_env_value("authbaseUrl", BASE_URLS["auth"], "Auth Service base URL"),
        create_env_value("assetbaseUrl", BASE_URLS["asset"], "Asset Service base URL"),
        create_env_value("notificationbaseUrl", BASE_URLS["notification"], "Notification Service base URL"),
        create_env_value("helpdeskbaseUrl", BASE_URLS["helpdesk"], "Helpdesk Service base URL"),
        # Auth tokens (shared)
        create_env_value("bearerToken", "", "JWT Bearer token - get from auth login", secret=True),
        create_env_value("accessToken", "", "JWT access token", secret=True),
        create_env_value("refreshToken", "", "Refresh token", secret=True),
        # User context
        create_env_value("userId", "1", "Current user ID"),
        create_env_value("username", "john.doe", "Username"),
        create_env_value("password", "SecurePass123!", "Password", secret=True),
        create_env_value("email", "john.doe@example.com", "Email"),
        create_env_value("mobile", "+919876543210", "Mobile with country code"),
        create_env_value("projectType", "ASSET_SERVICE", "Project type"),
        # Asset service IDs
        create_env_value("assetId", "1", "Asset ID"),
        create_env_value("categoryId", "1", "Category ID"),
        create_env_value("subCategoryId", "1", "SubCategory ID"),
        create_env_value("makeId", "1", "Make ID"),
        create_env_value("modelId", "1", "Model ID"),
        create_env_value("vendorId", "1", "Vendor ID"),
        create_env_value("outletId", "1", "Outlet ID"),
        create_env_value("componentId", "1", "Component ID"),
        create_env_value("warrantyId", "1", "Warranty ID"),
        create_env_value("amcId", "1", "AMC ID"),
        create_env_value("documentId", "1", "Document ID"),
        create_env_value("docType", "pdf", "Document type: pdf, doc, docx, txt, jpg, png, etc."),
        create_env_value("document", "SGVsbG8gV29ybGQ=", "Base64 of document file (e.g. base64 -i file.pdf | tr -d '\\n')"),
        create_env_value("targetUserId", "2", "Target user ID for linking"),
        create_env_value("targetUsername", "user1", "Target username"),
        create_env_value("entityType", "ASSET", "Entity type"),
        create_env_value("entityId", "1", "Entity ID"),
        create_env_value("scanValue", "AST-LAP-001", "QR/Barcode scan value"),
        # Helpdesk IDs
        create_env_value("issueId", "1", "Issue ID"),
        create_env_value("queryId", "1", "Query ID"),
        create_env_value("faqId", "1", "FAQ ID"),
        create_env_value("knowledgeId", "1", "Knowledge ID"),
        create_env_value("escalationMatrixId", "1", "Escalation Matrix ID"),
        create_env_value("sessionId", "session-123", "Chatbot session ID"),
        create_env_value("notificationId", "1", "Notification ID"),
        create_env_value("violationId", "1", "Compliance violation ID"),
        create_env_value("ruleId", "1", "Compliance rule ID"),
        # Notification
        create_env_value("channel", "SMS", "Notification channel"),
        create_env_value("templateCode", "OTP_SMS", "Template code"),
        create_env_value("recipient", "+919876543210", "Recipient"),
        create_env_value("otp", "123456", "OTP code"),
    ]
    
    return {
        "id": str(uuid.uuid4()),
        "name": "Microservices - Consolidated Environment",
        "values": values,
        "_postman_variable_scope": "environment",
        "_postman_exported_at": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S.000Z"),
        "_postman_exported_using": "Postman/11.84.5"
    }

def with_document_raw_body(request_json):
    """Build raw JSON body for with-document endpoints: request, document (base64 of file), docType."""
    return {"mode": "raw", "raw": json.dumps({
        "request": request_json,
        "document": "{{document}}",
        "docType": "{{docType}}"
    }, indent=2), "options": {"raw": {"language": "json"}}}

def create_request(name, method, base_var, path, description="", body=None, params=None, headers=None, no_auth=False, path_var_defaults=None):
    """Create a Postman request item. path_var_defaults: dict e.g. {'code': 'ASSET_SERVICE'}."""
    default_headers = [
        {"key": "Content-Type", "value": "application/json", "type": "text"}
    ]
    if not no_auth:
        default_headers.insert(0, {"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"})
    
    hdrs = headers if headers else default_headers
    
    url_path = path.strip("/").split("/")
    path_parts = []
    path_vars = []
    path_var_defaults = path_var_defaults or {}
    
    for part in url_path:
        if part.startswith("{"):
            var_name = part[1:-1]
            path_parts.append(":" + var_name)
            default_val = path_var_defaults.get(var_name, "1")
            path_vars.append({"key": var_name, "value": str(default_val), "description": var_name})
        else:
            path_parts.append(part)
    
    raw_url = "{{" + base_var + "}}/" + "/".join(path_parts)
    
    req = {
        "name": name,
        "request": {
            "method": method,
            "header": hdrs,
            "url": {
                "raw": raw_url,
                "host": ["{{" + base_var + "}}"],
                "path": path_parts
            },
            "description": description
        },
        "response": []
    }
    
    if path_vars:
        req["request"]["url"]["variable"] = path_vars
    if body:
        req["request"]["body"] = body
    if params:
        req["request"]["url"]["query"] = params
    
    return req

def build_auth_modules():
    """Build Auth Service as list of module folders."""
    return [
        {"name": "1. Registration", "item": [
            create_request("Register User", "POST", "authbaseUrl", "api/auth/register",
                "Register new user. Requires acceptTc, countryCode. Optional: pincode, city, state, country, address1-3",
                {"mode": "raw", "raw": json.dumps({
                    "username": "{{username}}",
                    "password": "{{password}}",
                    "email": "{{email}}",
                    "mobile": "{{mobile}}",
                    "countryCode": "+91",
                    "projectType": "{{projectType}}",
                    "acceptTc": True,
                    "pincode": "{{pincode}}",
                    "city": "{{city}}",
                    "state": "{{state}}",
                    "country": "{{country}}",
                    "address1": "{{address1}}",
                    "address2": "{{address2}}",
                    "address3": "{{address3}}"
                }, indent=2), "options": {"raw": {"language": "json"}}},
                no_auth=True),
            create_request("Register Admin", "POST", "authbaseUrl", "api/auth/adminregister",
                "Register admin user. Same schema as RegisterRequest", no_auth=True,
                body={"mode": "raw", "raw": json.dumps({
                    "username": "admin", "password": "Admin123!", "email": "admin@example.com",
                    "mobile": "+1234567890", "projectType": "{{projectType}}"
                }, indent=2), "options": {"raw": {"language": "json"}}}),
        ]},
        {"name": "2. Login", "item": [
            create_request("Login (Password)", "POST", "authbaseUrl", "api/auth/login",
                "LoginRequest: loginType PASSWORD (required), username, password. Optional: deviceInfo",
                {"mode": "raw", "raw": json.dumps({
                    "loginType": "PASSWORD",
                    "username": "{{username}}",
                    "password": "{{password}}",
                    "deviceInfo": "web"
                }, indent=2), "options": {"raw": {"language": "json"}}},
                no_auth=True),
            create_request("Login (OTP)", "POST", "authbaseUrl", "api/auth/login",
                "LoginRequest: loginType OTP, username, otp (required)",
                {"mode": "raw", "raw": json.dumps({
                    "loginType": "OTP",
                    "username": "{{username}}",
                    "otp": "{{otp}}"
                }, indent=2), "options": {"raw": {"language": "json"}}},
                no_auth=True),
            create_request("Login (MPIN)", "POST", "authbaseUrl", "api/auth/login",
                "LoginRequest: loginType MPIN, userId, mpin, deviceInfo",
                {"mode": "raw", "raw": json.dumps({
                    "loginType": "MPIN",
                    "userId": "{{userId}}",
                    "mpin": "1234",
                    "deviceInfo": "web"
                }, indent=2), "options": {"raw": {"language": "json"}}},
                no_auth=True),
            create_request("Refresh Token", "POST", "authbaseUrl", "api/auth/refresh",
                "Refresh access token. Query: refreshToken (required)",
                params=[{"key": "refreshToken", "value": "{{refreshToken}}"}],
                no_auth=True),
        ]},
        {"name": "3. OTP", "item": [
            create_request("Send OTP", "POST", "authbaseUrl", "api/auth/otp/send",
                "Send OTP. Required: userId, channel (SMS|EMAIL|WHATSAPP), purpose (LOGIN|RESET_PASSWORD|CHANGE_MOBILE|CHANGE_EMAIL). Optional: mobile, email, templateCode, projectType, type",
                {"mode": "raw", "raw": json.dumps({
                    "userId": "{{userId}}",
                    "purpose": "LOGIN",
                    "channel": "SMS",
                    "mobile": "{{mobile}}",
                    "email": "{{email}}",
                    "projectType": "{{projectType}}",
                    "templateCode": "OTP_SMS"
                }, indent=2), "options": {"raw": {"language": "json"}}},
                no_auth=True),
        ]},
        {"name": "4. Profile", "item": [
            create_request("Get Current User Profile", "GET", "authbaseUrl", "api/users/me",
                "Get authenticated user profile - UserDto"),
            create_request("Get User by ID", "GET", "authbaseUrl", "api/users/{id}",
                "Get user by ID (admin or self)"),
            create_request("Get Profile (Extended)", "GET", "authbaseUrl", "api/auth/profile/me",
                "Extended profile with address"),
            create_request("Get Profile by UserId", "GET", "authbaseUrl", "api/auth/profile/{userId}",
                "Get profile by userId (admin)", path_var_defaults={"userId": "1"}),
            create_request("Update Profile (JSON)", "PUT", "authbaseUrl", "api/auth/profile/me",
                "Update own profile. UserProfileRequest: pincode, city, state, country, address1-3 (all optional)"),
            create_request("Update Profile with Document (profilePhoto)", "PUT", "authbaseUrl", "api/auth/profile/me",
                "Form-data: profilePhoto (document). Optional: pincode, city, state, country, address1-3. docType in env for reference.",
                body={"mode": "formdata", "formdata": [
                    {"key": "profilePhoto", "type": "file", "src": [], "description": "Profile photo document (IMAGE)"},
                    {"key": "pincode", "value": "{{pincode}}", "type": "text", "description": "Optional"},
                    {"key": "city", "value": "{{city}}", "type": "text", "description": "Optional"},
                    {"key": "state", "value": "{{state}}", "type": "text", "description": "Optional"},
                    {"key": "country", "value": "{{country}}", "type": "text", "description": "Optional"},
                    {"key": "address1", "value": "{{address1}}", "type": "text", "description": "Optional"}
                ]},
                headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
            create_request("Update Profile by UserId", "PUT", "authbaseUrl", "api/auth/profile/{userId}",
                "Update user profile (admin). Same body as Update Profile",
                path_var_defaults={"userId": "1"}),
            create_request("Get Communication Preferences", "GET", "authbaseUrl", "api/auth/profile/me/communication-preferences",
                "Get current user opt-out preferences"),
            create_request("Get Communication Preferences by UserId", "GET", "authbaseUrl", "api/auth/profile/{userId}/communication-preferences",
                "Get user communication preferences (admin)", path_var_defaults={"userId": "1"}),
        ]},
        {"name": "5. Password", "item": [
            create_request("Change Password", "POST", "authbaseUrl", "api/auth/password/change",
                "ChangePasswordRequest: userId, currentPassword, newPassword",
                {"mode": "raw", "raw": json.dumps({
                    "userId": 1, "currentPassword": "oldpass", "newPassword": "newpass"
                }, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Forgot Password", "POST", "authbaseUrl", "api/auth/password/forgot",
                "ForgotPasswordRequest: username, projectType",
                {"mode": "raw", "raw": json.dumps({
                    "username": "{{username}}", "projectType": "{{projectType}}"
                }, indent=2), "options": {"raw": {"language": "json"}}},
                no_auth=True),
        ]},
        {"name": "6. Credentials & MPIN", "item": [
            create_request("RSA Verify", "POST", "authbaseUrl", "api/auth/credential/rsa/verify",
                "RsaVerifyRequest: userId, challenge, signature (required)"),
            create_request("WebAuthn Verify", "POST", "authbaseUrl", "api/auth/credential/webauthn/verify",
                "CredentialChallengeResponse: userId, credentialId, signature"),
            create_request("Get RSA Challenge", "GET", "authbaseUrl", "api/auth/credential/rsa/challenge/{userId}",
                "Get RSA challenge for user", path_var_defaults={"userId": "1"}),
            create_request("Get WebAuthn Challenge", "GET", "authbaseUrl", "api/auth/credential/webauthn/challenge/{userId}",
                "Get WebAuthn challenge for user", path_var_defaults={"userId": "1"}),
            create_request("Register Credential", "POST", "authbaseUrl", "api/auth/credential/register",
                "CredentialRegisterRequest: userId, type, credentialId, publicKey"),
            create_request("MPIN Register", "POST", "authbaseUrl", "api/auth/mpin/register",
                "MpinRegisterRequest: userId, mpin, deviceInfo"),
            create_request("MPIN Verify", "POST", "authbaseUrl", "api/auth/mpin/verify",
                "MpinVerifyRequest: userId, mpin, deviceInfo"),
            create_request("MPIN Reset Request", "POST", "authbaseUrl", "api/auth/mpin/reset/request",
                "MpinResetRequest: userId, mobile, otp"),
            create_request("MPIN Reset Confirm", "POST", "authbaseUrl", "api/auth/mpin/reset/confirm",
                "MpinResetConfirmRequest: resetToken, newMpin, deviceInfo"),
            create_request("Contact Change Request", "POST", "authbaseUrl", "api/auth/contact/change/request",
                "EmailMobileChangeRequest: userId, oldValue, otp, type (EMAIL|MOBILE)"),
            create_request("Contact Change Confirm", "POST", "authbaseUrl", "api/auth/contact/change/confirm",
                "EmailMobileChangeConfirmRequest: resetToken, newValue, otp"),
        ]},
        {"name": "7. Admin", "item": [
            create_request("List All Users (Admin)", "GET", "authbaseUrl", "api/admin/users",
                "Admin only - list all users"),
            create_request("Block User (Admin)", "POST", "authbaseUrl", "api/admin/users/{userId}/block",
                "BlockUserRequest (optional): reason, blockedUntil",
                {"mode": "raw", "raw": json.dumps({"reason": "Policy violation", "blockedUntil": "2025-12-31"}, indent=2), "options": {"raw": {"language": "json"}}},
                path_var_defaults={"userId": "1"}),
            create_request("Unblock User (Admin)", "POST", "authbaseUrl", "api/admin/users/{userId}/unblock",
                "Unblock user", path_var_defaults={"userId": "1"}),
            create_request("Permanent Block (Admin)", "POST", "authbaseUrl", "api/admin/users/{userId}/permanent-block",
                "BlockUserRequest (optional): reason",
                {"mode": "raw", "raw": json.dumps({"reason": "Permanent ban"}, indent=2), "options": {"raw": {"language": "json"}}},
                path_var_defaults={"userId": "1"}),
            create_request("Get Audit Logs", "GET", "authbaseUrl", "api/admin/audit/logs",
                "Admin audit logs. Query: userId, action, url, method, from, to (all optional)",
                params=[
                    {"key": "userId", "value": "{{userId}}", "disabled": True},
                    {"key": "action", "value": "CREATE", "disabled": True},
                    {"key": "url", "value": "/api/asset", "disabled": True},
                    {"key": "method", "value": "POST", "disabled": True},
                    {"key": "from", "value": "2024-01-01", "disabled": True},
                    {"key": "to", "value": "2024-12-31", "disabled": True}
                ]),
            create_request("Get Audit Logs Paged", "GET", "authbaseUrl", "api/admin/audit/logs/paged",
                "Admin audit logs paginated. Query: page, size, sort (all optional)",
                params=[
                    {"key": "page", "value": "0"},
                    {"key": "size", "value": "20"},
                    {"key": "sort", "value": "createdAt,desc", "disabled": True}
                ]),
            create_request("Export Audit Logs CSV", "GET", "authbaseUrl", "api/admin/audit/logs/csv",
                "Export audit logs as CSV"),
            create_request("Export Audit Logs Excel", "GET", "authbaseUrl", "api/admin/audit/logs/excel",
                "Export audit logs as Excel"),
        ]},
        {"name": "8. Terms & Conditions", "item": [
            create_request("Get Terms and Conditions", "GET", "authbaseUrl", "api/auth/terms-and-conditions",
                "Query: projectType (optional), language (optional, default: en)", no_auth=True,
                params=[{"key": "projectType", "value": "{{projectType}}", "disabled": True}, {"key": "language", "value": "en", "disabled": True}]),
            create_request("Get T&C by Version", "GET", "authbaseUrl", "api/auth/terms-and-conditions/version",
                "Query: projectType (required), version (required)", no_auth=True,
                params=[{"key": "projectType", "value": "{{projectType}}"}, {"key": "version", "value": "1"}]),
            create_request("Get T&C Versions", "GET", "authbaseUrl", "api/auth/terms-and-conditions/versions",
                "Query: projectType (required)", no_auth=True,
                params=[{"key": "projectType", "value": "{{projectType}}"}]),
            create_request("Create T&C", "POST", "authbaseUrl", "api/auth/terms-and-conditions",
                "TermsAndConditions: version, title, content (required). Optional: projectType, summary, language"),
            create_request("Activate T&C", "PUT", "authbaseUrl", "api/auth/terms-and-conditions/{tcId}/activate",
                "Activate T&C version", path_var_defaults={"tcId": "1"}),
            create_request("Deactivate T&C", "PUT", "authbaseUrl", "api/auth/terms-and-conditions/{tcId}/deactivate",
                "Deactivate T&C version", path_var_defaults={"tcId": "1"}),
        ]},
        {"name": "9. Project Types", "item": [
            create_request("Get Project Types", "GET", "authbaseUrl", "api/auth/v1/project-types",
                "Get all active project types", no_auth=True),
            create_request("Get Project Type by Code", "GET", "authbaseUrl", "api/auth/v1/project-types/code/{code}",
                "Get project type by code", no_auth=True,
                path_var_defaults={"code": "ASSET_SERVICE"}),
            create_request("Get Project Type by ID", "GET", "authbaseUrl", "api/auth/v1/project-types/{projectTypeId}",
                "Get project type by ID", no_auth=True),
            create_request("Create Project Type", "POST", "authbaseUrl", "api/auth/v1/project-types",
                "Query: code, name (required); description, displayOrder (default: 0), createdBy (default: SYSTEM)", no_auth=True,
                params=[{"key": "code", "value": "NEW_PROJECT"}, {"key": "name", "value": "New Project"}, {"key": "description", "value": "", "disabled": True}, {"key": "displayOrder", "value": "0", "disabled": True}]),
            create_request("Update Project Type", "PUT", "authbaseUrl", "api/auth/v1/project-types/{projectTypeId}",
                "Query: name (required); description, displayOrder, updatedBy (optional)", no_auth=True,
                params=[{"key": "name", "value": "Updated Project"}], path_var_defaults={"projectTypeId": "1"}),
            create_request("Delete Project Type", "DELETE", "authbaseUrl", "api/auth/v1/project-types/{projectTypeId}",
                "Query: deletedBy (optional, default: SYSTEM)", no_auth=True,
                path_var_defaults={"projectTypeId": "1"}),
            create_request("Validate Project Type", "GET", "authbaseUrl", "api/auth/v1/project-types/validate/{code}",
                "Validate project type exists", no_auth=True,
                path_var_defaults={"code": "ASSET_SERVICE"}),
        ]},
    ]

def build_notification_modules():
    """Build Notification Service as list of module folders."""
    return [
        {"name": "1. Send", "item": [
            create_request("Send SMS Notification", "POST", "notificationbaseUrl", "api/notifications",
                "NotificationRequest: channel (required). Optional: username, mobile, email, subject, templateCode, placeholders/variables, userId, documentId, docType",
                {"mode": "raw", "raw": json.dumps({
                    "channel": "SMS",
                    "templateCode": "OTP_SMS",
                    "recipient": "{{recipient}}",
                    "mobile": "{{mobile}}",
                    "variables": {"otp": "123456", "expiry": "3", "documentId": "{{documentId}}", "docType": "{{docType}}"},
                    "userId": "{{userId}}",
                    "username": "{{username}}"
                }, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Send Email Notification", "POST", "notificationbaseUrl", "api/notifications",
                "Send Email. Swagger example",
                {"mode": "raw", "raw": json.dumps({
                    "channel": "EMAIL",
                    "templateCode": "WELCOME_EMAIL",
                    "recipient": "user@example.com",
                    "variables": {"name": "John Doe", "activationLink": "https://example.com/activate"}
                }, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Send WhatsApp Notification", "POST", "notificationbaseUrl", "api/notifications",
                "Send WhatsApp. Swagger example",
                {"mode": "raw", "raw": json.dumps({
                    "channel": "WHATSAPP",
                    "templateCode": "ORDER_CONFIRMATION",
                    "recipient": "+1234567890",
                    "variables": {"orderId": "ORD-12345", "amount": "99.99"}
                }, indent=2), "options": {"raw": {"language": "json"}}}),
            create_request("Send In-App Notification", "POST", "notificationbaseUrl", "api/notifications",
                "Send In-App. Swagger example",
                {"mode": "raw", "raw": json.dumps({
                    "channel": "INAPP",
                    "templateCode": "ASSIGNMENT_NOTIFICATION",
                    "recipient": "user123",
                    "variables": {"assetName": "Laptop-001", "assignedBy": "Admin"}
                }, indent=2), "options": {"raw": {"language": "json"}}}),
        ]},
        {"name": "2. List & Count", "item": [
            create_request("Get Notification List", "GET", "notificationbaseUrl", "api/notifications/list",
                "Get notifications for user. Query: days, userId (optional)",
                params=[
                    {"key": "days", "value": "7", "disabled": True},
                    {"key": "userId", "value": "{{userId}}", "disabled": True}
                ]),
            create_request("Get Notification List by UserId", "GET", "notificationbaseUrl", "api/notifications/list/{userId}",
                "Get notifications for specific user. Query: days (optional)",
                params=[{"key": "days", "value": "7", "disabled": True}],
                path_var_defaults={"userId": "1"}),
            create_request("Get Notification Count", "GET", "notificationbaseUrl", "api/notifications/count",
                "Get unread count for badge. Query: days, userId (optional)",
                params=[
                    {"key": "days", "value": "7", "disabled": True},
                    {"key": "userId", "value": "{{userId}}", "disabled": True}
                ]),
            create_request("Get Notification Count by UserId", "GET", "notificationbaseUrl", "api/notifications/count/{userId}",
                "Get count for user. Query: days (optional)",
                params=[{"key": "days", "value": "7", "disabled": True}],
                path_var_defaults={"userId": "1"}),
        ]},
        {"name": "3. Read Status", "item": [
            create_request("Mark Notification Read", "PUT", "notificationbaseUrl", "api/notifications/read/{notificationId}",
                "Mark single as read. Query: userId (optional)", path_var_defaults={"notificationId": "1"},
                params=[{"key": "userId", "value": "{{userId}}", "disabled": True}]),
            create_request("Mark All Read", "PUT", "notificationbaseUrl", "api/notifications/read-all",
                "Mark all read for current user. Query: days, userId (optional)",
                params=[{"key": "days", "value": "7", "disabled": True}, {"key": "userId", "value": "{{userId}}", "disabled": True}]),
            create_request("Mark All Read by UserId", "PUT", "notificationbaseUrl", "api/notifications/read-all/{userId}",
                "Mark all read for user. Query: days (optional)", path_var_defaults={"userId": "1"},
                params=[{"key": "days", "value": "7", "disabled": True}]),
            create_request("Toggle Read", "PATCH", "notificationbaseUrl", "api/notifications/toggle-read/{notificationId}",
                "Toggle read status. Query: userId (optional)", path_var_defaults={"notificationId": "1"}),
            create_request("Mark Unread", "PUT", "notificationbaseUrl", "api/notifications/unread/{notificationId}",
                "Mark as unread. Query: userId (optional)", path_var_defaults={"notificationId": "1"}),
            create_request("Mark All Unread", "PUT", "notificationbaseUrl", "api/notifications/unread-all",
                "Mark all unread. Query: days, userId (optional)",
                params=[{"key": "days", "value": "7", "disabled": True}, {"key": "userId", "value": "{{userId}}", "disabled": True}]),
            create_request("Mark All Unread by UserId", "PUT", "notificationbaseUrl", "api/notifications/unread-all/{userId}",
                "Mark all unread for user. Query: days (optional)", path_var_defaults={"userId": "1"},
                params=[{"key": "days", "value": "7", "disabled": True}]),
        ]},
    ]

def build_asset_modules():
    """Build Asset Service as list of module folders."""
    return [
        {"name": "1. Asset Scanning", "item": [
        create_request("Scan Asset (POST)", "POST", "assetbaseUrl", "api/asset/v1/scan",
            "Scan by QR/barcode. Swagger: AssetScanRequest - scanValue, scanType (QR|BARCODE|AUTO)",
            {"mode": "raw", "raw": json.dumps({
                "scanValue": "{{scanValue}}",
                "scanType": "AUTO"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Scan Asset (GET)", "GET", "assetbaseUrl", "api/asset/v1/scan",
            "Scan by query param. ?value=AST-LAP-001&type=AUTO",
            params=[{"key": "value", "value": "{{scanValue}}"}, {"key": "type", "value": "AUTO"}]),
        create_request("Scan and Save", "POST", "assetbaseUrl", "api/asset/v1/scan/save",
            "Scan and create/update with AI. Multipart: request (JSON), document (file, required), docType (required)",
            body={"mode": "formdata", "formdata": [
                {"key": "request", "value": json.dumps({"scanValue": "{{scanValue}}", "scanType": "AUTO", "userId": "{{userId}}", "username": "{{username}}"}, indent=2), "type": "text", "description": "AssetScanCreateRequest JSON"},
                {"key": "document", "type": "file", "src": [], "description": "Required: document file"},
                {"key": "docType", "value": "{{docType}}", "type": "text", "description": "Required: PDF, IMAGE, RECEIPT, etc."}
            ]},
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Scan QR from Image", "POST", "assetbaseUrl", "api/asset/v1/scan/qr/image",
            "Upload image (multipart) with QR/barcode. Form: file",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Universal QR Scan (POST)", "POST", "assetbaseUrl", "api/asset/v1/scan/qr",
            "Scan any QR - returns asset or master data entity. Body: AssetScanRequest",
            {"mode": "raw", "raw": json.dumps({"scanValue": "{{scanValue}}", "scanType": "AUTO"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Universal QR Scan (GET)", "GET", "assetbaseUrl", "api/asset/v1/scan/qr",
            "Scan any QR. Query: value, type",
            params=[{"key": "value", "value": "{{scanValue}}"}, {"key": "type", "value": "AUTO"}]),
        ]},
        {"name": "2. Assets", "item": [
        create_request("Create Asset", "POST", "assetbaseUrl", "api/asset/v1/assets",
            "JSON body. AssetRequest: userId, username, projectType, asset{...}. Optional: purchaseDate, purchasePrice, invoiceNumber, billNumber, assetStatus, sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "asset": {
                    "categoryId": 1,
                    "subCategoryId": 1,
                    "makeId": 1,
                    "modelId": 1,
                    "assetNameUdv": "AST-LAP-001",
                    "serialNumber": "SN123456",
                    "purchaseDate": "2024-01-15",
                    "purchasePrice": 999.99,
                    "invoiceNumber": "INV-001",
                    "billNumber": "BILL-001",
                    "assetStatus": "ACTIVE",
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Asset with Document", "POST", "assetbaseUrl", "api/asset/v1/assets/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "asset": {"categoryId": 1, "subCategoryId": 1, "makeId": 1, "modelId": 1, "assetNameUdv": "AST-LAP-001", "serialNumber": "SN123456", "assetStatus": "ACTIVE", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}})),
        create_request("Search Assets", "GET", "assetbaseUrl", "api/asset/v1/assets/search",
            "Query: keyword, page, size (all optional - page defaults to 0, size to 20)",
            params=[
                {"key": "keyword", "value": "laptop"},
                {"key": "page", "value": "0"},
                {"key": "size", "value": "20"}
            ]),
        create_request("Get Asset by ID", "GET", "assetbaseUrl", "api/asset/v1/assets/{id}"),
        create_request("Bulk Create Assets", "POST", "assetbaseUrl", "api/asset/v1/assets/bulk",
            "Bulk create assets",
            {"mode": "raw", "raw": json.dumps({
                "assets": [
                    {"assetNameUdv": "AST-001", "modelId": 1, "serialNumber": "SN001", "categoryId": 1, "subCategoryId": 1, "makeId": 1},
                    {"assetNameUdv": "AST-002", "modelId": 1, "serialNumber": "SN002", "categoryId": 1, "subCategoryId": 1, "makeId": 1}
                ]
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Upload Assets Excel", "POST", "assetbaseUrl", "api/asset/v1/assets/bulk/excel",
            "Multipart: file. Query: userId, username, projectType (optional)",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Complete Asset Creation", "POST", "assetbaseUrl", "api/asset/v1/assets/complete",
            "All-in-one: asset, warranty, document, user assignment. Required: userId, username, assetNameUdv, modelId, warrantyStartDate, warrantyEndDate, targetUserId, document (file), docType. Optional: assetStatus, warrantyTerms, targetUsername",
            body={"mode": "formdata", "formdata": [
                {"key": "userId", "value": "{{userId}}", "type": "text"},
                {"key": "username", "value": "{{username}}", "type": "text"},
                {"key": "projectType", "value": "{{projectType}}", "type": "text"},
                {"key": "assetNameUdv", "value": "AST-001", "type": "text"},
                {"key": "modelId", "value": "1", "type": "text"},
                {"key": "serialNumber", "value": "SN001", "type": "text"},
                {"key": "categoryId", "value": "1", "type": "text"},
                {"key": "subCategoryId", "value": "1", "type": "text"},
                {"key": "makeId", "value": "1", "type": "text"},
                {"key": "assetStatus", "value": "ACTIVE", "type": "text", "description": "Optional"},
                {"key": "warrantyStartDate", "value": "2024-01-01", "type": "text"},
                {"key": "warrantyEndDate", "value": "2025-01-01", "type": "text"},
                {"key": "warrantyProvider", "value": "Dell", "type": "text"},
                {"key": "warrantyStatus", "value": "ACTIVE", "type": "text"},
                {"key": "warrantyTerms", "value": "Standard warranty terms", "type": "text", "description": "Optional"},
                {"key": "targetUserId", "value": "{{targetUserId}}", "type": "text"},
                {"key": "targetUsername", "value": "{{targetUsername}}", "type": "text"},
                {"key": "document", "type": "file", "src": [], "description": "Required: document file"},
                {"key": "docType", "value": "{{docType}}", "type": "text", "description": "Required: PDF, IMAGE, RECEIPT, etc."}
            ]},
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Update Asset", "PUT", "assetbaseUrl", "api/asset/v1/assets/{id}",
            "AssetRequest: userId, username, projectType, asset{...}. Optional: purchaseDate, purchasePrice, invoiceNumber, billNumber, assetStatus, sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "asset": {
                    "categoryId": 1,
                    "subCategoryId": 1,
                    "makeId": 1,
                    "modelId": 1,
                    "assetNameUdv": "AST-LAP-001-Updated",
                    "serialNumber": "SN123456",
                    "purchaseDate": "2024-01-15",
                    "purchasePrice": 999.99,
                    "invoiceNumber": "INV-001",
                    "billNumber": "BILL-001",
                    "assetStatus": "ACTIVE",
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update Asset with Document", "PUT", "assetbaseUrl", "api/asset/v1/assets/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "asset": {"categoryId": 1, "subCategoryId": 1, "makeId": 1, "modelId": 1, "assetNameUdv": "AST-LAP-001-Updated", "serialNumber": "SN123456", "assetStatus": "ACTIVE", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}}),
            path_var_defaults={"id": "1"}),
        create_request("Delete Asset", "DELETE", "assetbaseUrl", "api/asset/v1/assets/{id}",
            "AssetRequest body required: userId, username, projectType",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Asset Favourite", "PUT", "assetbaseUrl", "api/asset/v1/assets/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Asset Most Like", "PUT", "assetbaseUrl", "api/asset/v1/assets/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Asset Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/assets/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "3. Master Data - Categories", "item": [
        create_request("List Categories", "GET", "assetbaseUrl", "api/asset/v1/categories",
            "List all categories"),
        create_request("Create Category", "POST", "assetbaseUrl", "api/asset/v1/categories",
            "JSON body. CategoryRequest: userId, username, projectType, category{categoryName}. Optional: description, sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "category": {
                    "categoryName": "Electronics",
                    "description": "Electronic devices",
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Category with Document", "POST", "assetbaseUrl", "api/asset/v1/categories/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "category": {"categoryName": "Electronics", "description": "Electronic devices", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}})),
        create_request("Get Category by ID", "GET", "assetbaseUrl", "api/asset/v1/categories/{id}"),
        create_request("Update Category", "PUT", "assetbaseUrl", "api/asset/v1/categories/{id}",
            "CategoryRequest: userId, username, projectType, category{...}",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}",
                "category": {"categoryName": "Electronics Updated", "description": "Updated", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update Category with Document", "PUT", "assetbaseUrl", "api/asset/v1/categories/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "category": {"categoryName": "Electronics Updated", "description": "Updated", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}}),
            path_var_defaults={"id": "1"}),
        create_request("Delete Category", "DELETE", "assetbaseUrl", "api/asset/v1/categories/{id}",
            "CategoryRequest body: userId, username, projectType",
            {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Create Categories", "POST", "assetbaseUrl", "api/asset/v1/categories/bulk",
            "BulkCategoryRequest: categories array"),
        create_request("Bulk Upload Categories Excel", "POST", "assetbaseUrl", "api/asset/v1/categories/bulk/excel",
            "Multipart: file. Query: userId, username, projectType",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Category Favourite", "PUT", "assetbaseUrl", "api/asset/v1/categories/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Category Most Like", "PUT", "assetbaseUrl", "api/asset/v1/categories/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Category Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/categories/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "4. Master Data - SubCategories", "item": [
        create_request("List SubCategories", "GET", "assetbaseUrl", "api/asset/v1/subcategories"),
        create_request("Create SubCategory", "POST", "assetbaseUrl", "api/asset/v1/subcategories",
            "JSON body. SubCategoryRequest: userId, username, projectType, subCategory{subCategoryName, category{categoryId}}. Optional: description, sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "subCategory": {
                    "subCategoryName": "Laptops",
                    "category": {"categoryId": 1},
                    "description": "Laptop computers",
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create SubCategory with Document", "POST", "assetbaseUrl", "api/asset/v1/subcategories/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "subCategory": {"subCategoryName": "Laptops", "category": {"categoryId": 1}, "description": "Laptop computers", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}})),
        create_request("Get SubCategory by ID", "GET", "assetbaseUrl", "api/asset/v1/subcategories/{id}"),
        create_request("Update SubCategory", "PUT", "assetbaseUrl", "api/asset/v1/subcategories/{id}",
            "SubCategoryRequest body", {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}",
                "subCategory": {"subCategoryName": "Laptops Updated", "category": {"categoryId": 1}, "description": "Updated", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update SubCategory with Document", "PUT", "assetbaseUrl", "api/asset/v1/subcategories/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "subCategory": {"subCategoryName": "Laptops Updated", "category": {"categoryId": 1}, "description": "Updated", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}}),
            path_var_defaults={"id": "1"}),
        create_request("Delete SubCategory", "DELETE", "assetbaseUrl", "api/asset/v1/subcategories/{id}",
            "SubCategoryRequest body", {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Create SubCategories", "POST", "assetbaseUrl", "api/asset/v1/subcategories/bulk",
            "BulkSubCategoryRequest"),
        create_request("Bulk Upload SubCategories Excel", "POST", "assetbaseUrl", "api/asset/v1/subcategories/bulk/excel",
            "Multipart: file. Query: userId, username, projectType",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("SubCategory Favourite", "PUT", "assetbaseUrl", "api/asset/v1/subcategories/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("SubCategory Most Like", "PUT", "assetbaseUrl", "api/asset/v1/subcategories/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("SubCategory Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/subcategories/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "5. Master Data - Makes", "item": [
        create_request("List Makes", "GET", "assetbaseUrl", "api/asset/v1/makes"),
        create_request("Create Make", "POST", "assetbaseUrl", "api/asset/v1/makes",
            "JSON body. MakeRequest: userId, username, projectType, make{makeName, subCategory{subCategoryId}}. Optional: sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "make": {
                    "makeName": "Dell",
                    "subCategory": {"subCategoryId": 1},
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Make with Document", "POST", "assetbaseUrl", "api/asset/v1/makes/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "make": {"makeName": "Dell", "subCategory": {"subCategoryId": 1}, "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}})),
        create_request("Get Make by ID", "GET", "assetbaseUrl", "api/asset/v1/makes/{id}"),
        create_request("Update Make", "PUT", "assetbaseUrl", "api/asset/v1/makes/{id}",
            "MakeRequest body", {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}",
                "make": {"makeName": "Dell Updated", "subCategory": {"subCategoryId": 1}, "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update Make with Document", "PUT", "assetbaseUrl", "api/asset/v1/makes/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "make": {"makeName": "Dell Updated", "subCategory": {"subCategoryId": 1}, "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}}),
            path_var_defaults={"id": "1"}),
        create_request("Delete Make", "DELETE", "assetbaseUrl", "api/asset/v1/makes/{id}",
            "MakeRequest body", {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Create Makes", "POST", "assetbaseUrl", "api/asset/v1/makes/bulk",
            "BulkMakeRequest"),
        create_request("Bulk Upload Makes Excel", "POST", "assetbaseUrl", "api/asset/v1/makes/bulk/excel",
            "Multipart: file. Query: userId, username, projectType",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Make Favourite", "PUT", "assetbaseUrl", "api/asset/v1/makes/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Make Most Like", "PUT", "assetbaseUrl", "api/asset/v1/makes/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Make Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/makes/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "6. Master Data - Models", "item": [
        create_request("List Models", "GET", "assetbaseUrl", "api/asset/v1/models"),
        create_request("Create Model", "POST", "assetbaseUrl", "api/asset/v1/models",
            "JSON body. ModelRequest: userId, username, projectType, model{modelName, make{makeId}}. Optional: description, sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "model": {
                    "modelName": "XPS 15",
                    "make": {"makeId": 1},
                    "description": "Dell XPS 15 laptop",
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Model with Document", "POST", "assetbaseUrl", "api/asset/v1/models/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "model": {"modelName": "XPS 15", "make": {"makeId": 1}, "description": "Dell XPS 15", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}})),
        create_request("Get Model by ID", "GET", "assetbaseUrl", "api/asset/v1/models/{id}"),
        create_request("Update Model", "PUT", "assetbaseUrl", "api/asset/v1/models/{id}",
            "ModelRequest body", {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}",
                "model": {"modelName": "XPS 15 Updated", "make": {"makeId": 1}, "description": "Updated", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update Model with Document", "PUT", "assetbaseUrl", "api/asset/v1/models/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "model": {"modelName": "XPS 15 Updated", "make": {"makeId": 1}, "description": "Updated", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}}),
            path_var_defaults={"id": "1"}),
        create_request("Delete Model", "DELETE", "assetbaseUrl", "api/asset/v1/models/{id}",
            "ModelRequest body", {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Create Models", "POST", "assetbaseUrl", "api/asset/v1/models/bulk",
            "BulkModelRequest"),
        create_request("Bulk Upload Models Excel", "POST", "assetbaseUrl", "api/asset/v1/models/bulk/excel",
            "Multipart: file. Query: userId, username, projectType",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Model Favourite", "PUT", "assetbaseUrl", "api/asset/v1/models/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Model Most Like", "PUT", "assetbaseUrl", "api/asset/v1/models/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Model Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/models/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "7. Master Data - Vendors", "item": [
        create_request("List Vendors", "GET", "assetbaseUrl", "api/asset/v1/vendors"),
        create_request("Create Vendor", "POST", "assetbaseUrl", "api/asset/v1/vendors",
            "JSON body. VendorRequest: userId, username, projectType, vendor{vendorName}. Optional: contactPerson, email, mobile, address, sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "vendor": {
                    "vendorName": "Dell Inc",
                    "contactPerson": "John",
                    "email": "contact@dell.com",
                    "mobile": "+1234567890",
                    "address": "123 Vendor St",
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Vendor with Document", "POST", "assetbaseUrl", "api/asset/v1/vendors/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "vendor": {"vendorName": "Dell Inc", "contactPerson": "John", "email": "contact@dell.com", "mobile": "+1234567890", "address": "123 Vendor St", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}})),
        create_request("Get Vendor by ID", "GET", "assetbaseUrl", "api/asset/v1/vendors/{id}"),
        create_request("Update Vendor", "PUT", "assetbaseUrl", "api/asset/v1/vendors/{id}",
            "VendorRequest body", {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}",
                "vendor": {"vendorName": "Dell Inc Updated", "contactPerson": "John", "email": "contact@dell.com", "mobile": "+1234567890", "address": "123 St", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update Vendor with Document", "PUT", "assetbaseUrl", "api/asset/v1/vendors/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "vendor": {"vendorName": "Dell Inc Updated", "contactPerson": "John", "email": "contact@dell.com", "mobile": "+1234567890", "address": "123 St", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}}),
            path_var_defaults={"id": "1"}),
        create_request("Delete Vendor", "DELETE", "assetbaseUrl", "api/asset/v1/vendors/{id}",
            "VendorRequest body", {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Create Vendors", "POST", "assetbaseUrl", "api/asset/v1/vendors/bulk",
            "BulkVendorRequest"),
        create_request("Bulk Upload Vendors Excel", "POST", "assetbaseUrl", "api/asset/v1/vendors/bulk/excel",
            "Multipart: file. Query: userId, username, projectType",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Vendor Favourite", "PUT", "assetbaseUrl", "api/asset/v1/vendors/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Vendor Most Like", "PUT", "assetbaseUrl", "api/asset/v1/vendors/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Vendor Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/vendors/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "8. Master Data - Outlets", "item": [
        create_request("List Outlets", "GET", "assetbaseUrl", "api/asset/v1/outlets"),
        create_request("Create Outlet", "POST", "assetbaseUrl", "api/asset/v1/outlets",
            "JSON body. OutletRequest: userId, username, projectType, outlet{outletName}. Optional: outletAddress, contactInfo, vendor{vendorId}, sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "outlet": {
                    "outletName": "Best Buy",
                    "outletAddress": "123 Main St",
                    "contactInfo": "+1234567890",
                    "vendor": {"vendorId": 1},
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Outlet with Document", "POST", "assetbaseUrl", "api/asset/v1/outlets/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "outlet": {"outletName": "Best Buy", "outletAddress": "123 Main St", "contactInfo": "+1234567890", "vendor": {"vendorId": 1}, "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}})),
        create_request("Get Outlet by ID", "GET", "assetbaseUrl", "api/asset/v1/outlets/{id}"),
        create_request("Update Outlet", "PUT", "assetbaseUrl", "api/asset/v1/outlets/{id}",
            "OutletRequest body", {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}",
                "outlet": {"outletName": "Best Buy Updated", "outletAddress": "456 Oak St", "contactInfo": "+123", "vendor": {"vendorId": 1}, "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update Outlet with Document", "PUT", "assetbaseUrl", "api/asset/v1/outlets/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "outlet": {"outletName": "Best Buy Updated", "outletAddress": "456 Oak St", "contactInfo": "+123", "vendor": {"vendorId": 1}, "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}}),
            path_var_defaults={"id": "1"}),
        create_request("Delete Outlet", "DELETE", "assetbaseUrl", "api/asset/v1/outlets/{id}",
            "OutletRequest body", {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Create Outlets", "POST", "assetbaseUrl", "api/asset/v1/outlets/bulk",
            "BulkOutletRequest"),
        create_request("Bulk Upload Outlets Excel", "POST", "assetbaseUrl", "api/asset/v1/outlets/bulk/excel",
            "Multipart: file. Query: userId, username, projectType",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Outlet Favourite", "PUT", "assetbaseUrl", "api/asset/v1/outlets/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Outlet Most Like", "PUT", "assetbaseUrl", "api/asset/v1/outlets/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Outlet Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/outlets/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "9. Master Data - Components", "item": [
        create_request("List Components", "GET", "assetbaseUrl", "api/asset/v1/components"),
        create_request("Create Component", "POST", "assetbaseUrl", "api/asset/v1/components",
            "JSON body. ComponentRequest: userId, username, projectType, component{componentName}. Optional: description, sequenceOrder, isFavourite, isMostLike.",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "component": {
                    "componentName": "RAM 16GB",
                    "description": "16GB DDR4 RAM",
                    "sequenceOrder": 1,
                    "isFavourite": False,
                    "isMostLike": False
                }
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Component with Document", "POST", "assetbaseUrl", "api/asset/v1/components/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "component": {"componentName": "RAM 16GB", "description": "16GB DDR4 RAM", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}})),
        create_request("Get Component by ID", "GET", "assetbaseUrl", "api/asset/v1/components/{id}"),
        create_request("Update Component", "PUT", "assetbaseUrl", "api/asset/v1/components/{id}",
            "ComponentRequest body", {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}",
                "component": {"componentName": "RAM 16GB Updated", "description": "Updated", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update Component with Document", "PUT", "assetbaseUrl", "api/asset/v1/components/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "component": {"componentName": "RAM 16GB Updated", "description": "Updated", "sequenceOrder": 1, "isFavourite": False, "isMostLike": False}}),
            path_var_defaults={"id": "1"}),
        create_request("Delete Component", "DELETE", "assetbaseUrl", "api/asset/v1/components/{id}",
            "ComponentRequest body", {"mode": "raw", "raw": json.dumps({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Create Components", "POST", "assetbaseUrl", "api/asset/v1/components/bulk",
            "BulkComponentRequest"),
        create_request("Bulk Upload Components Excel", "POST", "assetbaseUrl", "api/asset/v1/components/bulk/excel",
            "Multipart: file. Query: userId, username, projectType",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Component Favourite", "PUT", "assetbaseUrl", "api/asset/v1/components/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Component Most Like", "PUT", "assetbaseUrl", "api/asset/v1/components/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Component Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/components/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "10. User Links", "item": [
        create_request("Link Entity to User", "POST", "assetbaseUrl", "api/asset/v1/userlinks/link",
            "AssetUserUniversalLinkRequest: entityType, entityId, targetUserId, targetUsername",
            {"mode": "raw", "raw": json.dumps({
                "entityType": "ASSET",
                "entityId": 1,
                "targetUserId": 2,
                "targetUsername": "user1"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Delink Entity from User", "POST", "assetbaseUrl", "api/asset/v1/userlinks/delink",
            "Delink entity from user",
            {"mode": "raw", "raw": json.dumps({
                "entityType": "ASSET",
                "entityId": 1,
                "targetUserId": 2,
                "targetUsername": "user1"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Link Multiple Entities", "POST", "assetbaseUrl", "api/asset/v1/userlinks/link-multiple",
            "AssetUserMultiLinkRequest: targetUserId, targetUsername, entities[]",
            {"mode": "raw", "raw": json.dumps({
                "targetUserId": 2,
                "targetUsername": "user1",
                "entities": [{"entityType": "ASSET", "entityId": 1}, {"entityType": "ASSET", "entityId": 2}]
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Delink Multiple Entities", "POST", "assetbaseUrl", "api/asset/v1/userlinks/delink-multiple",
            "AssetUserMultiDelinkRequest: targetUserId, entities[]",
            {"mode": "raw", "raw": json.dumps({
                "targetUserId": 2,
                "entities": [{"entityType": "ASSET", "entityId": 1}, {"entityType": "ASSET", "entityId": 2}]
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get Assigned Assets", "GET", "assetbaseUrl", "api/asset/v1/userlinks/assigned-assets",
            "Query: targetUserId",
            params=[{"key": "targetUserId", "value": "1"}]),
        create_request("Get Single Asset", "GET", "assetbaseUrl", "api/asset/v1/userlinks/asset",
            "Query: assetId or componentId (at least one optional)",
            params=[
                {"key": "assetId", "value": "1"},
                {"key": "componentId", "value": "1", "disabled": True}
            ]),
        create_request("Get By SubCategory", "GET", "assetbaseUrl", "api/asset/v1/userlinks/by-subcategory",
            "Query: subCategoryId",
            params=[{"key": "subCategoryId", "value": "1"}]),
        create_request("Get Master Data All", "GET", "assetbaseUrl", "api/asset/v1/userlinks/master-data/all",
            "Get comprehensive master data"),
        create_request("Need Your Attention", "GET", "assetbaseUrl", "api/asset/v1/userlinks/need-your-attention",
            "Get attention data for logged-in user"),
        ]},
        {"name": "11. Documents", "item": [
        create_request("Get Allowed Document Types", "GET", "assetbaseUrl", "api/asset/v1/documents/allowed-types",
            "Returns allowed docType values (PDF, IMAGE, RECEIPT, etc.)"),
        create_request("Upload Document", "POST", "assetbaseUrl", "api/asset/v1/documents/upload",
            "Multipart: file (required), entityType, entityId, userId, username, docType (required). Optional: projectType",
            body={"mode": "formdata", "formdata": [
                {"key": "file", "type": "file", "src": [], "description": "Required: document file"},
                {"key": "entityType", "value": "ASSET", "type": "text"},
                {"key": "entityId", "value": "{{entityId}}", "type": "text"},
                {"key": "userId", "value": "{{userId}}", "type": "text"},
                {"key": "username", "value": "{{username}}", "type": "text"},
                {"key": "projectType", "value": "{{projectType}}", "type": "text", "description": "Optional"},
                {"key": "docType", "value": "{{docType}}", "type": "text", "description": "Required: PDF, IMAGE, RECEIPT, etc."}
            ]},
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Get Document", "GET", "assetbaseUrl", "api/asset/v1/documents/{id}"),
        create_request("Download Document", "GET", "assetbaseUrl", "api/asset/v1/documents/download/{id}"),
        create_request("Delete Document", "DELETE", "assetbaseUrl", "api/asset/v1/documents/{id}",
            "DocumentRequest body: userId, username, projectType, entityType, entityId",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "entityType": "ASSET",
                "entityId": "{{entityId}}"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Bulk Upload Documents", "POST", "assetbaseUrl", "api/asset/v1/documents/bulk",
            "Multipart: files (required), request (BulkDocumentRequest with docType required per document), userId, username, projectType",
            body={"mode": "formdata", "formdata": [
                {"key": "files", "type": "file", "src": [], "description": "Required: document files"},
                {"key": "userId", "value": "{{userId}}", "type": "text"},
                {"key": "username", "value": "{{username}}", "type": "text"},
                {"key": "projectType", "value": "{{projectType}}", "type": "text"},
                {"key": "request", "value": json.dumps({"documents": [{"entityType": "ASSET", "entityId": 1, "docType": "{{docType}}"}]}, indent=2), "type": "text", "description": "Required: each document must have entityType, entityId, docType"}
            ]},
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Bulk Upload Documents (Excel)", "POST", "assetbaseUrl", "api/asset/v1/documents/bulk/excel",
            "Multipart: file. Query: userId, username, projectType",
            body={"mode": "formdata", "formdata": [{"key": "file", "type": "file", "src": []}]},
            params=[{"key": "userId", "value": "{{userId}}"}, {"key": "username", "value": "{{username}}"}, {"key": "projectType", "value": "{{projectType}}"}],
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Bulk Create from Paths", "POST", "assetbaseUrl", "api/asset/v1/documents/bulk/paths",
            "BulkDocumentRequest: documents array. Each: entityType, entityId, docType (required), filePath (required)",
            {"mode": "raw", "raw": json.dumps({
                "documents": [
                    {"entityType": "ASSET", "entityId": 1, "docType": "{{docType}}", "filePath": "/path/to/doc.pdf"}
                ]
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        ]},
        {"name": "12. Warranty", "item": [
        create_request("Create Warranty", "POST", "assetbaseUrl", "api/asset/v1/warranty",
            "AssetWarrantyRequest: userId, username, assetId, warrantyStatus, startDate, endDate, documentId, docType (required). Optional: projectType, componentId, warrantyProvider, warrantyTerms",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "assetId": 1,
                "componentId": None,
                "warrantyStatus": "ACTIVE",
                "warrantyProvider": "Dell",
                "warrantyTerms": "Standard warranty terms",
                "startDate": "2024-01-01",
                "endDate": "2025-01-01",
                "documentId": "{{documentId}}",
                "docType": "{{docType}}"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Warranty with Document", "POST", "assetbaseUrl", "api/asset/v1/warranty/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "assetId": 1, "componentId": None, "warrantyStatus": "ACTIVE", "warrantyProvider": "Dell", "warrantyTerms": "Standard terms", "startDate": "2024-01-01", "endDate": "2025-01-01"})),
        create_request("List Warranties", "GET", "assetbaseUrl", "api/asset/v1/warranty"),
        create_request("Get Warranty by ID", "GET", "assetbaseUrl", "api/asset/v1/warranty/{id}"),
        create_request("Warranty Favourite", "PUT", "assetbaseUrl", "api/asset/v1/warranty/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Warranty Most Like", "PUT", "assetbaseUrl", "api/asset/v1/warranty/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Warranty Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/warranty/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        create_request("Update Warranty", "PUT", "assetbaseUrl", "api/asset/v1/warranty/{id}",
            "Update warranty",
            {"mode": "raw", "raw": json.dumps({
                "assetId": 1,
                "warrantyStatus": "EXPIRED"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update Warranty with Document", "PUT", "assetbaseUrl", "api/asset/v1/warranty/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "assetId": 1, "componentId": None, "warrantyStatus": "ACTIVE", "warrantyProvider": "Dell", "warrantyTerms": "Standard terms", "startDate": "2024-01-01", "endDate": "2025-01-01"}),
            path_var_defaults={"id": "1"}),
        create_request("Delete Warranty", "DELETE", "assetbaseUrl", "api/asset/v1/warranty/{id}"),
        ]},
        {"name": "13. AMC", "item": [
        create_request("Create AMC", "POST", "assetbaseUrl", "api/asset/v1/amc",
            "AssetAmcRequest: userId, username, assetId, startDate, endDate, documentId, docType (required). Optional: projectType, componentId, amcStatus",
            {"mode": "raw", "raw": json.dumps({
                "userId": "{{userId}}",
                "username": "{{username}}",
                "projectType": "{{projectType}}",
                "assetId": 1,
                "componentId": None,
                "amcStatus": "ACTIVE",
                "startDate": "2024-01-01",
                "endDate": "2025-01-01",
                "documentId": "{{documentId}}",
                "docType": "{{docType}}"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create AMC with Document", "POST", "assetbaseUrl", "api/asset/v1/amc/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "assetId": 1, "componentId": None, "amcStatus": "ACTIVE", "startDate": "2024-01-01", "endDate": "2025-01-01"})),
        create_request("List AMCs", "GET", "assetbaseUrl", "api/asset/v1/amc"),
        create_request("Get AMC by ID", "GET", "assetbaseUrl", "api/asset/v1/amc/{id}"),
        create_request("AMC Favourite", "PUT", "assetbaseUrl", "api/asset/v1/amc/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("AMC Most Like", "PUT", "assetbaseUrl", "api/asset/v1/amc/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("AMC Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/amc/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        create_request("Update AMC", "PUT", "assetbaseUrl", "api/asset/v1/amc/{id}",
            "Update AMC",
            {"mode": "raw", "raw": json.dumps({
                "assetId": 1,
                "amcStatus": "EXPIRED"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Update AMC with Document", "PUT", "assetbaseUrl", "api/asset/v1/amc/{id}/with-document",
            "Raw JSON: request, document (base64 of file), docType (required)",
            body=with_document_raw_body({"userId": "{{userId}}", "username": "{{username}}", "projectType": "{{projectType}}", "assetId": 1, "componentId": None, "amcStatus": "ACTIVE", "startDate": "2024-01-01", "endDate": "2025-01-01"}),
            path_var_defaults={"id": "1"}),
        create_request("Delete AMC", "DELETE", "assetbaseUrl", "api/asset/v1/amc/{id}"),
        ]},
        {"name": "14. Compliance", "item": [
        create_request("Validate Entity", "POST", "assetbaseUrl", "api/asset/v1/compliance/validate",
            "entityType, entityId",
            {"mode": "raw", "raw": json.dumps({"entityType": "ASSET", "entityId": 1}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Validate Entity (GET)", "GET", "assetbaseUrl", "api/asset/v1/compliance/validate/{entityType}/{entityId}",
            path_var_defaults={"entityType": "ASSET", "entityId": "1"}),
        create_request("Get Compliance Status", "GET", "assetbaseUrl", "api/asset/v1/compliance/status/{entityType}/{entityId}",
            path_var_defaults={"entityType": "ASSET", "entityId": "1"}),
        create_request("Get Violations", "GET", "assetbaseUrl", "api/asset/v1/compliance/violations/{entityType}/{entityId}",
            "Query: unresolvedOnly (optional, default: true)",
            path_var_defaults={"entityType": "ASSET", "entityId": "1"},
            params=[{"key": "unresolvedOnly", "value": "true", "disabled": True}]),
        create_request("Get Violations Summary", "GET", "assetbaseUrl", "api/asset/v1/compliance/violations/summary"),
        create_request("Bulk Validate", "POST", "assetbaseUrl", "api/asset/v1/compliance/validate/bulk/{entityType}",
            "Body: List of entityIds", {"mode": "raw", "raw": json.dumps([1, 2, 3], indent=2), "options": {"raw": {"language": "json"}}},
            path_var_defaults={"entityType": "ASSET"}),
        create_request("Get Compliance Metrics by Type", "GET", "assetbaseUrl", "api/asset/v1/compliance/metrics/{entityType}",
            path_var_defaults={"entityType": "ASSET"}),
        create_request("Resolve Violation", "POST", "assetbaseUrl", "api/asset/v1/compliance/violations/{violationId}/resolve",
            "Query: resolvedBy (required), notes (optional)",
            params=[
                {"key": "resolvedBy", "value": "{{username}}"},
                {"key": "notes", "value": "Violation resolved", "disabled": True}
            ]),
        create_request("Get Compliance Metrics", "GET", "assetbaseUrl", "api/asset/v1/compliance/metrics"),
        create_request("Get Compliance Report", "GET", "assetbaseUrl", "api/asset/v1/compliance/report/{entityType}/{entityId}",
            path_var_defaults={"entityType": "ASSET", "entityId": "1"}),
        ]},
        {"name": "15. Compliance Rules", "item": [
        create_request("List Compliance Rules", "GET", "assetbaseUrl", "api/asset/v1/compliance/rules"),
        create_request("List Rules by Entity Type", "GET", "assetbaseUrl", "api/asset/v1/compliance/rules/entity-type/{entityType}",
            path_var_defaults={"entityType": "ASSET"}),
        create_request("Get Rule by ID", "GET", "assetbaseUrl", "api/asset/v1/compliance/rules/{ruleId}"),
        create_request("Create Compliance Rule", "POST", "assetbaseUrl", "api/asset/v1/compliance/rules",
            "ComplianceRule body. Query: createdBy",
            {"mode": "raw", "raw": json.dumps({
                "ruleCode": "RULE001",
                "ruleName": "Asset Warranty Required",
                "entityType": "ASSET",
                "ruleExpression": "warranty != null"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Initialize Compliance Rules", "POST", "assetbaseUrl", "api/asset/v1/compliance/rules/initialize",
            "Query: createdBy"),
        create_request("Get Rule Templates", "GET", "assetbaseUrl", "api/asset/v1/compliance/rules/templates"),
        ]},
        {"name": "16. Master Data Agent", "item": [
        create_request("Create Category (Agent)", "POST", "assetbaseUrl", "api/asset/v1/masters/categories",
            "categoryName, createdBy",
            {"mode": "raw", "raw": json.dumps({"categoryName": "Electronics", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create SubCategory (Agent)", "POST", "assetbaseUrl", "api/asset/v1/masters/subcategories",
            "subCategoryName, categoryId, createdBy",
            {"mode": "raw", "raw": json.dumps({"subCategoryName": "Laptops", "categoryId": 1, "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Make (Agent)", "POST", "assetbaseUrl", "api/asset/v1/masters/makes",
            "makeName, subCategoryId, createdBy",
            {"mode": "raw", "raw": json.dumps({"makeName": "Dell", "subCategoryId": 1, "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Model (Agent)", "POST", "assetbaseUrl", "api/asset/v1/masters/models",
            "modelName, makeId, createdBy",
            {"mode": "raw", "raw": json.dumps({"modelName": "XPS 15", "makeId": 1, "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Vendor (Agent)", "POST", "assetbaseUrl", "api/asset/v1/masters/vendors",
            "vendorName, createdBy",
            {"mode": "raw", "raw": json.dumps({"vendorName": "Dell Inc", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Outlet (Agent)", "POST", "assetbaseUrl", "api/asset/v1/masters/outlets",
            "outletName, createdBy",
            {"mode": "raw", "raw": json.dumps({"outletName": "Best Buy", "createdBy": "admin"}, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Create Component (Agent)", "POST", "assetbaseUrl", "api/asset/v1/masters/components",
            "Query: componentName, description, createdBy",
            params=[{"key": "componentName", "value": "RAM 16GB"}, {"key": "description", "value": "16GB DDR4"}]),
        create_request("Bulk Create Categories (Agent)", "POST", "assetbaseUrl", "api/asset/v1/masters/categories/bulk",
            "Body: array of category names. Query: createdBy",
            {"mode": "raw", "raw": json.dumps(["Category1", "Category2"], indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get Master Data Summary", "GET", "assetbaseUrl", "api/asset/v1/masters/summary"),
        ]},
        {"name": "17. Audit Agent", "item": [
        create_request("Log Audit Event", "POST", "assetbaseUrl", "api/asset/v1/audit/log",
            "username, eventMessage, action, entityType, entityId",
            {"mode": "raw", "raw": json.dumps({
                "username": "admin",
                "eventMessage": "Asset created",
                "action": "CREATE",
                "entityType": "ASSET",
                "entityId": 1
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get Audit Logs", "GET", "assetbaseUrl", "api/asset/v1/audit"),
        create_request("Get Audit by Username", "GET", "assetbaseUrl", "api/asset/v1/audit/username/{username}",
            path_var_defaults={"username": "admin"}),
        create_request("Get Audit by Entity Type", "GET", "assetbaseUrl", "api/asset/v1/audit/entity-type/{entityType}",
            path_var_defaults={"entityType": "ASSET"}),
        create_request("Get Audit Statistics", "GET", "assetbaseUrl", "api/asset/v1/audit/statistics"),
        ]},
        {"name": "18. User Asset Link Agent", "item": [
        create_request("Link Asset to User (Agent)", "POST", "assetbaseUrl", "api/asset/v1/user-asset-links/link-asset",
            "assetId, userId, username, email, mobile, createdBy",
            {"mode": "raw", "raw": json.dumps({
                "assetId": 1,
                "userId": 2,
                "username": "user1",
                "email": "user@example.com",
                "mobile": "1234567890",
                "createdBy": "admin"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Link Component to User (Agent)", "POST", "assetbaseUrl", "api/asset/v1/user-asset-links/link-component",
            "componentId, userId, username, email, mobile, createdBy",
            {"mode": "raw", "raw": json.dumps({
                "componentId": 1,
                "userId": 2,
                "username": "user1",
                "createdBy": "admin"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Delink Asset (Agent)", "POST", "assetbaseUrl", "api/asset/v1/user-asset-links/delink-asset",
            "Query: assetId, userId, updatedBy",
            params=[{"key": "assetId", "value": "1"}, {"key": "userId", "value": "2"}]),
        create_request("Get Assets for User (Agent)", "GET", "assetbaseUrl", "api/asset/v1/user-asset-links/user/{userId}/assets",
            "Get assets assigned to user"),
        ]},
        {"name": "19. LLM & Extraction", "item": [
        create_request("LLM Extract", "POST", "assetbaseUrl", "api/asset/v1/llm-extraction/extract",
            "Multipart: file (required), documentType (required)",
            body={"mode": "formdata", "formdata": [
                {"key": "file", "type": "file", "src": [], "description": "Required: document file"},
                {"key": "documentType", "value": "{{docType}}", "type": "text", "description": "Required: PDF, IMAGE, RECEIPT, etc."}
            ]},
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Intelligent Extract", "POST", "assetbaseUrl", "api/asset/v1/intelligent-extraction/extract",
            "Multipart: file (required), documentType (required). Optional: userId, username, projectType, autoCreateEntities, extractWarranty, extractAmc, extractComponents, existingAssetId",
            body={"mode": "formdata", "formdata": [
                {"key": "file", "type": "file", "src": [], "description": "Required: document file"},
                {"key": "userId", "value": "{{userId}}", "type": "text", "description": "Optional"},
                {"key": "username", "value": "{{username}}", "type": "text", "description": "Optional"},
                {"key": "projectType", "value": "{{projectType}}", "type": "text", "description": "Optional"},
                {"key": "documentType", "value": "{{docType}}", "type": "text", "description": "Required: PDF, IMAGE, RECEIPT, etc."},
                {"key": "autoCreateEntities", "value": "true", "type": "text", "description": "Optional"},
                {"key": "extractWarranty", "value": "true", "type": "text", "description": "Optional"},
                {"key": "extractAmc", "value": "true", "type": "text", "description": "Optional"},
                {"key": "extractComponents", "value": "true", "type": "text", "description": "Optional"},
                {"key": "existingAssetId", "value": "", "type": "text", "description": "Optional - link to existing asset"}
            ]},
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Product OCR Scan", "POST", "assetbaseUrl", "api/asset/v1/products/ocr/scan",
            "Multipart: file. Optional: userId, username, projectType, subCategoryId, subCategoryName, autoCreateMake, autoCreateModel",
            body={"mode": "formdata", "formdata": [
                {"key": "file", "type": "file", "src": []},
                {"key": "userId", "value": "{{userId}}", "type": "text", "description": "Optional"},
                {"key": "username", "value": "{{username}}", "type": "text", "description": "Optional"},
                {"key": "projectType", "value": "{{projectType}}", "type": "text", "description": "Optional"},
                {"key": "subCategoryId", "value": "1", "type": "text", "description": "Optional"},
                {"key": "subCategoryName", "value": "Laptops", "type": "text", "description": "Optional"},
                {"key": "autoCreateMake", "value": "true", "type": "text", "description": "Optional"},
                {"key": "autoCreateModel", "value": "true", "type": "text", "description": "Optional"}
            ]},
            headers=[{"key": "Authorization", "value": "Bearer {{bearerToken}}", "type": "text"}]),
        create_request("Product OCR Extract from Text", "POST", "assetbaseUrl", "api/asset/v1/products/ocr/extract-text",
            "Query: ocrText (required). Optional: userId, username, projectType, subCategoryId, subCategoryName, autoCreateMake, autoCreateModel",
            params=[{"key": "ocrText", "value": "Dell XPS 15 SN123456"}, {"key": "userId", "value": "{{userId}}", "disabled": True}, {"key": "username", "value": "{{username}}", "disabled": True}]),
        create_request("Product OCR Correct", "POST", "assetbaseUrl", "api/asset/v1/products/ocr/correct",
            "OcrCorrectionRequest: trainingId (required). Optional: correctedMake, correctedModel, correctedSerial, username",
            {"mode": "raw", "raw": json.dumps({
                "trainingId": 1,
                "correctedMake": "Dell",
                "correctedModel": "XPS 15",
                "correctedSerial": "SN123456",
                "username": "{{username}}"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Product OCR Train", "POST", "assetbaseUrl", "api/asset/v1/products/ocr/train",
            "Query: username (optional, default: SYSTEM). Body: empty",
            params=[{"key": "username", "value": "{{username}}", "disabled": True}]),
        ]},
        {"name": "20. File Download", "item": [
        create_request("Download File", "GET", "assetbaseUrl", "api/asset/v1/files/download",
            "Query: filename, inline",
            params=[{"key": "filename", "value": "document.pdf"}, {"key": "inline", "value": "false"}]),
        ]},
        {"name": "21. Statuses", "item": [
        create_request("List Statuses", "GET", "assetbaseUrl", "api/asset/v1/statuses"),
        create_request("List Active Statuses", "GET", "assetbaseUrl", "api/asset/v1/statuses/active"),
        create_request("List Statuses by Category", "GET", "assetbaseUrl", "api/asset/v1/statuses/category/{category}",
            path_var_defaults={"category": "ASSET"}),
        create_request("List Active Statuses by Category", "GET", "assetbaseUrl", "api/asset/v1/statuses/category/{category}/active",
            path_var_defaults={"category": "ASSET"}),
        create_request("Get Status by Code", "GET", "assetbaseUrl", "api/asset/v1/statuses/code/{code}",
            path_var_defaults={"code": "ACTIVE"}),
        create_request("Get Status by ID", "GET", "assetbaseUrl", "api/asset/v1/statuses/{id}"),
        create_request("Validate Status", "GET", "assetbaseUrl", "api/asset/v1/statuses/validate/{code}",
            path_var_defaults={"code": "ACTIVE"}),
        create_request("Initialize Statuses", "POST", "assetbaseUrl", "api/asset/v1/statuses/initialize"),
        create_request("Status Favourite", "PUT", "assetbaseUrl", "api/asset/v1/statuses/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("Status Most Like", "PUT", "assetbaseUrl", "api/asset/v1/statuses/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("Status Sequence Order", "PUT", "assetbaseUrl", "api/asset/v1/statuses/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        ]},
        {"name": "22. Entity Types", "item": [
        create_request("List Entity Types", "GET", "assetbaseUrl", "api/asset/v1/entity-types"),
        create_request("List Active Entity Types", "GET", "assetbaseUrl", "api/asset/v1/entity-types/active"),
        create_request("Get Entity Type by Code", "GET", "assetbaseUrl", "api/asset/v1/entity-types/code/{code}",
            path_var_defaults={"code": "ASSET"}),
        create_request("Get Entity Type by ID", "GET", "assetbaseUrl", "api/asset/v1/entity-types/{id}"),
        create_request("Validate Entity Type", "GET", "assetbaseUrl", "api/asset/v1/entity-types/validate/{code}",
            path_var_defaults={"code": "ASSET"}),
        create_request("Initialize Entity Types", "POST", "assetbaseUrl", "api/asset/v1/entity-types/initialize"),
        ]},
    ]

def build_helpdesk_modules():
    """Build Helpdesk Service as list of module folders."""
    return [
        {"name": "1. Issues", "item": [
        create_request("Create Issue", "POST", "helpdeskbaseUrl", "api/helpdesk/issues",
            "IssueRequest: title, description, priority, relatedService. Swagger: helpdesk_openapi.yaml",
            {"mode": "raw", "raw": json.dumps({
                "title": "Network connectivity issue",
                "description": "Unable to connect to network",
                "priority": "HIGH",
                "relatedService": "ASSET_SERVICE"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get All Issues", "GET", "helpdeskbaseUrl", "api/helpdesk/issues"),
        create_request("Get Issue by ID", "GET", "helpdeskbaseUrl", "api/helpdesk/issues/{id}"),
        create_request("Get Issues by Status", "GET", "helpdeskbaseUrl", "api/helpdesk/issues/status/{status}",
            "status: OPEN|IN_PROGRESS|RESOLVED|CLOSED|REOPENED",
            path_var_defaults={"status": "OPEN"}),
        create_request("Get Issues by Service", "GET", "helpdeskbaseUrl", "api/helpdesk/issues/service/{service}",
            "service: AUTH_SERVICE|NOTIFICATION_SERVICE|ASSET_SERVICE|HELPDESK_SERVICE",
            path_var_defaults={"service": "ASSET_SERVICE"}),
        create_request("Get My Issues", "GET", "helpdeskbaseUrl", "api/helpdesk/issues/my-issues"),
        create_request("Update Issue Status", "PATCH", "helpdeskbaseUrl", "api/helpdesk/issues/{id}/status",
            "Query param: status (OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED)",
            params=[{"key": "status", "value": "IN_PROGRESS"}]),
        create_request("Assign Issue", "PATCH", "helpdeskbaseUrl", "api/helpdesk/issues/{id}/assign",
            "Query param: assignedTo",
            params=[{"key": "assignedTo", "value": "agent@example.com"}]),
        create_request("Resolve Issue", "POST", "helpdeskbaseUrl", "api/helpdesk/issues/{id}/resolve",
            "IssueResolutionRequest: resolution",
            {"mode": "raw", "raw": json.dumps({
                "resolution": "Issue resolved by restarting the router"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Close Issue", "PATCH", "helpdeskbaseUrl", "api/helpdesk/issues/{id}/close"),
        ]},
        {"name": "2. Queries", "item": [
        create_request("Create Query", "POST", "helpdeskbaseUrl", "api/helpdesk/queries",
            "QueryRequest: question, relatedService",
            {"mode": "raw", "raw": json.dumps({
                "question": "How do I create an asset?",
                "relatedService": "ASSET_SERVICE"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get All Queries", "GET", "helpdeskbaseUrl", "api/helpdesk/queries"),
        create_request("Get Query by ID", "GET", "helpdeskbaseUrl", "api/helpdesk/queries/{id}"),
        create_request("Get Queries by Status", "GET", "helpdeskbaseUrl", "api/helpdesk/queries/status/{status}",
            path_var_defaults={"status": "PENDING"}),
        create_request("Get Queries by Service", "GET", "helpdeskbaseUrl", "api/helpdesk/queries/service/{service}",
            path_var_defaults={"service": "ASSET_SERVICE"}),
        create_request("Get My Queries", "GET", "helpdeskbaseUrl", "api/helpdesk/queries/my-queries"),
        create_request("Answer Query", "POST", "helpdeskbaseUrl", "api/helpdesk/queries/{id}/answer",
            "QueryAnswerRequest: answer",
            {"mode": "raw", "raw": json.dumps({
                "answer": "You can create an asset using the asset creation API"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Close Query", "PATCH", "helpdeskbaseUrl", "api/helpdesk/queries/{id}/close"),
        ]},
        {"name": "3. FAQs", "item": [
        create_request("Create FAQ", "POST", "helpdeskbaseUrl", "api/helpdesk/faqs",
            "FAQRequest: question, answer, relatedService, category",
            {"mode": "raw", "raw": json.dumps({
                "question": "How do I reset my password?",
                "answer": "Click on forgot password link",
                "relatedService": "ASSET_SERVICE",
                "category": "Authentication"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get All FAQs", "GET", "helpdeskbaseUrl", "api/helpdesk/faqs"),
        create_request("Get FAQ by ID", "GET", "helpdeskbaseUrl", "api/helpdesk/faqs/{id}"),
        create_request("Get FAQs by Service", "GET", "helpdeskbaseUrl", "api/helpdesk/faqs/service/{service}",
            path_var_defaults={"service": "ASSET_SERVICE"}),
        create_request("Get FAQs by Category", "GET", "helpdeskbaseUrl", "api/helpdesk/faqs/category/{category}",
            path_var_defaults={"category": "Authentication"}),
        create_request("Search FAQs", "GET", "helpdeskbaseUrl", "api/helpdesk/faqs/search",
            "Query: keyword (required)", params=[{"key": "keyword", "value": "password"}]),
        create_request("Search FAQs by Service", "GET", "helpdeskbaseUrl", "api/helpdesk/faqs/service/{service}/search",
            "Query: keyword (required)", path_var_defaults={"service": "ASSET_SERVICE"},
            params=[{"key": "keyword", "value": "asset"}]),
        create_request("Update FAQ", "PUT", "helpdeskbaseUrl", "api/helpdesk/faqs/{id}",
            "Update FAQ",
            {"mode": "raw", "raw": json.dumps({
                "question": "Updated question",
                "answer": "Updated answer",
                "relatedService": "ASSET_SERVICE",
                "category": "Authentication"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Mark FAQ Helpful", "POST", "helpdeskbaseUrl", "api/helpdesk/faqs/{id}/helpful"),
        create_request("FAQ Favourite", "PUT", "helpdeskbaseUrl", "api/helpdesk/faqs/{id}/favourite",
            params=[{"key": "isFavourite", "value": "true"}]),
        create_request("FAQ Most Like", "PUT", "helpdeskbaseUrl", "api/helpdesk/faqs/{id}/most-like",
            params=[{"key": "isMostLike", "value": "true"}]),
        create_request("FAQ Sequence Order", "PUT", "helpdeskbaseUrl", "api/helpdesk/faqs/{id}/sequence-order",
            params=[{"key": "sequenceOrder", "value": "1"}]),
        create_request("Delete FAQ", "DELETE", "helpdeskbaseUrl", "api/helpdesk/faqs/{id}"),
        ]},
        {"name": "4. Escalation Matrix", "item": [
        create_request("Get All Escalation Matrices", "GET", "helpdeskbaseUrl", "api/helpdesk/escalation-matrix"),
        create_request("Get Escalation Matrix by ID", "GET", "helpdeskbaseUrl", "api/helpdesk/escalation-matrix/{id}"),
        create_request("Get Escalation Matrix by Service", "GET", "helpdeskbaseUrl", "api/helpdesk/escalation-matrix/service/{service}",
            path_var_defaults={"service": "ASSET_SERVICE"}),
        create_request("Create Escalation Matrix", "POST", "helpdeskbaseUrl", "api/helpdesk/escalation-matrix",
            "EscalationMatrixRequest: relatedService, priority, supportLevel, initialAssignmentLevel, responseTimeMinutes, resolutionTimeMinutes",
            {"mode": "raw", "raw": json.dumps({
                "relatedService": "ASSET_SERVICE",
                "priority": "HIGH",
                "supportLevel": "L1",
                "initialAssignmentLevel": "L1",
                "responseTimeMinutes": 30,
                "resolutionTimeMinutes": 240
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get Escalation Matrix by Service/Priority", "GET", "helpdeskbaseUrl",
            "api/helpdesk/escalation-matrix/service/{service}/priority/{priority}",
            "Get active matrix for service and priority",
            path_var_defaults={"service": "ASSET_SERVICE", "priority": "HIGH"}),
        create_request("Update Escalation Matrix", "PUT", "helpdeskbaseUrl", "api/helpdesk/escalation-matrix/{id}",
            "Update escalation matrix",
            {"mode": "raw", "raw": json.dumps({
                "relatedService": "ASSET_SERVICE",
                "priority": "HIGH",
                "supportLevel": "L1",
                "initialAssignmentLevel": "L1",
                "responseTimeMinutes": 30,
                "resolutionTimeMinutes": 240
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Delete Escalation Matrix", "DELETE", "helpdeskbaseUrl", "api/helpdesk/escalation-matrix/{id}"),
        ]},
        {"name": "5. Knowledge Base", "item": [
        create_request("Create Knowledge Entry", "POST", "helpdeskbaseUrl", "api/helpdesk/knowledge",
            "ServiceKnowledgeRequest: service, topic, content, category",
            {"mode": "raw", "raw": json.dumps({
                "service": "ASSET_SERVICE",
                "topic": "Asset Creation",
                "content": "To create an asset, use POST /api/asset/v1/assets",
                "category": "Asset Management"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get All Knowledge", "GET", "helpdeskbaseUrl", "api/helpdesk/knowledge"),
        create_request("Get Knowledge by ID", "GET", "helpdeskbaseUrl", "api/helpdesk/knowledge/{id}"),
        create_request("Get Knowledge by Service", "GET", "helpdeskbaseUrl", "api/helpdesk/knowledge/service/{service}",
            path_var_defaults={"service": "ASSET_SERVICE"}),
        create_request("Search Knowledge", "GET", "helpdeskbaseUrl", "api/helpdesk/knowledge/service/{service}/search",
            "Query: keyword",
            path_var_defaults={"service": "ASSET_SERVICE"},
            params=[{"key": "keyword", "value": "asset"}]),
        create_request("Update Knowledge", "PUT", "helpdeskbaseUrl", "api/helpdesk/knowledge/{id}",
            "Update knowledge entry",
            {"mode": "raw", "raw": json.dumps({
                "service": "ASSET_SERVICE",
                "topic": "Updated Topic",
                "content": "Updated content",
                "category": "Asset Management"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Delete Knowledge", "DELETE", "helpdeskbaseUrl", "api/helpdesk/knowledge/{id}"),
        ]},
        {"name": "6. Escalations & SLA", "item": [
        create_request("Escalate Issue", "POST", "helpdeskbaseUrl", "api/helpdesk/escalations/issue/{issueId}",
            "IssueEscalationRequest: toLevel (L1, L2, L3), escalationReason",
            {"mode": "raw", "raw": json.dumps({
                "toLevel": "L2",
                "escalationReason": "SLA breach imminent"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get Issue Escalations", "GET", "helpdeskbaseUrl", "api/helpdesk/escalations/issue/{issueId}",
            "Get escalation history for an issue"),
        create_request("Get SLA Tracking", "GET", "helpdeskbaseUrl", "api/helpdesk/sla/issue/{issueId}",
            "Get SLA tracking for issue"),
        create_request("Get SLA Breaches", "GET", "helpdeskbaseUrl", "api/helpdesk/sla/breaches"),
        create_request("Record First Response (SLA)", "POST", "helpdeskbaseUrl", "api/helpdesk/sla/issue/{issueId}/first-response",
            "Record first response time for SLA tracking"),
        create_request("Auto-Escalate Issue", "POST", "helpdeskbaseUrl", "api/helpdesk/escalations/issue/{issueId}/auto-escalate",
            "Trigger auto-escalation check"),
        ]},
        {"name": "7. Chatbot", "item": [
        create_request("Chatbot Message", "POST", "helpdeskbaseUrl", "api/helpdesk/chatbot/message",
            "ChatbotMessageRequest: message (required), sessionId (optional)",
            {"mode": "raw", "raw": json.dumps({
                "message": "How do I create an asset?",
                "sessionId": "{{sessionId}}"
            }, indent=2), "options": {"raw": {"language": "json"}}}),
        create_request("Get Chatbot Session History", "GET", "helpdeskbaseUrl", "api/helpdesk/chatbot/session/{sessionId}",
            "Get conversation history for session", path_var_defaults={"sessionId": "{{sessionId}}"}),
        ]},
    ]

def create_consolidated_collection():
    """Create consolidated Postman collection."""
    collection = {
        "info": {
            "_postman_id": str(uuid.uuid4()),
            "name": "Microservices - Complete API Collection",
            "description": """# Microservices Complete API Collection

Consolidated Postman collection for all microservices with Swagger/OpenAPI examples.

## Services Included
1. **Auth Service** - auth_openapi.yaml (localhost:8080)
2. **Notification Service** - notification_openapi.yaml (localhost:7071)
3. **Asset Service** - asset_openapi.yaml (localhost:7072)
4. **Helpdesk Service** - helpdesk_openapi.yaml (localhost:7074)

## Swagger/OpenAPI Specs
- asset-service/docs/swagger/asset_openapi.yaml
- auth-service/docs/swagger/auth_openapi.yaml
- notification-service/docs/swagger/notification_openapi.yaml
- helpdesk-service/docs/swagger/helpdesk_openapi.yaml

## Setup
1. Import the **Microservices - Consolidated Environment** file
2. Run **Auth Service > Login** to get bearerToken
3. Copy accessToken from response to bearerToken in environment
4. All other requests will use the token automatically

## Environment Variables
- authbaseUrl, assetbaseUrl, notificationbaseUrl, helpdeskbaseUrl
- bearerToken (from login)
- userId, username, projectType, etc.
""",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
            "_exporter_id": "microservices"
        },
        "item": [
            {"name": "1. Auth Service", "item": build_auth_modules()},
            {"name": "2. Notification Service", "item": build_notification_modules()},
            {"name": "3. Asset Service", "item": build_asset_modules()},
            {"name": "4. Helpdesk Service", "item": build_helpdesk_modules()},
        ],
        "variable": [
            {"key": "authbaseUrl", "value": BASE_URLS["auth"], "type": "string"},
            {"key": "assetbaseUrl", "value": BASE_URLS["asset"], "type": "string"},
            {"key": "notificationbaseUrl", "value": BASE_URLS["notification"], "type": "string"},
            {"key": "helpdeskbaseUrl", "value": BASE_URLS["helpdesk"], "type": "string"},
            {"key": "bearerToken", "value": "", "type": "string"},
        ]
    }
    return collection

def create_auth_environment():
    """Auth Service environment - Local (localhost:8080)."""
    return {
        "id": str(uuid.uuid4()),
        "name": "Auth Service - Local",
        "values": [
            create_env_value("authbaseUrl", BASE_URLS["auth"], "Auth Service base URL (localhost:8080)"),
            create_env_value("accessToken", "", "JWT from login", secret=True),
            create_env_value("bearerToken", "", "Same as accessToken", secret=True),
            create_env_value("refreshToken", "", "Refresh token", secret=True),
            create_env_value("projectType", "ASSET_SERVICE", "Project type"),
            create_env_value("userId", "1", "User ID"),
            create_env_value("username", "john.doe", "Username"),
            create_env_value("password", "SecurePass123!", "Password", secret=True),
            create_env_value("email", "john.doe@example.com", "Email"),
            create_env_value("mobile", "9876543210", "Mobile"),
            create_env_value("countryCode", "+91", "Country code"),
            create_env_value("pincode", "400001", "Pincode"),
            create_env_value("city", "Mumbai", "City"),
            create_env_value("state", "Maharashtra", "State"),
            create_env_value("country", "India", "Country"),
            create_env_value("address1", "Building A, Floor 5", "Address 1"),
            create_env_value("address2", "Tech Park, Sector 18", "Address 2"),
            create_env_value("address3", "Near Metro Station", "Address 3"),
            create_env_value("otp", "", "OTP code"),
            create_env_value("tcId", "1", "Terms & Conditions ID"),
            create_env_value("documentId", "1", "Document ID (for references)"),
            create_env_value("docType", "IMAGE", "Document type: IMAGE for profile photo, PDF, etc."),
        ],
        "_postman_variable_scope": "environment",
        "_postman_exported_at": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S.000Z"),
        "_postman_exported_using": "Postman/11.84.5"
    }

def create_asset_environment():
    """Asset Service environment - Consolidated (localhost:7072)."""
    return {
        "id": str(uuid.uuid4()),
        "name": "Asset Service - Consolidated Environment",
        "values": [
            create_env_value("assetbaseUrl", BASE_URLS["asset"], "Asset Service base URL (localhost:7072)"),
            create_env_value("bearerToken", "", "JWT from auth login", secret=True),
            create_env_value("accessToken", "", "Same as bearerToken", secret=True),
            create_env_value("userId", "1", "User ID"),
            create_env_value("username", "admin", "Username"),
            create_env_value("projectType", "ASSET_SERVICE", "Project type"),
            create_env_value("assetId", "1", "Asset ID"),
            create_env_value("categoryId", "1", "Category ID"),
            create_env_value("subCategoryId", "1", "SubCategory ID"),
            create_env_value("makeId", "1", "Make ID"),
            create_env_value("modelId", "1", "Model ID"),
            create_env_value("vendorId", "1", "Vendor ID"),
            create_env_value("outletId", "1", "Outlet ID"),
            create_env_value("componentId", "1", "Component ID"),
            create_env_value("warrantyId", "1", "Warranty ID"),
            create_env_value("amcId", "1", "AMC ID"),
            create_env_value("documentId", "1", "Document ID"),
            create_env_value("targetUserId", "2", "Target user ID"),
            create_env_value("targetUsername", "user1", "Target username"),
            create_env_value("entityType", "ASSET", "Entity type"),
            create_env_value("entityId", "1", "Entity ID"),
            create_env_value("scanValue", "AST-LAP-001", "QR/Barcode scan value"),
            create_env_value("violationId", "1", "Violation ID"),
            create_env_value("ruleId", "1", "Rule ID"),
            create_env_value("searchKeyword", "laptop", "Search keyword"),
            create_env_value("filename", "example.pdf", "Filename"),
            create_env_value("docType", "PDF", "Document type: PDF, IMAGE, RECEIPT, etc."),
        ],
        "_postman_variable_scope": "environment",
        "_postman_exported_at": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S.000Z"),
        "_postman_exported_using": "Postman/11.84.5"
    }

def create_notification_environment():
    """Notification Service environment - Local (localhost:7071)."""
    return {
        "id": str(uuid.uuid4()),
        "name": "Notification Service - Local",
        "values": [
            create_env_value("notificationbaseUrl", BASE_URLS["notification"], "Notification Service base URL (localhost:7071)"),
            create_env_value("bearerToken", "", "JWT from auth login", secret=True),
            create_env_value("accessToken", "", "Same as bearerToken", secret=True),
            create_env_value("userId", "1", "User ID"),
            create_env_value("projectType", "ASSET_MGMT", "Project type"),
            create_env_value("channel", "SMS", "Channel: SMS, EMAIL, WHATSAPP, INAPP"),
            create_env_value("templateCode", "OTP_SMS", "Template code"),
            create_env_value("recipient", "+919876543210", "Recipient (phone/email/userId)"),
            create_env_value("mobile", "+919876543210", "Mobile for SMS/WhatsApp"),
            create_env_value("email", "user@example.com", "Email"),
            create_env_value("notificationId", "1", "Notification ID"),
            create_env_value("documentId", "1", "Document ID (for document-related templates)"),
            create_env_value("docType", "PDF", "Document type: PDF, IMAGE, RECEIPT, etc."),
        ],
        "_postman_variable_scope": "environment",
        "_postman_exported_at": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S.000Z"),
        "_postman_exported_using": "Postman/11.84.5"
    }

def create_helpdesk_environment():
    """Helpdesk Service environment - Local (localhost:7074)."""
    return {
        "id": str(uuid.uuid4()),
        "name": "Helpdesk Service - Local",
        "values": [
            create_env_value("helpdeskbaseUrl", BASE_URLS["helpdesk"], "Helpdesk Service base URL (localhost:7074)"),
            create_env_value("bearerToken", "", "JWT from auth login", secret=True),
            create_env_value("authbaseUrl", BASE_URLS["auth"], "Auth Service URL (for login)"),
            create_env_value("issueId", "1", "Issue ID"),
            create_env_value("queryId", "1", "Query ID"),
            create_env_value("faqId", "1", "FAQ ID"),
            create_env_value("knowledgeId", "1", "Knowledge ID"),
            create_env_value("escalationMatrixId", "1", "Escalation Matrix ID"),
            create_env_value("sessionId", "session-123", "Chatbot session ID"),
            create_env_value("relatedService", "ASSET_SERVICE", "Related service"),
            create_env_value("issuePriority", "HIGH", "Issue priority"),
            create_env_value("issueStatus", "OPEN", "Issue status"),
            create_env_value("documentId", "1", "Document ID (for attachment references)"),
            create_env_value("docType", "PDF", "Document type: PDF, IMAGE, RECEIPT, etc."),
        ],
        "_postman_variable_scope": "environment",
        "_postman_exported_at": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S.000Z"),
        "_postman_exported_using": "Postman/11.84.5"
    }

def create_individual_collections():
    """Create individual service collections."""
    auth_coll = {
        "info": {
            "_postman_id": str(uuid.uuid4()),
            "name": "1. Auth Service API - Complete Collection",
            "description": "Postman collection for Auth Service API. Swagger: auth-service/docs/swagger/auth_openapi.yaml. Base URL: localhost:8080",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
        },
        "item": build_auth_modules(),
        "variable": [
            {"key": "authbaseUrl", "value": BASE_URLS["auth"], "type": "string"},
            {"key": "bearerToken", "value": "", "type": "string"},
            {"key": "documentId", "value": "1", "type": "string"},
            {"key": "docType", "value": "IMAGE", "type": "string"},
        ]
    }
    asset_coll = {
        "info": {
            "_postman_id": str(uuid.uuid4()),
            "name": "2. Asset Service - Complete API Collection",
            "description": "Complete Postman collection for Asset Management Service. Swagger: asset-service/docs/swagger/asset_openapi.yaml. Base URL: localhost:7072",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
        },
        "item": build_asset_modules(),
        "variable": [
            {"key": "assetbaseUrl", "value": BASE_URLS["asset"], "type": "string"},
            {"key": "bearerToken", "value": "", "type": "string"},
            {"key": "documentId", "value": "1", "type": "string"},
            {"key": "docType", "value": "PDF", "type": "string"},
        ]
    }
    notif_coll = {
        "info": {
            "_postman_id": str(uuid.uuid4()),
            "name": "3. Notification Service API - Complete Collection",
            "description": "Postman collection for Notification Service. Swagger: notification-service/docs/swagger/notification_openapi.yaml. Base URL: localhost:7071. Use bearerToken or accessToken.",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
        },
        "item": build_notification_modules(),
        "variable": [
            {"key": "notificationbaseUrl", "value": BASE_URLS["notification"], "type": "string"},
            {"key": "bearerToken", "value": "", "type": "string"},
            {"key": "documentId", "value": "1", "type": "string"},
            {"key": "docType", "value": "PDF", "type": "string"},
        ]
    }
    helpdesk_coll = {
        "info": {
            "_postman_id": str(uuid.uuid4()),
            "name": "4. Helpdesk Service - Complete API Collection",
            "description": "Complete Postman collection for Helpdesk Service. Swagger: helpdesk-service/docs/swagger/helpdesk_openapi.yaml. Base URL: localhost:7074",
            "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
        },
        "item": build_helpdesk_modules(),
        "variable": [
            {"key": "helpdeskbaseUrl", "value": BASE_URLS["helpdesk"], "type": "string"},
            {"key": "bearerToken", "value": "", "type": "string"},
            {"key": "documentId", "value": "1", "type": "string"},
            {"key": "docType", "value": "PDF", "type": "string"},
        ]
    }
    return {
        "1. Auth_Service_API.postman_collection.json": auth_coll,
        "2. Asset Service - Complete API Collection.postman_collection.json": asset_coll,
        "3. Notification_Service_API.postman_collection.json": notif_coll,
        "4. Helpdesk_Service_Complete_API_Collection.postman_collection.json": helpdesk_coll,
    }

def main():
    out_dir = "Deployment/Postman Collection"
    
    # Consolidated
    env = create_consolidated_environment()
    with open(f"{out_dir}/Microservices_Consolidated_Environment.postman_environment.json", "w") as f:
        json.dump(env, f, indent=2)
    print("✅ Microservices_Consolidated_Environment.postman_environment.json")
    
    coll = create_consolidated_collection()
    with open(f"{out_dir}/Microservices_Complete_API_Collection.postman_collection.json", "w") as f:
        json.dump(coll, f, indent=2)
    print("✅ Microservices_Complete_API_Collection.postman_collection.json")
    
    # Individual collections
    for filename, collection in create_individual_collections().items():
        with open(f"{out_dir}/{filename}", "w") as f:
            json.dump(collection, f, indent=2)
        print(f"✅ {filename}")
    
    # Individual environments
    envs = [
        ("Auth Service - Local.postman_environment.json", create_auth_environment()),
        ("Auth_Service_Environment.postman_environment.json", create_auth_environment()),
        ("Asset Service - Consolidated Environment.postman_environment.json", create_asset_environment()),
        ("Notification Service - Local.postman_environment.json", create_notification_environment()),
        ("Helpdesk Service - Local.postman_environment.json", create_helpdesk_environment()),
    ]
    for filename, env_obj in envs:
        if "Auth_Service_Environment" in filename:
            env_obj["name"] = "Auth Service - Environment"
        with open(f"{out_dir}/{filename}", "w") as f:
            json.dump(env_obj, f, indent=2)
        print(f"✅ {filename}")
    
    print("\n🎉 All Postman collections and environments revised!")

if __name__ == "__main__":
    main()
