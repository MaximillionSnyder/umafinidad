package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.overlay.PosicionPanel
import org.junit.Assert.assertEquals
import org.junit.Test

/* Tests de la posición de la franja del panel de la burbuja flotante. */
class PosicionPanelTest {

    private val ancho = 1080
    private val alto = 1920
    private val anchoPanel = 730
    private val altoPanel = 1382
    private val margen = 8

    @Test
    fun vaAlLadoOpuestoDeLaBurbuja() {
        val izquierda = PosicionPanel.calcular(
            ancho, alto, anchoPanel, altoPanel, margen, burbujaDerecha = true,
        )
        assertEquals(margen, izquierda.x)

        val derecha = PosicionPanel.calcular(
            ancho, alto, anchoPanel, altoPanel, margen, burbujaDerecha = false,
        )
        assertEquals(ancho - anchoPanel - margen, derecha.x)
    }

    @Test
    fun quedaCentradaVerticalmente() {
        val posicion = PosicionPanel.calcular(
            ancho, alto, anchoPanel, altoPanel, margen, burbujaDerecha = true,
        )
        assertEquals((alto - altoPanel) / 2, posicion.y)
    }

    @Test
    fun seAcotaSiNoEntraEnPantalla() {
        val posicion = PosicionPanel.calcular(
            ancho, alto, anchoPanel = 1200, altoPanel = 2000, margen, burbujaDerecha = false,
        )
        assertEquals(margen, posicion.x)
        assertEquals(margen, posicion.y)
    }

    /* Ancho proporcional con topes: teléfono chico, intermedio y tablet. */
    @Test
    fun elAnchoRespetaElMinimoEnTelefonosChicos() {
        assertEquals(570, PosicionPanel.ancho(960, 0.50f, minPx = 570, maxPx = 900))
    }

    @Test
    fun elAnchoRespetaElMaximoEnPantallasGrandes() {
        assertEquals(900, PosicionPanel.ancho(2400, 0.50f, minPx = 570, maxPx = 900))
    }

    @Test
    fun elAnchoEsProporcionalEnTamanosIntermedios() {
        assertEquals(610, PosicionPanel.ancho(1220, 0.50f, minPx = 570, maxPx = 900))
    }
}
