# Informe de accesibilidad Android — Estado del arte 2026 y auditoría de Uma Afinidad

- **Fecha de corte**: 2026-09-11
- **Alcance**: novedades de plataforma (Android 16/17), TalkBack, estándares (WCAG 2.2, EN 301 549 v4.1.1, EAA), prácticas de Jetpack Compose, testing, y auditoría aplicada a la app Uma Afinidad.
- **App analizada**: `comparacionuma/umafinidad`, commit `14dba60`, Compose 1.11.4 / Material 3 1.4.0, `minSdk 24` / `targetSdk 36`.

---

## 1. Resumen ejecutivo

1. **La accesibilidad dejó de ser opcional**: la European Accessibility Act (EAA) está vigente desde el 28-jun-2025 y **EN 301 549 v4.1.1** (publicada el 2-sep-2026) adopta WCAG 2.2 AA para web, documentos y **software nativo (cláusula 11)**. Aún no está citada en el OJEU (previsto ~30-nov-2026), por lo que v3.2.1 sigue siendo la referencia legal, pero la dirección es clara.
2. **Android 16 (API 36)** renovó las APIs de semántica (texto con contorno, multi-etiqueta, estado expandido, campo requerido, check tri-estado) y **deprecó los anuncios disruptivos** (`announceForAccessibility` / `TYPE_ANNOUNCEMENT`), empujando a las *live regions*.
3. **TalkBack 16.x** incorporó Gemini (describir imagen/pantalla, preguntas de seguimiento), navegación de tablas, mejoras de braille y de teclado físico, y ajustes de latencia.
4. **Compose** ya ofrece testing automático de accesibilidad con el mismo framework que Accessibility Scanner (`ui-test-junit4-accessibility` + `enableAccessibilityChecks()`, desde Compose 1.8.0).
5. **Uma Afinidad** ya tiene una base sólida para baja visión (escala de texto 1.15×/1.3× sobre la del sistema, negrita, tema de alto contraste, i18n es/en/ja). Los gaps principales están en la semántica fina: **selección de personajes no anunciada, doble foco en filas con Switch/RadioButton, estados de secciones desplegables no expuestos, ausencia de headings/live regions y falta de tests de accesibilidad instrumentados**.

---

## 2. Novedades de plataforma: Android 16 (API 36)

Fuente: `developer.android.com/about/versions/16/features` y `/summary`; blog de Google del 2-dic-2025.

### 2.1 APIs de semántica mejoradas
- **Outline text**: reemplaza el antiguo "high contrast text". Dibuja un contorno contrastante alrededor del texto para personas con baja sensibilidad al contraste. Hay APIs nuevas de `AccessibilityManager` para que los toolkits (por ejemplo, Compose) repliquen el efecto si pintan texto fuera de `android.text.Layout`.
- **`TtsSpan.TYPE_DURATION`**: anotación de duraciones (horas/minutos/segundos) para una lectura precisa por TalkBack.
- **Múltiples etiquetas**: `AccessibilityNodeInfo#addLabeledBy/getLabeledByList` (se depreca el par singular).
- **Estado expandido/colapsado**: `setExpandedState` + `TYPE_WINDOW_CONTENT_CHANGED` con `CONTENT_CHANGE_TYPE_EXPANDED`, para menús y listas expandibles.
- **ProgressBar indeterminada**: `RANGE_TYPE_INDETERMINATE`.
- **CheckBox tri-estado**: `getChecked/setChecked(int)` con estado "parcialmente marcado" (reemplaza el booleano deprecado).
- **`setSupplementalDescription`**: información adicional de un contenedor sin pisar las etiquetas de sus hijos.
- **`setFieldRequired`**: marca campos de formulario obligatorios.

### 2.2 Cambio de comportamiento importante (todas las apps)
Android 16 **deprecia los anuncios de accesibilidad disruptivos** (`announceForAccessibility` y eventos `TYPE_ANNOUNCEMENT`). La vía recomendada es exponer los cambios de estado con **live regions** (en Compose, `liveRegion`) para que el servicio decida cuándo y cómo anunciar.

