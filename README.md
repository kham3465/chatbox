# ChatBox Backend Service

## Overview
This is a Spring Boot backend service for user authentication, JWT-based security, and content generation via Google Gemini.

It provides:
- User registration and login
- JWT token generation and validation
- Public AI query endpoint for Gemini-powered content
- Swagger/OpenAPI documentation support
- H2 in-memory database by default (configurable for MySQL)
- SMTP mail support and file upload configuration

## Tech stack
- Java 17
- Spring Boot 3.1.0-RC1
- Spring Security
- Spring Data JPA
- Spring Web
- Spring Validation
- Spring WebSocket
- Spring Mail
- H2 database (default)
- MySQL connector (runtime optional)
- JSON Web Tokens via jjwt
- Swagger/OpenAPI via springdoc-openapi-starter-webmvc-ui
- Lombok
- Log4j / Log4j iostreams
- RestTemplate + Jackson for Google Gemini API calls

## Tech stack details
- Java 17: the runtime and language level used for this project. Provides modern Java features and long-term support.
- Spring Boot 3.1.0-RC1: the framework that bootstraps the application, manages dependency configuration, and embeds the web server.
- Spring Security: secures the API endpoints, authenticates users, and integrates JWT-based stateless authentication.
- Spring Data JPA: simplifies database access with repository abstractions and object-relational mapping.
- Spring Web: provides REST API support, request routing, and the embedded Tomcat/Netty server.
- Spring Validation: validates incoming request payloads using annotations like `@Valid` and constraint annotations.
- Spring WebSocket: included for real-time communication support if future WebSocket features are added.
- Spring Mail: supports SMTP email sending when mail functionality is required.
- H2 Database: default in-memory database used for development and testing.
- MySQL Connector/J: optional JDBC driver for connecting to a MySQL database in production.
- JJWT (`io.jsonwebtoken`): library for generating, signing, and validating JWT access tokens.
- Springdoc OpenAPI: auto-generates OpenAPI documentation and serves Swagger UI for API exploration.
- Lombok: reduces boilerplate code by generating getters, setters, constructors, and builders at compile time.
- Log4j / Log4j iostreams: application logging and streaming utilities.
- RestTemplate + Jackson: used by `GeminiServiceImpl` to call Google Gemini and parse JSON responses.

### Additional libraries in the project
- `commons-codec`: helpers for encoding and decoding binary/ASCII formats.
- `httpclient` / `httpcore`: Apache HTTP client support for custom request handling.
- `okhttp`: alternative HTTP client library used for external API calls.
- `gson`: JSON serialization/deserialization helper.
- `commons-io`: utility helpers for I/O operations and file handling.

## Project structure
- com.vn.nhom2.AppChatBox - application entry point
- config/ - Spring security, OpenAPI, JWT, CORS, application config
- controller/ - REST API controllers
- service/ - business logic and integration services
- repository/ - JPA repositories
- entity/ - JPA entity model
- util/ - utility and response wrappers

## Purpose
This service is designed to act as a backend API for:
- user registration and authentication
- issuing JWT access tokens
- securing API routes with stateless JWT auth
- providing an AI content generation endpoint
- exposing OpenAPI documentation for client integration

## Configuration
Configuration lives in `src/main/resources/application.yml`.

### Database configuration
This project is configured to use H2 in-memory database by default. H2 is useful for development and testing because it requires no external database server.

Default H2 configuration in `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
```

If you need MySQL instead, update the datasource section and provide your MySQL credentials. Example MySQL configuration:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database_name?useSSL=false&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: your_mysql_user
    password: your_mysql_password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

Important MySQL notes:
- Create the database before starting the application.
- Use an appropriate user account with permissions to create tables and read/write data.
- `ddl-auto: update` will create/update tables automatically, but for production consider using `validate` or `none` and schema migrations instead.

### Other key settings
- `spring.servlet.multipart` - file upload limits
- `spring.mail` - SMTP settings for email support
- `application.security.jwt.secret-key` - JWT signing secret
- `application.security.jwt.expiration` - token lifetime in ms
- `server.port` - default port 3465
- `nhom2.openapi.dev-url` / `prod-url` - OpenAPI server URLs
- `nhom2.file.*` - allowed file extensions and limits
- `gemini.api-key` - API key used by `GeminiServiceImpl`

### Recommended production changes
- Replace `application.security.jwt.secret-key` with a secure Base64 secret.
- Do not commit real API keys or SMTP credentials into source control.
- Use environment variables or externalized configuration for secrets.
- Switch from H2 to MySQL by updating `spring.datasource` and using a proper production database.

## Running locally
### Prerequisites
- Java 17 installed
- Maven installed, or use the bundled Maven wrapper
- Internet access for Gemini API if using AI generation

### Start the app
From the project root:

Windows:
```powershell
.\\mvnw.cmd spring-boot:run
```

Linux/macOS:
```bash
./mvnw spring-boot:run
```

Or build and run the jar:
```bash
./mvnw clean package
java -jar target/nhom2-0.0.1-SNAPSHOT.jar
```

### Default server URL
- http://localhost:3465

## API Endpoints
### Authentication
- POST /api/v1/auth/register
  - Request body: RegisterRequest
  - Registers a new user
- POST /api/v1/auth/authenticate
  - Request body: AuthenticationRequest
  - Authenticates a user and returns AuthenticationResponse including a JWT

### Public AI endpoint
- GET /api/v1/public/ask?q={prompt}
  - Sends q to Google Gemini and returns the raw response body

### API docs
- OpenAPI JSON: /v3/api-docs
- Swagger UI: /swagger-ui/index.html

## Security behavior
- Stateless JWT security with JwtAuthenticationFilter
- SecurityConfiguration permits unauthenticated access to:
  - /api/v1/auth/**
  - /v3/api-docs/**
  - /swagger-ui/**
  - /api/v1/file/**
  - /api/v1/user/**
  - /api/v1/admin/**
  - /api/v1/public/**
- All other routes require authentication
- CORS is enabled for http://localhost:3465 and http://localhost:8080

## Important implementation details
- AuthenticationServiceImpl handles registration and login
- User entity implements UserDetails for Spring Security
- JwtService creates and validates JWT tokens
- GeminiServiceImpl posts prompt content to Google Gemini via REST

## Notes
- The current application.yml contains sensitive example values. Replace them before deploying.
- If you want MySQL instead of H2, configure the spring.datasource.url, username, password, and driver accordingly.
- springdoc-openapi configuration is in OpenAPIConfig.

## Useful commands
- ./mvnw clean package - build jar
- ./mvnw test - run tests
- ./mvnw spring-boot:run - run app directly

## License
No license is defined in this repository. Add a license file if this project will be shared publicly.