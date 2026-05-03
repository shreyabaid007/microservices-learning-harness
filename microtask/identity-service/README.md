# Identity Service

User registration, login, and JWT issuance (RS256). Exposes a JWKS endpoint so other services can verify tokens locally without calling back.

Default port: **8080**.

## Configuration

Environment variables (see [.env.example](.env.example)):

| Var | Description |
|---|---|
| `SERVER_PORT` | HTTP port (default 8080) |
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | DB user |
| `DB_PASSWORD` | DB password |
| `JWT_EXPIRATION_MS` | Access-token lifetime in ms (default 3600000) |

The RSA keypair is generated in-memory at startup. Public key is published at `/.well-known/jwks.json`.

## Run

### With Docker Compose (from repo root)
```bash
docker compose up --build identity-db identity-service
```

### Locally with Maven
```bash
docker run --rm -d --name identity-db -e POSTGRES_DB=identity_db \
  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=secret \
  -p 5432:5432 postgres:16-alpine

export $(grep -v '^#' identity-service/.env.example | xargs)
./mvnw -pl identity-service spring-boot:run
```

## Endpoints

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/register` | — | Create user |
| POST | `/login` | — | Exchange credentials for JWT |
| GET | `/me` | Bearer | Current user metadata |
| GET | `/.well-known/jwks.json` | — | RSA public key (JWKS) |
| GET | `/swagger-ui.html` | — | Live API docs |

### Register
```bash
curl -s -X POST http://localhost:8080/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"s3cret"}'
# => 201 { "id": "...", "email": "alice@example.com", "createdAt": "..." }
```
- `400` on missing/blank field or malformed email
- `409` on duplicate email

### Login
```bash
curl -s -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"s3cret"}'
# => 200 { "token": "eyJhbGciOiJSUzI1NiIs..." }
```
- `401` on unknown email or wrong password

### Get current user
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"s3cret"}' | jq -r .token)

curl -s http://localhost:8080/me -H "Authorization: Bearer $TOKEN"
# => 200 { "id": "...", "email": "alice@example.com", ... }
```
- `401` for missing/expired/wrong-key token

### JWKS
```bash
curl -s http://localhost:8080/.well-known/jwks.json
# => { "keys": [{ "kty":"RSA", "alg":"RS256", "kid":"...", "n":"...", "e":"AQAB" }] }
```

## Build & test

```bash
./mvnw -pl identity-service verify    # unit + IT (Testcontainers PG) + spotless + jacoco
```
