package com.maximillionsnyder.umafinidad.data

import android.content.Context
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer

/* ===== Lógica pura (testeable en JVM sin Android) ===== */

fun serializarElenco(ids: Set<Int>): String =
    jsonParser.encodeToString(SetSerializer(Int.serializer()), ids)

fun deserializarElenco(json: String): Set<Int> =
    jsonParser.decodeFromString(SetSerializer(Int.serializer()), json)

/* ===== Wrapper de SharedPreferences ===== */

/* Elenco personal: ids de los personajes que el usuario posee ("Mis
   corredoras"). Con ellos se calculan sus mejores linajes reales. */
class ElencoRepository(context: Context) {

    private val prefs = context.getSharedPreferences("mi_elenco", Context.MODE_PRIVATE)

    fun obtener(): Set<Int> =
        prefs.getString(KEY_IDS, null)?.let { deserializarElenco(it) } ?: emptySet()

    fun reemplazar(ids: Set<Int>) {
        prefs.edit().putString(KEY_IDS, serializarElenco(ids)).apply()
    }

    private companion object {
        const val KEY_IDS = "ids"
    }
}
