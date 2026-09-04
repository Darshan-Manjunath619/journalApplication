# Journal Application Master Roadmap

This is the permanent end-to-end delivery plan for the Journal Application.
Use [ROADMAP.md](ROADMAP.md) as the short execution checklist and this file to
compare current progress with the complete target architecture.

## How to use this roadmap

- `[ ]` Not started, `[-]` in progress, `[x]` completed.
- Update both roadmap files in the same verified sub-phase commit.
- A phase is complete only when its acceptance criteria pass.
- Do not start a later major phase merely because part of the current phase is usable.
- Every sub-phase follows: Explain → Implement → Review Diff → Verify → Learn →
  Commit → Update Roadmaps → Checkpoint.

## Current position

| Major phase | Status | Outcome |
|---|---|---|
| Phase 1 — Modular monolith and SPA | `[x]` | Secure full-stack journal application |
| Phase 2 — Microservices | `[-]` | Extract justified service boundaries |
| Phase 3 — Kafka and Redis | `[ ]` | Add measured event and cache use cases |
| Phase 4 — Containers and deployment | `[ ]` | Reproducible containerized deployment |
| Phase 5 — Production architecture | `[ ]` | Resilience, observability, security, and scale |

Current checkpoint: **Phase 2.2A Journal Service scaffold completed**.
Next: **Phase 2.2B — Move journal and tag capabilities into the new service**.

---

## Phase 1 — Production-Style Modular Monolith and React SPA `[x]`

### Goal

Build one secure Spring Boot deployable, one MySQL database, and a separate
React/TypeScript frontend. Establish domain boundaries and production practices
before adding distributed-system complexity.

### 1.0 Repository analysis and baseline `[x]`

- Inventory controllers, services, repositories, entities, APIs, security,
  configuration, integrations, database shape, and tests.
- Establish an isolated test profile and a green baseline.
- Record existing risks and architecture.

Acceptance: the baseline build is reproducible without real email, Redis, or
weather calls.

### 1.1 Backend foundation cleanup `[x]`

- [x] **1.1A Configuration and secret safety** — externalize credentials,
  ignore local `.env`, isolate optional integrations, and provide safe examples.
- [x] **1.1B DTO, validation, and errors**
  - [x] Validation and centralized Problem Detail responses.
  - [x] Authentication/user request and response DTO boundary.
  - [x] Journal DTO and ownership boundary.
- [x] **1.1C Logging and structural hygiene** — constructor injection,
  correlation IDs, safe logging, and explicit dependencies.
- [x] **1.1D Flyway foundation** — versioned migrations, schema history,
  Hibernate validation, and legacy-schema baseline support.

Acceptance: no HTTP endpoint exposes a JPA entity, invalid requests have a
stable error contract, secrets are not tracked, and Flyway owns schema changes.

### 1.2 Authentication and security `[x]`

- [x] **1.2A Access-token and HTTP hardening** — 15-minute JWTs with issuer,
  audience, token ID, deny-by-default authorization, CORS, and JSON `401/403`.
- [x] **1.2B Refresh-token persistence** — opaque tokens, hashes in MySQL,
  expiry and revocation fields.
- [x] **1.2C Rotation and logout** — HttpOnly cookie, one-time rotation,
  row locking, reuse detection, origin validation, and logout revocation.
- [x] **1.2D Profile and password security** — authenticated profile read/update,
  current-password verification, secure rehashing, and session invalidation.
- [x] **1.2E Security verification and documentation** — registration conflicts,
  invalid/expired/reused tokens, roles, CORS/origin behavior, and complete flow docs.

Acceptance: registration assigns only `USER`; login/refresh/logout work; stolen
or reused refresh tokens are contained; profile and password actions enforce the
authenticated user; security tests cover failure paths.

### 1.3 Journal core features `[x]`

- Versioned `/api/v1/journals` CRUD endpoints.
- Authenticated ownership in every repository query.
- Correct empty-list, missing-resource, create, update, and delete semantics.
- Transactional service boundaries and UTC audit timestamps.

