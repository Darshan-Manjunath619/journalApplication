# Journal Application

A Spring Boot REST API for creating and managing personal journal entries. Users can sign up, log in, receive a JWT token, and use that token to create, read, update, and delete their own journal entries.

The project also includes weather lookup with Redis caching, email support, admin APIs, and a scheduled weekly sentiment-analysis email flow.

## Tech Stack

- Java 17
- Spring Boot 3.5.4
- Spring Web
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- Redis
- Spring Mail
- Swagger/OpenAPI
- Maven
- Lombok

## Main Features

- Public signup and login APIs
- JWT-based authentication
- User-specific journal CRUD APIs
- Admin-only user listing API
- Weather API integration
- Redis caching for weather responses
- Email sending through SMTP
- Weekly scheduled sentiment analysis email
- Swagger API documentation

## Project Structure

```text
src/main/java/com/darshan/journalApplication
├── apiresponse
│   └── WeatherResponse.java
├── config
│   ├── RedisConfig.java
│   ├── SpringSecurity.java
│   └── SwaggerConfig.java
├── controller
│   ├── AdminController.java
│   ├── JournalEntryController.java
│   ├── PublicController.java
│   └── UserController.java
├── entity
│   ├── JournalEntry.java
│   └── User.java
├── filter
│   └── JwtFilter.java
├── repository
│   ├── JournalEntryRepository.java
│   └── UserEntryRepository.java
├── scheduler
│   └── UserScheduler.java
├── service
│   ├── EmailService.java
│   ├── JournalEntryService.java
│   ├── RedisService.java
│   ├── SentimentAnalysis.java
│   ├── UserDetailsImp.java
│   ├── UserEntryService.java
│   └── WeatherService.java
├── utils
│   └── JwtUtil.java
└── JournalApplication.java
```

## Dependencies

The dependencies are managed by Maven in `pom.xml`. You do not need to install each Java library manually. Maven downloads them automatically.

Main dependencies used:

- `spring-boot-starter-web` - builds REST APIs
- `spring-boot-starter-security` - authentication and authorization
- `spring-boot-starter-data-jpa` - database access through JPA/Hibernate
- `spring-boot-starter-data-redis` - Redis integration
- `spring-boot-starter-mail` - email sending
- `spring-boot-starter-test` - testing support
- `mysql-connector-java` - MySQL database driver
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - JWT token creation and validation
- `springdoc-openapi-starter-webmvc-ui` - Swagger UI
- `lombok` - reduces boilerplate code
- `flyway-core` - database migration support, currently disabled in config

## Required Software

Install these before running the application:

1. Java 17
2. Maven, or use the included Maven wrapper
3. MySQL Server
4. Redis Server
5. An API key from Weatherstack, or another configured weather provider
6. SMTP email credentials, such as a Gmail app password, if email sending is needed

## Configuration

The active profile is configured in:

```text
src/main/resources/application.yml
```

Current active profile:

```yaml
spring:
  profiles:
    active: dev
```

Development configuration is in:

```text
src/main/resources/application-dev.yml
```

Important configuration values:

```yaml
server:
  port: 8080
  servlet:
    context-path: /journal
```

Because of the context path, all APIs start with:

```text
http://localhost:8080/journal
```

Example:

```text
POST http://localhost:8080/journal/public/login
```

### MySQL Configuration

The app expects a MySQL database:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/JournalApplication
    username: root
    password: your_mysql_password
```

Create the database before running the app:

```sql
CREATE DATABASE JournalApplication;
```

Hibernate is configured with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

This means Hibernate will automatically create or update tables during development.

### Redis Configuration

Redis is used to cache weather data:

```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

Make sure Redis is running before testing weather-related APIs.

### Email Configuration

Email sending uses Spring Mail:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_app_password
```

Use environment variables or secret management for real credentials. Do not commit passwords or API keys to Git.

### Weather API Configuration

Weather API key:

```yaml
weather:
  api:
    key: your_weather_api_key
