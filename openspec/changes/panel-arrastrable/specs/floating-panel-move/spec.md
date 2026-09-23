## Purpose

Defines how the floating quick panel can be moved by the user, how its position is clamped and remembered, and how resizing keeps working once the panel is no longer in its automatic place.

## ADDED Requirements

### Requirement: Dragging the panel

The panel header SHALL be a drag surface that moves the whole panel, following the finger in both axes, and the movement SHALL be clamped so that the panel always stays inside the screen with the standard margin.

#### Scenario: Move the panel with the finger
- **WHEN** the user drags the panel header
- **THEN** the panel follows the drag live and stops at the screen edges instead of leaving the visible area

#### Scenario: The close button still works
- **WHEN** the user taps the close (X) button in the header
- **THEN** the panel closes as before (the drag only starts after the touch slop)

### Requirement: Remembered position

The panel position SHALL be remembered between openings and restored clamped to the current screen; while no position has been stored, the panel SHALL keep the automatic placement (opposite side of the bubble, vertically centered).

#### Scenario: Reopen keeps the position
- **WHEN** the user moves the panel, closes it and opens it again
- **THEN** it appears where the user left it

#### Scenario: Rotation or smaller screen
- **WHEN** the stored position would place the panel outside the screen after a rotation
- **THEN** the panel is shown clamped inside the screen

#### Scenario: Reset returns to automatic
- **WHEN** the user taps "Reset size" in Ajustes → Floating bubble
- **THEN** the stored size and position are cleared and the panel opens in its automatic place

### Requirement: Moving and resizing are Pro

Moving the panel SHALL require a Pro license, like resizing. Without one the header SHALL NOT be draggable, the resize handle SHALL NOT be shown, a shortcut to the Pro screen SHALL be offered, and any stored position or size SHALL be ignored.

#### Scenario: Pro user moves and resizes
- **WHEN** Pro is active
- **THEN** the header drags the panel and the corner handle resizes it, and both are remembered

#### Scenario: Free user keeps the automatic panel
- **WHEN** Pro is not active and the user opens the panel
- **THEN** the panel is placed automatically, shows the lock shortcut to the Pro screen, and neither dragging the header nor the handle changes anything

### Requirement: Resize relative to the panel

The resize handle SHALL be placed on the panel's inner corner (facing the centre of the screen) according to the panel's own position, and resizing SHALL keep the edge opposite the handle anchored, so the panel grows towards the centre instead of jumping back to the bubble's side.

#### Scenario: Panel moved to the other half
- **WHEN** the user moves the panel to the other half of the screen and resizes it
- **THEN** the handle appears on the inner corner of its new side and the panel grows towards the centre, keeping the outer edge in place
