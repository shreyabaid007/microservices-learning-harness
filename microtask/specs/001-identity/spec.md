# Identity Service — Specification

## Functional Requirements

### FR-1: User Registration
- Accept `POST /register` with a JSON body containing `email` (string) and `password` (string).
- Validate that `email` is a well-formed email address and `password` is non-blank.
- Reject duplicate email addresses with `409 Conflict`.
- Store the user with a BCrypt-hashed password; never store the plaintext password.
- Return `201 Created` with a `UserResponse` body on success.

### FR-2: User Login
- Accept `POST /login` with a JSON body containing `email` and `password`.
- Look up the user by email. If not found or password does not match the stored BCrypt hash, return `401 Unauthorized`. Do not distinguish between "user not found" and "wrong password" in the response body.
- On success, issue an RS256-signed JWT and return `200 OK` with `{ "token": "<jwt>" }`.

### FR-3: Get Current User
- Accept `GET /me` with an `Authorization: Bearer <token>` header.
- Validate the JWT (algorithm RS256, signature, `exp` claim). Reject invalid or expired tokens with `401 Unauthorized`.
- Return `200 OK` with a `UserResponse` body containing the authenticated user's `id`, `email`, `createdAt`.

### FR-4: JWKS Endpoint
- Expose `GET /.well-known/jwks.json` — publicly accessible, no authentication required.
- Return the RSA public key in JWKS format (RFC 7517) so that external services (Task Service) can verify tokens without contacting Identity Service per-request.
- The JWKS must include `kty`, `use`, `alg`, `kid`, `n`, and `e` fields.

---

## Non-Functional Requirements

### NFR-1: Password Security
- BCrypt via Spring Security `BCryptPasswordEncoder`. Default strength factor (10) unless a higher value is justified.
- Plaintext password must never appear in logs, responses, or persistence layer.

### NFR-2: JWT Algorithm and Key Lifecycle
- Algorithm: RS256 (asymmetric). Identity Service holds the RSA private key; Task Service holds only the public key.
- Key pair is generated in-memory at application startup using `KeyPairGenerator` with a 2048-bit RSA key.
- No PEM files are required on the filesystem; `JWT_PRIVATE_KEY_PATH` / `JWT_PUBLIC_KEY_PATH` env vars are not used in this in-memory strategy.
- Token expiry is configurable via `JWT_EXPIRATION_MS` environment variable (default: 3600000 ms / 1 hour).
- Payload claims: `sub` (user UUID as string), `iat`, `exp`. No custom claims beyond these.
- All tokens become invalid when Identity Service restarts (key pair is regenerated). This is a known and accepted trade-off.

### NFR-3: Architecture
- Hexagonal Architecture (Ports and Adapters) as defined in `structure.md`. Domain layer has zero Spring/JPA imports.
- Constructor injection only throughout; no `@Autowired` on fields.
- Lombok allowed only on JPA `@Entity` classes (`@Getter`, `@Setter`, `@NoArgsConstructor`).

### NFR-4: Persistence
- PostgreSQL via Spring Data JPA + Hibernate. Schema managed by Flyway.
- No H2 in any scope.

### NFR-5: Testing
- Unit tests (JUnit 5 + Mockito) cover use case services and JWT token issuer.
- Integration tests (Spring Boot Test + Testcontainers PostgreSQL) cover the full HTTP stack via `AuthControllerIT`.
- Integration tests for the JWT filter use the in-memory key pair produced by the application context under test.

### NFR-6: API Documentation
- SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui`) generates Swagger UI at runtime.
- Live endpoints: `GET /swagger-ui.html`, `GET /v3/api-docs`.
- No spec file committed to the repository.

### NFR-7: Logging
- Spring Boot default logback configuration only. No custom `logback-spring.xml`.
- No request/response body logging. No MDC / trace IDs.

### NFR-8: Configuration
- All secrets and configurable values come from environment variables. Nothing hardcoded.
- Required env vars: `SERVER_PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_EXPIRATION_MS`.

---

## Explicit Non-Goals

- No refresh tokens or `/auth/refresh` endpoint.
- No token revocation, denylist, or `/logout` endpoint.
- No account lockout or failed-attempt tracking.
- No login rate limiting.
- No RBAC or roles.
- No OAuth2 / social login.
- No password reset or email verification.
- No admin endpoints.
- No frontend or UI.
- No audit logging.

---

## Data Model

### Table: `users`

| Column | Type | Constraints |
|---|---|---|
| `id` | `UUID` | `PRIMARY KEY`, default `gen_random_uuid()` |
| `email` | `VARCHAR(255)` | `UNIQUE NOT NULL` |
| `password_hash` | `VARCHAR(255)` | `NOT NULL` |
| `created_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT now()` |
| `updated_at` | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT now()` |

