# JustWoo — Project Context for Claude

This file gives Claude the business context for JustWoo. Architecture and tech stack details that change frequently live in [README.md](README.md); this file is for the **why**, not the **how**.

## Product in one line

**Jira for your household.** JustWoo lets roommates / families / shared houses assign chores, track who paid for what, and settle up cleanly at the end of the month.

## Target audience (TA)

The product is built for **shared-living groups where money and chores blur together** — and where ignoring that friction silently corrodes the relationship. Concretely:

- **Roommates in their 20s–30s** splitting rent-adjacent expenses (utilities, groceries, cleaning supplies) and rotating household work.
- **Couples / partners** who want a transparent ledger instead of mental accounting.
- **Families with adult children** living together where chore ownership is contested.

Shared characteristics that drive design decisions:

- **Mobile-first.** Sessions are short, in-context (kitchen, post-payment, commute).
- **Trust-sensitive.** Correctness > feature breadth. A wrong settlement number is catastrophic.
- **Multi-currency.** Currency is ISO 4217 string, not an enum.
- **Low onboarding patience.** Auth + first house creation < 1 minute.

Bias toward clarity over cleverness, explicit money/status over implicit assumptions.

## Core domains

JustWoo has four product domains. Each has a dedicated `feature-*` skill — read it when working on that area.

| Domain | One-line definition | Skill |
|:---|:---|:---|
| **Auth** | Email + password, JWT access (1h) + Redis refresh (7d), Bcrypt hashing, rate-limited login. | `feature-auth` |
| **House** | A household = a group of users with roles (`ADMIN` / `MEMBER`). Joined via invite codes / join requests. | `feature-house` |
| **Task** | A chore inside a house. Has owner, optional executor, optional price + currency, lifecycle status, assignees. | `feature-task` |
| **Settlement** | A payment between two house members. Repository keeps a per-member balance with currency conversion. | `feature-settlement` |

These four are intentionally orthogonal: a Task may have a price, but it does not become a Settlement until a user explicitly records a payment. Don't auto-couple them in code.

## Engineering shape

Three platform skills plus one cross-cutting skill cover the implementation:

- `backend-best-practice` — Ktor server, Exposed ORM, PostgreSQL, Redis, **Swagger is mandatory** for every public endpoint change.
- `aos-best-practice` — Android client (Jetpack Compose + Decompose nav). Pairs with `compose-authoring` and `decompose-nav`.
- `ios-best-practice` — iOS client (SwiftUI bound to Decompose Components from `shared`).
- `kmp-best-practice` — Shared module (`commonMain`): DTOs in `:core`, Repository + DataSource pattern, sealed result types.

## Non-negotiable rules across all skills

1. **TDD by default.** Write the failing test first, then the implementation. Tests live next to the code:
   - Backend: `backend/src/test/kotlin/...` (JUnit + MockK; integration tests use TestContainers + real Postgres).
   - Shared: `shared/src/commonTest/...` (Kotlin test).
   - Android: `androidApp/src/androidUnitTest/...` (for ViewModels / Components).
   - Skip TDD only when the change is purely cosmetic (e.g. spacing, copy). State explicitly when you skip it.
2. **Swagger is the API contract.** Any change to a backend route signature, request body, response body, or status code **must** update [`backend/src/main/resources/openapi/documentation.yaml`](backend/src/main/resources/openapi/documentation.yaml) in the same change. A PR that touches a `*Routes.kt` without touching the YAML is incomplete.
3. **`:core` is the cross-stack contract.** DTOs there are compiled into backend, Android, and iOS. Renaming a field is a breaking change on three sides at once — be deliberate.
4. **Money is `Double price` + `String currencyCode` (ISO 4217).** Never compare amounts across currencies without explicit conversion. Never store currency as an enum.
5. **Sealed result types over exceptions across module boundaries.** `ApiResult<T>`, `AuthDataResult`, etc. Exceptions are for genuinely exceptional conditions, not for control flow.

## Skill conflicts

If a skill rule conflicts with a user instruction in the current conversation, follow the user but flag the conflict so the skill can be updated.
