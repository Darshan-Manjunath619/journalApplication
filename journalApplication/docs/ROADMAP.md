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
[ ] 1.7 API documentation
[ ] 1.8 Frontend foundation
[ ] 1.9 Frontend authentication
[ ] 1.10 Journal frontend
[ ] 1.11 Frontend search and pagination
[ ] 1.12 UI error handling and user experience
[ ] 1.13 Full integration verification
[ ] 1.14 Documentation and phase completion

## Later phases

[ ] Phase 2 — Microservices learning architecture
[ ] Phase 3 — Measured Kafka and Redis use cases
  - [ ] Real MySQL Testcontainers compatibility verification
[ ] Phase 4 — Containers and deployment
[ ] Phase 5 — Production architecture

Each sub-phase follows: Explain, Implement, Review Diff, Verify, Learn,
Propose Commit, Commit, Update Roadmap, Checkpoint. A failed verification
blocks both the commit and the next sub-phase.
