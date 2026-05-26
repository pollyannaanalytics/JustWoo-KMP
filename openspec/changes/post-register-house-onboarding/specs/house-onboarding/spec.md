## ADDED Requirements

### Requirement: New user is routed to house onboarding after registration
After a successful registration, if the authenticated user has no house association, the app SHALL navigate to the House Onboarding screen instead of the main app screen.

#### Scenario: Registration completes with no house
- **WHEN** a user completes registration successfully
- **THEN** the app navigates to the House Onboarding screen

#### Scenario: User with existing house skips onboarding
- **WHEN** a registered user logs in and already has a house association
- **THEN** the app navigates directly to the main app screen, bypassing onboarding

#### Scenario: App launch with incomplete onboarding
- **WHEN** the app launches and the authenticated user has no house association
- **THEN** the app routes to the House Onboarding screen regardless of prior session state

### Requirement: House Onboarding screen presents two actions
The House Onboarding screen SHALL present exactly two options: "Join a House" and "Create a House".

#### Scenario: User sees both options
- **WHEN** the House Onboarding screen is displayed
- **THEN** both "Join a House" and "Create a House" actions are visible and tappable

#### Scenario: User selects Join a House
- **WHEN** the user taps "Join a House"
- **THEN** the app navigates to the Join House screen

#### Scenario: User selects Create a House
- **WHEN** the user taps "Create a House"
- **THEN** the app navigates to the Create House screen