```

`WeatherService` calls Weatherstack and stores the response in Redis for 300 seconds.

## How to Run

From the project root:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Or, if Maven is installed globally:

```bash
mvn spring-boot:run
```

The app runs at:

```text
http://localhost:8080/journal
```

Health check:

```text
GET http://localhost:8080/journal/public/health-checkup
```

## Application Flow

### 1. User Signup

Endpoint:

```text
POST /journal/public/signup
```

Request body:

```json
{
  "userName": "darshan",
  "password": "password123",
  "email": "darshan@example.com",
  "sentimentAnalysis": true
}
```

Flow:

1. `PublicController` receives the signup request.
2. `UserEntryService.saveNewUser()` encrypts the password using BCrypt.
3. Default roles are assigned to the user.
4. The user is saved in MySQL through `UserEntryRepository`.

### 2. User Login

Endpoint:

```text
POST /journal/public/login
```

Request body:

```json
{
  "userName": "darshan",
  "password": "password123"
}
```

Flow:

1. `PublicController` receives the login request.
2. Spring Security authenticates the username and password.
3. `UserDetailsImp` loads the user from MySQL.
4. `JwtUtil` generates a JWT token.
5. The API returns the token.

Example response:

```text
eyJhbGciOiJIUzI1NiJ9...
```

### 3. Access Protected APIs

For protected APIs, send the JWT token in the `Authorization` header:

```text
Authorization: Bearer your_jwt_token
```

Flow:

1. `JwtFilter` reads the `Authorization` header.
2. It extracts the username from the token.
3. It validates that the token is not expired.
4. It loads user details from the database.
5. It sets the authenticated user in `SecurityContextHolder`.
6. Controllers can access the logged-in username from Spring Security.

## Journal Entry Flow

### Create Journal Entry

Endpoint:

```text
POST /journal/journal
```

Headers:

```text
Authorization: Bearer your_jwt_token
```

Request body:

```json
{
  "title": "My First Entry",
  "content": "Today I started writing my journal."
}
```

Flow:

1. `JournalEntryController` gets the logged-in username.
2. `JournalEntryService.EntryRecord()` finds the user.
3. The journal entry date is set.
4. The entry is linked to the user.
5. The journal entry is saved in MySQL.

### Get All Journal Entries

Endpoint:

```text
GET /journal/journal
```

Returns all journal entries belonging to the logged-in user.

### Get Journal Entry By ID

Endpoint:

```text
GET /journal/journal/id/{id}
```

The controller checks whether the requested entry belongs to the logged-in user before returning it.

### Update Journal Entry

Endpoint:

```text
PUT /journal/journal/{id}
```

Request body:

```json
{
  "title": "Updated Title",
  "content": "Updated content"
}
```

Only the owner of the journal entry can update it.

### Delete Journal Entry

Endpoint:

```text
DELETE /journal/journal/{id}
```

The service removes the entry from the user's journal list and deletes it from the database.

## User Flow

### Get Greeting

Endpoint:

```text
GET /journal/user
```

Flow:

1. Gets the logged-in username.
2. Calls `WeatherService.getWeather("Bangalore")`.
3. Weather response is fetched from Redis if cached.
4. If not cached, the app calls the external weather API.
5. The response is cached in Redis.
6. A greeting message is returned.

### Update User

Endpoint:

```text
PUT /journal/user
```

Updates the logged-in user's password.

### Delete User

Endpoint:

```text
DELETE /journal/user
```

Deletes the logged-in user by username.

## Admin Flow

Endpoint:

```text
GET /journal/admin
```

Only users with the `ADMIN` role can access this endpoint.

It returns all users from the database.

## Scheduled Sentiment Email Flow

`UserScheduler` runs every Sunday at 9 AM:

```java
@Scheduled(cron = "0 0 9 * * SUN")
```

Flow:

1. Finds users who have an email and enabled sentiment analysis.
2. Collects their journal entries from the last 7 days.
3. Joins the journal content into one text.
4. Sends the text to `SentimentAnalysis`.
5. Sends the result by email using `EmailService`.

Note: `SentimentAnalysis` currently returns an empty string, so actual sentiment detection still needs to be implemented.

## Swagger Documentation

Swagger UI is available when the application is running:

```text
http://localhost:8080/journal/swagger-ui/index.html
```

Use Swagger to explore and test APIs from the browser.

## Entity Relationship

```text
User
 ├── id
 ├── userName
 ├── password
 ├── email
 ├── sentimentAnalysis
 ├── role
 └── journalEntries

JournalEntry
 ├── id
 ├── title
 ├── content
 ├── date
 └── user
```

Relationship:

```text
One User has many Journal Entries.
One Journal Entry belongs to one User.
```

## Security Rules

Configured in `SpringSecurity.java`:

```text
/journal/** and /user/** require authentication
/admin/** requires ADMIN role
all other endpoints are public
```

Because the app also has `/journal` as the server context path, the full protected journal URL becomes:

```text
/journal/journal/**
```

## Running Tests

Run all tests:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

## Important Security Note

Do not commit real passwords, API keys, JWT secrets, or email credentials to Git.

Recommended approach:

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}

weather:
  api:
    key: ${WEATHER_API_KEY}
```

Then set those values as environment variables on your machine or deployment server.

## Quick API Testing Order

1. Start MySQL.
2. Create the `JournalApplication` database.
3. Start Redis.
4. Run the Spring Boot app.
5. Call signup API.
6. Call login API and copy the JWT token.
7. Use the token in the `Authorization` header.
8. Create a journal entry.
9. Get all journal entries.
10. Update or delete entries as needed.

