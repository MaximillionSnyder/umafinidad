## 1. Baseline and strings

- [x] 1.1 Re-run the audit greps on a clean tree and record counts: `rg -n "heading\(|liveRegion|semantics \{|Role\.|toggleable|selectable" app/src/main` must return only the occurrences this change adds (baseline today: 0) — verify output matches the plan before editing
- [x] 1.2 Add `expandido` and `contraido` strings to `app/src/main/res/values/strings.xml` (en), `values-es/strings.xml` (es), `values-ja/strings.xml` (ja) — verify all three files declare both names with no XML errors by running `./gradlew :app:processDebugResources`
- [x] 1.3 Add `quitar_personaje`, `abrir_grupos`, `abrir_ranking` strings to the same three files; confirm `ver_herencia` already exists at all three `strings.xml:58` and is reused instead of duplicated — verify `rg -n "ver_herencia" app/src/main/res` shows exactly one definition per locale

## 2. Fase 1 — P0 state semantics (release blocker)

- [x] 2.1 In `app/src/main/java/com/maximillionsnyder/umafinidad/ui/compat/CompatScreen.kt` `CardTarjeta` (`:373-429`), replace `Modifier.clickable(onClick)` with `Modifier.toggleable(value = seleccionado, role = Role.Checkbox, onValueChange = { onClick() })` and add `stateDescription` built from `roles.map { stringResource(it) }` resolved before `semantics {}` — verify by composing the screen in the androidTest from task 5.2 (or a manual TalkBack pass) that an unselected card reads "unchecked" and a selected one reads "checked, child"
- [x] 2.2 Apply the same `toggleable` + `stateDescription` change to `CardFila` (`:452-498`) — verify the merged node announces name, checked state, and occupied slots in one focus stop
- [x] 2.3 In `app/src/main/java/com/maximillionsnyder/umafinidad/ui/settings/SettingsScreen.kt` `FilaInterruptor` (`:394-412`), move the click to `Row.toggleable(value = activado, role = Role.Switch, onValueChange = onCambio)` keeping the exact `padding(horizontal = 12.dp, vertical = 8.dp)` and set `Switch(checked = activado, onCheckedChange = null)`; remove the `!activado` inversion at the single call-site (`:158`) — verify one focus target with switch role via instrumented test 5.3 and visual parity by screenshot comparison
- [x] 2.4 In `OpcionGrilla` (`:415-430`), use `Row.selectable(selected = seleccionado, role = Role.RadioButton, onClick = onClick)` keeping `padding(horizontal = 6.dp, vertical = 4.dp)` and `RadioButton(selected = seleccionado, onClick = null)` — verify one focus target with radio role and that all 9 call-sites still change the right setting
- [x] 2.5 In `SeccionDesplegable` (`:433-481`), replace `Row.clickable` with `Row.toggleable(value = abierto, role = Role.Button, onValueChange = { onToggle() })` plus `stateDescription` from `expandido`/`contraido`, and drop the `"∧"/"∨"` `Text` from the semantics tree (decorative `Icon(contentDescription = null)` or non-merged glyph) — verify TalkBack announces the section title and expanded/collapsed on toggle
- [x] 2.6 In `app/src/main/java/com/maximillionsnyder/umafinidad/ui/groups/GroupsScreen.kt` (`:100-103`), replace `Card.clickable` with `toggleable(value = abierto, role = Role.Button, onValueChange = { grupoAbierto = if (abierto) null else grupo.tipo })` plus `stateDescription` — verify expanded/collapsed is announced and the single-open behavior is unchanged
- [ ] 2.7 Run `./gradlew :app:assembleDebug :app:testDebugUnitTest` and confirm both succeed (domain tests unaffected by semantics changes)

## 3. Fase 2 — headings, live regions, labels, extreme text

- [x] 3.1 Add `Modifier.headingSemantica(): Modifier = semantics { heading() }` in `app/src/main/java/com/maximillionsnyder/umafinidad/ui/componentes/` (small new file or `Componentes.kt`) — verify it compiles and is used by at least one screen
- [x] 3.2 Apply `headingSemantica()` to the result section titles in `ResultadoPanel` (`sec_hijo_padres`, `sec_hijo_padres_abuelos`, `sec_entre_padres`, `total_herencia`) in `CompatScreen.kt:507-592` — verify TalkBack heading navigation lists each section
- [x] 3.3 Apply `headingSemantica()` to Settings accordion titles (`SettingsScreen.kt`), Groups screen/group-type headers (`GroupsScreen.kt`), Ranking title/section headers (`RankingScreen.kt`), and Top lineages title (`TopLinajesScreen.kt`) — verify heading navigation in each screen
- [x] 3.4 Add `Modifier.semantics { liveRegion = LiveRegionMode.Polite }` to the total container in `ResultadoPanel` (`CompatScreen.kt:519-531`) — verify the total is announced politely when the selection changes while the panel is open
- [ ] 3.5 Add `liveRegion = LiveRegionMode.Polite` to the FAB container (`CompatScreen.kt:267-278`) and validate with TalkBack; if it re-announces on recomposition, remove the modifier (fallback per design) — record the outcome in the change notes
- [x] 3.6 Add role and labels to remaining `clickable` surfaces: `CardFilaTop` (`FilaTop.kt:45`) `Role.Button` + `onClickLabel = ver_herencia`; `SlotChip` (`CompatScreen.kt:314-346`) `Role.Button` + `quitar_personaje`; Settings navigation cards `abrir_grupos`/`abrir_ranking` — verify each announces role + localized action label
- [x] 3.7 Raise `maxLines` to 2 for primary and secondary names in `CardTarjeta` (`CompatScreen.kt:409-411`) and `CardFila` (`:475-477`) — verify at 200% system font scale + app "Muy grande" that no name is clipped and rows do not overlap
- [ ] 3.8 Run the manual scale pass (emulator `fontScale=2.0`, app text size "Muy grande"): Compat grid and list, Top, Elenco, Ajustes, Grupos, Ranking — document any remaining clipping and fix within the same phase if it is a regression of this change

