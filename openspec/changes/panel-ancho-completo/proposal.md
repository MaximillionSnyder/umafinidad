## Why

The resize cap kept the panel from reaching the bubble: `maxAncho` was `screenWidth - bubbleSize - 3 * margin`. That was fine while the panel was always placed automatically on the opposite side, but now that the user can move it wherever they want, the cap is an arbitrary limit: someone playing with the panel at the top or bottom of the screen may want it full width, and the bubble is not in the way there.

## What Changes

- **Full-width resize**: the maximum width becomes the whole screen minus the two margins (`screenWidth - 2 * margin`). The minimum and the height behaviour are unchanged.
- **Bubble no longer reserved**: the panel may end up covering the floating bubble. Closing affordances are unaffected (the header X, the "Hide" button and the Back key), and the panel can be moved or shrunk again at any time.
- **Simpler geometry**: `PosicionPanel.maxAncho` and `PosicionPanel.redimensionar` no longer take the bubble size.
- The automatic opening width is untouched (50 % of the screen, capped at 300 dp): widening is a deliberate gesture and the chosen size is remembered.

## Capabilities

### Modified Capabilities

- `floating-panel-move`: the resize requirement now allows the panel to reach the full screen width instead of stopping before the bubble.

## Impact

- **Code**: `overlay/PosicionPanel.kt` (`maxAncho`, `redimensionar` signature), `overlay/BurbujaService.kt` (call sites).
- **Tests**: `PosicionPanelTest` — the old "never reaches the bubble" case becomes "reaches the full width" (both sides, with the anchored edge landing on the left margin) plus a direct `maxAncho` check.
- **Docs**: README bubble section.
- **Risks**: with the panel full width and overlapping the bubble, the bubble cannot be tapped; the panel still closes with X, Hide or Back. No change for users who never resize.
