package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.TamanoTexto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/* Accesibilidad: la escala de texto crece con el nivel y el normal es neutro. */
class AccesibilidadTest {

    @Test
    fun normalEsNeutro() {
        assertEquals(1f, TamanoTexto.NORMAL.escala, 0.001f)
    }

    @Test
    fun escalasCrecientes() {
        assertTrue(TamanoTexto.GRANDE.escala > TamanoTexto.NORMAL.escala)
        assertTrue(TamanoTexto.MUY_GRANDE.escala > TamanoTexto.GRANDE.escala)
    }

    @Test
    fun valoresDocumentados() {
        assertEquals(1.15f, TamanoTexto.GRANDE.escala, 0.001f)
        assertEquals(1.3f, TamanoTexto.MUY_GRANDE.escala, 0.001f)
    }
}