Acceptance: all CRUD operations pass, cross-user access returns `404`, empty
collections return `200`, and entities never cross the HTTP boundary.

### 1.4 Search, filtering, sorting, and pagination `[x]`

- Stable `PageResponse<T>` contract.
- Page size bounded to 1–100.
- Search title/content; filter by dates, tag, and favorite.
- Allowlisted sorting by `createdAt`, `updatedAt`, and `title`.
- Database indexes and combined-query tests.

Acceptance: boundaries, invalid inputs, stable ordering, and combined filters
are verified without unbounded database reads.

### 1.5 Additional domain features `[x]`

- [x] User-owned reusable tags with normalized unique names.
- [x] Tag assignment ownership and deletion conflict behavior.
- [x] Journal favorites.
- [x] User profile and audit timestamps completed in earlier sub-phases.
- Deferred unless justified: categories, archive/soft delete, analytics.

Acceptance: each retained feature solves a clear journal use case and has its
own ownership, validation, migration, and tests.

### 1.6 Backend testing `[x]`

- [x] Audit and remove unsafe, skipped, or meaningless legacy tests.
- [x] Unit tests for mapping, validation, services, and token rules.
- [x] MockMvc tests for controllers and security.
- [x] H2 MySQL-mode repository, transaction rollback, and Flyway tests.
- No test contacts production or real external services.

Real MySQL Testcontainers verification is tracked in Phase 3, where Docker
infrastructure is introduced.

Acceptance: `mvn verify` is green and covers critical success and failure paths.

### 1.7 API documentation `[x]`

- OpenAPI endpoint, DTO, validation, pagination, authentication, and error docs.
- Request/response examples and Bearer security scheme.
- Verify documentation against actual controller behavior.
- [x] Restricted Actuator health endpoint for runtime and deployment checks.

Acceptance: a developer can exercise every supported API from the generated docs.

### 1.8 Frontend foundation `[x]`

- [x] React, TypeScript, and Vite initialization.
- [x] Routing and responsive application shell.
- [x] API client, TanStack Query, and environment configuration.
- [x] Forms, styling, and frontend test infrastructure.
  - [x] Vitest, React Testing Library, jsdom, and routing tests.
  - [x] React Hook Form, Zod, and reusable form controls.
  - [x] Tailwind CSS and reusable responsive visual styles.
- React Router, TanStack Query, React Hook Form, Zod, and Tailwind.
- Feature-based structure, environment configuration, API client, and test setup.

Acceptance: lint, type-check, tests, and production build pass; the app starts locally.

### 1.9 Frontend authentication `[x]`

- [x] Authentication state, typed contracts, and refresh-based session bootstrap.
- [x] Register and login forms.
- [x] Protected routes, automatic refresh retry, and logout.
- [x] Profile integration.
- Access token held in memory, never local storage.
- Single shared refresh request and one retry after access-token expiry.

Acceptance: authentication lifecycle and failure states pass component/API-mock tests.

### 1.10 Journal frontend `[x]`

- [x] Dashboard and journal listing.
- [x] Create and view workflows.
- [x] Edit and delete workflows with confirmation and recovery.
- [x] Tag and favorite controls.

Acceptance: each workflow works against the real backend and has focused tests.

### 1.11 Frontend search and pagination `[x]`

- [x] Search, filtering, sorting, page navigation, and URL query-state integration.
- [x] Tag, favorite, and UTC date-boundary filters.
- Query flow documented from browser parameters to database and response.

Acceptance: filters combine correctly, navigation is stable, and stale requests
do not overwrite newer results.

### 1.12 UI error handling and user experience `[x]`

- [x] Shared page-level loading, empty, retry, and server-error presentation.
- [x] Non-blocking tag failure and focused retry behavior.
- [x] Keyboard, focus, responsive layout, and screen-reader accessibility pass.

Acceptance: important failure scenarios are usable and verified on mobile and desktop.

### 1.13 Full integration verification `[x]`

- [x] Backend verify and frontend lint, tests, type-check, and production build.
- [x] Register → login → dashboard → create → view → search/filter → edit →
  favorite → delete → logout.
