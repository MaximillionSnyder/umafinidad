package com.maximillionsnyder.umafinidad.overlay

/* Zona de descarte de la burbuja: la X que aparece abajo al arrastrarla.
   Puro (sin Android) para poder testearlo en la JVM. */

object ZonaQuitar {

    /* Centrada horizontalmente, a `margenInferior` del borde de abajo. */
    fun centro(pantallaAncho: Int, pantallaAlto: Int, tamano: Int, margenInferior: Int): Posicion {
        val x = ((pantallaAncho - tamano) / 2).coerceAtLeast(0)
        val y = (pantallaAlto - tamano - margenInferior).coerceAtLeast(0)
        return Posicion(x, y)
    }

    /* ¿La burbuja está sobre la zona? Intersección de rectángulos con un
       margen de agarre para no exigir puntería exacta. */
    fun sobre(
        burbujaX: Int,
        burbujaY: Int,
        tamanoBurbuja: Int,
        x: Int,
        y: Int,
        tamano: Int,
        agarre: Int,
    ): Boolean {
        val izquierda = x - agarre
        val derecha = x + tamano + agarre
        val arriba = y - agarre
        val abajo = y + tamano + agarre
        return burbujaX + tamanoBurbuja > izquierda &&
            burbujaX < derecha &&
            burbujaY + tamanoBurbuja > arriba &&
            burbujaY < abajo
    }
}
