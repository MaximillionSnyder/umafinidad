## Purpose

Defines the optional two-column (pedigree) layout of the floating quick panel's lineage.

## ADDED Requirements

### Requirement: Two-column lineage layout

The panel SHALL offer an optional layout (Ajustes → Burbuja flotante → "Genealogía en dos columnas", off by default) where the child keeps a full-width card and the remaining six slots are shown two per row, grouped by branch: the left column is Parent 1's line (slots 1, 3, 4) and the right column Parent 2's line (slots 2, 5, 6). Every slot SHALL appear exactly once.

#### Scenario: Enable the option
- **WHEN** the user turns the option on and opens the panel
- **THEN** the child is a full-width card and below it the rows are `Parent 1 | Parent 2`, then the first grandparent of each branch, then the second grandparent of each branch

#### Scenario: Tapping a slot
- **WHEN** the user taps a slot in the two-column layout
- **THEN** the same action runs as in the single-column list (remove the character or mark the slot as the destination)

#### Scenario: Option off
- **WHEN** the option is off (default)
- **THEN** the lineage is listed as seven full-width rows, as before

### Requirement: Marquee on narrow slot cards

In the two-column layout the slot name and role SHALL scroll (marquee) when they do not fit the card, so the full name can be read.

#### Scenario: Long name in a narrow card
- **WHEN** a slot holds a character whose name does not fit its half-width card
- **THEN** the name scrolls horizontally instead of being truncated, and the full name is still announced by screen readers

### Requirement: Correct grandparent numbering

Slot labels SHALL number the grandparents within their branch: slots 3 and 5 are the first grandparent of each branch and slots 4 and 6 the second, in both layouts.

#### Scenario: Grandparent labels
- **WHEN** the panel shows the grandparents
- **THEN** the label of slot 4 reads "Grandparent (2)" (second of Parent 1's line) and the label of slot 6 reads "Grandparent (2)" (second of Parent 2's line), instead of both reading "Grandparent (1)"
