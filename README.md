# Uma Afinidad

App **nativa Android** (Kotlin + Jetpack Compose) para calcular la afinidad y
la herencia de Uma Musume con los datos datamined de GameTora. Es la versión
nativa del visor web publicado en
[MaximillionSnyder.github.io](https://github.com/MaximillionSnyder/MaximillionSnyder.github.io).

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

## Estructura

```
├── app/src/main/assets/data/     # JSON datamined (committeados)
├── app/src/main/java/            # Kotlin (Compose + lógica porteada)
│   └── .../umafinidad/
│       ├── data/                 # DTOs + repositorio (lee assets)
│       ├── domain/               # AffinityModel + Herencia (porte 1:1)
│       └── ui/                   # tema M3 oscuro + pantallas
├── scripts/
│   ├── fetch-gametora.mjs        # actualiza assets/data desde GameTora
│   └── generate-launcher-icons.mjs
└── .github/workflows/android.yml # build debug (push) + release firmado (tags v*)
```

## Uso

```bash
npm run fetch              # actualizar datos datamined (incluye aptitudes) → app/src/main/assets/data/
npm run fetch:aptitudes    # solo aptitudes (track/distancia/estilo) por personaje
npm run icons              # regenerar íconos launcher legacy
./gradlew :app:assembleDebug          # APK debug
./gradlew :app:testDebugUnitTest      # tests de paridad con la web
```

## Releases

El workflow compila y firma automáticamente:

- **push a `dev`** → APK debug como artifact
- **tag `v*`** → AAB + APK firmados y release en GitHub

La primera vez hay que generar el keystore (modo bootstrap): ejecutar el
workflow manualmente con una contraseña, descargar el zip con la clave y
cargar los secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD` y
`ANDROID_KEY_PASSWORD`.
