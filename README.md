# 🏗️ Microservices Learning Harness

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](https://docker.com)

A hands-on microservices playground built with **Spring Boot** — designed for learning distributed systems patterns by building a real task management platform with identity and task services.

## Architecture

```
┌─────────────────────────────────────────────────┐
│                 Docker Compose                   │
│                                                  │
│  ┌──────────────────┐  ┌──────────────────────┐ │
│  │ Identity Service  │  │    Task Service       │ │
│  │  (Auth / Users)   │──│  (CRUD / Assignment)  │ │
│  │  Spring Security  │  │  Spring Data JPA      │ │
│  └──────────────────┘  └──────────────────────┘ │
│                                                  │
└─────────────────────────────────────────────────┘
```

## What's inside

- **Identity Service** — user registration, authentication, JWT tokens via Spring Security
- **Task Service** — task CRUD, assignment, status management with Spring Data JPA
- **Docker Compose** — one-command local stack with service discovery
- **API Specs** — OpenAPI/Swagger specifications in `specs/`

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Auth | Spring Security + JWT |
| Data | Spring Data JPA |
| Build | Maven (wrapper included) |
| Infra | Docker Compose |

## Quick start

```bash
cd microtask
docker-compose up -d
```

Or run individually:

```bash
cd microtask
./mvnw spring-boot:run -pl identity-service
./mvnw spring-boot:run -pl task-service
```

## License

MIT
