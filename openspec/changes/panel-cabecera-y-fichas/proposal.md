## Why

Two wastes in the quick panel, both more visible now that the panel opens as a wide band:

1. The quick actions (clear lineage, autofill) sat in their own row under the header, spending ~48 dp of a strip that is only 38 % of the screen tall. They belong next to the title they act on.
2. The suggestion tiles are distributed to fill the row, but the face inside each tile stayed at a fixed 32 dp: on a 383 dp panel each 54.8 dp tile had ~22.8 dp of empty space around the face.

## What Changes

- **Header holds the quick actions**: clear, autofill and the affinity total move into the header row (grip · title · clear · autofill · total · close) and the separate actions row disappears. The title keeps `weight(1f)` with ellipsis so narrow panels still work.
- **Faces fill their tile**: the avatar size is derived from the distributed tile width (`ladoCaraFicha = tile − 8 dp`, clamped to 24–96 dp). On the reference panel the face goes from 32 dp to ~46.8 dp (+46 %) and the per-tile gap from 22.8 dp to 8 dp.
- **The "more" gradient follows the row height** (`fillMaxHeight`) instead of the old fixed tile height, since the row is taller now.

## Capabilities

### Modified Capabilities

- `floating-quick-panel` (new capability documented here): layout of the floating quick panel — header actions and suggestion tile sizing.

## Impact

- **Code**: `overlay/PanelBurbuja.kt` (header row, removed actions row, gradient height), `overlay/PanelBurbujaComponentes.kt` (`ladoCaraFicha`, `FichaOpcion`).
- **Resources**: none (no new strings).
- **Tests**: no new unit tests (pure Compose layout); the panel geometry tests are unaffected.
- **Risks**: the header is busier — on very narrow panels the title ellipsizes; the taller suggestion row (face + name) makes the search state scroll a bit more.
