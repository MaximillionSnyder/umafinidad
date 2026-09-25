## 1. Shared rule and state

- [x] 1.1 Add `sePuedeCompletar(seleccion)` to the domain and use it from `EstadoBurbuja`
- [x] 1.2 `AppViewModel`: `autocompletando` flow and `autocompletar()` (`FALTA_HIJO` / `SELECCION_COMPLETA` / `CALCULANDO`) running on `Dispatchers.Default`

## 2. Compatibility screen

- [x] 2.1 Header chip: the bubble's compact autofill button next to the 5/7 pill, replaced by a spinner while computing
- [x] 2.2 Snackbars for "pick the child first" and "selection is already full"

## 3. Verification

- [x] 3.1 `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes (153 tests)
- [ ] 3.2 Manual pass on a device: with a child chosen and empty slots, tap the button and confirm the slots fill with the best options; without a child, confirm the explanatory snackbar
