package com.maximillionsnyder.umafinidad.data

import android.content.Context

/* API key de uma.moe para el buscador de padres.
 *
 * Vive en su propio archivo de SharedPreferences ("umamoe_api") en lugar de
 * ui_prefs, y no por capricho: las reglas de backup de Android solo saben
 * excluir archivos enteros, no claves sueltas. Con archivo propio se puede
 * dejar la key fuera del backup y de la transferencia entre dispositivos sin
 * perder el resto de las preferencias.
 * Ver res/xml/backup_rules.xml y res/xml/data_extraction_rules.xml.
 *
 * La key no se escribe nunca en logs ni en la salida de una búsqueda. */
class UmamoeApiKey(context: Context) {

    private val prefs = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    fun get(): String? = prefs.getString(KEY, null)?.takeIf { it.isNotBlank() }

    fun hay(): Boolean = get() != null

    fun guardar(valor: String) {
        val limpia = valor.trim()
        if (limpia.isEmpty()) borrar() else prefs.edit().putString(KEY, limpia).apply()
    }

    fun borrar() {
        prefs.edit().remove(KEY).apply()
    }

    /* Solo comprueba la forma, para avisar en Ajustes si parece mal copiada.
     * Que sea válida de verdad lo dice el primer 401 del servidor. */
    fun pareceValida(): Boolean = get()?.startsWith("uma_") == true

    private companion object {
        const val ARCHIVO = "umamoe_api"
        const val KEY = "api_key"
    }
}
