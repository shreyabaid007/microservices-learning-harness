# Task Service

Per-user task CRUD, protected by JWT (RS256). Verifies tokens locally with the public key fetched once from the Identity Service's JWKS endpoint at startup.

Default port: **8081**.

## Configuration

Environment variables (see [.env.example](.env.example)):

| Var | Description |
|---|---|
| `SERVER_PORT` | HTTP port (default 8081) |
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | DB user |
| `DB_PASSWORD` | DB password |
| `IDENTITY_JWKS_URI` | URL of the Identity Service `jwks.json` (read once at startup) |

If JWKS loading fails at startup, the application fails fast.

## Run

### With Docker Compose (from repo root)
```bash
docker compose up --build
```

### Locally with Maven
```bash
docker run --rm -d --name task-db -e POSTGRES_DB=task_db \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret \
  -p 5433:5432 postgres:16-alpine

# Identity Service must already be reachable at IDENTITY_JWKS_URI
export $(grep -v '^#' task-service/.env.example | xargs)
./mvnw -pl task-service spring-boot:run
```

## Endpoints

All `/tasks*` endpoints require `Authorization: Bearer <jwt>`. Issue a token from Identity Service first.

| Method | Path | Purpose |
|---|---|---|
| POST | `/tasks` | Create task |
| GET | `/tasks` | List caller's tasks |
| PUT | `/tasks/{id}` | Replace task fields |
| DELETE | `/tasks/{id}` | Delete task |
| GET | `/swagger-ui.html` | Live API docs |

Tasks not owned by the caller return `404` (never `403`).

### Create
```bash
curl -s -X POST http://localhost:8081/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Buy groceries","description":"Milk, eggs","dueDate":"2026-05-01","isCompleted":false}'
# => 201 TaskResponse
```
- `400` on missing or blank `title`
- `401` on bad/missing token

### List
```bash
curl -s http://localhost:8081/tasks -H "Authorization: Bearer $TOKEN"
# => 200 [TaskResponse, ...]    (only the caller's tasks; [] if none)
```

### Update
```bash
curl -s -X PUT "http://localhost:8081/tasks/$ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Buy groceries (updated)","isCompleted":true}'
# => 200 TaskResponse
```
- `404` if the task doesn't exist or belongs to another user

### Delete
```bash
curl -s -X DELETE "http://localhost:8081/tasks/$ID" \
  -H "Authorization: Bearer $TOKEN" -o /dev/null -w '%{http_code}\n'
# => 204
```
- `404` if the task doesn't exist or belongs to another user

### TaskResponse
```json
{
  "id": "550e8400-...",
  "userId": "a1b2c3d4-...",
  "title": "Buy groceries",
  "description": "Milk, eggs",
  "dueDate": "2026-05-01",
  "isCompleted": false,
  "createdAt": "2026-04-29T07:00:00Z",
  "updatedAt": "2026-04-29T07:00:00Z"
}
```

## Build & test

```bash
./mvnw -pl task-service verify    # unit + IT (Testcontainers PG) + spotless + jacoco
```
