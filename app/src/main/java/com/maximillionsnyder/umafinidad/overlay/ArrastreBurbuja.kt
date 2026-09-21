package com.maximillionsnyder.umafinidad.overlay

import kotlin.math.roundToInt

/* Arrastre de la burbuja flotante.
   Puro (sin Android) para poder testearlo en la JVM.

   Guarda la posición de partida y va acumulando el desplazamiento del gesto
   en Float: el redondeo a píxeles se hace una sola vez, al aplicarlo a la
   ventana. Así no se acumulan errores de redondeo y el gesto lento también
   mueve (antes cada delta se redondeaba por separado y los menores a 1px se
   perdían). */
class ArrastreBurbuja(
    /* Posición de la ventana al empezar el gesto. */
    private val origenX: Int,
    private val origenY: Int,
    private val tamano: Int,
    private val margen: Int,
    private val pantalla: Posicion,
) {

    private var desplazadoX = 0f
    private var desplazadoY = 0f
    private var activo = false

    /* Posición acotada a la pantalla para este punto del gesto. */
    val posicion: Posicion
        get() = PosicionBurbuja.acotar(
            origenX + desplazadoX.roundToInt(),
            origenY + desplazadoY.roundToInt(),
            pantalla.x,
            pantalla.y,
            tamano,
            margen,
        )

    fun iniciar() {
        activo = true
        desplazadoX = 0f
        desplazadoY = 0f
    }

    fun mover(dx: Float, dy: Float) {
        if (!activo) return
        desplazadoX += dx
        desplazadoY += dy
    }

    fun terminar() {
        activo = false
    }
}

/* Cuántos fotogramas dura el "imán" al borde. Una cantidad fija de pasos hace
   que la animación se sienta igual de rápida en cualquier recorrido. */
const val FOTOGRAMAS_IMAN = 10

/* Separación entre fotogramas del imán: la duración no depende del recorrido.
   El retardo real lo impone el sistema, esto es solo un piso para que la
   animación no se coma el gesto siguiente. */
const val RETARDO_FOTOGRAMA_MS = 16L

/* Posiciones por las que pasa la ventana hasta pegarse al borde más cercano.
   Devuelve la lista completa, con la posición final incluida; si ya está en el
   borde, un solo paso (sin transacción de ventana de más). */
fun pasosIman(
    desdeX: Int,
    desdeY: Int,
    ancho: Int,
    tamano: Int,
    margen: Int,
    fotogramas: Int = FOTOGRAMAS_IMAN,
): List<Posicion> {
    val destinoX = PosicionBurbuja.iman(desdeX, ancho, tamano, margen)
    val delta = destinoX - desdeX
    if (delta == 0) return listOf(Posicion(desdeX, desdeY))

    val pasos = fotogramas.coerceAtLeast(1)
    return (1..pasos).map { i ->
        Posicion(x = desdeX + (delta * i) / pasos, y = desdeY)
    }
}