### 2.3 Sistema
- **Dark theme expandido**: Android 16 oscurece automáticamente apps sin tema oscuro propio, útil para baja visión y fotosensibilidad.
- **Expressive Captions**: subtítulos con emoción (`[joy]`, `[sadness]`), intensidad y sonidos del entorno.
- **AutoClick** con cursor de permanencia configurable (clic por reposo, tipo de clic), para dificultades motoras.
- **Voice Access sin manos**: se lanza con Gemini ("Hey Google, start Voice Access") y suma japonés.
- **Fast Pair para audífonos LE Audio**: emparejamiento en un toque; además entrada de micrófono del teléfono y volumen ambiente para audífonos (LEA).
- **Dictado con TalkBack**: doble toque con dos dedos inicia/detiene dictado de Gboard.
- **Guided Frame** con Gemini en la cámara Pixel: descripciones más ricas para fotos.

---

## 3. Android 17 (API 37) y hoja de ruta

Fuente: `developer.android.com/about/versions/17/*`; blog "The First Beta of Android 17" (13-feb-2026); sesión de accesibilidad de Google I/O 2026.

- **Accesibilidad de tecleo complejo con IME físico**: nuevas APIs (`TextAttribute.Builder.setTextSuggestionSelected()`, `AccessibilityEvent.setTextChangeTypes()`) para que los lectores de pantalla den feedback preciso al componer texto en idiomas CJKV. Las apps con `TextView` estándar y foco en API 37 lo tienen por defecto.
- **Fin del opt-out de adaptabilidad**: en pantallas grandes (sw ≥ 600dp), targeting 37 ignora `screenOrientation`/`resizeableActivity`/`min-maxAspectRatio`. Para accesibilidad importa porque la reflow/rotación y el tamaño de texto grande exigen layouts flexibles (WCAG 1.4.10 Reflow, 1.3.4 Orientación).
- **Recreación de actividades**: cambios de configuración como teclado, navegación, touchscreen o color mode ya no recrean la actividad por defecto (`android:recreateOnConfigChanges` para opt-in).
- El modelo de previews cambió a **Canary continuo** (OTA), lo que facilita probar la app en la versión en desarrollo.
- Google I/O 2026 dedicó una sesión a "Android accessibility updates": TalkBack, Voice Access, dark theme expandido y los cambios de API de Android 17.

---

## 4. TalkBack 16.x (lector de pantalla)

Fuentes: soporte oficial de Google y `accessibleandroid.com`.

### 4.1 TalkBack 16.0 (may-2025)
- **Ask Gemini / Describe image / Describe screen**: descripción de la pantalla completa y preguntas de seguimiento por voz o teclado.
- **Navegación de tablas** por filas y columnas (con control de lectura conmutable y cambio automático opcional).
- **Braille**: lectura de mensajes del sistema (toasts/notificaciones) en la línea braille, con descarte e historial.
- **Teclado físico**: tecla modificadora configurable (Search/Windows), "Modifier+V" para enlaces visitados.
- Agrupación de símbolos repetidos conmutable.

### 4.2 TalkBack 16.1 (sep-2025)
- Gemini en más idiomas (incluido español).
- **Reduce delay**: retardo de foco y de tecleo configurables (0.15–0.30 s), "single tap to activate".
- **Gestos del teclado braille** personalizables.
- Mensaje visual para desactivar TalkBack si se activó por error (mantener dos dedos 5 s).

### 4.3 TalkBack 16.2
- **Keymap Enhanced** para teclado físico, con **Browse mode** y **Smart browse** (desactiva browse en campos editables).
- **Anuncio de formato de texto**: negrita, cursiva, subrayado, tamaño y color.
- **Split-tap** para activar elementos fuera del alcance del teclado.
- Doble toque con dos dedos para dictado con Gboard; reorganización de Ajustes.

