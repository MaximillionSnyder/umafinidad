# Plan — Opción A: Pestaña Ranking “Umas más versátiles” (total afinidad)

> Lista minimal ordenada por **versatilidad total** = suma de `puntajePar` con todas las demás. Es la métrica que pone a `Agnes Digital` arriba. **Pestaña puramente informativa: sin búsqueda, sin click, sin navegación cruzada — solo el listado.**

## 1. Contexto y objetivo

* Estado actual: `AffinityModel` `domain/AffinityModel.kt:41` calcula `puntajePar` `domain/AffinityModel.kt:70` y `matrizPares` `domain/AffinityModel.kt:135`, pero no hay ranking global por personaje. Navegación 5 tabs `MainActivity.kt:97` (`Compat=0`, `Groups=1`, `Top=2`, `Corredora=3`, `Ajustes=4`), ViewModel único `ui/AppViewModel.kt:59`, datos `data/AffinityRepository.kt:8` lazy. Verificado: `m = 132` umas jugables/activas.
* Objetivo Opción A: 6ª pestaña `Ranking` que liste **todas** las umas `playable==true && active==true` ordenadas `desc total`, con `Avatar` `ui/componentes/Componentes.kt:41` + nombre `displayName(japones)` `domain/Models.kt:16` + `RankPill` total. Sin drill-down expandible, sin sub-tabs, sin interacción (v1 minimal pura). Verificado: Agnes Digital (1019) queda #1 con `total=3057`, `mejorPareja=Hokko Tarumae (38 pts)`.

Definición formal Opción A:
```
charsTop = personajes.filter { playable && active } // domain/AffinityModel.kt:131-132 — verificado m=132
total[id] = sum_{otro != id} puntajePar(id, otro)  // usa matrizPares parEn()
orden = total desc -> charId asc                   // estable (6 empates exactos verificados -> tie-break real)
```

## 2. Alcance v1 (qué entra / no entra)

**Entra:** dominio ranking, UI lista pura sin controles, navegación 6 tabs, strings es/en/ja, icono.
**No entra v1:** búsqueda difusa, expandible top 5 parejas, sub-filtro “Especialistas”, comparación 2 umas, click/navegación cruzada, ranking como hija `mejorLinajeDe` `domain/AffinityModel.kt:231` — queda para v2.

## 3. Diseño detallado

### 3.1 Dominio — `domain/AffinityModel.kt:41`
Añadir al final de la clase antes de `}` `domain/AffinityModel.kt:339` (clase 340 líneas; `}` en 339):
```kotlin
// Data class mínima — solo lo que la UI muestra (sin promedio/mejorPareja/numGrupos)
data class RankingAfinidad(
  val personaje: Character,
  val total: Int,
)

// Umbrales por percentil sobre los totales (value-based, tolerante a empates):
// umbralGreat = totalesDesc[ceil(0.10·m)-1]  // top 10% → ~2748: 14 umas ◎
// umbralGood  = totalesDesc[ceil(0.50·m)-1]  // mediana → ~2377: hasta 66 ○
private val cacheRanking: List<RankingAfinidad> by lazy { calcularRankingAfinidad() }
private var umbralRankingGreat: Int = 0
private var umbralRankingGood: Int = 0

fun rankingAfinidad(): List<RankingAfinidad> = cacheRanking
fun rangoRanking(total: Int): Rango = when {
    total >= umbralRankingGreat -> Rango("◎", "rank-great")
    total >= umbralRankingGood  -> Rango("○", "rank-good")
    else                        -> Rango("△", "rank-fair")
}
private fun calcularRankingAfinidad(): List<RankingAfinidad> // O(m²) reuse matrizPares/parEn
```
Reusa `charsTop` `domain/AffinityModel.kt:131-132`, `idsTop` `:133`, `matrizPares` `:135`, `parEn()` `:144`, `tiposPorChar` `domain/AffinityModel.kt:46`. No tocar `K_TOP=300` `:7`. Verificado: con `m=132`, el ranking reusa la matriz ya cacheada.

