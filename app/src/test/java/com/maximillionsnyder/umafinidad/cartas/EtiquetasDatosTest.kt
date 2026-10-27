package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.domain.FILAS_ETIQUETAS
import com.maximillionsnyder.umafinidad.cartas.domain.IDIOMAS
import com.maximillionsnyder.umafinidad.cartas.domain.IDIOMAS_UI
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaIdioma
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaKind
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaObtencion
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaRareza
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaTipo
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaTipoApoyo
import com.maximillionsnyder.umafinidad.cartas.domain.texto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/* Guardián de las etiquetas de ficha (paso 2 del repaso de descripciones): los
   códigos que trae GameTora tienen que salir traducidos en los cinco idiomas de
   la interfaz (los cuatro del dato y el español), y ninguna carta puede quedarse
   sin nombre en ninguno de los idiomas del dato. Si un "npm run fetch" trae un
   código nuevo (o una carta sin nombre en los cuatro idiomas), esto cae en CI en
   vez de que lo vea el usuario. */
class EtiquetasDatosTest {

    private fun asset(nombre: String): JsonElement {
        val fichero = listOf(
            File("src/main/assets/data/$nombre"),
            File("app/src/main/assets/data/$nombre"),
        ).firstOrNull { it.isFile }
            ?: error("no encuentro $nombre (¿desde qué directorio corre el test?)")
        return Json.parseToJsonElement(fichero.readText())
    }

    /* skills.json es una lista; las cartas son objetos indexados por id. */
    private fun objetos(nombre: String): List<JsonObject> = when (val raiz = asset(nombre)) {
        is JsonArray -> raiz.mapNotNull { it as? JsonObject }
        is JsonObject -> raiz.values.mapNotNull { it as? JsonObject }
        else -> error("$nombre no es lista ni objeto")
    }

    private fun textoDe(obj: JsonObject, clave: String): String? =
        (obj[clave] as? JsonPrimitive)?.contentOrNull

    private fun textosDe(obj: JsonObject, clave: String): List<String> =
        (obj[clave] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull } ?: emptyList()

    private fun mapaDe(obj: JsonObject, clave: String): Map<String, String?> =
        (obj[clave] as? JsonObject)?.mapValues { (_, v) -> (v as? JsonPrimitive)?.contentOrNull }
            ?: emptyMap()

    /* Un código está sin traducir si la función devuelve el propio código (lo que
       hace el motor cuando no encuentra la fila) o si se queda en inglés cuando
       se pide otro idioma (señal de una tabla a la que le falta la columna).
       Mira solo IDIOMAS porque son los idiomas del dato: las etiquetas ya traen
       español y el español lo cubre EtiquetasTest. */
    private fun sinTraducir(codigos: List<Pair<String, String>>, traducir: (String, String) -> String): List<String> =
        codigos.flatMap { (de, codigo) ->
            IDIOMAS.filter { idioma ->
                val texto = traducir(codigo, idioma)
                texto == codigo || (idioma != "en" && texto == traducir(codigo, "en"))
            }.map { idioma -> "$codigo ($de, $idioma)" }
        }.distinct()

    /* Toda fila de las tablas tiene que estar en los cinco idiomas de la
       interfaz: el motor cae al inglés en silencio y eso dejaría media app sin
       traducir. */
    @Test
    fun todasLasEtiquetasEstanEnLosCincoIdiomas() {
        val faltan = FILAS_ETIQUETAS.flatMap { (nombre, textos) ->
            IDIOMAS_UI.filter { textos[it].isNullOrBlank() }.map { "$nombre ($it)" }
        }
        assertTrue(
            "etiquetas sin fila en algún idioma:\n" + faltan.joinToString("\n"),
            faltan.isEmpty(),
        )
        assertTrue("no se leyó ninguna etiqueta", FILAS_ETIQUETAS.size > 40)
    }

