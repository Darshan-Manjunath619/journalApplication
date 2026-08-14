# Delivery Roadmap

Status: [ ] not started, [-] in progress, [x] completed.

## Phase 1 — Modular Monolith and SPA

[x] 1.0 Repository analysis and baseline
[-] 1.1 Backend foundation cleanup
  - [x] 1.1A Configuration and secret safety
  - [x] 1.1B DTO, validation, and error foundation
    - [x] 1.1B-1 Validation and centralized error contract
    - [x] 1.1B-2 User and authentication DTO boundary
    - [x] 1.1B-3 Journal DTO boundary
  - [x] 1.1C Logging, correlation, and structural hygiene
  - [x] 1.1D Flyway database migration foundation
[-] 1.2 Authentication and security
  - [x] 1.2A Access-token and HTTP security hardening
  - [x] 1.2B Refresh-token persistence
  - [x] 1.2C Rotation, reuse detection, and logout
  - [ ] 1.2D Profile and password security
  - [ ] 1.2E Security verification and documentation
[ ] 1.3 Journal core features
[ ] 1.4 Search, filtering, sorting, and pagination
[ ] 1.5 Additional domain features
[ ] 1.6 Backend testing
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
[ ] Phase 4 — Containers and deployment
[ ] Phase 5 — Production architecture

Each sub-phase follows: Explain, Implement, Review Diff, Verify, Learn,
Propose Commit, Commit, Update Roadmap, Checkpoint. A failed verification
blocks both the commit and the next sub-phase.
