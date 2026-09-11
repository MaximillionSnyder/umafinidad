## Purpose

Defines the semantics contract for Uma Afinidad's interactive UI so screen-reader users can perceive state (selected, checked, expanded), navigate by headings, hear result changes, and reach every control with a single focus target, including at extreme font scales.

## ADDED Requirements

### Requirement: Character selection is exposed with checkbox semantics

Character cards and list rows in the compatibility tab SHALL expose selection as a checkbox state to assistive technology, and SHALL announce the occupied family slot(s) as the state description when selected. Toggling a card SHALL produce one focusable accessibility node whose activation toggles selection.

#### Scenario: Unselected character announces availability

- **WHEN** TalkBack focus lands on an unselected character card
- **THEN** it announces the character name and an unchecked checkbox state

#### Scenario: Selected character announces slot

- **WHEN** a character occupying the "child" slot is focused
- **THEN** it announces the character name, checked state, and the localized slot label (for example "child")

#### Scenario: Multiple slots are announced together

- **WHEN** the same character occupies both "father" and "mother" slots
- **THEN** the state description lists both localized slot labels

### Requirement: Settings controls expose exactly one focus target per row

Each toggle row and each radio-style option row in Settings SHALL be a single accessibility focus target with the corresponding role (switch or radio button). The visual control (switch/radio) SHALL NOT be a separate focus target.

#### Scenario: Toggle row has one target

- **WHEN** TalkBack explores the accessibility settings and reaches a toggle row
- **THEN** focus lands once, with a switch role and checked state, and activating it flips the value

#### Scenario: Radio option row has one target

- **WHEN** TalkBack explores a group of options (text size, theme, grid mode)
- **THEN** each option is focused once with a radio-button role and selected state

### Requirement: Expand/collapse state is announced

Accordion sections in Settings and expandable group cards SHALL expose their expanded/collapsed state to assistive technology, and toggling SHALL update the announced state.

#### Scenario: Collapsed section announces state

- **WHEN** TalkBack focuses a collapsed accordion section
- **THEN** it announces the section title and a "collapsed" state

#### Scenario: Expanded section announces state

- **WHEN** the user activates a collapsed section
- **THEN** it expands and the focused section announces an "expanded" state

#### Scenario: Group card announces state

- **WHEN** a group card in Groups is focused after being expanded
- **THEN** it announces the group and its expanded state

### Requirement: Section titles are headings

Screen titles and section titles in Settings, Groups, Ranking, Top lineages, and the compatibility result panel SHALL expose heading semantics so assistive technology can navigate between sections.

#### Scenario: Heading navigation reaches result sections

- **WHEN** a TalkBack user navigates by headings inside the compatibility result panel
- **THEN** each result section title is offered as a heading navigation target

### Requirement: Result total is announced as a live region

The compatibility result total SHALL be a polite live region so changes are announced without moving focus. The "view affinity" action SHALL be discoverable when it appears.

#### Scenario: Total change is announced

- **WHEN** the selection changes and the inheritance total updates while the result panel is visible
- **THEN** the new total is announced politely

#### Scenario: Action button is reachable

- **WHEN** the "view affinity" action appears after the first character is selected
- **THEN** it is present in accessibility traversal and announces its label

### Requirement: Interactive surfaces expose role and action label

Every interactive surface that is not already a switch/checkbox/radio/tab SHALL expose a button (or link) role and, when the visual content does not describe the action, a localized action label.

#### Scenario: Lineage card announces action

- **WHEN** TalkBack focuses a lineage card in Top or Elenco
- **THEN** it announces the button role and the "view lineage" action label

#### Scenario: Slot removal announces action

- **WHEN** TalkBack focuses an occupied family-slot chip
- **THEN** it announces the character, the slot label, and the "remove character" action label

#### Scenario: Settings navigation card announces destination

- **WHEN** TalkBack focuses the Groups or Ranking navigation card in Settings
- **THEN** it announces the button role and the destination action label

### Requirement: Text tolerates extreme font scale

Character names and secondary names in the compatibility grid and list SHALL remain readable without clipping at a system font scale of 200% combined with the app's largest text size (1.3×). Names MAY truncate only after at least two lines.

#### Scenario: Two-line name at maximum scale

- **WHEN** the system font scale is 200% and the app text size is "very large"
- **THEN** the character name wraps to up to two lines and is not clipped in the grid or the list

#### Scenario: No horizontal overflow of row content

- **WHEN** a list row is rendered at maximum scale with an occupied slot label
- **THEN** the row content fits without overlapping or clipping outside the card
