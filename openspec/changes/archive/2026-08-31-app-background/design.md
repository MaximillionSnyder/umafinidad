## Context

See `proposal.md` — Why for motivation. Current state verified on disk:

- Theme tokens at `app/src/main/java/com/maximillionsnyder/umafinidad/ui/theme/Theme.kt:24-60` define dark (`FondoArriba #1B1E24`, `FondoAbajo #14161A`, `CardFondo #2C2824`) and light (`FondoArribaClaro #FFF8F0`, `FondoAbajoClaro #F5EFE6`, `CardFondoClaro #FFF3E8`) palettes. `BrushFondo`/`BrushFondoClaro` at `:150-151` and `Modifier.fondoGradiente(isDark)` at `:153-154` provide the gradient. `EsquemaOscuro`/`EsquemaClaro` at `:96-128` feed `MaterialTheme`.
- Window chrome at `app/src/main/res/values/colors.xml:3` (`window_background #14161A`) and `app/src/main/res/values/themes.xml:4-6` is static dark, set before Compose theming. `UmaAfinidadTheme` `SideEffect` at `Theme.kt:168-174` sets `window.statusBarColor = esquema.background.toArgb()` and `isAppearanceLightStatusBars = !esOscuro`, but `navigationBarColor` is not handled and `windowBackground` still flashes dark.
- Root layer at `app/src/main/java/com/maximillionsnyder/umafinidad/MainActivity.kt:65-120`: `enableEdgeToEdge()` + `Box(Modifier.fondoGradiente(esOscuro))` wrapping `Scaffold(containerColor = Color.Transparent)` and `HorizontalPager`. Cards in `ui/ranking/RankingScreen.kt`, `ui/groups/GroupsScreen.kt`, `ui/top/TopLinajesScreen.kt`, `ui/compat/CompatScreen.kt` mix `cardFondo()` and `surfaceContainerLow`.

Constraint: `AffinityModel` (`domain/AffinityModel.kt:41`) and data layer are untouched; this is pure UI theming. Must work on API 24+ with gesture and 3-button nav, in both `SISTEMA` and explicit `CLARO`/`OSCURO`.

## Goals / Non-Goals

**Goals:**
- Single Compose-owned gradient source of truth that follows `ThemeMode` and `isSystemInDarkTheme()`.
- Window chrome (windowBackground, statusBar, navigationBar) theme-correct from first frame, no dark flash in light theme.
- Transparent Scaffold/root so gradient is visible; cards/bottom bar remain legible.

**Non-Goals:**
- New artwork, image backgrounds, per-screen gradients, or wallpaper picker.
- Changes to `AffinityModel`, `AffinityRepository`, `AppViewModel` logic beyond theme exposure (already done).
- Dynamic color (Material You) or per-Uma theming.

## Decisions

### Decision 1: Keep Compose gradient as source of truth; make XML windowBackground transparent or theme-synced

**Choice:** Keep `BrushFondo`/`BrushFondoClaro` and `Modifier.fondoGradiente(esOscuro)` at `Theme.kt:150-154` + `MainActivity.kt:120` as the only background. Make `themes.xml:windowBackground` `@android:color/transparent` (or remove) and drive `statusBarColor`/`navigationBarColor` to `Color.Transparent` in `UmaAfinidadTheme` `SideEffect`, letting the gradient show through. Fallback: if transparent nav bar is unsupported (API < 29 with 3-button), use `esquema.background` / `esquema.surface`.

**Rationale:** Eliminates dual source of truth (XML vs Compose). Edge-to-edge requires transparent bars; `enableEdgeToEdge()` already called. Verified `MainActivity.kt:65` exists.

**Alternatives considered:**
- `values-night/colors.xml` with separate `window_background` per night mode → rejected: does not handle in-app `ThemeMode.CLARO` override when system is dark, and adds XML duplication.
- Drawable XML gradient as `windowBackground` → rejected: not theme-reactive at runtime without recreation; Compose recomposition is needed for live `SISTEMA` switches.