**Implicación para apps**: TalkBack lee el **árbol de semántica**, no los píxeles. Todo lo que se anuncia mal (o no se anuncia) es un problema de semántica, no del lector.

---

## 5. Otros servicios de asistencia relevantes

| Servicio | Perfil | Qué exige de la app |
|---|---|---|
| **Switch Access** | Motor | Elementos enfocables con orden lógico; alternativa no gestual a cada acción; sin trampas de foco |
| **Voice Access** | Motor / habla | Etiquetas visibles y accesibles coherentes (`contentDescription`, texto); acciones con nombre claro |
| **Braille (HID)** | Sordoceguera | Semántica correcta y compatibilidad con teclados braille; TalkBack 16.x mejora el soporte |
| **Magnificación / display size** | Baja visión | Layouts reflow, sin clipping; targets amplios |
| **Expressive Captions** | Sordo/hipoacúsico | Subtítulos del sistema; textos legibles y sincronizados en contenido propio |

---

## 6. Estándares y legislación (2026)

### 6.1 WCAG 2.2 (W3C)
Nivel AA es el objetivo habitual. Los 6 criterios nuevos respecto a 2.1:
| Criterio | Nivel | Requisito |
|---|---|---|
| 2.4.11 Focus Not Obscured (Minimum) | AA | El elemento enfocado no puede quedar completamente tapado por headers/footers/overlays |
| 2.5.7 Dragging Movements | AA | Toda acción de arrastre necesita alternativa de un solo puntero |
| 2.5.8 Target Size (Minimum) | AA | Targets ≥ 24×24 CSS px o separación suficiente |
| 3.2.6 Consistent Help | A | Ayuda en la misma posición relativa |
| 3.3.7 Redundant Entry | A | No pedir datos ya ingresados en el mismo proceso |
| 3.3.8 Accesible Authentication (Minimum) | AA | Sin tests cognitivos como único método; permitir gestores de contraseñas/pegado |

Además, 4.1.1 Parsing quedó obsoleto. La W3C publicó `WCAG2Mobile` (nota informativa, 2025) con la interpretación de WCAG 2.2 para apps nativas.

### 6.2 EN 301 549 v4.1.1 (Europa)
- **Publicada el 2-sep-2026** (adoptada el 24-ago-2026); reemplaza a v3.2.1 (2021).
- Adopta **WCAG 2.2 AA** en cláusulas 9 (web), 10 (documentos) y **11 (software, incluye apps nativas)**.
- La **cláusula 11.5** exige interoperar con las APIs de accesibilidad de la plataforma (`AccessibilityNodeInfo` en Android / semántica en Compose); **11.7** exige respetar las preferencias del usuario (tamaño de texto, color, contraste, tamaño del puntero, filtros).
- **Aún no da presunción de conformidad**: falta la cita en el OJEU (prevista ~30-nov-2026). Hasta entonces, v3.2.1 (WCAG 2.1 AA) es la referencia legal.
- La EAA es exigible desde el 28-jun-2025 para servicios nuevos (y renovaciones significativas), con multas por incumplimiento.

### 6.3 WCAG 3.0
Sigue en **Working Draft** (última: 3-mar-2026), con un nuevo borrador aprobado en sep-2026. No es citable como estándar; faltan años.

### 6.4 Google Play
El **informe de pre-lanzamiento** de Play Console ejecuta el **Accessibility Test Framework (ATF)** y muestra oportunidades por categoría (contraste, targets, etiquetas). Es una fuente de auditoría gratuita en cada subida.

---

## 7. Guía práctica para Jetpack Compose

Base: documentación oficial de Compose (actualizada jun-2026).

