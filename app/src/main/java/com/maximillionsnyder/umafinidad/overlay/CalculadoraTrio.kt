package com.maximillionsnyder.umafinidad.overlay

import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.GrupoCompartido
import com.maximillionsnyder.umafinidad.domain.Rango

/* Calculadora rápida de afinidad de la burbuja flotante: hijo + hasta dos
   padres. Pura (sin Android) para poder testearla en la JVM. */

const val SLOTS_TRIO = 3

data class TrioEstado(val ids: List<Int?> = List(SLOTS_TRIO) { null }) {

    /* Mismo gesto que la pantalla de compatibilidad: si el personaje ya
       estaba, se quita; si no, va al primer hueco libre. */
    fun alternar(id: Int): TrioEstado {
        if (ids.contains(id)) return TrioEstado(ids.map { if (it == id) null else it })
        val hueco = ids.indexOfFirst { it == null }
        if (hueco == -1) return this
        return TrioEstado(ids.toMutableList().also { it[hueco] = id })
    }

    fun quitar(slot: Int): TrioEstado {
        if (slot !in ids.indices || ids[slot] == null) return this
        return TrioEstado(ids.toMutableList().also { it[slot] = null })
    }

    fun limpiar(): TrioEstado = TrioEstado()

    val elegidos: List<Int> get() = ids.filterNotNull()
}

data class TrioResultado(
    val puntos: Int,
    val rango: Rango?,
    val compartidos: List<GrupoCompartido>,
)

/* Misma matemática que la pantalla de compatibilidad: par con dos
   elegidos, trío con tres. Null mientras haya menos de dos. */
fun calcularTrio(modelo: AffinityModel, estado: TrioEstado): TrioResultado? {
    val ids = estado.elegidos
    return when (ids.size) {
        2 -> resultado(modelo, modelo.puntajePar(ids[0], ids[1]), ids)
        3 -> resultado(modelo, modelo.puntajeTrio(ids[0], ids[1], ids[2]), ids)
        else -> null
    }
}

private fun resultado(modelo: AffinityModel, puntos: Int, ids: List<Int>) = TrioResultado(
    puntos = puntos,
    rango = modelo.rango(puntos),
    compartidos = modelo.gruposCompartidos(ids),
)
