## Purpose

Defines the layout of the floating quick panel: where its quick actions live and how the suggestion tiles use the available width.

## ADDED Requirements

### Requirement: Quick actions in the header

The panel header SHALL hold the title together with the quick actions (clear lineage, autofill) and the affinity total, so the panel does not spend a separate row on them. The title SHALL ellipsize instead of pushing the actions out on narrow panels.

#### Scenario: Panel opens
- **WHEN** the user opens the quick panel
- **THEN** the header shows the drag grip (with Pro), the title, the clear button, the autofill button, the affinity total and the close button, and the content starts right below with the genealogy slots

#### Scenario: Narrow panel
- **WHEN** the panel is resized narrow
- **THEN** the title is ellipsized and the actions stay visible and tappable

### Requirement: Suggestion tiles fill their width

Each suggestion tile SHALL size its face from the width distributed for that tile (minus a small padding, clamped between 24 dp and 96 dp), so a wide panel shows bigger faces instead of empty space around them. The "more suggestions" gradient SHALL match the real height of the row.

#### Scenario: Wide panel
- **WHEN** the panel is at its default width and the search shows suggestions
- **THEN** each face fills its tile and the row uses the full available width

#### Scenario: Narrow panel
- **WHEN** the panel is narrow
- **THEN** the faces stay at their minimum size and the tiles remain tappable
