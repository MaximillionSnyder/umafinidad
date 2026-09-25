# Uma Afinidad

App **nativa Android** (Kotlin + Jetpack Compose) para calcular la afinidad y
la herencia de Uma Musume con los datos datamined de GameTora. Es la versión
nativa del visor web publicado en
[MaximillionSnyder.github.io](https://github.com/MaximillionSnyder/MaximillionSnyder.github.io).

Además, el repo incluye **`sitio/`**, la versión web nueva derivada de la app
(SvelteKit + Svelte 5, 100% estática/offline):

- rutas reales (`/compat`, `/top`, `/corredora`, `/elenco`, `/ajustes`,
  `/grupos`, `/ranking`, `/ranking-padres`), pensadas para el navegador;
- **compartir la selección por URL** (`?s=...`), con validación de ids;
- **export/import** de las configuraciones guardadas como archivo JSON;
- tema claro minimalista con modo oscuro y alto contraste, los mismos 9
  idiomas que la app y la misma lógica de dominio (tests de paridad
  incluidos).


## Idiomas

- Inglés (por defecto)
- Español (`values-es`)
- Japonés (`values-ja`)
- Chino simplificado (`values-zh-rCN`) y tradicional (`values-zh-rTW`)
- Coreano (`values-ko`)
- Indonesio (`values-id`)
- Tailandés (`values-th`)
- Vietnamita (`values-vi`)

El idioma se puede forzar desde **Ajustes**. Los nombres de personajes se
muestran en japonés (`jp_name`) si el teléfono está en japonés; en cualquier
otro caso se usa `en_name`.

## Accesibilidad

La app funciona con TalkBack y con ajustes de baja visión:

- **Tamaño de texto**: Normal, Grande (15 %) y Muy grande (30 %) sobre la
  escala del sistema.
- **Texto en negrita** y **tema de alto contraste**, en
  **Ajustes → Accesibilidad**.
- **Ventana de bienvenida**: en el primer inicio (y reabrible desde Ajustes)
  propone estas opciones según las señales del sistema (fuente ampliada,
  TalkBack, alto contraste del sistema), con vista previa en vivo.
- Estado anunciado para lectores de pantalla: selección de Umas, secciones
  desplegables, encabezados y total de herencia.

Para reportar problemas de accesibilidad, abrí un issue en
[GitHub](https://github.com/MaximillionSnyder/umafinidad/issues).
Alineado con EN 301 549 capítulo 12 (documentación de accesibilidad).

## Burbuja flotante (acceso rápido)

Desde **Ajustes → Burbuja flotante** se puede activar una burbuja sobre
cualquier app (estilo grabador de pantalla) para consultar afinidad mientras
se juega:

- al tocarla se despliega un panel lateral con la **calculadora rápida**
  (genealogía completa de 7 posiciones, buscador difuso, autocompletar y
  total de afinidad); las acciones rápidas —limpiar y autocompletar— y la
  afinidad total viven en la **cabecera**, junto al título, para no gastar
  alto en una fila aparte;
- el buscador propone las caras que entran a lo ancho de la franja, en un
  carrusel deslizable (el degradado del borde avisa que hay más). Cada cara
  ocupa el ancho que le toca, así que en una franja ancha las caras son más
  grandes en vez de dejar huecos;
- se puede **elegir el lugar**: tocar un slot vacío lo marca como destino y
  la próxima ficha va ahí, aunque no sea el próximo del orden; si el
  personaje ya estaba colocado, se mueve. Sin destino elegido, se coloca en
  el primer hueco válido como siempre;
- el fondo del panel se puede dejar **translúcido** para ver el juego detrás
  (**Ajustes → Burbuja flotante → Panel translúcido**, activado por defecto);
- la genealogía se puede ver en **una columna** (7 filas) o en **dos
  columnas** (**Ajustes → Burbuja flotante → Genealogía en dos columnas**): el
  hijo queda en una card grande y abajo van los padres y los abuelos de a dos
  por fila, agrupados por rama (la columna izquierda es la línea del Padre 1).
  En las cards angostas los nombres largos pasan a marquesina;
- el panel **abre como una banda ancha pegada al borde superior** (93 % del
  ancho y 38 % del alto de la pantalla, del lado opuesto a la burbuja), así el
  centro y el fondo quedan libres para el juego;
- se puede **mover** arrastrando su cabecera y **redimensionar** con la manija
  de su esquina inferior interna, hasta **todo el ancho de la pantalla**: se
  recuerdan la posición y el tamaño, la franja nunca se sale de la pantalla y
  la manija mira siempre al centro, esté donde esté el panel (si queda encima
  de la burbuja, se cierra con la X, Ocultar o Atrás); El círculo de la burbuja se elige en
  **Ajustes → Burbuja flotante → Tamaño de la burbuja** (Chico 44, Normal 56,
  Grande 72, Muy grande 88 dp; hay un botón para restablecer tamaño y
  posición);
- el panel **no se cierra al tocar afuera**: solo con la burbuja, la X, Atrás
  o el botón Ocultar, así podés seguir jugando con la franja abierta;
- incluye atajos para abrir Compatibilidad, Mi corredora, Mis Umas y Ajustes;
- se arrastra y se pega al borde más cercano con una animación corta,
  recordando su posición, y se oculta desde el propio panel o desde la
  notificación.

Requiere el permiso **Mostrar sobre otras apps** (`SYSTEM_ALERT_WINDOW`); en
Android 13+ también pide permiso de notificaciones para el aviso persistente.
El servicio en primer plano declara tipo `specialUse` (Android 14+).

La burbuja y la zona de descarte se crean una sola vez por servicio. El panel
se monta en una ventana nueva en cada apertura (una ventana recién creada es
la que engancha el teclado del buscador). El estado —selección, búsqueda,
sugerencias y slot destino— vive en `EstadoBurbuja`, así que cerrar y reabrir
el panel no lo pierde. La posición y el tamaño automáticos salen de
`FRACCION_ANCHO_PANEL` (0.93) y `FRACCION_ALTO_PANEL` (0.38) con el borde
superior como ancla, salvo que el usuario la haya movido o redimensionado a
mano (ahí mandan la posición y el tamaño guardados).

## Versión Pro

Tres funciones se desbloquean con una **licencia Pro** de pago único,
validada en el propio dispositivo (sin cuentas, sin servidor y sin permisos
nuevos):

- **Burbuja: mover y redimensionar el panel** — arrastrar la cabecera y la
  manija de la esquina solo funcionan con licencia; sin ella el panel muestra
  un candado que abre la pantalla Pro y vuelve a la posición y el tamaño
  automáticos (lo guardado no se borra: al activar Pro vuelve).
- **Mejores padres** — el modo queda a la vista con un candado y, sin
  licencia, ni se calcula (es el cálculo más pesado de la app).
- **Guardar genealogías** — el botón de guardar en Mi corredora lleva a la
  pantalla Pro. Leer y borrar las genealogías ya guardadas sigue siendo
  gratis.

La pantalla Pro (**Ajustes → Uma Afinidad Pro**, o el candado de cada
función) lista las funciones, explica cómo conseguir un código y permite
activarlo, desactivarlo y volver a activarlo. El código tiene la forma
`UMA-XXXX-XXXX-XXXX`: 8 caracteres de cuerpo y 4 de firma (checksum FNV-1a de
32 bits con finalizador de murmur3); la validación tolera minúsculas, espacios
y guiones, y el alfabeto no usa caracteres ambiguos (`0/O`, `1/I/L`).

Para emitir códigos (mantenedor):

```bash
node scripts/generar-codigos-pro.mjs        # 5 códigos
node scripts/generar-codigos-pro.mjs 20     # 20 códigos
node scripts/generar-codigos-pro.mjs --verificar UMA-XXXX-XXXX-XXXX
```

El script y `LicenciaPro` comparten el algoritmo; `ProTest` fija vectores
comunes para que no se separen.

## Estructura

```
├── app/src/main/assets/data/     # JSON datamined (committeados)
├── app/src/main/java/            # Kotlin (Compose + lógica porteada)
│   └── .../umafinidad/
│       ├── data/                 # DTOs + repositorio (lee assets) + licencia Pro
│       ├── domain/               # AffinityModel + Herencia (porte 1:1)
│       ├── overlay/              # burbuja flotante + panel de acceso rápido
│       └── ui/                   # tema M3 oscuro + pantallas (+ ui/pro)
├── sitio/                        # Web nueva (SvelteKit + Svelte 5)
│   ├── scripts/                  # sync de assets y extracción de strings
│   ├── src/lib/                  # dominio, datos, worker, i18n, componentes
│   ├── src/routes/               # rutas reales de la web
│   └── static/                   # datos y avatares generados (no versionados)
├── scripts/
│   ├── fetch-gametora.mjs        # actualiza assets/data desde GameTora
│   └── generate-launcher-icons.mjs
└── .github/workflows/            # android.yml (APK) + sitio.yml (web)
```

## Uso

```bash
npm run fetch              # actualizar datos datamined (incluye aptitudes) → app/src/main/assets/data/
npm run fetch:aptitudes    # solo aptitudes (track/distancia/estilo) por personaje
npm run icons              # regenerar íconos launcher legacy
node scripts/generar-codigos-pro.mjs   # emitir códigos de licencia Pro
node scripts/verificar-strings.mjs     # paridad de claves/marcadores i18n
./gradlew :app:assembleDebug          # APK debug
./gradlew :app:testDebugUnitTest      # tests de paridad con la web

# Web nueva (sitio/)
npm run sitio:dev          # servidor de desarrollo
npm run sitio:check        # svelte-check (0 errores)
npm run sitio:lint         # ESLint
npm run sitio:test         # Vitest (dominio, paridad, a11y)
npm run sitio:build        # build estático en sitio/build/
```

## Releases

El workflow compila y firma automáticamente:

- **push a `dev`** → APK debug como artifact
- **tag `v*`** → AAB + APK firmados y release en GitHub

La primera vez hay que generar el keystore (modo bootstrap): ejecutar el
workflow manualmente con una contraseña, descargar el zip con la clave y
cargar los secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD` y
`ANDROID_KEY_PASSWORD`.

## Web

`sitio/` es la web publicada en GitHub Pages
(https://maximillionsnyder.github.io/umafinidad/). El workflow `sitio.yml`
corre typecheck, lint, tests y build en cada push/PR; para republicar, ejecutar
el workflow **Sitio** manualmente con `deploy` activado. El port React anterior
(`web/`) se retiró una vez verificada la paridad y el deploy.

