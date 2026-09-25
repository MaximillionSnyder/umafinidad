## Why

The panel shows the seven lineage slots as seven full-width rows, which in a wide band wastes a lot of horizontal space and pushes the suggestion carousel and the shortcuts below the fold. In the game UI the same information is a pedigree tree, so reading it as one is faster: child on top, then each parent with their own line below.

## What Changes

- **Optional two-column layout** (Ajustes → Burbuja flotante → "Genealogía en dos columnas", off by default): the child keeps a full-width card and the other six slots are shown two per row, grouped **by branch** — left column is Parent 1's line (1, 3, 4) and right column Parent 2's line (2, 5, 6), so rows are `[1|2]`, `[3|5]`, `[4|6]`.
- **Marquee for narrow cards**: in the two-column layout the name and the role label scroll (`basicMarquee`) when they do not fit, instead of being cut with an ellipsis; the avatar drops from 32 dp to 28 dp to give the text more room.
- **Slot label fix**: `etiquetaDeSlot` numbered the grandparents as 3,4 → "Grandparent (1)" and 5,6 → "Grandparent (2)", so slots 4 and 6 (the *second* grandparent of each branch) were mislabelled. Now 3 and 5 are the first grandparent of each branch and 4 and 6 the second, in both layouts.
- Scope: only the floating panel; the app's Compatibility screen keeps its layout. Free option (it is a view preference, not a Pro feature).

## Capabilities

### Modified Capabilities

- `floating-quick-panel`: adds the two-column lineage layout, its switch and the marquee behaviour for narrow slot cards.

## Impact

- **Code**: new `overlay/GrillaGenealogia.kt` (`FILAS_GENEALOGIA_POR_RAMA`), `overlay/PanelBurbuja.kt` (layout switch, fixed labels), `overlay/PanelBurbujaComponentes.kt` (`compacto` + marquee in `SlotGenealogia`), `overlay/BurbujaService.kt`, `data/PrefsRepository.kt` (`panel_dos_columnas`), `ui/AppViewModel.kt`, `ui/settings/SettingsScreen.kt`, `MainActivity.kt`.
- **Resources**: 2 strings × 9 locales (`burbuja_dos_columnas`, `burbuja_dos_columnas_desc`).
- **Tests**: `GrillaGenealogiaTest` pins the row/branch mapping (7 slots once each, child alone, pairs, column = branch).
- **Risks**: the marquee only runs when the text overflows; with the panel open the layout changes on the next opening (the switch lives in Ajustes).
