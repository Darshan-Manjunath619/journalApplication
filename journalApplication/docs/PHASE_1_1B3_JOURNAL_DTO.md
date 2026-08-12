# Phase 1.1B-3 — Journal DTO boundary

## Changes

- Replaced JournalEntry request and response bodies with validated DTOs.
- Added explicit journal mapping that excludes ownership relationships.
- Moved ownership enforcement into repository queries containing both the
  journal ID and authenticated username.
- Made empty journal lists return HTTP 200 with an empty array.
- Added correct create, partial update, not-found, and delete semantics.
- Removed controller-side collection filtering and duplicate ID lookups.

## Contract

Create requires a nonblank title of at most 160 characters and content of
at most 20,000 characters. PATCH updates one or both fields. IDs, dates,
and owners are controlled by the server and cannot be supplied as DTO fields.

## Security and performance impact

Unowned and missing entries both return 404, avoiding resource disclosure.
Ownership is checked in SQL rather than by loading all user journals into
memory. Pagination remains a later phase.