    @Test
    fun losCodigosDeLasSkillsSalenTraducidos() {
        val tipos = mutableListOf<Pair<String, String>>()
        val kinds = mutableListOf<Pair<String, String>>()
        val idiomas = mutableListOf<Pair<String, String>>()
        for (skill in objetos("skills.json")) {
            val nombre = textoDe(skill, "id") ?: "?"
            tipos += textosDe(skill, "types").map { nombre to it }
            idiomas += textosDe(skill, "unreleased").map { nombre to it }
            val fuentes = skill["sources"] as? JsonObject
            (fuentes?.get("characterCards") as? JsonArray)?.forEach { f ->
                val kindsDeLaFuente = (f as? JsonObject)?.get("kinds") as? JsonArray
                kindsDeLaFuente?.forEach { k ->
                    (k as? JsonPrimitive)?.contentOrNull?.let { kinds += nombre to it }
                }
            }
        }
        assertTrue("no se leyó ningún tipo: ¿asset incompleto?", tipos.size > 1000)
        assertTrue(
            "tipos de skill sin traducir:\n" + sinTraducir(tipos, ::etiquetaTipo).joinToString("\n"),
            sinTraducir(tipos, ::etiquetaTipo).isEmpty(),
        )
        assertTrue(
            "kinds sin traducir:\n" + sinTraducir(kinds, ::etiquetaKind).joinToString("\n"),
            sinTraducir(kinds, ::etiquetaKind).isEmpty(),
        )
        /* Los idiomas no se dejan "tal cual": tienen que ser una de las cuatro
           etiquetas conocidas (el dato usa zh_tw, no tw). */
        val conocidas = setOf("EN", "日本語", "한국어", "中文")
        val raros = idiomas.filter { etiquetaIdioma(it.second) !in conocidas }
            .map { "${it.second} (${it.first})" }
        assertTrue("códigos de idioma sin etiqueta:\n" + raros.joinToString("\n"), raros.isEmpty())
    }

    @Test
    fun lasCartasDeApoyoTraducenTipoYObtencion() {
        val cartas = objetos("support-cards.json")
        assertTrue("no se leyó ninguna carta de apoyo", cartas.size > 500)
        val tipos = cartas.mapNotNull { c -> textoDe(c, "type")?.let { (textoDe(c, "supportId") ?: "?") to it } }
        val obtenciones = cartas.mapNotNull { c -> textoDe(c, "obtained")?.let { (textoDe(c, "supportId") ?: "?") to it } }
        assertTrue(
            "tipos de apoyo sin traducir:\n" + sinTraducir(tipos, ::etiquetaTipoApoyo).joinToString("\n"),
            sinTraducir(tipos, ::etiquetaTipoApoyo).isEmpty(),
        )
        assertTrue(
            "obtenciones de apoyo sin traducir:\n" + sinTraducir(obtenciones, ::etiquetaObtencion).joinToString("\n"),
            sinTraducir(obtenciones, ::etiquetaObtencion).isEmpty(),
        )
    }

    @Test
    fun lasCartasDePersonajeTraducenObtencion() {
        val cartas = objetos("character-cards.json")
        assertTrue("no se leyó ninguna carta de personaje", cartas.size > 200)
        val obtenciones = cartas.mapNotNull { c -> textoDe(c, "obtained")?.let { (textoDe(c, "cardId") ?: "?") to it } }
        assertTrue(
            "obtenciones de personaje sin traducir:\n" + sinTraducir(obtenciones, ::etiquetaObtencion).joinToString("\n"),
            sinTraducir(obtenciones, ::etiquetaObtencion).isEmpty(),
        )
    }

    /* Las rarezas que usan los assets (1 a 6) tienen nombre en los cinco idiomas
       de la interfaz; el "?" solo vale para una rareza que no llegue. */
    @Test
    fun lasRarezasDelDatoTienenNombre() {
        val rarezas = mutableSetOf<Int>()
        for (fichero in listOf("skills.json", "character-cards.json", "support-cards.json")) {
            for (objeto in objetos(fichero)) {
                textoDe(objeto, "rarity")?.toIntOrNull()?.let { rarezas += it }
            }
        }
        assertTrue("no se leyó ninguna rareza", rarezas.isNotEmpty())
        val sinNombre = rarezas.flatMap { rareza ->
            IDIOMAS_UI.filter { idioma -> etiquetaRareza(rareza, idioma) == "?" }
                .map { "rareza $rareza ($it)" }
        }
        assertTrue("rarezas sin nombre:\n" + sinNombre.joinToString("\n"), sinNombre.isEmpty())
    }

    /* El nombre se enseña con respaldo (pedido → EN → JA): ninguna carta de
       ninguna tabla puede quedarse sin nombre en ninguno de los idiomas. */
    @Test
    fun ningunaCartaSeQuedaSinNombre() {
        val sinNombre = mutableListOf<String>()
        for (fichero in listOf("character-cards.json", "support-cards.json", "scenarios.json")) {
            for (carta in objetos(fichero)) {
                val id = textoDe(carta, "cardId") ?: textoDe(carta, "supportId") ?: textoDe(carta, "id") ?: "?"
                val nombre = mapaDe(carta, "name")
                for (idioma in IDIOMAS) {
                    if (nombre.texto(idioma).isNullOrBlank()) sinNombre += "$fichero $id ($idioma)"
                }
            }
        }
        assertTrue("cartas sin nombre mostrable:\n" + sinNombre.joinToString("\n"), sinNombre.isEmpty())
    }
}
