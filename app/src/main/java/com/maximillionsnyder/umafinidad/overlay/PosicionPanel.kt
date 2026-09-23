package com.maximillionsnyder.umafinidad.overlay

import kotlin.math.roundToInt

/* Posición de la franja del panel de la burbuja.
   Puro (sin Android) para poder testearlo en la JVM. */

/* Tamaño de la franja (ancho x alto en píxeles). */
data class TamanoPanel(val ancho: Int, val alto: Int)

/* Rectángulo de la franja: posición y tamaño juntos. El redimensionado puede
   correr la X (la franja crece hacia el centro desde el borde anclado). */
data class RectanguloPanel(val x: Int, val y: Int, val ancho: Int, val alto: Int)

object PosicionPanel {

    /* Ancho de la franja: proporcional a la pantalla, con topes para que no
       quede una tira ilegible en teléfonos chicos ni enorme en tablets. */
    fun ancho(pantallaAncho: Int, fraccion: Float, minPx: Int, maxPx: Int): Int =
        (pantallaAncho * fraccion).toInt().coerceIn(minPx, maxPx)

    /* Posición automática: la franja va al borde opuesto al de la burbuja
       (para no taparla), centrada verticalmente y acotada a la pantalla. */
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

    /* Coordenada X de la franja automática: pegada al borde si la burbuja
       está a la derecha, o al borde opuesto si no. */
    fun x(ancho: Int, anchoPanel: Int, margen: Int, burbujaDerecha: Boolean): Int =
        if (burbujaDerecha) {
            margen
        } else {
            (ancho - anchoPanel - margen).coerceAtLeast(margen)
        }

    /* ¿La franja quedó en la mitad derecha de la pantalla? De eso dependen la
       manija de redimensionar (mira al centro) y el degradado del carrusel.
       Se mide la franja, no la burbuja: el usuario puede moverla. */
    fun enLadoDerecho(x: Int, anchoPanel: Int, pantallaAncho: Int): Boolean =
        x + anchoPanel / 2 >= pantallaAncho / 2

    /* Deja la franja dentro de la pantalla, con margen en los bordes. */
    fun acotar(
        x: Int,
        y: Int,
        anchoPanel: Int,
        altoPanel: Int,
        pantallaAncho: Int,
        pantallaAlto: Int,
        margen: Int,
    ): Posicion {
        val maxX = (pantallaAncho - anchoPanel - margen).coerceAtLeast(margen)
        val maxY = (pantallaAlto - altoPanel - margen).coerceAtLeast(margen)
        return Posicion(x.coerceIn(margen, maxX), y.coerceIn(margen, maxY))
    }

    /* Posición nueva al arrastrar la franja entera desde su cabecera. */
    fun mover(
        x: Int,
        y: Int,
        dx: Float,
        dy: Float,
        anchoPanel: Int,
        altoPanel: Int,
        pantallaAncho: Int,
        pantallaAlto: Int,
        margen: Int,
    ): Posicion = acotar(
        x = x + dx.roundToInt(),
        y = y + dy.roundToInt(),
        anchoPanel = anchoPanel,
        altoPanel = altoPanel,
        pantallaAncho = pantallaAncho,
        pantallaAlto = pantallaAlto,
        margen = margen,
    )

    /* Rectángulo nuevo tras arrastrar la manija de la esquina inferior
       interna. La manija mira al centro: el ancho crece al alejarse del borde
       por el que está anclada la franja y el borde opuesto queda fijo (el
       izquierdo si la franja está a la izquierda, el derecho si está a la
       derecha). El ancho puede llegar a toda la pantalla; el alto crece hacia
       abajo y la franja nunca se sale de la pantalla. */
    fun redimensionar(
        x: Int,
        y: Int,
        anchoActual: Int,
        altoActual: Int,
        dx: Float,
        dy: Float,
        pantallaAncho: Int,
        pantallaAlto: Int,
        margen: Int,
        panelDerecha: Boolean,
        minAncho: Int,
        minAlto: Int,
    ): RectanguloPanel {
        val deltaAncho = if (panelDerecha) -dx else dx
        val maxAncho = maxAncho(pantallaAncho, margen, minAncho)
        val maxAlto = (pantallaAlto - y - margen).coerceAtLeast(minAlto)
        val ancho = (anchoActual + deltaAncho.roundToInt()).coerceIn(minAncho, maxAncho)
        val alto = (altoActual + dy.roundToInt()).coerceIn(minAlto, maxAlto)
        val nuevaX = if (panelDerecha) x + anchoActual - ancho else x
        return RectanguloPanel(
            x = nuevaX.coerceIn(margen, (pantallaAncho - ancho - margen).coerceAtLeast(margen)),
            y = y,
            ancho = ancho,
            alto = alto,
        )
    }

    /* Ancho máximo: toda la pantalla menos los márgenes. La franja puede
       quedar encima de la burbuja (el usuario la agranda a propósito): para
       cerrarla siguen estando la X, Ocultar y Atrás. */
    fun maxAncho(pantallaAncho: Int, margen: Int, minAncho: Int): Int =
        (pantallaAncho - 2 * margen).coerceAtLeast(minAncho)
}
