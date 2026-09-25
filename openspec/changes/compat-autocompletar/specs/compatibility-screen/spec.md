## Purpose

Defines the autofill shortcut of the Compatibility screen, which completes the lineage with the best available options.

## ADDED Requirements

### Requirement: Autofill from the Compatibility header

The Compatibility screen SHALL offer, next to the inheritance counter, the same autofill action as the floating panel: it fills every empty slot with the best affinity option, keeping the slots the user already chose and respecting the game rules. The child is never proposed as a grandparent.

#### Scenario: Complete a partial lineage
- **WHEN** the user has chosen the child (and maybe some other slots) and taps the autofill button
- **THEN** the empty slots are filled with the best options, the counter reaches 7/7 and the result panel updates

#### Scenario: Already chosen slots are respected
- **WHEN** the lineage has some slots filled
- **THEN** autofill keeps those characters and only completes the empty slots

### Requirement: Autofill feedback

While the computation runs the button SHALL show progress and reject new taps; when the action cannot run, the screen SHALL explain why instead of doing nothing.

#### Scenario: Computing
- **WHEN** the user taps autofill
- **THEN** the header shows a small progress indicator until the lineage is completed

#### Scenario: No child chosen
- **WHEN** the user taps autofill without a child
- **THEN** a message asks to pick the child first and nothing else changes

#### Scenario: Selection already complete
- **WHEN** the user taps autofill with all seven slots filled
- **THEN** the screen reports that the selection is complete and nothing changes
