package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.domain.Textos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/* El motor de textos: una tabla de filas por idioma con respaldo al inglés. Lo
   que no está en la tabla se devuelve tal cual para que se vea que falta (nunca
   un texto inventado). */
class TextosTest {

    private val textos = Textos(
        mapOf(
            "greeting" to mapOf(
                "en" to "Hello",
                "ja" to "こんにちは",
                "ko" to "안녕하세요",
                "tw" to "你好",
                "es" to "Hola",
            ),
            "english_only" to mapOf("en" to "Only English"),
            "blank_japanese" to mapOf("en" to "English text", "ja" to "   "),
        ),
    )

    @Test
    fun devuelveElTextoDelIdiomaPedido() {
        assertEquals("Hello", textos["greeting", "en"])
        assertEquals("こんにちは", textos["greeting", "ja"])
        assertEquals("안녕하세요", textos["greeting", "ko"])
        assertEquals("你好", textos["greeting", "tw"])
        assertEquals("Hola", textos["greeting", "es"])
    }

    /* Si al idioma le falta la fila, se enseña el inglés antes que la clave. */
    @Test
    fun siFaltaElIdiomaCaeAlIngles() {
        assertEquals("Only English", textos["english_only", "ja"])
        assertEquals("Only English", textos["english_only", "tw"])
        assertEquals("Only English", textos["english_only", "es"])
        assertEquals("Hello", textos["greeting", "fr"])
    }

    @Test
    fun unTextoEnBlancoTambienCaeAlIngles() {
        assertEquals("English text", textos["blank_japanese", "ja"])
    }

    @Test
    fun unaClaveDesconocidaDevuelveLaClave() {
        assertEquals("missing", textos["missing", "en"])
        assertEquals("missing", textos["missing", "ja"])
    }

    @Test
    fun clavesDevuelveLasClavesDeLaTabla() {
        assertEquals(setOf("greeting", "english_only", "blank_japanese"), textos.claves)
        assertTrue(textos.claves.isNotEmpty())
    }
}
