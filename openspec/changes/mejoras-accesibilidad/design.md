## Context

See `proposal.md` — Why. Verified current state (commit `14dba60`):

- `CompatScreen.kt`: `CardTarjeta` (`:373`, modifier at `:382`) and `CardFila` (`:452`, modifier at `:460`) use raw `clickable`; `posicionesRes` (`:500`) already derives occupied slots; `SlotChip` (`:314`) uses `Card.clickable`; result total lives in `ResultadoPanel` (`:507`); FAB is `AnimatedVisibility` + `ExtendedFloatingActionButton` (`:267-278`).
- `SettingsScreen.kt`: `FilaInterruptor` (`:394`) is `Card.clickable` + active `Switch`; `OpcionGrilla` (`:415`) is `Card.clickable` + active `RadioButton`; `SeccionDesplegable` (`:433`) is `Row.clickable` + literal `"∧"/"∨"` text. Call-site audit: `FilaInterruptor` has exactly **one** caller (`:158`, bold text — `acce2.md`'s note about two call-sites is stale); `OpcionGrilla` has 9 callers, all pass a plain setter `onClick`.
- `GroupsScreen.kt`: group card `Card.clickable` (`:100-103`), expanded via local `grupoAbierto` (`:60`, `:99`).
- `FilaTop.kt`: `CardFilaTop` (`:40`, modifier at `:45`) raw `clickable`; it already displays `R.string.ver_herencia` (`:61-65`), which exists in all three locales (`values/strings.xml:58`, `values-es/strings.xml:58`, `values-ja/strings.xml:58`).
- `MainActivity.kt`: `App` (`:103`) holds pager/nav history; `SettingsScreen` is wired at `:348-371`; global dialog pattern already exists (`confirmarSalida`, `:380-396`). Theme flows are reactive: `MainActivity` collects `vm.tema`, `vm.tamanoTexto`, `vm.textoNegrita` (`:89-95`) and `UmaAfinidadTheme` applies them.
- `AppViewModel.kt`: prefs-backed `StateFlow`s at `:75-116`; `prefs` is private (`:66`); data loads in `init` (`:178`).
- `PrefsRepository.kt`: SharedPreferences `ui_prefs`, string/bool keys at `:54-62`; no welcome flag.
- `app/build.gradle.kts`: Compose `1.11.4` pinned (`:83`), no `testInstrumentationRunner`, no androidTest source set; unit tests only (`AccesibilidadTest.kt` tests `TamanoTexto` scales). `app/src/androidTest` does not exist.
- `.github/workflows/android.yml`: `debug` job runs `:app:assembleDebug :app:testDebugUnitTest` (`:40`) on push-to-main/PR; no emulator job.
- `grep` for `heading(`, `liveRegion`, `semantics{`, `Role.` in `src/main` = 0 hits before this change.

## Goals / Non-Goals

**Goals:**

- Single source of truth for state in the semantics tree: `toggleable`/`selectable` instead of `clickable` + decoration, with localized `stateDescription`.
- One accessibility focus target per settings control, without changing visual padding or the one-open accordion behavior.
- Headings, live regions, and action labels with the minimum new API surface (one helper module).
- Instrumented Compose ATF checks that run in CI on PRs.
- First-run welcome window reusing the phase 1 focus pattern, with snapshot revert.

**Non-Goals:**

- Contrast-color changes (needs on-device Accessibility Scanner; tracked as phase 4 hygiene, not spec behavior here).
- Using deprecated `announceForAccessibility` (Android 16) — live regions only.
- Domain/data model or persisted selection/árbol format changes.
- A full WCAG/EN 301 549 certification or screen-reader QA on every locale.

## Decisions

1. **`toggleable` for selection cards, not manual semantics.** `Modifier.toggleable(value, role = Role.Checkbox, onValueChange)` provides role, state, activation, and descendant merging in one modifier. Manual `semantics { }` + `clickable` was rejected: it needs explicit `mergeDescendants` and duplicates activation logic. The decorative `✓` `Text` is removed or left as non-announced content since `toggleable` already exposes checked state.
2. **Slots in `stateDescription`, resolved outside `semantics {}`.** `stringResource` is composable-only, so `CardTarjeta`/`CardFila` compute `val rolesTexto = roles.map { stringResource(it) }` first and then set `stateDescription = rolesTexto.joinToString(", ")` inside `semantics {}`. Reuses existing `rol_corto_*` strings (no new strings).
3. **Move the click to the row and null out the inner control.** `FilaInterruptor`: `Row.toggleable(value = activado, role = Role.Switch, onValueChange = onCambio)` + `Switch(checked = activado, onCheckedChange = null)`. `OpcionGrilla`: `Row.selectable(selected, role = Role.RadioButton, onClick)` + `RadioButton(selected, onClick = null)`. The `Row` keeps the exact current paddings (`12/8` and `6/4`) and the `Card` loses only its `clickable` modifier, so the visual result is unchanged. Rationale: `onCheckedChange = null` removes the duplicate focus target without disabling the control (null overload is visual-only).
4. **`FilaInterruptor` API stays `(Boolean) -> Unit`.** `toggleable`'s `onValueChange` already delivers the new value, so the single call-site passes `onCambio` directly; the `!activado` inversion is deleted. No signature change.
5. **Accordion uses `toggleable(role = Role.Button)` + `stateDescription`.** New strings `expandido`/`contraido` (×3 locales). The `"∧"/"∨"` glyph is replaced by a decorative `Icon(contentDescription = null)` (or kept as `Text` outside the semantics tree) because the state description already announces expanded/collapsed; a literal glyph read by TalkBack is noise. The accordion's "only one open" logic in `SettingsScreen` (`:76-82`) is untouched.
6. **One heading helper.** `Modifier.headingSemantica(): Modifier = semantics { heading() }` in `ui/componentes` (new small file or `Componentes.kt`), applied to existing title `Text`s. Chosen over inline `semantics {}` at each call-site to avoid repeated imports and keep grep-ability.
7. **Live regions: total yes, FAB conditional.** `ResultadoPanel` total container gets `liveRegion = LiveRegionMode.Polite`. The FAB also gets Polite in the first implementation; if QA shows TalkBack re-announces it on recomposition, the modifier is removed and focus-based discovery is accepted. This is deliberately testable in QA, not a code risk.
8. **`onClickLabel`/`role` on remaining `clickable`s.** `CardFilaTop`: `role = Role.Button`, `onClickLabel = stringResource(R.string.ver_herencia)` (existing string). `SlotChip`: `role = Role.Button` + new `quitar_personaje` label, and its state description includes the slot/role so the chip reads as "character, slot, remove". Settings navigation cards: new `abrir_grupos`/`abrir_ranking` labels.
9. **Text scale: two lines, no layout rework.** Raise `maxLines` to 2 on the primary and secondary name in `CardTarjeta`/`CardFila` (`CompatScreen.kt:409-411`, `475-477`). No other layout change; if verification at 200% × 1.3 reveals overlap, adjust chips/row arrangement locally rather than redesigning the card.
10. **Instrumented tests through public composables.** `CardTarjeta`/`CardFila`/`FilaInterruptor`/`SeccionDesplegable` are `private`, so androidTest cannot call them directly without visibility changes. Tests compose the public entry points (`CompatScreen`, `SettingsScreen`, `GroupsScreen`) with fixture data built in-test, and assert on the merged semantics tree (`toggleableState`, `Role.Switch`, `stateDescription`) plus `tryPerformAccessibilityChecks()`. This avoids widening production visibility only for tests. Fixture `AffinityModel` is loaded from `src/main/assets` via `InstrumentationRegistry` (same JSON as unit tests) or from a minimal in-memory model if asset wiring is heavy.
11. **CI emulator job PR-only.** New `androidtest` job in `.github/workflows/android.yml`, gated to `pull_request`, API 34, `reactivecircus/android-emulator-runner@v2`, `:app:connectedDebugAndroidTest`. Rationale: keeps push-to-main cheap; can move to cron/tags later without changing the test code.
12. **Welcome window: ViewModel-owned state + snapshot.** `PrefsRepository.bienvenidaAccesibilidadVista` (key `bienvenida_acce_vista`, default `false`). `AppViewModel` exposes `mostrarBienvenida: StateFlow<Boolean>`, plus `confirmarBienvenida()`, `omitirBienvenida()`, and `abrirBienvenida()` (reopen). When opening, the ViewModel captures a snapshot of `tema/tamanoTexto/textoNegrita`; `omitir` restores it and persists the flag; `confirmar` persists the flag and leaves the current values. The dialog is an `AlertDialog` with `onDismissRequest = {}` (non-cancelable) and only Save/Skip actions. Rationale: state survives configuration changes and keeps `PrefsRepository` private to the ViewModel.
13. **System detection is UI-local and read-only.** Font scale from `LocalConfiguration.current.fontScale`; touch exploration from `AccessibilityManager.isTouchExplorationEnabled`; high text contrast from `Settings.Secure.HIGH_TEXT_CONTRAST_ENABLED`. Detection only feeds initial UI state (a `remember`-initialized value), never calls the setters. `enabledServices` is intentionally not enumerated (privacy/noise). The font-scale → `TamanoTexto` mapping is extracted as a pure function so it is unit-testable without Robolectric; flag persistence and snapshot revert are covered by instrumented tests (unit tests have no Android `Context`).
14. **Upgrade behavior: existing installs see the window once.** The flag defaults false, so after updating, users who already configured accessibility will see the welcome window one time. Accepted (it is non-destructive until saved; Skip restores the snapshot). Alternative (treat "any accessibility pref set" as seen) was rejected because it makes the flag state implicit and harder to test.

## Risks / Trade-offs

- [`toggleable` merges descendants → existing UI tests or semantics-dependent behavior could change] → Existing tests are pure domain logic (`ParidadTest`, `AccesibilidadTest`); there are no UI tests today. Verify with `testDebugUnitTest` and the new ATF tests.
- [Moving padding from `Card` to `Row` in `FilaInterruptor`/`OpcionGrilla` changes visuals] → Keep the exact `padding(horizontal/vertical)` values and `Arrangement.spacedBy`; visual check in both grid modes and all themes.
- [Live region on the FAB may repeat announcements on each recomposition] → Implement Polite, verify with TalkBack; remove the modifier if noisy (spec only requires discoverability, not announcement).
- [`toggleable(role = Role.Checkbox)` on a card containing chips/avatar may drop some descendant text from the merged node] → `toggleable` merges children and keeps text; verify with TalkBack that name + state + slots are all announced in one focus stop.
- [Instrumented tests need assets/`AffinityModel` and a model build could be slow] → Use minimal fixture models first; only load real assets if a test genuinely needs parity data.
- [New AndroidX test dependency versions must match Compose `1.11.4`] → Pin `ui-test-junit4` and `ui-test-junit4-accessibility` to `1.11.4`; `androidx.test:runner:1.6.2`.
- [First-run welcome could annoy existing users after update] → Non-cancelable but one-time; Skip restores prior settings; flag persists on either choice.
- [System high-contrast detection via `Settings.Secure` may return null/be unavailable] → Treat null/exception as false; never block the dialog.

## Migration Plan

1. Phase 1 (P0 semantics) ships first as its own PR; phases 2 and 5 can follow independently (phase 5 depends on the phase 1 focus pattern but not on phase 2). Phase 3 adds tests and CI; phase 4 is ongoing hygiene.
2. No data migration: one additive SharedPreferences boolean. Rollback is a code revert; the extra key is harmless if left behind.
3. CI emulator job can be disabled by removing the job block without affecting the `debug` job.

## Open Questions

- Does the FAB live region produce repeated announcements in practice? (Resolved in QA; fallback already designed.)
- Are two text lines enough at 200% × 1.3 for the longest localized names, or do the row chips need to move below the name? (Resolved during phase 2 verification; no spec change either way.)
- Should the emulator job move from PR-only to weekly cron + tags if CI cost grows? (Operational, deferrable.)
