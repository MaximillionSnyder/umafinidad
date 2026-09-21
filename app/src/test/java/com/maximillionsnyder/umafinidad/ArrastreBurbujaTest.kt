package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.overlay.ArrastreBurbuja
import com.maximillionsnyder.umafinidad.overlay.FOTOGRAMAS_IMAN
import com.maximillionsnyder.umafinidad.overlay.Posicion
import com.maximillionsnyder.umafinidad.overlay.PosicionBurbuja
import com.maximillionsnyder.umafinidad.overlay.pasosIman
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/* Tests del arrastre y del "imán" animado de la burbuja flotante. */
class ArrastreBurbujaTest {

    private val ancho = 1080
    private val alto = 1920
    private val tamano = 154
    private val margen = 22
    private val pantalla = Posicion(ancho, alto)

    private fun gesto(x: Int, y: Int) = ArrastreBurbuja(x, y, tamano, margen, pantalla)

    @Test
    fun sinMoverSeQuedaDondeEstaba() {
        val arrastre = gesto(300, 700).also { it.iniciar() }
        assertEquals(Posicion(300, 700), arrastre.posicion)
    }

    /* El desplazamiento se acumula en Float y se redondea una sola vez:
       -5.6 + 2.1 = -3.5, que redondeado da -3 (no se pierde por truncar cada
       delta por separado). */
    @Test
    fun acumulaElDesplazamientoDelGesto() {
        val arrastre = gesto(300, 700).also { it.iniciar() }
        arrastre.mover(10.4f, -5.6f)
        arrastre.mover(4.6f, 2.1f)
        assertEquals(Posicion(315, 697), arrastre.posicion)
    }

    /* El caso que el código viejo perdía: deltas de 0.25px redondeados uno por
       uno daban 0 de movimiento; acumulados dan los 5px reales del gesto. */
    @Test
    fun unGestoLentoIgualMueve() {
        val arrastre = gesto(100, 100).also { it.iniciar() }
        repeat(20) { arrastre.mover(0.25f, 0f) }
        assertEquals(5, arrastre.posicion.x - 100)
    }

    @Test
    fun acotaElArrastreDentroDeLaPantalla() {
        val arrastre = gesto(300, 700).also { it.iniciar() }
        arrastre.mover(-9999f, -9999f)
        assertEquals(Posicion(margen, margen), arrastre.posicion)

        val arrastre2 = gesto(300, 700).also { it.iniciar() }
        arrastre2.mover(9999f, 9999f)
        assertEquals(
            PosicionBurbuja.acotar(9999, 9999, ancho, alto, tamano, margen),
            arrastre2.posicion,
        )
    }

    @Test
    fun ignoraMovimientosSinGestoActivo() {
        val arrastre = gesto(300, 700)
        arrastre.mover(50f, 50f)
        assertEquals(Posicion(300, 700), arrastre.posicion)

        arrastre.iniciar()
        arrastre.mover(50f, 0f)
        arrastre.terminar()
        arrastre.mover(50f, 0f)
        assertEquals(Posicion(350, 700), arrastre.posicion)
    }

    /* ---- Imán animado ---- */

    @Test
    fun elImanTerminaEnElBordeMasCercano() {
        val pasos = pasosIman(500, 800, ancho, tamano, margen)
        assertEquals(PosicionBurbuja.iman(500, ancho, tamano, margen), pasos.last().x)
        assertEquals(800, pasos.last().y)
    }

    @Test
    fun elImanNoMueveElEjeVertical() {
        val pasos = pasosIman(100, 800, ancho, tamano, margen)
        assertTrue(pasos.all { it.y == 800 })
    }

    @Test
    fun elImanAvanzaSiempreHaciaElBorde() {
        val pasos = pasosIman(100, 700, ancho, tamano, margen)
        val destino = PosicionBurbuja.iman(100, ancho, tamano, margen)
        assertEquals(destino, pasos.last().x)
        /* Monótono: nunca retrocede ni se pasa del borde. */
        pasos.zipWithNext { a, b -> assertTrue(b.x <= a.x && b.x >= destino) }
    }

    @Test
    fun elImanDuraSiempreLosMismosFotogramas() {
        assertEquals(FOTOGRAMAS_IMAN, pasosIman(0, 0, ancho, tamano, margen).size)
        assertEquals(FOTOGRAMAS_IMAN, pasosIman(900, 0, ancho, tamano, margen).size)
        assertEquals(FOTOGRAMAS_IMAN, pasosIman(10, 0, ancho, tamano, margen).size)
    }

    @Test
    fun yaPegadoAlBordeNoGeneraTransacciones() {
        val borde = PosicionBurbuja.iman(5000, ancho, tamano, margen)
        val pasos = pasosIman(borde, 700, ancho, tamano, margen)
        assertEquals(1, pasos.size)
        assertEquals(Posicion(borde, 700), pasos.first())
    }

    @Test
    fun unSoloFotogramaVaDirectoAlBorde() {
        val pasos = pasosIman(300, 700, ancho, tamano, margen, fotogramas = 1)
        assertEquals(1, pasos.size)
        assertEquals(PosicionBurbuja.iman(300, ancho, tamano, margen), pasos.first().x)
    }
}
