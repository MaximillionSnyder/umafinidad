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

- **push a `main`** → APK debug como artifact
- **tag `v*`** → AAB + APK firmados y release en GitHub

La primera vez hay que generar el keystore (modo bootstrap): ejecutar el
workflow manualmente con una contraseña, descargar el zip con la clave y
cargar los secrets `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD` y
`ANDROID_KEY_PASSWORD`.
