## Why

Two frictions in "Mi corredora":

1. Tapping a slot to swap a character (Vodka, a grandparent, …) only offered the best 20 candidates, so the character the user actually owns could be missing from the list.
2. "Guardar configuración" sat alone in its own full-width row below the panel, spending a whole line for a secondary action while "Ver herencia" lived inside the panel.

## What Changes

- **All alternatives**: the slot sheet lists every candidate that satisfies the game rules instead of `take(20)` (the option is a lazy column, so the height does not depend on the count). The computation cost is unchanged: the cap was applied *after* evaluating every candidate; only the sort/list is longer.
- **Save next to view lineage**: "Guardar configuración" moves into the panel's action row beside "Ver herencia", each button at half width, with `TextAutoSize` (9–14 sp) so the label fits in all nine languages. The Pro lock hint stays below the row for free users.
- No new strings, no domain change, no change to the bubble panel.

## Capabilities

### New Capabilities

- `runner-screen`: the "Mi corredora" screen — completeness of the slot alternatives and the action row of the best-lineage panel.

## Impact

- **Code**: `ui/corredora/CorredoraScreen.kt` (`HojaAlternativas` limit, `PanelMejorLinaje` action row, removed standalone save button).
- **Tests**: new `AlternativasTest` — returns every valid candidate (checked against `puedeIrEn`), keeps the total/direct ordering, and the full computation stays under 250 ms per slot.
- **Risks**: the sheet can now list ~130 rows (lazy, so scrolling is not affected); the two half-width buttons could ellipsize in a very long translation, which `TextAutoSize` prevents by shrinking the label instead.