Migration file: `V1__create_users_table.sql`

---

## API Contract

### `POST /register`

**Request**
```json
{
  "email": "user@example.com",
  "password": "s3cr3tP@ss"
}
```

**Responses**

| Status | Body | Condition |
|---|---|---|
| `201 Created` | `UserResponse` | Registration successful |
| `400 Bad Request` | Error body | Missing / malformed fields |
| `409 Conflict` | Error body | Email already registered |

**UserResponse**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "createdAt": "2025-01-01T12:00:00Z"
}
```

---

### `POST /login`

**Request**
```json
{
  "email": "user@example.com",
  "password": "s3cr3tP@ss"
}
```

**Responses**

| Status | Body | Condition |
|---|---|---|
| `200 OK` | `{ "token": "<jwt>" }` | Credentials valid |
| `401 Unauthorized` | Error body | Unknown email or wrong password (response does not distinguish) |

---

### `GET /me`

**Request headers**
```
Authorization: Bearer <jwt>
```

**Responses**

| Status | Body | Condition |
|---|---|---|
| `200 OK` | `UserResponse` | Token valid and not expired |
| `401 Unauthorized` | Error body | Missing, malformed, or expired token |

---

### `GET /.well-known/jwks.json`

No authentication required.

**Response** `200 OK`
```json
{
  "keys": [
    {
      "kty": "RSA",
      "use": "sig",
      "alg": "RS256",
      "kid": "<key-id>",
      "n": "<base64url-encoded modulus>",
      "e": "<base64url-encoded exponent>"
    }
  ]
}
```

---

## Acceptance Checklist

### Registration
- [ ] `POST /register` with valid email and password returns `201` and a `UserResponse` with non-null `id`, matching `email`, and non-null `createdAt`.
- [ ] `POST /register` with the same email a second time returns `409`.
- [ ] `POST /register` with a missing `password` field returns `400`.
- [ ] `POST /register` with a malformed email (e.g., `"notanemail"`) returns `400`.
- [ ] The `password_hash` stored in the database is not equal to the plaintext password.
- [ ] The `password_hash` stored in the database starts with `$2a$` (BCrypt indicator).

### Login
- [ ] `POST /login` with correct credentials returns `200` and a non-empty `token` string.
- [ ] The returned token decodes as a valid RS256 JWT with `sub` equal to the user's UUID.
- [ ] The returned token contains `iat` and `exp` claims; `exp - iat` equals `JWT_EXPIRATION_MS / 1000` seconds.
- [ ] `POST /login` with an unknown email returns `401`.
- [ ] `POST /login` with a correct email but wrong password returns `401`.
- [ ] The `401` body does not reveal whether the email exists.

### Get Current User
- [ ] `GET /me` with a valid token returns `200` and a `UserResponse` matching the token's `sub`.
- [ ] `GET /me` with no `Authorization` header returns `401`.
- [ ] `GET /me` with a malformed token (not a JWT) returns `401`.
- [ ] `GET /me` with an expired token returns `401`.
- [ ] `GET /me` with a token signed by a different key returns `401`.

### JWKS Endpoint
- [ ] `GET /.well-known/jwks.json` returns `200` with `Content-Type: application/json`.
- [ ] The response body contains a `keys` array with exactly one entry.
- [ ] The entry contains `kty`, `use`, `alg`, `kid`, `n`, and `e` fields.
- [ ] The `alg` field is `"RS256"`.
- [ ] The endpoint is accessible without an `Authorization` header.

### Architecture / Code Quality
- [ ] Domain classes (`User`, `PasswordHasher`) import zero Spring or JPA classes.
- [ ] No `@Autowired` field injection exists anywhere in the codebase.
- [ ] No H2 dependency appears in the build file.
- [ ] Lombok annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`) appear only on `@Entity` classes.
- [ ] All integration tests use Testcontainers to spin up a real PostgreSQL instance.
