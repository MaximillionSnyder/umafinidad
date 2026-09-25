## Why

The "find the best options to complete the lineage" shortcut only existed inside the floating bubble panel. On the Compatibility screen — the app's main screen, where the lineage is visible with the 5/7 counter — the user had to fill the seven slots by hand one by one.

## What Changes

- **Autofill in the Compatibility header**: the same compact button as the bubble panel (same `ic_autocompletar` icon and label) sits next to the 5/7 counter, and runs the same `completarSeleccion` computation off the main thread, filling only the empty slots and respecting what is already chosen.
- **Feedback**: while computing, the button is replaced by a small progress spinner (the header keeps its height); if the child is missing or the selection is already complete, a snackbar explains it instead of doing nothing silently.
- **Single source of truth**: the "can it be autofilled?" rule (`has child` + `at least one empty slot`) moves to the domain as `sePuedeCompletar`, used by both the bubble panel and the Compatibility screen.
- Free, like the bubble shortcut. No new strings (reuses `burbuja_autocompletar`, `elegi_hijo_empezar` and `seleccion_completa`).

## Capabilities

### New Capabilities

- `compatibility-screen`: the Compatibility screen header actions — the autofill shortcut and its feedback.

## Impact

- **Code**: `domain/Herencia.kt` (`sePuedeCompletar`), `overlay/EstadoBurbuja.kt` (uses the helper), `ui/AppViewModel.kt` (`autocompletando` + `autocompletar()` returning `AutocompletarResultado`), `ui/compat/CompatScreen.kt` (header chip + snackbars), `MainActivity.kt` (wiring).
- **Tests**: `CompletarSeleccionTest` gains the `sePuedeCompletar` cases (no child, empty, full, partial).
- **Risks**: none on the free tier; the computation already existed in the panel and the button is disabled while it runs, so it cannot be triggered twice.
