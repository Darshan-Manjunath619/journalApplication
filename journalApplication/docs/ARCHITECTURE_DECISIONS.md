# Phase 1 Architecture Decisions

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
