## 1. Layout

- [x] 1.1 Add `FILAS_GENEALOGIA_POR_RAMA` (`[0]`, `[1,2]`, `[3,5]`, `[4,6]`)
- [x] 1.2 `PanelBurbuja`: render the grid when the option is on, keep the single-column list otherwise
- [x] 1.3 Fix `etiquetaDeSlot` so slots 4 and 6 are the second grandparent of their branch
- [x] 1.4 `SlotGenealogia(compacto = true)`: avatar 28 dp plus `basicMarquee` on name and role

## 2. Option and wiring

- [x] 2.1 `PrefsRepository.panelDosColumnas` (`panel_dos_columnas`, default off) + `AppViewModel` state and setter
- [x] 2.2 Switch in Ajustes → Burbuja flotante and `BurbujaService` passing the preference to the panel

## 3. Verification

- [x] 3.1 `GrillaGenealogiaTest` (6 tests) plus the rest of the suite green, and `:app:assembleDebug` builds
- [x] 3.2 `node scripts/verificar-strings.mjs` passes with the 2 new strings in the 9 locales
- [ ] 3.3 Manual pass on a device: enable the option, check the tree layout (child big, two per row, Parent 1's line on the left), the marquee on long names and that tapping a slot still marks it as destination
