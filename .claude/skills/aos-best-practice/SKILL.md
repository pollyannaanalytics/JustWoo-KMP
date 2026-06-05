---
name: aos-best-practice
description: Android client rules for JustWoo — Decompose Components own state, Compose is purely presentational, TDD on Components/ViewModels via androidUnitTest, design tokens from shared/design. Trigger when editing anything under androidApp/** that is not a pure @Composable file (those go to compose-authoring).
paths: androidApp/**/*.kt
---

# Android Best Practice (JustWoo `androidApp`)

Use when editing **non-Composable** Android code: Components, ViewModels, DI wiring, Activity / Application setup, anything under `androidApp/src/androidMain/` outside `ui/**`.

For pure Composable authoring rules see [`compose-authoring`](../compose-authoring/SKILL.md). For navigation see [`decompose-nav`](../decompose-nav/SKILL.md). This skill is for the **glue around** Compose.

## TDD for state holders

Components and ViewModels are testable plain Kotlin — write tests first.

1. Add `androidApp/src/androidUnitTest/kotlin/.../<feature>/<XxxComponentTest>.kt`.
2. Construct the Component with a fake `ComponentContext` (`DefaultComponentContext(LifecycleRegistry())`) and stub Repository / UseCase dependencies.
3. Drive events (`component.onAddClick()`) and assert on `component.state.value`.
4. Implement / fix the Component to make the test green.

UI-only changes (typography, color tweak) skip TDD — `@Preview` is the verification surface there.

## State ownership

- **Components own state.** A Composable receives `state: SomeState` and `onEvent: (SomeEvent) -> Unit` (or the Component itself). It does not own a `StateFlow` or `mutableStateOf` for domain data.
- **ViewModels are only used when a screen has no Decompose Component** (rare — most navigation-attached screens use Components). When used, they live in `androidApp/.../ui/<feature>/` next to the screen.
- **Side effects** (network calls, DB writes) go through UseCases injected into the Component. Never call a Repository directly from a Composable.

## Dependency injection (Koin)

- Module files live in `androidApp/.../di/` and `shared/.../di/`.
- Bind interfaces, not concrete classes — makes test fakes trivial.
- Components and ViewModels are injected via constructor; never `get<Foo>()` inside a Component body. The factory in `RootComponent` (or wherever the child is created) calls Koin.
- New dependency? Add to a `module { }` block, then register in `KoinApplication` startup in `MainApplication.kt`.

## Threading

- Coroutines only. `viewModelScope` for ViewModels; for Components use `coroutineScope()` from Decompose Essenty (cancels with the Component lifecycle).
- Network / DB on `Dispatchers.IO`. State updates back on `Dispatchers.Main` (or just let `MutableStateFlow` handle it — emissions are thread-safe).
- Never `runBlocking` outside tests.

## Cross-platform discipline

If a piece of logic is platform-agnostic, it belongs in `shared/commonMain`, not `androidApp/`. Common offenders that should NOT be Android-only:

- Domain UseCases.
- Repository interfaces.
- Component interfaces (`*Component.kt`).
- DTO mapping.

Acceptable Android-only:

- `MainApplication`, `MainActivity`, Activity result contracts.
- Platform-specific implementations (`androidMain` of an `expect`/`actual` in shared).
- `@Composable` content and `@Preview`s.

If you touched anything that the iOS client will also need, move it to `shared/commonMain` in the same change — see [`kmp-best-practice`](../kmp-best-practice/SKILL.md).

## Backend interaction reminder

If your change requires a new backend endpoint or a shape change to an existing one, the Swagger YAML at [`backend/src/main/resources/openapi/documentation.yaml`](../../../backend/src/main/resources/openapi/documentation.yaml) **must** be updated as part of the same change. The Android side is allowed to merge only after the backend contract is in place — see [`backend-best-practice`](../backend-best-practice/SKILL.md).

## Done = build + tests + preview

```
./gradlew :androidApp:testDebugUnitTest
./gradlew :androidApp:compileDebugKotlin
./gradlew :androidApp:assembleDebug   # for screen-level changes
```

Plus: open the relevant `@Preview` in Android Studio (or state explicitly you couldn't run it from CLI).

## Anti-patterns — reject on sight

- `Repository` or `ApiService` instantiated directly inside a Composable or Component body — must come via Koin.
- `runBlocking` in production code.
- An Android-only file that re-defines a domain UseCase that should be in `shared/commonMain`.
- `LiveData` (we are coroutines + `StateFlow` only).
- A Component without a unit test — write one before adding more logic.
- Hardcoded base URLs / secrets — use `BuildConfig` or shared `config/` constants.