### 3.2 UI — nuevo `ui/ranking/RankingScreen.kt`
Patrón espejo de `ui/top/TopLinajesScreen.kt:47` y `ui/groups/GroupsScreen.kt:46`, pero **sin interacción**:
* Estado `var ranking by remember { mutableStateOf<List<RankingAfinidad>?>(null) }` + `LaunchedEffect(modelo) { withContext(Dispatchers.Default){ ranking = modelo.rankingAfinidad() } }`
* Loading: `CircularProgressIndicator` `ui/top/TopLinajesScreen.kt:57-59` (Box centrado)
* Lista: `LazyColumn` `contentPadding 12.dp` `verticalArrangement 8.dp` como `ui/top/TopLinajesScreen.kt:73`. Cada item `Card` `surfaceContainerLow` (`MaterialTheme.colorScheme.surfaceContainerLow`, derivado del `darkColorScheme` `ui/theme/Theme.kt:64`) con `RoundedCornerShape(16.dp)` **sin `clickable`**:
  ```
  Row(10.dp): [Medalla/pos] [Avatar 36dp] [nombre Bold weight(1f)] [RankPill(rangoRanking(total), total)]
  ```
  Ejemplo Agnes: `#1 [Avatar] Agnes Digital  ◎ 3057`. Sin sub-texto (v1 puro: “solo el número”). Medalla top 3 opcional igual que `TopLinajesScreen`.
* Sin `OutlinedTextField`, sin `coincideDifuso`, sin `compareByDescending` (orden fijo del dominio).
* `japones = LocalConfiguration.current.locales[0].language=="ja"` se pasa desde `MainActivity.kt:84` como en otras tabs.

No `ViewModel` nuevo — usa `modelo` ya expuesto `ui/AppViewModel.kt:65`.

### 3.3 Navegación — `MainActivity.kt:67` (`@Composable App` en `:67`, `var tab` real en `:75`)
* Imports: `import ...ui.ranking.RankingScreen`
* `var tab` `rememberSaveable mutableIntStateOf(0)` sigue 0..5 (6 items, único que crece es Ajustes).
* `NavigationBar` `MainActivity.kt:97` añadir último item antes de Ajustes:
  ```
  0 ic_tab_compat Compat
  1 ic_tab_groups Groups
  2 ic_tab_top Top
  3 ic_tab_corredora Corredora
  4 ic_tab_ranking Ranking   // nuevo — ante-último para mínima renumeración
  5 ic_tab_ajustes Ajustes   // 4→5
  ```
  Usar nuevo vector `res/drawable/ic_tab_ranking.xml`.
* `Crossfade` `MainActivity.kt:140` `when(t) { 0->Compat, 1->Groups, 2->Top, 3->Corredora, 4->RankingScreen(modelo,japones), 5->Settings }`
* `LaunchedEffect(arbolPendiente)` `:87` **sin cambio** (Corredora sigue en 3).

### 3.4 Recursos
* `res/values/strings.xml:4` + `values-es/strings.xml:1` + `values-ja/strings.xml`:
  ```
  tab_ranking Ranking / Ranking / ランキング
  ```
  (solo 1 string nuevo — sin ranking_titulo/subtitulo/promedio que requerirían header/búsqueda).
  Reusa para vacíos: `R.string.sin_datos` existente.
* `res/drawable/ic_tab_ranking.xml` nuevo vector 24dp (trofeo/podio — variante distinguible de `ic_tab_top.xml:1`).

### 3.5 Performance y cache
`m = 132` verificado → `m²~17k` ints (matriz triangular), <5ms tras matriz caliente. Primera llamada paga `matrizPares` lazy (~8.7k `puntajePar` × sets ~60 tipos) — cubierta por `Dispatchers.Default`. `cacheRanking` `by lazy` igual que `cacheTop` `domain/AffinityModel.kt:128`. Totales reales: máx 3057 (Agnes Digital), p10 2748, mediana 2377, mín 1079 (Haru Urara).

