---
name: git-pr-flow
description: Git PR workflow rules for JustWoo — verify build + tests + no-conflict BEFORE opening a PR, always target develop, and observe every merge action to catch and fix failures rather than fire-and-forget. Trigger when the user asks to push a feature branch, open/create a PR, or merge a PR.
---

# Git PR Flow

Use whenever you are about to:

- Push a `feature/*` branch upstream
- Open or create a pull request (`gh pr create`, "open a PR", "送 PR")
- Merge a pull request (`gh pr merge`, "merge the PR", "幫我 merge")

Read the project [README — CI/CD](../../../README.md#cicd) for the wider branch strategy. This skill is the operational checklist that surrounds those policies.

## Branch strategy (non-negotiable)

```
feature/*  →  develop  →  main  →  production
```

- **Every PR targets `develop`.** Never `main`. The only `develop → main` PRs are produced by CI after tests pass — you do not open those by hand.
- No direct pushes to `develop` or `main`. Every change goes through a `feature/*` branch and a reviewed PR.
- If the user asks to open a PR without naming a base branch, the base is `develop`. Confirm only if the request explicitly contradicts (e.g. "open against main") — in that case, push back before doing it.

## Pre-PR checklist (run in order, no shortcuts)

All three must pass before calling `gh pr create`. If any step is red, stop and fix — do not open the PR "to see what CI says".

### 1. Build + unit tests pass locally

Pick gradle targets that match what the diff actually touched. The fastest way is `/build-verifier`; otherwise run them directly:

| Diff touches | Run |
|:---|:---|
| `backend/**` | `./gradlew :backend:test` |
| `shared/**`, `core/**` | `./gradlew :shared:allTests` |
| `androidApp/**` | `./gradlew :androidApp:testDebugUnitTest` |
| Anything | `./gradlew :backend:compileKotlin :shared:compileDebugKotlinAndroid` as a smoke check |

A compile-only "build passed" is not enough — tests must run and be green. If a test is skipped, say so in the PR description; do not hide it.

### 2. No conflict with `develop` or `main`

Fetch first, then verify both refs merge cleanly into the feature branch:

```bash
git fetch origin
git merge --no-commit --no-ff origin/develop ; git merge --abort
git merge --no-commit --no-ff origin/main    ; git merge --abort
```

If either test-merge reports a conflict, resolve by rebasing onto `develop` (since that is the PR target):

```bash
git rebase origin/develop
# resolve conflicts, then:
git rebase --continue
git push --force-with-lease   # NEVER plain --force on a shared branch
```

Re-run the test suite after any rebase — a clean merge tree is not the same as a green build.

A clean working tree (`git status` empty) does NOT mean no upstream conflict. Always do the fetch + test-merge.

### 3. Confirm the PR target is develop

```bash
gh pr create --base develop --title "..." --body "..."
```

Never omit `--base develop`. If `gh` defaults to `main` because of repo settings, that is a project-config problem — flag it to the user instead of quietly opening against `main`.

## Merging a PR — observe, do not fire-and-forget

When the user asks you to merge a PR, treat the merge as a transaction that you must verify completed cleanly. The exit code of `gh pr merge` alone is not enough.

### 1. Run the merge

The repo's existing history uses merge commits (see `git log --merges`), so default to:

```bash
gh pr merge <number> --merge --delete-branch
```

If the user explicitly asks for squash or rebase, honor that instead. Always include `--delete-branch` to avoid stale feature branches accumulating.

### 2. Read the output — don't assume success

Watch for any of these in the command output:

- `failed to merge` / `not mergeable` / `Conflict`
- `required status check ... has not been met`
- `branch protection rule` blocking the merge
- `Could not delete branch` (usually permissions; not fatal but report it)
- A non-zero exit code with no obvious message

### 3. Confirm post-merge state

```bash
gh pr view <number> --json state,mergedAt,mergeStateStatus,headRefName
```

Required: `state == "MERGED"` and `mergedAt` is a fresh timestamp. If `state` is still `OPEN`, or `CLOSED` without a `mergedAt`, the merge did not happen — investigate before reporting done.

### 4. If anything is wrong, fix it before reporting done

| Symptom | Fix |
|:---|:---|
| Merge conflict on the GitHub side | Check out the branch, `git rebase origin/develop`, resolve, `git push --force-with-lease`, retry the merge. |
| Failing required check | `gh pr checks <number>` to find which one; open the failing job, fix the underlying cause, push, retry. |
| Branch protection / permission denied | Report exactly which rule blocked the merge. Do NOT silently retry with `--admin` or any bypass. |
| `Could not delete branch` after a successful merge | Note it to the user; offer to delete the branch via `git push origin --delete <branch>` if they want. |

### 5. Bypass flags need explicit authorization

Never use `gh pr merge --admin`, `git push --force` (without `--with-lease`), or any branch-protection bypass without the user explicitly saying so **for this specific PR**. Standing approval from a previous task does not carry over.

## Anti-patterns — reject on sight

- Opening a PR with `--base main`.
- Opening a PR while local tests are red ("CI will tell us").
- Skipping the conflict check because `git status` is clean.
- Reporting "PR merged ✅" based only on `gh pr merge`'s exit code, without re-reading PR state.
- `gh pr merge --admin` or `git push --force` to bypass a failing check without user-granted authorization.
- Squash/merge without `--delete-branch` — leaves dead branches in the remote.
- `git push --force` on a shared branch — always use `--force-with-lease`.
- Merging into `main` by hand (CI owns that step).

## Done bar

- Local build + tests green for the targets the diff touches.
- `git fetch` then test-merge of both `origin/develop` and `origin/main` clean (or rebased + retested).
- PR opened against `develop` with an accurate title/body.
- For merges: `gh pr view --json state` shows `MERGED`, no required check left failing, and the feature branch is deleted (or the reason it wasn't is reported to the user).
