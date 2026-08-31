## Why

The app renders a vertical gradient (`BrushFondo` / `BrushFondoClaro` in `app/src/main/java/com/maximillionsnyder/umafinidad/ui/theme/Theme.kt:150-154`) via `Modifier.fondoGradiente` at `MainActivity.kt:120`, but window chrome (`window_background` in `app/src/main/res/values/colors.xml:3` and `themes.xml:4-6`) is hard-coded to dark `#14161A`. In light theme (`ThemeMode.CLARO`) this causes a dark flash on cold start, status/navigation bars that do not match `MaterialTheme.colorScheme.background`, and two competing sources of truth (XML windowBackground vs Compose gradient). Users switching theme in `SettingsScreen` see inconsistent chrome and cards (`CardFondo` vs `surfaceContainer`) lack a documented contrast contract.

## What Changes

- Unify app background to a single Compose-owned gradient that follows `ThemeMode` (`SISTEMA`/`CLARO`/`OSCURO`) and `isSystemInDarkTheme()`, extending edge-to-edge under status and navigation bars.
- Make window chrome (windowBackground, statusBarColor, navigationBarColor) theme-aware and transparent where the gradient is drawn, eliminating the dark flash in light theme.
- Keep `Scaffold(containerColor = Color.Transparent)` and root `Box(Modifier.fondoGradiente(esOscuro))` as the sole background layer; no opaque `Surface` covers it.
- Preserve card hierarchy: `cardFondo()` (`CardFondo #2C2824` dark / `CardFondoClaro #FFF3E8` light at `Theme.kt:59,74,139`) and `surfaceContainer` remain the only card surfaces, with verified contrast.
- No domain logic, data, or API changes; `AffinityModel`, `AffinityRepository`, and `AppViewModel` unchanged.

## Capabilities

### New Capabilities
- `app-background`: Defines how the app background, window chrome, and surface layering behave across dark/light/system themes, including gradient, edge-to-edge, and contrast requirements.

### Modified Capabilities
- None — this is a new capability (no existing `openspec/specs/`).

## Impact

- **Code**: `app/src/main/java/com/maximillionsnyder/umafinidad/ui/theme/Theme.kt` (colors, Brush definitions, `UmaAfinidadTheme` SideEffect), `app/src/main/java/com/maximillionsnyder/umafinidad/MainActivity.kt` (edge-to-edge, root Box, Scaffold), `app/src/main/res/values/colors.xml` and `themes.xml` (windowBackground), `app/src/main/res/values-night/colors.xml` (new if needed), card usages in `ui/ranking/RankingScreen.kt`, `ui/groups/GroupsScreen.kt`, `ui/top/TopLinajesScreen.kt`, `ui/compat/CompatScreen.kt`.
- **Resources**: Possible new `values-night/colors.xml` or removal of hard-coded `window_background` in favor of transparent window.
- **APIs/Dependencies**: None; uses existing `androidx.core:core-splashscreen`/`WindowCompat` + `enableEdgeToEdge()`.
- **Systems**: Visual only; no migration, no config change for `PrefsRepository`/`ThemeMode`.
- **Risks**: Transparent navigation bar on API < 29 needs fallback to `esquema.background`.
