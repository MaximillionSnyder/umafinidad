package com.maximillionsnyder.umafinidad.domain

/* Porte 1:1 de src/herencia.js de la PWA.
   7 slots fijos: [0] hijo, [1..2] padres, [3..6] abuelos (2 por rama). */

const val SLOTS = 7

data class Arbol(
    val hijo: Int?,
    val padres: List<Int?>,
    val abuelosDe: List<List<Int?>>,
)

fun armarArbol(seleccion: Array<Int?>): Arbol = Arbol(
    hijo = seleccion[0],
    padres = listOf(seleccion[1], seleccion[2]),
    abuelosDe = listOf(
        listOf(seleccion[3], seleccion[4]),
        listOf(seleccion[5], seleccion[6]),
    ),
)

enum class Rol { HIJO, PADRE, ABUELO }

fun rolDeSlot(i: Int): Rol = when {
    i == 0 -> Rol.HIJO
    i <= 2 -> Rol.PADRE
    else -> Rol.ABUELO
}

/* Reglas del juego al colocar un personaje en un slot:
   - el hijo no puede ser padre (en ninguna rama);
   - los padres deben ser distintos entre sí;
   - nadie puede ser abuelo de su propia rama;
   - los dos abuelos de una misma rama no se repiten;
   - el hijo SÍ puede ser abuelo (corredora: esa relación vale 0) y los
     cruces entre ramas están permitidos. */
fun puedeIrEn(seleccion: Array<Int?>, slot: Int, id: Int?): Boolean {
    if (slot < 0 || slot >= SLOTS || id == null) return false
    return when (rolDeSlot(slot)) {
        Rol.HIJO -> seleccion[1] != id && seleccion[2] != id
        Rol.PADRE -> {
            val rama = slot - 1
            for (i in 0..2) {
                if (seleccion[i] == id) return false /* ya es el hijo u otro padre */
            }
            val abuelosRama = listOf(seleccion[3 + rama * 2], seleccion[4 + rama * 2])
            abuelosRama.none { it == id } /* prohibido: abuelo de su propia rama */
        }
        Rol.ABUELO -> {
            val rama = (slot - 3) / 2 /* slots 3-4 → rama 0, 5-6 → rama 1 */
            val hermano = if (slot % 2 == 1) slot + 1 else slot - 1
            if (seleccion[1 + rama] == id) return false /* su propio padre */
            seleccion[hermano] != id /* abuelo repetido en la rama */
        }
    }
}

/* Primer slot vacío donde el personaje sí pueda ir. Devuelve:
   índice del slot libre, -1 si la selección está completa,
   -2 si hay huecos pero ninguno le sirve al personaje. */
fun slotPara(seleccion: Array<Int?>, id: Int): Int {
    val hayHueco = seleccion.any { it == null }
    for (i in 0 until SLOTS) {
        if (seleccion[i] == null && puedeIrEn(seleccion, i, id)) return i
    }
    return if (hayHueco) -2 else -1
}

enum class TipoVinculo { HIJO_PADRE, ENTRE_PADRES, HIJO_PADRE_ABUELO }

data class Vinculo(
    val tipo: TipoVinculo,
    val ids: List<Int>,
    val esCorredora: Boolean = false,
)

fun vinculos(arbol: Arbol): List<Vinculo> {
    val v = mutableListOf<Vinculo>()
    val (hijo, padres, abuelosDe) = arbol
    if (hijo != null) {
        for (p in padres) {
            if (p != null) v.add(Vinculo(TipoVinculo.HIJO_PADRE, listOf(hijo, p)))
        }
    }
    if (padres[0] != null && padres[1] != null) {
        v.add(Vinculo(TipoVinculo.ENTRE_PADRES, listOf(padres[0]!!, padres[1]!!)))
    }
    /* Regla del juego: cada abuelo se vincula con el padre de su misma rama
       y el hijo a la vez. Si el abuelo es la misma corredora, vale 0. */
    if (hijo != null) {
        for (p in 0..1) {
            val padre = padres[p]
            if (padre == null) continue
            for (a in abuelosDe[p]) {
                if (a != null) {
                    v.add(Vinculo(TipoVinculo.HIJO_PADRE_ABUELO, listOf(hijo, padre, a), a == hijo))
                }
            }
        }
    }
    return v
}

fun posicionesDe(seleccion: Array<Int?>, id: Int): List<Int> =
    seleccion.withIndex().filter { it.value == id }.map { it.index }

fun contarSeleccionados(seleccion: Array<Int?>): Int =
    seleccion.count { it != null }
