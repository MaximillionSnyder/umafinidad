package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.overlay.PosicionPanel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        x: Int = margen,
        y: Int = 100,
        anchoActual: Int = 300,
        altoActual: Int = 800,
        dx: Float = 0f,
        dy: Float = 0f,
        panelDerecha: Boolean = false,
    ) = PosicionPanel.redimensionar(
        x = x,
        y = y,
        anchoActual = anchoActual,
        altoActual = altoActual,
        dx = dx,
        dy = dy,
        pantallaAncho = ancho,
        pantallaAlto = alto,
        margen = margen,
        panelDerecha = panelDerecha,
        minAncho = 100,
        minAlto = 200,
    )

    @Test
    fun elPanelDeLaIzquierdaCreceHaciaLaDerecha() {
        val r = redimensionar(dx = 50f, dy = 30f, panelDerecha = false)
        assertEquals(350, r.ancho)
        assertEquals(830, r.alto)
        /* El borde izquierdo (opuesto a la manija) queda fijo. */
        assertEquals(margen, r.x)
    }

    @Test
    fun elPanelDeLaDerechaCreceHaciaLaIzquierda() {
        val r = redimensionar(
            x = ancho - 300 - margen,
            dx = -50f,
            dy = 30f,
            panelDerecha = true,
        )
        assertEquals(350, r.ancho)
        assertEquals(830, r.alto)
        /* El borde derecho queda fijo: la X se corre lo que creció el ancho. */
        assertEquals(ancho - 350 - margen, r.x)
    }

    @Test
    fun elAnchoLlegaATodoElAnchoDeLaPantalla() {
        val izquierda = redimensionar(dx = 9999f, panelDerecha = false)
        assertEquals(ancho - 2 * margen, izquierda.ancho)
        assertEquals(margen, izquierda.x)

        /* Desde la derecha el borde derecho queda fijo y la X se corre hasta
           el margen izquierdo. */
        val derecha = redimensionar(
            x = ancho - 300 - margen,
            dx = -9999f,
            panelDerecha = true,
        )
        assertEquals(ancho - 2 * margen, derecha.ancho)
        assertEquals(margen, derecha.x)
    }

    @Test
    fun elAnchoMaximoEsLaPantallaMenosLosMargenes() {
        assertEquals(ancho - 2 * margen, PosicionPanel.maxAncho(ancho, margen, minAncho = 100))
        /* En pantallas muy chicas manda el mínimo. */
        assertEquals(100, PosicionPanel.maxAncho(90, margen, minAncho = 100))
    }

    @Test
    fun elAltoNoSeSaleDeLaPantalla() {
        val r = redimensionar(dy = 9999f, y = 100)
        assertEquals(alto - 100 - margen, r.alto)
    }

    @Test
    fun noBajaDeLosMinimos() {
        val r = redimensionar(dx = -9999f, dy = -9999f)
        assertEquals(100, r.ancho)
        assertEquals(200, r.alto)
    }

    @Test
    fun laXSeAnclaAlLadoDelPanel() {
        assertEquals(margen, PosicionPanel.x(ancho, 300, margen, burbujaDerecha = true))
        assertEquals(
            ancho - 300 - margen,
            PosicionPanel.x(ancho, 300, margen, burbujaDerecha = false),
        )
    }

    /* ---- Mover la franja entera (arrastre de la cabecera) ---- */

    private fun mover(
        x: Int = 100,
        y: Int = 200,
        dx: Float = 0f,
        dy: Float = 0f,
        anchoMovido: Int = 300,
        altoMovido: Int = 800,
    ) = PosicionPanel.mover(
        x = x,
        y = y,
        dx = dx,
        dy = dy,
        anchoPanel = anchoMovido,
        altoPanel = altoMovido,
        pantallaAncho = ancho,
        pantallaAlto = alto,
        margen = margen,
    )

    @Test
    fun moverCorreLaFranjaConElDedo() {
        val p = mover(dx = 40f, dy = -30f)
        assertEquals(140, p.x)
        assertEquals(170, p.y)
    }

    @Test
    fun moverNoDejaLaFranjaFueraDeLaPantalla() {
        val abajoDerecha = mover(dx = 9999f, dy = 9999f)
        assertEquals(ancho - 300 - margen, abajoDerecha.x)
        assertEquals(alto - 800 - margen, abajoDerecha.y)

        val arribaIzquierda = mover(dx = -9999f, dy = -9999f)
        assertEquals(margen, arribaIzquierda.x)
        assertEquals(margen, arribaIzquierda.y)
    }

    @Test
    fun unaFranjaMasGrandeQueLaPantallaQuedaEnElMargen() {
        val p = PosicionPanel.acotar(
            x = 500, y = 500,
            anchoPanel = 1200, altoPanel = 2000,
            pantallaAncho = ancho, pantallaAlto = alto, margen = margen,
        )
        assertEquals(margen, p.x)
        assertEquals(margen, p.y)
    }

    @Test
    fun elLadoDependeDeDondeEstaLaFranja() {
        assertFalse(PosicionPanel.enLadoDerecho(margen, 300, ancho))
        assertTrue(PosicionPanel.enLadoDerecho(ancho - 300 - margen, 300, ancho))
        /* Al cruzar la mitad cambia el lado (y con él, la manija). */
        assertFalse(PosicionPanel.enLadoDerecho(ancho / 2 - 200, 300, ancho))
        assertTrue(PosicionPanel.enLadoDerecho(ancho / 2 + 50, 300, ancho))
    }
}
