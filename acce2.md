# Informe de implementación de accesibilidad — Uma Afinidad

- **Fecha**: 2026-09-11
- **Base**: análisis de `acce1.md` (estado del arte + auditoría) verificado
  contra el código real en el commit `14dba60`.
- **Objetivo**: convertir los hallazgos del acce1 en un plan de implementación
  concreto, con cambios por archivo, strings nuevos, tests y ajustes de CI.

---

## 1. Verificación de la auditoría (acce1 §10)

Se confirmó cada hallazgo contra el código:

| # acce1 | Verificación | Nota |
|---|---|---|
| 1 (P0) Selección no anunciada | ✓ Confirmado | `CardTarjeta` y `CardFila` usan `Card.clickable` plano; el "✓" es un `Text` decorativo sin semántica |
| 2 (P0) Doble foco en ajustes | ✓ Confirmado | `FilaInterruptor`: `Card.clickable` + `Switch(onCheckedChange)` activo = 2 focos; `OpcionGrilla`: `Card.clickable` + `RadioButton(onClick)` activo = 2 focos |
| 3 (P0) Secciones sin estado | ✓ Confirmado | `SeccionDesplegable` y tarjetas de `GroupsScreen` usan `clickable`; el glifo `"∧"/"∨"` es un `Text` literal |
| 4 (P1) Sin headings | ✓ Confirmado | grep de `heading()` en `src/main`: 0 resultados |
| 5 (P1) Sin live regions | ✓ Confirmado | grep de `liveRegion`: 0 resultados; el FAB aparece con `AnimatedVisibility` sin anuncio |
| 6 (P1) `clickable` sin role/label | ✓ Confirmado | `FilaTop.kt:45`, `SlotChip`, tarjetas de Ajustes |
| 7 (P1) Sin tests instrumentados | ✓ Confirmado | No existe `app/src/androidTest`; CI corre solo `testDebugUnitTest` |
| 8 (P1) Recorte con texto extremo | ✓ Confirmado | `maxLines = 1` + `Ellipsis` en nombres (`CompatScreen.kt:409-411, 475-477`) |
| 9 (P2) Contraste | Pendiente de medición | Requiere auditoría con Scanner; no accionable solo con código |
| 10 (P2) Foco en diálogos | Bajo riesgo | `AlertDialog`/`ModalBottomSheet` de Material 3 gestionan foco básico; revisar en QA manual |

**Dato clave adicional**: grep de `semantics|Role|toggleable|selectable` en
`src/main` = **0 resultados**. Toda la semántica actual proviene de los
componentes Material; los `Card.clickable` crudos quedan como "clic" sin rol ni
estado. Esto valida el enfoque del plan: la mayor parte del trabajo es
reemplazar `clickable` por `toggleable`/`selectable` y añadir semántica
explícita.

---

## 2. Plan de implementación

Organizado en 4 fases. Cada fase es independiente y commiteable.

### Fase 1 — P0: semántica de estado (corregir antes del próximo release)

#### 1.1 Selección de personajes (`CompatScreen.kt`)

**Cambio**: reemplazar `clickable` por `toggleable` con rol `Checkbox` en
`CardTarjeta` y `CardFila`. El estado ya existe (`seleccionado = roles.isNotEmpty()`),
solo falta exponerlo.

```kotlin
// CardTarjeta (~línea 382) y CardFila (~línea 460)
Card(
    modifier = Modifier.toggleable(
        value = seleccionado,
        role = Role.Checkbox,
        onValueChange = { onClick() },
    ),
    ...
)
```

**Extra recomendado**: el anuncio queda "nombre, marcado". Para que diga
*también* en qué slot está, añadir `stateDescription` con los roles ya
localizados:

```kotlin
modifier = Modifier
    .toggleable(value = seleccionado, role = Role.Checkbox, onValueChange = { onClick() })
    .semantics {
        if (roles.isNotEmpty()) {
            stateDescription = roles.joinToString(", ") { stringResource(it) }
        }
    }
```

