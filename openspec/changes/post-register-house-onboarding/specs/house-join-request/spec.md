## ADDED Requirements

### Requirement: User can submit a join request using an invite code
A user without a house association SHALL be able to enter an invite code to submit a join request for the corresponding house.

#### Scenario: Valid code submitted
- **WHEN** a user submits a valid, unexpired, unused invite code
- **THEN** a join request record is created with status PENDING
- **AND** the user sees a confirmation that their request was submitted

#### Scenario: Invalid or expired code
- **WHEN** a user submits a code that is expired, already used, or does not exist
- **THEN** the backend SHALL return a 400 error
- **AND** the UI displays a clear error message (e.g., "Code is invalid or expired. Ask your admin to generate a new one.")

#### Scenario: User already has a house
- **WHEN** a user who already belongs to a house submits a join request
- **THEN** the backend SHALL return a 409 Conflict response
- **AND** the UI displays an appropriate error

### Requirement: Admin can view pending join requests
A house admin SHALL be able to see a list of all PENDING join requests for their house.

#### Scenario: Admin views pending requests
- **WHEN** an admin navigates to the pending requests screen
- **THEN** all PENDING join requests for their house are displayed, each showing the requester's name and request timestamp

#### Scenario: No pending requests
- **WHEN** there are no pending join requests
- **THEN** the screen displays an empty state message

### Requirement: Admin can approve or reject a join request
A house admin SHALL be able to approve or reject any PENDING join request.

#### Scenario: Admin approves a request
- **WHEN** an admin taps "Approve" on a pending join request
- **THEN** the join request status is set to APPROVED
- **AND** the user is added to the house as a member
- **AND** the request is removed from the pending list in the UI

#### Scenario: Admin rejects a request
- **WHEN** an admin taps "Reject" on a pending join request
- **THEN** the join request status is set to REJECTED
- **AND** the request is removed from the pending list in the UI

#### Scenario: Race condition — request already processed
- **WHEN** an admin attempts to approve or reject a request that is no longer PENDING
- **THEN** the backend SHALL return a 409 Conflict response
- **AND** the UI refreshes the list to reflect the current state

### Requirement: Join request status is visible to the requester
After submitting a join request, the requesting user SHALL see their request status while waiting for admin action.

#### Scenario: Request is pending
- **WHEN** the user's join request is PENDING
- **THEN** the app shows a "Waiting for admin approval" state on the onboarding screen

#### Scenario: Request is approved
- **WHEN** the user's join request is APPROVED
- **THEN** the app routes the user into the main app screen

#### Scenario: Request is rejected
- **WHEN** the user's join request is REJECTED
- **THEN** the app returns the user to the House Onboarding screen with a message indicating rejection
