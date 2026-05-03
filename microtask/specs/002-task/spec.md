# Task Service — Specification

## Functional Requirements

### FR-1: Create Task

- Accept `POST /tasks` with a valid `Authorization: Bearer <token>` header and a JSON body.
- Required field: `title` (non-blank string).
- Optional fields: `description` (string), `dueDate` (ISO-8601 date string `YYYY-MM-DD`), `isCompleted` (boolean, default `false`).
- Create the task with `userId` set to the `sub` claim of the authenticated JWT.
- Return `201 Created` with a `TaskResponse` body.

### FR-2: List Tasks

- Accept `GET /tasks` with a valid `Authorization: Bearer <token>` header.
- Return `200 OK` with a JSON array of `TaskResponse` objects belonging exclusively to the authenticated user.
- An authenticated user with no tasks receives an empty array `[]`, not a 404.
- Tasks from other users must never appear in the response.

### FR-3: Update Task

- Accept `PUT /tasks/{id}` with a valid `Authorization: Bearer <token>` header and a JSON body.
- Updatable fields: `title`, `description`, `dueDate`, `isCompleted`. All fields in the body replace the existing values.
- If `{id}` does not exist, or exists but belongs to a different user, return `404 Not Found`. Never return `403`.
- Return `200 OK` with the updated `TaskResponse` on success.

### FR-4: Delete Task

- Accept `DELETE /tasks/{id}` with a valid `Authorization: Bearer <token>` header.
- If `{id}` does not exist, or exists but belongs to a different user, return `404 Not Found`. Never return `403`.
- Return `204 No Content` on successful deletion.

---

## Non-Functional Requirements

### NFR-1: JWT Validation

- Algorithm: RS256. Task Service holds only the RSA public key; it never holds a private key.
- Public key is loaded once at application startup from the Identity Service's `GET /.well-known/jwks.json` endpoint (URL configurable via `IDENTITY_JWKS_URI` env var).
- After startup, Task Service makes no further HTTP calls to Identity Service. All JWT validation is local.
- A request with a missing, malformed, expired, or wrongly-signed token returns `401 Unauthorized`.
- If JWKS loading fails at startup, the application fails fast and must not start in a degraded state.

### NFR-2: Multi-Tenancy Enforcement

- Ownership is enforced at the database query boundary, not in application-layer conditionals.
- The `TaskRepository` outbound port exposes `findByIdAndUserId(UUID id, UUID userId)`. The JPA query is `WHERE id = :id AND user_id = :userId`.
- An `Optional.empty()` result — whether the task doesn't exist or belongs to another user — maps to `TaskNotFoundException`, which maps to `HTTP 404`.
- The distinction between "task not found" and "task owned by another user" is never revealed to the caller.

### NFR-3: Architecture

- Hexagonal Architecture (Ports and Adapters) as defined in `structure.md`. Domain layer has zero Spring/JPA imports.
- Constructor injection only throughout; no `@Autowired` on fields.
- Lombok allowed only on JPA `@Entity` classes (`@Getter`, `@Setter`, `@NoArgsConstructor`).

### NFR-4: Persistence

- PostgreSQL via Spring Data JPA + Hibernate. Schema managed by Flyway.
- No H2 in any scope.

### NFR-5: Testing

- Unit tests (JUnit 5 + Mockito) cover `CreateTaskService`, `UpdateTaskService`, and `DeleteTaskService`. Ownership enforcement logic is explicitly unit-tested in `UpdateTaskServiceTest` and `DeleteTaskServiceTest`.
- Integration tests (Spring Boot Test + Testcontainers PostgreSQL) cover the full HTTP stack via `TaskControllerIT`.
- `TaskControllerIT` uses a **test-local RSA key fixture**: the test class generates (or loads from `src/test/resources`) its own RSA key pair, registers the public key with the Spring context via `@DynamicPropertySource`, and signs test JWTs with the private key. No WireMock, no running Identity Service.

### NFR-6: API Documentation

- SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui`) generates Swagger UI at runtime.
- Live endpoints: `GET /swagger-ui.html`, `GET /v3/api-docs`.
- No spec file committed to the repository.

### NFR-7: Logging

- Spring Boot default logback configuration only. No custom `logback-spring.xml`.
- No request/response body logging. No MDC / trace IDs.

### NFR-8: Configuration

- All secrets and configurable values come from environment variables. Nothing hardcoded.
- Required env vars: `SERVER_PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `IDENTITY_JWKS_URI`.

---

## Explicit Non-Goals

- No pagination, filtering, or sorting of task lists.
- No task sharing between users.
- No RBAC or task categories/labels.
- No admin endpoints.
- No inter-service HTTP calls at request time (only at startup for JWKS loading).
- No frontend or UI.
- No audit logging.

---

## Data Model

### Table: `tasks`


