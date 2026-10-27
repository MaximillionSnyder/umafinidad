package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.domain.IDIOMAS_UI
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.TX_PANTALLAS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/* Las pantallas de listas y fichas leen todos sus textos de TX_PANTALLAS. Si a
   una clave le falta una lengua, o si una traducción se deja un marcador por el
   camino, la pantalla sale a medias justo en el idioma que nadie revisa. Esto
   lo caza sin abrir la app: recorre la tabla entera y compara las cinco
   lenguas entre sí. */
class TextosPantallasTest {

    private val idiomas = IDIOMAS_UI

    /* %1$s, %2$s...: el "$" se pinta con una expresión para no abrir plantilla
       de cadena, y entre corchetes para que en el patrón sea un dólar literal. */
    private val marcador = Regex("""%(\d+)[${'$'}][sd]""")

    @Test
    fun cadaClaveTieneLasCincoLenguasYNingunaEstaEnBlanco() {
        val claves = TX_PANTALLAS.claves
        assertTrue("la tabla de textos de las pantallas está vacía", claves.isNotEmpty())
        for (clave in claves) {
            for (idioma in idiomas) {
                val texto = TX_PANTALLAS[clave, idioma]
                assertTrue("«$clave» no tiene texto en «$idioma»", texto.isNotBlank())
            }
        }
    }

    /* Una clave con %1$s y otra con %2$s en la misma tabla es una trampa: el
       primer idioma que se descuide pide un argumento que nadie le pasa. */
    @Test
    fun losMarcadoresDeUnaClaveSonLosMismosEnLasCincoLenguas() {
        for (clave in TX_PANTALLAS.claves) {
            /* Ordenados porque cada lengua coloca los argumentos donde quiere
               («%2$s件中%1$s件» es correcto en japonés). */
            val referencia = marcadores(TX_PANTALLAS[clave, "en"]).sorted()
            assertEquals(
                "«$clave» no numera sus argumentos del 1 al ${referencia.size}",
                (1..referencia.size).toSet(),
                referencia.toSet(),
            )
            for (idioma in idiomas) {
                assertEquals(
                    "«$clave» usa argumentos distintos en «$idioma»",
                    referencia,
                    marcadores(TX_PANTALLAS[clave, idioma]).sorted(),
                )
            }
        }
    }

    @Test
    fun lasPlantillasNoDejanPorcentajesSueltos() {
        for (clave in TX_PANTALLAS.claves) {
            for (idioma in idiomas) {
                val resto = TX_PANTALLAS[clave, idioma].replace(marcador, "")
                assertTrue(
                    "«$clave» en «$idioma» tiene un % que no es un marcador",
                    '%' !in resto,
                )
            }
        }
    }

    /* Las claves son la mitad del contrato con las pantallas: si una se cuela en
       CamelCase o con espacios, la llamada de la pantalla no la encuentra. */
    @Test
    fun lasClavesSiguenElFormatoSnakeCase() {
        val formato = Regex("""^[a-z0-9]+(_[a-z0-9]+)*$""")
        for (clave in TX_PANTALLAS.claves) {
            assertTrue("«$clave» no está en snake_case", formato.matches(clave))
        }
    }

    /* El español viene de su propia columna: si alguien la pisa copiando el
       inglés, el test de cobertura no lo nota (la clave tendría texto en «es»). */
    @Test
    fun elEspanolTieneSuPropiaColumna() {
        assertEquals("Cartas de apoyo", TX_PANTALLAS["titulo_apoyos", "es"])
        assertEquals("5 skills", TX_PANTALLAS["contador_skills", "es"].format("5"))
        assertEquals("Duración base: 12 s", TX_PANTALLAS["duracion_base", "es"].format("12 s"))
    }

    private fun marcadores(texto: String): List<Int> =
        marcador.findAll(texto).map { it.groupValues[1].toInt() }.toList()
}
