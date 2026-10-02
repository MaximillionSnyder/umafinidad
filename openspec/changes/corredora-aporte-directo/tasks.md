## 1. Domain

- [x] 1.1 Kotlin `AffinityModel`: extract `aporteDirectoDeCandidato` (child/parent/grandparent, trainee-as-grandparent 0) from `alternativasParaSlot` and add `aportesDirectos`
- [x] 1.2 TypeScript `affinity.ts`: same extraction and methods, `alternativasParaSlot` reuses them

## 2. UI

- [x] 2.1 Android `ChipRol`: `puntos` parameter rendering `Npt` with `claseDePuntos`/`colorClase`; `PanelMejorLinaje` passes each slot's contribution (child included)
- [x] 2.2 Web `ChipRol.svelte` + `DetalleCorredora.svelte`: same rendering and `.chip-rol-puntos` style
- [x] 2.3 Points included in the web `aria-label`

## 3. Tests

- [x] 3.1 Android `AporteDirectoTest`: manual sum per slot, child = total − parents pair, trainee-as-grandparent 0, empty slots, consistency with alternatives
- [x] 3.2 Web `aporteDirecto.test.ts`: mirrored cases

## 4. Verification

- [ ] 4.1 `./gradlew :app:testDebugUnitTest` (pending: no JDK in this environment) and `node scripts/verificar-strings.mjs` (passed)
- [x] 4.2 `npm test` (97 tests; the rankingPadres timeout only passes with `--testTimeout=30000` on this slow machine), `npm run check` and `npm run lint` in `sitio/` (passed)
- [ ] 4.3 Manual pass: the seven rows show numbers and the child equals total − parents' pair
