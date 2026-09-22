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

    /* ---- Redimensionado con la manija ---- */

    private fun redimensionar(
        anchoActual: Int = 300,
        altoActual: Int = 800,
        dx: Float = 0f,
        dy: Float = 0f,
        y: Int = 100,
        burbujaDerecha: Boolean = true,
        tamanoBurbuja: Int = 168,
    ) = PosicionPanel.redimensionar(
        anchoActual = anchoActual,
        altoActual = altoActual,
        dx = dx,
        dy = dy,
        pantallaAncho = ancho,
        pantallaAlto = alto,
        y = y,
        margen = margen,
        burbujaDerecha = burbujaDerecha,
        tamanoBurbuja = tamanoBurbuja,
        minAncho = 100,
        minAlto = 200,
    )

    @Test
    fun elPanelDeLaIzquierdaCreceHaciaLaDerecha() {
        val tamano = redimensionar(dx = 50f, dy = 30f, burbujaDerecha = true)
        assertEquals(350, tamano.ancho)
        assertEquals(830, tamano.alto)
    }

    @Test
    fun elPanelDeLaDerechaCreceHaciaLaIzquierda() {
        val tamano = redimensionar(dx = -50f, dy = 30f, burbujaDerecha = false)
        assertEquals(350, tamano.ancho)
        assertEquals(830, tamano.alto)
    }

    @Test
    fun elAnchoNoLlegaHastaLaBurbuja() {
        val tamano = redimensionar(dx = 9999f, burbujaDerecha = true)
        assertEquals(ancho - 168 - 3 * margen, tamano.ancho)
    }

    @Test
    fun elAltoNoSeSaleDeLaPantalla() {
        val tamano = redimensionar(dy = 9999f, y = 100)
        assertEquals(alto - 100 - margen, tamano.alto)
    }

    @Test
    fun noBajaDeLosMinimos() {
        val tamano = redimensionar(dx = -9999f, dy = -9999f)
        assertEquals(100, tamano.ancho)
        assertEquals(200, tamano.alto)
    }

    @Test
    fun laXSeAnclaAlLadoDelPanel() {
        assertEquals(margen, PosicionPanel.x(ancho, 300, margen, burbujaDerecha = true))
        assertEquals(
            ancho - 300 - margen,
            PosicionPanel.x(ancho, 300, margen, burbujaDerecha = false),
        )
    }
}
