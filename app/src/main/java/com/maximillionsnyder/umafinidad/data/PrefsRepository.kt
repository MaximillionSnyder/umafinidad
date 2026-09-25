package com.maximillionsnyder.umafinidad.data

import android.content.Context
import android.content.SharedPreferences

/* Modo de visualización de la grilla de personajes. */
enum class ModoGrilla { TARJETAS, LISTA }

/* Tamaño de texto de accesibilidad (multiplicador sobre la escala del sistema). */
enum class TamanoTexto(val escala: Float) { NORMAL(1f), GRANDE(1.15f), MUY_GRANDE(1.3f) }

/* Tamaño del círculo de la burbuja flotante, en dp (Normal = 56 dp). */
enum class TamanoBurbuja(val dp: Int) { CHICO(44), NORMAL(56), GRANDE(72), MUY_GRANDE(88) }

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

    /* Burbuja flotante de acceso rápido sobre otras apps. */
    var burbujaActiva: Boolean
        get() = prefs.getBoolean(KEY_BURBUJA_ACTIVA, false)
        set(valor) = prefs.edit().putBoolean(KEY_BURBUJA_ACTIVA, valor).apply()

    /* Posición guardada de la burbuja en píxeles; -1 = todavía sin ubicar. */
    var burbujaX: Int
        get() = prefs.getInt(KEY_BURBUJA_X, -1)
        set(valor) = prefs.edit().putInt(KEY_BURBUJA_X, valor).apply()

    var burbujaY: Int
        get() = prefs.getInt(KEY_BURBUJA_Y, -1)
        set(valor) = prefs.edit().putInt(KEY_BURBUJA_Y, valor).apply()

    /* Fondo del panel de la burbuja: translúcido deja ver la app de atrás. */
    var panelTranslucido: Boolean
        get() = prefs.getBoolean(KEY_PANEL_TRANSLUCIDO, true)
        set(valor) = prefs.edit().putBoolean(KEY_PANEL_TRANSLUCIDO, valor).apply()

    /* Tamaño del círculo de la burbuja flotante. */
    var tamanoBurbuja: TamanoBurbuja
        get() = prefs.getString(KEY_TAMANO_BURBUJA, null)?.let { raw ->
            try {
                TamanoBurbuja.valueOf(raw)
            } catch (_: IllegalArgumentException) {
                TamanoBurbuja.NORMAL
            }
        } ?: TamanoBurbuja.NORMAL
        set(valor) = prefs.edit().putString(KEY_TAMANO_BURBUJA, valor.name).apply()

    /* Panel redimensionado a mano (dp); -1 = tamaño automático por fracción. */
    var panelAnchoDp: Int
        get() = prefs.getInt(KEY_PANEL_ANCHO_DP, -1)
        set(valor) = prefs.edit().putInt(KEY_PANEL_ANCHO_DP, valor).apply()

    var panelAltoDp: Int
        get() = prefs.getInt(KEY_PANEL_ALTO_DP, -1)
        set(valor) = prefs.edit().putInt(KEY_PANEL_ALTO_DP, valor).apply()

    /* Genealogía del panel en dos columnas: el hijo en una card grande y
       padres/abuelos de a dos por fila, por rama. */
    var panelDosColumnas: Boolean
        get() = prefs.getBoolean(KEY_PANEL_DOS_COLUMNAS, false)
        set(valor) = prefs.edit().putBoolean(KEY_PANEL_DOS_COLUMNAS, valor).apply()

    /* Posición de la franja movida a mano (px); -1 = automática (lado
       opuesto a la burbuja, centrada verticalmente). */
    var panelX: Int
        get() = prefs.getInt(KEY_PANEL_X, -1)
        set(valor) = prefs.edit().putInt(KEY_PANEL_X, valor).apply()

    var panelY: Int
        get() = prefs.getInt(KEY_PANEL_Y, -1)
        set(valor) = prefs.edit().putInt(KEY_PANEL_Y, valor).apply()

    /* Avisa cuando cambia el tamaño de la burbuja, para aplicarlo en vivo
       (el servicio lo observa; la pref se cambia desde Ajustes). */
    fun observarTamanoBurbuja(
        alCambiar: () -> Unit,
    ): SharedPreferences.OnSharedPreferenceChangeListener {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, clave ->
            if (clave == KEY_TAMANO_BURBUJA) alCambiar()
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return listener
    }

    fun dejarDeObservar(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private companion object {
        const val KEY_GRID_VERTICAL = "grid_vertical"
        const val KEY_ESTILO_AVATAR = "estilo_avatar"
        const val KEY_TEMA = "tema_modo"
        const val KEY_IDIOMA = "idioma_modo"
        const val KEY_TAMANO_TEXTO = "tamano_texto"
        const val KEY_TEXTO_NEGRITA = "texto_negrita"
        const val KEY_BIENVENIDA_ACCE = "bienvenida_acce_vista"
        const val KEY_BURBUJA_ACTIVA = "burbuja_activa"
        const val KEY_BURBUJA_X = "burbuja_x"
        const val KEY_BURBUJA_Y = "burbuja_y"
        const val KEY_PANEL_TRANSLUCIDO = "panel_translucido"
        const val KEY_TAMANO_BURBUJA = "burbuja_tamano"
        const val KEY_PANEL_ANCHO_DP = "panel_ancho_dp"
        const val KEY_PANEL_ALTO_DP = "panel_alto_dp"
        const val KEY_PANEL_DOS_COLUMNAS = "panel_dos_columnas"
        const val KEY_PANEL_X = "panel_x"
        const val KEY_PANEL_Y = "panel_y"
        // Solo lectura para migrar instalaciones con el interruptor viejo.
        const val KEY_ALTO_CONTRASTE = "alto_contraste"
    }
}
