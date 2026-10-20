package com.maximillionsnyder.umafinidad.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer

/* ===== Lógica pura (testeable en JVM sin Android) ===== */

fun serializarElenco(ids: Set<Int>): String =
    jsonParser.encodeToString(SetSerializer(Int.serializer()), ids)

/* Decodifica sin lanzar: null si el JSON está corrupto o no es un array de
   enteros. El llamador decide si limpia los datos o sigue con vacío. */
fun decodificarElenco(json: String): Set<Int>? =
    try {
        jsonParser.decodeFromString(SetSerializer(Int.serializer()), json)
    } catch (_: Exception) {
        null
    }

/* Variante segura para persistencia: ante datos corruptos devuelve conjunto
   vacío en vez de tumbar el arranque de la app. */
fun deserializarElenco(json: String): Set<Int> =
    decodificarElenco(json) ?: emptySet()

/* ===== Wrapper de SharedPreferences ===== */

/* Elenco personal: ids de los personajes que el usuario posee ("Mis
   corredoras"). Con ellos se calculan sus mejores linajes reales. */
/* El constructor de producción delega en Context; el interno permite
   inyectar SharedPreferences en los tests JVM sin Android. */
class ElencoRepository internal constructor(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("mi_elenco", Context.MODE_PRIVATE),
    )

    /* Nunca lanza: si la preferencia está corrupta (JSON inválido o valor de
       otro tipo) devuelve conjunto vacío y borra la clave para no repetir el
       fallo en cada arranque. */
    fun obtener(): Set<Int> {
        val json = try {
            prefs.getString(KEY_IDS, null)
        } catch (_: Exception) {
            limpiarClaveCorrupta()
            return emptySet()
        } ?: return emptySet()

        val ids = decodificarElenco(json)
        if (ids == null) {
            limpiarClaveCorrupta()
            return emptySet()
        }
        return ids
    }

    fun reemplazar(ids: Set<Int>) {
        prefs.edit().putString(KEY_IDS, serializarElenco(ids)).apply()
    }

    private fun limpiarClaveCorrupta() {
        try {
            prefs.edit().remove(KEY_IDS).apply()
        } catch (_: Exception) {
            /* Si tampoco se puede limpiar, seguimos devolviendo vacío. */
        }
    }

    private companion object {
        const val KEY_IDS = "ids"
    }
}
