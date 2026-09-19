## 1. Dependency and transition infrastructure

- [ ] 1.1 Add `implementation("androidx.compose.animation:animation:1.11.4")` to `app/build.gradle.kts` next to the other pinned Compose artifacts — verify `./gradlew :app:assembleDebug` succeeds
- [ ] 1.2 Create `app/src/main/java/com/maximillionsnyder/umafinidad/ui/componentes/Transiciones.kt` with the key object (`overlay-grupos`, `overlay-ranking`, `overlay-ranking-padres`, `…-titulo`, `…-icono`, `elenco-panel`, `elenco-contador`), nullable `LocalSharedTransitionScope`/`LocalAnimatedVisibilityScope`, and `Modifier.compartido(...)` that no-ops when a scope is missing — verify it compiles with `./gradlew :app:assembleDebug` and that previews/tests without scopes are unaffected
- [x] 1.3 Confirm whether `@OptIn(ExperimentalSharedTransitionApi::class)` is required by Compose 1.11.4 and centralize it in `Transiciones.kt` (remove it if the marker no longer exists) — confirmed: shared transitions went stable in Compose Animation 1.10, so no opt-in is used anywhere; the build is validated by the release workflow

## 2. Overlay host in MainActivity

- [ ] 2.1 In `MainActivity.kt`, derive `destinoActivo` (`null` / `"grupos"` / `"ranking"` / `"ranking-padres"`) from the existing booleans and replace the `if/else` at `:307-503` with `SharedTransitionLayout { AnimatedContent(destinoActivo) { … } }`, providing both scopes via `CompositionLocalProvider` and keeping `aplicarDestino`/`irA`/`volver`/`historial` untouched — verify `./gradlew :app:assembleDebug` succeeds and manual swipe between the 5 tabs still works without clipping or measurement changes
- [ ] 2.2 Verify the fallback path: open Grupos from the floating-bubble shortcut while Ajustes is not visible, confirm the screen appears with a plain content transition and back returns to the previous tab — record the result in the change notes
- [ ] 2.3 Verify reduced motion and interruption: with system animations off, open/close an overlay (instant, no partial elements); press back while a transition is running (no stuck/duplicated overlay) — record both results in the change notes

## 3. Ajustes source cards

- [ ] 3.1 In `SettingsScreen.kt`, apply `sharedBounds` (container key), `sharedBounds` (title key) and `sharedElement` (icon key) to the Grupos (`:338`), Ranking (`:369`) and Ranking padres (`:400`) cards using `Modifier.compartido(...)` — verify the three cards render identically when no transition is running (idle visual parity) and `./gradlew :app:assembleDebug` succeeds

## 4. Overlay destinations

- [ ] 4.1 Extend `HeaderBarConVolver` in `ui/componentes/Componentes.kt` with an optional leading icon and optional shared modifiers for title/icon, keeping the current height, insets and default appearance for existing callers (`ElencoScreen`, `GroupsScreen`, `RankingScreen`) — verify existing screens render unchanged and the build succeeds
- [ ] 4.2 In `GroupsScreen.kt`, wrap the root in a full-bleed `Surface(color = surfaceContainerLow)` with the `overlay-grupos` container key and wire the header to the `overlay-grupos-titulo`/`overlay-grupos-icono` keys — verify tapping the Grupos card morphs card → screen and the icon/title land in the header, and back plays the reverse
- [ ] 4.3 In `RankingScreen.kt`, add a `claveOverlay` parameter and apply the same container/header sharing for both `ranking` and `ranking-padres` destinations; pass the distinct keys from `MainActivity.kt` — verify each Ranking entry point morphs from its own card and that switching the internal versátil/padres mode still works
- [ ] 4.4 QA pass for all three overlays: open, close, open a different overlay, back during/after transition, and confirm no duplicate shared keys jump between cards — record the outcome in the change notes

## 5. Mis Umas tabs

- [ ] 5.1 In `ElencoScreen.kt`, replace the `when(tab)` swap (`:117-120`) with `AnimatedContent(targetState = tab)` (fade; header and TabRow live inside each state so the shared pill can travel within one `AnimatedContent`) and override `LocalAnimatedVisibilityScope` with its scope — verify switching tabs animates and both tabs keep their state
- [ ] 5.2 Wrap the content in a `sharedBounds("elenco-panel")` container that is full-bleed/transparent on the edit tab and a 12dp-margin, 16dp-corner `surfaceContainerLow` surface on the lineages tab — verify the panel morphs on each switch and the grid/list remain scrollable
- [ ] 5.3 Move the roster counter pill into the shared motion: keep it in `HeaderBar` on the edit tab, render it at the top of the lineages content with the same `sharedBounds("elenco-contador")` key, and pass `pillTexto = null` to `HeaderBar` on the lineages tab — verify the pill travels, resizes, and is never rendered twice
- [ ] 5.4 Verify roster edits (mark/clear) and the computed lineage list survive tab switches, and that the pill count updates while on the lineages tab

## 6. Tests and accessibility

- [ ] 6.1 Add an instrumented test (new or extending `app/src/androidTest/.../AjustesSemanticaTest.kt`) that opens each overlay from Ajustes and asserts the destination heading and back action semantics, then closes it and asserts the Ajustes card is focusable again — verify with `./gradlew :app:assembleDebugAndroidTest` (and `:app:connectedDebugAndroidTest` when an emulator is available)
- [ ] 6.2 Run the existing suites: `./gradlew :app:testDebugUnitTest :app:assembleDebugAndroidTest` — verify all pass with no changes to domain tests
- [ ] 6.3 Manual accessibility pass with TalkBack and animations enabled/disabled: heading navigation in Grupos/Ranking, single focus targets in Mis Umas after switching tabs, and text at 200% font scale — record findings in the change notes

## 7. Validation

- [ ] 7.1 Run `openspec validate transiciones-compartidas --strict` and fix any spec-format issues until it passes
- [ ] 7.2 Review the final diff against the spec scenarios (`specs/screen-transitions/spec.md`) and confirm each scenario is either covered by a test or recorded in the change notes
