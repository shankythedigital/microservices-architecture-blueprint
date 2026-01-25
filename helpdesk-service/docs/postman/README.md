# Helpdesk Service - Postman Collection & Environment

This directory contains the complete Postman collection and environment files for the Helpdesk Service API.

## Files

- **Helpdesk_Service_Complete_API_Collection.postman_collection.json** - Complete API collection with all endpoints
- **Helpdesk_Service_Environment.postman_environment.json** - Environment variables for local development

## Quick Start

### 1. Import Collection and Environment

1. Open Postman
2. Click **Import** button
3. Import both files:
   - `Helpdesk_Service_Complete_API_Collection.postman_collection.json`
   - `Helpdesk_Service_Environment.postman_environment.json`

### 2. Select Environment

1. In Postman, select the **"Helpdesk Service - Local"** environment from the environment dropdown (top right)
2. This will enable all environment variables for the collection

### 3. Get JWT Token

Before making API calls, you need to authenticate and get a JWT token:

1. Use the Auth Service login endpoint:
   ```
   POST http://localhost:8081/api/auth/login
   Body: {
     "username": "your-username",
     "password": "your-password"
   }
   ```

2. Copy the `accessToken` from the response

3. Update the `bearerToken` variable in the environment:
   - Click on the environment name in Postman
   - Find `bearerToken` variable
   - Paste your token as the value
   - Save the environment

### 4. Start Testing

Now you can use any request in the collection. The `bearerToken` will be automatically included in the Authorization header.

## Environment Variables

The environment includes the following variables:

### Service URLs
- `helpdeskbaseUrl` - Base URL for Helpdesk Service (default: http://localhost:8084)
- `authbaseUrl` - Base URL for Auth Service (default: http://localhost:8081)

### Authentication
- `bearerToken` - JWT Bearer token (set this after login)

### Common IDs
- `issueId` - Issue ID for operations
- `queryId` - Query ID for operations
- `faqId` - FAQ ID for operations
- `knowledgeId` - Knowledge entry ID
- `escalationMatrixId` - Escalation Matrix ID
- `sessionId` - Chatbot session ID

### Enum Values
- `relatedService` - Related service enum (ASSET_SERVICE, HELPDESK_SERVICE, etc.)
- `issuePriority` - Issue priority (LOW, MEDIUM, HIGH, CRITICAL)
- `issueStatus` - Issue status (OPEN, IN_PROGRESS, RESOLVED, CLOSED)
- `queryStatus` - Query status (PENDING, ANSWERED, CLOSED)
- `escalationLevel` - Escalation level (LEVEL_1, LEVEL_2, LEVEL_3)

### Search & Filter
- `searchKeyword` - Search keyword for FAQ/Knowledge search
- `faqCategory` - FAQ category name

### User Emails
- `reportedBy` - Email of user reporting issue
- `assignedTo` - Email of support agent
- `resolvedBy` - Email of agent resolving issue
- `escalatedBy` - Email of agent escalating issue
- `answeredBy` - Email of agent answering query

### SLA Configuration
- `firstResponseTimeMinutes` - First response time in minutes
- `resolutionTimeMinutes` - Resolution time in minutes
- `level1EscalationMinutes` - Level 1 escalation time
- `level2EscalationMinutes` - Level 2 escalation time
- `level3EscalationMinutes` - Level 3 escalation time

## Collection Structure

The collection is organized into the following folders:

1. **Issues** - Issue management endpoints (create, update, assign, resolve, close)
2. **Escalations** - Issue escalation endpoints
3. **Escalation Matrix** - SLA and escalation matrix configuration
4. **SLA** - SLA tracking and monitoring
5. **FAQs** - Frequently Asked Questions management
6. **Queries** - User query management
7. **Chatbot** - Chatbot interaction endpoints
8. **Service Knowledge** - Knowledge base management

## Prerequisites

- Helpdesk Service running on `http://localhost:8084`
- Auth Service running on `http://localhost:8081` (for authentication)
- Valid user credentials for login

## Notes

- All endpoints require JWT authentication via Bearer token
- Update the `bearerToken` variable after logging in
- You can modify any environment variable values as needed for your testing
- The environment variables are automatically used in all collection requests

## Troubleshooting

### 401 Unauthorized
- Make sure you've set the `bearerToken` variable with a valid JWT token
- Check if the token has expired (tokens typically expire after a certain time)

### Connection Refused
- Verify that the Helpdesk Service is running on port 8084
- Check the `helpdeskbaseUrl` variable is correct

### 404 Not Found
- Ensure the service is running and the endpoint path is correct
- Check the API documentation for the correct endpoint paths