- [x] Real MySQL, authentication, health, OpenAPI, CORS, and error checks.

Acceptance: the complete journey passes with no obvious regressions.

### 1.14 Documentation and completion `[x]`

- [x] **1.14A Legacy API retirement** — remove obsolete unversioned controllers,
  expose administration at `/api/v1/admin/users`, and verify deny-by-default behavior.
- [x] **1.14B Final documentation and completion** — finish architecture and setup
  documentation, run final verification, and create the Phase 1 tag.
- Architecture, package structure, database, authentication, request flows,
  setup, environment variables, and testing instructions.
- ADRs for modular monolith, tokens, Flyway/MySQL, tags, and frontend state.
- Create Git tag `phase-1-complete` only after all Phase 1 acceptance criteria pass.

### 1.15 Modular monolith HLD diagrams `[x]`

- Document system context, deployment containers, backend components, authentication,
  journal request flow, database relationships, and runtime configuration.
- Preserve this as the Phase 1 baseline for comparison with Phase 2 microservices.

Acceptance: diagrams match the implemented system, render as Mermaid, and introduce
no runtime changes.

---

## Phase 2 — Microservices Learning Architecture `[-]`

### Goal

Demonstrate service extraction only after Phase 1 boundaries are stable. This is
an architectural learning/scalability phase, not a claim that current traffic
requires microservices.

- [x] **2.0 Architecture design** — business boundaries, ADRs, operational cost,
  failure modes, and extraction order.
- [x] **2.1 Service boundaries** — contracts and ownership for auth/user,
  journal/tag, and optional notification capabilities.
  - [x] **2.1A Access-token identity contract** — signed immutable user ID and
    roles shared consistently by login and refresh.
  - [x] **2.1B Module ownership enforcement** — remove forbidden code dependencies
    before moving journal/tag code.
    - [x] **2.1B-1 Scalar journal owner boundary** — replace cross-domain JPA
      relationships with stable owner IDs behind a journal-owned identity port.
    - [x] **2.1B-2 Package dependency guardrails** — reorganize remaining journal
      code and enforce allowed module dependencies with automated tests.
- [-] **2.2 Extract first microservice** — smallest justified boundary with parity.
  - [x] **2.2A Separate deployable scaffold** — independent Maven build, port,
    Actuator health endpoint, executable JAR, and real HTTP startup test.
  - [ ] **2.2B Move journal and tag capability** — copy module-owned API,
    application, domain, persistence, validation, and error behavior.
  - [ ] **2.2C Journal Service JWT validation** — validate the existing access
    token locally and authorize using its immutable user ID and roles.
  - [ ] **2.2D Frontend cutover** — route journal/tag calls to the new service
    while keeping auth/profile calls on Identity.
  - [ ] **2.2E Remove legacy journal code** — retire duplicate endpoints only
    after parity, rollback, and frontend verification.
- [ ] **2.3 Database separation** — database ownership, migration, and consistency plan.
- [ ] **2.4 Service communication** — synchronous contracts, timeouts, and versioning.
- [ ] **2.5 Distributed authentication** — token validation and authorization boundaries.
- [ ] **2.6 Integration testing** — contract, component, and cross-service tests.
- [ ] **2.7 Phase verification** — end-to-end parity and documented trade-offs.

Acceptance: extracted services own their code/data, failure handling is explicit,
and Phase 1 user journeys remain functional.

---

## Phase 3 — Kafka and Redis `[ ]`

### Goal

Introduce asynchronous messaging and caching only for measured, well-defined use cases.

- [ ] **3.0 Use-case analysis** — identify latency, coupling, throughput, or repeated-read need.
- [ ] **3.0A Container-backed database verification** — run Flyway and repository
  integration tests against a temporary real MySQL Testcontainer.
