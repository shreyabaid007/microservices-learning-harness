# CLAUDE.md — Microtask Project

## Steering files (read first, non-negotiable)

All architectural, technology, and structural decisions are locked in:

- `.claude/steering/product.md` — functional scope and non-goals
- `.claude/steering/tech.md` — stack, libraries, constraints, and tradeoffs
- `.claude/steering/structure.md` — hexagonal architecture, package layout, naming conventions, data flow

Do not override decisions in those files without an explicit user instruction to update them first.

## Project layout

```
microtask/
├── identity-service/   Spring Boot service: user registration, login, JWT issuance
├── task-service/       Spring Boot service: task CRUD, JWT validation
└── specs/              Interview transcript and per-service specs
```

Each service is an independent Spring Boot application (separate JAR, separate Docker image, separate database).

## Specifications (read before implementing a feature)

- `specs/000-system/interview-transcript.md` — decisions made before coding (key strategy, non-goals)
- `specs/001-identity/spec.md` — Identity Service: functional requirements, data model, API contract, acceptance checklist
- `specs/002-task/spec.md` — Task Service: functional requirements, data model, API contract, acceptance checklist

Before writing any code for a feature, read the relevant spec in full. The acceptance checklist is the definition of done.

### When in doubt
Steering files override Claude's defaults. Specs override steering for feature-specific details. If steering and a spec conflict, stop and ask — do not silently choose.

## TDD workflow (mandatory)

1. **Red** — write the failing test first. Do not write production code before a test exists.
2. **Green** — write the minimum production code needed to make the test pass.
3. **Refactor** — clean up without changing behaviour; all tests must stay green.
4. **Verify** — run `./mvnw verify` before declaring a task done. The build must be green.

Unit tests live in `src/test/java/.../unit/`. Integration tests (`*IT.java`) live in `src/test/java/.../integration/`. Surefire runs unit tests; Failsafe runs `*IT.java` classes.

Coverage gates enforced by JaCoCo on every `verify`:
- Domain packages: 80% instruction coverage
- Application packages: 70% instruction coverage


## Forbidden (from tech.md — enforced strictly)

| Rule | Detail |
|---|---|
| No Lombok outside `@Entity` | Only `@Getter`, `@Setter`, `@NoArgsConstructor` on JPA entity classes |
| No field injection | Use constructor injection everywhere; `@Autowired` on fields is banned |
| No H2 | Testcontainers provides real PostgreSQL for every test scope |
| No SpringFox | Use `springdoc-openapi-starter-webmvc-ui` exclusively |
| No shared library | Services do not share a common module; duplication is acceptable |
| No preview features | Java 17 LTS baseline only |

## Architecture rules (enforced by ArchUnit)

Dependency direction: `adapters → application → domain`

- `domain` must not import Spring, JPA, or any framework class
- `application` imports only domain classes and its own port interfaces
- `adapters` import Spring/JPA and implement ports
- `infrastructure` wires via `@Configuration` and `@Bean`

## Key commands

```bash
./mvnw verify                  # compile + unit tests + ITs + coverage + spotless check
./mvnw spotless:apply          # auto-format all Java sources (google-java-format)
./mvnw test                    # unit tests only (no ITs, no spotless)
./mvnw -pl identity-service verify   # single-module verify
```
