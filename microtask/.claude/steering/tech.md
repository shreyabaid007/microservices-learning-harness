# tech.md

## Tech Stack

| Layer | Choice | Rationale |
|---|---|---|
| Language | Java 17 | Required by assignment |
| Framework | Spring Boot 3.x | Required by assignment; aligns with Java 17 (Jakarta EE namespace) |
| Web | Spring MVC (embedded Tomcat) | Standard REST in Spring Boot |
| Security | Spring Security 6 | JWT filter chain integration; pairs with Spring Boot 3 |
| JWT | `java-jwt` (Auth0) or `jjwt` (io.jsonwebtoken) | Mature libraries supporting RS256; configurable expiry |
| Password Hashing | BCrypt via Spring Security `BCryptPasswordEncoder` | Assignment requires secure hashing; BCrypt is natively supported |
| Database | PostgreSQL | Assignment specifies relational DB; pluggable via Spring Data JPA |
| ORM | Spring Data JPA + Hibernate | Standard persistence layer; reduces boilerplate |
| DB Migrations | Flyway | Versioned schema migrations; automatable in CI |
| API Docs | SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui`) | Generates Swagger UI from annotations; satisfies OpenAPI requirement |
| Testing (unit) | JUnit 5 + Mockito | Standard for Spring Boot unit tests |
| Testing (integration) | Spring Boot Test + Testcontainers (PostgreSQL) | Real DB container per assignment; CI-automatable |
| Containerization | Docker + Docker Compose | Required by assignment |
| Config | Spring `application.properties` with `${ENV_VAR}` substitution | Native Spring Boot env var binding; no extra dependency |
| Build Tool | Maven or Gradle | Either works; Gradle preferred for faster builds in multi-module setups |

## JWT Configuration

- Algorithm: **RS256** (asymmetric; Identity Service signs with RSA private key, Task Service verifies with public key)
- Identity Service exposes `GET /.well-known/jwks.json` — public key in JWKS format; Task Service loads it at startup
- Private key path: injected via `JWT_PRIVATE_KEY_PATH`; public key path: `JWT_PUBLIC_KEY_PATH`
- Expiry: configurable via `JWT_EXPIRATION_MS` environment variable
- Payload claims: `sub` (user UUID), `iat`, `exp`
- Task Service never holds a private key; no runtime HTTP call to Identity Service after startup

## Constraints

- Java 17 LTS baseline; no preview features
- Each service is a separate Spring Boot application (separate JARs, separate Docker images)
- PostgreSQL is the only supported DB; no H2 in any test scope — use Testcontainers exclusively
- All secrets (DB password, RSA key paths) must come from environment variables — never hardcoded
- HTTP ports must be configurable via environment variable (`SERVER_PORT`)
- Services do not share a codebase or common library module

## Forbidden

- **No Lombok** outside of JPA `@Entity` classes (`@Getter`, `@Setter`, `@NoArgsConstructor` only); not allowed in domain, application, or adapter layers
- **No field injection** (`@Autowired` on fields); constructor injection only throughout
- **No H2** in any configuration; Testcontainers provides the PostgreSQL instance for all tests
- **No SpringFox**; use SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui`) exclusively

## Tradeoffs

| Decision | Tradeoff |
|---|---|
| RS256 over HS256 | Asymmetric keys mean the Task Service never holds the signing secret. JWKS endpoint allows key rotation without redeploying Task Service. Cost: RSA key pair generation at setup. |
| BCrypt over Argon2 | BCrypt is natively supported in Spring Security; Argon2 requires additional dependency (`spring-security-crypto` with `Argon2PasswordEncoder`). Assignment lists both as acceptable. |
| Testcontainers for integration tests | Requires Docker daemon at test time; provides true DB fidelity over mocks. Satisfies assignment's "real containers" requirement. |
| Separate services, no shared library | Avoids coupling; increases duplication of JWT utility code. Acceptable per assignment's "independently deployable" requirement. |
