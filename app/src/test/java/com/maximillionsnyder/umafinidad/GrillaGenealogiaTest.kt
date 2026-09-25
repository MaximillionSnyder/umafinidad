package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.overlay.FILAS_GENEALOGIA_POR_RAMA
import org.junit.Assert.assertEquals
import org.junit.Test

/* Layout en dos columnas del panel de la burbuja: el hijo en una card grande y
   el resto de a dos por fila, agrupado por rama (la columna izquierda es la
   línea del Padre 1 y la derecha la del Padre 2). */
class GrillaGenealogiaTest {

    @Test
    fun cubreTodosLosSlotsUnaVez() {
        val slots = FILAS_GENEALOGIA_POR_RAMA.flatten()
        assertEquals(SLOTS, slots.size)
        assertEquals((0 until SLOTS).toList(), slots.sorted())
    }

    @Test
    fun elHijoVaSoloEnLaPrimeraFila() {
        assertEquals(listOf(0), FILAS_GENEALOGIA_POR_RAMA.first())
    }

    @Test
    fun lasFilasDeAbajoSonParejas() {
        FILAS_GENEALOGIA_POR_RAMA.drop(1).forEach { fila ->
            assertEquals("cada fila de abajo lleva dos cards", 2, fila.size)
        }
    }

    @Test
    fun losPadresVanPrimeroYPorRama() {
        assertEquals(listOf(1, 2), FILAS_GENEALOGIA_POR_RAMA[1])
    }

    @Test
    fun cadaColumnaEsUnaRama() {
        /* Columna 0: Padre 1 (1) y sus abuelos (3, 4).
           Columna 1: Padre 2 (2) y sus abuelos (5, 6). */
        val columna0 = FILAS_GENEALOGIA_POR_RAMA.drop(1).map { it[0] }
        val columna1 = FILAS_GENEALOGIA_POR_RAMA.drop(1).map { it[1] }
        assertEquals(listOf(1, 3, 4), columna0)
        assertEquals(listOf(2, 5, 6), columna1)
    }

    @Test
    fun cadaFilaJuntaAlPrimerOAlSegundoAbueloDeLasDosRamas() {
        /* Fila 2: primeros abuelos (3 y 5); fila 3: segundos (4 y 6). */
        assertEquals(listOf(3, 5), FILAS_GENEALOGIA_POR_RAMA[2])
        assertEquals(listOf(4, 6), FILAS_GENEALOGIA_POR_RAMA[3])
    }
}
