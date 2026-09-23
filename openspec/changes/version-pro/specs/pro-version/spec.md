## Purpose

Defines the offline Pro license and the features it unlocks, so the free app keeps every current capability and the paid tools (bubble panel resize, best-parents ranking, saving lineages) are available only with a valid license code.

## ADDED Requirements

### Requirement: Offline license activation

The app SHALL validate a Pro license code entirely on the device, accepting codes of the form `UMA-XXXX-XXXX-XXXX` whose last four characters match the checksum of the eight-character body, and SHALL tolerate lowercase letters, spaces and missing dashes. No network access, account or external service SHALL be required.

#### Scenario: Valid code activates Pro
- **WHEN** the user enters a valid license code in the Pro screen and taps Activate
- **THEN** the app stores the license, shows "Pro activated", displays the formatted code and the activation date, and the Pro features become available immediately

#### Scenario: Invalid code is rejected
- **WHEN** the user enters a code with a wrong length, wrong prefix or altered checksum
- **THEN** the app shows an inline "not valid" message, keeps the free tier and stores nothing

#### Scenario: Activation survives restarts
- **WHEN** the app is closed and reopened after a successful activation
- **THEN** Pro is still active and the Pro screen shows the stored code and date

### Requirement: License deactivation

The Pro screen SHALL let the user deactivate the license on the device after an explicit confirmation, and the same code SHALL reactivate it.

#### Scenario: Deactivate Pro
- **WHEN** the user confirms "Deactivate Pro" in the Pro screen
- **THEN** the license is removed, the Pro features lock again and the free tier is fully usable

#### Scenario: Reactivate with the same code
- **WHEN** the user enters the previously used code after deactivating
- **THEN** Pro is activated again

### Requirement: Bubble panel resize is Pro

Resizing the floating quick panel SHALL require a license. Without one the panel SHALL NOT offer the resize handle, SHALL ignore any previously saved manual panel size, and SHALL offer a shortcut that opens the Pro screen.

#### Scenario: Licensed user resizes the panel
- **WHEN** Pro is active and the user drags the panel's corner handle
- **THEN** the panel resizes within the existing limits and the size is remembered for the next openings

#### Scenario: Free user cannot resize
- **WHEN** Pro is not active and the user opens the quick panel
- **THEN** the panel uses its automatic size, shows the lock shortcut instead of the handle, and tapping the shortcut opens the Pro screen

### Requirement: Best-parents ranking is Pro

The best-parents ranking mode SHALL require a license. Without one the mode SHALL remain visible with a lock indicator, the ranking SHALL NOT be computed, and the content SHALL show the unlock call to action.

#### Scenario: Licensed user opens best parents
- **WHEN** Pro is active and the user selects the "Best parents" mode
- **THEN** the ranking is computed and listed with times chosen and average points

#### Scenario: Free user sees the locked mode
- **WHEN** Pro is not active and the user selects the "Best parents" mode
- **THEN** the app shows the locked explanation with an unlock button that opens the Pro screen, without computing the ranking

### Requirement: Saving lineages is Pro

Saving a lineage configuration SHALL require a license. Reading and deleting already-saved lineages SHALL remain free.

#### Scenario: Licensed user saves a configuration
- **WHEN** Pro is active and the user taps Save configuration in Mi corredora
- **THEN** the name dialog opens and the configuration is stored and listed

#### Scenario: Free user is sent to the Pro screen
- **WHEN** Pro is not active and the user taps Save configuration
- **THEN** the save dialog does not open and the Pro screen is shown instead

#### Scenario: Saved lineages stay available
- **WHEN** Pro is not active and the user has previously saved lineages
- **THEN** they are still listed in Mi corredora and Ajustes and can still be opened and deleted

### Requirement: Free tier is unchanged

Every feature that is not one of the three Pro tools SHALL keep working without a license, and the Pro gate SHALL NOT alter accessibility roles, headings or announced state.

#### Scenario: Free user keeps the rest of the app
- **WHEN** the app runs without a license
- **THEN** affinity, top lineages, the versatile ranking, groups, the roster, saved-lineage reading, bubble sizes, translucent panel, themes, languages and accessibility options all behave as before

#### Scenario: Locked states remain accessible
- **WHEN** a screen reader user reaches a locked Pro feature
- **THEN** the lock is announced and the unlock button is focusable and labelled

### Requirement: License issuing stays reproducible

The repository SHALL provide a script that issues codes with the same algorithm the app validates, and the unit tests SHALL pin fixed vectors shared by both implementations.

#### Scenario: Issue codes from the repository
- **WHEN** the maintainer runs `node scripts/generar-codigos-pro.mjs 20`
- **THEN** 20 codes are printed and each one validates in the app

#### Scenario: Algorithms cannot drift
- **WHEN** the Kotlin checksum changes without updating the script (or the other way around)
- **THEN** `ProTest` fails on the pinned vectors
