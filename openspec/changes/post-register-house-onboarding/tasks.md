## 1. Database Migrations

- [x] 1.1 Create `invite_codes` table (fields: id, house_id, code, expires_at, used, created_by)
- [x] 1.2 Create `join_requests` table (fields: id, house_id, user_id, invite_code_id, status enum[PENDING/APPROVED/REJECTED], created_at)
- [x] 1.3 Create `houses` table if not already present (fields: id, name, created_at) — already existed
- [x] 1.4 Add `house_id` and `role` columns to `users` table — skipped; membership is handled by the existing `house_members` table (many-to-many)

## 2. Backend API — House Creation

- [x] 2.1 Implement `POST /api/houses` endpoint — creates house + assigns caller as ADMIN in a single transaction — already existed; validated and wired
- [x] 2.2 Add validation: name required, max 50 chars; reject if caller already has a house (409)
- [x] 2.3 Write unit tests for creation endpoint (success, empty name, duplicate house membership)

## 3. Backend API — Invite Code

- [x] 3.1 Implement `POST /api/houses/{houseId}/invite-codes` endpoint — admin-only, generates 6-char alphanumeric code with 15-min TTL
- [x] 3.2 Enforce single-use: mark code `used = true` when a join request is submitted against it
- [x] 3.3 Add 403 guard for non-admin callers
- [x] 3.4 Write unit tests (success, expired code, non-admin, already-used)

## 4. Backend API — Join Request

- [x] 4.1 Implement `POST /api/join-requests` endpoint — validates code, creates PENDING join request
- [x] 4.2 Implement `GET /api/houses/{houseId}/join-requests` endpoint — returns all PENDING requests (admin-only)
- [x] 4.3 Implement `PATCH /api/join-requests/{id}` endpoint — approve or reject; on approve, insert into house_members; use row-level lock to prevent race conditions (return 409 if not PENDING)
- [x] 4.4 Implement `GET /api/join-requests/me` endpoint — returns current user's latest join request status
- [x] 4.5 Write unit tests for all join request endpoints

## 5. Shared / KMP Domain Layer

- [x] 5.1 Define `HouseInviteRepository` interface with `createHouse`, `generateInviteCode`, `submitJoinRequest`, `getPendingRequests`, `approveRequest`, `rejectRequest`, `getMyJoinRequestStatus`
- [x] 5.2 Implement `DefaultHouseInviteRepository` wiring each method to the corresponding API call via `HouseInviteApiService`
- [x] 5.3 Add `CreateHouseUseCase`, `GenerateInviteCodeUseCase`, `SubmitJoinRequestUseCase`, `ApproveMemberUseCase`, `RejectMemberUseCase`, `GetJoinRequestStatusUseCase`
- [x] 5.4 Add DTOs: `InviteCodeResponse`, `JoinRequestBody`, `JoinRequestResponse`, `JoinRequestDecision`; add `JoinRequestStatus` enum to core

## 6. Backend Verification

- [ ] 6.1 Manual smoke test: create house → invite code generated → join request submitted → admin approves → user has house_id set
- [ ] 6.2 Manual smoke test: rejected join request returns correct status via `GET /api/join-requests/me`
- [ ] 6.3 Confirm expired and already-used codes return appropriate errors
- [ ] 6.4 Confirm non-admin callers receive 403 on admin-only endpoints
