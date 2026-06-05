---
name: kmp-best-practice
description: KMP shared-module rules for JustWoo — DTOs live in :core, domain UseCases in shared/commonMain, expect/actual only at platform seams, sealed result types, TDD in commonTest. Trigger when editing anything under shared/** or :core.
paths: shared/src/**/*.kt, shared/src/commonMain/sqldelight/**, core/**/*.kt
---

# KMP Best Practice (JustWoo `:shared` + `:core`)

Use when editing anything under `shared/src/` or the `:core` module. This is the cross-platform code consumed by backend, Android, and iOS — getting it wrong breaks three sides at once.

## Where things live

```
:core
  └─ DTOs, domain enums, kotlinx.serialization @Serializable types.
     Compiled for JVM (backend) + Android + iOS. THIS IS THE API CONTRACT.

shared/src/commonMain/
  ├─ domain/usecase/          UseCase classes — pure business logic, suspend funs.
  ├─ data/datasource/         DataSource interfaces (local persistence) + ApiService interface.
  ├─ data/network/            Ktor client setup, request/response mapping to sealed results.
  ├─ data/db/                 SQLDelight queries wrapped in idiomatic Kotlin APIs.
  ├─ ui/nav/                  Decompose Component interfaces + default impls (see decompose-nav).
  ├─ design/                  Design tokens (color/spacing/typography) shared by Compose + SwiftUI.
  ├─ config/                  Base URLs, feature flags.
  └─ di/                      Koin modules common to both platforms.

shared/src/androidMain/  ←  actuals for expect declarations + Android-only engines.
shared/src/iosMain/      ←  actuals for expect declarations + Darwin-only engines.
shared/src/commonTest/   ←  TESTS GO HERE FIRST.
```

## TDD in `commonTest`

Domain code in `commonMain` is the easiest layer in the whole project to TDD — no Android/iOS toolchain needed.

1. New UseCase / Repository method → `shared/src/commonTest/kotlin/.../<XxxUseCaseTest>.kt`.
2. Stub `DataSource` and `ApiService` with fakes (plain classes implementing the interface).
3. Drive `suspend` functions with `runTest { }` from `kotlinx-coroutines-test`.
4. Assert on the sealed result.
5. Implement.

Platform-specific actuals (`androidMain` / `iosMain`) need their own platform tests (`androidUnitTest`, `iosTest`).

Skip TDD only for: trivial DTO additions in `:core` (the compiler is the test), or design-token tweaks.

## `:core` is sacred

DTOs in `:core` are the contract. Rules:

- Every DTO is `@Serializable`, has a stable explicit field name (`@SerialName` when the Kotlin name and wire name differ), and uses **only** primitive / `kotlinx.serialization`-supported types.
- `currencyCode: String` (ISO 4217). **Never** an enum, **never** a typed wrapper. Backend, Android, and iOS all agree on the wire format because it's a 3-letter string.
- Renaming a field is a **breaking change** on all three platforms simultaneously. If you must, do it as: add new field → migrate readers → remove old field. Don't rename in place.
- No platform-specific imports in `:core` — it must compile for JVM, Android, AND iOS targets.

Any change to a DTO that crosses the wire **must** be mirrored in [`backend/src/main/resources/openapi/documentation.yaml`](../../../backend/src/main/resources/openapi/documentation.yaml). See [`backend-best-practice`](../backend-best-practice/SKILL.md).

## Repository + DataSource pattern

A Repository in `shared/commonMain` coordinates remote + local:

```kotlin
class TaskRepository(
    private val api: ApiService,
    private val local: TaskDataSource,   // interface, platform implements
) {
    suspend fun loadTasks(houseId: Long): ApiResult<List<Task>> { ... }
}
```

- `DataSource` is a `commonMain` interface. `androidMain` and `iosMain` each provide an implementation using SQLDelight (or platform-specific storage like Keychain for tokens).
- Repositories return **sealed result types** (`ApiResult<T>`, `AuthDataResult`, `HouseDataResult`). Never `throw` across a Repository boundary.
- New failure mode → new sealed subtype, not a magic string.

## `expect` / `actual` — only at platform seams

Use `expect`/`actual` for: SQLDelight driver, HTTP client engine, secure storage (Keychain / EncryptedSharedPreferences), platform date formatting, file paths. **Not** for business rules — those are in `commonMain` and platform-pure.

If you find yourself writing matching Android + iOS Kotlin files for the same domain function, you've put logic in the wrong place. Move it to `commonMain`.

## SQLDelight

- Schema lives in `shared/src/commonMain/sqldelight/.../db/`.
- Queries are written in `.sq` files; generated APIs are consumed via `DataSource` implementations.
- Schema migrations: add a new `.sq` file with `BEFORE VERSION N` migration block — never mutate an existing migration after release.
- Local IDs vs server IDs: store the server-side `Long` PK as the SQLDelight `INTEGER PRIMARY KEY` to keep round-trips simple.

## Coroutines

- Suspend funs everywhere. Repositories return `T` or `ApiResult<T>` (suspending), never `Flow<T>` unless the data really is a stream.
- Use `Flow<T>` for: SQLDelight reactive queries, WebSocket events, UI state in Components.
- `Dispatchers.IO` on JVM/Android; on iOS map to `Dispatchers.Default` or platform-appropriate dispatcher. Never `runBlocking`.

## Done = all platforms build

```
./gradlew :core:compileKotlin
./gradlew :shared:compileDebugKotlinAndroid
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
./gradlew :shared:allTests
```

If any of these fail, the change is not done.

## Anti-patterns — reject on sight

- An enum for `currencyCode` (must be `String` ISO 4217).
- DTO in `:core` referencing `java.time.Instant` or any JVM-only type — use `kotlinx.datetime.Instant`.
- `expect fun fetchTasks(): List<Task>` — that's domain logic, must be in `commonMain` only.
- A Repository throwing instead of returning a sealed result.
- `runBlocking` outside tests.
- Two parallel `androidMain` / `iosMain` implementations of the same business rule — refactor up to `commonMain`.
- Editing an existing migration `.sq` block instead of adding a new versioned one.
- Backend-only field added to a `:core` DTO without updating the Swagger YAML.
