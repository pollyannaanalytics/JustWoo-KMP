---
name: build-verifier
description: Verify JustWoo-KMP builds compile and tests pass for whatever the current diff touches. Use after a non-trivial edit, before declaring a task done, or when the user explicitly asks to verify. Runs gradle in an isolated subagent so build logs do not flood the main conversation. Skip for cosmetic-only edits (spacing, copy, comments).
argument-hint: [backend|android|ios|shared|core|all]
context: fork
agent: general-purpose
allowed-tools: Bash(./gradlew *), Bash(git diff *), Bash(git status *), Bash(git diff-index *)
---

# Build Verifier

You are running in an isolated subagent context. Your job is to verify that the relevant gradle targets compile and tests pass for the changes in the current working tree, then return a concise pass/fail report. Do not modify any files.

## Working directory

All commands run from the repo root: `/Users/pin-yunwu/SideProjects/JustWoo-KMP`. The gradle wrapper is at `./gradlew`.

## Step 1 — Pick the target set

Arguments: `$ARGUMENTS`

- If `$ARGUMENTS` is one of `backend`, `android`, `ios`, `shared`, `core`, `all` → use that target.
- Otherwise (no arg, or unrecognized) → auto-detect from the diff.

### Auto-detect from diff

Get changed files:

```!
git diff --name-only HEAD
git status --short
```

Map paths to targets (a single diff can hit multiple):

| Touched path | Add target(s) |
|:---|:---|
| `backend/src/**` | `backend` |
| `backend/src/main/resources/openapi/**` | `backend` |
| `androidApp/**` | `android` |
| `shared/src/commonMain/**` or `shared/src/commonTest/**` | `shared`, `android`, `ios` |
| `shared/src/androidMain/**` | `shared`, `android` |
| `shared/src/iosMain/**` | `shared`, `ios` |
| `core/**` | `core`, `backend`, `android`, `ios` |

If no Kotlin/resource files changed (only `.md`, images, etc.), report "no buildable changes" and exit without running gradle.

## Step 2 — Run the gradle commands

For each selected target, run **exactly** these commands:

| Target | Commands |
|:---|:---|
| `core` | `./gradlew :core:compileKotlin` |
| `backend` | `./gradlew :backend:test` |
| `android` | `./gradlew :androidApp:compileDebugKotlin :androidApp:testDebugUnitTest` |
| `shared` | `./gradlew :shared:compileDebugKotlinAndroid :shared:allTests` |
| `ios` | `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` |
| `all` | All of the above |

Run them sequentially. If a target fails, **continue** to the next target — the user wants the full picture, not just the first failure.

## Step 3 — Report

Return a single concise report. Format:

```
build-verifier — targets: <list>

✅ core                 (12s)
✅ backend              (47s)
❌ android              (8s)
  :androidApp:compileDebugKotlin FAILED
  e: file://.../TaskCard.kt:42: Unresolved reference: foo
✅ shared               (18s)

Summary: 3 passed, 1 failed
Next: fix TaskCard.kt:42, re-run /build-verifier android
```

Rules for the report:

- One line per target with status icon + duration.
- On failure: include only the **first** error block — the gradle line containing `FAILED` plus the next ~10 lines of compiler / test output. Strip ANSI codes if present.
- Never dump full successful build logs. They are noise.
- If a test failed (not a compile error): include the test class + test name + first stack frame.
- If gradle itself fails to start (daemon issue, JDK mismatch): say so explicitly so the user can diagnose environment vs. code.
- End with `Summary:` line. If anything failed, also suggest the narrowest re-run command (`/build-verifier <target>`).

## Anti-patterns — do not

- Edit any source files. You are read-only here.
- Run additional gradle tasks beyond the table above (no `:build`, no `:check`, no `clean`).
- Re-run a failed target hoping for a different result.
- Output gigabytes of build log — the whole point of running in fork is to keep the main conversation clean.
- Speculate about fixes beyond a one-line next-step hint at the end.

## When the report goes back

The main conversation will see only your final report — no gradle output, no intermediate reasoning. Make the report self-sufficient: which targets ran, pass/fail, the minimal failure context to act on.
