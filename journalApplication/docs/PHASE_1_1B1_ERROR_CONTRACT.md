# Phase 1.1B-1 — Validation and error contract

## Changes

- Added the Spring Boot Bean Validation starter.
- Added shared resource-not-found and conflict exceptions.
- Added centralized ProblemDetail responses for validation failures,
  malformed JSON, missing resources, conflicts, and unexpected failures.
- Added field-level validation details while hiding internal exception
  information from generic server-error responses.

## HTTP mappings

- 400: invalid fields or malformed JSON
- 404: requested resource does not exist
- 409: resource state or uniqueness conflict
- 500: unexpected failure with a safe client message

## Design impact

Controllers can now throw meaningful exceptions instead of duplicating
try/catch response construction. DTO adoption in the next increments will
activate the validation contract on real application requests.
