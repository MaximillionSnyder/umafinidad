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
- tema claro minimalista con modo oscuro y alto contraste, i18n es/en/ja y
  la misma lógica de dominio que la app (tests de paridad incluidos).


## Idiomas

- Inglés (por defecto)
- Español (`values-es`)
- Japonés (`values-ja`)

Los nombres de personajes se muestran en japonés (`jp_name`) si el teléfono
está en japonés; en cualquier otro caso se usa `en_name`.

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
  (hijo + dos padres, con buscador difuso y desglose de grupos compartidos);
- incluye atajos para abrir Compatibilidad, Mi corredora, Mis Umas y Ajustes;
- se arrastra y se pega al borde más cercano, recordando su posición, y se
  oculta desde el propio panel o desde la notificación.

Requiere el permiso **Mostrar sobre otras apps** (`SYSTEM_ALERT_WINDOW`); en
Android 13+ también pide permiso de notificaciones para el aviso persistente.
El servicio en primer plano declara tipo `specialUse` (Android 14+).

## Estructura

```
├── app/src/main/assets/data/     # JSON datamined (committeados)
├── app/src/main/java/            # Kotlin (Compose + lógica porteada)
│   └── .../umafinidad/
│       ├── data/                 # DTOs + repositorio (lee assets)
│       ├── domain/               # AffinityModel + Herencia (porte 1:1)
│       ├── overlay/              # burbuja flotante + panel de acceso rápido
│       └── ui/                   # tema M3 oscuro + pantallas
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

