---
name: ios-best-practice
description: iOS client rules for JustWoo SwiftUI app — bind to Decompose Components from shared, no NavigationStack-driven state, TDD on view models, currency/money as ISO 4217 string. Trigger when editing anything under iosApp/** or shared/src/iosMain/**.
paths: iosApp/**, shared/src/iosMain/**/*.kt
---

# iOS Best Practice (JustWoo `iosApp` + `shared/iosMain`)

Use when editing iOS-side Swift code or `shared/src/iosMain/kotlin/**` actuals.

**Status note:** iOS is in progress. If files referenced below do not yet exist, you are likely the one creating the pattern — follow these rules as the design intent.

## Navigation goes through Decompose, not SwiftUI

A SwiftUI `View` binds to a Decompose Component from `shared/commonMain` (e.g. `TaskComponent`). It does **not** own the navigation stack.

Wrong:

```swift
@State private var path = NavigationPath()
NavigationStack(path: $path) { ... }
```

Right:

```swift
struct TaskView: View {
    let component: TaskComponent
    @StateValue var state: TaskState  // bound to component.state
    var body: some View {
        ...
        Button("Edit") { component.onEditClick() }
    }
}
```

`@StateValue` is the bridge that subscribes a SwiftUI view to Decompose's `Value<T>`. See [`decompose-nav`](../decompose-nav/SKILL.md) for the full pattern. Parent Components own child stacks/slots; a SwiftUI view's job is to render one Component's state and forward events.

## TDD for iOS-side logic

If you write Swift code that does anything beyond rendering — e.g. an `ObservableObject` wrapper, formatting helpers, a Keychain helper — write the test first:

1. Add an XCTest case under `iosApp/iosAppTests/` (or the equivalent target).
2. Construct the unit, stub dependencies via protocol-based fakes.
3. Assert, then implement.

For pure layout / typography: rely on SwiftUI previews — TDD is not required.

For `shared/iosMain` actuals (Kotlin code): test in `shared/src/iosTest/` using Kotlin test. See [`kmp-best-practice`](../kmp-best-practice/SKILL.md).

## Money and dates

- Money: `Double` amount + ISO 4217 `String` currency code, mirroring the backend / `:core`. Format for display via `NumberFormatter` with `currencyCode` set per row — do **not** assume `Locale.current` matches the user's house currency.
- Dates: backend sends `Instant` (`String` in ISO 8601). Parse with `ISO8601DateFormatter`. Always display in the user's local timezone, never the server's.

## Sharing logic with `shared`

Domain logic (use cases, repositories, DTOs) lives in `shared/commonMain` and is consumed via the generated Kotlin/Native framework. If you find yourself reimplementing a Repository or UseCase in Swift, stop — push it into `shared/commonMain` first.

Acceptable iOS-only code:

- SwiftUI views.
- Platform-specific implementations of `expect` declarations in `shared/iosMain` (e.g. Keychain, DataStore-equivalent, Darwin Ktor engine).
- DI bootstrap (Koin → Swift bridge in `iosApp`).

## Backend interaction reminder

A new iOS feature that requires a backend endpoint change cannot ship without the matching Swagger update in [`backend/src/main/resources/openapi/documentation.yaml`](../../../backend/src/main/resources/openapi/documentation.yaml). See [`backend-best-practice`](../backend-best-practice/SKILL.md).

## Done = it builds and renders

```
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
# then build iosApp from Xcode against the iOS Simulator
```

A SwiftUI change is not done until the view renders in the simulator (or you explicitly tell the user you can't drive Xcode from CLI).

## Anti-patterns — reject on sight

- `NavigationStack(path: $state)` driven by SwiftUI state for in-app navigation.
- A protocol re-declaring a Repository / UseCase that already exists in `shared/commonMain`.
- `Bundle.main.string(forKey: "API_BASE_URL")` or hardcoded URLs — use the shared config module.
- `JSONDecoder` re-decoding a DTO that `:core` already serializes.
- `NSLocale.current.currencyCode` used as the source of truth for a transaction's currency — it's not.
- A `View` constructing its own Component instead of receiving one from the parent.
