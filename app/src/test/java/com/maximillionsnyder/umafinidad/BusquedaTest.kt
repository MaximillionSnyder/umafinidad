package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.distanciaDamerau
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/* Tests de la búsqueda difusa del autocompletado. */

class BusquedaTest {

    private fun char(id: Int, en: String, jp: String = "", url: String = "") =
        Character(charId = id, enName = en, jpName = jp.ifEmpty { en }, playable = true, active = true, urlName = url)

    private val personajes = listOf(
        char(1001, "Special Week", "スペシャルウィーク", "special-week"),
        char(1032, "Silence Suzuka", "サイレンススズカ", "silence-suzuka"),
        char(1042, "Air Groove", "エアグルーヴ", "air-groove"),
        char(1015, "TM Opera O", "テイエムオペラオー", "tm-opera-o"),
    )

    /* ===== Damerau-Levenshtein ===== */

    @Test
    fun distanciaBasica() {
        assertEquals(0, distanciaDamerau("abc", "abc"))
        assertEquals(3, distanciaDamerau("kitten", "sitting"))
        assertEquals(3, distanciaDamerau("", "abc"))
        assertEquals(2, distanciaDamerau("ab", ""))
    }

    @Test
    fun transposicionCuestaUno() {
        assertEquals(1, distanciaDamerau("suzuak", "suzuka"))
        assertEquals(1, distanciaDamerau("ab", "ba"))
    }

    /* ===== Sugerencias ===== */

    @Test
    fun susukaSugiereSuzuka() {
        val top = rankearSugerencias(personajes, "susuka")
        assertTrue(top.isNotEmpty())
        assertEquals("Silence Suzuka", top.first().enName)
    }

    @Test
    fun omisionYExcesoDeLetras() {
        assertEquals("Silence Suzuka", rankearSugerencias(personajes, "szuka").first().enName)
        assertEquals("Silence Suzuka", rankearSugerencias(personajes, "suzukaa").first().enName)
        assertEquals("Silence Suzuka", rankearSugerencias(personajes, "suzuak").first().enName)
    }

    @Test
    fun palabraConTypoEnNombreCompuesto() {
        val top = rankearSugerencias(personajes, "special wek")
        assertEquals("Special Week", top.first().enName)
    }

    @Test
    fun prefijoRankeaPrimero() {
        val top = rankearSugerencias(personajes, "spec")
        assertEquals("Special Week", top.first().enName)
    }

    @Test
    fun japonesExacto() {
        val top = rankearSugerencias(personajes, "スペシャル")
        assertEquals("Special Week", top.first().enName)
    }

    @Test
    fun romajiPorUrlName() {
        val top = rankearSugerencias(personajes, "air grove")
        assertEquals("Air Groove", top.first().enName)
    }

    @Test
    fun queryBasuraNoSugiereNada() {
        assertTrue(rankearSugerencias(personajes, "xyzwq").isEmpty())
    }

    @Test
    fun queryCortaNoSugiere() {
        assertTrue(rankearSugerencias(personajes, "s").isEmpty())
    }

    @Test
    fun limiteDeSugerencias() {
        val muchas = (2000..2099).map { char(it, "Name $it") }
        assertEquals(5, rankearSugerencias(muchas, "nam").size)
    }
}
