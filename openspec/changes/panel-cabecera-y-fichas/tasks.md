## 1. Header

- [x] 1.1 Move `BotonCompacto` (clear / autofill) and `TotalCompacto` into the header row of `PanelBurbuja`, with `spacedBy(4.dp)` and an ellipsized title
- [x] 1.2 Delete the now empty actions row (the progress bar stays right under the header)

## 2. Suggestion tiles

- [x] 2.1 Add `ladoCaraFicha(anchoFicha)` (tile − 8 dp, clamped 24–96 dp) and use it for the tile avatar
- [x] 2.2 Make the "more" gradient span the real row height (`fillMaxHeight`) instead of a fixed height

## 3. Verification

- [x] 3.1 `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes (143 tests)
- [ ] 3.2 Manual pass on a device: header shows both buttons plus the affinity pill without crowding; suggestion faces visibly fill their tiles
