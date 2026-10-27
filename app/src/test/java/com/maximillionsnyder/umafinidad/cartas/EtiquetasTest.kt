package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.domain.IDIOMAS_UI
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaIdioma
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaKind
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaObtencion
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaRareza
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaTipo
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaTipoApoyo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/* Etiquetas de las fichas de personaje y apoyo. El guardián de que el dato no
   traiga códigos nuevos sin traducir es EtiquetasDatosTest; aquí se fijan los
   textos en las cinco lenguas de la interfaz y, sobre todo, los dos falsos
   amigos que estaban mal. Las claves son los códigos de GameTora y no se
   traducen. */
class EtiquetasTest {

    /* "dir" es DIRT: las descripciones dicen "if it's a dirt race" / "in Dirt
       races". Estaba como "Arena", que es otra cosa (y la tabla de condiciones
       ya lo llamaba "tierra"). En los datos el japonés dice ダート, el coreano
       더트 y el chino 沙地. */
    @Test
    fun elTerrenoDeTierraNoEsArena() {
        assertEquals("Dirt", etiquetaTipo("dir", "en"))
        assertEquals("ダート", etiquetaTipo("dir", "ja"))
        assertEquals("더트", etiquetaTipo("dir", "ko"))
        assertEquals("沙地", etiquetaTipo("dir", "tw"))
        assertEquals("Turf", etiquetaTipo("tur", "en"))
        assertEquals("芝", etiquetaTipo("tur", "ja"))
        assertEquals("Tierra", etiquetaTipo("dir", "es"))
        assertEquals("Césped", etiquetaTipo("tur", "es"))
    }

    @Test
    fun losTiposDeSkillSeTraducenConLaEscalaDelJuego() {
        assertEquals("Short", etiquetaTipo("sho", "en"))
        assertEquals("Mile", etiquetaTipo("mil", "en"))
        assertEquals("Medium", etiquetaTipo("med", "en"))
        assertEquals("Long", etiquetaTipo("lng", "en"))
        assertEquals("Front Runner", etiquetaTipo("run", "en"))
        assertEquals("Pace Chaser", etiquetaTipo("ldr", "en"))
        assertEquals("Late Surger", etiquetaTipo("btw", "en"))
        assertEquals("End Closer", etiquetaTipo("cha", "en"))
        assertEquals("Final Leg", etiquetaTipo("l_2", "en"))
        assertEquals("Last Spurt", etiquetaTipo("l_3", "en"))
        assertEquals("Puntera", etiquetaTipo("run", "es"))
        assertEquals("Perseguidora", etiquetaTipo("ldr", "es"))
        assertEquals("Remontadora", etiquetaTipo("btw", "es"))
        assertEquals("Rematadora", etiquetaTipo("cha", "es"))
        assertEquals("Arranque final", etiquetaTipo("l_3", "es"))
    }

    /* Los datos llaman 선입 (no 차입) a 差し y 領頭/前列/居中/後追 a los estilos:
       los textos salen de los assets, no de una traducción a ojo. */
    @Test
    fun losEstilosSeTraducenComoEnLosDatos() {
        assertEquals("逃げ", etiquetaTipo("run", "ja"))
        assertEquals("差し", etiquetaTipo("btw", "ja"))
        assertEquals("追込", etiquetaTipo("cha", "ja"))
        assertEquals("도주", etiquetaTipo("run", "ko"))
        assertEquals("선입", etiquetaTipo("btw", "ko"))
        assertEquals("추입", etiquetaTipo("cha", "ko"))
        assertEquals("領頭", etiquetaTipo("run", "tw"))
        assertEquals("居中", etiquetaTipo("btw", "tw"))
        assertEquals("後追", etiquetaTipo("cha", "tw"))
    }

    /* El dato marca los idiomas que faltan con "zh_tw" (326 skills), no con
       "tw": la pastilla salía como "sin ZH_TW". Esto no cambia con el idioma de
       la app: es el nombre del idioma que falta en el dato. */
    @Test
    fun elChinoTradicionalSeLlamaIgualQueTw() {
        assertEquals("中文", etiquetaIdioma("zh_tw"))
        assertEquals("中文", etiquetaIdioma("tw"))
        assertEquals("EN", etiquetaIdioma("en"))
        assertEquals("한국어", etiquetaIdioma("ko"))
    }

    @Test
    fun laRarezaTieneNombreEnLosCincoIdiomas() {
        assertEquals("Normal", etiquetaRareza(1, "en"))
        assertEquals("Rare", etiquetaRareza(2, "en"))
        assertEquals("Unique", etiquetaRareza(3, "en"))
        assertEquals("Unique", etiquetaRareza(5, "en"))
        assertEquals("Evolution", etiquetaRareza(4, "en"))
        assertEquals("Evolution", etiquetaRareza(6, "en"))
        assertEquals("ノーマル", etiquetaRareza(1, "ja"))
        assertEquals("유니크", etiquetaRareza(3, "ko"))
        assertEquals("進化", etiquetaRareza(4, "tw"))
        assertEquals("Normal", etiquetaRareza(1, "es"))
        assertEquals("Rara", etiquetaRareza(2, "es"))
        assertEquals("Única", etiquetaRareza(3, "es"))
        assertEquals("Evolución", etiquetaRareza(6, "es"))
    }