| Column         | Type                       | Constraints                                                                                   |
| -------------- | -------------------------- | --------------------------------------------------------------------------------------------- |
| `id`           | `UUID`                     | `PRIMARY KEY`, default `gen_random_uuid()`                                                    |
| `user_id`      | `UUID`                     | `NOT NULL` (references the user UUID from Identity Service; no FK constraint across services) |
| `title`        | `VARCHAR(255)`             | `NOT NULL`                                                                                    |
| `description`  | `TEXT`                     | nullable                                                                                      |
| `due_date`     | `DATE`                     | nullable                                                                                      |
| `is_completed` | `BOOLEAN`                  | `NOT NULL DEFAULT FALSE`                                                                      |
| `created_at`   | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT now()`                                                                      |
| `updated_at`   | `TIMESTAMP WITH TIME ZONE` | `NOT NULL DEFAULT now()`                                                                      |


Migration file: `V1__create_tasks_table.sql`

Index: `CREATE INDEX idx_tasks_user_id ON tasks(user_id)` — supports the `GET /tasks` list query.

Note: `user_id` carries no foreign key to the `users` table. The two services use separate databases; referential integrity is enforced by the JWT's `sub` claim at the application boundary.

---

## API Contract

All endpoints require `Authorization: Bearer <token>` unless otherwise noted.

### `POST /tasks`

**Request**

```json
{
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "dueDate": "2025-02-01",
  "isCompleted": false
}
```

**Responses**


| Status             | Body           | Condition                          |
| ------------------ | -------------- | ---------------------------------- |
| `201 Created`      | `TaskResponse` | Task created successfully          |
| `400 Bad Request`  | Error body     | Missing or blank `title`           |
| `401 Unauthorized` | Error body     | Missing, invalid, or expired token |


---

### `GET /tasks`

**Responses**


| Status             | Body             | Condition                                        |
| ------------------ | ---------------- | ------------------------------------------------ |
| `200 OK`           | `TaskResponse[]` | Returns array (possibly empty) of caller's tasks |
| `401 Unauthorized` | Error body       | Missing, invalid, or expired token               |


---

### `PUT /tasks/{id}`

**Request**

```json
{
  "title": "Buy groceries (updated)",
  "description": "Milk, eggs, bread, butter",
  "dueDate": "2025-02-05",
  "isCompleted": true
}
```

**Responses**


| Status             | Body           | Condition                                                  |
| ------------------ | -------------- | ---------------------------------------------------------- |
| `200 OK`           | `TaskResponse` | Task updated successfully                                  |
| `400 Bad Request`  | Error body     | Missing or blank `title`                                   |
| `401 Unauthorized` | Error body     | Missing, invalid, or expired token                         |
| `404 Not Found`    | Error body     | Task does not exist, or exists but belongs to another user |


---

### `DELETE /tasks/{id}`

**Responses**


| Status             | Body       | Condition                                                  |
| ------------------ | ---------- | ---------------------------------------------------------- |
| `204 No Content`   | (empty)    | Task deleted successfully                                  |
| `401 Unauthorized` | Error body | Missing, invalid, or expired token                         |
| `404 Not Found`    | Error body | Task does not exist, or exists but belongs to another user |


---

### `TaskResponse` shape

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "dueDate": "2025-02-01",
  "isCompleted": false,
  "createdAt": "2025-01-01T12:00:00Z",
  "updatedAt": "2025-01-01T12:00:00Z"
}
```

---

## Acceptance Checklist

### Authentication

- Any request to any `/tasks` endpoint without an `Authorization` header returns `401`.
- Any request with a malformed (non-JWT) token returns `401`.
- Any request with a token signed by a key not matching the loaded JWKS returns `401`.
- Any request with an expired token returns `401`.

### Create Task

- `POST /tasks` with a valid token and `{ "title": "T" }` returns `201` with a `TaskResponse` containing a non-null `id`, `userId` matching the token's `sub`, and `isCompleted: false`.
- `POST /tasks` with optional fields `description`, `dueDate`, `isCompleted` stores and returns them correctly.
- `POST /tasks` with a missing `title` returns `400`.
- `POST /tasks` with a blank `title` (whitespace only) returns `400`.
- Two different authenticated users can each create tasks; neither sees the other's task in subsequent list calls.

### List Tasks

- `GET /tasks` returns `200` with an array containing only the authenticated user's tasks.
- `GET /tasks` for a user with no tasks returns `200` with `[]`.
- Tasks created by User A do not appear in User B's `GET /tasks` response.

### Update Task

- `PUT /tasks/{id}` with a valid token and ownership returns `200` with all updated fields reflected.
- `PUT /tasks/{id}` where `{id}` does not exist returns `404`.
- `PUT /tasks/{id}` where `{id}` exists but belongs to a different user returns `404` (not `403`).
- `PUT /tasks/{id}` with a blank `title` returns `400`.

### Delete Task

- `DELETE /tasks/{id}` with a valid token and ownership returns `204` with an empty body.
- Subsequent `GET /tasks` does not include the deleted task.
- `DELETE /tasks/{id}` where `{id}` does not exist returns `404`.
- `DELETE /tasks/{id}` where `{id}` exists but belongs to a different user returns `404` (not `403`).

### Multi-Tenancy Enforcement

- The `TaskRepository` outbound port declares `findByIdAndUserId(UUID id, UUID userId)`.
- The corresponding JPA query filters by both `id` and `user_id` columns in a single query (no separate existence check followed by ownership check).
- `UpdateTaskService` and `DeleteTaskService` unit tests confirm that an `Optional.empty()` from the repository throws `TaskNotFoundException`.

### Architecture / Code Quality

- Domain class `Task` imports zero Spring or JPA classes.
- No `@Autowired` field injection exists anywhere in the codebase.
- No H2 dependency appears in the build file.
- Lombok annotations appear only on `@Entity` classes.
- `TaskControllerIT` configures the Spring context with a test-generated RSA public key via `@DynamicPropertySource` and issues signed JWTs locally — no WireMock, no running Identity Service.
- All integration tests use Testcontainers to spin up a real PostgreSQL instance.

