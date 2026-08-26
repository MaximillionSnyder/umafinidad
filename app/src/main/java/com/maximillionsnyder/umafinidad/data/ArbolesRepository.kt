package com.maximillionsnyder.umafinidad.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer

/* Configuración de árbol genealógico guardada por el usuario en
   "Mi corredora" (puede diferir del óptimo teórico porque el usuario la
   ajusta según los personajes que posee). */
@Serializable
data class ArbolGuardado(
    val id: Long,
    val hijoId: Int,
    val nombre: String,
    /* Los 7 slots; nulls permitidos por si guarda algo incompleto. */
    val seleccion: List<Int?>,
    val total: Int,
    val creadoEn: Long,
)

/* ===== Lógica pura (testeable en JVM sin Android) ===== */

fun serializarArboles(arboles: List<ArbolGuardado>): String =
    jsonParser.encodeToString(ListSerializer(ArbolGuardado.serializer()), arboles)

fun deserializarArboles(json: String): List<ArbolGuardado> =
    jsonParser.decodeFromString(ListSerializer(ArbolGuardado.serializer()), json)

/* Dedupe: si ya existía una config del mismo hijo con selección idéntica,
   se reemplaza; si no, se agrega. Devuelve ordenada por fecha desc. */
fun fusionarArbol(existente: List<ArbolGuardado>, nuevo: ArbolGuardado): List<ArbolGuardado> {
    val sinDuplicado = existente.filterNot {
        it.hijoId == nuevo.hijoId && it.seleccion == nuevo.seleccion
    }
    return (listOf(nuevo) + sinDuplicado).sortedByDescending { it.creadoEn }
}

/* ===== Wrapper de SharedPreferences ===== */

class ArbolesRepository(context: Context) {

    private val prefs = context.getSharedPreferences("arboles_guardados", Context.MODE_PRIVATE)

    fun todos(): List<ArbolGuardado> =
        prefs.getString(KEY_LISTA, null)?.let { deserializarArboles(it) } ?: emptyList()

    fun reemplazarTodos(lista: List<ArbolGuardado>) {
        prefs.edit().putString(KEY_LISTA, serializarArboles(lista)).apply()
    }

    private companion object {
        const val KEY_LISTA = "lista"
    }
}