### 7.1 Semántica
- `Modifier.semantics { ... }`: etiquetas, `role`, `stateDescription`, `liveRegion`, `heading()`, `error()`, `customActions`.
- `mergeDescendants = true` agrupa hijos en un solo nodo (los componentes Material ya lo hacen con `clickable`/`toggleable`).
- `clearAndSetSemantics { }` reemplaza la semántica del subárbol (usar poco: también afecta tests y autofill/AI agents).
- `hideFromAccessibility { }` (nuevo, reemplaza `invisibleToUser`): oculta decorativo manteniendo semántica para tests.
- Roles y estados: `role = Role.Button/Checkbox/Switch/Tab`, `stateDescription`, `toggleableState`, `selected`.
- Acciones alternativas: `customActions = listOf(CustomAccessibilityAction(label) { ... })` para gestos complejos.

### 7.2 Foco y traversal
- `isTraversalGroup = true` delimita grupos; `traversalIndex` ordena dentro del grupo (útil en layouts no lineales).
- `heading()` en títulos de sección permite navegación por headings con TalkBack.
- Foco de teclado/switch: `focusRequester`, `focusProperties`, orden explícito; test con teclado Bluetooth.

### 7.3 Baja visión
- Texto siempre en `sp`; Compose respeta la escala del sistema (Android 14+ permite hasta 200 % con escalado no lineal).
- Contraste: **4.5:1** texto normal, **3:1** texto grande, **3:1** componentes/íconos (WCAG 1.4.3/1.4.11).
- Targets: **48dp** mínimo (Material). `Modifier.minimumInteractiveComponentSize()` amplía el hitbox sin cambiar el visual.
- No depender solo del color (formas, texto, íconos).
- Soporte de *outline text* y de display size/magnificación.

### 7.4 Anuncios dinámicos
- `liveRegion = LiveRegionMode.Polite` para la mayoría de actualizaciones; `Assertive` solo para urgencias.
- Evitar `announceForAccessibility` (deprecado en Android 16).

### 7.5 Ejemplos
```kotlin
// Fila de ajuste con un único foco y estado
Row(
    modifier = Modifier
        .fillMaxWidth()
        .toggleable(value = activado, role = Role.Switch, onValueChange = onCambio),
) {
    Text(titulo, modifier = Modifier.weight(1f))
    Switch(checked = activado, onCheckedChange = null)
}

// Tarjeta seleccionable que anuncia selección
Card(
    modifier = Modifier.toggleable(
        value = seleccionado,
        role = Role.Checkbox,
        onValueChange = { onClick() },
    ),
) { /* contenido */ }

// Sección expandible con estado anunciado
Row(
    modifier = Modifier
        .fillMaxWidth()
        .toggleable(value = abierto, role = Role.Button, onValueChange = { onToggle() })
        .semantics {
            stateDescription = if (abierto) expandido else contraido
            onClick(label = abrirCerrar)
        },
) { /* título + flecha */ }

// Total que se anuncia al cambiar
Text(
    text = total,
    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
)

// Heading
Text(titulo, modifier = Modifier.semantics { heading() })
```

---

## 8. Necesidades por perfil

| Perfil | Claves |
|---|---|
| **Baja visión** | Escala de texto hasta 200 % sin recortes; contraste 4.5:1/3:1; no usar solo color; targets 48dp; dark theme y alto contraste; outline text; magnificación |
| **TalkBack (ciego)** | Etiquetas únicas y descriptivas; roles y estados; orden de lectura; headings; live regions; acciones alternativas a gestos; imágenes decorativas en `null` |
| **Motor** | Targets amplios y separados; alternativas a arrastre (WCAG 2.5.7); Switch Access/Voice Access; sin timeouts ajustables sin control (WCAG 2.2.1) |
| **Cognitivo** | Lenguaje claro; ayuda consistente (3.2.6); no re-pedir datos (3.3.7); errores claros; sin tests cognitivos en login (3.3.8) |
| **Auditivo** | Subtítulos y transcripciones; no depender de audio para información o alertas |

---

## 9. Testing y herramientas

