# Architecture Decisions

## ADR 1 - Modular monolith

Decision: keep one Spring Boot deployment and one MySQL database while separating
auth, user, journal, tag, and shared responsibilities in code.

Why: current scale does not justify network failures, distributed transactions,
and deployment overhead. The trade-off is that boundaries rely on code discipline.

## ADR 2 - JWT access and rotating refresh tokens

Decision: use a short-lived access JWT in frontend memory and an opaque rotating
refresh token in an HttpOnly cookie.

Why: API calls remain stateless while logout and refresh-token revocation are
possible. The trade-off is additional database and rotation logic.

## ADR 3 - MySQL and Flyway

Decision: Flyway owns every schema change and Hibernate only validates mappings.

Why: migrations are ordered, repeatable across environments, and recorded in
`flyway_schema_history`. The trade-off is writing SQL for every schema change.

## ADR 4 - User-owned normalized tags

Decision: store reusable tags in their own table and link them to journals.

Why: users get consistent filtering and duplicate names are prevented per user.
The trade-off is joins and ownership validation.

## ADR 5 - Feature-based React with TanStack Query

Decision: group frontend code by feature and use TanStack Query for server state,
React context for authentication, and URL parameters for dashboard query state.

Why: each tool owns one kind of state without a large global store. The trade-off
is learning several focused libraries instead of one broad state framework.

## ADR 6 - Two Phase 2 business services

Decision: target Identity Service and Journal Service. Keep tags with journals
and defer optional integrations.

Why: these are cohesive business and data boundaries. More services would add
network and deployment cost without independent ownership or scaling needs.

## ADR 7 - Extract Journal Service first

Decision: retain auth/user behavior in the existing application and extract
journals/tags through a strangler cutover.

Why: Journal can validate tokens and own requests locally. Stable authentication
reduces simultaneous change while the first distributed boundary is learned.

## ADR 8 - Database per service

Decision: Identity and Journal own separate schemas, credentials, migrations,
and Flyway histories. Journal stores immutable user IDs without a database
foreign key to Identity.

Why: independent data ownership is the main boundary that prevents a distributed
monolith. The trade-off is explicit cross-service consistency and migration work.

## ADR 9 - No gateway in Phase 2

Decision: configure the SPA with Identity and Journal base URLs during Phase 2.

Why: an API gateway is a later production concern. Direct routing exposes the
real CORS, availability, and client-configuration costs of service extraction.

## ADR 10 - Asymmetric service token validation

Decision: use Identity-owned private signing keys and verification-only public
keys in resource services as the Phase 2.5 target.

Why: a compromised Journal Service cannot mint Identity tokens. Temporary shared
HMAC validation is allowed only as a tested migration step.
