---
name: backend-best-practice
description: Backend rules for JustWoo Ktor server — TDD-first, Swagger-mandatory, layered routes/service/repository, Exposed ORM transactions, sealed result types. Trigger when editing anything under backend/src/main/kotlin/** or backend/src/test/kotlin/**.
paths: backend/src/**/*.kt, backend/src/main/resources/openapi/**
---

# Backend Best Practice (Ktor + Exposed + Postgres + Redis)

Use when editing anything in `backend/src/main/kotlin/com/pollyannawu/justwoo/backend/` or its tests.

## TDD is the default flow

**Always**:

1. Pick the lowest layer the change affects (usually a `Service` or `Repository`).
2. Write the failing test first in `backend/src/test/kotlin/...` mirroring the production package.
3. Run only that test — confirm it fails for the **right** reason.
4. Implement the smallest change that makes it pass.
5. Move up a layer (e.g. route test) and repeat.

Skip TDD only for: pure refactors with no behavior change (and you have a green test suite to lean on), or trivial DTO-field additions. State explicitly when you skip.

### Test layering

- **Service tests** (`backend/src/test/.../service/*ServiceTest.kt`): unit-style, MockK the repositories. Cover business rules, role checks, currency edge cases, status transitions. See `SettlementServiceTest.kt`, `TaskServiceTest.kt` for the pattern.
- **Repository tests**: integration-style against a real Postgres (TestContainers). Cover SQL semantics, FK constraints, concurrent updates.
- **Route tests** (`backend/src/test/.../routes/*Test.kt`): full Ktor `testApplication` with real DB container. Cover JWT auth, status codes, request validation. See `ApiIntegrationTest.kt`, `UserFlowIntegrationTest.kt`, `HouseInviteFlowTest.kt`.

Never mock the database when testing repository SQL — the whole point is to catch dialect / FK / migration bugs.

## Swagger is part of the change

Every modification to a public endpoint **must** update [`backend/src/main/resources/openapi/documentation.yaml`](../../../backend/src/main/resources/openapi/documentation.yaml) in the same commit. This includes:

- New route → new `paths:` entry with full request/response schemas.
- Changed request/response shape → update the matching `components/schemas/*`.
- Changed status code or error envelope → update the `responses:` block.
- Removed route → delete the entry. Don't leave dead docs.

Checklist before declaring done:

- [ ] Route, service, repository, and OpenAPI YAML all updated.
- [ ] `tags:` is one of `Auth`, `Houses`, `Tasks`, `Profiles` (add a new tag only if you're adding a whole feature area).
- [ ] Schemas reuse existing `$ref`s where possible — don't inline duplicates.
- [ ] Manually load `http://localhost:8080/swagger` after `./gradlew :backend:run` to confirm the YAML parses.

## Layered architecture — keep layers honest

```
Route (HTTP)  →  Service (business rules)  →  Repository (SQL via Exposed)  →  Postgres
                       ↘  Redis (sessions / rate limits)
```

- **Routes** (`backend/.../routes/`) only do: parse request, call JWT principal extraction, call a service, map result to HTTP. No business logic. No direct Exposed calls.
- **Services** (`backend/.../service/`) own business rules: role checks (ADMIN vs MEMBER), state transitions, cross-repository orchestration. Return sealed result types, not `throw`.
- **Repositories** (`backend/.../repositories/`) own all `transaction { ... }` blocks. Routes/services must never open a transaction.
- **Schemas** (`backend/.../schema/`) are Exposed `Table` definitions only — no business logic.

Bypassing a layer (e.g. route → repository directly) is a hard reject.

## Exposed + transactions

- Every DB operation runs inside `transaction { ... }` (or `newSuspendedTransaction { ... }` for coroutine context).
- Transactions live in the **repository** layer, not the service.
- Reads that touch multiple tables: prefer one `transaction { }` over multiple round-trips.
- For multi-step writes that must be atomic (e.g. create task + insert assignees), wrap them in a single transaction in the repository, expose as one method to the service.
- `currencyCode` is a `varchar(3)` storing ISO 4217 — never an enum column.

## Auth & security

- JWT verification is wired via Ktor `authenticate("auth-jwt") { ... }` blocks. Never re-implement token parsing inside a route.
- `call.principal<JWTPrincipal>()?.payload?.getClaim("userId")` is the only allowed way to read the caller's userId.
- Password hashing: `BcryptHasher` with current cost factor — never `MessageDigest`, never plain SHA-anything.
- Login attempts: rate-limit via Redis (sliding 24h window, max 10) — see `repositories/auth/` for the existing pattern.
- Refresh tokens live in Redis with 7-day TTL — never store them in Postgres.

## Result types

Return sealed classes from services and repositories. Look at `utils/dataresult/` for the existing hierarchy (`ApiResult`, `AuthDataResult`, etc.). Adding a new failure mode means adding a new sealed subtype, not a magic string in an error envelope.

Routes convert the sealed result into an HTTP response in one place — typically a `when` over the sealed type at the end of the handler.

## Done = build + tests pass

```
./gradlew :backend:test
./gradlew :backend:run    # then hit /swagger to confirm YAML
```

Compile failure, red tests, or a broken `/swagger` page = not done.

## Anti-patterns — reject on sight

- `transaction { }` inside a route or service.
- A route that does `select`/`insert` directly via Exposed.
- A new endpoint without a matching `paths:` entry in `documentation.yaml`.
- `throw IllegalStateException("...")` to signal a domain error that callers need to handle — use a sealed result.
- `password.encodeToByteArray().sha256()` or any non-Bcrypt password handling.
- An enum class for `currencyCode`.
- Tests that mock the database to test SQL — mock at the repository boundary, not below it.