## 4. Fase 5 — welcome window (after phase 1)

- [x] 4.1 Add `bienvenidaAccesibilidadVista: Boolean` (key `bienvenida_acce_vista`, default `false`) to `app/src/main/java/com/maximillionsnyder/umafinidad/data/PrefsRepository.kt` and extract a pure `tamanoSegunFontScale(fontScale: Float): TamanoTexto` mapping (`>= 1.3` → `MUY_GRANDE`, `>= 1.15` → `GRANDE`, else `NORMAL`) — verify with a new unit test in `app/src/test/.../AccesibilidadTest.kt` covering the three ranges
- [x] 4.2 Add `mostrarBienvenida`, `confirmarBienvenida()`, `omitirBienvenida()` (snapshot restore), and `abrirBienvenida()` to `app/src/main/java/com/maximillionsnyder/umafinidad/ui/AppViewModel.kt` — verify by instrumented test that confirm persists the flag and skip restores `tema`/`tamanoTexto`/`textoNegrita`
- [x] 4.3 Create `app/src/main/java/com/maximillionsnyder/umafinidad/ui/componentes/BienvenidaAccesibilidad.kt`: non-cancelable `AlertDialog`, 3 text-size options, bold toggle, high-contrast toggle, Save/Skip; preselect from `LocalConfiguration.current.fontScale`, `AccessibilityManager.isTouchExplorationEnabled`, `Settings.Secure.HIGH_TEXT_CONTRAST_ENABLED` without persisting — verify instrumented test 5.4 and that detection never writes prefs when the dialog is opened and left untouched
- [x] 4.4 Host the dialog in `App` (`MainActivity.kt`, alongside the existing `confirmarSalida` dialog at `:380-396`) and add a "review accessibility" row in `SettingsScreen.kt` calling `vm.abrirBienvenida()` — verify first clean launch shows it once, Save/Skip hides it on relaunch, and Settings reopens it with current values preselected
- [x] 4.5 Add the welcome strings (title, description, options, Save, Skip) to the three `strings.xml` files — verify all three locales resolve by running `./gradlew :app:processDebugResources`

## 5. Fase 3 — instrumented tests and CI

- [x] 5.1 In `app/build.gradle.kts` add `defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"` and dependencies `androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.11.4")`, `androidTestImplementation("androidx.compose.ui:ui-test-junit4-accessibility:1.11.4")`, `androidTestImplementation("androidx.test:runner:1.6.2")`, `androidTestImplementation("androidx.test.ext:junit:1.2.1")`, `debugImplementation("androidx.compose.ui:ui-test-manifest:1.11.4")` — verify `./gradlew :app:assembleDebugAndroidTest` succeeds
- [x] 5.2 Create `app/src/androidTest/java/com/maximillionsnyder/umafinidad/AccesibilidadSemanticaTest.kt` composing `CompatScreen` with fixture data and asserting `toggleableState`/role plus `tryPerformAccessibilityChecks()` on selection cards — verify the test fails before task 2.1/2.2 and passes after
- [x] 5.3 In the same source set create `AjustesSemanticaTest.kt` composing `SettingsScreen`: exactly one focus node with `Role.Switch` per toggle row and `Role.RadioButton` per option, and `stateDescription` changes when toggling an accordion section — verify `./gradlew :app:connectedDebugAndroidTest` (or local instrumented run) passes on API 34
- [x] 5.4 Create `BienvenidaTest.kt` covering: first launch shows dialog, Save persists and hides, Skip restores prior values, reopen from Settings preselects current values, each control has one focus target — verify all cases pass on API 34
- [x] 5.5 Add an `androidtest` job to `.github/workflows/android.yml` gated to `pull_request`, with KVM enable step, `reactivecircus/android-emulator-runner@v2` (API 34, x86_64) and `./gradlew :app:connectedDebugAndroidTest` — verify the job passes on a test PR and the existing `debug` job is unchanged
- [ ] 5.6 Run `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest` locally and confirm all succeed

## 6. Fase 4 — ongoing hygiene

- [ ] 6.1 Run Accessibility Scanner over the 5 tabs in the 4 themes (system, light, dark, high contrast) and record findings; apply only contrast fixes that are actionable in `Theme.kt`/`fondoDeRango`/`RankPill` — verify each fix with a re-scan
- [x] 6.2 Add the accessibility release checklist from `acce1.md` §11 to the release process (README or PR template) — verify the checklist is reachable from the repo
- [x] 6.3 Add an accessibility note to `README.md` (supported features: text scale, bold, high contrast, TalkBack; contact for reports) — verify the section renders and mentions EN 301 549 chapter 12 alignment

## 7. Final verification

- [ ] 7.1 Run `./gradlew :app:assembleDebug :app:testDebugUnitTest` and confirm green
- [x] 7.2 Run `openspec validate mejoras-accesibilidad --strict` and confirm no errors
- [ ] 7.3 Manual TalkBack acceptance pass on a real device: selection announces name + checked + slot; every settings row is one focus stop; accordion and groups announce expanded/collapsed; result total announces on change; welcome window is fully navigable with one focus per control
