## Why

The quick panel that opens from the floating bubble is always placed for the user: on the opposite side of the bubble and vertically centered. It can be resized (Pro) but not moved, so on phones with a notch, a keyboard, or a game UI with fixed controls, the panel can end up covering exactly what the player needs to see. The user should be able to put it wherever they want.

## What Changes

- **Draggable panel**: the panel header becomes a drag surface (with a grip icon and a "Move panel" label); dragging moves the whole strip live, clamped to the screen so it can never be lost off-screen.
- **Remembered position**: the position is stored in pixels (`panel_x`, `panel_y`) and restored on the next opening, clamped to the current screen (rotation, split screen or a smaller display). `-1` keeps the automatic placement (opposite side of the bubble, vertically centered).
- **Pro gate**: moving joins the Pro feature list (`FuncionPro.BURBUJA_MOVER`), next to resizing. Both are enforced in the UI (no drag surface, no handle, lock shortcut to the Pro screen) and in the service (the drag/resize/persist paths return early).
- **Resize follows the panel, not the bubble**: the handle and the suggestion-carousel gradient now look at the panel's own half of the screen, so resizing keeps working after the panel has been moved (the anchored edge is the one opposite the handle, instead of the screen margin).
- **Reset**: "Reset size" in Ajustes also clears the stored position, returning the panel to its automatic placement.
- No new permissions, no service lifecycle change, no change to the bubble itself, and the free tier keeps today's automatic placement.

## Capabilities

### New Capabilities

- `floating-panel-move`: dragging, clamping, remembering and Pro-gating the position of the floating quick panel, and the panel-relative resize behavior.

### Modified Capabilities

- `pro-version`: the Pro feature list gains "move the floating panel" (`FuncionPro.BURBUJA_MOVER`); the existing resize requirement is unchanged.

## Impact

- **Code**: `overlay/PosicionPanel.kt` (new `acotar`/`mover`/`enLadoDerecho`, `redimensionar` now returns the rectangle including X), `overlay/PanelBurbuja.kt` (draggable header, `panelDerecha`, `puedeMover`/`puedeRedimensionar`), `overlay/BurbujaService.kt` (move/persist paths, panel side flow, gates via `funcionDisponible`), `data/PrefsRepository.kt` (`panelX`/`panelY`), `data/ProRepository.kt` (`BURBUJA_MOVER`), `ui/AppViewModel.kt` (reset also clears the position), `ui/pro/ProScreen.kt` (fourth feature card).
- **Resources**: `ic_mover.xml`; 3 strings × 9 locales (`burbuja_mover`, `pro_func_mover`, `pro_func_mover_desc`).
- **Tests**: `PosicionPanelTest` covers move, clamping, side detection and the anchored edge of the resize.
- **Risks**: dragging must not steal taps from the header's close button (the drag detector only consumes after touch slop); the window is repositioned with `updateViewLayout` per drag frame, same as the existing bubble drag.
