---
name: feature-task
description: Task domain rules for JustWoo — house-scoped chores with owner/executor/assignees, lifecycle status, optional price + ISO 4217 currency, TDD across all layers, Swagger contract is mandatory. Trigger when touching TaskRoutes, TaskService, TaskRepository, Tasks schema, or shared Task UseCases / Components.
paths: **/TaskRoutes.kt, **/TaskService.kt, **/TaskRepository.kt, **/schema/Tasks.kt, **/domain/usecase/task/**, **/ui/nav/tasks/**, **/ui/task/**
---

# Feature: Task

A Task is a chore inside a House. Files involved:

- `backend/.../routes/TaskRoutes.kt`
- `backend/.../service/TaskService.kt`
- `backend/.../repositories/TaskRepository.kt`
- `backend/.../schema/Tasks.kt` (plus `TasksAssignees`, `ChatsTasks`)
- `:core` Task DTOs, `TaskStatus` enum, `AccessLevel` enum
- `shared/.../domain/usecase/task/**`
- `shared/.../ui/nav/tasks/**`

Also read [`backend-best-practice`](../backend-best-practice/SKILL.md) and [`kmp-best-practice`](../kmp-best-practice/SKILL.md).

## Invariants — non-negotiable

1. **Every task belongs to exactly one house** (`houseId` FK, non-null). Any list endpoint takes `houseId` and verifies the caller is a member — see [`feature-house`](../feature-house/SKILL.md) for the membership check.
2. **Three role slots on a task:** `ownerId` (creator, never null), `executorId` (whoever is currently doing it, nullable), and rows in `TasksAssignees` (the candidate pool / people offered the task). Each has different permissions:
   - Owner: edit metadata, delete, change assignees.
   - Executor: change status (claim → in progress → done).
   - Assignee: claim (move themselves into executor slot).
   - Other house members: read-only.
3. **Status transitions are explicit.** Use the `TaskStatus` enum from `:core`. Transitions are validated in `TaskService` against a small state machine — don't allow "Done → Open" without going through "Reopen". Each transition is tested.
4. **Price is optional. When present, currency is mandatory.** `price: Double?` and `currencyCode: String?` (ISO 4217) move together — either both null or both non-null. Enforced in the request DTO via validation, and in the service layer as a defensive check.
5. **A priced task does NOT auto-create a Settlement.** Settlement is a separate explicit action by the user. See [`feature-settlement`](../feature-settlement/SKILL.md). Coupling them in code is wrong.
6. **`AccessLevel`** (private / house-wide / public) gates who sees the task. Default is house-wide. A private task is visible only to owner + executor + assignees + house ADMINs.

## TDD flow for a Task change

1. **Service test first** (`TaskServiceTest.kt`):
   - Status transition matrix — allowed and forbidden combinations.
   - Owner-only edits, executor-only status changes.
   - Price + currency pair validation (both-or-neither).
   - AccessLevel filtering on list/get.
   - Non-member of the house → 403/404.
2. **Repository test** against TestContainers Postgres — assignee join queries, cascade on house delete, currency string round-trip.
3. **Route test** — create task → assign → claim → complete → list filtering by status.

## Swagger contract

Touching any of these requires updating [`backend/src/main/resources/openapi/documentation.yaml`](../../../backend/src/main/resources/openapi/documentation.yaml):

- `GET/POST /houses/{id}/tasks`
- `GET/PUT/DELETE /houses/{id}/tasks/{taskId}`
- Status-transition endpoints (`POST .../claim`, `.../complete`, `.../reopen`, etc.)
- Assignee endpoints

Watch for:

- The optional `price` + `currencyCode` pair in request and response schemas — keep them documented together with a note that they move as a pair.
- `TaskStatus` enum values in the YAML matching the enum in `:core` exactly.
- Pagination shape (consistent with House + Settlement listings).

## Shared / client side

- `TaskStatus` and `AccessLevel` are `enum class` in `:core` — fixed closed sets, fine to encode as enums.
- `currencyCode: String?` — never an enum.
- Components: `TaskComponent`, `TaskDetailsComponent`, `TaskExplorationComponent`, etc., in `shared/.../ui/nav/tasks/`. Compose binding in `androidApp/.../ui/task/`.
- The Task explore / list screen uses SQLDelight-backed cache for offline read. Background sync writes through the Repository.

## Anti-patterns — reject on sight

- A task that doesn't take `houseId` in its request or doesn't filter by it in its query.
- `price: Double?` with no `currencyCode`, or vice versa.
- `currencyCode` typed as an enum.
- Inline status-transition logic in a route handler (must be in the service).
- Creating a Settlement automatically when a task is marked complete (decouple — that's a separate user action).
- A list endpoint that ignores `AccessLevel` and leaks private tasks.
- New endpoint without Swagger.
