package com.maximillionsnyder.umafinidad.overlay

import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.armarArbol
import com.maximillionsnyder.umafinidad.domain.puedeIrEn
import com.maximillionsnyder.umafinidad.domain.slotPara
import com.maximillionsnyder.umafinidad.domain.vinculos

/* Calculadora rápida de la burbuja flotante: genealogía completa de 7
   posiciones (hijo, dos padres y dos abuelos por rama), con las mismas
   reglas de colocación que la pantalla de compatibilidad.
   Puro (sin Android) para poder testearlo en la JVM. */

/* Selección de 7 posiciones; null = slot vacío. */
val seleccionVacia: List<Int?> = List(SLOTS) { null }

enum class ColocacionResultado { COLOCADO, QUITADO, COMPLETA, REGLA }

data class Colocacion(
    val seleccion: List<Int?>,
    val resultado: ColocacionResultado,
)

/* Mismo gesto que la pantalla de compatibilidad: si el personaje ya estaba,
   se quita; si no, va al primer hueco donde las reglas lo permitan. */
fun alternar(seleccion: List<Int?>, id: Int): Colocacion {
    val actual = seleccion.toTypedArray()
    val posiciones = actual.withIndex().filter { it.value == id }.map { it.index }
    if (posiciones.isNotEmpty()) {
        val quitado = actual.toMutableList().also { it[posiciones.last()] = null }
        return Colocacion(quitado, ColocacionResultado.QUITADO)
    }

    val slot = slotPara(actual, id)
    if (slot >= 0 && puedeIrEn(actual, slot, id)) {
        val colocado = actual.toMutableList().also { it[slot] = id }
        return Colocacion(colocado, ColocacionResultado.COLOCADO)
    }
    return Colocacion(seleccion, if (slot == -1) ColocacionResultado.COMPLETA else ColocacionResultado.REGLA)
}

fun quitar(seleccion: List<Int?>, slot: Int): List<Int?> {
    if (slot !in seleccion.indices || seleccion[slot] == null) return seleccion
    return seleccion.toMutableList().also { it[slot] = null }
}

/* Total del árbol con la semántica del juego; null mientras no haya ningún
   vínculo, para no mostrar un 0 engañoso. */
fun totalDe(modelo: AffinityModel, seleccion: List<Int?>): Int? {
    if (vinculos(armarArbol(seleccion.toTypedArray())).isEmpty()) return null
    return modelo.totalDeSeleccion(seleccion)
}