    @Test
    fun lasEtiquetasDeApoyoYObtencionCubrenLosCodigosDelDato() {
        assertEquals("Speed", etiquetaTipoApoyo("speed", "en"))
        assertEquals("Stamina", etiquetaTipoApoyo("stamina", "en"))
        assertEquals("Guts", etiquetaTipoApoyo("guts", "en"))
        assertEquals("Wit", etiquetaTipoApoyo("intelligence", "en"))
        assertEquals("Friend", etiquetaTipoApoyo("friend", "en"))
        assertEquals("スタミナ", etiquetaTipoApoyo("stamina", "ja"))
        assertEquals("지능", etiquetaTipoApoyo("intelligence", "ko"))
        assertEquals("持久力", etiquetaTipoApoyo("stamina", "tw"))
        assertEquals("Gacha", etiquetaObtencion("gacha", "en"))
        assertEquals("Story Event", etiquetaObtencion("story_event", "en"))
        assertEquals("Limited Quest", etiquetaObtencion("limited_quest", "en"))
        assertEquals("限定クエスト", etiquetaObtencion("limited_quest", "ja"))
        assertEquals("스토리 이벤트", etiquetaObtencion("story_event", "ko"))
        assertEquals("轉蛋", etiquetaObtencion("gacha", "tw"))
        assertEquals("Unique", etiquetaKind("unique", "en"))
        assertEquals("Innate", etiquetaKind("innate", "en"))
        assertEquals("Awakening", etiquetaKind("awakening", "en"))
        assertEquals("固有", etiquetaKind("unique", "ja"))
        assertEquals("각성", etiquetaKind("awakening", "ko"))
        assertEquals("進化", etiquetaKind("evolution", "tw"))
        assertEquals("Velocidad", etiquetaTipoApoyo("speed", "es"))
        assertEquals("Resistencia", etiquetaTipoApoyo("stamina", "es"))
        assertEquals("Potencia", etiquetaTipoApoyo("power", "es"))
        assertEquals("Agallas", etiquetaTipoApoyo("guts", "es"))
        assertEquals("Ingenio", etiquetaTipoApoyo("intelligence", "es"))
        assertEquals("Amistad", etiquetaTipoApoyo("friend", "es"))
        assertEquals("Gacha", etiquetaObtencion("gacha", "es"))
        assertEquals("Historia principal", etiquetaObtencion("main_story", "es"))
        assertEquals("Misión limitada", etiquetaObtencion("limited_quest", "es"))
        assertEquals("Única", etiquetaKind("unique", "es"))
        assertEquals("Innata", etiquetaKind("innate", "es"))
        assertEquals("Despertar", etiquetaKind("awakening", "es"))
        assertEquals("Evolución", etiquetaKind("evolution", "es"))
    }

    /* Lo que no se conoce se deja tal cual, para que se vea que falta: nunca un
       texto inventado, en ningún idioma. */
    @Test
    fun loDesconocidoSeDejaTalCual() {
        for (idioma in IDIOMAS_UI) {
            assertEquals("zz", etiquetaTipo("zz", idioma))
            assertEquals("nuevo_kind", etiquetaKind("nuevo_kind", idioma))
            assertEquals("otro_tipo", etiquetaTipoApoyo("otro_tipo", idioma))
            assertEquals("otra_cosa", etiquetaObtencion("otra_cosa", idioma))
        }
        assertEquals("ZZ", etiquetaIdioma("zz"))
    }

    /* Una rareza que no llega (o rara) sigue siendo el neutro "?", no un texto
       de otro idioma ni la clave. */
    @Test
    fun laRarezaDesconocidaEsElNeutro() {
        assertEquals("?", etiquetaRareza(null, "en"))
        assertEquals("?", etiquetaRareza(null, "ja"))
        assertEquals("?", etiquetaRareza(9, "tw"))
        assertEquals("?", etiquetaTipoApoyo(null, "ko"))
        assertEquals("?", etiquetaObtencion(null, "en"))
    }

    /* Ninguna etiqueta puede quedarse en inglés cuando se pide otro idioma (el
       motor cae al inglés en silencio; esto lo caza). */
    @Test
    fun losCincoIdiomasTienenSuPropioTexto() {
        val codigos = listOf("run", "ldr", "btw", "cha", "sho", "mil", "med", "lng", "dir", "tur", "l_0", "l_1", "l_2", "l_3")
        for (codigo in codigos) {
            val ingles = etiquetaTipo(codigo, "en")
            for (idioma in IDIOMAS_UI.filter { it != "en" }) {
                assertNotEquals("$codigo sin traducir a $idioma", ingles, etiquetaTipo(codigo, idioma))
            }
        }
    }
}
