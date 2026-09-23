## 1. Geometry

- [x] 1.1 `PosicionPanel.maxAncho` → `screenWidth - 2 * margin`; drop the bubble size from `maxAncho` and `redimensionar`
- [x] 1.2 Update `BurbujaService` call sites (`crearParametrosPanel`, `redimensionarPanel`)
- [x] 1.3 `PosicionPanelTest`: full-width case from both sides (anchored edge lands on the left margin) and a direct `maxAncho` check

## 2. Verification

- [x] 2.1 `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes
- [ ] 2.2 Manual pass on a device: widen the panel to the full width, confirm it stays there after closing and reopening, and that it can be shrunk back (record the result in the change notes)
