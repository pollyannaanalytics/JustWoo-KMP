---
name: feature-auth
description: Auth domain rules for JustWoo — email + Bcrypt password, JWT (1h) + Redis refresh (7d), rate-limited login, TDD across all layers, Swagger contract is mandatory. Trigger when touching anything related to register/login/refresh/logout, AuthRoutes, AuthService, auth repositories, or shared auth UseCases / DataSource.
paths: **/AuthRoutes.kt, **/AuthService.kt, **/repositories/auth/**, **/utils/security/**, **/domain/usecase/auth/**, **/data/datasource/auth/**, **/ui/nav/auth/**
---

# Feature: Auth

Covers user identity, credentials, sessions, and rate limiting. Files involved:

- `backend/.../routes/AuthRoutes.kt`
- `backend/.../service/AuthService.kt`
- `backend/.../repositories/auth/**`
- `backend/.../utils/security/**` (Bcrypt, JWT helpers)
- `shared/.../domain/usecase/auth/**`
- `shared/.../data/datasource/auth/**`
- `:core` Auth DTOs (`RegisterRequest`, `LoginRequest`, `AuthResponse`, etc.)
- `shared/.../ui/nav/auth/**` Components

Also read [`backend-best-practice`](../backend-best-practice/SKILL.md) and [`kmp-best-practice`](../kmp-best-practice/SKILL.md) — this skill only covers auth-specific invariants.

## Invariants — non-negotiable

1. **Passwords are Bcrypt only.** Hash on register via `BcryptHasher`. Verify on login with constant-time comparison. Never `sha256`, never plaintext, never reversible encryption. The Bcrypt cost factor lives in one place — bump it in one PR, do not sprinkle constants.
2. **JWT access tokens are 1 hour. Refresh tokens are 7 days, stored in Redis.** Don't change these TTLs without a ticket — they affect security posture and the mobile UX (when does the user get logged out).
3. **Rate-limit login.** Failed attempts increment a Redis counter in a 24h sliding window, max 10 per email. Exceeded → return `429` with retry-after. This must be tested.
4. **Auth flow never reveals which side is wrong.** Wrong email and wrong password both produce the same generic error (`"Invalid email or password"`). Don't leak whether the email exists.
5. **JWT contains `userId` claim, nothing else sensitive.** Never embed roles, profile data, or anything that becomes stale. Roles are checked per-request by hitting `HouseMembers`.
6. **Refresh rotation:** every refresh request issues a new refresh token AND invalidates the old one in Redis. Detect replay → revoke entire session, force re-login.

## TDD flow for an Auth change

**TDD is mandatory.**

1. **Service test first** (`backend/src/test/.../service/AuthServiceTest.kt`):
   - Cover the happy path, wrong password, unknown email, rate-limit exceeded, expired refresh, replayed refresh.
   - Mock `AuthRepository`, mock `RedisClient` (or use an embedded fake).
2. **Repository test**: integration test against TestContainers Postgres + an embedded / mocked Redis. Verifies Bcrypt round-trip, refresh token storage, rate counter increment.
3. **Route test** (`UserFlowIntegrationTest.kt` pattern): full `testApplication` exercising register → login → refresh → access protected endpoint.
4. Only then implement / modify the production code.

## Swagger contract

Any change to:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout` (when it exists)

…must update [`backend/src/main/resources/openapi/documentation.yaml`](../../../backend/src/main/resources/openapi/documentation.yaml) in the same PR. Especially watch for:

- Status code changes (`401` vs `429` vs `403`).
- Response envelope changes — auth errors share an envelope shape that the clients pattern-match on.
- New required header (e.g. `X-Device-Id` if added) needs to appear in every protected endpoint, not just `/auth/*`.

## Shared / client side

- Token storage: Android uses EncryptedSharedPreferences (or DataStore with crypto), iOS uses Keychain. Both behind a `commonMain` `TokenStore` interface — `expect`/`actual` lives in `shared/{androidMain,iosMain}/.../data/datasource/auth/`.
- The Ktor client has an `Auth` plugin configured to attach the Bearer token and retry once on `401` after refreshing. Don't hand-roll header attachment in individual API calls.
- UseCases: `LoginUseCase`, `RegisterUseCase`, `RefreshTokenUseCase`, `LogoutUseCase` — keep them as single-method classes, return `AuthDataResult`.

## Anti-patterns — reject on sight

- Storing JWT in plain SharedPreferences / UserDefaults.
- Comparing password hashes with `==` (must be Bcrypt's constant-time verify).
- Returning a different error message for "unknown email" vs "wrong password".
- A protected route reading `userId` from a query param or body instead of the JWT principal.
- Embedding roles into the JWT.
- A new auth endpoint without a Swagger entry.
- Skipping the rate-limit test because "it's hard to test" — use a fake clock.