Notas de implementación:
- `stringResource` no se puede usar dentro de `semantics {}`; resolver los
  strings antes (en el scope del composable) con `val rolesTxt = roles.map { stringResource(it) }`.
- No usar `mergeDescendants` manual: `toggleable` ya hace merge y agrupa
  avatar, nombre y check en un solo nodo.

**Strings nuevos** (3 archivos): ninguno obligatorio si se reutilizan los
`posicionesRes` existentes (`hijo`, `padre`, `madre`, `abuelo`, `abuela`).

#### 1.2 Unificar foco en Ajustes (`SettingsScreen.kt`)

**`FilaInterruptor`** (líneas 393-412): mover el click al `Row` con
`toggleable` y dejar el `Switch` en solo-visual:

```kotlin
Card(shape = ..., colors = ...) {
    Row(
        modifier = Modifier
            .toggleable(value = activado, role = Role.Switch, onValueChange = onCambio)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        ...
    ) {
        Column(modifier = Modifier.weight(1f)) { /* títulos */ }
        Switch(checked = activado, onCheckedChange = null)
    }
}
```

**`OpcionGrilla`** (líneas 415-430): análogo con radio:

```kotlin
Row(
    modifier = Modifier
        .selectable(selected = seleccionado, role = Role.RadioButton, onClick = onClick)
        .padding(horizontal = 6.dp, vertical = 4.dp),
    ...
) {
    RadioButton(selected = seleccionado, onClick = null)
    ...
}
```

**Detalle importante**: `FilaInterruptor` recibe `onCambio: (Boolean) -> Unit`;
con `toggleable` el `onValueChange` entrega el nuevo valor, así que el caller
debe aceptarlo tal cual (`onCambio(nuevo)`). Verificar los 2 call-sites que hoy
invierten el valor (`onCambio(!activado)`); con el patrón nuevo el caller pasa
la lambda directa y se elimina la inversión.

#### 1.3 Estado expandido/colapsado (`SettingsScreen.kt` + `GroupsScreen.kt`)

**`SeccionDesplegable`** (líneas 433-466): reemplazar el `Row.clickable`:

```kotlin
val expandidoTxt = stringResource(R.string.expandido)
val contraidoTxt = stringResource(R.string.contraido)
Row(
    modifier = Modifier
        .fillMaxWidth()
        .toggleable(value = abierto, role = Role.Button, onValueChange = { onToggle() })
        .semantics { stateDescription = if (abierto) expandidoTxt else contraidoTxt }
        .padding(horizontal = 12.dp, vertical = 12.dp),
    ...
)
```

Y reemplazar el glifo `Text("∧"/"∨")` por un `Icon` con
`contentDescription = null` (el estado ya lo dice `stateDescription`; el glifo
literal "∧" es ruido para TalkBack). Mantener el glifo visual si se quiere.

**`GroupsScreen.kt`** (~línea 102): mismo patrón sobre la `Card.clickable` que
expande cada grupo; el estado es `abierto = grupoAbierto == grupo.tipo`.

**Strings nuevos** (en `values/`, `values-es/`, `values-ja/`):
- `expandido` → "Expanded" / "Expandido" / "展開済み"
- `contraido` → "Collapsed" / "Contraído" / "折りたたみ済み"

---

### Fase 2 — P1: navegación y anuncios

#### 2.1 Headings

Añadir `Modifier.semantics { heading() }` a los títulos de sección:

| Archivo | Elementos |
|---|---|
| `SettingsScreen.kt` | Títulos de cada `SeccionDesplegable` y encabezados de grupos de ajustes |
| `GroupsScreen.kt` | Encabezado de pantalla y tipo de grupo |
| `RankingScreen.kt` | Título y encabezados de secciones |
| `TopLinajesScreen.kt` | Título |
| `CompatScreen.kt` (`ResultadoPanel`) | `sec_hijo_padres`, `sec_hijo_padres_abuelos` y el "Total de herencia" |

