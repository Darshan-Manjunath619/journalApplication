# Journal Application API

The supported browser/client contract is `/api/v1/**`. Legacy unversioned
controllers are intentionally excluded from the generated documentation.

## OpenAPI and Swagger UI

With the application running locally:

- Swagger UI: `http://localhost:8080/journal/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/journal/v3/api-docs/v1`

Swagger UI is public so developers can read the contract. Protected operations
still require a JWT through the **Authorize** button.

## Authentication example

1. Call `POST /api/v1/auth/login` with username and password.
2. The JSON response contains a 15-minute access token.
3. The response also sets a seven-day rotating refresh token as an HttpOnly
   cookie; JavaScript cannot read that cookie.
4. Enter `Bearer <access-token>` in Swagger authorization, or send it as:

```http
Authorization: Bearer eyJ...
```

5. Call `POST /api/v1/auth/refresh` when the access token expires. The browser
   sends the refresh cookie and the backend rotates it.

## Error contract

Validation, authentication, authorization, conflicts, and missing resources use
Spring Problem Detail JSON. The `X-Correlation-ID` response header and
`correlationId` error property identify the same request in server logs.

Typical status codes:

| Status | Meaning |
|---|---|
| `400` | Invalid body or query parameter |
| `401` | Authentication or token is invalid |
| `403` | Authenticated request is not allowed |
| `404` | Owned resource was not found |
| `409` | Unique-name/email conflict or tag is in use |

## Operational health

`GET http://localhost:8080/journal/actuator/health` is public for load balancers
and deployment checks. It reports only the overall status; component details and
all other Actuator endpoints are not publicly exposed. Core health explicitly
includes MySQL, disk space, and the application ping. Optional Redis and mail
integrations do not make the core service unhealthy when they are disabled.

```json
{"status":"UP"}
```

## Journal pagination

`GET /api/v1/journals` supports `page`, `size`, `q`, `from`, `to`, `favorite`,
`tag`, and `sort`. For example:

```http
GET /api/v1/journals?page=0&size=20&q=spring&favorite=true&sort=createdAt,desc
```

Allowed sort fields are `createdAt`, `updatedAt`, and `title`; size is limited
to 1–100.
