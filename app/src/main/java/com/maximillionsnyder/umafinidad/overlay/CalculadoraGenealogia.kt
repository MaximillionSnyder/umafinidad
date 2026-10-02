package com.maximillionsnyder.umafinidad.overlay

import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.armarArbol
import com.maximillionsnyder.umafinidad.domain.vinculos

/* Total del árbol con la semántica del juego, para el panel de la burbuja.
   El resto de la colocación (alternar, colocarEn, quitar, slotsPara) vive en
   domain/Herencia.kt porque la comparten la burbuja y la pantalla de
   compatibilidad. */

fun totalDe(modelo: AffinityModel, seleccion: List<Int?>): Int? {
    if (vinculos(armarArbol(seleccion.toTypedArray())).isEmpty()) return null
    return modelo.totalDeSeleccion(seleccion)
}