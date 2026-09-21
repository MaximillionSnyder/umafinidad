package com.maximillionsnyder.umafinidad.overlay

import kotlin.math.max

/* Reparto de las caras del buscador en la franja del panel.
   Puro (sin Android) para poder testearlo en la JVM.

   La franja conserva su tamaño: lo que se reparte distinto adentro son las
   caras de las sugerencias. Con esto el carrusel muestra todas las fichas que
   entran a lo ancho (y no una cantidad fija que deja huecos en pantallas
   grandes o corta fichas en las chicas). */
data class RepartoOpciones(
    /* Cuántas fichas entran a lo ancho. */
    val visibles: Int,
    /* Ancho de cada ficha para ocupar el ancho disponible sin cortarse. */
    val anchoFicha: Int,
)

/* `anchoDisponible` es el ancho útil de la franja, `ficha` el ancho deseado de
   una ficha, `espacio` la separación entre fichas y `maximo` el tope de fichas
   que tiene sentido mostrar a la vez. */
fun repartirOpciones(
    anchoDisponible: Int,
    ficha: Int,
    espacio: Int,
    maximo: Int,
): RepartoOpciones {
    if (anchoDisponible <= 0 || ficha <= 0) return RepartoOpciones(visibles = 0, anchoFicha = ficha)
    val tope = max(maximo, 1)
    val visibles = ((anchoDisponible + espacio) / (ficha + espacio)).coerceIn(1, tope)
    val anchoFicha = (anchoDisponible - (visibles - 1) * espacio) / visibles
    return RepartoOpciones(visibles = visibles, anchoFicha = max(anchoFicha, 1))
}
