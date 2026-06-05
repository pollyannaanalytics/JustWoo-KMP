---
name: compose-performance
description: Optimize Jetpack Compose performance. Use this skill when diagnosing laggy UIs, excessive recompositions, or complex list rendering.
---

# Compose Performance Skill

This skill provides guidelines and best practices for ensuring smooth and efficient Jetpack Compose UIs.

## Core Performance Principles

### 1. Minimize Recomposition
Recomposition is the process of calling your composable functions again when inputs change. Unnecessary recompositions lead to dropped frames.

- **Use `remember`**: Cache expensive calculations so they don't run every time the composable is called.
- **Use `derivedStateOf`**: Use this when a state is derived from other state objects that change more frequently than the result needs to (e.g., scroll position).
- **Use `key` in Loops**: Always provide a unique `key` in `LazyColumn` or `items` to help Compose identify which items have actually changed.

### 2. Defer State Reads
Read state as late as possible in the composition tree. If you only need a value in a child composable, pass a lambda or the state object itself rather than the raw value to avoid recomposing the parent.

### 3. Stability and Strong Skipping
Compose can skip recomposition if all parameters are "stable".
- **Prefer Data Classes**: Use data classes for UI state.
- **Avoid Unstable Collections**: Standard `List`, `Set`, and `Map` are considered unstable by the Compose compiler. Consider using `ImmutableList` from `kotlinx.collections.immutable` or wrapping them in a stable class.

### 4. Layout Optimization
- **Avoid Deep Hierarchies**: Use `ConstraintLayout` or custom layouts if the nesting gets too deep.
- **Use `Modifier.graphicsLayer`**: For animations like rotation, scale, or alpha, use `graphicsLayer` to perform the transformation on the GPU without triggering a full relayout/recompose.

## Troubleshooting Workflow

1.  **Enable Layout Inspector**: Use the Android Studio Layout Inspector to view recomposition counts.
2.  **Identify "Hot" Composables**: Look for composables that recompose more than expected.
3.  **Check Parameter Stability**: Check if the inputs to the hot composable are changing or if they are considered "unstable".
4.  **Apply `derivedStateOf`**: Check if you are reading a frequently changing state (like scroll) directly.

## Example: Optimized List
```kotlin
@Composable
fun TaskList(tasks: List<Task>) {
    LazyColumn {
        items(
            items = tasks,
            key = { it.id } // CRITICAL: Helps Compose avoid recomposing all items when one is added/removed
        ) { task ->
            TaskItem(task)
        }
    }
}
```
