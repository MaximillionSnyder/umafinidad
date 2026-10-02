## Why

On the Compatibility screen a character could only be picked once. Tapping it
again removed it, and `slotPara` always filled the first valid hole, so there
was no way to say *which* slot it should go to — nor to place the same
character twice, even though the game rules explicitly allow it (the child can
also be a grandparent: the "corredora" worth 0, see `Herencia.kt` and
`rankingAbuelos`). The floating bubble panel already solved this with a
destination slot; the app's main screen never got it.

## What Changes

- **Destination slot**: tapping an *empty* slot chip marks it as the
  destination of the next pick (highlighted, with the "will be placed in: …"
  hint the bubble panel uses). The next character goes exactly there, even if
  it was already placed elsewhere — then it moves. The destination is consumed
  after placing. Tapping an *occupied* chip still removes it (with the existing
  confirmation for the child).
- **Re-picking a placed character**: with no destination chosen, tapping a
  character that is already in the lineage opens a picker with the seven slots —
  slots where the rules allow it can be tapped to add a *second* copy (child and
  grandparent, cross-branch reuse), the slots it already occupies can be tapped
  to remove that copy, and slots blocked by the rules or taken by another
  character are disabled with the reason. If no empty slot is valid, the tap
  simply removes it as before.
- **Single source of truth**: `slotsPara`/`agregarEn` join `puedeIrEn` in the
  domain, so the picker can never offer something the rules reject. The pure
  selection helpers (`alternar`, `colocarEn`, `quitar`) move from the bubble's
  `CalculadoraGenealogia.kt` to `domain/Herencia.kt`, where `AppViewModel.toggle`
  already duplicated them, and both platforms share the same names.
- **Search respects the destination**: with a destination marked, the suggestions
  only offer characters that can go there (same as the bubble panel).
- Four new strings, in the app's nine languages and mirrored to the site.

## Capabilities

### Modified Capabilities

- `compatibility-screen`: how a character is placed, moved, duplicated and
  removed from the lineage.

## Impact

- **Code**: `domain/Herencia.kt` (moved helpers + `slotsPara`/`agregarEn`),
  `overlay/CalculadoraGenealogia.kt` (keeps only `totalDe`), `overlay/EstadoBurbuja.kt` (imports),
  `ui/AppViewModel.kt` (`slotDestino`, `slotsPara`, `agregar`, `toggle` with destination),
  `ui/compat/CompatScreen.kt` (destination chip + slot picker), `MainActivity.kt` (wiring),
  `sitio/src/lib/domain/herencia.ts` (mirror), `sitio/src/lib/state/store.svelte.ts`,
  `sitio/src/lib/screens/CompatScreen.svelte`, `sitio/src/lib/theme/base.css`,
  `res/values*/strings.xml` and `sitio/src/lib/i18n/locales/*.json`.
- **Tests**: new `HerenciaSlotsTest` (Kotlin) and `herencia.test.ts` (Vitest);
  `CalculadoraGenealogiaTest` follows the moved imports.
- **Risks**: removing from the grid now takes two taps (the picker, or the slot
  chip, as before); a character can occupy two slots at once, so the 7/7 counter
  counts slots (unchanged) and the extra link shows as a "corredora" worth 0.