## 4. Cambios de archivos — checklist

| Archivo | Acción | Detalle línea |
|---------|--------|---------------|
| `domain/AffinityModel.kt:41` | EDIT | Añadir `RankingAfinidad` + `rankingAfinidad()` + `rangoRanking()` antes de `}` |
| `ui/ranking/RankingScreen.kt` | CREATE | Nuevo ~90 líneas (lista pura) |
| `MainActivity.kt:97` | EDIT | 6 items, Ranking en pos 4, Ajustes 4→5 |
| `MainActivity.kt:140` | EDIT | `when` nuevo case 4→Ranking, 5→Settings |
| `res/drawable/ic_tab_ranking.xml` | CREATE | vector podio/ranking |
| `res/values/strings.xml:4` | EDIT | +1 string `tab_ranking` |
| `res/values-es/strings.xml:1` | EDIT | idem ES |
| `res/values-ja/strings.xml` | EDIT | idem JA |
| `app/src/test/.../ParidadTest.kt:67` | EDIT ✓ | test `rankingAfinidadEsConsistente` (data-independiente) |

No tocar `data/AffinityRepository.kt:8`, `data/Dtos.kt:1`, `domain/Herencia.kt:1`, `build.gradle.kts:8`.

## 5. Orden de implementación

1. Dominio `AffinityModel` + unit test + `./gradlew :app:testDebugUnitTest` verificar `Agnes Digital (1019)` #1 total 3057, Gentildonna 2890, Haru Urara 1079.
2. Crear `ic_tab_ranking.xml`
3. Crear `RankingScreen.kt` (sin navegación aún, preview con `modelo` fake)
4. Strings 3 idiomas
5. Cablear `MainActivity` navegación
6. `./gradlew :app:testDebugUnitTest` + `./gradlew :app:assembleDebug`
7. Test manual: `japones` flag, scroll 132 items, rotación, tap no hace nada, colores top/bottom (◎/○/△) correctos.

## 6. Verificación

> **Estado de implementación (v1 completa):** todos los archivos del checklist editados/creados. Verificación local **estática** (este dispositivo Termux no tiene Java/Gradle): XMLs parseados ✓, balance de llaves/paréntesis ✓, firmas de `RankPill`/`Avatar`/`stringResource` cotejadas contra el código existente ✓, sin referencias residuales a índices de tab ✓. **`testDebugUnitTest` + `assembleDebug` pendientes — los corre GitHub Actions en el próximo push a `main`.**

* **Automática:** ranking es funcionalidad **nueva** (no existe en la PWA), sin fixture de paridad. Invariantes **data-independientes** (no se rompen con `npm run fetch`): `assertEquals(ids.size, ranking.size)`, orden descendente con `zipWithNext`, empates por `charId` asc, y spot-check de que `total == sum puntajePar` contra el resto (3 primeras + 3 últimas). Valores actuales verificados por script: Agnes Digital (1019) #1 con 3057, Gentildonna 2890, Haru Urara 1079.
* **Manual:** en emulador filtrar no existe (lista pura); comprobar que cada fila muestra solo nombre + pastilla número, que tap no navega, y que 6 tabs se ven cómodos en 360dp.

## 7. Riesgos

* 6 tabs apretado en 360dp → labels truncados. Mitiga: `tab_ranking` corto (“Ranking”), ubica Ranking en pos 4 (antes de Ajustes) minimiza movimiento visual; `NavigationBarItem` maneja overflow.
* Color `RankPill` por percentil: umbrales se recalibran con `npm run fetch` — colores pueden migrar entre releases (aceptado, documentado). *(Referencia: `rangoTotal >=151` `domain/AffinityModel.kt:86` es para herencia y no aplica aquí.)*
* Cache inmutable ok porque `AffinityModel` es inmutable tras carga `data/AffinityRepository.kt:12`.

## 8. Estimación v1

Dominio 0.4d + UI 0.3d + navegación/strings 0.2d + tests 0.2d = ~1.2d.
