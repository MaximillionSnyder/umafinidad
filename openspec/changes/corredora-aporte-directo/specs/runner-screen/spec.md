## ADDED Requirements

### Requirement: Each genealogy row shows its direct contribution

Every row of the best-lineage list (child, both parents and the four grandparents) SHALL show, next to the character name, the direct affinity points that character contributes to the tree: the sum of the bonds it participates in, formatted as `Npt` with the same rank colours as the slot alternatives sheet (◎ ≥20, ○ ≥10, △ ≥4). The fixed child row SHALL show its value too. A grandparent that is the same character as the child SHALL show 0 (the trainee bond is worth 0 in the game).

#### Scenario: Complete lineage

- **WHEN** the optimal lineage or a custom configuration is displayed
- **THEN** all seven rows show a number, including the child row

#### Scenario: Trainee as grandparent

- **WHEN** a grandparent slot holds the same character as the child
- **THEN** that row shows 0 pt

#### Scenario: Consistency with the alternatives sheet

- **WHEN** a character is a valid candidate in the alternatives sheet for a slot
- **THEN** the direct points the sheet lists for it equal the value computed by the shared helper for that slot

#### Scenario: Empty slots

- **WHEN** a slot has no character
- **THEN** its contribution is 0 and no number is shown on that row
