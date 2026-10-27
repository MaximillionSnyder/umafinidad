package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.domain.FILAS_AJUSTES
import com.maximillionsnyder.umafinidad.cartas.domain.IDIOMAS_UI
import com.maximillionsnyder.umafinidad.cartas.domain.duracionEnSegundos
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaVista
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AjustesTest {

    /* Los segundos se enseñan con la unidad de cada idioma; el número es el
       mismo. Entero cuando el dato es exacto. */
    @Test
    fun duracionesExactasSeMuestranEnSegundosEnteros() {
        assertEquals("1 s", duracionEnSegundos(1000, "en"))
        assertEquals("9 s", duracionEnSegundos(9000, "en"))
        assertEquals("60 s", duracionEnSegundos(60000, "en"))
        assertEquals("130 s", duracionEnSegundos(130000, "en"))
        assertEquals("60秒", duracionEnSegundos(60000, "ja"))
        assertEquals("60초", duracionEnSegundos(60000, "ko"))
        assertEquals("60秒", duracionEnSegundos(60000, "tw"))
        assertEquals("60 s", duracionEnSegundos(60000, "es"))
    }

    /* Decimales con punto en todos los idiomas: es el separador de los datos y
       el que no hay que escapar en ja/ko/tw (el español sí pedía coma). */
    @Test
    fun duracionesConFraccionUsanUnDecimal() {
        assertEquals("1.5 s", duracionEnSegundos(1500, "en"))
        assertEquals("2.5 s", duracionEnSegundos(2500, "en"))
        assertEquals("0.3 s", duracionEnSegundos(250, "en"))
        assertEquals("1.5秒", duracionEnSegundos(1500, "ja"))
        assertEquals("1.5초", duracionEnSegundos(1500, "ko"))
        assertEquals("1.5秒", duracionEnSegundos(1500, "tw"))
        assertEquals("1.5 s", duracionEnSegundos(1500, "es"))
    }

    /* Un idioma que no sea de los cinco cae al inglés, como el resto de textos. */
    @Test
    fun unIdiomaDesconocidoUsaElIngles() {
        assertEquals("60 s", duracionEnSegundos(60000, "fr"))
    }

    @Test
    fun etiquetasDeVistaSeTraducen() {
        assertEquals("Comfortable", etiquetaVista("comoda", "en"))
        assertEquals("Compact", etiquetaVista("compacta", "en"))
        assertEquals("Comfortable", etiquetaVista("otra", "en"))
        assertEquals("標準", etiquetaVista("comoda", "ja"))
        assertEquals("컴팩트", etiquetaVista("compacta", "ko"))
        assertEquals("緊湊", etiquetaVista("compacta", "tw"))
        assertEquals("Cómoda", etiquetaVista("comoda", "es"))
        assertEquals("Compacta", etiquetaVista("compacta", "es"))
    }

    /* Las dos filas de Ajustes (vista cómoda y compacta) tienen que estar en los
       cinco idiomas de la interfaz: el motor cae al inglés en silencio y la
       pantalla quedaría a medias. */
    @Test
    fun lasEtiquetasDeAjustesEstanEnLosCincoIdiomas() {
        val faltan = FILAS_AJUSTES.flatMap { (nombre, textos) ->
            IDIOMAS_UI.filter { textos[it].isNullOrBlank() }.map { "$nombre ($it)" }
        }
        assertTrue("ajustes sin fila en algún idioma:\n" + faltan.joinToString("\n"), faltan.isEmpty())
        assertTrue("no se leyó ninguna etiqueta de ajustes", FILAS_AJUSTES.size >= 2)
    }
}
