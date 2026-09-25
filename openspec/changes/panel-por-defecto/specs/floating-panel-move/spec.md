## Purpose

Defines where the floating quick panel opens when the user has not moved or resized it: a wide band stuck to the top edge of the screen.

## MODIFIED Requirements

### Requirement: Remembered position

The panel position SHALL be remembered between openings and restored clamped to the current screen. While no position has been stored, the panel SHALL open as a wide band stuck to the top edge of the screen, on the side opposite the bubble, with a default size of 93 % of the screen width (capped for tablets) and 38 % of its height.

#### Scenario: Reopen keeps the position
- **WHEN** the user moves the panel, closes it and opens it again
- **THEN** it appears where the user left it

#### Scenario: First opening
- **WHEN** the user opens the panel for the first time (no stored position or size)
- **THEN** it appears as a band stuck to the top edge, about 93 % of the screen wide and 38 % tall, on the side opposite the bubble

#### Scenario: Rotation or smaller screen
- **WHEN** the stored position would place the panel outside the screen after a rotation
- **THEN** the panel is shown clamped inside the screen

#### Scenario: Reset returns to automatic
- **WHEN** the user taps "Reset size" in Ajustes → Floating bubble
- **THEN** the stored size and position are cleared and the panel opens again in the top band
