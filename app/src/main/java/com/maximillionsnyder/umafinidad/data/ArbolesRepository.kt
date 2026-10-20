package com.maximillionsnyder.umafinidad.data

import android.content.Context
import android.content.SharedPreferences
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

/* Decodifica sin lanzar: null si el JSON está corrupto o no tiene la forma
   esperada. El llamador decide si limpia los datos o sigue con vacío. */
fun decodificarArboles(json: String): List<ArbolGuardado>? =
    try {
        jsonParser.decodeFromString(ListSerializer(ArbolGuardado.serializer()), json)
    } catch (_: Exception) {
        null
    }

/* Variante segura para persistencia: ante datos corruptos devuelve lista
   vacía en vez de tumbar el arranque de la app. */
fun deserializarArboles(json: String): List<ArbolGuardado> =
    decodificarArboles(json) ?: emptyList()

/* Id único para una configuración nueva. Se parte del mayor id guardado (o
   del reloj si el listado está vacío) y se avanza hasta encontrar uno libre:
   dos guardados en el mismo milisegundo ya no colisionan. `creadoEn` es un
   dato aparte (la fecha que se muestra), no la identidad del árbol. */
fun siguienteIdArbol(
    existentes: List<ArbolGuardado>,
    ahora: Long = System.currentTimeMillis(),
): Long {
    var candidato = existentes.maxOfOrNull { it.id }?.coerceAtLeast(ahora) ?: ahora
    while (existentes.any { it.id == candidato }) candidato++
    return candidato
}

/* Arma la configuración nueva con su id único y su fecha de guardado. Se
   calcula `ahora` una sola vez para que id y creadoEn sean consistentes sin
   confundirse entre sí. */
fun crearArbolGuardado(
    existentes: List<ArbolGuardado>,
    hijoId: Int,
    nombre: String,
    seleccion: List<Int?>,
    total: Int,
    ahora: Long = System.currentTimeMillis(),
): ArbolGuardado = ArbolGuardado(
    id = siguienteIdArbol(existentes, ahora),
    hijoId = hijoId,
    nombre = nombre.trim(),
    seleccion = seleccion,
    total = total,
    creadoEn = ahora,
)

/* Dedupe: si ya existía una config del mismo hijo con selección idéntica,
   se reemplaza; si no, se agrega. Devuelve ordenada por fecha desc. */
fun fusionarArbol(existente: List<ArbolGuardado>, nuevo: ArbolGuardado): List<ArbolGuardado> {
    val sinDuplicado = existente.filterNot {
        it.hijoId == nuevo.hijoId && it.seleccion == nuevo.seleccion
    }
    return (listOf(nuevo) + sinDuplicado).sortedByDescending { it.creadoEn }
}

/* ===== Wrapper de SharedPreferences ===== */

/* El constructor de producción delega en Context; el interno permite
   inyectar SharedPreferences en los tests JVM sin Android. */
class ArbolesRepository internal constructor(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("arboles_guardados", Context.MODE_PRIVATE),
    )

    /* Nunca lanza: si la preferencia está corrupta (JSON inválido o valor de
       otro tipo) devuelve lista vacía. Además borra la clave corrupta para
       que el fallo no se repita en cada arranque. */
    fun todos(): List<ArbolGuardado> {
        val json = try {
            prefs.getString(KEY_LISTA, null)
        } catch (_: Exception) {
            limpiarClaveCorrupta()
            return emptyList()
        } ?: return emptyList()

        val lista = decodificarArboles(json)
        if (lista == null) {
            limpiarClaveCorrupta()
            return emptyList()
        }
        return lista
    }

    fun reemplazarTodos(lista: List<ArbolGuardado>) {
        prefs.edit().putString(KEY_LISTA, serializarArboles(lista)).apply()
    }

    private fun limpiarClaveCorrupta() {
        try {
            prefs.edit().remove(KEY_LISTA).apply()
        } catch (_: Exception) {
            /* Si tampoco se puede limpiar, seguimos devolviendo vacío. */
        }
    }

    private companion object {
        const val KEY_LISTA = "lista"
    }
}
