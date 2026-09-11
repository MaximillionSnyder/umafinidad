package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.TamanoTexto
import com.maximillionsnyder.umafinidad.data.tamanoSegunFontScale
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

    @Test
    fun fontScaleNormalNoAjusta() {
        assertEquals(TamanoTexto.NORMAL, tamanoSegunFontScale(1f))
        assertEquals(TamanoTexto.NORMAL, tamanoSegunFontScale(1.14f))
    }

    @Test
    fun fontScaleGrandeAjustaAGrande() {
        assertEquals(TamanoTexto.GRANDE, tamanoSegunFontScale(1.15f))
        assertEquals(TamanoTexto.GRANDE, tamanoSegunFontScale(1.29f))
    }

    @Test
    fun fontScaleMuyGrandeAjustaAMuyGrande() {
        assertEquals(TamanoTexto.MUY_GRANDE, tamanoSegunFontScale(1.3f))
        assertEquals(TamanoTexto.MUY_GRANDE, tamanoSegunFontScale(2f))
    }
}