- [ ] **3.1 Kafka infrastructure** — local/test configuration, topics, schemas, and ownership.
- [ ] **3.2 First event flow** — useful domain event and consumer with observable outcome.
- [ ] **3.3 Reliability** — retry, dead-letter handling, idempotency, ordering, and replay.
- [ ] **3.4 Redis infrastructure** — isolated configuration and failure behavior.
- [ ] **3.5 Cache implementation** — one measured read path with key/TTL design.
- [ ] **3.6 Cache correctness** — invalidation, stampede prevention, and Redis outage behavior.
- [ ] **3.7 Performance comparison** — before/after latency, load, and resource measurements.
- [ ] **3.8 Phase verification** — correctness under duplicate events and dependency outages.

Acceptance: Kafka/Redis solve documented problems, degrade safely, and have
measured benefit rather than existing only as technology demonstrations.

---

## Phase 4 — Containers and Deployment `[ ]`

### Goal

Create reproducible runtime artifacts and deploy them through increasingly
production-like environments.

- [ ] **4.0 Containerization design** — images, configuration, networks, volumes, and secrets.
- [ ] **4.1 Backend image** — multi-stage build, non-root runtime, health check.
- [ ] **4.2 Frontend image** — production build and SPA web-server routing.
- [ ] **4.3 Dependency containers** — MySQL, Kafka, Redis, and test dependencies as applicable.
- [ ] **4.4 Docker Compose** — complete local stack and service connectivity.
- [ ] **4.5 Kubernetes fundamentals** — Deployments, Services, ConfigMaps, Secrets, probes.
- [ ] **4.6 Kubernetes deployment** — manifests/Helm, migrations, rollout, rollback, persistence.
- [ ] **4.7 Cloud deployment** — selected provider, managed dependencies, TLS, and DNS.
- [ ] **4.8 Phase verification** — clean-environment deployment and recovery checks.

Acceptance: a new environment can deploy reproducibly without embedded secrets,
with healthy startup, persistence, and documented rollback.

---

## Phase 5 — Production Architecture `[ ]`

### Goal

Harden the deployed system for diagnosis, failure, security, and measured scale.

- [ ] **5.0 Architecture analysis** — workloads, SLOs, threats, bottlenecks, and costs.
- [ ] **5.1 API gateway** — justified routing, authentication boundary, and rate limits.
- [ ] **5.2 Load balancing** — stateless scaling and session-independent behavior.
- [ ] **5.3 Resilience** — timeouts, retries, circuit breaking, bulkheads, and graceful degradation.
- [ ] **5.4 Observability** — structured logs, metrics, traces, dashboards, and alerts.
- [ ] **5.5 Security hardening** — secret manager, key rotation, dependency scanning,
  headers, least privilege, audit, and incident response.
- [ ] **5.6 Performance testing** — realistic load, bottleneck analysis, and capacity results.
- [ ] **5.7 Final architecture** — diagrams, decisions, trade-offs, and cost model.
- [ ] **5.8 Final verification** — production-readiness checklist and disaster exercises.

Acceptance: the system has measurable objectives, actionable diagnostics,
tested failure behavior, secure operations, and evidence-based capacity limits.

## Deferred decisions

- Categories: defer until a distinct hierarchical use case exists beyond tags.
- Soft deletion/archive: defer until restore, retention, or compliance semantics exist.
- Full-text/search engine: defer until measured SQL search limitations justify it.
- Sentiment processing and email: require privacy, consent, provider, and failure policies.
- New Redis/Kafka uses: require measurement and a documented correctness model.

## Phase completion history

| Checkpoint | Commit | Result |
|---|---|---|
| 1.1A Configuration safety | `7cbc130` | Completed |
| 1.1B-1 Error contract | `fdef506` | Completed |
| 1.1B-2 Auth DTO boundary | `fa597f0` | Completed |
| 1.1B-3 Journal DTO boundary | `f7b1117` | Completed |
| 1.1C Correlation/logging | `493cd0f` | Completed |
| 1.1D Flyway foundation | `5ad2301` | Completed |
| 1.2A Access-token security | `53b3aa6` | Completed |
| 1.2B Refresh persistence | `cad4905` | Completed |
| 1.2C Rotation/logout | `1a0a938` | Completed |
