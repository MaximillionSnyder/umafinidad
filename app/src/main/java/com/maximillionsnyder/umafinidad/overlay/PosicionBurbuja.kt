package com.maximillionsnyder.umafinidad.overlay

/* Posición de la burbuja flotante y reglas de "imán" a los bordes.
   Puro (sin Android) para poder testearlo en la JVM. */

data class Posicion(val x: Int, val y: Int)

object PosicionBurbuja {

    /* Deja la posición dentro de la pantalla, con margen en los bordes. */
    fun acotar(x: Int, y: Int, ancho: Int, alto: Int, tamano: Int, margen: Int): Posicion {
        val maxX = (ancho - tamano - margen).coerceAtLeast(margen)
        val maxY = (alto - tamano - margen).coerceAtLeast(margen)
        return Posicion(x.coerceIn(margen, maxX), y.coerceIn(margen, maxY))
    }

    /* Pega la burbuja al borde izquierdo o derecho, el más cercano. */
    fun iman(x: Int, ancho: Int, tamano: Int, margen: Int): Int {
        val izquierda = margen
        val derecha = (ancho - tamano - margen).coerceAtLeast(margen)
        return if (centro(x, tamano) < ancho / 2) izquierda else derecha
    }

    /* El panel se abre del lado al que quedó pegada la burbuja. */
    fun enLadoDerecho(x: Int, ancho: Int, tamano: Int): Boolean =
        centro(x, tamano) >= ancho / 2

    /* ¿El punto (en coordenadas de pantalla) cayó dentro de la ventana de la
       burbuja? Se usa para distinguir el toque sobre la burbuja del toque
       fuera del panel. */
    fun contiene(x: Int, y: Int, tamano: Int, puntoX: Float, puntoY: Float): Boolean =
        puntoX >= x && puntoX <= x + tamano && puntoY >= y && puntoY <= y + tamano

    private fun centro(x: Int, tamano: Int): Int = x + tamano / 2
}
