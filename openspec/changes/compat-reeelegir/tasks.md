## 1. Domain (Kotlin + web)

- [x] 1.1 Move `alternar`, `colocarEn`, `quitar`, `seleccionVacia`, `Colocacion` and `ColocacionResultado` from `overlay/CalculadoraGenealogia.kt` to `domain/Herencia.kt` (update `EstadoBurbuja.kt` and `CalculadoraGenealogiaTest.kt` imports)
- [x] 1.2 Add `SlotEstado`, `SlotOpcion`, `slotsPara` and `agregarEn` to `domain/Herencia.kt`
- [x] 1.3 Mirror everything in `sitio/src/lib/domain/herencia.ts` with the same names

## 2. State

- [x] 2.1 `AppViewModel`: `slotDestino` flow, `marcarDestino`, `slotsPara`, `agregar`, `toggle` honouring the destination and consuming it
- [x] 2.2 `store.svelte.ts`: same mirror

## 3. Compatibility screen

- [x] 3.1 Android: empty chips mark the destination (highlight + hint) and the slot picker bottom sheet shows the four slot states
- [x] 3.2 Android: `manejarToggle` / `elegirSugerencia` open the picker for a placed character; suggestions filtered by destination
- [x] 3.3 Web: empty chips become buttons with `aria-pressed`, `.destino` style, picker with `BottomSheet`
- [x] 3.4 Web: picker handling in `manejarToggle` / `elegirSugerencia` and suggestions filtered by destination

## 4. Strings

- [x] 4.1 `elegir_slot_titulo`, `slot_aqui`, `slot_bloqueado`, `slot_agregar` in the 9 `strings.xml` (`node scripts/verificar-strings.mjs`)
- [x] 4.2 `sitio/src/lib/i18n/locales/*.json` updated with the same keys (plus `burbuja_destino` / `burbuja_elegir_lugar`, already in the app's strings.xml)

## 5. Tests

- [x] 5.1 `HerenciaSlotsTest`: the `slotsPara` matrix, `agregarEn` rejects occupied slots, `alternar` moves to the destination
- [x] 5.2 `herencia.test.ts`: the same cases on the web side
- [x] 5.3 `store.test.ts`: destination consumed after placing, adding a second copy, placed character not removed by a plain toggle

## 6. Verification

- [x] 6.1 `./gradlew :app:testDebugUnitTest` + `:app:assembleDebug` — BUILD SUCCESSFUL en CI (Android #37015782894)
- [x] 6.2 `npm run sitio:test` (97 pass), `npm run sitio:check` (only the pre-existing `aporteDirecto.test.ts` errors), `npm run sitio:lint`
- [x] 6.3 `node scripts/verificar-strings.mjs`
- [ ] 6.4 Manual pass: pick a child, pick it again and add it as a grandparent (7/7 counter and the "corredora" row worth 0); mark a destination and move a character there