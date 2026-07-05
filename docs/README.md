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

### 2. Epics, Features & User Stories
**File:** `EPICS_FEATURES_USER_STORIES.md`

Detailed product backlog covering all platform components:
- common-service, auth-service, notification-service, helpdesk-service, asset-service, keeply_flutter_app
- 7 epics, 47 features, 152 user stories with acceptance criteria
- Personas, architecture context, implementation roadmap, traceability matrix

**Use this for:**
- Sprint planning and backlog grooming
- Requirements traceability from business goals to APIs
- Onboarding product owners, BA, QA, and developers

**Related:** [BUSINESS_REQUIREMENTS_DOCUMENT.md](./BUSINESS_REQUIREMENTS_DOCUMENT.md) | Word: [📘 BRD.docx](./📘%20Business%20Requirements%20Document%20(BRD).docx)

---

### 3. Business Requirements Document (BRD)
**File:** `BUSINESS_REQUIREMENTS_DOCUMENT.md` | **Word:** `📘 Business Requirements Document (BRD).docx`

Authoritative business requirements for the Keeply platform:
- Executive summary, problem statement, business objectives & KPIs
- Stakeholders, scope, personas, 60+ numbered business requirements (BR-xxx)
- Business rules, non-functional requirements, compliance (DPDPA/PDPA)
- User journeys, roadmap phasing, risks, traceability to epics

**Use this for:**
- Executive and stakeholder alignment
- Compliance and legal review
- BRD → epic → story traceability for BA and product owners

**Related:** [EPICS_FEATURES_USER_STORIES.md](./EPICS_FEATURES_USER_STORIES.md) | [SRS/FRD](./SOFTWARE_REQUIREMENTS_SPECIFICATION_FRD.md)

---

### 4. Software Requirements Specification (SRS / FRD)
**File:** `SOFTWARE_REQUIREMENTS_SPECIFICATION_FRD.md` | **Word:** `📘 Software Requirements Specification (SRS _ FRD).docx`

Combined SRS and Functional Requirements Document:
- System overview, architecture, BR-xxx summary, FR-xxx by service
- Interface, data, security, NFR, and compliance requirements
- Traceability matrix (BR → FR → user stories), implementation phases

**Use this for:**
- Single reference for BA, QA, and engineering sign-off
- Test planning from FR-xxx requirements
- Linking business needs to technical delivery

**Related:** [BRD](./BUSINESS_REQUIREMENTS_DOCUMENT.md) | [Epics & Stories](./EPICS_FEATURES_USER_STORIES.md)

**Regenerate Word files:** `python3 docs/scripts/md_to_docx.py <input.md> <output.docx>`

---

### 5. API Documentation Guide
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

### 6. Swagger/OpenAPI Documentation

#### Auth Service
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI YAML**: `../auth-service/docs/swagger/openapi.yaml`
- **API Docs**: `http://localhost:8080/api-docs`

#### Asset Service
- **Swagger UI**: `http://localhost:7073/swagger-ui.html`
- **OpenAPI YAML**: `../asset-service/docs/swagger/openapi.yaml`
- **API Docs**: `http://localhost:7073/api-docs`

#### Notification Service
- **Swagger UI**: `http://localhost:7072/swagger-ui.html`
- **OpenAPI YAML**: `../notification-service/docs/swagger/openapi.yaml`
- **API Docs**: `http://localhost:7072/api-docs`

**Use these for:**
- Interactive API exploration
- API specification reference
- Code generation

---

### 7. Postman Collections

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

