## ADDED Requirements

### Requirement: Admin can generate a short-lived invite code
A house admin SHALL be able to generate a single-use, time-limited invite code for their house via the app.

#### Scenario: Admin generates a code
- **WHEN** an admin taps "Generate Invite Code"
- **THEN** the backend creates a 6-character alphanumeric code tied to the admin's house, with a 15-minute TTL
- **AND** the code is displayed on screen for the admin to share

#### Scenario: Code expires after TTL
- **WHEN** 15 minutes have elapsed since the code was generated
- **THEN** the code is no longer valid and any attempt to use it SHALL return an error

#### Scenario: Code is single-use
- **WHEN** a code has already been used to submit a join request
- **THEN** subsequent attempts to use the same code SHALL return an error

#### Scenario: Non-admin cannot generate a code
- **WHEN** a user without admin role calls the generate-code endpoint
- **THEN** the backend SHALL return a 403 Forbidden response

### Requirement: Backend returns invite code in response
The generate-invite-code API SHALL return the code and its expiry timestamp in the response body.

#### Scenario: Successful code generation response
- **WHEN** the generate-code request succeeds
- **THEN** the response includes `code` (string) and `expiresAt` (ISO-8601 timestamp)
