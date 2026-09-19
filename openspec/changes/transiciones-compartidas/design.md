## Context

See `proposal.md` — Why. Current state that shapes the approach (verified on this tree):

- **Navigation is not Navigation Compose.** `MainActivity.App` (`MainActivity.kt:188`) hosts a 5-page `HorizontalPager` (`:208`, `:409-499`) plus three overlay booleans `verGrupos`/`verRanking`/`verRankingPadres` (`:233-235`). `aplicarDestino` (`:242-249`) flips those flags and the pager page; `historial` (`:239`) is the back stack and `BackHandler` (`:290-292`) pops it. Overlays are rendered by an `if/else` that replaces the whole `Scaffold` (`:307-503`).
- **Mis Umas tabs are local state.** `ElencoScreen` keeps `tab` (`ElencoScreen.kt:81`) and swaps `EditorElenco`/`LinajesElenco` with a plain `when` (`:117-120`). The roster counter pill lives in the `HeaderBar` (`:98-102`) and uses `elenco_contador`, already localized in every locale.
- **Compose 1.11.4** (April '26 stable, includes shared element tooling) with `minSdk 24`. `androidx.compose.animation:animation` arrives transitively; the shared transition APIs may still require `@OptIn(ExperimentalSharedTransitionApi::class)` in this version.
- **Accessibility contract exists.** The `mejoras-accesibilidad` change added roles, state descriptions, headings, and single focus targets, with instrumented tests running on API 34 PRs with `disable-animations: true` in CI.
- **No strings to add**: both flows reuse existing, already-localized keys.

## Goals / Non-Goals

**Goals:**

- Container transform for the three Ajustes → reference overlays with shared icon/title, driven by the existing destination state (no new navigation model).
- Shared counter pill and morphing content panel between the two Mis Umas tabs.
- Graceful fallback when the source element is not composed (bubble shortcut), and instant completion when system animations are off.
- Zero changes to domain/data/persistence, web project, or the semantics tree.

**Non-Goals:**

- Migrating to Navigation Compose/Nav3 or replacing the pager's swipe UX.
- Shared transitions for tab swipes or the "Ver herencia" list→Compat flow (deferred; different hero semantics).
- SvelteKit view transitions in `sitio/`.
- New motion on unrelated screens, custom spring/easing tuning beyond the framework defaults.

## Decisions

1. **`SharedTransitionLayout` at the app root + one `AnimatedContent` keyed by destination.** `destinoActivo` is `null` for the tabs and `"grupos" | "ranking" | "ranking-padres"` for overlays; the existing `if/else` becomes the `AnimatedContent` content. Alternatives: Navigation Compose/Nav3 (rejected: adds a dependency and rewrites the pager/back-stack that already works), hand-rolled `graphicsLayer` animation (rejected: reimplements match/overlay/clipping and cannot follow layout changes). The pager state is hoisted in `App`, so it survives the tabs state leaving composition (same as today's `if/else`).
2. **Scope propagation through CompositionLocals.** New `ui/componentes/Transiciones.kt` exposes `LocalSharedTransitionScope` and `LocalAnimatedVisibilityScope` (both nullable), provided once inside `SharedTransitionLayout`/`AnimatedContent`, plus `Modifier.compartido(key, …)` which no-ops when either scope is absent (previews, unit-rendered screens, tests). `ElencoScreen` overrides `LocalAnimatedVisibilityScope` locally with its inner `AnimatedContent` scope. Alternatives: threading two parameters through every screen (large signature churn, easy to miss a call site), context receivers (still experimental in Kotlin). Rationale: the scopes are stable objects and the repo already uses this pattern for `LocalEstiloAvatar`.
3. **Stable string keys, one per destination and shared part**: `overlay-grupos`, `overlay-ranking`, `overlay-ranking-padres` for the container; `…-titulo` for the title; `…-icono` for the icon. `RankingScreen` receives `claveOverlay` because both Ranking destinations reuse the composable. Elenco keys: `elenco-panel`, `elenco-contador`. Keys are plain strings in one object so source and destination cannot drift.
4. **Container transform = full-screen `Surface` + shared icon/title.** The Settings card uses `sharedBounds` (container), `sharedBounds` for the title text (docs prefer it for text so font/weight changes morph), and `sharedElement` for the icon. The destination wraps its root in a `Surface(color = surfaceContainerLow)` with `sharedBounds` on the same container key, and the header hosts the same title/icon keys. Shapes differ (card 12dp rounded vs full-bleed destination); with the default `ScaleToBounds` resize mode the destination's stable layout is scaled into the animated bounds and never overflows them, so no custom `OverlayClip` is needed (the planned constant 12dp clip was dropped during implementation).
5. **Elenco transition uses `AnimatedContent` + two shared pieces.** The `when(tab)` becomes `AnimatedContent(targetState = tab)` with a fade transition. Implementation finding: the header and `TabRow` must live **inside** each `AnimatedContent` state, otherwise the outgoing pill is removed in the same frame the incoming one appears and the shared match never starts. With the whole body inside, a slide would drag the header/TabRow sideways, so the spec is fade-only (updated from the planned slide+fade); the motion comes from the shared elements. The panel carries `sharedBounds("elenco-panel")` with different bounds per state (edit: full-bleed transparent; lineages: 12dp margins, 16dp corners, `surfaceContainerLow`), so the bounds genuinely change and the transform is visible. The counter pill is rendered once per state — header `Surface` in edit, content `Surface` in lineages — both with `sharedBounds("elenco-contador")` so it travels and resizes; the header on the lineages tab shows no pill to avoid a duplicate key.
6. **Reduced motion comes for free.** `AnimatedContent` and shared element bounds are `Transition`-driven and respect `MotionDurationScale`; with animations disabled every step completes in one frame. No `rememberInfiniteTransition`, no manual `Animatable`, no work in `LaunchedEffect` that could outlive the transition. `BackHandler` and the `historial` stack are untouched, so back stays correct mid-transition.
7. **No semantics impact.** `sharedElement`/`sharedBounds` add no semantics nodes; the only structural change is `AnimatedContent`, which is transient. Existing instrumented tests run with animations disabled, so they see a settled tree. A new instrumented test asserts the overlay's semantics (heading, back action) after opening from Ajustes and after closing.
8. **Dependency and opt-in.** Add explicit `implementation("androidx.compose.animation:animation:1.11.4")` (same pinned version) so the compile classpath does not rely on transitivity. Apply `@OptIn(ExperimentalSharedTransitionApi::class)` at the helper/screen level only if the compiler requires it; the first build decides, and the annotation is centralized in `Transiciones.kt` where possible.

## Risks / Trade-offs

- [`SharedTransitionLayout` + `AnimatedContent` wrapping the pager changes measurement] → both are transparent layouts; `assembleDebug` plus a manual swipe/tab check on device verifies no clipping or scroll regressions. The pager stays inside the `null` state and never animates during swipes.
- [Overlay background changes from gradient to an opaque `surfaceContainerLow` panel] → intended by the container transform; fallback if rejected in review: share only the header bounds and keep the container transparent (one-line change in the destination Surface).
- [Shape mismatch between the rounded card and the square destination] → the shared overlay is unclipped and the destination content is scaled, so the morph reads as a growing panel; the final corner difference is only visible at the screen edges and is checked in QA.
- [Putting the header inside the Elenco `AnimatedContent` duplicates it during the transition] → both copies render the same header and tab strip, so the fade is visually seamless; the exiting copy is removed when the transition ends.
- [Duplicate shared key would make an element jump to the wrong bounds] → keys live in one object and each is rendered exactly once per state; `HeaderBar` hides the pill on the lineages tab.
- [Ajustes scroll position resets when returning from an overlay] → not a regression (the `if/else` already removes the Scaffold from composition today); noted, not fixed here.
- [Compose 1.11.4 API may still be experimental] → centralized opt-in, verified by `assembleDebug`; if the marker no longer exists, the annotation is removed in the same task.
- [CI instrumented tests could catch a mid-transition tree] → CI already runs `disable-animations: true`; new assertions use `waitForIdle` semantics.

## Migration Plan

No data, schema, or preference migration. No feature flag: the fallback path (no composed source) degrades to a plain content transition, so the change is safe to ship whole. Rollback is a code revert of the transition host in `MainActivity.kt` plus the screen modifiers; nothing persisted is affected.

## Open Questions

- Exact durations/easing of the two `AnimatedContent` specs are tuned during QA (framework defaults first); they do not change the specs or task breakdown.