### 9.1 Automatizado
- **Compose accessibility checks** (desde Compose 1.8.0):
  ```kotlin
  androidTestImplementation("androidx.compose.ui:ui-test-junit4-accessibility:1.11.4")

  @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

  @Before fun setUp() = rule.enableAccessibilityChecks()

  @Test fun pantallaSinProblemas() {
      rule.setContent { /* pantalla */ }
      rule.onRoot().tryPerformAccessibilityChecks()
  }
  ```
  - Requiere **API 34+** y dispositivo/emulador real (no Robolectric).
  - Corre el **ATF** (el mismo de Accessibility Scanner y Espresso); se puede compartir el `AccessibilityValidator` con Espresso en apps híbridas.
  - `tryPerformAccessibilityChecks()` después de `performMultiModalInput` y `requestFocus()` (no se auto-ejecutan).
- **Accessibility Scanner 2.5** (15-abr-2026): escaneo en dispositivo, ahora multi-ventana.
- **Pre-launch report de Google Play** (ATF) en cada release.
- **Lint** de Android Studio para etiquetas faltantes y patrones problemáticos.

### 9.2 Manual (imprescindible)
- TalkBack completo en dispositivo físico sobre los flujos clave (agregar hijo, seleccionar padres, ver resultado, guardar árbol).
- Switch Access y Voice Access en los mismos flujos.
- Teclado Bluetooth: foco visible, orden, sin trampas.
- Escala de texto del sistema al 200 % + escala de la app al máximo.
- Contraste con analizador (TPGi Color Contrast Analyzer) y Scanner.

---

## 10. Auditoría aplicada a Uma Afinidad

### 10.1 Fortalezas verificadas

| Área | Evidencia |
|---|---|
| Escala de texto sobre la del sistema | `Theme.kt:216-217` combina `fontScale` del sistema × `TamanoTexto.escala` (1.0/1.15/1.3) |
| Texto en negrita | `Theme.kt:215, 252-268` eleva todos los estilos a Bold mínimo |
| Tema de alto contraste | `Theme.kt:180-214`: textos a negro puro, primario oscuro, contornos reforzados |
| Etiquetas localizadas en iconos funcionales | `Componentes.kt:180` (volver), `CompatScreen.kt:204` / `CorredoraScreen.kt:211` (limpiar), `SettingsScreen.kt:375` / `CorredoraScreen.kt:633` (cancelar), `RankingScreen.kt:124` (ayuda) |
| Decorativos en `null` | Iconos de tabs y tarjetas: `MainActivity.kt:423`, `SettingsScreen.kt:252-333`, `CompatScreen.kt:251,275` |
| Componentes con semántica integrada | `NavigationBarItem` (`MainActivity.kt:250-284`), `RadioButton`, `Switch`, `FilterChip`, `Tab`, `AlertDialog`, `ModalBottomSheet`, `ExtendedFloatingActionButton` |
| i18n completo | `values`, `values-es`, `values-ja` + `res/xml/locales_config.xml` |
| Sin gestos de arrastre | Todo el flujo es tap; cumple WCAG 2.5.7 por diseño |
| Sin timeouts ni autenticación | No aplica WCAG 2.2.1/3.3.8 |

### 10.2 Gaps y riesgos (con ubicación)

