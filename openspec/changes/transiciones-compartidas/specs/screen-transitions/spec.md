## Purpose

Defines how the app visually connects a screen change to its origin through shared element motion: the tapped surface travels into its destination, so users keep spatial context when opening full-screen references or switching Mis Umas tabs.

## ADDED Requirements

### Requirement: Overlay container transform from Ajustes

Opening Grupos, Ranking, or Ranking padres from its Ajustes card SHALL animate the tapped card's bounds into the destination full-screen container, and the card's icon and title SHALL travel to the destination header. Closing the overlay SHALL play the reverse motion back to the card.

#### Scenario: Open Grupos from its card
- **WHEN** the user taps the Grupos card in Ajustes
- **THEN** the card bounds expand into the Grupos container and the card icon/title move to the header, with no abrupt swap

#### Scenario: Close the overlay
- **WHEN** the user presses back (or the header back button) while an overlay opened from Ajustes is visible
- **THEN** the container contracts back into the originating Ajustes card and the tabs return on the Ajustes page

#### Scenario: Each overlay keeps its own motion
- **WHEN** the user opens Ranking after having opened Grupos
- **THEN** the Ranking card morphs into the Ranking container (not the Grupos container) and back returns to the Ajustes page

### Requirement: Overlay opening without a composed source

When an overlay is requested while its Ajustes card is not composed (for example from the floating bubble shortcut), the app SHALL still show the destination without errors, falling back to a non-shared content transition.

#### Scenario: Overlay requested externally
- **WHEN** the floating bubble requests the Grupos destination while the Ajustes page is not visible
- **THEN** the Grupos screen appears with a plain content transition and back navigation behaves as today

### Requirement: Mis Umas tab transition

Switching between "Editar elenco" and "Mis linajes" SHALL animate the content change and SHALL move the roster counter pill between the header (edit tab) and the lineage content (lineages tab), so the same element is never rendered twice.

#### Scenario: Switch to Mis linajes
- **WHEN** the user selects the "Mis linajes" tab
- **THEN** the counter pill leaves the header and appears at the top of the lineage content through the shared motion, and the content panel morphs from full-bleed to a rounded surface

#### Scenario: Switch back to Editar
- **WHEN** the user selects the "Editar elenco" tab
- **THEN** the pill returns to the header and the panel expands back to full-bleed, and the roster edit state is preserved

### Requirement: Reduced motion and interrupted navigation

Transitions SHALL honor the system animator duration scale: when animations are disabled the screen change SHALL complete immediately with no intermediate frames. Back navigation SHALL remain functional at any point during a transition.

#### Scenario: Animations disabled
- **WHEN** the device has animations turned off and the user opens an overlay
- **THEN** the destination is shown immediately with no partially animated elements

#### Scenario: Back during a transition
- **WHEN** the user presses back while an overlay transition is still running
- **THEN** the app navigates back to the correct previous screen without a stuck or duplicated overlay

### Requirement: Motion preserves the accessibility contract

Shared element motion SHALL NOT add, remove, or merge semantics nodes, roles, labels, or focus targets of the affected screens, and SHALL NOT change the background layering defined by the existing theme capability.

#### Scenario: Semantics after opening an overlay
- **WHEN** a screen-reader user opens Grupos from Ajustes
- **THEN** the Grupos heading, group cards, and back action expose the same semantics as before the transition and remain focusable

#### Scenario: Mis Umas tabs keep single focus targets
- **WHEN** the Mis Umas screen is explored with a screen reader after switching tabs
- **THEN** each tab and each roster/lineage item remains a single focus target with its existing role and state
