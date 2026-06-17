# JustWoo — Household Task Distribution App

<div align="center">
      <img src="Screen_recording_20260611_145434.gif" width="300" alt="App Demo">
</div>

Think of it as **Jira for your household** — assign chores to roommates, track who owes what, and settle up when it's payday.

A **full-stack Kotlin Multiplatform** application for household task management — covering the full engineering stack from backend to native mobile clients.

| 🖥️ Backend | 📱 Native Clients |
|:---|:---|
| 🔐 JWT auth + Bcrypt password hashing | 🤖 Android — Jetpack Compose + SQLDelight |
| 🗄️ PostgreSQL via Exposed ORM + HikariCP | 🍎 iOS — SwiftUI + SQLDelight |
| ⚡ Redis for session tokens & rate limiting | 🔗 Shared KMP business logic across both |
| ☁️ Deployed on AWS Lightsail + Nginx SSL | 📦 Compile-time API contracts via `:core` module |
| 🔄 GitHub Actions CI/CD — test → merge → deploy | |

> **Status:** Actively under development. Backend and shared module are functional; mobile clients are in progress.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [AI-Assisted Development](#ai-assisted-development)
- [Technical Decisions](#technical-decisions)
- [Backend API](#backend-api)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [CI/CD](#cicd)
- [Production Infrastructure](#production-infrastructure)
- [Project Status](#project-status)

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
| **Local DB (KMP)** | SQLDelight | Type-safe SQL with shared schema across Android and iOS |
| **iOS UI** | SwiftUI | Native Apple UI framework |
| **Dependency Injection** | Koin 4.1 | Multiplatform DI across server and client |
| **Concurrency** | Kotlin Coroutines 1.10 | Structured concurrency across all layers |
| **Containerization** | Jib 3.4 | Dockerized backend deployment |
| **CI/CD** | GitHub Actions | Automated test → merge → deploy pipeline |
| **Cloud Infra** | AWS Lightsail + Nginx | Ubuntu server with SSL reverse proxy |
| **Testing** | JUnit + TestContainers + MockK | Integration tests with real PostgreSQL containers |

---

## Architecture Overview

```mermaid
graph TD
    subgraph Clients[Client Layer]
        subgraph Android
            AUI[Jetpack Compose UI]
            ADB[(SQLDelight)]
        end
        subgraph iOS
            IUI[SwiftUI]
            IDB[(SQLDelight)]
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

## AI-Assisted Development

Claude Code is treated as a structured collaborator. A spec-driven workflow (`/opsx`) plans the change, then dispatches each **vertical slice** — backend → shared → Android → verify — to a platform-scoped subagent that loads only the rules for its layer. Cross-stack non-negotiables (TDD, Swagger contract, sealed result types, ISO 4217 currency) live in [`CLAUDE.md`](CLAUDE.md).

```mermaid
flowchart LR
    Propose["/opsx:propose<br/>proposal · design · tasks"]
    Ship["🚢 git-pr-flow<br/>PR → develop → merge"]
    Archive["/opsx:archive"]

    subgraph Slice["/opsx:apply — one feature end-to-end"]
        direction LR
        BE["🖥 backend"]
        KMP["🔗 shared / kmp"]
        AOS["🤖 android"]
        IOS["🍎 ios"]
        Verify["✅ verify"]
        BE --> KMP --> AOS --> Verify
        AOS -. opt-in .-> IOS -. opt-in .-> Verify
    end

    Propose --> Slice --> Ship --> Archive
```

A feature lands end-to-end before the next one starts — no horizontal “all backend, then all Android” phases. iOS is opt-in. Shipping runs through `git-pr-flow`, which gates the PR on green build + clean rebase against `develop`/`main` and verifies every merge action.

### Platform subagents

Each slice step runs in its own subagent so only the relevant rules load into context.

| Subagent | Scope | Enforces |
|:---|:---|:---|
| `backend-engineer` | `backend/**`, `openapi/**` | Ktor layering · TDD · Swagger as contract |
| `kmp-engineer` | `shared/**`, `core/**` | Cross-stack DTOs · UseCases · sealed results |
| `android-engineer` | `androidApp/**` | Decompose · Compose · Koin · coroutines |
| `ios-engineer` | `iosApp/**`, `shared/iosMain/**` | SwiftUI ↔ Decompose binding |
| `build-verifier` | gradle targets inferred from diff | Compile + test pass/fail report |

### Path-triggered skills

Auto-load when matching files are edited — no manual invocation.

| Worker skill | Trigger | Focus |
|:---|:---|:---|
| `compose-authoring` | `androidApp/**/ui/**/*.kt` | One `@Composable` per file, mandatory `@Preview`, state hoisting |
| `decompose-nav` | `**/ui/nav/**`, `*Component.kt` | Component+Content pattern, `@Serializable` configs |

| Feature skill | Domain invariants |
|:---|:---|
| `feature-auth` | Bcrypt only · JWT 1h + Redis refresh 7d · rate-limited login |
| `feature-house` | ADMIN/MEMBER roles · last-admin protection · single-use invite TTL |
| `feature-task` | House-scoped · status state machine · optional price + ISO 4217 currency |
| `feature-settlement` | Immutable ledger · `payerId ≠ payeeId` · multi-currency balance |

### Intent-triggered skills

Load when the user's request matches the workflow, not the file path.

| Skill | Trigger phrases | Enforces |
|:---|:---|:---|
| `git-pr-flow` | "open a PR", "送 PR", "merge the PR", "幫我 merge" | Pre-PR: build + tests green, no conflict against `develop`/`main`. PR target is always `develop`. Post-merge: re-read `gh pr view` state and fix anything left red instead of trusting the exit code. |

---

## Technical Decisions

### Contract-Driven Full-Stack Type Safety

The `:core` module is compiled for JVM, Android, and iOS. DTOs defined here are serialized with `kotlinx.serialization` on both server and client. This eliminates manual JSON mapping and guarantees that API contract changes are caught at compile time — not in production.

### DataSource Abstraction for Native Persistence

Local storage interfaces are defined in `shared/commonMain` and implemented per platform with native solutions:

- **Android**: SQLDelight — type-safe SQL with generated Kotlin APIs
- **iOS**: SQLDelight — same shared schema, with generated Swift-compatible APIs

This approach provides the benefits of shared business logic and a single source of truth for the database schema across both platforms.

### Repository Pattern with Sealed Result Types

Repositories in the shared module coordinate between `ApiService` (remote) and `DataSource` (local), exposing results through sealed classes (`ApiResult<T>`, `AuthDataResult`, `HouseDataResult`). This gives callers exhaustive `when` handling for loading, success, and typed failure states — no unchecked exceptions leaking across layers.

### Backend Security Design

- **JWT authentication** with 1-hour access tokens and Redis-backed refresh tokens (7-day TTL)
- **Bcrypt password hashing** — passwords never stored in plaintext
- **Login attempt rate limiting** via Redis with 24-hour sliding window (max 10 attempts)
- **Layered authorization** — routes delegate auth checks to services, not embedded in HTTP handlers

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
| `/schema` | GET | Interactive Mermaid ER diagram of the database |

---

## Database Schema

Interactive ER diagram: [`https://justwoo-tw.uk/schema`](https://justwoo-tw.uk/schema)

```mermaid
erDiagram
    Users {
        Long id PK
        String email
        String password
        Instant createTime
        Instant updateTime
    }
    Profiles {
        Long id PK
        Long userId FK
        String name
        String avatar
        String bankAccount
        Instant createTime
        Instant updateTime
    }
    Houses {
        Long id PK
        String name
        String description
        String avatar
        Instant createTime
        Instant updateTime
    }
    HouseMembers {
        Long id PK
        Long houseId FK
        Long memberId FK
        Int role
        Instant joinAt
    }
    Tasks {
        Long id PK
        String title
        Long ownerId FK
        Long houseId FK
        Long executorId FK
        String description
        Int accessLevel
        Int taskStatus
        Double price
        String currencyCode
        Instant dueTime
        Instant createTime
        Instant updateTime
    }
    TasksAssignees {
        Long id PK
        Long taskId FK
        Long userId FK
        Int status
    }
    Settlements {
        Long id PK
        Long houseId FK
        Long payerId FK
        Long payeeId FK
        Double amount
        String currencyCode
        String note
        Instant createTime
    }
    Chats {
        Long id PK
        String content
    }
    ChatsTasks {
        Long id PK
        Long taskId FK
        Long chatId FK
    }

    Users ||--o| Profiles : "has profile"
    Users ||--o{ HouseMembers : "joins"
    Houses ||--o{ HouseMembers : "has members"
    Users ||--o{ Tasks : "owns"
    Houses ||--o{ Tasks : "contains"
    Users ||--o{ Tasks : "executes"
    Tasks ||--o{ TasksAssignees : "assigned to"
    Users ||--o{ TasksAssignees : "is assignee"
    Houses ||--o{ Settlements : "tracks"
    Users ||--o{ Settlements : "pays"
    Users ||--o{ Settlements : "receives"
    Tasks ||--o{ ChatsTasks : "linked to chat"
    Chats ||--o{ ChatsTasks : "linked to task"
```

Key design choices:
- **Junction tables** (`HouseMembers`, `TasksAssignees`, `ChatsTasks`) for many-to-many relationships
- **Role-based membership** — `HouseMembers` includes a `MemberRole` (ADMIN / MEMBER)
- **Task lifecycle** — status tracking with `TaskStatus` and `AccessLevel` enums
- **Optional pricing** — tasks support an optional `price` with ISO 4217 `currencyCode`
- **Settlement ledger** — `Settlements` tracks payments between members for cost reconciliation

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
- [x] Android: Decompose navigation setup
- [ ] Android: Compose UI (in progress)
- [ ] iOS: SwiftUI + SQLDelight DataSource implementation

---

**Pin-Yun (Pollyanna) Wu** — [LinkedIn](https://www.linkedin.com/in/pin-yun-wu/)
