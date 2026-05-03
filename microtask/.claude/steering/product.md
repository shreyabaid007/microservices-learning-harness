# [product.md](http://product.md)

## Purpose

Two independently deployable microservices that together provide secure user authentication and personal task management. This is a take-home assignment demonstrating production-grade backend design.

## Target Users

Authenticated end-users who register, log in, and manage their own tasks via API clients (e.g., mobile apps, frontends, or direct API consumers).

## Core Features

### Identity Service

- Register a new user with email and password (password stored as a secure hash)
- Authenticate a user and return a signed JWT
- Return metadata of the currently authenticated user

### Task Service

- Create a task scoped to the authenticated user
- List all tasks belonging to the authenticated user
- Update an existing task (any field)
- Delete an existing task

## Key User Flows

1. **Registration → Login → Use Tasks**
  - POST `/register` with email + password
  - POST `/login` with credentials → receives JWT
  - GET /me returns current authenticated user
  - User includes JWT in `Authorization: Bearer <token>` header on all Task Service requests
2. **Task lifecycle**
  - POST `/tasks` → creates task owned by the JWT's user ID
  - GET `/tasks` → lists only that user's tasks
  - PUT `/tasks/{id}` → updates task if owned by authenticated user
  - DELETE `/tasks/{id}` → deletes task if owned by authenticated user

## Non-Goals

- No OAuth2 / social login
- No role-based access control (RBAC)
- No task sharing between users
- No pagination, filtering, or sorting of task lists
- No password reset or email verification flows
- No admin interface or admin endpoints
- No frontend or UI
- No inter-service HTTP communication (Task Service validates JWT independently, does not call Identity Service at runtime)
- No rate limiting
- No audit logging