### Decision 2: Handle chrome in `UmaAfinidadTheme` SideEffect, not per-screen

**Choice:** Centralize in `Theme.kt:168-174` `SideEffect`:
```kotlin
window.statusBarColor = Color.Transparent.toArgb()
window.navigationBarColor = Color.Transparent.toArgb()
WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !esOscuro
WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !esOscuro
```
For API < 29, set `navigationBarColor = esquema.background.toArgb()` and `isAppearanceLightNavigationBars` only if `Build.VERSION.SDK_INT >= 26`.

**Rationale:** Single place, reacts to `esOscuro` recomposition. Matches spec "Window chrome matches active theme".

**Alternative:** Set chrome in `MainActivity.onCreate` → rejected: would not respond to in-app theme change without recreation.

### Decision 3: Audit cards to use `cardFondo()` / `surfaceContainer` only

**Choice:** Standardize:
- Ranking cards: `cardFondo()` with top-3 `primaryContainer` tint (already at `RankingScreen.kt`).
- Groups/Top/Compat cards: `MaterialTheme.colorScheme.surfaceContainerLow` or `cardFondo()` — pick one per screen and document. Floating `NavigationBar` at `MainActivity.kt:169-174` stays `surfaceContainer` with `shadow` and `RoundedCornerShape(28.dp)`.

**Rationale:** Guarantees contrast specs; `TextoPrincipal`/`TextoSecundario` tokens already meet contrast on those surfaces.

**Alternative:** Introduce new `CardFondo` variants per screen → rejected: unnecessary proliferation.

### Decision 4: No new dependencies

**Choice:** Use `androidx.core:core-ktx` already present (`WindowCompat`), no `accompanist-systemuicontroller`.

**Rationale:** Minimize risk; `enableEdgeToEdge()` is AndroidX core.

## Risks / Trade-offs

- **Transparent nav bar on legacy 3-button devices (API 24-28)** → icons may sit on gradient with low contrast. Mitigation: fallback to `esquema.background` solid color when `!WindowCompat.getInsetsController` supports light nav bar, and ensure gradient bottom (`FondoAbajo` / `FondoAbajoClaro`) is dark/light enough for icon contrast.
- **Splash screen (if added) shows wrong background** → Mitigation: if `core-splashscreen` is used later, configure its `windowSplashScreenBackground` per theme, not hard-coded dark.
- **Overscroll glow tint** → default glow uses `primary`; may clash on light gradient. Mitigation: accept M3 defaults; no custom `LocalOverscrollConfiguration` v1.
- **Performance** → `Brush.verticalGradient` recomputed per recomposition is cheap; ensure `BrushFondo` vals are `remember`ed or top-level vals (they are), not recreated per frame.

## Migration Plan

1. Land behind no flag — visual only, no data migration.
2. Steps: edit `Theme.kt` `SideEffect` + `themes.xml`/`colors.xml`; run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest` (unit tests unaffected).
3. Manual verification checklist (emulator + physical):
   - Cold start in `CLARO`, `OSCURO`, `SISTEMA` (both system modes) — no flash.
   - Toggle in Settings → More → Appearance (in `ui/settings/SettingsScreen.kt`) — instant gradient + chrome update.
   - Scroll each pager page, open `verRanking`/`verGrupos`/`verElenco` full-screen from `MainActivity.kt:93-95` — gradient visible behind.
   - Gesture nav and 3-button nav both legible.
4. Rollback: revert 2 files (`Theme.kt`, `MainActivity.kt`) + restore `colors.xml`/`themes.xml`; no DB to revert.

## Open Questions

- None that block spec or tasks. Deferred: should we add `values-night/colors.xml` as belt-and-suspenders for the windowBackground fallback on pre-Compose launch? Answer does not change spec; can be decided during implementation and documented in PR.
