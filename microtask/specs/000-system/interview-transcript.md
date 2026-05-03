# Interview Transcript

Decisions captured prior to spec authoring. Questions focused exclusively on topics not already settled by `.claude/steering/product.md`, `.claude/steering/tech.md`, and `.claude/steering/structure.md`.

---

**Q1 — RSA Key Distribution**

> RSA key pair lifecycle for local dev and Docker Compose: how should the private/public PEM files be generated and made available to the services? The steering files show JWT_PRIVATE_KEY_PATH pointing to /run/secrets/jwt_private.pem, but don't specify who generates the key pair or how it's distributed.

**A1:** Identity Service generates keys at startup — Identity Service generates a fresh RSA key pair in memory on every cold start and exposes the public key via JWKS. Task Service fetches it once at startup. Keys are ephemeral — all JWTs are invalidated on Identity Service restart. Simplest setup.

---

**Q2 — Refresh Tokens**

> Refresh tokens: the product spec only mentions a single JWT returned at login. Should refresh tokens be an explicit non-goal for this assignment, or do you want a refresh endpoint?

**A2:** Explicit non-goal. No refresh endpoint. Clients re-authenticate via POST /login when the access token expires.

---

**Q3 — Token Revocation**

> Token revocation: once a JWT is issued, it's valid until expiry — there's no logout endpoint or blacklist. Should this be an explicit non-goal, or do you want a stateful revocation mechanism (e.g., a token denylist table)?

**A3:** Explicit non-goal. No logout endpoint. No token blacklist. Token validity is determined solely by the `exp` claim. Client-side logout = discard the token.

---

**Q4 — Account Lockout**

> Account lockout: product.md lists 'no rate limiting' as a non-goal, but account lockout (locking an account after N failed login attempts) is a distinct security control. Should it be in scope or an explicit non-goal?

**A4:** Explicit non-goal. No failed-attempt tracking, no lockout. Login always returns 401 on bad credentials regardless of attempt count.

---

**Q5 — Task Service IT JWT Strategy**

> Task Service integration tests need a valid RS256-signed JWT to hit the controller. Since Identity Service generates keys ephemerally at startup, how should TaskControllerIT produce tokens?

**A5:** Test-local key fixture. Integration tests generate their own RSA key pair once per test class (or load a fixed PEM from src/test/resources). The Spring context is configured with that public key directly — no WireMock, no running Identity Service. Fastest and most isolated.

---

**Q6 — OpenAPI File**

> OpenAPI spec: SpringDoc generates the spec at runtime from annotations. Should the generated JSON/YAML also be committed to the repo as a build artifact (e.g., via a Maven plugin snapshot), or is the live Swagger UI endpoint sufficient?

**A6:** Runtime endpoint only. Only the live `/swagger-ui.html` and `/v3/api-docs` endpoints are required. No committed spec file. No springdoc-openapi-maven-plugin needed.

---

**Q7 — Logging Scope**

> Logging and observability: product.md rules out audit logging. What level of application logging should be in scope for this assignment?

**A7:** Minimal — Spring Boot defaults only. No custom logging configuration. No structured JSON logs, no request/response logging, no MDC correlation IDs.
