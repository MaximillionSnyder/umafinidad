package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.overlay.ZonaQuitar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/* Tests de la zona de descarte de la burbuja flotante. */
class ZonaQuitarTest {

    private val pantallaAncho = 1220
    private val pantallaAlto = 2712
    private val tamano = 168
    private val margenInferior = 168

    @Test
    fun laZonaQuedaCentradaAbajo() {
        val centro = ZonaQuitar.centro(pantallaAncho, pantallaAlto, tamano, margenInferior)
        assertEquals((pantallaAncho - tamano) / 2, centro.x)
        assertEquals(pantallaAlto - tamano - margenInferior, centro.y)
    }

    @Test
    fun laBurbujaSobreLaZonaSeDescarta() {
        val centro = ZonaQuitar.centro(pantallaAncho, pantallaAlto, tamano, margenInferior)
        assertTrue(
            ZonaQuitar.sobre(centro.x, centro.y, tamano, centro.x, centro.y, tamano, 42),
        )
    }

    @Test
    fun elMargenDeAgarreCuenta() {
        val centro = ZonaQuitar.centro(pantallaAncho, pantallaAlto, tamano, margenInferior)
        val pegado = centro.x + tamano + 20
        assertTrue(ZonaQuitar.sobre(pegado, centro.y, tamano, centro.x, centro.y, tamano, 42))
        assertFalse(ZonaQuitar.sobre(pegado + 60, centro.y, tamano, centro.x, centro.y, tamano, 42))
    }

    @Test
    fun lejosDeLaZonaNoSeDescarta() {
        assertFalse(ZonaQuitar.sobre(100, 100, tamano, 500, 2000, tamano, 42))
    }
}
