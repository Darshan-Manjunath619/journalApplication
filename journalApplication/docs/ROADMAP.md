# Delivery Roadmap

Permanent scope and acceptance criteria: [MASTER_ROADMAP.md](MASTER_ROADMAP.md).
This file is the concise execution checklist; update both files together.

Status: [ ] not started, [-] in progress, [x] completed.

## Phase 1 — Modular Monolith and SPA

[x] 1.0 Repository analysis and baseline
[x] 1.1 Backend foundation cleanup
  - [x] 1.1A Configuration and secret safety
  - [x] 1.1B DTO, validation, and error foundation
    - [x] 1.1B-1 Validation and centralized error contract
    - [x] 1.1B-2 User and authentication DTO boundary
    - [x] 1.1B-3 Journal DTO boundary
  - [x] 1.1C Logging, correlation, and structural hygiene
  - [x] 1.1D Flyway database migration foundation
[x] 1.2 Authentication and security
  - [x] 1.2A Access-token and HTTP security hardening
  - [x] 1.2B Refresh-token persistence
  - [x] 1.2C Rotation, reuse detection, and logout
  - [x] 1.2D Profile and password security
  - [x] 1.2E Security verification and documentation
[x] 1.3 Journal core features
[x] 1.4 Search, filtering, sorting, and pagination
[x] 1.5 Additional domain features
  - [x] 1.5A Journal favorites
  - [x] 1.5B User-owned tags
[x] 1.6 Backend testing
  - [x] 1.6A Test audit and cleanup
  - [x] 1.6B Unit and controller coverage
  - [x] 1.6C Repository and integration coverage
    - [x] 1.6C-1 H2 MySQL-mode repository and transaction tests
[x] 1.7 API documentation
  - [x] 1.7A Restricted operational health endpoint
[x] 1.8 Frontend foundation
  - [x] 1.8A React, TypeScript, and Vite initialization
  - [x] 1.8B Routing and application shell
  - [x] 1.8C API client, TanStack Query, and environment configuration
  - [x] 1.8D Forms, styling, and test infrastructure
    - [x] 1.8D-1 Frontend testing foundation
    - [x] 1.8D-2 Form and validation foundation
    - [x] 1.8D-3 Tailwind styling foundation
[x] 1.9 Frontend authentication
  - [x] 1.9A Authentication state, contracts, and session bootstrap
  - [x] 1.9B Registration and login forms
  - [x] 1.9C Protected routes, refresh retry, and logout
  - [x] 1.9D Profile integration
[x] 1.10 Journal frontend
  - [x] 1.10A Journal dashboard and paginated listing
  - [x] 1.10B Journal create and detail workflows
  - [x] 1.10C Journal edit and delete workflows
[x] 1.11 Frontend search and pagination
  - [x] 1.11A Journal search and sorting
  - [x] 1.11B Tag, favorite, and date filters with URL query state
[x] 1.12 UI error handling and user experience
  - [x] 1.12A Consistent feedback and retry states
  - [x] 1.12B Accessibility and responsive layout
[x] 1.13 Full integration verification
  - [x] 1.13A Automated backend and frontend verification
  - [x] 1.13B Real MySQL and browser application journey
[x] 1.14 Documentation and phase completion
  - [x] 1.14A Retire legacy API routes
  - [x] 1.14B Final documentation, verification, and Phase 1 tag
[x] 1.15 Modular monolith HLD diagrams

## Later phases

[-] Phase 2 — Microservices learning architecture
  - [x] 2.0 Architecture design
  - [x] 2.1 Service boundaries
    - [x] 2.1A Access-token identity contract
    - [x] 2.1B Enforce module ownership
      - [x] 2.1B-1 Scalar journal owner boundary
      - [x] 2.1B-2 Package dependency guardrails
  - [x] 2.2 Extract Journal Service
    - [x] 2.2A Separate deployable scaffold and health check
    - [x] 2.2B Move journal and tag capability
      - [x] 2.2B-1 Domain and persistence foundation
      - [x] 2.2B-2 Application services
      - [x] 2.2B-3 HTTP API parity
    - [x] 2.2C Journal Service JWT validation
    - [x] 2.2D Frontend journal API cutover
    - [x] 2.2E Remove legacy journal code
  - [-] 2.3 Database separation
    - [x] 2.3A Establish independent Journal Service schema ownership
    - [x] 2.3B Build and rehearse the existing-data migration
    - [ ] 2.3C Verify cutover and retire legacy Identity tables
  - [ ] 2.4 Service communication
  - [ ] 2.5 Distributed authentication
  - [ ] 2.6 Integration testing
  - [ ] 2.7 Phase verification
[ ] Phase 3 — Measured Kafka and Redis use cases
  - [ ] Real MySQL Testcontainers compatibility verification
[ ] Phase 4 — Containers and deployment
[ ] Phase 5 — Production architecture

Each sub-phase follows: Explain, Implement, Review Diff, Verify, Learn,
Propose Commit, Commit, Update Roadmap, Checkpoint. A failed verification
blocks both the commit and the next sub-phase.
