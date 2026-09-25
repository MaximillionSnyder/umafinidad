## Purpose

Defines the behaviour of the "Mi corredora" screen: which alternatives a slot offers and how the panel's actions are laid out.

## ADDED Requirements

### Requirement: Every valid alternative is offered

Tapping an editable slot SHALL list every character that satisfies the game rules for that slot (playable and active characters, excluding the current occupant), ordered by resulting total descending.

#### Scenario: Open a slot
- **WHEN** the user taps a parent or grandparent slot
- **THEN** the sheet lists all valid candidates, not a truncated top list, so a character outside the previous top 20 can still be chosen

#### Scenario: Ordering
- **WHEN** the list is shown
- **THEN** it is ordered by resulting affinity total (descending) and, on ties, by the points the candidate contributes directly

### Requirement: Actions share one row

The best-lineage panel SHALL show "Ver herencia" and "Guardar configuración" side by side in a single row, with labels that shrink to fit instead of being clipped.

#### Scenario: Panel shown with Pro
- **WHEN** the panel is displayed with an active license
- **THEN** both buttons appear in one row at half width each and the save button opens the save dialog

#### Scenario: Panel shown without Pro
- **WHEN** the panel is displayed without a license
- **THEN** the save button keeps the lock icon, opens the Pro screen, and the Pro hint is shown under the row
