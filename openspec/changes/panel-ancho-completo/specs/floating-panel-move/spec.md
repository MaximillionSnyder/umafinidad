## Purpose

Defines how far the floating quick panel can be resized: the user can widen it to the whole screen width.

## MODIFIED Requirements

### Requirement: Resize relative to the panel

The resize handle SHALL be placed on the panel's inner corner (facing the centre of the screen) according to the panel's own position, and resizing SHALL keep the edge opposite the handle anchored, so the panel grows towards the centre instead of jumping back to the bubble's side. The panel SHALL be able to grow up to the full screen width minus the standard margins, and SHALL never leave the screen.

#### Scenario: Panel moved to the other half
- **WHEN** the user moves the panel to the other half of the screen and resizes it
- **THEN** the handle appears on the inner corner of its new side and the panel grows towards the centre, keeping the outer edge in place

#### Scenario: Widen to the full screen
- **WHEN** the user drags the resize handle outwards until the panel stops
- **THEN** the panel spans the full screen width minus the margins, and that size is remembered for the next openings

#### Scenario: The panel can still be closed when it covers the bubble
- **WHEN** the panel is wide enough to overlap the floating bubble
- **THEN** the panel still closes with the header X, the "Hide" button or the Back key, and it can be moved or shrunk again
