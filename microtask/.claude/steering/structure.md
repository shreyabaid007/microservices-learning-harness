# structure.md

## Architectural Pattern

**Hexagonal Architecture (Ports and Adapters)** applied within each Spring Boot service.

```
Domain Layer         ← pure business entities and domain services (no framework deps)
Application Layer    ← use case orchestration; depends only on domain + port interfaces
Ports (Interfaces)   ← inbound (use case interfaces) and outbound (repository/external interfaces)
Adapters             ← inbound: REST controllers; outbound: JPA repositories, JWT utilities
Infrastructure       ← Spring Boot wiring, config, Flyway migrations, security filter chain
```

Dependency direction: `adapters → application → domain`. Domain has zero outward dependencies.

---

## Repository Layout

```
microtask/
├── identity-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/identity/
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── User.java                  # Domain entity (UUID id, email, passwordHash, timestamps)
│   │   │   │   │   └── service/
│   │   │   │   │       └── PasswordHasher.java         # Domain port: hash + verify
│   │   │   │   ├── application/
│   │   │   │   │   ├── port/
│   │   │   │   │   │   ├── in/
│   │   │   │   │   │   │   ├── RegisterUserUseCase.java
│   │   │   │   │   │   │   ├── LoginUseCase.java
│   │   │   │   │   │   │   └── GetCurrentUserUseCase.java
│   │   │   │   │   │   └── out/
│   │   │   │   │   │       ├── UserRepository.java     # Outbound port
│   │   │   │   │   │       └── TokenIssuer.java        # Outbound port: sign JWT
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── RegisterUserService.java    # Implements RegisterUserUseCase
│   │   │   │   │       ├── LoginService.java
│   │   │   │   │       └── GetCurrentUserService.java
│   │   │   │   ├── adapter/
│   │   │   │   │   ├── in/
│   │   │   │   │   │   └── web/
│   │   │   │   │   │       ├── AuthController.java     # POST /register, POST /login, GET /me
│   │   │   │   │   │       ├── dto/
│   │   │   │   │   │       │   ├── RegisterRequest.java
│   │   │   │   │   │       │   ├── LoginRequest.java
│   │   │   │   │   │       │   └── UserResponse.java
│   │   │   │   │   │       └── JwtAuthFilter.java      # Validates JWT for /me
│   │   │   │   │   └── out/
│   │   │   │   │       ├── persistence/
│   │   │   │   │       │   ├── UserJpaRepository.java  # Spring Data interface
│   │   │   │   │       │   ├── UserEntity.java         # JPA @Entity
│   │   │   │   │       │   └── UserRepositoryAdapter.java # Implements UserRepository port
│   │   │   │   │       └── jwt/
│   │   │   │   │           ├── JwtTokenIssuer.java     # Implements TokenIssuer port; signs with RSA private key
│   │   │   │   │           └── JwksController.java     # GET /.well-known/jwks.json; serves RSA public key
│   │   │   │   └── infrastructure/
│   │   │   │       ├── config/
│   │   │   │       │   ├── SecurityConfig.java
│   │   │   │       │   └── JwtConfig.java              # Binds JWT_PRIVATE_KEY_PATH, JWT_PUBLIC_KEY_PATH, JWT_EXPIRATION_MS
│   │   │   │       └── IdentityServiceApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/
│   │   │           └── V1__create_users_table.sql
│   │   └── test/
│   │       └── java/com/example/identity/
│   │           ├── unit/
│   │           │   ├── usecase/
│   │           │   │   ├── RegisterUserServiceTest.java
│   │           │   │   └── LoginServiceTest.java
│   │           │   └── adapter/jwt/
│   │           │       └── JwtTokenIssuerTest.java
│   │           └── integration/
│   │               └── web/
│   │                   └── AuthControllerIT.java       # Testcontainers + full Spring context
│   ├── Dockerfile
│   ├── .env.example
│   └── README.md
│
├── task-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/task/
│   │   │   │   ├── domain/
│   │   │   │   │   └── model/
│   │   │   │   │       └── Task.java                   # Domain entity (UUID id, userId, title, description, dueDate, isCompleted, timestamps)
│   │   │   │   ├── application/
│   │   │   │   │   ├── port/
│   │   │   │   │   │   ├── in/
│   │   │   │   │   │   │   ├── CreateTaskUseCase.java
│   │   │   │   │   │   │   ├── GetTasksUseCase.java
│   │   │   │   │   │   │   ├── UpdateTaskUseCase.java
│   │   │   │   │   │   │   └── DeleteTaskUseCase.java
│   │   │   │   │   │   └── out/
│   │   │   │   │   │       └── TaskRepository.java     # Outbound port
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── CreateTaskService.java
│   │   │   │   │       ├── GetTasksService.java
│   │   │   │   │       ├── UpdateTaskService.java      # Verifies task.userId == authenticated userId
│   │   │   │   │       └── DeleteTaskService.java      # Verifies task.userId == authenticated userId
│   │   │   │   ├── adapter/
│   │   │   │   │   ├── in/
│   │   │   │   │   │   └── web/
│   │   │   │   │   │       ├── TaskController.java     # POST/GET /tasks, PUT/DELETE /tasks/{id}
│   │   │   │   │   │       ├── dto/
│   │   │   │   │   │       │   ├── CreateTaskRequest.java
│   │   │   │   │   │       │   ├── UpdateTaskRequest.java
│   │   │   │   │   │       │   └── TaskResponse.java
│   │   │   │   │   │       └── JwtAuthFilter.java      # Validates JWT; sets userId in SecurityContext
│   │   │   │   │   └── out/
│   │   │   │   │       └── persistence/
│   │   │   │   │           ├── TaskJpaRepository.java
│   │   │   │   │           ├── TaskEntity.java
│   │   │   │   │           └── TaskRepositoryAdapter.java
│   │   │   │   └── infrastructure/
│   │   │   │       ├── config/
│   │   │   │       │   ├── SecurityConfig.java
│   │   │   │       │   └── JwtConfig.java              # Binds JWT_PUBLIC_KEY_PATH for RS256 validation only
│   │   │   │       └── TaskServiceApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/
│   │   │           └── V1__create_tasks_table.sql
│   │   └── test/
│   │       └── java/com/example/task/
│   │           ├── unit/
│   │           │   └── usecase/
│   │           │       ├── CreateTaskServiceTest.java
│   │           │       ├── UpdateTaskServiceTest.java  # Tests ownership enforcement
│   │           │       └── DeleteTaskServiceTest.java
│   │           └── integration/
│   │               └── web/
│   │                   └── TaskControllerIT.java       # Testcontainers + JWT fixture
│   ├── Dockerfile
│   ├── .env.example
│   └── README.md
│
└── docker-compose.yml
```

