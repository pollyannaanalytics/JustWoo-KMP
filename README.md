# JustWoo — Household Task Distribution App

A **full-stack Kotlin Multiplatform** application for household task management, built with a clean layered architecture spanning backend, shared business logic, and native mobile clients for both Android and iOS.

> **Status:** Actively under development. Backend and shared module are functional; mobile clients are in progress.

---

## Architecture Overview

```mermaid
graph TD
    subgraph Clients[Client Layer]
        subgraph Android
            AUI[Jetpack Compose UI]
            ADB[(Room Database)]
        end
        subgraph iOS
            IUI[SwiftUI]
            IDB[(Core Data / SQLite)]
        end
    end

    subgraph SharedModule[shared - KMP Business Logic]
        REPO[Repository]
        DS[DataSource Interface]
        NET[ApiService - Ktor Client]
    end

    subgraph CoreModule[core - Shared Contract / compiled for all targets]
        DTO[DTOs + Domain Models / kotlinx.serialization]
    end

    subgraph BackendModule[backend - Ktor Server / JVM]
        ROUTES[Routes + JWT Auth]
        SVC[Service Layer]
        REPOBE[Repository - Exposed ORM]
    end

    subgraph Infra[Infrastructure]
        PG[(PostgreSQL)]
        REDIS[(Redis)]
    end

    AUI --> REPO
    IUI --> REPO
    REPO --> DS
    REPO --> NET
    DS --> ADB
    DS --> IDB
    NET -->|HTTP / JSON| ROUTES
    DTO -.->|compile-time contract| NET
    DTO -.->|compile-time contract| ROUTES
    ROUTES --> SVC
    SVC --> REPOBE
    SVC --> REDIS
    REPOBE --> PG
```

### Module Breakdown

| Module | Role |
|:---|:---|
| **`:core`** | Shared DTOs and domain models, compiled for all targets. Acts as the API contract between server and clients — a field change breaks compilation on both sides, preventing runtime mismatches. |
| **`:shared`** | Platform-agnostic business logic. Defines `DataSource` interfaces for local persistence and `Repository` implementations that coordinate between network and local storage. Each platform provides its own `DataSource` implementation using native database solutions. |
| **`:backend`** | Ktor server with layered architecture: routes handle HTTP, services encapsulate business rules, repositories manage data access via Exposed ORM against PostgreSQL. Redis handles session tokens and login attempt rate limiting. |

---

## Technical Decisions

### Contract-Driven Full-Stack Type Safety

The `:core` module is compiled for JVM, Android, and iOS. DTOs defined here are serialized with `kotlinx.serialization` on both server and client. This eliminates manual JSON mapping and guarantees that API contract changes are caught at compile time — not in production.

### DataSource Abstraction for Native Persistence

Local storage interfaces are defined in `shared/commonMain` and implemented per platform with native solutions:

- **Android**: Room Database — leveraging Jetpack's lifecycle-aware persistence
- **iOS**: Native SQLite / Core Data — idiomatic to the Apple ecosystem

This approach provides the benefits of shared business logic without sacrificing platform-native database capabilities or forcing a lowest-common-denominator solution.

### Repository Pattern with Sealed Result Types

Repositories in the shared module coordinate between `ApiService` (remote) and `DataSource` (local), exposing results through sealed classes (`ApiResult<T>`, `AuthDataResult`, `HouseDataResult`). This gives callers exhaustive `when` handling for loading, success, and typed failure states — no unchecked exceptions leaking across layers.

### Backend Security Design

- **JWT authentication** with 1-hour access tokens and Redis-backed refresh tokens (7-day TTL)
- **Bcrypt password hashing** — passwords never stored in plaintext
- **Login attempt rate limiting** via Redis with 24-hour sliding window (max 10 attempts)
- **Layered authorization** — routes delegate auth checks to services, not embedded in HTTP handlers

---

## Tech Stack

| Layer | Technology | Purpose |
|:---|:---|:---|
| **Language** | Kotlin 2.2 | Single language across backend, shared logic, and Android |
| **Backend Framework** | Ktor 3.3 + Netty | Async HTTP server with coroutine-native request handling |
| **Server ORM** | Exposed 0.61 | Type-safe SQL DSL for PostgreSQL |
| **Server Database** | PostgreSQL + HikariCP | Production persistence with connection pooling |
| **Caching / Sessions** | Redis (Jedis 5.1) | Refresh tokens, login attempt tracking |
| **Authentication** | Auth0 JWT + Bcrypt | Stateless auth with secure password hashing |
| **Shared Logic** | Kotlin Multiplatform | Business logic shared across Android, iOS, and JVM |
| **Serialization** | kotlinx.serialization 1.9 | Compile-time safe JSON for API contracts |
| **Networking (Client)** | Ktor Client | Platform-specific engines (OkHttp / Darwin) |
| **Android UI** | Jetpack Compose | Declarative UI |
| **Android Local DB** | Room | Lifecycle-aware local persistence |
| **iOS UI** | SwiftUI | Native Apple UI framework |
| **iOS Local DB** | Core Data / SQLite | Native Apple persistence |
| **Dependency Injection** | Koin 4.1 | Multiplatform DI across server and client |
| **Concurrency** | Kotlin Coroutines 1.10 | Structured concurrency across all layers |
| **Containerization** | Jib 3.4 | Dockerized backend deployment |
| **Testing** | JUnit + TestContainers + MockK | Integration tests with real PostgreSQL containers |

