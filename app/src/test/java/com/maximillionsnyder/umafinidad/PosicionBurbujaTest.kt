package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.overlay.PosicionBurbuja
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/* Tests de la posición e "imán" de bordes de la burbuja flotante. */
class PosicionBurbujaTest {

    private val ancho = 1080
    private val alto = 1920
    private val tamano = 168
    private val margen = 8

    @Test
    fun acotaPosicionesFueraDePantalla() {
        val arribaIzquierda = PosicionBurbuja.acotar(-50, -10, ancho, alto, tamano, margen)
        assertEquals(margen, arribaIzquierda.x)
        assertEquals(margen, arribaIzquierda.y)

        val abajoDerecha = PosicionBurbuja.acotar(5000, 5000, ancho, alto, tamano, margen)
        assertEquals(ancho - tamano - margen, abajoDerecha.x)
        assertEquals(alto - tamano - margen, abajoDerecha.y)
    }

    @Test
    fun conservaPosicionesValidas() {
        val posicion = PosicionBurbuja.acotar(300, 700, ancho, alto, tamano, margen)
        assertEquals(300, posicion.x)
        assertEquals(700, posicion.y)
    }

    @Test
    fun imanAlBordeMasCercano() {
        assertEquals(margen, PosicionBurbuja.iman(100, ancho, tamano, margen))
        assertEquals(ancho - tamano - margen, PosicionBurbuja.iman(800, ancho, tamano, margen))
    }

    @Test
    fun elPanelSeAbreDelLadoDeLaBurbuja() {
        assertFalse(PosicionBurbuja.enLadoDerecho(100, ancho, tamano))
        assertTrue(PosicionBurbuja.enLadoDerecho(800, ancho, tamano))
    }
}
