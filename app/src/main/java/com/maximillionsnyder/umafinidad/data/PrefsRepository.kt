package com.maximillionsnyder.umafinidad.data

import android.content.Context

/* Modo de visualización de la grilla de personajes. */
enum class ModoGrilla { TARJETAS, LISTA }

/* Preferencias de UI persistidas (SharedPreferences, sin dependencias). */
class PrefsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)

    var modoGrilla: ModoGrilla
        get() = if (prefs.getBoolean(KEY_GRID_VERTICAL, true)) ModoGrilla.TARJETAS else ModoGrilla.LISTA
        set(valor) = prefs.edit().putBoolean(KEY_GRID_VERTICAL, valor == ModoGrilla.TARJETAS).apply()

    private companion object {
        const val KEY_GRID_VERTICAL = "grid_vertical"
    }
}
