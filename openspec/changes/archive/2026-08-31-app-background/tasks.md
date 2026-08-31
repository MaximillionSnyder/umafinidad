## 1. Audit & Baseline

- [x] 1.1 Audit all background/surface usages (`grep -rn "fondoGradiente\|BrushFondo\|window_background\|surfaceContainer\|cardFondo"` in `app/src`) and record current screenshots in dark/light/system at `MainActivity.kt:86-120` — verify list of files collected
- [x] 1.2 Confirm `enableEdgeToEdge()` at `MainActivity.kt:65` and `UmaAfinidadTheme` `isSystemInDarkTheme()` branching at `Theme.kt:161-165` behave as described — verify by code inspection against spec "Gradient background follows ThemeMode"

## 2. Window Chrome & Gradient Unification

- [x] 2.1 Update `app/src/main/res/values/themes.xml:4-6` and `app/src/main/res/values/colors.xml:3` to make `windowBackground` transparent (or remove hard-coded `#14161A`) so Compose gradient is sole background — verify `cat themes.xml` shows transparent and `assembleDebug` still succeeds
- [x] 2.2 Modify `UmaAfinidadTheme` `SideEffect` at `app/src/main/java/com/maximillionsnyder/umafinidad/ui/theme/Theme.kt:168-174` to set `statusBarColor` and `navigationBarColor` to `Color.Transparent` and drive `isAppearanceLightStatusBars` / `isAppearanceLightNavigationBars = !esOscuro` (with API fallback for nav bar) — verify by inspecting the edited `Theme.kt` and confirming `esOscuro` controls both bars
- [x] 2.3 Ensure fallback for API < 26/29: when light nav bar unsupported, use `esquema.background.toArgb()` solid — verify conditional guard `Build.VERSION.SDK_INT` present in `Theme.kt`

## 3. Root Layering

- [x] 3.1 Verify `MainActivity.kt:120` root `Box(Modifier.fondoGradiente(esOscuro))` wraps `Scaffold(containerColor = Color.Transparent)` and `HorizontalPager` — no opaque `Surface` between gradient and content — verify by reading `MainActivity.kt:120-204`
- [x] 3.2 Verify full-screen overlays (`verGrupos`/`verRanking`/`verElenco` at `MainActivity.kt:121-161`) also sit inside the gradient Box or re-apply `fondoGradiente` — verify by code review of the `if (verGrupos)` branches

## 4. Cards & Surfaces Contrast

- [x] 4.1 Standardize card surfaces: `ui/ranking/RankingScreen.kt` uses `cardFondo()` with top-3 tint, `ui/groups/GroupsScreen.kt` and `ui/top/TopLinajesScreen.kt` use `surfaceContainerLow` or `cardFondo()` consistently — verify each file's `CardDefaults.cardColors` matches design decision 3
- [x] 4.2 Verify `ui/theme/Theme.kt:59,74,139` `CardFondo`/`CardFondoClaro`/`cardFondo()` still provide legible contrast with `TextoPrincipal`/`TextoPrincipalClaro` and rank tints `RankGreat/Good/Fair` at `:62-64` — verify by visual check of `RankPill` on `cardFondo()` in both themes

## 5. Theme Persistence & Reactivity

- [x] 5.1 Verify `PrefsRepository` / `ThemeMode` flow in `ui/AppViewModel.kt` and `ui/settings/SettingsScreen.kt` propagates theme change without recreation — verify by toggling Theme in Settings and observing immediate recomposition per spec "Theme change is immediate and persistent"

## 6. Verification

- [x] 6.1 Run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest` — verify both succeed (unit tests unaffected; visual change only) and no XML/theme errors
- [x] 6.2 Manual smoke on emulator/device: cold-start in CLARO/OSCURO/SISTEMA (both OS modes), check no dark flash; toggle Settings theme live; scroll each pager page and the full-screen Ranking/Groups/Elenco overlays; test both gesture nav and 3-button nav for icon contrast — verify all scenarios in `specs/app-background/spec.md` pass
- [x] 6.3 Run `openspec validate --change app-background --strict` — verify no errors
