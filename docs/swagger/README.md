# API Documentation (Swagger/OpenAPI)

This directory contains OpenAPI 3.0 specification files for all microservices in the system.

## Files

- **auth-service/docs/swagger/openapi.yaml** - Authentication and Authorization Service API
- **notification-service/docs/swagger/openapi.yaml** - Notification Service API
- **asset-service/docs/swagger/openapi.yaml** - Asset Management Service API

## Viewing the Documentation

### Option 1: Swagger UI (Recommended)

1. **Install Swagger UI** (if not already installed):
   ```bash
   npm install -g swagger-ui-serve
   ```

2. **View a specific service**:
   ```bash
   # Auth Service
   swagger-ui-serve auth-service/docs/swagger/openapi.yaml

   # Notification Service
   swagger-ui-serve notification-service/docs/swagger/openapi.yaml

   # Asset Service
   swagger-ui-serve asset-service/docs/swagger/openapi.yaml
   ```

3. **Access Swagger UI**:
   Open your browser and navigate to `http://localhost:3000`

### Option 2: Swagger Editor (Online)

1. Go to [Swagger Editor](https://editor.swagger.io/)
2. Copy the contents of any `openapi.yaml` file
3. Paste into the editor
4. View the interactive documentation

### Option 3: Postman Import

1. Open Postman
2. Click **Import**
3. Select the `openapi.yaml` file
4. Postman will automatically generate a collection with all endpoints

### Option 4: SpringDoc OpenAPI (If Integrated)

If SpringDoc OpenAPI is integrated in the services, you can access the documentation at:

- **Auth Service**: `http://localhost:8080/swagger-ui.html`
- **Notification Service**: `http://localhost:8081/swagger-ui.html`
- **Asset Service**: `http://localhost:8082/swagger-ui.html`

## Service Overview

### Auth Service

**Base URL**: `http://localhost:8080`

**Main Endpoints**:
- `/api/auth/register` - User registration
- `/api/auth/login` - User authentication (multiple methods)
- `/api/auth/refresh` - Token refresh
- `/api/users/me` - Get current user profile
- `/api/admin/users` - Admin user management
- `/api/auth/v1/project-types` - Project type management

**Authentication Methods Supported**:
- Password
- OTP (One-Time Password)
- MPIN
- RSA Signature
- WebAuthn/Passkey
- Auth Code

### Notification Service

**Base URL**: `http://localhost:8081`

**Main Endpoints**:
- `/api/notifications` - Send notifications

**Channels Supported**:
- SMS
- Email
- WhatsApp
- In-App

**Features**:
- Template-based notifications
- Dynamic variable substitution
- Multi-channel support
- Asynchronous processing

### Asset Service

**Base URL**: `http://localhost:8082`

**Main Endpoints**:
- `/api/asset/v1/assets` - Asset CRUD operations
- `/api/asset/v1/categories` - Category management
- `/api/asset/v1/makes` - Make management
- `/api/asset/v1/models` - Model management
- `/api/asset/v1/vendors` - Vendor management
- `/api/asset/v1/userlinks` - User-Asset linking
- `/api/asset/v1/compliance` - Compliance checking
- `/api/asset/v1/documents` - Document management
- `/api/asset/v1/warranty` - Warranty management
- `/api/asset/v1/amc` - AMC management

**Features**:
- Comprehensive asset lifecycle management
- Master data management
- Bulk upload (JSON and Excel)
- Compliance validation
- Document management
- User-Asset assignment

## Authentication

Most endpoints require authentication using Bearer tokens:

```http
Authorization: Bearer <your-jwt-token>
```

To get a token:
1. Register a user via `/api/auth/register`
2. Login via `/api/auth/login`
3. Use the `accessToken` from the response

## Common Response Format

Most endpoints return responses in the following format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

## Error Responses

Error responses typically follow this format:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

Common HTTP status codes:
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Not Found
- `500` - Internal Server Error

## Testing with Swagger UI

1. **Set up authentication**:
   - Click the "Authorize" button at the top
   - Enter your Bearer token: `Bearer <your-token>`
   - Click "Authorize"

2. **Test endpoints**:
   - Expand any endpoint
   - Click "Try it out"
   - Fill in the required parameters
   - Click "Execute"
   - View the response

## Generating Client SDKs

You can generate client SDKs from the OpenAPI specifications:

### Using OpenAPI Generator

```bash
# Generate Java client
openapi-generator generate -i auth-service/docs/swagger/openapi.yaml -g java -o ./clients/auth-service-java

# Generate TypeScript client
openapi-generator generate -i asset-service/docs/swagger/openapi.yaml -g typescript-axios -o ./clients/asset-service-ts

# Generate Python client
openapi-generator generate -i notification-service/docs/swagger/openapi.yaml -g python -o ./clients/notification-service-python
```

### Using Swagger Codegen

```bash
swagger-codegen generate -i auth-service/docs/swagger/openapi.yaml -l java -o ./clients/auth-service-java
```

## Updating Documentation

When adding new endpoints or modifying existing ones:

1. Update the corresponding `openapi.yaml` file
2. Follow the OpenAPI 3.0 specification
3. Include:
   - Endpoint path and method
   - Request/response schemas
   - Parameters
   - Authentication requirements
   - Example requests/responses
   - Error responses

## Validation

Validate your OpenAPI files:

```bash
# Using swagger-cli
npm install -g @apidevtools/swagger-cli
swagger-cli validate auth-service/docs/swagger/openapi.yaml

# Using openapi-validator
npm install -g openapi-validator
openapi-validator auth-service/docs/swagger/openapi.yaml
```

## Additional Resources

- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [Swagger Editor](https://editor.swagger.io/)

## Support

For questions or issues with the API documentation, please contact the development team.
