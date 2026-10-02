## Purpose

Defines how characters are placed, moved, duplicated and removed from the lineage on the Compatibility screen.

## ADDED Requirements

### Requirement: Place, move and remove characters

The Compatibility screen SHALL place a character that is not in the lineage in
the destination slot when one is marked, and in the first empty slot the rules
allow otherwise; picking a character already in the lineage SHALL follow the
destination when marked and open the picker otherwise; picking a character that
is in the lineage and has no empty slot where the rules allow it SHALL remove it
from its last occupied slot, asking for confirmation when it is the child.
Tapping an occupied slot chip SHALL remove that character.

#### Scenario: First valid hole when nothing is chosen
- **WHEN** the user picks a character with an empty lineage
- **THEN** it goes into the first empty slot the rules allow and the counter advances

#### Scenario: Removing the child asks first
- **WHEN** the lineage has other slots filled and the user removes the child
- **THEN** the screen asks whether to remove only the child or to clear everything

### Requirement: Destination slot

The Compatibility screen SHALL let the user mark an empty slot as the
destination of the next pick: tapping the empty chip of a role highlights it and
the screen states which role the next character will go to. The destination
SHALL be consumed after placing and cleared when the selection is loaded,
cleared or autofilled.

#### Scenario: Place in a chosen slot
- **WHEN** the user taps an empty slot chip and then a character
- **THEN** the character goes into that slot and the destination highlight clears

#### Scenario: Move an already placed character
- **WHEN** a destination is marked and the user picks a character that is already in the lineage
- **THEN** the character moves from its previous slot to the destination

#### Scenario: Unmark the destination
- **WHEN** the user taps the same empty chip again
- **THEN** the destination is cleared and the next pick uses the first valid hole again

### Requirement: Picking a placed character again

With no destination marked, picking a character that is already in the lineage
SHALL open a picker with the seven slots instead of removing it. The picker
SHALL offer every empty slot the game rules allow — the child may also be a
grandparent, and cross-branch reuse is allowed — SHALL mark the slots the
character already occupies as removable, and SHALL disable the slots that are
taken by another character or rejected by the rules, stating the reason.

#### Scenario: The child also as a grandparent
- **WHEN** a child is chosen and the user picks the same character again and chooses a grandparent slot
- **THEN** the character occupies both slots and the result shows that link as a "corredora" worth 0

#### Scenario: Rules still decide
- **WHEN** the character cannot legally go in a slot (a parent cannot be a grandparent of its own branch, nor repeat within it, nor the child be a parent)
- **THEN** that slot is disabled in the picker

#### Scenario: Remove one of the copies
- **WHEN** the picker shows a slot the character already occupies
- **THEN** picking it removes that copy, asking for confirmation when it is the child

#### Scenario: Nothing else to place
- **WHEN** no empty slot is valid for the character
- **THEN** picking it removes it, exactly as before

### Requirement: Search respects the destination

While a destination is marked, the search suggestions SHALL only offer
characters the rules allow in that slot.

#### Scenario: Suggestions of a chosen slot
- **WHEN** the user marks a destination and types in the search field
- **THEN** only characters that may go in that slot are suggested