package com.maximillionsnyder.umafinidad.overlay

/* Posición de la franja lateral del panel de la burbuja.
   Puro (sin Android) para poder testearlo en la JVM. */

object PosicionPanel {

    /* Ancho de la franja: proporcional a la pantalla, con topes para que no
       quede una tira ilegible en teléfonos chicos ni enorme en tablets. */
    fun ancho(pantallaAncho: Int, fraccion: Float, minPx: Int, maxPx: Int): Int =
        (pantallaAncho * fraccion).toInt().coerceIn(minPx, maxPx)

    /* La franja va al borde opuesto al de la burbuja (para no taparla),
       centrada verticalmente y acotada a la pantalla. */
    fun calcular(
        ancho: Int,
        alto: Int,
        anchoPanel: Int,
        altoPanel: Int,
        margen: Int,
        burbujaDerecha: Boolean,
    ): Posicion {
        val x = if (burbujaDerecha) {
            margen
        } else {
            (ancho - anchoPanel - margen).coerceAtLeast(margen)
        }
        val maxY = (alto - altoPanel - margen).coerceAtLeast(margen)
        val y = ((alto - altoPanel) / 2).coerceIn(margen, maxY)
        return Posicion(x, y)
    }
}
