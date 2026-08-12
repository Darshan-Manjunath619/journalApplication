# Phase 1.1C - Logging, Correlation IDs, and Structural Hygiene

## Purpose

This increment makes request diagnostics consistent and makes dependencies
explicit without changing the API's business behavior.

## Request correlation flow

1. CorrelationIdFilter reads the optional X-Correlation-ID request header.
2. A safe value is reused; otherwise, the application creates a UUID.
3. The value is stored as a request attribute and in SLF4J MDC.
4. Log lines include the correlation ID, and the response returns the same
   X-Correlation-ID header.
5. GlobalExceptionHandler includes the ID in Problem Detail error responses.
6. The MDC value is removed in a finally block so servlet worker threads
   cannot leak one request's identifier into another request.

## Structural cleanup

- Request-path controllers, security classes, services, configuration, and the
  scheduler use constructor injection.
- Redis configuration and service types are explicit.
- Logging uses parameterized SLF4J calls.
- Lombok builder defaults preserve initialized user roles and journal lists.

## Verification

Run: .\mvnw.cmd clean test

Result: 29 tests run, 0 failures, 0 errors, and 3 intentionally skipped
external-integration tests.

The tests verify safe incoming IDs, replacement of unsafe IDs, response
headers, Problem Detail correlation properties, MDC cleanup on success and
failure, application context wiring, and existing controller/service behavior.

## Production considerations

An edge proxy may generate correlation IDs before traffic reaches this
application. The application still validates the value to prevent unbounded or
control-character input from contaminating logs. Correlation IDs aid tracing
and support investigations, but they are not authentication credentials and
must not be trusted for authorization.