Implementación mínima invasiva: crear un helper en `ui/componentes`:

```kotlin
fun Modifier.headingSemantica(): Modifier = semantics { heading() }
```

y aplicarlo a los `Text` de título existentes (no requiere strings nuevos).

#### 2.2 Live regions

- **Total del resultado** (`ResultadoPanel`, `RankPillGrande`): añadir al
  contenedor del total:
  ```kotlin
  modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
  ```
- **FAB "Ver afinidad"** (`CompatScreen.kt:267-278`): `ExtendedFloatingActionButton`
  ya anuncia texto+rol al recibir foco; el gap es que su *aparición* no se
  anuncia. Solución ligera: `liveRegion = Polite` en el FAB. Alternativa más
  limpia si genera anuncios repetidos: dejarlo sin live region y confiar en el
  foco (el usuario navega linealmente y lo encuentra). **Decisión sugerida**:
  probar con Polite en QA; si TalkBack lo anuncia en cada recomposición, quitarlo.
- **No usar `announceForAccessibility`**: deprecado en Android 16 (acce1 §2.2).
  Hoy el código no lo usa — mantenerlo así.

#### 2.3 Roles y labels en `clickable` restantes

Patrón: `Modifier.clickable(role = Role.Button, onClickLabel = "...") { }`

| Ubicación | `onClickLabel` sugerido (string nuevo) |
|---|---|
| `FilaTop.kt:45` (tarjeta de linaje) | `ver_herencia` ("Ver herencia") |
| `SlotChip` (`CompatScreen.kt:315-318`) | `quitar_personaje` ("Quitar personaje") |
| Tarjetas de navegación de `SettingsScreen.kt` (Grupos, Ranking) | `abrir_grupos` / `abrir_ranking` |
| `CardTarjeta`/`CardFila` ya no aplican (Fase 1 los convierte a `toggleable`) | — |

Strings nuevos (3 idiomas): `ver_herencia`, `quitar_personaje`, `abrir_grupos`,
`abrir_ranking`. Verificar en `strings.xml` si alguno ya existe (ej.
`ver_afinidad` existe; no reutilizarlo si la acción difiere).

#### 2.4 Texto extremo (200 % × 1.3)

Cambios puntuales en `CardTarjeta`/`CardFila`:
- Bajar `maxLines = 1` a `maxLines = 2` en nombre principal y secundario, o
- Reemplazar `Ellipsis` por información alternativa: ya existe el patrón
  `roles` chips; para el nombre, `2 líneas` es suficiente.

Verificación: emulador con `fontScale` 2.0 + escala de app "Muy grande", revisar
Compat (grilla y lista), Top, Elenco. Documentar recortes restantes si los hay.

---

### Fase 3 — P1: tests de accesibilidad instrumentados

#### 3.1 Dependencias (`app/build.gradle.kts`)

```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4-accessibility:1.11.4")
androidTestImplementation("androidx.compose.ui:ui-test-junit4:<alineado con BOM/compose>")
androidTestImplementation("androidx.test:runner:1.6.2")
```

Requiere `testInstrumentationRunner` (ya default con AGP) y API 34+ para correr.

#### 3.2 Tests mínimos (`app/src/androidTest/java/...`)

1. `AccesibilidadSemanticaTest`: renderizar `CardTarjeta` y afirmar
   `toggleableState`/role tras `tryPerformAccessibilityChecks()`.
2. `AjustesSemanticaTest`: `FilaInterruptor` → 1 solo nodo enfocable con rol
   Switch; `SeccionDesplegable` → `stateDescription` cambia al togglear.
3. `FlujoResultadoTest`: seleccionar hijo+padres → FAB visible → abrir sheet →
   checks sin errores en el panel de resultado.