---

## Backend API

| Endpoint | Method | Description |
|:---|:---|:---|
| `/auth/register` | POST | User registration with Bcrypt-hashed password |
| `/auth/login` | POST | Login with rate limiting, returns JWT + refresh token |
| `/auth/refresh` | POST | Rotate refresh token |
| `/houses` | GET/POST | House CRUD with pagination |
| `/houses/{id}/members` | POST | Member management with role-based access (ADMIN/MEMBER) |
| `/houses/{id}/tasks` | GET/POST | Task creation and assignment (optional price + ISO 4217 currency) |
| `/houses/{id}/settlements` | GET/POST | Record a payment between house members |
| `/houses/{id}/settlements/balance` | GET | Outstanding balance per member (with currency conversion) |
| `/profiles` | GET/PUT | Profile management |

---

## Database Schema

```
Users ──┬── Profiles
        │
        └── HouseMembers ── Houses
                │
                └── Tasks ── TasksAssignees
                      │
                      └── ChatsTasks ── Chats
```

Key design choices:
- **Junction tables** (`HouseMembers`, `TasksAssignees`, `ChatsTasks`) for many-to-many relationships
- **Role-based membership** — `HouseMembers` includes a `MemberRole` (ADMIN / MEMBER)
- **Task lifecycle** — status tracking with `TaskStatus` and `AccessLevel` enums
- **Audit timestamps** — `createdAt` / `updatedAt` on all entities

---

## Getting Started

### Prerequisites

- JDK 21+
- Docker & Docker Compose v2

### Option 1 — Docker Compose (recommended)

1. **Create a `.env` file** in the `backend/` directory:
   ```env
   KTOR_ENV=production
   DB_HOST=db
   DB_PORT=5432
   DB_NAME=justwoo
   DB_USER=your_db_user
   DB_PASSWORD=your_db_password
   JWT_SECRET=your_jwt_secret
   JWT_AUDIENCE=justwoo-users
   REDIS_HOST=redis
   ```

2. **Build the fat JAR and start all services:**
   ```bash
   ./gradlew :backend:buildFatJar
   cd backend
   docker compose up --build -d
   ```

   | Service | URL |
   |:---|:---|
   | Backend API | `http://localhost:8080` |
   | Swagger UI | `http://localhost:8080/swagger` |
   | Portainer (Docker UI) | `http://localhost:9000` |

   PostgreSQL and Redis data are persisted in named Docker volumes.

### Option 2 — Run locally

Ensure PostgreSQL and Redis are running, then:

```bash
export DB_HOST=localhost DB_PORT=5432 DB_NAME=justwoo DB_USER=... DB_PASSWORD=...
export JWT_SECRET=... REDIS_HOST=localhost
./gradlew :backend:run
```

The server starts on port `8000`. Swagger UI is available at `http://localhost:8000/swagger`.

---

## CI/CD

Hosted on **AWS Lightsail** with a GitHub Actions pipeline.

### Branch Strategy

```
feature/xxx  →  develop  →  main  →  production
```

- Every issue gets its own `feature/*` branch
- Merge to `develop` triggers the CI pipeline
- If all tests pass, `develop` is automatically merged into `main` and deployed

### Pipeline

```yaml
push to develop
  └─ Run Unit Tests
       ├─ FAIL → upload test report artifact, stop
       └─ PASS → Merge develop → main
                   └─ Build fat JAR
                   └─ SCP to server
                   └─ docker-compose build + up
```

### Secrets required

| Secret | Description |
|:---|:---|
| `SSH_HOST` | Server public IP |
| `SSH_USER` | SSH login user (e.g. `ubuntu`) |
| `SSH_PRIVATE_KEY` | PEM private key content |
| `SSH_PORT` | SSH port (usually `22`) |

---

## Production Infrastructure

| Component | Solution |
|:---|:---|
| Server | AWS Lightsail (Ubuntu) |
| Reverse proxy | Nginx with Let's Encrypt SSL |
| Container runtime | Docker Compose v2 |
| Database | PostgreSQL 15 (Docker volume) |
| Cache / Sessions | Redis 7 (Docker volume) |
| Container UI | Portainer CE (SSH tunnel access only) |
| Domain | `https://justwoo-tw.uk` |
| API Docs | `https://justwoo-tw.uk/swagger` |

### Viewing logs (Portainer)

```bash
# Open SSH tunnel on your local machine
ssh -L 9000:localhost:9000 -i ~/.ssh/your-key.pem ubuntu@your-server-ip

# Then open in browser
http://localhost:9000
```

---

## Project Status

- [x] Backend: Auth, House, Task, Profile, Settlement services with full CRUD
- [x] Core: Shared DTOs and domain models across all targets (currency as ISO 4217 string)
- [x] Shared: Repository pattern, API client, DataSource interfaces
- [x] CI/CD: GitHub Actions — test → auto-merge → deploy
- [x] Production: AWS Lightsail + Nginx SSL + Docker Compose
- [ ] Android: Compose UI + Room DataSource implementation
- [ ] iOS: SwiftUI + native DataSource implementation

---

**Pin-Yun (Pollyanna) Wu** — [LinkedIn](https://www.linkedin.com/in/pin-yun-wu/)
