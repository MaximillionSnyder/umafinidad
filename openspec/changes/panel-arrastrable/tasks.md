## 1. Geometry

- [x] 1.1 `PosicionPanel`: add `RectanguloPanel`, `acotar`, `mover` and `enLadoDerecho`; make `redimensionar` take the current `(x, y)` and return the new rectangle (the edge opposite the handle stays anchored)
- [x] 1.2 `PosicionPanelTest`: cover the drag, the screen clamping (both extremes and a panel bigger than the screen), the side detection and the anchored edge for both sides — `./gradlew :app:testDebugUnitTest --tests "*PosicionPanelTest"` passes

## 2. Persistence and entitlement

- [x] 2.1 `PrefsRepository`: `panelX` / `panelY` (px, `-1` = automatic) with their keys
- [x] 2.2 `ProRepository`: `FuncionPro.BURBUJA_MOVER`
- [x] 2.3 `AppViewModel.restablecerTamanos`: clear size and position

## 3. Overlay

- [x] 3.1 `PanelBurbuja`: draggable header with grip icon and `burbuja_mover` label, `panelDerecha` for the handle/carousel side, `puedeMover` / `puedeRedimensionar` flags
- [x] 3.2 `BurbujaService`: `moverPanel` / `guardarPosicionPanel`, restore the stored position clamped to the screen, recompute the panel side on open/move end/resize end, and gate move and resize with `funcionDisponible`

## 4. Pro screen, resources and docs

- [x] 4.1 `ProScreen`: fourth feature card ("move the floating panel") with `ic_mover`
- [x] 4.2 `ic_mover.xml` drawable; `burbuja_mover`, `pro_func_mover`, `pro_func_mover_desc` in the 9 locales
- [x] 4.3 README: bubble section and Pro list updated

## 5. Verification

- [x] 5.1 `node scripts/verificar-strings.mjs` passes for the 9 locales
- [x] 5.2 `./gradlew :app:testDebugUnitTest :app:assembleDebug` passes
- [ ] 5.3 Manual pass on a device/emulator: drag the panel with Pro active, reopen and confirm the position is remembered; deactivate Pro and confirm the panel returns to the automatic placement (record the result in the change notes)
