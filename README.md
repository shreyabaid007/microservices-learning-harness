# 🏗️ Microservices Learning Harness

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot 3.4](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://postgresql.org)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-orange?style=flat-square)](https://testcontainers.com)
[![Docker](https://img.shields.io/badge/Docker_Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](https://docker.com)

**Spec-driven, hexagonal-architecture microservices** — two Spring Boot services built from written specs, strict TDD, ArchUnit-enforced layering, and AI-in-the-SDLC as a first-class engineering practice.

This isn't a tutorial. It's a harness for practicing how production microservices should be built: specs before code, tests before implementation, architecture rules enforced at compile time.

---

## Architecture

```
┌──────────────────┐   POST /register, /login, GET /me   ┌─────────────┐
│ identity-service │ ──────────────────────────────────▶  │ identity-db │
│      :8080       │                                      │ (PostgreSQL)│
└──────────────────┘                                      └─────────────┘
        │ GET /.well-known/jwks.json (read once at startup)
        ▼
┌──────────────────┐   POST/GET/PUT/DELETE /tasks          ┌─────────────┐
│  task-service    │ ──────────────────────────────────▶   │   task-db   │
│      :8081       │  (verifies RS256 JWT locally)         │ (PostgreSQL)│
└──────────────────┘                                       └─────────────┘
```

## What makes this different

| Practice | How it's enforced |
|---|---|
| **Spec-driven development** | Written specs with acceptance checklists exist *before* any code — see `specs/` |
| **Hexagonal architecture** | Domain has zero framework imports. ArchUnit tests fail the build if layering drifts |
| **Strict TDD** | Red → Green → Refactor. JaCoCo gates: 80% domain, 70% application |
| **Real databases in tests** | Testcontainers spins up PostgreSQL — no H2, no mocks for persistence |
| **AI-in-SDLC** | Steering files lock decisions, hooks run `mvn verify` after every AI turn, specs are the AI's definition of done |

## Stack

| Layer | Choice |
|---|---|
| Language | Java 17 LTS |
| Framework | Spring Boot 3.4, Spring Security |
| Auth | RS256 JWT issuance (identity) + JWKS local verification (task) |
| Data | Spring Data JPA, PostgreSQL |
| Testing | JUnit 5, Testcontainers, ArchUnit, JaCoCo |
| Build | Maven (multi-module, Spotless for formatting) |
| Infra | Docker Compose (2 services + 2 databases) |

## Quickstart

```bash
cd microtask
docker compose up --build -d

# Register → Login → Create a task
curl -s -X POST http://localhost:8080/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"s3cret"}'

TOKEN=$(curl -s -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"s3cret"}' | jq -r .token)

curl -s -X POST http://localhost:8081/tasks \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Buy groceries","dueDate":"2026-05-01"}'
```

API docs at [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) and [localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html).

## Build & test

```bash
./mvnw verify                        # full: unit + IT + ArchUnit + coverage
./mvnw -pl identity-service test     # unit tests only, single module
./mvnw spotless:apply                # auto-format (google-java-format)
```

## Repo structure

| Path | Purpose |
|---|---|
| `specs/` | Interview transcript + per-service functional specs with acceptance checklists |
| `identity-service/` | User registration, login, JWT issuance, JWKS endpoint |
| `task-service/` | Task CRUD, JWT verification, multi-tenant isolation |
| `.claude/steering/` | Locked architectural decisions — product, tech, structure |
| `.claude/hooks/` | `verify-on-stop.sh` — runs `mvn verify` after every AI turn |
| `docker-compose.yml` | Full stack: 2 services + 2 PostgreSQL databases |

## License

MIT
