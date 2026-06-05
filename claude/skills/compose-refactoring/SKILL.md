---
name: compose-refactoring
description: Refactor Jetpack Compose screens to separate logic from UI content. Use this skill when a screen composable is bloated with ViewModel calls, state management, and side effects.
---

# Compose Refactoring Skill

This skill provides a standardized workflow for refactoring Jetpack Compose screens into a "Screen/Content" pattern to improve testability, previewability, and separation of concerns.

## Best Practice Pattern

### 1. The Screen Layer (Logic & Orchestration)
The `Screen` composable should be the entry point that interacts with the ViewModel and navigation.

- **Responsibilities**:
    - Collect state from ViewModel (using `collectAsState`).
    - Handle navigation callbacks.
    - Orchestrate side effects using `LaunchedEffect`.
    - Provide callbacks that wrap ViewModel method calls.
    - Call the `Content` composable.

### 2. The Content Layer (Stateless UI)
The `Content` composable should be private and stateless.

- **Responsibilities**:
    - Receive data (state) as plain objects/primitives.
    - Receive user interactions as lambda callbacks.
    - Manage its own UI-only state (e.g., `isExpanded`, `showDialog`).
    - Layout the UI components.

## Refactoring Workflow

1.  **Identify State and Callbacks**: Analyze the current screen to see what state is being read from the ViewModel and what methods are being called.
2.  **Define Content Parameters**: Create a new `Content` composable that takes the identified state and callbacks.
3.  **Move UI Code**: Move the `Scaffold` and layout code into the `Content` composable.
4.  **Wire Screen to Content**: Update the `Screen` composable to collect state and pass it down along with lambda wrappers for ViewModel methods.
5.  **Encapsulate UI State**: If the screen had `remember { mutableStateOf(...) }` for UI-specific things like dialogs, move them into the `Content` layer.

## Example

### Before
```kotlin
@Composable
fun TaskScreen(viewModel: TaskViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    
    Scaffold {
        Column {
            TextField(value = state.title, onValueChange = { viewModel.onTitleChange(it) })
            Button(onClick = { viewModel.submit() }) { Text("Submit") }
        }
    }
}
```

### After
```kotlin
@Composable
fun TaskScreen(viewModel: TaskViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    
    TaskContent(
        state = state,
        onTitleChange = viewModel::onTitleChange,
        onSubmit = viewModel::submit
    )
}

@Composable
private fun TaskContent(
    state: TaskUiState,
    onTitleChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold {
        Column {
            TextField(value = state.title, onValueChange = onTitleChange)
            Button(onClick = onSubmit) { Text("Submit") }
        }
    }
}
```
