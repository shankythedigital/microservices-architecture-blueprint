# Helpdesk Service

A comprehensive helpdesk management microservice with chatbot integration for handling issues, FAQs, queries, and providing support for auth-service, notification-service, asset-service, and upcoming projects.

## Features

- **Issue Management**: Raise, track, assign, and resolve issues across all services
- **FAQ Management**: Create, search, and manage frequently asked questions
- **Query Handling**: Submit queries and get answers from support agents
- **Chatbot Integration**: AI-powered chatbot for instant support and troubleshooting
- **Service Knowledge Base**: Comprehensive knowledge repository about all services
- **Multi-Service Support**: Complete knowledge of auth-service, notification-service, and asset-service

## Technology Stack

- **Java 17**
- **Spring Boot 3.3.2**
- **Spring Data JPA**
- **MySQL**
- **Spring Security with JWT**
- **OpenFeign** (for inter-service communication)
- **Swagger/OpenAPI** (for API documentation)

## Project Structure

```
helpdesk-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/helpdesk/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── enums/           # Enumeration types
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── service/         # Business logic services
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application.yml  # Application configuration
│   │       └── db/migration/    # Database migration scripts
│   └── test/
└── pom.xml
```

## API Endpoints

### Issue Management
- `POST /api/helpdesk/issues` - Create a new issue
- `GET /api/helpdesk/issues` - Get all issues
- `GET /api/helpdesk/issues/{id}` - Get issue by ID
- `GET /api/helpdesk/issues/status/{status}` - Get issues by status
- `GET /api/helpdesk/issues/service/{service}` - Get issues by service
- `GET /api/helpdesk/issues/my-issues` - Get my issues
- `PATCH /api/helpdesk/issues/{id}/status` - Update issue status
- `PATCH /api/helpdesk/issues/{id}/assign` - Assign issue
- `POST /api/helpdesk/issues/{id}/resolve` - Resolve issue
- `PATCH /api/helpdesk/issues/{id}/close` - Close issue

### FAQ Management
- `POST /api/helpdesk/faqs` - Create a new FAQ
- `GET /api/helpdesk/faqs` - Get all FAQs
- `GET /api/helpdesk/faqs/{id}` - Get FAQ by ID
- `GET /api/helpdesk/faqs/service/{service}` - Get FAQs by service
- `GET /api/helpdesk/faqs/category/{category}` - Get FAQs by category
- `GET /api/helpdesk/faqs/search?keyword={keyword}` - Search FAQs
- `PUT /api/helpdesk/faqs/{id}` - Update FAQ
- `POST /api/helpdesk/faqs/{id}/helpful` - Mark FAQ as helpful
- `DELETE /api/helpdesk/faqs/{id}` - Delete FAQ

### Query Management
- `POST /api/helpdesk/queries` - Create a new query
- `GET /api/helpdesk/queries` - Get all queries
- `GET /api/helpdesk/queries/{id}` - Get query by ID
- `GET /api/helpdesk/queries/status/{status}` - Get queries by status
- `GET /api/helpdesk/queries/service/{service}` - Get queries by service
- `GET /api/helpdesk/queries/my-queries` - Get my queries
- `POST /api/helpdesk/queries/{id}/answer` - Answer a query
- `PATCH /api/helpdesk/queries/{id}/close` - Close query

### Chatbot
- `POST /api/helpdesk/chatbot/message` - Send message to chatbot
- `GET /api/helpdesk/chatbot/session/{sessionId}` - Get session history

### Service Knowledge
- `POST /api/helpdesk/knowledge` - Create knowledge entry
- `GET /api/helpdesk/knowledge` - Get all knowledge
- `GET /api/helpdesk/knowledge/{id}` - Get knowledge by ID
- `GET /api/helpdesk/knowledge/service/{service}` - Get knowledge by service
- `GET /api/helpdesk/knowledge/service/{service}/search?keyword={keyword}` - Search knowledge
- `PUT /api/helpdesk/knowledge/{id}` - Update knowledge
- `DELETE /api/helpdesk/knowledge/{id}` - Delete knowledge

## Configuration

### Database Setup
1. Create a MySQL database named `helpdeskdb`
2. Update `application.yml` with your database credentials

### Application Properties
Key configuration in `application.yml`:
- Server port: `8084`
- Database connection settings
- JWT security configuration
- Service URLs for auth-service, notification-service, asset-service

## Running the Service

1. **Build the project:**
   ```bash
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **Access Swagger UI:**
   ```
   http://localhost:8084/swagger-ui.html
   ```

## Authentication

Most endpoints require JWT authentication. Obtain a token from the auth-service:
```
POST http://localhost:8081/api/auth/login
```

Include the token in the Authorization header:
```
Authorization: Bearer <your-token>
```

## Service Independence

The helpdesk-service is designed to be independent of other services while maintaining complete knowledge about:
- **auth-service**: Authentication, login, user management
- **notification-service**: Notifications, alerts, email
- **asset-service**: Asset management, AMC, warranty, compliance
- **Upcoming projects**: Extensible for future services

The service uses Feign clients for optional inter-service communication but can operate independently with its knowledge base.

## Chatbot Features

The integrated chatbot can:
- Answer questions based on FAQs
- Provide information from the service knowledge base
- Guide users through troubleshooting steps
- Help raise issues
- Provide context-aware responses based on service-related keywords

## Database Schema

The service uses the following main entities:
- **Issue**: Issue tracking and resolution
- **FAQ**: Frequently asked questions
- **Query**: User queries and answers
- **ChatbotSession**: Chatbot conversation sessions
- **ChatbotMessage**: Individual chatbot messages
- **ServiceKnowledge**: Knowledge base entries for each service

## Development

### Adding New Service Knowledge
Use the Service Knowledge API to add information about new services or update existing knowledge.

### Extending Chatbot
The chatbot logic in `ChatbotService` can be extended to:
- Integrate with external AI services
- Add more sophisticated natural language processing
- Implement learning from resolved issues

## License

Apache 2.0

