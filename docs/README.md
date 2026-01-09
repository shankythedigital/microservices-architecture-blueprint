# Documentation Index
## Asset Lifecycle Management System

This directory contains comprehensive documentation for all three microservices.

---

## 📚 Documentation Files

### 1. Technical Functional Document
**File:** `TECHNICAL_FUNCTIONAL_DOCUMENT.md`

Comprehensive technical and functional documentation covering:
- System overview and architecture
- Service details (Auth, Asset, Notification)
- API documentation with examples
- Data models
- Security implementation
- Integration points
- Deployment guide
- Testing guide
- Troubleshooting

**Use this for:**
- Understanding system architecture
- API reference with examples
- Deployment and configuration
- Troubleshooting issues

---

### 2. API Documentation Guide
**File:** `API_DOCUMENTATION_GUIDE.md`

Complete guide for using Swagger/OpenAPI and Postman collections:
- How to access Swagger UI
- How to import and use Postman collections
- Environment variable setup
- API testing workflow
- Common issues and solutions

**Use this for:**
- Setting up API testing
- Understanding Postman collections
- Quick API reference

---

### 3. Swagger/OpenAPI Documentation

#### Auth Service
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI YAML**: `../auth-service/docs/swagger/openapi.yaml`
- **API Docs**: `http://localhost:8080/api-docs`

#### Asset Service
- **Swagger UI**: `http://localhost:8083/swagger-ui.html`
- **OpenAPI YAML**: `../asset-service/docs/swagger/openapi.yaml`
- **API Docs**: `http://localhost:8083/api-docs`

#### Notification Service
- **Swagger UI**: `http://localhost:8082/swagger-ui.html`
- **OpenAPI YAML**: `../notification-service/docs/swagger/openapi.yaml`
- **API Docs**: `http://localhost:8082/api-docs`

**Use these for:**
- Interactive API exploration
- API specification reference
- Code generation

---

### 4. Postman Collections

#### Auth Service
- **Collection**: `../auth-service/docs/postman/Auth_Service_API.postman_collection.json`
- **Includes**: Registration, Login, OTP, Credentials, MPIN, T&C, Project Types, Admin

#### Asset Service
- **Main Collection**: `../asset-service/docs/postman/Asset_Service_API.postman_collection.json`
- **Master Data API**: `../asset-service/docs/postman/Master_Data_API.postman_collection.json`
- **Complete Asset Creation**: `../asset-service/docs/postman/Complete_Asset_Creation_API.postman_collection.json`
- **User Link Controller**: `../asset-service/docs/postman/UserLinkController.postman_collection.json`
- **Warranty & AMC**: `../asset-service/docs/postman/Warranty_AMC_Controllers.postman_collection.json`
- **Compliance Agent**: `../asset-service/docs/postman/Compliance_Agent_API.postman_collection.json`

#### Notification Service
- **Collection**: `../notification-service/docs/postman/Notification_Service_API.postman_collection.json` (to be created)

**Use these for:**
- API testing
- Integration testing
- API exploration
- Sharing API examples

---

## 🚀 Quick Start

### 1. Read the Documentation

Start with:
1. **Technical Functional Document** - Understand the system
2. **API Documentation Guide** - Learn how to use the APIs

### 2. Access Swagger UI

1. Start the services
2. Open Swagger UI URLs in browser
3. Explore APIs interactively

### 3. Import Postman Collections

1. Open Postman
2. Import collections from service `docs/postman/` directories
3. Set up environment variables
4. Start testing APIs

### 4. Test APIs

Follow the workflow in **API Documentation Guide**:
1. Register a user
2. Login to get access token
3. Test protected endpoints
4. Test asset operations
5. Test notifications

---

## 📋 Documentation Structure

```
docs/
├── README.md (this file)
├── TECHNICAL_FUNCTIONAL_DOCUMENT.md
├── API_DOCUMENTATION_GUIDE.md
└── swagger/
    ├── README.md
    ├── INTEGRATION_GUIDE.md
    └── TROUBLESHOOTING.md

auth-service/docs/
├── swagger/
│   └── openapi.yaml
└── postman/
    └── Auth_Service_API.postman_collection.json

asset-service/docs/
├── swagger/
│   └── openapi.yaml
└── postman/
    ├── Asset_Service_API.postman_collection.json
    ├── Master_Data_API.postman_collection.json
    ├── Complete_Asset_Creation_API.postman_collection.json
    └── ... (other collections)

notification-service/docs/
├── swagger/
│   └── openapi.yaml
└── postman/
    └── Notification_Service_API.postman_collection.json
```

---

## 🔍 Finding Information

### Need to understand the system?
→ Read `TECHNICAL_FUNCTIONAL_DOCUMENT.md`

### Need to test APIs?
→ Read `API_DOCUMENTATION_GUIDE.md` and use Postman collections

### Need API specifications?
→ Use Swagger UI or OpenAPI YAML files

### Need code examples?
→ Check Postman collections for request/response examples

### Need deployment help?
→ See "Deployment" section in `TECHNICAL_FUNCTIONAL_DOCUMENT.md`

### Need troubleshooting?
→ See "Troubleshooting" section in `TECHNICAL_FUNCTIONAL_DOCUMENT.md`

---

## 📝 Documentation Updates

When updating documentation:

1. **Technical Changes**: Update `TECHNICAL_FUNCTIONAL_DOCUMENT.md`
2. **API Changes**: Update OpenAPI YAML files and Postman collections
3. **New Features**: Add to relevant documentation files
4. **Version**: Update version numbers and dates

---

## 🤝 Contributing

When adding new features:

1. Update OpenAPI specifications
2. Update Postman collections
3. Update technical functional document
4. Update API documentation guide if needed

---

## 📞 Support

For questions or issues:
1. Check documentation files
2. Review service logs
3. Check Swagger UI for API details
4. Contact development team

---

**Last Updated:** 2024-01-15  
**Version:** 1.0.0