Nota: los tests instrumentados no pueden usar `AppViewModel` con assets reales
si el setup es pesado; para los tests 1-2 basta componer los composables con
datos de fixture (los mismos JSON ya están en `src/main/assets`, accesibles
desde androidTest vía context).

#### 3.3 CI (`.github/workflows/android.yml`)

- Nuevo job `androidtest` (solo en PRs para no duplicar costo en push):
  `reactivecircus/android-emulator-runner@v2` con API 34, corriendo
  `:app:connectedDebugAndroidTest`.
- El job `debug` existente queda igual (unit tests rápidos en cada push).

Costo: ~5-10 min por corrida de emulador. Alternativa si es demasiado: correr
el job instrumentado solo semanalmente (cron) + antes de tags.

---

### Fase 4 — P2: higiene continua

1. **Contraste**: pasar Accessibility Scanner 2.5 por las 5 tabs en los 4 temas
   (sistema/claro/oscuro/alto contraste). Corregir lo que marque en `Theme.kt`
   y fondos de rango (`fondoDeRango`, `RankPill`). Es medición, no código: hacer
   en dispositivo.
2. **Checklist de release**: incorporar la checklist del acce1 §11 al PR
   template o al proceso de tag `v*`.
3. **Nota de accesibilidad**: sección en README con las funciones de
   accesibilidad soportadas (escala de texto, negrita, alto contraste, TalkBack)
   y contacto para reportes. Alineado con EN 301 549 cap. 12.

---

## 3. Resumen de archivos a tocar

| Archivo | Fase | Cambio |
|---|---|---|
| `ui/compat/CompatScreen.kt` | 1, 2 | `toggleable` en tarjetas, `liveRegion` en total/FAB, roles en `SlotChip`, `heading()` en resultado |
| `ui/settings/SettingsScreen.kt` | 1, 2 | `toggleable`/`selectable` en filas, estado en secciones, `heading()`, labels en tarjetas |
| `ui/groups/GroupsScreen.kt` | 1, 2 | Estado expandido en tarjetas de grupo, `heading()` |
| `ui/componentes/FilaTop.kt` | 2 | `role` + `onClickLabel` |
| `ui/ranking/RankingScreen.kt`, `ui/top/TopLinajesScreen.kt` | 2 | `heading()` |
| `res/values*/strings.xml` (×3) | 1, 2 | `expandido`, `contraido`, `ver_herencia`, `quitar_personaje`, `abrir_grupos`, `abrir_ranking` |
| `app/build.gradle.kts` | 3 | dependencias androidTest |
| `app/src/androidTest/...` (nuevo) | 3 | 3 clases de test |
| `.github/workflows/android.yml` | 3 | job de emulador (PR o cron) |
| `README.md` | 4 | nota de accesibilidad |

## 4. Orden sugerido y criterio de aceptación

1. **Fase 1** (1 PR): semántica de estado. Criterio: TalkBack anuncia
   seleccionado/slot, un solo foco por fila de ajuste, "expandido/contraído"
   al recorrer secciones. Verificación manual en dispositivo + unit tests
   existentes en verde.
2. **Fase 2** (1-2 PRs): headings, live regions, labels, texto extremo.
   Criterio: navegación por headings funcional en Ajustes/Grupos/Ranking;
   total del resultado anunciado al cambiar; sin recortes a 200 % × 1.3.
3. **Fase 3** (1 PR + CI): tests instrumentados en CI. Criterio:
   `connectedDebugAndroidTest` verde en PR con las checks de ATF activas.
4. **Fase 4**: continuo (Scanner, checklist, README).

Riesgos: los cambios de `toggleable` alteran el árbol de semántica (merge de
hijos); verificar que los tests unitarios existentes (que no dependen de UI)
no se vean afectados — no deberían, son lógica pura. El mayor riesgo real es
regresión visual en `FilaInterruptor`/`OpcionGrilla` al mover padding del
`Card` al `Row`: mantener los valores idénticos.

