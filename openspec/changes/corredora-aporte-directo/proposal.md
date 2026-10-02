## Why

The best-lineage list of "Mi corredora" only shows the role label and the character name. To know how much each character weighs in the total you have to open the slot alternatives sheet one by one (the only place where "direct points" are shown today).

## What Changes

- **Shared direct contribution**: the per-candidate direct computation inside `alternativasParaSlot` becomes a reusable helper (`aporteDirectoDeCandidato`), plus `aportesDirectos(seleccion)` returning the seven values. `alternativasParaSlot` now calls the helper, so its ordering and numbers do not change.
- **Per-row value**: every genealogy row (child, parents, grandparents) shows `Npt` next to the name, with the same rank colours as the alternatives sheet (◎ ≥20, ○ ≥10, △ ≥4). The fixed child row also shows its value; a grandparent that is the trainee shows 0 (game rule).
- Both platforms (Android and web) get the same behaviour, same metric.
- No new strings: the `Npt` format already exists in the alternatives sheet.

Semantics: a character's direct contribution is the sum of the bonds it participates in — child: pairs with both parents plus the four branch trios; parent: pair with the child, pair with the other parent and its own branch trios; grandparent: its trio. Rows are not meant to add up to the total (each bond counts in every participant and the parents' pair counts twice).

## Capabilities

### New Capabilities

- `runner-screen`: the direct contribution shown on each row of the best-lineage list.

## Impact

- **Code**: `domain/AffinityModel.kt`, `sitio/src/lib/domain/affinity.ts`, `ui/corredora/CorredoraScreen.kt`, `sitio/src/lib/screens/DetalleCorredora.svelte`, `sitio/src/lib/screens/ChipRol.svelte`, `sitio/src/lib/theme/base.css`.
- **Tests**: new `AporteDirectoTest.kt` and `aporteDirecto.test.ts` (mirrored): manual sum per slot, child identity, trainee-as-grandparent 0, empty slots and consistency with the alternatives' direct points.
- **Risks**: none on the domain (pure extraction verified against the alternatives), UI-only change otherwise.
