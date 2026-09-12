package com.maximillionsnyder.umafinidad.data

import android.content.Context

/* Modo de visualización de la grilla de personajes. */
enum class ModoGrilla { TARJETAS, LISTA }

/* Tamaño de texto de accesibilidad (multiplicador sobre la escala del sistema). */
enum class TamanoTexto(val escala: Float) { NORMAL(1f), GRANDE(1.15f), MUY_GRANDE(1.3f) }

fun tamanoSegunFontScale(fontScale: Float): TamanoTexto = when {
    fontScale >= 1.3f -> TamanoTexto.MUY_GRANDE
    fontScale >= 1.15f -> TamanoTexto.GRANDE
    else -> TamanoTexto.NORMAL
}

/* Preferencias de UI persistidas (SharedPreferences, sin dependencias). */
class PrefsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)

    var modoGrilla: ModoGrilla
        get() = if (prefs.getBoolean(KEY_GRID_VERTICAL, true)) ModoGrilla.TARJETAS else ModoGrilla.LISTA
        set(valor) = prefs.edit().putBoolean(KEY_GRID_VERTICAL, valor == ModoGrilla.TARJETAS).apply()

    var estiloAvatar: EstiloAvatar
        get() = prefs.getString(KEY_ESTILO_AVATAR, null)?.let { raw ->
            try { EstiloAvatar.valueOf(raw) } catch (_: IllegalArgumentException) { EstiloAvatar.COLOR }
        } ?: EstiloAvatar.COLOR
        set(valor) = prefs.edit().putString(KEY_ESTILO_AVATAR, valor.name).apply()

    var tema: ThemeMode
        get() {
            prefs.getString(KEY_TEMA, null)?.let { raw ->
                try {
                    return ThemeMode.valueOf(raw)
                } catch (_: IllegalArgumentException) {
                }
            }
            // Migración: el interruptor de contraste (v3.9.x) ahora es un tema.
            if (prefs.getBoolean(KEY_ALTO_CONTRASTE, false)) {
                prefs.edit().putString(KEY_TEMA, ThemeMode.ALTO_CONTRASTE.name)
                    .remove(KEY_ALTO_CONTRASTE).apply()
                return ThemeMode.ALTO_CONTRASTE
            }
            return ThemeMode.SISTEMA
        }
        set(valor) = prefs.edit().putString(KEY_TEMA, valor.name).apply()

    var idioma: Idioma
        get() = prefs.getString(KEY_IDIOMA, null)?.let { raw ->
            try { Idioma.valueOf(raw) } catch (_: IllegalArgumentException) { Idioma.SISTEMA }
        } ?: Idioma.SISTEMA
        set(valor) = prefs.edit().putString(KEY_IDIOMA, valor.name).apply()

    var tamanoTexto: TamanoTexto
        get() = prefs.getString(KEY_TAMANO_TEXTO, null)?.let { raw ->
            try { TamanoTexto.valueOf(raw) } catch (_: IllegalArgumentException) { TamanoTexto.NORMAL }
        } ?: TamanoTexto.NORMAL
        set(valor) = prefs.edit().putString(KEY_TAMANO_TEXTO, valor.name).apply()

    var textoNegrita: Boolean
        get() = prefs.getBoolean(KEY_TEXTO_NEGRITA, false)
        set(valor) = prefs.edit().putBoolean(KEY_TEXTO_NEGRITA, valor).apply()

    var bienvenidaAccesibilidadVista: Boolean
        get() = prefs.getBoolean(KEY_BIENVENIDA_ACCE, false)
        set(valor) = prefs.edit().putBoolean(KEY_BIENVENIDA_ACCE, valor).apply()

    private companion object {
        const val KEY_GRID_VERTICAL = "grid_vertical"
        const val KEY_ESTILO_AVATAR = "estilo_avatar"
        const val KEY_TEMA = "tema_modo"
        const val KEY_IDIOMA = "idioma_modo"
        const val KEY_TAMANO_TEXTO = "tamano_texto"
        const val KEY_TEXTO_NEGRITA = "texto_negrita"
        const val KEY_BIENVENIDA_ACCE = "bienvenida_acce_vista"
        // Solo lectura para migrar instalaciones con el interruptor viejo.
        const val KEY_ALTO_CONTRASTE = "alto_contraste"
    }
}
