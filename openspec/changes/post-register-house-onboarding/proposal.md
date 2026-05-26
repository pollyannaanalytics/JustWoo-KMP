## Why

After completing registration, new users have no context for how to proceed — they need to either join an existing household or create one before they can use the app meaningfully. Without this onboarding step, new users land in a broken state with no house association.

## What Changes

- Add a post-registration "House Onboarding" screen that lets users choose between joining a house or creating one
- **Join flow**: house admin generates a short-lived invite code → new user enters the code to request membership → admin reviews and approves/rejects the request
- **Create flow**: new user creates a house and becomes its admin
- Backend provides three new API endpoints: generate invite code, submit join request, and approve/reject join request
- Navigation: after register completes, route new users to the house onboarding screen instead of directly to the main app

## Capabilities

### New Capabilities

- `house-onboarding`: Post-registration screen allowing users to choose between joining or creating a house
- `house-invite-code`: Admin generates a temporary invite code; members use it to request joining a house
- `house-join-request`: Member submits a join request using an invite code; admin reviews and approves or rejects it
- `house-creation`: New user creates a house and is assigned the admin role

### Modified Capabilities

- (none — no existing spec-level behavior is changing)

## Impact

- **Navigation**: Post-register flow must route to `HouseOnboardingComponent` instead of the main screen; requires changes to Decompose root component
- **Backend API**: Three new endpoints needed (generate code, submit request, approve/reject)
- **Android UI**: Two new screens (`HouseOnboardingScreen`, `JoinHouseScreen`, `CreateHouseScreen`) plus an admin-side pending-requests screen
- **Database**: New tables for `invite_codes` and `join_requests`
- **Auth**: Invite code endpoint is admin-only; join request approval is admin-only