| # | Prioridad | Hallazgo | Evidencia | Impacto |
|---|---|---|---|---|
| 1 | **P0** | Selección de personajes no anunciada: tarjetas con `clickable` sin `selected`/`toggleableState`; el check visual "✓" no se expone | `CompatScreen.kt:375-429` (tarjetas), `452-498` (lista) | TalkBack no informa si la Uma está seleccionada ni en qué slot |
| 2 | **P0** | Doble foco y estado ausente en ajustes: `Card.clickable` + `Switch` interno; `Card.clickable` + `RadioButton` | `SettingsScreen.kt:393-412` (`FilaInterruptor`), `415-430` (`OpcionGrilla`) | Dos paradas de foco para el mismo control; el contenedor no anuncia rol ni estado |
| 3 | **P0** | Secciones desplegables sin estado: glifos "∧/∨" sin semántica de expandido/colapsado | `SettingsScreen.kt:433-466` (`SeccionDesplegable`), `GroupsScreen.kt:100-134` | No se anuncia si está abierto/cerrado; el glifo puede leerse literalmente |
| 4 | **P1** | Sin `heading()` en títulos de sección | Ajustes, Grupos, Ranking, Resultado | Imposible navegar por headings con TalkBack |
| 5 | **P1** | Sin live regions: el FAB "Ver afinidad" aparece dinámicamente y el panel de resultado cambia totales | `CompatScreen.kt:267-278` (FAB), `505-539` (`ResultadoPanel`) | Usuarios de lector de pantalla no se enteran de que hay resultados |
| 6 | **P1** | `clickable` crudos sin `role`/`onClickLabel` en tarjetas de navegación | `SettingsScreen.kt:240-345`, `FilaTop.kt:45`, `CompatScreen.kt:315-318` (`SlotChip`) | TalkBack no dice "botón" ni el resultado de la acción |
| 7 | **P1** | Sin tests de accesibilidad instrumentados: no hay `src/androidTest`; el CI solo corre tests unitarios | `AccesibilidadTest.kt` solo verifica constantes; `.github/workflows/android.yml:40` | Las regresiones de semántica/contraste/targets no se detectan |
| 8 | **P1** | Riesgo de recorte con texto extremo: sistema 200 % × 1.3 de la app = 260 % efectivo, con muchos `maxLines = 1` + ellipsis | `Theme.kt:217`, `CompatScreen.kt:409-411`, `475-477` | Nombres truncados; verificar WCAG 1.4.4 / EN 11.1.4.4 |
| 9 | **P2** | Contraste no auditado sistemáticamente (RankPill, chips, textos secundarios en 4 temas) | `Theme.kt` colores; `CompatScreen.kt` fondo de rangos | Posibles fallos 1.4.3/1.4.11 en estados de rango |
| 10 | **P2** | `ModalBottomSheet`/diálogos: verificar retorno de foco y anuncio al abrir/cerrar | `CompatScreen.kt:281-309` | Focus management fino para TalkBack/Switch |

### 10.3 Recomendaciones priorizadas

**P0 — antes del próximo release**
1. Exponer selección en `CardTarjeta`/`CardFila`:
   ```kotlin
   Card(
       modifier = Modifier.toggleable(
           value = seleccionado,
           role = Role.Checkbox,
           onValueChange = { onClick() },
       ),
   )
   ```
2. Unificar foco en `FilaInterruptor` y `OpcionGrilla`:
   ```kotlin
   Row(
       modifier = Modifier.toggleable(value = activado, role = Role.Switch, onValueChange = onCambio),
   ) {
       /* textos */
       Switch(checked = activado, onCheckedChange = null)
   }
   ```
   (Análogo con `selectable(role = Role.RadioButton)` y el `RadioButton(onClick = null)`.)
3. Exponer estado en `SeccionDesplegable` y en las tarjetas de Grupos con `toggleable` + `stateDescription` localizado ("expandido"/"contraído") y `onClickLabel`.

**P1 — siguiente iteración**
4. `heading()` en títulos de sección de Ajustes, Grupos, Ranking y Resultado.
5. `liveRegion = LiveRegionMode.Polite` en el total del resultado y anuncio cuando aparece el FAB; evitar `announceForAccessibility` (deprecado).
6. `role = Role.Button` + `onClickLabel` en tarjetas de navegación y `SlotChip`.
7. Crear `app/src/androidTest` con `ui-test-junit4-accessibility` y `enableAccessibilityChecks()`; agregar un job/emulador API 34+ al workflow. Mínimo: tests de las 5 tabs y del flujo de resultado.
8. Probar en dispositivo con fuente del sistema al 200 % y escala de app Muy grande; ajustar `maxLines`/ellipsis y alturas fijas si recorta.

