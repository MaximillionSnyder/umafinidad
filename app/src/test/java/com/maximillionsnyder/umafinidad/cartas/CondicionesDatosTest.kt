package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.domain.FILAS_CONDICIONES
import com.maximillionsnyder.umafinidad.cartas.domain.IDIOMAS
import com.maximillionsnyder.umafinidad.cartas.domain.IDIOMAS_UI
import com.maximillionsnyder.umafinidad.cartas.domain.leerCondiciones
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/* Guardián de la lectura de las condiciones: recorre TODAS las condiciones del
   asset real y comprueba que ninguna se queda sin traducir en NINGUNO de los
   cuatro idiomas de los datos (en/ja/ko/tw). El español no viene en el asset:
   sus textos son de la interfaz y los cubre la tabla de FILAS_CONDICIONES. La
   ficha enseña igualmente la expresión original de GameTora, así que esto no va
   de no perder el dato: va de que la lectura no se quede a medias. Si GameTora
   añade una clave nueva, "npm run fetch" la mete en el dato y este test cae en
   CI: hay que traducirla en los cuatro idiomas contrastando la descripción
   oficial de las skills que la usan, no a ojo. (Antes de esto, 265 términos de
   87 claves salían tal cual.) */
class CondicionesDatosTest {

    private fun asset(): File = listOf(
        File("src/main/assets/data/skills.json"),
        File("app/src/main/assets/data/skills.json"),
    ).firstOrNull { it.isFile }
        ?: error("no encuentro skills.json (¿desde qué directorio corre el test?)")

    /* Ojo con JsonNull: `elemento.jsonObject` LANZA si el elemento es un
       JsonNull (y geneVersion es null en 1629 de las 1921 skills), así que
       aquí todo va con `as?` en vez de con accesores que revientan. */
    private fun texto(elemento: JsonObject, clave: String): String? =
        (elemento[clave] as? JsonPrimitive)?.contentOrNull

    private fun expresionesDe(skill: JsonObject): List<String> {
        val salida = mutableListOf<String>()
        (skill["conditions"] as? JsonArray)?.forEach { c ->
            val grupo = c as? JsonObject ?: return@forEach
            texto(grupo, "condition")?.let { salida.add(it) }
            texto(grupo, "precondition")?.let { salida.add(it) }
        }
        val gene = skill["geneVersion"] as? JsonObject
        (gene?.get("condition_groups") as? JsonArray)?.forEach { c ->
            val grupo = c as? JsonObject ?: return@forEach
            texto(grupo, "condition")?.let { salida.add(it) }
        }
        return salida
    }

    /* Toda fila de las tablas tiene que estar en los cinco idiomas de la
       interfaz: el motor cae al inglés en silencio y eso dejaría media ficha sin
       traducir. El G1 es la única fila que se escribe igual en los cinco (es el
       nombre de la categoría), así que se comprueba la presencia de la fila, no
       que difiera. */
    @Test
    fun todasLasTablasDeCondicionesEstanEnLosCincoIdiomas() {
        val faltan = FILAS_CONDICIONES.flatMap { (nombre, textos) ->
            IDIOMAS_UI.filter { textos[it].isNullOrBlank() }.map { "$nombre ($it)" }
        }
        assertTrue(
            "condiciones sin fila en algún idioma:\n" + faltan.joinToString("\n"),
            faltan.isEmpty(),
        )
        assertTrue("no se leyó ninguna tabla", FILAS_CONDICIONES.size > 150)
    }

    @Test
    fun todasLasCondicionesDelDatoSalenTraducidas() {
        val raiz = Json.parseToJsonElement(asset().readText()) as? JsonArray
            ?: error("el asset no es una lista de skills")
        for (idioma in IDIOMAS) {
            val crudos = linkedMapOf<String, String>()
            var terminos = 0
            for (elemento in raiz) {
                val skill = elemento as? JsonObject ?: continue
                val nombre = (skill["name"] as? JsonObject)?.let { texto(it, "en") } ?: "?"
                for (expresion in expresionesDe(skill)) {
                    for (termino in leerCondiciones(expresion, idioma)) {
                        terminos++
                        if (!termino.traducido && !crudos.containsKey(termino.texto)) {
                            crudos[termino.texto] = nombre
                        }
                    }
                }
            }
            // Si el asset se leyera a medias, el test pasaría por vacío.
            assertTrue(
                "sólo se leyeron $terminos términos en $idioma: ¿asset incompleto?",
                terminos > 5000,
            )
            assertTrue(
                "hay ${crudos.size} condiciones sin lectura en $idioma:\n" +
                    crudos.entries.joinToString("\n") { "  ${it.key}   (p.ej. ${it.value})" },
                crudos.isEmpty(),
            )
        }
    }

    /* Cada idioma tiene que decir algo distinto del inglés: si una clave se
       quedó sin su columna, el motor devuelve el inglés y la ficha se vería a
       medias. Se admiten las filas que son un símbolo o un nombre propio igual
       en los cuatro (los valores numéricos y G1), así que sólo se revisan los
       términos que llevan letras. */
    @Test
    fun ningunIdiomaSeQuedaEnIngles() {
        val raiz = Json.parseToJsonElement(asset().readText()) as? JsonArray
            ?: error("el asset no es una lista de skills")
        val iguales = linkedMapOf<String, String>()
        for (elemento in raiz) {
            val skill = elemento as? JsonObject ?: continue
            val nombre = (skill["name"] as? JsonObject)?.let { texto(it, "en") } ?: "?"
            for (expresion in expresionesDe(skill)) {
                val en = leerCondiciones(expresion, "en").map { it.texto }
                for (idioma in IDIOMAS.filter { it != "en" }) {
                    val otro = leerCondiciones(expresion, idioma).map { it.texto }
                    for ((indice, terminoEn) in en.withIndex()) {
                        val traducido = otro.getOrNull(indice) ?: continue
                        if (terminoEn == traducido && terminoEn.any { it.isLetter() }) {
                            val clave = "$terminoEn ($idioma)"
                            if (!iguales.containsKey(clave)) iguales[clave] = nombre
                        }
                    }
                }
            }
        }
        assertTrue(
            "términos que se quedan en inglés:\n" +
                iguales.entries.joinToString("\n") { "  ${it.key}   (p.ej. ${it.value})" },
            iguales.isEmpty(),
        )
    }
}
