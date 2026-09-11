## Purpose

Defines the first-run accessibility welcome window: it proposes accessibility settings based on read-only system signals, previews each change live, and lets the user confirm or revert, with the choice persisted and re-openable from Settings.

## ADDED Requirements

### Requirement: Welcome window appears only until the user decides

On first launch (no persisted decision), the app SHALL show a non-cancelable accessibility welcome window offering text size, bold text, and high-contrast theme. Confirming or skipping SHALL persist the decision so the window does not appear again on later launches. Skipping SHALL restore the settings that were active before the window opened.

#### Scenario: First launch shows the window

- **WHEN** the app is launched on a clean install
- **THEN** the accessibility welcome window is shown and cannot be dismissed without choosing Save or Skip

#### Scenario: Decision persists

- **WHEN** the user chooses Save or Skip and later relaunches the app
- **THEN** the welcome window is not shown again

#### Scenario: Skip reverts preview

- **WHEN** the user changes options and then chooses Skip
- **THEN** text size, bold text, and theme return to the values they had before the window opened

### Requirement: System signals preselect options without auto-applying

The window SHALL read local system accessibility signals to preselect options: enlarged system font preselects the matching text size, enabled touch exploration (TalkBack) preselects bold text, and the system high-text-contrast setting preselects the high-contrast theme. Detection SHALL NOT apply or persist any setting by itself.

#### Scenario: Enlarged system font preselects text size

- **WHEN** the system font scale is at least 1.3 and the window opens
- **THEN** "very large" is preselected, and no larger than the detected level

#### Scenario: TalkBack preselects bold text

- **WHEN** touch exploration is enabled and the window opens
- **THEN** the bold text option is preselected

#### Scenario: System high contrast preselects high-contrast theme

- **WHEN** the system high-text-contrast setting is enabled and the window opens
- **THEN** the high-contrast theme is preselected

#### Scenario: Detection does not persist on its own

- **WHEN** the window opens with detected signals but the user never confirms
- **THEN** no accessibility preference is written

### Requirement: Changes preview live

Every option change in the welcome window SHALL apply immediately to the app behind the window, so the user sees the effect before deciding.

#### Scenario: Text size preview

- **WHEN** the user selects a different text size in the window
- **THEN** the app content behind the window re-renders at that size immediately

#### Scenario: Theme preview

- **WHEN** the user enables the high-contrast theme in the window
- **THEN** the app behind the window switches theme immediately

### Requirement: Welcome window is reopenable from Settings

Settings SHALL provide an entry that reopens the accessibility welcome window. Reopening SHALL NOT reset any previously persisted decision on its own; confirming SHALL apply the chosen options.

#### Scenario: Reopen from settings

- **WHEN** the user activates the "review accessibility" entry in Settings
- **THEN** the welcome window opens with the current settings preselected

#### Scenario: Confirm after reopen applies options

- **WHEN** the user changes an option in the reopened window and chooses Save
- **THEN** the chosen settings persist and remain after relaunch

### Requirement: Welcome window is accessible

Each control in the welcome window SHALL be a single accessibility focus target with the appropriate role and state, and the window SHALL be reachable with TalkBack.

#### Scenario: Single focus per control

- **WHEN** TalkBack explores the welcome window
- **THEN** each text-size option, the bold toggle, and the high-contrast toggle is focused exactly once with its role and checked state

#### Scenario: Actions are labeled

- **WHEN** TalkBack focuses the confirmation and skip actions
- **THEN** both announce their localized labels