**P2 — higiene continua**
9. Pasar Accessibility Scanner 2.5 por las 5 tabs en los 4 temas y corregir contraste.
10. Checklist manual de TalkBack/Switch Access antes de cada tag `v*` (ver sección 11).
11. Publicar una nota de accesibilidad en el README/releases (EN 301 549 cap. 12 pide información accesible; refuerza confianza).

---

## 11. Checklist de release (para CI y QA manual)

- [ ] `./gradlew :app:testDebugUnitTest` en verde (existente).
- [ ] `connectedDebugAndroidTest` con `enableAccessibilityChecks()` en verde (nuevo).
- [ ] Accessibility Scanner sin errores en: Compat, Top, Corredora, Elenco, Ajustes, Grupos, Ranking, Resultado.
- [ ] TalkBack: agregar hijo → asignar 2 padres → ver resultado → guardar árbol → abrir árbol desde Ajustes.
- [ ] Switch Access: recorrer las 5 tabs y completar una selección.
- [ ] Fuente del sistema 200 % + escala "Muy grande": sin recortes de información esencial.
- [ ] Los 4 temas: contraste de texto y de componentes ≥ 4.5:1 / 3:1.
- [ ] Targets interactivos ≥ 48dp (Scanner).
- [ ] Nada depende solo del color (selección, rangos, errores).
- [ ] Foco visible con teclado Bluetooth; sin trampas; retorno de foco en diálogos/sheet.

---

## 12. Fuentes

- Android 16 features / behavior changes: https://developer.android.com/about/versions/16/features · https://developer.android.com/about/versions/16/summary
- Google blog, accesibilidad Android (2-dic-2025): https://blog.google/products-and-platforms/platforms/android/accessibility-update-expanded-dark-theme-gemini-talkback/
- Android 17 behavior changes / beta: https://developer.android.com/about/versions/17/behavior-changes-17 · https://android-developers.googleblog.com/2026/02/the-first-beta-of-android-17.html
- Google I/O 2026, sesión de accesibilidad: https://io.google/2026/explore/technical-session-5
- TalkBack 16.0 / 16.1 / 16.2 (soporte Google): https://support.google.com/accessibility/android/answer/16294093 · https://support.google.com/accessibility/android/answer/16561624 · https://support.google.com/accessibility/android/answer/16800105
- Cobertura TalkBack (Accessible Android): https://accessibleandroid.com/whats-new-in-talkback-16-0/ · https://accessibleandroid.com/whats-new-in-talkback-version-16-1/
- Compose accesibilidad / semántica / testing / traversal / merging: https://developer.android.com/develop/ui/compose/accessibility · /semantics · /testing · /traversal · /merging-clearing
- API `enableAccessibilityChecks`: https://developer.android.com/reference/kotlin/androidx/compose/ui/test/junit4/accessibility/package-summary
- Testeo de accesibilidad y pre-launch report: https://developer.android.com/guide/topics/ui/accessibility/testing
- Accessibility Scanner 2.5: https://play.google.com/store/apps/details?id=com.google.android.apps.accessibility.auditor
- WCAG 2.2 para apps móviles (W3C): https://www.w3.org/TR/wcag2mobile-22/
- EN 301 549 v4.1.1 (ETSI, sep-2026): https://www.etsi.org/deliver/etsi_en/301500_301599/301549/04.01.01_60/en_301549v040101p.pdf
- Análisis EN 301 549 v4.1.1 (Deque, 8-sep-2026): https://www.deque.com/blog/en-301-549-v4-1-1-is-final-what-changed-what-it-means-and-what-you-should-do/
- Actualización EN 301 549 (AccessibleEU, 7-sep-2026): https://accessible-eu-centre.ec.europa.eu/content-corner/news/european-accessibility-standard-en-301-549-has-been-updated-2026-09-07_en
- WCAG 3.0 Working Draft (mar-2026): https://www.w3.org/TR/wcag3/ · https://www.w3.org/WAI/standards-guidelines/wcag/wcag3-intro/
