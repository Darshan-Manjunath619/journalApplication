# Phase 1 Architecture

## 1. System context

Phase 1 is a modular monolith: one Spring Boot deployment and one MySQL
database, with business responsibilities separated inside the codebase. The
React SPA is a separate application that depends only on the HTTP contract.

```mermaid
flowchart LR
    User[Journal user]
    Admin[Administrator]
    SPA[React and TypeScript SPA]
    Backend[Spring Boot modular monolith]
    DB[(MySQL database)]

    User -->|Uses browser| SPA
    Admin -->|Uses application| SPA
    Admin -->|Uses Swagger for API administration| Backend
    SPA -->|HTTPS JSON /api/v1| Backend
    Backend -->|JPA and SQL transactions| DB
```

This keeps deployment and database transactions simple while creating boundaries
that can be evaluated for extraction during Phase 2.

The frontend knows only the versioned HTTP contract. It does not connect to the
database or import backend Java classes.

## 2. Container and deployment view

```mermaid
flowchart TB
    subgraph Client["Client device"]
        Browser[Browser]
        SPA[React SPA static files]
        Browser --> SPA
    end

    subgraph AppHost["Application environment"]
        Backend["Spring Boot process :8080 /journal"]
        Optional["Optional adapters: weather, Redis, mail"]
    end

    subgraph DataHost["Database environment"]
        MySQL[(MySQL 8)]
        History[(flyway_schema_history)]
        MySQL --- History
    end

    SPA -->|"HTTPS /api/v1 and refresh cookie"| Backend
    Backend -->|"JDBC through connection pool"| MySQL
    Backend -.->|"Feature flagged"| Optional
```

The SPA and backend are separate deployables even though they share one Git
repository. MySQL is the only required Phase 1 runtime dependency.

## 3. Backend component view

```mermaid
flowchart TB
    Request[HTTP request]

    subgraph Monolith["Single Spring Boot application"]
        Security["Security boundary: CORS, JWT filter, authorization"]
        Controllers["Versioned DTO controllers"]
        Auth["Auth module: login, refresh, logout"]
        User["User module: profile and password"]
        Journal["Journal module: CRUD, search, favorites"]
        Tag["Tag module: ownership and assignment"]
        Shared["Shared: Problem Detail, pagination, correlation ID"]
        Repositories["Module-facing JPA repositories"]
        Hibernate[JPA and Hibernate]
        Flyway[Flyway]
    end

    Database[(MySQL)]

    Request --> Security --> Controllers
    Controllers --> Auth
    Controllers --> User
    Controllers --> Journal
    Controllers --> Tag
    Shared -.-> Controllers
    Auth --> Repositories
    User --> Repositories
    Journal --> Repositories
    Tag --> Repositories
    Repositories --> Hibernate --> Database
    Flyway -->|Migrates before JPA starts| Database
```

All modules execute inside the same process. Calls between them are normal Java
method calls, and a service transaction can update related tables atomically.

## 4. Repository structure

```text
journalApplication/
|-- src/main/java/.../
|   |-- auth/          access and refresh-token lifecycle
|   |-- user/          profile DTOs and mapping
|   |-- journal/       journal DTOs, mapping, and search criteria
|   |-- tag/           user-owned tag domain
|   |-- controller/    versioned HTTP boundary
|   |-- service/       application rules and transactions
|   |-- repository/    JPA persistence interfaces
|   |-- entity/        user and journal persistence models
|   |-- config/        security, OpenAPI, Redis, and time configuration
|   |-- filter/        JWT authentication
|   `-- shared/        errors, pagination, and correlation IDs
|-- src/main/resources/
|   `-- db/migration/ versioned Flyway SQL
|-- frontend/
|   |-- src/app/       router, providers, and Query client
|   |-- src/features/  auth, profile, journals, and tags
|   |-- src/pages/     route-level screens
|   |-- src/components reusable UI
|   |-- src/lib/       shared API client
|   `-- src/test/      frontend test setup
|-- docs/
|-- pom.xml
`-- README.md
```

Controllers accept validated request DTOs and return response DTOs. JPA entities
remain inside the backend and are never serialized as the public contract.

## 5. Authentication flow

