## Context

JustWoo-KMP uses Decompose for navigation. Currently, after a successful registration the app routes to the main screen without checking whether the new user has a house association. The backend has a concept of "houses" (households) and user roles (admin vs. member), but there is no onboarding step to get a user into a house.

The invite-code flow is chosen over a direct "search and request" approach so that admins control who can even see their house — privacy-by-default.

## Goals / Non-Goals

**Goals:**
- Route newly-registered users to a house-selection screen before entering the main app
- Let users create a new house (they become admin) or request to join an existing one
- Provide a short-lived, admin-generated invite code to gate join requests
- Give admins an in-app flow to approve or reject pending join requests

**Non-Goals:**
- Editing or deleting a house after creation (separate change)
- Bulk invite (multiple codes at once)
- Push notifications for join-request events (can be added later)
- Web or iOS UI (Android-first; iOS SwiftUI wiring is a follow-up)

## Decisions

### 1. Decompose navigation: add `HouseOnboardingComponent` as a root-level destination

**Decision**: Add a new `HouseOnboardingComponent` to the root `RootComponent`. After registration succeeds, if the user has no house, the root navigates to `HouseOnboarding` instead of `Main`.

**Rationale**: Keeps nav logic in the component layer (consistent with existing pattern). The child `Content` composable renders whichever child is active — no special casing in UI code.

**Alternative considered**: Push the onboarding screen onto the auth stack. Rejected because it muddies the auth/main boundary; house onboarding logically belongs to "initial app setup", not auth.

---

### 2. Invite code: short-lived, server-generated, single-use

**Decision**: The backend generates a random alphanumeric code (e.g., 6 chars), stores it with a 15-minute TTL and a `used` flag, and ties it to a specific house. The code is returned once to the admin and never stored client-side beyond display.

**Rationale**: Short TTL limits exposure. Single-use prevents replay. Server-generated avoids predictable patterns.

**Alternative considered**: QR code. Deferred — adds library dependency; plain code covers the near-term need.

---

### 3. Join request state machine: PENDING → APPROVED | REJECTED

**Decision**: `join_requests` table has a `status` enum. Admins call a dedicated approve/reject endpoint. Approved requests trigger a house_members insert. Rejected requests are soft-deleted (status = REJECTED, row kept for audit).

**Rationale**: Simple two-outcome state machine is sufficient. Keeping rejected rows gives admins visibility into past requests.

---

### 4. Admin pending-requests surface: pull-based (no push)

**Decision**: Admins see pending requests on a screen within the house management section. They navigate there explicitly; no real-time push in this change.

**Rationale**: Simpler to ship. Push notifications can be layered in later without changing the data model.

## Risks / Trade-offs

- **Invite code exposure** → The code is displayed in plain text on the admin's screen. Mitigation: 15-min TTL + single-use; advise admins in UI copy to share only via private channel.
- **Race condition on approval** → Two admins could both approve the same request. Mitigation: backend `UPDATE … WHERE status = 'PENDING'` with a row-level lock; second approval returns a 409.
- **User stuck without a house** → If onboarding is dismissed or the app crashes mid-flow, the user re-enters with no house. Mitigation: on app launch, check `user.houseId`; if null, redirect to onboarding regardless of auth state.
- **No iOS UI** → Android ships first; iOS wiring is a follow-up.

## Migration Plan

1. Apply DB migrations: add `invite_codes` and `join_requests` tables.
2. Deploy backend with new endpoints behind a feature flag.
3. Ship Android app update; post-registration navigation change is gated on backend availability.
4. Remove feature flag once rollout is stable.

## Open Questions

- Should we allow a user to belong to multiple houses, or exactly one? (Current assumption: exactly one — simplifies UI; revisit if needed.)
- What happens if an admin's code expires before the member tries to use it? UI should show a clear "code expired, ask admin to generate a new one" message — confirm copy with design.
