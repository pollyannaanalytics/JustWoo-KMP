# JustWoo — Household Task Distribution App

A **full-stack Kotlin Multiplatform** application for household task management, built with a clean layered architecture spanning backend, shared business logic, and native mobile clients for both Android and iOS.

> **Status:** Actively under development. Backend and shared module are functional; mobile clients are in progress.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                         Client Layer                                │
│  ┌─────────────────────────┐      ┌─────────────────────────────┐   │
│  │       Android           │      │           iOS               │   │
│  │   Jetpack Compose       │      │         SwiftUI             │   │
│  │   Room Database         │      │     Core Data / SQLite      │   │
│  │  (DataSource impl)      │      │     (DataSource impl)       │   │
│  └────────────┬────────────┘      └──────────────┬──────────────┘   │
│               │                                  │                  │
│  ┌────────────┴──────────────────────────────────┴──────────────┐   │
│  │                   :shared (KMP)                              │   │
│  │  Repository ← DataSource (interface) + ApiService (Ktor)    │   │
│  │  UseCase ← Repository                                       │   │
│  │  ApiResult / DataResult sealed classes                       │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │                                       │
│  ┌──────────────────────────┴───────────────────────────────────┐   │
│  │                    :core (KMP)                               │   │
│  │  DTOs (AuthDto, HouseDto, TaskDto, ProfileDto, PageDto)      │   │
│  │  Domain Models (Task, House, Profile, HouseMember)           │   │
│  │  Shared between server and clients — compile-time safety     │   │
│  └──────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────┘
                              │  Ktor HTTP
┌─────────────────────────────┴────────────────────────────────────────┐
│                      :backend (Ktor / JVM)                          │
│  Routes → Service → Repository → Exposed ORM → PostgreSQL          │
│  JWT Authentication │ Redis (sessions & rate limiting) │ Bcrypt     │
└──────────────────────────────────────────────────────────────────────┘
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
| `/tasks` | GET/POST | Task creation and assignment |
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

- JDK 17+
- PostgreSQL
- Redis

### Run the Backend

```bash
./gradlew :backend:run
```

The server starts on port `8000` (configurable in `application.yaml`). Swagger UI is available at [http://localhost:8000/swagger](http://localhost:8000/swagger).

---

## Project Status

- [x] Backend: Auth, House, Task, Profile services with full CRUD
- [x] Core: Shared DTOs and domain models across all targets
- [x] Shared: Repository pattern, API client, DataSource interfaces
- [ ] Android: Compose UI + Room DataSource implementation
- [ ] iOS: SwiftUI + native DataSource implementation

---

**Pin-Yun (Pollyanna) Wu** — [LinkedIn](https://www.linkedin.com/in/pin-yun-wu/)
