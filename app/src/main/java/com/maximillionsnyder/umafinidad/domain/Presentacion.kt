package com.maximillionsnyder.umafinidad.domain

/* SOLO presentación de la pantalla "Mi corredora": reparte el total de
   afinidad entre las filas del árbol para que la suma de las filas sea
   exactamente el total. NO interviene en ningún cálculo: el total, los
   rankings, las alternativas y la herencia siguen usando los vínculos
   reales (totalDeSeleccion, puntajePar, puntajeTrio).

   Reparto:
   - Hijo: null (participa en todos los vínculos, no se le atribuye ninguno).
   - Padre: afinidad hijo-padre + la mitad de la afinidad padre-padre.
   - Abuelo: afinidad del trío hijo-padre-abuelo (0 si es la corredora).
   Slots vacíos o árbol sin hijo: null. */
fun AffinityModel.puntosFilaPresentacion(seleccion: List<Int?>): List<Double?> {
    val hijo = seleccion.getOrNull(0)
    if (hijo == null) return List(SLOTS) { null }

    val padre1 = seleccion.getOrNull(1)
    val padre2 = seleccion.getOrNull(2)
    val mitadEntrePadres =
        if (padre1 != null && padre2 != null) puntajePar(padre1, padre2) / 2.0 else 0.0

    return List(SLOTS) { slot ->
        val id = seleccion.getOrNull(slot)
        if (id == null) {
            null
        } else if (slot == 0) {
            null
        } else if (slot == 1 || slot == 2) {
            puntajePar(hijo, id) + mitadEntrePadres
        } else {
            val padre = seleccion.getOrNull(1 + (slot - 3) / 2)
            when {
                padre == null -> null
                id == hijo -> 0.0
                else -> puntajeTrio(hijo, padre, id).toDouble()
            }
        }
    }
}
