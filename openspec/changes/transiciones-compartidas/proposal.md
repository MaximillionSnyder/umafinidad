## Why

Screen changes in the app are instant swaps: opening Grupos/Ranking/Ranking padres from Ajustes replaces the tabs with a full-screen reference, and switching between "Editar elenco" and "Mis linajes" in Mis Umas swaps the content with no visual continuity. Without a visual link, users lose context about where the new screen came from. Shared element transitions (Compose `SharedTransitionLayout`) connect the tapped surface to its destination, matching platform motion expectations while staying inside the app's existing accessibility contract.

## What Changes

- **Shared transitions layer**: wrap the app content in `SharedTransitionLayout` and route the overlay destinations (Grupos, Ranking, Ranking padres) through an `AnimatedContent` keyed by the active destination instead of the current boolean `if/else` swap.
- **Ajustes → Grupos/Ranking/Ranking padres (container transform)**: the tapped Settings card's bounds morph into the destination full-screen container, and the card icon + title travel to the destination header. Opening from the floating-bubble shortcut (source card not composed) degrades to the container cross-fade with no error.
- **Mis Umas: Editar ↔ Mis linajes**: `AnimatedContent` between the two tabs; the roster counter pill travels from the header into the lineage list, and the content panel morphs from full-bleed (grid) to a rounded surface (lineages).
- **Motion accessibility**: transitions honor the system animator duration scale (animations disabled ⇒ instant change), and no shared modifier alters the semantics tree, roles, or focusability of existing controls.
- No new user-visible settings, no new strings, no domain/data/persistence changes, and no changes to the web project (`sitio/`).

## Capabilities

### New Capabilities

- `screen-transitions`: shared-element motion contract for screen changes — overlay container transform with shared icon/title, Mis Umas tab container/pill, the no-source fallback, and reduced-motion behavior.

### Modified Capabilities

- None — `app-background` and `accessibility` behavior are unchanged (the transition layer adds no new semantics and keeps the same background/gradient layering).

## Impact

- **Code**: `MainActivity.kt` (SharedTransitionLayout + AnimatedContent host, destination state), `ui/settings/SettingsScreen.kt` (shared source cards), `ui/groups/GroupsScreen.kt` and `ui/ranking/RankingScreen.kt` (shared destination container/header), `ui/elenco/ElencoScreen.kt` (tab AnimatedContent, pill, panel), `ui/componentes/Componentes.kt` (`HeaderBarConVolver` optional icon + shared hooks), new `ui/componentes/Transiciones.kt` (keys, scope locals, modifier helper).
- **Resources**: none (reuses `elenco_contador` and existing header strings in all locales).
- **Dependencies**: `androidx.compose.animation:animation:1.11.4` declared explicitly (currently transitive) in `app/build.gradle.kts`; API opt-in verified at compile time.
- **Systems**: no data migration, no new permissions, no service/overlay changes.
- **Risks**: `SharedTransitionLayout` wrapping the `HorizontalPager` must not alter measurement or swipe behavior; the overlay background becomes an opaque `surfaceContainerLow` panel during/after the morph (visible change, with a transparent-container fallback documented in design).
