---
name: compose-authoring
description: Authoring rules for Jetpack Compose UI in JustWoo-KMP — one Composable per file, mandatory @Preview, performance + refactoring discipline, must build before done. Trigger when writing or editing any @Composable function in androidApp/.
paths: androidApp/src/androidMain/kotlin/**/ui/**/*.kt
---

# Compose Authoring

Use when creating or editing any `@Composable` in `androidApp/src/androidMain/kotlin/com/pollyannawu/justwoo/android/ui/**`.

Navigation is **out of scope** here — handle that via the `decompose-nav` skill. This skill is about the Composable layer only.

## Rules

### 1. One Composable per file
- File name = public Composable name (e.g. `TaskCard.kt` contains `fun TaskCard(...)`).
- Private helper Composables (`@Composable private fun ...`) used only by the public one may live in the same file. If a helper grows past ~30 lines or gets reused, extract to its own file.
- `@Preview` functions live in the same file as the Composable they preview.
- No `Screen.kt` files holding multiple unrelated Composables. Existing examples to follow: `ui/task/TaskExplorationScreen.kt` (one screen), `ui/profile/ProfileEditScreen.kt`.

### 2. @Preview is mandatory
Every public Composable file must end with at least one `@Preview`. For screens and stateful components, provide multiple:

```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TaskCardPreview() {
    JustWooTheme { TaskCard(state = TaskCardState.sample()) }
}
```

For non-trivial state, add Empty / Loading / Error / Filled previews. Always wrap in `JustWooTheme { ... }` so design tokens resolve. Sample data goes in a `companion object` on the state class or a `*Preview.kt` sibling — never inline magic strings in the preview body.

### 3. Performance + refactoring (same discipline)
These are not separate concerns. Apply on every edit:

- **State hoisting**: stateless Composables take `state` + `onEvent` params; state owners are ViewModels or Decompose Components. No `remember { mutableStateOf(...) }` in a presentational Composable unless the state is purely visual (animation, focus).
- **Stable params**: prefer `data class` state + immutable collections (`ImmutableList` from kotlinx.collections.immutable, or `List` of stable items). Annotate with `@Immutable` / `@Stable` when the compiler can't infer it.
- **Lambda stability**: pass method references (`viewModel::onClick`) over inline lambdas where possible; if inline, ensure captured vars are stable.
- **`derivedStateOf`** for computed state read inside Composable; **`remember(key)`** for expensive computations; **never** allocate lists/objects directly in the Composable body.
- **`Modifier` parameter**: every public Composable accepts `modifier: Modifier = Modifier` as the first optional parameter, applied to the outermost layout. No internal `Modifier.fillMaxSize()` unless documented.
- **Recomposition scope**: split read-heavy and write-heavy state into separate Composables so writes don't recompose the reader. If you find yourself adding `key()` to fix a perf bug, the parent is doing too much — split it.
- **No business logic in Composables**: side effects go through `LaunchedEffect`, `DisposableEffect`, or are owned by the Component/ViewModel. UI calls events; events drive state; state drives UI.

When refactoring an existing Composable, apply the above checklist top-to-bottom and stop at the first real fix — don't rewrite for taste.

### 4. Design tokens, not magic values
- Colors: `MaterialTheme.colorScheme.*` or tokens from `shared/.../design/`.
- Spacing / radius / typography: tokens from `shared/.../design/`. No literal `16.dp` for spacing if a token exists.
- New token needed? Add it to the shared design module, don't hard-code.

### 5. Done = build passes
A Composable change is not complete until:

1. Preview(s) render in Android Studio (or you have explicitly told the user you can't run the preview from CLI).
2. Build passes:
   ```
   ./gradlew :androidApp:compileDebugKotlin
   ```
   For a screen-level change, also run `:androidApp:assembleDebug` before declaring done.
3. If you touched shared code: `./gradlew :shared:compileDebugKotlinAndroid`.

Compile failure or red preview = task not done. Fix before reporting completion.

## Anti-patterns — reject on sight

- `var x by remember { mutableStateOf(...) }` inside a leaf Composable that has an `onEvent` callback available.
- Composable functions over ~80 lines without an extraction.
- Multiple unrelated public Composables in one file.
- Files without any `@Preview`.
- Direct `navController.navigate(...)` or `findNavController()` — that's `decompose-nav` territory.
- `LazyColumn { items(list) { ... } }` where `list` is a `List<T>` of unstable items — wrap state or use `key = { it.id }`.

## Quick checklist before saying "done"

- [ ] One public `@Composable` per file, file named after it
- [ ] At least one `@Preview` (light + dark for screens)
- [ ] `modifier: Modifier = Modifier` first optional param
- [ ] No `mutableStateOf` in stateless Composables
- [ ] Design tokens for colors/spacing/typography
- [ ] No navigation calls — events bubble up to a Component
- [ ] `:androidApp:compileDebugKotlin` passes
