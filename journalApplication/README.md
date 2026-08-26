# Journal Application

A production-style learning project with a Spring Boot modular monolith,
MySQL database, and React/TypeScript single-page application.

Users can register, manage their profile, create private journal entries,
organize them with tags and favorites, and search, filter, sort, and paginate
their entries.

## Architecture

```text
Browser
  |
  v
React + TypeScript SPA :5173
  |
  | JSON over /api/v1
  v
Spring Security
  |-- short-lived JWT access token
  |-- rotating HttpOnly refresh cookie
  |-- CORS and role authorization
  v
Versioned DTO controllers
  v
Transactional application services
  v
Spring Data JPA repositories
  v
MySQL schema managed by Flyway
```

The backend and frontend live in one Git repository but are separate
applications with separate build tools. See [Architecture](docs/ARCHITECTURE.md)
for the complete structure and request flows.

## Technology

Backend:

- Java 17, Spring Boot 3.5, Maven
- Spring Web, Security, Validation, Data JPA
- JWT access tokens and rotating refresh tokens
- MySQL, Hibernate, and Flyway
- Springdoc OpenAPI and Actuator
- JUnit, Mockito, MockMvc, and H2 MySQL compatibility mode

Frontend:

- React 19 and TypeScript
- Vite and npm
- React Router and TanStack Query
- React Hook Form and Zod
- Tailwind CSS
- Vitest and React Testing Library

Redis, weather, email, and sentiment scheduling are optional integrations.
They are disabled or isolated from core journal behavior by default.

## Requirements

- Java 17
- MySQL 8
- Node.js 24 and npm 11
- No global Maven installation is required; the Maven wrapper is included.

## First-time setup

### 1. Create the database

```sql
CREATE DATABASE JournalApplication;
```

Flyway creates and updates application tables when the backend starts. Hibernate
validates the resulting schema; it does not modify it.

### 2. Configure the backend

From the repository root:

```powershell
Copy-Item .env.example .env
```

Edit the ignored `.env` file and set at least:

```properties
DB_USERNAME=root
DB_PASSWORD=your-local-mysql-password
JWT_SECRET=replace-with-a-random-secret-of-at-least-32-bytes
```

Do not commit `.env`. The safe variable list is documented in
[Environment configuration](docs/ENVIRONMENT.md).

### 3. Configure the frontend

```powershell
Copy-Item frontend\.env.example frontend\.env.local
```

The default frontend configuration points to:

```text
http://localhost:8080/journal/api/v1
```

Browser-visible `VITE_*` variables must never contain secrets.

### 4. Install frontend dependencies

```powershell
cd frontend
npm.cmd install
cd ..
```

## Run locally

Start the backend from the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

In a second terminal, start the frontend:

```powershell
cd frontend
npm.cmd run dev
```

Open:

- Frontend: `http://localhost:5173`
- Health: `http://localhost:8080/journal/actuator/health`
- Swagger UI: `http://localhost:8080/journal/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/journal/v3/api-docs/v1`

If port 8080 is already in use, stop the existing process before starting a
second backend instance.

## Supported API

All application endpoints use `/api/v1`.

| Area | Endpoints |
|---|---|
| Authentication | `POST /auth/register`, `/login`, `/refresh`, `/logout` |
| Profile | `GET/PATCH /users/me`, `PATCH /users/me/password` |
| Journals | `GET/POST /journals`, `GET/PATCH/DELETE /journals/{id}` |
| Tags | `GET/POST /tags`, `PATCH/DELETE /tags/{id}` |
| Administration | `GET /admin/users` with the `ADMIN` role |

These paths are relative to:

```text
http://localhost:8080/journal/api/v1
```

The journal list accepts `page`, `size`, `q`, `from`, `to`,
`favorite`, `tag`, and `sort` query parameters. See
[API guide](docs/API.md).

## Authentication example

Login returns a 15-minute access token in JSON and sets a rotating refresh token
as an HttpOnly cookie.

```text
Login -> access token kept in frontend memory
      -> refresh token kept by the browser as an HttpOnly cookie

API call -> Authorization: Bearer <access-token>

Expired access token -> frontend calls /auth/refresh once
                     -> backend rotates refresh cookie
                     -> frontend retries the original request once
```

The access token is never stored in local storage. Full details are in
[Frontend authentication](docs/frontend/AUTHENTICATION.md).

## Database migrations

Flyway runs migrations from `src/main/resources/db/migration` in version order:

- V1 creates users, roles, and journals.
- V2 creates refresh-token persistence.
- V3 adds journal audit timestamps.
- V4 adds favorites and its index.
- V5 creates tags and journal/tag relationships.

Flyway records applied versions in `flyway_schema_history`. Never edit an
already-applied migration; add the next version for every schema change.

## Verification

Backend:

```powershell
.\mvnw.cmd verify
```

Frontend:

```powershell
cd frontend
npm.cmd run lint
npm.cmd run test
npm.cmd run build
```

Backend tests require no running MySQL, Redis, SMTP, or weather service. See
[Testing strategy](docs/TESTING.md).

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Architecture decisions](docs/ARCHITECTURE_DECISIONS.md)
- [API guide](docs/API.md)
- [Environment configuration](docs/ENVIRONMENT.md)
- [Testing strategy](docs/TESTING.md)
- [Master roadmap](docs/MASTER_ROADMAP.md)
- [Frontend README](frontend/README.md)

## Security

- Never commit passwords, JWT secrets, tokens, API keys, or local environment files.
- Registration creates only the `USER` role.
- Administrator roles must be assigned through controlled administration.
- Production requires HTTPS, secure refresh cookies, trusted frontend origins,
  secret management, and restricted API documentation.