---

## Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| Java packages | `com.example.<service>.<layer>` | `com.example.identity.domain.model` |
| Use case interfaces | `<Verb><Noun>UseCase` | `RegisterUserUseCase` |
| Use case implementations | `<Verb><Noun>Service` | `RegisterUserService` |
| Outbound port interfaces | `<Noun>Repository`, `<Noun>Issuer` | `UserRepository`, `TokenIssuer` |
| JPA adapters | `<Noun>RepositoryAdapter` | `UserRepositoryAdapter` |
| REST DTOs | `<Action>Request`, `<Noun>Response` | `LoginRequest`, `UserResponse` |
| DB migrations | `V<n>__<description>.sql` | `V1__create_users_table.sql` |
| Integration tests | `<Subject>IT.java` | `AuthControllerIT.java` |
| Environment variables | `UPPER_SNAKE_CASE` | `JWT_SECRET`, `DB_URL` |

---

## Data Flow

### Registration
```
POST /register
  → AuthController (adapter/in)
  → RegisterUserUseCase (port/in)
  → RegisterUserService (application)
      → PasswordHasher.hash(password)
      → UserRepository.save(user) (port/out)
          → UserRepositoryAdapter → UserJpaRepository (adapter/out)
  ← UserResponse
```

### Login
```
POST /login
  → AuthController
  → LoginUseCase → LoginService
      → UserRepository.findByEmail()
      → PasswordHasher.verify(raw, hash)
      → TokenIssuer.issue(userId) (port/out)
          → JwtTokenIssuer (adapter/out)
  ← { token: "..." }
```

### Authenticated Task Request
```
PUT /tasks/{id}  [Authorization: Bearer <token>]
  → JwtAuthFilter: validate RS256 token → extract userId → set in SecurityContext
  → TaskController
  → UpdateTaskUseCase → UpdateTaskService
      → TaskRepository.findByIdAndUserId(id, authenticatedUserId)  ← single query, ownership enforced at DB
          → returns empty Optional if id exists but belongs to another user
          → throws TaskNotFoundException → HTTP 404  (never 403; do not leak task existence)
      → TaskRepository.save(updated)
  ← TaskResponse
```

---

## Environment Variables

### Identity Service `.env.example`
```
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/identity_db
DB_USERNAME=postgres
DB_PASSWORD=secret
JWT_PRIVATE_KEY_PATH=/run/secrets/jwt_private.pem
JWT_PUBLIC_KEY_PATH=/run/secrets/jwt_public.pem
JWT_EXPIRATION_MS=3600000
```

### Task Service `.env.example`
```
SERVER_PORT=8081
DB_URL=jdbc:postgresql://localhost:5432/task_db
DB_USERNAME=postgres
DB_PASSWORD=secret
JWT_PUBLIC_KEY_PATH=/run/secrets/jwt_public.pem
```

---

## Docker Compose Structure

```yaml
# docker-compose.yml (root)
services:
  identity-db:      # PostgreSQL for Identity Service
  task-db:          # PostgreSQL for Task Service (separate DB)
  identity-service: # Builds identity-service/Dockerfile; depends_on identity-db
  task-service:     # Builds task-service/Dockerfile; depends_on task-db
```

Each service image: multi-stage Dockerfile (`maven:3.9-eclipse-temurin-17` build → `eclipse-temurin:17-jre` runtime).

---

## Module Boundaries

- Domain must not import Spring, JPA, or any framework class
- Application layer imports only domain classes and its own port interfaces
- Adapters import Spring/JPA; they implement ports from application layer
- Infrastructure wires everything via Spring `@Configuration` and `@Bean`; constructor injection only — no `@Autowired` on fields
- Cross-cutting: JWT filter lives in `adapter/in/web`; JWT config lives in `infrastructure/config`
- Multi-tenancy enforcement: outbound `TaskRepository` port exposes `findByIdAndUserId(UUID id, UUID userId)` — ownership is enforced at the query boundary, not in application logic; missing or unowned tasks always produce `TaskNotFoundException` → HTTP 404
