## ADDED Requirements

### Requirement: User can create a new house and become its admin
A user without a house association SHALL be able to create a new house by providing a house name, and they SHALL be assigned the admin role for that house upon creation.

#### Scenario: Successful house creation
- **WHEN** a user submits a valid house name on the Create House screen
- **THEN** a new house record is created in the backend
- **AND** the user is assigned the admin role for that house
- **AND** the app navigates the user to the main app screen

#### Scenario: House name is empty
- **WHEN** the user submits the Create House form with an empty name
- **THEN** the app displays a validation error and does NOT submit to the backend

#### Scenario: House name is too long
- **WHEN** the user submits a house name exceeding 50 characters
- **THEN** the backend SHALL return a 400 error
- **AND** the UI displays an appropriate validation message

#### Scenario: User already has a house
- **WHEN** a user who already belongs to a house attempts to create a new house
- **THEN** the backend SHALL return a 409 Conflict response

### Requirement: House creation is atomic
The backend SHALL ensure that house creation and admin role assignment succeed or fail together — no partial state.

#### Scenario: Creation rolls back on failure
- **WHEN** any step of the house creation transaction fails
- **THEN** no house record or role assignment is persisted
- **AND** the backend returns a 500 error
