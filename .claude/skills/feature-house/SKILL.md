---
name: feature-house
description: House domain rules for JustWoo — household groups with ADMIN/MEMBER roles, invite codes + join requests, role checks live in the service layer, TDD across all layers, Swagger contract is mandatory. Trigger when touching HouseRoutes, HouseService, HouseRepository, InviteCode / JoinRequest code, or shared House UseCases.
paths: **/HouseRoutes.kt, **/InviteRoutes.kt, **/HouseService.kt, **/HouseInviteService.kt, **/HouseRepository.kt, **/InviteCodeRepository.kt, **/JoinRequestRepository.kt, **/schema/Houses.kt, **/schema/InviteCodes.kt, **/schema/JoinRequests.kt, **/domain/usecase/house/**, **/ui/nav/house/**, **/ui/nav/houseinfo/**
---

# Feature: House

A House is the group container — members, tasks, and settlements all belong to a house. Files involved:

- `backend/.../routes/HouseRoutes.kt`, `InviteRoutes.kt`
- `backend/.../service/HouseService.kt`, `HouseInviteService.kt`
- `backend/.../repositories/HouseRepository.kt`, `InviteCodeRepository.kt`, `JoinRequestRepository.kt`
- `backend/.../schema/Houses.kt` (plus `HouseMembers` table)
- `:core` House DTOs, `MemberRole` enum, invite DTOs
- `shared/.../domain/usecase/house/**`
- `shared/.../ui/nav/house/**`, `shared/.../ui/nav/houseinfo/**`

Also read [`backend-best-practice`](../backend-best-practice/SKILL.md) and [`kmp-best-practice`](../kmp-best-practice/SKILL.md).

## Invariants — non-negotiable

1. **Two roles exist: `ADMIN` and `MEMBER`.** Stored in `HouseMembers.role` as `Int`. Only ADMIN can: change house metadata, remove a member, change another member's role, approve/reject join requests, delete the house. Don't invent a third role; if you need finer permissions, talk through the design first.
2. **Every house has at least one ADMIN.** The creator is ADMIN on house creation. Removing the last ADMIN is forbidden (service-layer check). Deleting an ADMIN account must transfer or block — never leave an orphan house.
3. **Role check belongs in the service.** Routes call `HouseService.requireAdmin(houseId, userId)`-style guards. Don't sprinkle `if (role != ADMIN)` inside route handlers.
4. **Invite codes are single-use by default and expire** (TTL stored in `TtlData` / Redis). Joining via an expired or used code returns a distinct error so the client can show a clear message — not a generic 403.
5. **Join requests are explicit.** A user requesting to join != being a member. The request enters `JoinRequests`, an ADMIN approves, and only then a row is inserted into `HouseMembers`. Don't shortcut by inserting directly.
6. **Cross-house data leakage is the worst-case bug.** Any list endpoint that returns Tasks / Settlements / Members must filter by `houseId` AND verify the requesting user is in that house. Tested in route-level integration tests.

## TDD flow for a House change

1. **Service test first** — role checks (admin path, member path, non-member path), last-admin protection, invite TTL expiry, duplicate join request.
2. **Repository test** against TestContainers Postgres — FK cascades on house deletion, member uniqueness constraints.
3. **Route test** in the style of `HouseInviteFlowTest.kt`:
   - Create house → generate invite → join via invite → see house in member's list.
   - Try to act as a non-member → 403.
   - Try to promote yourself → 403.

## Swagger contract

Touching any of these requires a YAML update in [`backend/src/main/resources/openapi/documentation.yaml`](../../../backend/src/main/resources/openapi/documentation.yaml):

- `GET/POST /houses`, `GET/PUT/DELETE /houses/{id}`
- `POST /houses/{id}/members`, `DELETE /houses/{id}/members/{userId}`, `PATCH .../role`
- Invite endpoints (`POST /houses/{id}/invites`, `POST /invites/{code}/join`, etc.)
- Join request endpoints

Pay attention to:

- `403` (member but not admin) vs `404` (not a member — don't leak existence).
- Pagination params on list endpoints — keep them consistent across House / Task / Settlement.

## Shared / client side

- `MemberRole` is an `enum class` in `:core` (this is fine — roles are a fixed closed set, unlike currency).
- Components in `shared/.../ui/nav/house/` and `houseinfo/` use Decompose (see [`decompose-nav`](../decompose-nav/SKILL.md)).
- Cached House list lives in SQLDelight; stale-while-revalidate when the user opens the house picker.

## Anti-patterns — reject on sight

- Role check inside `HouseRoutes.kt` instead of the service.
- A repository method `getAllTasks()` that doesn't take `houseId` — every read is house-scoped.
- Inserting into `HouseMembers` directly from an invite-accept handler without going through the JoinRequest flow (unless the invite is single-step by design — document why).
- Returning `403` for a non-member trying to GET a private house (use `404` to avoid leaking existence).
- A new endpoint without a Swagger entry.
- Removing a member without checking they aren't the last ADMIN.
