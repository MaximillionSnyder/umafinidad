package com.maximillionsnyder.umafinidad.overlay

import kotlin.math.roundToInt

/* Posición de la franja lateral del panel de la burbuja.
   Puro (sin Android) para poder testearlo en la JVM. */

/* Tamaño de la franja (ancho x alto en píxeles). */
data class TamanoPanel(val ancho: Int, val alto: Int)

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
        val x = x(ancho, anchoPanel, margen, burbujaDerecha)
        val maxY = (alto - altoPanel - margen).coerceAtLeast(margen)
        val y = ((alto - altoPanel) / 2).coerceIn(margen, maxY)
        return Posicion(x, y)
    }

    /* Coordenada X de la franja: pegada al borde si la burbuja está a la
       derecha, o al borde opuesto si no. */
    fun x(ancho: Int, anchoPanel: Int, margen: Int, burbujaDerecha: Boolean): Int =
        if (burbujaDerecha) {
            margen
        } else {
            (ancho - anchoPanel - margen).coerceAtLeast(margen)
        }

    /* Tamaño nuevo tras arrastrar la manija de la esquina inferior interna.
       La manija mira al centro: el ancho crece al alejarse del borde donde
       está anclado el panel (según el lado) y el alto crece hacia abajo.
       La franja nunca llega a la burbuja (ni se sale de la pantalla). */
    fun redimensionar(
        anchoActual: Int,
        altoActual: Int,
        dx: Float,
        dy: Float,
        pantallaAncho: Int,
        pantallaAlto: Int,
        y: Int,
        margen: Int,
        burbujaDerecha: Boolean,
        tamanoBurbuja: Int,
        minAncho: Int,
        minAlto: Int,
    ): TamanoPanel {
        val deltaAncho = if (burbujaDerecha) dx else -dx
        val maxAncho = maxAncho(pantallaAncho, tamanoBurbuja, margen, minAncho)
        val maxAlto = (pantallaAlto - y - margen).coerceAtLeast(minAlto)
        return TamanoPanel(
            ancho = (anchoActual + deltaAncho.roundToInt()).coerceIn(minAncho, maxAncho),
            alto = (altoActual + dy.roundToInt()).coerceIn(minAlto, maxAlto),
        )
    }

    /* Ancho máximo: el espacio libre en el lado del panel sin llegar a la
       burbuja (que está pegada al borde opuesto, con su margen y una
       separación extra). */
    fun maxAncho(pantallaAncho: Int, tamanoBurbuja: Int, margen: Int, minAncho: Int): Int =
        (pantallaAncho - tamanoBurbuja - 3 * margen).coerceAtLeast(minAncho)
}
