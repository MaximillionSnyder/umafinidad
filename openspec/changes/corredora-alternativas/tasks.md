## 1. Alternatives

- [x] 1.1 `HojaAlternativas`: drop the 20-item cap (`limite = Int.MAX_VALUE`) with a comment explaining the cost is unchanged
- [x] 1.2 `AlternativasTest`: all valid candidates for slots 1..6, ordering invariant and a timing budget

## 2. Action row

- [x] 2.1 Move the save button into `PanelMejorLinaje` beside "Ver herencia" (two half-width buttons, `TextAutoSize` 9–14 sp)
- [x] 2.2 Keep the Pro lock hint under the row and remove the old standalone button

## 3. Verification

- [x] 3.1 `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes (152 tests)
- [ ] 3.2 Manual pass on a device: open a slot and confirm the sheet lists many more candidates (including characters outside the previous top 20), and that both buttons fit on one line
