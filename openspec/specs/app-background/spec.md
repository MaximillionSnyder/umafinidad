# app-background Specification

## Purpose
Defines the observable background, window chrome, and surface layering for Uma Afinidad across dark, light, and system themes, ensuring a consistent vertical gradient and correct contrast without visual flashes.

## Requirements

### Requirement: Gradient background follows ThemeMode

The system SHALL render a full-window vertical gradient as the sole background layer. The gradient SHALL map to `ThemeMode`: `CLARO` uses light tokens (`FondoArribaClaro #FFF8F0` → `FondoAbajoClaro #F5EFE6`), `OSCURO` uses dark tokens (`FondoArriba #1B1E24` → `FondoAbajo #14161A`), and `SISTEMA` follows `isSystemInDarkTheme()`. The gradient SHALL extend edge-to-edge under status and navigation bars.

#### Scenario: Dark theme shows dark gradient
- **WHEN** user selects Theme = Dark
- **THEN** the window shows the dark vertical gradient from `#1B1E24` top to `#14161A` bottom behind all content

#### Scenario: Light theme shows light gradient
- **WHEN** user selects Theme = Light
- **THEN** the window shows the light vertical gradient from `#FFF8F0` top to `#F5EFE6` bottom behind all content

#### Scenario: System theme follows OS
- **WHEN** Theme = System and the OS switches from light to dark
- **THEN** the background recomposes to the matching gradient without relaunch

#### Scenario: Edge-to-edge coverage
- **WHEN** app is launched on a device with gesture navigation or transparent bars
- **THEN** the gradient is visible behind status and navigation bars (no opaque bar fill covering it)

### Requirement: Window chrome matches active theme

The system SHALL ensure `windowBackground`, `statusBarColor`, and `navigationBarColor` match the active theme's `MaterialTheme.colorScheme.background` or are transparent where the gradient is drawn. There SHALL be no hard-coded dark `window_background` visible in light theme during cold start, and status/navigation icon contrast (light vs dark icons) SHALL follow `isAppearanceLightStatusBars` / `isAppearanceLightNavigationBars` for the active theme.

#### Scenario: Cold start in light theme has no dark flash
- **WHEN** the app is cold-started with Theme = Light
- **THEN** the first frame's window background is light (no dark `#14161A` flash)

#### Scenario: Status bar icons contrast is correct
- **WHEN** theme is Light
- **THEN** status bar icons are dark (light appearance); when theme is Dark, icons are light

#### Scenario: Navigation bar matches background
- **WHEN** user is in Light theme on a device with 3-button navigation
- **THEN** the navigation bar background matches the light gradient bottom or is transparent over it, not dark

### Requirement: Scaffold and root layering is transparent over gradient

The system SHALL use a root `Box` with the gradient modifier as the single background. `Scaffold` and intermediate containers SHALL be transparent (`containerColor = Transparent`) so the gradient remains visible. No intermediate opaque `Surface` SHALL obscure the gradient except intentional card surfaces.

#### Scenario: Content scrolls over gradient
- **WHEN** user scrolls a `LazyColumn` (e.g., Ranking, Groups, Compat)
- **THEN** the gradient remains fixed behind the list and is visible in overscroll and between cards

#### Scenario: No double background
- **WHEN** inspecting the view hierarchy
- **THEN** there is exactly one gradient background layer; Scaffold and root containers do not add a second opaque fill

### Requirement: Card and bottom-bar surfaces preserve contrast

Cards and the floating `NavigationBar` SHALL use theme-aware surfaces (`cardFondo()` → `CardFondo #2C2824` dark / `CardFondoClaro #FFF3E8` light, or `MaterialTheme.colorScheme.surfaceContainer` / `surfaceContainerLow`) with elevation/shadow as defined in the theme. Text on those surfaces SHALL maintain readable contrast (primary text vs surface) in both themes, and rank tints (`RankGreat #7ED07E`, `RankGood #E7C86A`, `RankFair #D98F8F`) SHALL remain distinguishable on those surfaces.

#### Scenario: Cards readable in light theme
- **WHEN** theme is Light and a card is shown (e.g., Ranking row, TopLinaje card)
- **THEN** `TextoPrincipalClaro #1B1E24` on `CardFondoClaro #FFF3E8` is legible and passes contrast

#### Scenario: Cards readable in dark theme
- **WHEN** theme is Dark and a card is shown
- **THEN** `TextoPrincipal #E8EAF0` on `CardFondo #2C2824` is legible

#### Scenario: Rank tints visible on cards
- **WHEN** a `RankPill` or rank-tinted surface is shown in either theme
- **THEN** `rank-great`/`rank-good`/`rank-fair` colors are distinguishable from the card background

### Requirement: Theme change is immediate and persistent

Changing theme in Settings (via `PrefsRepository` / `ThemeMode`) SHALL persist, apply without restart, and survive process death. `AppViewModel` SHALL expose the current theme as a `StateFlow` and `UmaAfinidadTheme` SHALL recompose with the new `ColorScheme`.

#### Scenario: Theme toggle applies instantly
- **WHEN** user changes theme in Settings from Dark to Light
- **THEN** the gradient, chrome, and surfaces update in the same session without relaunch

#### Scenario: Theme persists after restart
- **WHEN** user kills and relaunches the app
- **THEN** the previously selected theme is restored
