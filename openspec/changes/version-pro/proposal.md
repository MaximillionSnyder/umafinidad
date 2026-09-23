## Why

Three features that already ship in the app are the ones users ask for most, but they are also the ones that cost the most to build and maintain: resizing the floating quick panel, the "best parents" ranking (the heaviest computation in the app), and saving lineage configurations. There is no way to support the project today: everything is free and offline, with no ads, no accounts and no telemetry.

A one-time-purchase Pro version keeps that model: the same offline app, with those three tools behind a local license code. Nothing phones home, nothing is removed from the free version, and the free app keeps working exactly as before.

## What Changes

- **Pro entitlement (offline)**: new `data/ProRepository.kt` with a pure `LicenciaPro` object (format `UMA-XXXX-XXXX-XXXX`, 8-char body + 4-char checksum) and a `SharedPreferences`-backed `ProRepository` (`es_pro`, `codigo`, `activado_en`). Activation is validated on the device: no server, no Play Billing, no network permission.
- **Pro screen**: new `ui/pro/ProScreen.kt` — license state, the list of Pro features, the activation field with inline feedback (activated / already active / invalid code), "how do I get a code?" dialog, and deactivation (re-activatable with the same code).
- **Ajustes entry**: a Pro card at the top of Ajustes (with a `PRO` pill when active) opens the Pro screen; the "Best parents" card shows a lock and opens the Pro screen instead of the ranking when there is no license.
- **Pro-gated features**:
  - *Bubble panel resize*: the corner handle only exists with a license; without it the panel shows a small lock shortcut that opens the Pro screen, and a manually saved panel size is ignored.
  - *Best parents*: the mode stays visible in the ranking segmented control with a lock; without a license the ranking is not even computed and the content shows the unlock call to action.
  - *Saving lineages*: the save button in Mi corredora carries a lock and opens the Pro screen; the save dialog and `AppViewModel.guardarArbol` refuse to persist without a license. Already-saved lineages remain readable and deletable for everyone.
- **License issuing**: `scripts/generar-codigos-pro.mjs` mirrors the Kotlin algorithm to issue codes (`node scripts/generar-codigos-pro.mjs 20`) and to check one (`--verificar`); `ProTest.kt` pins fixed vectors so both implementations cannot drift.
- **i18n**: 33 new strings in all 9 locales, plus `scripts/verificar-strings.mjs` to check key and placeholder parity across translations.
- **Non-goals**: no ads, no subscriptions, no server-side validation, no device-limit enforcement, no changes to the free features, no changes to the web project (`sitio/`).

## Capabilities

### New Capabilities

- `pro-version`: offline license activation and the Pro gate over bubble panel resize, best-parents ranking and lineage saving.

### Modified Capabilities

- None — `accessibility` (roles, headings, announced state) and `app-background` behavior are unchanged; the Pro gate adds locked states that keep their accessibility labels and never remove existing free functionality.

## Impact

- **Code**: new `data/ProRepository.kt`, `ui/pro/ProScreen.kt`, `app/src/test/.../ProTest.kt`; `ui/AppViewModel.kt` (Pro state + gated `guardarArbol`), `MainActivity.kt` (`pro` destination, `ProScreen`, `esPro` wiring), `ui/settings/SettingsScreen.kt`, `ui/corredora/CorredoraScreen.kt`, `ui/ranking/RankingScreen.kt`, `overlay/PanelBurbuja.kt`, `overlay/BurbujaService.kt`, `ui/Navegacion.kt` (`Destino.PRO`), `ui/componentes/Transiciones.kt` (`OVERLAY_PRO`).
- **Resources**: `ic_pro.xml`, `ic_candado.xml`, `ic_check.xml`; 33 strings × 9 locales (204 → 237 keys per locale).
- **Scripts**: `scripts/generar-codigos-pro.mjs`, `scripts/verificar-strings.mjs`.
- **Systems**: no new permissions, no service lifecycle change, no data migration (existing saved lineages stay readable).
- **Risks**: a checksum-only license is bypassable by patching the APK — accepted for a hobby offline app (documented in design); losing the code loses Pro until it is entered again (the same code always reactivates).
