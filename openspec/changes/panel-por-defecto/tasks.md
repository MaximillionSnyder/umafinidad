## 1. Default geometry

- [x] 1.1 `PosicionPanel.calcular`: anchor to the top margin (and drop the now unused screen-height parameter)
- [x] 1.2 `BurbujaUi`: `FRACCION_ANCHO_PANEL = 0.93`, `ANCHO_PANEL_MAX_DP = 420`, `FRACCION_ALTO_PANEL = 0.38`
- [x] 1.3 `BurbujaService`: updated call site
- [x] 1.4 `PosicionPanelTest`: top-anchored default instead of vertical centring

## 2. Verification

- [x] 2.1 `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes (17/17 in `PosicionPanelTest`)
- [ ] 2.2 Manual pass on a device: with a clean state ("Reset size" in Ajustes), the panel opens as the reference band (top edge, ~93 % × ~38 %), still movable and resizable
