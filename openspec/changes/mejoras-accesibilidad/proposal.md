## Why

The accessibility audit (`acce1.md`, verified against `14dba60` in `acce2.md`) found P0/P1 gaps: selection and expansion state are invisible to TalkBack (`CardTarjeta`/`CardFila` use raw `clickable`, `FilaInterruptor`/`OpcionGrilla` expose two focus targets per row, `SeccionDesplegable` and group cards don't announce expanded/collapsed), there are no headings or live regions (grep `heading()`/`liveRegion` = 0 in `src/main`), interactive `clickable` surfaces carry no role or action label, names truncate at extreme font scale, and there are no instrumented accessibility tests. Additionally, first-run setup offers no accessibility configuration, so low-vision and TalkBack users must discover Settings unaided. This blocks a release that respects platform accessibility expectations (and the app's own accessibility section in Settings).

## What Changes

- **P0 state semantics**: replace `clickable` with `toggleable(value, role = Role.Checkbox)` on `CardTarjeta`/`CardFila` (selection, with `stateDescription` listing occupied slots) and with `toggleable`/`selectable` on `FilaInterruptor`/`OpcionGrilla` (single focus per settings row, `Switch`/`RadioButton` visual-only); expose expanded/collapsed via `stateDescription` on `SeccionDesplegable` and group cards.
- **P1 navigation/announcements**: add `heading()` semantics to section titles (Settings, Groups, Ranking, Top, `ResultadoPanel`), `liveRegion = Polite` on the result total and FAB, and `role` + `onClickLabel` on remaining raw `clickable` surfaces (`CardFilaTop`, `SlotChip`, Settings navigation cards).
- **P1 extreme text**: raise `maxLines` from 1 to 2 on character names in `CardTarjeta`/`CardFila` and verify 200 % × 1.3 font scale without clipping.
- **P1 instrumented tests + CI**: add `app/src/androidTest` with Compose accessibility checks (`tryPerformAccessibilityChecks`) for selection, settings rows, and the result flow; add an emulator job to `.github/workflows/android.yml` (PR-only).
- **First-run accessibility welcome**: non-cancelable dialog on first launch that preselects options from system signals (font scale, touch exploration, high text contrast) and previews changes live; Save confirms, Skip restores the snapshot; reopenable from Settings. Persisted via a new `PrefsRepository` flag.
- **Hygiene**: document contrast scanning, release checklist, and an accessibility note in `README.md`.
- No domain/data model changes; `AffinityModel`, `AffinityRepository` and persisted `seleccion`/`arbol` formats stay untouched.

## Capabilities

### New Capabilities
- `accessibility`: Semantics contract for interactive UI — state exposure (selected/checked/expanded), roles and action labels, headings, live-region announcements, single focus target per control, and text-scale resilience.
- `accessibility-onboarding`: First-run accessibility welcome window — system-signal preselect, live preview, Save/Skip semantics with snapshot revert, persistence, and re-entry from Settings.

### Modified Capabilities
- None — `app-background` is unaffected (no visual/theme behavior changes beyond what it already defines).

## Impact

- **Code**: `ui/compat/CompatScreen.kt` (cards, slots, result panel, FAB), `ui/settings/SettingsScreen.kt` (rows, accordion, navigation cards), `ui/groups/GroupsScreen.kt` (group cards), `ui/componentes/FilaTop.kt` (lineage cards), `ui/componentes/` (new `heading()` helper and welcome dialog), `ui/ranking/RankingScreen.kt`, `ui/top/TopLinajesScreen.kt` (headings), `ui/AppViewModel.kt` (welcome flow), `MainActivity.kt` (host dialog), `data/PrefsRepository.kt` (flag).
- **Resources**: `res/values*/strings.xml` ×3 (`expandido`, `contraido`, `quitar_personaje`, `abrir_grupos`, `abrir_ranking`, welcome keys; `ver_herencia` already exists).
- **Dependencies/CI**: `androidx.compose.ui:ui-test-junit4-accessibility` + `ui-test-junit4` (Compose 1.11.4) and `androidx.test:runner` (androidTest); new emulator job in `.github/workflows/android.yml`; `testInstrumentationRunner` in `app/build.gradle.kts`.
- **Systems**: No data migration; new SharedPreferences boolean key (`bienvenida_acce_vista`, default `false`). System-signal detection is read-only and local.
- **Risks**: moving padding from `Card` to `Row` could regress visuals in `FilaInterruptor`/`OpcionGrilla` (keep values identical); `toggleable` merges descendants and changes the semantics tree (existing unit tests are pure logic and should be unaffected); live-region on the FAB may repeat announcements (validate in QA, remove if noisy).