---

## 5. Fase 5 — Ventana de accesibilidad al inicio (onboarding)

Decisión tomada con el usuario: la app mostrará una ventana en el primer inicio
que ofrece activar las funciones de accesibilidad, con **detección del sistema**
para preseleccionar opciones y **preview en vivo** de los cambios.

### 5.1 Comportamiento

- **Cuándo**: solo en el **primer inicio** (flag en prefs). Reabrible desde
  Ajustes.
- **Qué**: tamaño de texto (Normal/Grande/Muy grande), texto en negrita, tema
  de alto contraste.
- **Detección del sistema**: solo **preselecciona**, nunca aplica sin
  confirmación:
  | Señal | API | Efecto |
  |---|---|---|
  | Fuente del sistema ampliada | `LocalConfiguration.current.fontScale` | `>= 1.3` → `MUY_GRANDE`; `>= 1.15` → `GRANDE` |
  | TalkBack / exploración táctil | `AccessibilityManager.isTouchExplorationEnabled()` | Preselecciona negrita |
  | Alto contraste de texto del sistema | `Settings.Secure.HIGH_TEXT_CONTRAST_ENABLED` | Preselecciona tema `ALTO_CONTRASTE` |
- **Preview en vivo**: cada cambio se aplica al instante detrás del diálogo (el
  tema ya recompone vía `StateFlow`); "Guardar" confirma, "Omitir" revierte a
  los valores previos.

### 5.2 Cambios por archivo

1. **`data/PrefsRepository.kt`**: nuevo flag `bienvenidaAccesibilidadVista`
   (key `bienvenida_acce_vista`, default `false`). Reutiliza setters existentes.
2. **`ui/AppViewModel.kt`**: `_mostrarBienvenida: MutableStateFlow<Boolean>`
   inicializado con `!prefs.bienvenidaAccesibilidadVista`; `confirmarBienvenida()`
   (persiste flag y cierra) y `omitirBienvenida()` (restaura snapshot de
   `tema/tamanoTexto/textoNegrita` capturado al abrir, persiste flag y cierra).
3. **Nuevo `ui/componentes/BienvenidaAccesibilidad.kt`**: `AlertDialog` no
   cancelable (solo Guardar/Omitir) con selector de tamaño (3 chips), dos filas
   toggle (negrita, alto contraste) reutilizando el patrón de foco único de la
   Fase 1 (`Row.toggleable` + `Switch(onCheckedChange = null)`), y lógica de
   preselección por detección.
4. **`MainActivity.kt` (`App`)**: dibujar el diálogo si `vm.mostrarBienvenida`.
5. **`res/values*/strings.xml` (×3)**: ~8 claves nuevas (título, descripción,
   etiquetas de opciones, guardar, omitir).
6. **`ui/settings/SettingsScreen.kt`**: fila "Revisar accesibilidad" que reabre
   el diálogo.
7. **`AccesibilidadTest.kt`**: tests unitarios del flag (default `false`,
   persistencia al confirmar, restauración al omitir).

### 5.3 Criterio de aceptación

- Instalación limpia con fuente normal → diálogo con valores por defecto;
  Guardar/Omitir → no vuelve a aparecer.
- Fuente del sistema ≥ 1.3× → "Muy grande" preseleccionado.
- Cada toggle cambia la app al instante; Omitir revierte al estado previo.
- `testDebugUnitTest` en verde; TalkBack recorre el diálogo con un foco por
  control.

### 5.4 Notas y riesgos

- Implementar junto con (o después de) la Fase 1: el diálogo reutiliza el
  patrón de foco único de `FilaInterruptor`.
- No enumerar `enabledServices` (ruidoso); limitarse a las 3 señales sin
  permiso de la tabla.
- Privacidad: detección es solo lectura local; no se registra ni envía nada.
