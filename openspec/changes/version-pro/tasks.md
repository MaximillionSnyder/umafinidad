## 1. Pro domain and entitlement

- [x] 1.1 Add `LicenciaPro` (normalize / validate / format / checksum / generate) plus `FuncionPro`, `funcionDisponible`, `puedeGuardarArboles` and the `ProRepository` prefs wrapper in `app/src/main/java/com/maximillionsnyder/umafinidad/data/ProRepository.kt` — pure logic kept free of Android types so it runs in JVM tests
- [x] 1.2 Add `app/src/test/java/com/maximillionsnyder/umafinidad/ProTest.kt` with fixed vectors, normalization, tampered-signature, wrong length/prefix, alphabet and 500-generation checks — `./gradlew :app:testDebugUnitTest --tests "*ProTest"` passes
- [x] 1.3 Add `scripts/generar-codigos-pro.mjs` mirroring the Kotlin algorithm (issue N codes, `--verificar`) and pin the same vectors in `ProTest.kt` so both sides cannot drift

## 2. App state

- [x] 2.1 Expose `esPro`, `codigoPro` and `activadoEnPro` as `StateFlow`s in `AppViewModel`, with `activarPro` (returns `ACTIVADO` / `YA_ACTIVO` / `CODIGO_INVALIDO`) and `desactivarPro`
- [x] 2.2 Make `guardarArbol` return `Boolean` and refuse to persist without a license (defense in depth behind the UI gate)

## 3. Pro screen and Ajustes entry

- [x] 3.1 Create `ui/pro/ProScreen.kt`: license state card, feature list with check/lock, activation field with inline feedback, "how do I get a code?" dialog, deactivation with confirmation, shared-transition header
- [x] 3.2 Add `Destino.PRO` and `ClavesTransicion.OVERLAY_PRO`, and route the `pro` destination through `MainActivity`'s overlay stack (back returns to the previous screen)
- [x] 3.3 Add the Pro card at the top of Ajustes (with `PRO` pill when active) and make the "Best parents" card show a lock and open the Pro screen when there is no license

## 4. Gated features

- [x] 4.1 Bubble resize: `PanelBurbuja` only composes the corner handle with a license (otherwise a lock shortcut to the Pro screen); `BurbujaService` ignores the stored manual size and refuses to resize/persist without one
- [x] 4.2 Best parents: `RankingScreen` takes `esPro`/`onIrAPro`, shows a lock on the "Best parents" segment, skips `rankingPadres()` without a license and renders the unlock call to action
- [x] 4.3 Saving lineages: `CorredoraScreen` shows a lock and opens the Pro screen instead of the save dialog; the dialog itself is Pro-only
- [x] 4.4 Keep free behavior identical: versátil ranking, saved-lineage reading/deletion, bubble size options, translucent panel and every accessibility setting stay available without a license

## 5. Resources and translations

- [x] 5.1 Add `ic_pro.xml`, `ic_candado.xml`, `ic_check.xml` vector drawables
- [x] 5.2 Add the 33 `pro_*` strings to `values/` and `values-es/` (English source + Spanish), then to `values-ja`, `values-ko`, `values-id`, `values-th`, `values-vi`, `values-zh-rCN` and `values-zh-rTW`
- [x] 5.3 Add `scripts/verificar-strings.mjs` (key + placeholder parity, unescaped apostrophes) — reports 9 locales / 237 keys with identical keys and markers

## 6. Verification

- [x] 6.1 `node scripts/verificar-strings.mjs` passes for the 9 locales
- [x] 6.2 `./gradlew :app:testDebugUnitTest` passes (ProTest + the existing suite)
- [x] 6.3 `./gradlew :app:assembleDebug` builds the APK with the new resources and gated screens
- [ ] 6.4 Manual pass on a device/emulator: activate with a generated code, confirm the handle/mode/save unlock live, deactivate and confirm the free tier returns (record result in the change notes)
