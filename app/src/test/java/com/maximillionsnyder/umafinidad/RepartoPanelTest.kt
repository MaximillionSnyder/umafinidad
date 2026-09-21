package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.overlay.repartirOpciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/* Tests del reparto de caras del buscador en la franja del panel. */
class RepartoPanelTest {

    private val ficha = 121
    private val espacio = 16

    @Test
    fun reparteLasFichasQueEntran() {
        val reparto = repartirOpciones(
            anchoDisponible = 700, ficha = ficha, espacio = espacio, maximo = 6,
        )
        assertEquals(5, reparto.visibles)
        assertTrue(reparto.anchoFicha in 121..140)
    }

    @Test
    fun nuncaDevuelveCeroFichasConAnchoUtil() {
        val reparto = repartirOpciones(
            anchoDisponible = 80, ficha = ficha, espacio = espacio, maximo = 6,
        )
        assertEquals(1, reparto.visibles)
        assertEquals(80, reparto.anchoFicha)
    }

    @Test
    fun respetaElTopeDeFichas() {
        val reparto = repartirOpciones(
            anchoDisponible = 2400, ficha = ficha, espacio = espacio, maximo = 6,
        )
        assertEquals(6, reparto.visibles)
    }

    @Test
    fun sinEspacioNoReparteNada() {
        val reparto = repartirOpciones(
            anchoDisponible = 0, ficha = ficha, espacio = espacio, maximo = 6,
        )
        assertEquals(0, reparto.visibles)
    }

    @Test
    fun laUltimaFichaNoSeCorta() {
        val reparto = repartirOpciones(
            anchoDisponible = 1280, ficha = ficha, espacio = espacio, maximo = 6,
        )
        val usado = reparto.visibles * reparto.anchoFicha + (reparto.visibles - 1) * espacio
        assertTrue("no debe pasarse del ancho disponible", usado <= 1280)
    }
}
