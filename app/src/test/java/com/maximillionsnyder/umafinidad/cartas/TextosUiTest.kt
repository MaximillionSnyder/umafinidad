package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.domain.IDIOMAS_UI
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.ENTRADAS_TX_UI
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.TX_UI
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.textoUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/* Guardián de los textos de la interfaz: la app cambia de idioma entera, así
   que ninguna clave puede quedarse sin alguna de las cinco lenguas, ni con un
   hueco de formato distinto entre ellas (`%1$s` en una y `%1$d` en otra), ni con
   un `%` suelto que reviente `String.format` al pintar la fila. */
class TextosUiTest {

    private val idiomas = IDIOMAS_UI

    /* Los huecos siempre van numerados porque el orden de los argumentos cambia
       entre lenguas (en japonés el número va detrás del nombre). */
    private val hueco = Regex("""%(\d+)\$[sd]""")

    private fun sinHuecos(texto: String): String = hueco.replace(texto, "")

    private fun maximaReferencia(texto: String): Int =
        hueco.findAll(texto).map { it.groupValues[1].toInt() }.maxOrNull() ?: 0

    @Test
    fun cadaClaveTieneLasCincoLenguasYNingunaEnBlanco() {
        for ((clave, textos) in ENTRADAS_TX_UI) {
            for (idioma in idiomas) {
                val texto = textos[idioma]
                assertTrue("Falta el texto de «$clave» en «$idioma»", texto != null)
                assertTrue("El texto de «$clave» en «$idioma» está en blanco", !texto.isNullOrBlank())
            }
        }
    }

    @Test
    fun noSobranIdiomasNiFaltanClaves() {
        for ((clave, textos) in ENTRADAS_TX_UI) {
            assertEquals("«$clave» no trae justo en/ja/ko/tw/es", idiomas.toSet(), textos.keys)
        }
        assertTrue("La tabla de textos está vacía", ENTRADAS_TX_UI.isNotEmpty())
    }

    /* Una clave repetida no da error en Kotlin: la segunda entrada se come a la
       primera sin avisar. Por eso la tabla es una lista de pares. */
    @Test
    fun noHayClavesRepetidas() {
        val repetidas = ENTRADAS_TX_UI.groupBy { it.first }.filterValues { it.size > 1 }.keys
        assertTrue("Claves repetidas en TX_UI: $repetidas", repetidas.isEmpty())
    }

    @Test
    fun lasCincoLenguasTienenLosMismosHuecos() {
        for ((clave, textos) in ENTRADAS_TX_UI) {
            val referencia = hueco.findAll(textos.getValue("en")).map { it.value }.sorted().toList()
            for (idioma in idiomas) {
                val actuales = hueco.findAll(textos.getValue(idioma)).map { it.value }.sorted().toList()
                assertEquals("«$clave» pide otros argumentos en «$idioma»", referencia, actuales)
            }
        }
    }

    /* Un `%` que no abre un hueco válido ("100% de…") hace saltar una excepción
       en String.format y se lleva por delante la pantalla. */
    @Test
    fun noQuedanPorcentajesSueltos() {
        for ((clave, textos) in ENTRADAS_TX_UI) {
            for (idioma in idiomas) {
                val resto = sinHuecos(textos.getValue(idioma))
                assertTrue("«$clave» en «$idioma» tiene un % suelto: $resto", !resto.contains('%'))
            }
        }
    }

    @Test
    fun lasPlantillasSeFormateanConSusArgumentos() {
        for ((clave, textos) in ENTRADAS_TX_UI) {
            for (idioma in idiomas) {
                val texto = textos.getValue(idioma)
                val cuantos = maximaReferencia(texto)
                if (cuantos == 0) continue
                val args: Array<Any?> = Array(cuantos) { "7" }
                val salida = String.format(Locale.US, texto, *args)
                assertTrue("«$clave» en «$idioma» no rellena sus huecos: $salida", salida.contains("7"))
            }
        }
    }

    /* La tabla que lee la app es la misma que se prueba aquí: así una errata en
       el envoltorio de Textos no deja la comprobación mirando a otro sitio. */
    @Test
    fun laTablaQueConsumeLaAppEsLaQueSePrueba() {
        for ((clave, textos) in ENTRADAS_TX_UI) {
            for (idioma in idiomas) {
                assertEquals("TX_UI no devuelve el texto de «$clave» en «$idioma»", textos.getValue(idioma), TX_UI[clave, idioma])
            }
        }
    }

    /* Las llamadas fuera de la composición (Toasts, reporte de crash) usan
       `textoUi`: tienen que interpolar igual que `t`. */
    @Test
    fun textoUiInterpolaComoLaPantalla() {
        assertEquals("Skills", textoUi("tab_skills", "en"))
        assertEquals("スキル", textoUi("tab_skills", "ja"))
        assertEquals("7 skills", textoUi("skills_varios", "en", 7))
        assertEquals("스킬 7개", textoUi("skills_varios", "ko", 7))
        assertEquals("Skills", textoUi("tab_skills", "es"))
        assertEquals("7 skills", textoUi("skills_varios", "es", 7))
    }
}
