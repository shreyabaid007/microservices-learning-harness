# Microtask

![Java 17](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue)
![Docker](https://img.shields.io/badge/Docker-blue)
![Testcontainers](https://img.shields.io/badge/Testcontainers-orange)

Two Spring Boot services — **identity-service** issues RS256 JWTs, **task-service** verifies them locally via JWKS and serves per-user task CRUD. Java 17, Spring Boot 3.4, PostgreSQL, hexagonal architecture, Testcontainers-backed integration tests.

## Quickstart

Requires Docker (with `docker compose`), `curl`, and `jq`.

### 1. Start the stack
```bash
docker compose up --build -d
docker compose ps
# identity-service: http://localhost:8080
# task-service: http://localhost:8081
```

### 2. Register a user

```bash
curl -s -X POST http://localhost:8080/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"s3cret"}'
# => 201
```

### 3. Verify user in DB

```
docker compose exec identity-db \
  psql -U postgres -d identity_db -c \
  "SELECT id, email, left(password_hash, 7) AS hash_prefix FROM users WHERE email='alice@example.com';"
```

### 4. Login and store token
```
ALICE=$(curl -s -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"s3cret"}' | jq -r .token)
```

### 5. Call /me
```
curl -s http://localhost:8080/me -H "Authorization: Bearer $ALICE"
# => 200
```

### Task API

### 6. Create a task

```bash
ID=$(curl -s -X POST http://localhost:8081/tasks \
  -H "Authorization: Bearer $ALICE" -H 'Content-Type: application/json' \
  -d '{"title":"Buy groceries","dueDate":"2026-05-01"}' | jq -r .id)
# => 201
```

### 7. Verify task in DB

```
docker compose exec task-db \
  psql -U postgres -d task_db -c \
  "SELECT id, user_id, title, due_date, is_completed FROM tasks WHERE id='$ID';"
```

### 8. List tasks

```
curl -s http://localhost:8081/tasks -H "Authorization: Bearer $ALICE"
# => 200  [ TaskResponse, ... ]
```

### 9. Update task

```
curl -s -X PUT "http://localhost:8081/tasks/$ID" \
  -H "Authorization: Bearer $ALICE" -H 'Content-Type: application/json' \
  -d '{"title":"Buy groceries (done)","isCompleted":true}'
```

### 10. Delete task

```
curl -s -X DELETE "http://localhost:8081/tasks/$ID" \
  -H "Authorization: Bearer $ALICE" -o /dev/null -w '%{http_code}\n'
# => 204
```

### 11. Stop everything

```bash
docker compose down -v
```

### Live API docs (when the stack is up):

- Identity: <http://localhost:8080/swagger-ui.html> · <http://localhost:8080/v3/api-docs>
- Task: <http://localhost:8081/swagger-ui.html> · <http://localhost:8081/v3/api-docs>

## Build & test

```bash
./mvnw verify                          # full build: unit + IT + ArchUnit + 
./mvnw -pl identity-service test       # unit tests, identity service only
./mvnw -pl task-service verify         # full build, task service only
./mvnw spotless:apply                  # auto-format
```

Integration tests spin up real PostgreSQL via Testcontainers. The task-service IT generates its own RSA keypair and serves a JWKS from a temp file via `@DynamicPropertySource`, so it does not need a running identity-service.

## What's in this repo

| Path | Purpose |
|---|---|
| [identity-service/](identity-service/) | Spring Boot service — register / login / `/me` / JWKS |
| [task-service/](task-service/) | Spring Boot service — task CRUD, JWT-protected, multi-tenant |
| [specs/](specs/) | Interview transcript and per-service functional specs |
| [.claude/](.claude/) | Steering files, hooks, and agent config used during AI-assisted dev |
| [docker-compose.yml](docker-compose.yml) | Orchestrates both services + their PostgreSQL databases |
| [pom.xml](pom.xml) | Multi-module Maven parent |

## AI-in-SDLC

How AI was used as a first-class part of the workflow, not just autocomplete. Reviewers should look here:

- [.claude/steering/](.claude/steering/) — locked architectural and tech decisions ([product.md](.claude/steering/product.md), [tech.md](.claude/steering/tech.md), [structure.md](.claude/steering/structure.md)) that override default model behavior.
- [specs/001-identity/spec.md](specs/001-identity/spec.md), [specs/002-task/spec.md](specs/002-task/spec.md) — per-feature acceptance checklists used as the AI's definition of done.
- [.claude/settings.json](.claude/settings.json) + [.claude/hooks/verify-on-stop.sh](.claude/hooks/verify-on-stop.sh) — `Stop` hook that runs `./mvnw verify` whenever the AI ends a turn, so green-build claims are machine-checked.
- [task-service/.../ArchitectureTest.java](task-service/src/test/java/com/example/task/architecture/ArchitectureTest.java) — ArchUnit rules that fail the build if generated code drifts from the hexagonal layering.

## Architecture at a glance

```
+------------------+   POST /register, /login, GET /me     +-------------+
|  identity-service|--------------------------------------▶| identity-db |
|       :8080      |                                        +-------------+
+------------------+
        │ GET /.well-known/jwks.json   (read once at startup)
        ▼
+------------------+   POST/GET/PUT/DELETE /tasks          +-------------+
|   task-service   |--------------------------------------▶|   task-db   |
|       :8081      |  (verifies RS256 JWT locally)          +-------------+
+------------------+
```



