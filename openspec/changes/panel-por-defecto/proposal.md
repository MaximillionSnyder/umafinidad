## Why

The panel used to open vertically centred on the opposite side of the bubble, a strip that covers the middle of the screen — exactly where the game shows the roster and the selection controls. The reference screenshot (a real game session) shows the arrangement that works: a wide band stuck to the **top** edge, ~93 % wide and ~38 % tall, leaving the centre and the bottom of the screen free.

## What Changes

- **Default placement**: the automatic position is now the top edge (`y = margin`) on the side opposite the bubble, instead of vertically centred.
- **Default size**: `FRACCION_ANCHO_PANEL` 0.50 → **0.93** and `FRACCION_ALTO_PANEL` 0.66 → **0.38**, with `ANCHO_PANEL_MAX_DP` 300 → 420 so the default stays wide on tablets too. The measured reference is 383 × 346 dp on a 411 × 914 dp screen (93.1 % × 37.8 %).
- **Saved geometry still wins**: whoever already moved or resized the panel keeps their position and size; "Reset size" in Ajustes clears both and brings back the new defaults.
- `PosicionPanel.calcular` no longer needs the screen height (nothing to centre or clamp vertically: the band is anchored to the top margin).

## Capabilities

### Modified Capabilities

- `floating-panel-move`: the automatic placement requirement now anchors the panel to the top edge, and the documented default size matches the reference screenshot.

## Impact

- **Code**: `overlay/PosicionPanel.kt` (`calcular`), `overlay/BurbujaUi.kt` (three constants), `overlay/BurbujaService.kt` (call site).
- **Tests**: `PosicionPanelTest` — "centred vertically" becomes "opens stuck to the top edge".
- **Docs**: README (bubble section).
- **Risks**: the band covers the game's top bar by design; the bubble stays reachable because it sits vertically centred, well below the band. Users with a saved geometry see no change until they reset.
