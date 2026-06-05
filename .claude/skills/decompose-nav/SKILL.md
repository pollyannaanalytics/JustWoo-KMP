---
name: decompose-nav
description: Navigation must go through Decompose (Component + Content pattern) in JustWoo-KMP. Trigger when adding/removing a screen, changing nav flow, handling back/deeplink, or whenever code touches navigation between screens on Android or iOS (SwiftUI).
paths: **/ui/nav/**/*.kt, **/Component.kt, **/RootComponent.kt, **/*Content.kt, iosApp/**/View.swift
---

# Decompose Navigation

Single source of truth for navigation in this KMP project. Same Components are consumed by Compose (Android) and SwiftUI (iOS).

**Never use:** `NavController` / Navigation-Compose, Voyager, `findNavController()`, raw activity intents for in-app screens, SwiftUI `NavigationStack` driven by SwiftUI state.

## Target location

Components belong in `shared/src/commonMain/kotlin/com/pollyannawu/justwoo/ui/nav/` so iOS can consume them. The current code has them in `androidApp/ui/nav/` — when touching nav, prefer moving the Component to `shared/commonMain` if iOS will need it. Only the `*Content` Composable / `*View` SwiftUI stays platform-side.

Layout per screen:

```
shared/commonMain/.../ui/nav/tasks/
  TaskComponent.kt          // interface + DefaultTaskComponent
  TaskComponent.Config.kt   // if config sealed type is large

androidApp/.../ui/nav/tasks/
  TaskContent.kt            // @Composable TaskContent(component: TaskComponent)

iosApp/.../ui/nav/Tasks/
  TaskView.swift            // SwiftUI view bound to TaskComponent
```

## Adding a screen — checklist

1. **Define the Component interface in `shared/commonMain`**:
   ```kotlin
   interface TaskDetailsComponent {
       val state: Value<TaskDetailsState>
       fun onBack()
       fun onEditClick()
   }
   ```
2. **Implement `DefaultTaskDetailsComponent`** taking `ComponentContext` and any callbacks (`onFinished: () -> Unit`). Use `instanceKeeper`, `lifecycle`, `stateKeeper`, `backHandler` from `ComponentContext` — not Android lifecycle directly.
3. **Wire into the parent's `Config` sealed interface** (`@Serializable`) and the `childStack` / `childSlot` factory in `RootComponent` (or the relevant parent). Look at `androidApp/.../ui/nav/RootComponent.kt` for the existing pattern (`StackNavigation<Config>`, `SlotNavigation<SlotConfig>`).
4. **Pick the right router**:
   - `childStack` — push/pop history (most screens).
   - `childSlot` — modal / bottom sheet / dialog (see `taskQuickSlot` in `RootComponent`).
   - `childPages` — pager.
   - `childPanels` — master/detail on tablet.
5. **Write the Compose binding** in `androidApp` as `XxxContent(component: XxxComponent, modifier: Modifier = Modifier)`. Subscribe to `component.state` via `subscribeAsState()`. Follow `compose-authoring` skill rules.
6. **Write the SwiftUI binding** in `iosApp` consuming the same Component (via `StateFlow`/`Value` bridge).
7. **Verify build**:
   ```
   ./gradlew :shared:compileDebugKotlinAndroid :androidApp:compileDebugKotlin
   ```

## Rules

- **Composables never know about navigation.** A screen Composable receives a `Component` and calls `component.onXxxClick()`. It does not know what screen comes next. Same for SwiftUI views.
- **All nav config is `@Serializable`** — Decompose persists the stack across process death via this.
- **Parent owns child lifecycle.** Children are created by the parent's `childStack` factory; never `new` a Component manually inside a Composable.
- **Back handling** goes through `BackHandler` from Decompose (`backHandler.register(...)`) or the stack's `pop()` — never `Activity.onBackPressed` or Compose's `BackHandler` directly inside a screen.
- **Deeplinks** mutate the stack via `navigation.replaceAll(...)` / `bringToFront(...)` on the root, not via NavController-style URI routing.
- **Cross-platform-first.** If a Component depends on an Android-only API, push that API behind a `commonMain` interface with `expect`/`actual` or inject it. Keep Components in `commonMain`.

## Anti-patterns — reject on sight

- `rememberNavController()`, `NavHost`, `composable("route/...")` — wrong library.
- `LocalContext.current.startActivity(...)` to open another in-app screen.
- A Composable that takes `onNavigateToFoo: () -> Unit` AND `onNavigateToBar: () -> Unit` AND `onNavigateToBaz: () -> Unit` — collapse into one `component: XxxComponent`.
- Creating `DefaultXxxComponent(...)` inside a Composable body.
- Putting a Component in `androidApp/` when no Android-only API is used — it should be in `commonMain`.
- SwiftUI `@State var path: NavigationPath` driving navigation — bind to a Decompose Component instead.

## Quick checklist before saying "done"

- [ ] Component interface in `shared/commonMain` (unless Android-only by necessity — justify)
- [ ] Config is `@Serializable` and added to parent's sealed `Config`
- [ ] Wired into parent's `childStack` / `childSlot` factory
- [ ] Compose `XxxContent` consumes Component, no nav calls inside
- [ ] SwiftUI `XxxView` consumes the same Component (if iOS in scope)
- [ ] Back handling via Decompose, not platform back APIs
- [ ] `:shared:compileDebugKotlinAndroid` and `:androidApp:compileDebugKotlin` pass
