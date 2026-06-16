---
name: "OPSX: Apply"
description: Implement tasks from an OpenSpec change (Experimental)
category: Workflow
tags: [workflow, artifacts, experimental]
---

Implement tasks from an OpenSpec change.

**Input**: Optionally specify a change name (e.g., `/opsx:apply add-auth`). If omitted, check if it can be inferred from conversation context. If vague or ambiguous you MUST prompt for available changes.

**Steps**

1. **Select the change**

   If a name is provided, use it. Otherwise:
   - Infer from conversation context if the user mentioned a change
   - Auto-select if only one active change exists
   - If ambiguous, run `openspec list --json` to get available changes and use the **AskUserQuestion tool** to let the user select

   Always announce: "Using change: <name>" and how to override (e.g., `/opsx:apply <other>`).

2. **Check status to understand the schema**
   ```bash
   openspec status --change "<name>" --json
   ```
   Parse the JSON to understand:
   - `schemaName`: The workflow being used (e.g., "spec-driven")
   - Which artifact contains the tasks (typically "tasks" for spec-driven, check status for others)

3. **Get apply instructions**

   ```bash
   openspec instructions apply --change "<name>" --json
   ```

   This returns:
   - Context file paths (varies by schema)
   - Progress (total, complete, remaining)
   - Task list with status
   - Dynamic instruction based on current state

   **Handle states:**
   - If `state: "blocked"` (missing artifacts): show message, suggest using `/opsx:continue`
   - If `state: "all_done"`: congratulate, suggest archive
   - Otherwise: proceed to implementation

4. **Read context files**

   Read the files listed in `contextFiles` from the apply instructions output.
   The files depend on the schema being used:
   - **spec-driven**: proposal, specs, design, tasks
   - Other schemas: follow the contextFiles from CLI output

5. **Show current progress**

   Display:
   - Schema being used
   - Progress: "N/M tasks complete"
   - Remaining tasks overview
   - Dynamic instruction from CLI

6. **Implement tasks (vertical slice, dispatched to platform subagents)**

   JustWoo development is **vertical slice**: for each feature, finish backend → shared/kmp → android → verify before moving on to the next feature. **Do NOT do all backend tasks first and then all Android tasks** — that horizontal pattern is what this flow exists to prevent.

   **Default slice order:** `backend` → `kmp` → `android` → `verify`. **iOS is opt-in** — only include it when tasks explicitly tag it (or the change description names iOS).

   **Detecting which slice each task belongs to.** Use task tags first, then path heuristics, then ask if ambiguous:

   | Slice | Tag (preferred) | Path hint |
   |:---|:---|:---|
   | `backend` | `[backend]` | `backend/**` |
   | `kmp` | `[kmp]` or `[shared]` | `shared/**`, `core/**` |
   | `android` | `[android]` or `[aos]` | `androidApp/**` |
   | `ios` | `[ios]` | `iosApp/**`, `shared/src/iosMain/**` |
   | `verify` | `[verify]` | manual / cross-stack run |

   **For each feature group in tasks.md, loop through the slice in order.** For every pending task in the current slice step:

   - Show: `Feature X — slice step (Y/4): <slice-name>`, then the task description.
   - **Dispatch to the matching platform subagent** via the Agent tool:
     - `backend` → `subagent_type: "backend-best-practice"`
     - `kmp` → `subagent_type: "kmp-best-practice"`
     - `android` → `subagent_type: "aos-best-practice"`
     - `ios` → `subagent_type: "ios-best-practice"` (opt-in)
     - `verify` → handle inline (run gradle tests, hit the app, etc.) — no subagent.
   - The prompt to the subagent must be self-contained: include the task text, the relevant artifact excerpts (proposal / design / spec sections that scope this task), and a reminder to load its SKILL.md first.
   - When the subagent returns, read its hand-off note and pass anything load-bearing to the next slice step (e.g. backend DTOs that kmp will consume; kmp UseCase names that android will inject).
   - Mark the task complete in tasks.md: `- [ ]` → `- [x]`.

   **After finishing all tasks in a feature group**, do a `verify` step before starting the next feature:
   - Run the relevant gradle tests for what changed (`:backend:test`, `:shared:allTests`, `:androidApp:testDebugUnitTest`).
   - If the change is UI-facing, run the app or open the `@Preview`.
   - Only move to the next feature when the slice is green.

   **Pause if:**
   - A task crosses slice boundaries (e.g. asks one subagent to edit another's scope) → split the task or re-dispatch.
   - Task is unclear → ask for clarification.
   - Implementation reveals a design issue → suggest updating artifacts.
   - Error or blocker encountered → report and wait for guidance.
   - User interrupts.

   **If tasks.md is structured horizontally** (all backend tasks grouped, then all android tasks), do not silently flatten it. Tell the user the task ordering will be re-interleaved per feature and confirm before proceeding — or offer to regenerate tasks.md with vertical grouping via `/opsx:propose`.

7. **On completion or pause, show status**

   Display:
   - Tasks completed this session
   - Overall progress: "N/M tasks complete"
   - If all done: suggest archive
   - If paused: explain why and wait for guidance

**Output During Implementation**

```
## Implementing: <change-name> (schema: <schema-name>)

Working on task 3/7: <task description>
[...implementation happening...]
✓ Task complete

Working on task 4/7: <task description>
[...implementation happening...]
✓ Task complete
```

**Output On Completion**

```
## Implementation Complete

**Change:** <change-name>
**Schema:** <schema-name>
**Progress:** 7/7 tasks complete ✓

### Completed This Session
- [x] Task 1
- [x] Task 2
...

All tasks complete! You can archive this change with `/opsx:archive`.
```

**Output On Pause (Issue Encountered)**

```
## Implementation Paused

**Change:** <change-name>
**Schema:** <schema-name>
**Progress:** 4/7 tasks complete

### Issue Encountered
<description of the issue>

**Options:**
1. <option 1>
2. <option 2>
3. Other approach

What would you like to do?
```

**Guardrails**
- Keep going through tasks until done or blocked
- Always read context files before starting (from the apply instructions output)
- If task is ambiguous, pause and ask before implementing
- If implementation reveals issues, pause and suggest artifact updates
- Keep code changes minimal and scoped to each task
- Update task checkbox immediately after completing each task
- Pause on errors, blockers, or unclear requirements - don't guess
- Use contextFiles from CLI output, don't assume specific file names
- **Vertical, not horizontal.** Finish one feature's full slice (backend → kmp → android → verify) before starting the next feature. Dispatch each slice step to its platform subagent.
- **Do not edit code in the main agent for platform-scoped tasks.** If a task is `[backend]`, `[kmp]`, `[android]`, or `[ios]`, route it through the matching subagent so the SKILL rules and scope guardrails apply. The main agent only orchestrates and runs verify steps.

**Fluid Workflow Integration**

This skill supports the "actions on a change" model:

- **Can be invoked anytime**: Before all artifacts are done (if tasks exist), after partial implementation, interleaved with other actions
- **Allows artifact updates**: If implementation reveals design issues, suggest updating artifacts - not phase-locked, work fluidly