```mermaid
sequenceDiagram
    actor User
    participant SPA as React SPA
    participant Auth as Auth controller/service
    participant Resource as Protected API
    participant Security as Spring Security
    participant DB as MySQL

    User->>SPA: Submit username and password
    SPA->>Auth: POST /api/v1/auth/login
    Auth->>Security: Authenticate credentials
    Security->>DB: Load user and verify BCrypt hash
    DB-->>Security: User and roles
    Security-->>Auth: Authenticated
    Auth->>DB: Store refresh-token hash
    Auth-->>SPA: Access JWT plus HttpOnly refresh cookie
    SPA->>Resource: Protected request with Bearer JWT
    Resource->>Security: Validate JWT and load authorities
    Security-->>Resource: Authenticated identity
    Resource-->>SPA: API response
    Note over SPA,Auth: When JWT expires
    SPA->>Auth: POST /api/v1/auth/refresh with cookie
    Auth->>DB: Revoke old hash and store rotated hash
    Auth-->>SPA: New JWT plus rotated cookie
```

The frontend keeps the access token in memory and sends it in the
`Authorization` header. When it expires, one shared refresh request rotates the
HttpOnly cookie and the original request is retried once. Logout revokes the
refresh token.

## 6. Journal request flow

Example: create a journal.

```mermaid
sequenceDiagram
    actor User
    participant Form as React journal form
    participant API as API client
    participant JWT as JWT filter
    participant Controller as Journals controller
    participant Service as Journal service
    participant Repository as JPA repository
    participant DB as MySQL

    User->>Form: Enter title, content, and tags
    Form->>Form: Validate with Zod
    Form->>API: Submit create command
    API->>JWT: POST /api/v1/journals with Bearer token
    JWT->>Controller: Authenticated request DTO
    Controller->>Service: Create for current user
    Service->>Repository: Verify tags and save in transaction
    Repository->>DB: INSERT journal and tag links
    DB-->>Repository: Commit
    Repository-->>Controller: Saved entity
    Controller-->>API: JournalResponse DTO
    API-->>Form: Invalidate journal query
```

Every journal and tag query is scoped by the authenticated username or user ID,
so another user's resource behaves as not found.

## 7. Database structure

```mermaid
erDiagram
    USERS ||--o{ USER_ROLES : has
    USERS ||--o{ JOURNAL_ENTRIES : owns
    USERS ||--o{ TAGS : owns
    USERS ||--o{ REFRESH_TOKENS : receives
    JOURNAL_ENTRIES ||--o{ JOURNAL_ENTRY_TAGS : contains
    TAGS ||--o{ JOURNAL_ENTRY_TAGS : assigned
```

Main tables:

- `users` and `user_roles`: identity, password hash, and roles.
- `journal_entries`: owned content, audit timestamps, and favorite flag.
- `tags`: normalized user-owned reusable names.
- `journal_entry_tags`: many-to-many association.
- `refresh_tokens`: token hashes, expiry, revocation, and creation time.
- `flyway_schema_history`: applied migration versions and checksums.

Flyway owns schema changes. Hibernate uses `ddl-auto=validate` to detect a
mismatch between Java mappings and the migrated schema.

## 8. Frontend state

- TanStack Query manages remote server state and cache invalidation.
- React context manages the in-memory authenticated session.
- React Hook Form and Zod manage form state and validation.
- URL query parameters preserve dashboard search, filters, sorting, and page.
- Local component state handles temporary UI behavior.

A general Redux store is unnecessary because no cross-application client state
currently justifies it.

## 9. Runtime configuration

```mermaid
flowchart LR
    Env["Backend environment / ignored .env"] --> Backend[Spring Boot]
    ViteEnv["Public VITE variables"] --> Build[Vite build]
    Build --> SPA[Browser SPA]
    Backend --> CORS[Allowed frontend origins]
    SPA -->|Origin must match CORS| Backend
```

Backend secrets come from environment variables or the ignored local `.env`.
Frontend `VITE_*` variables are public build-time settings. Local development
allows `http://localhost:5173` through CORS; production must list the actual
HTTPS frontend origin.

Weather, Redis, mail, and sentiment scheduling are optional integrations and do
not participate in core journal CRUD startup or health.
