package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.CharacterDto
import com.maximillionsnyder.umafinidad.data.MemberDto
import com.maximillionsnyder.umafinidad.data.RelationDto
import com.maximillionsnyder.umafinidad.data.jsonParser
import com.maximillionsnyder.umafinidad.data.toDomain
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.GrupoCompartido
import com.maximillionsnyder.umafinidad.domain.GrupoInfo
import com.maximillionsnyder.umafinidad.domain.Rango
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.TipoVinculo
import com.maximillionsnyder.umafinidad.domain.armarArbol
import com.maximillionsnyder.umafinidad.domain.puedeIrEn
import com.maximillionsnyder.umafinidad.domain.slotPara
import com.maximillionsnyder.umafinidad.domain.vinculos
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/* Tests de paridad contra la lógica JS original de la PWA.
   Los fixtures se generaron con scripts/generate-fixtures.mjs ejecutando
   affinity.js/herencia.js reales sobre estos mismos datos datamined. */

@Serializable
private data class RangoDto(val simbolo: String, val clase: String)

@Serializable
private data class RangosDto(val par: RangoDto?, val total: RangoDto)

@Serializable
private data class GrupoCompartidoDto(val tipo: Int, val puntos: Int)

private fun GrupoCompartidoDto.toDomain() = GrupoCompartido(tipo, puntos)

@Serializable
private data class GrupoInfoDto(val tipo: Int, val puntos: Int, val miembros: List<String>)

private fun GrupoInfoDto.toDomain() = GrupoInfo(tipo, puntos, miembros)

@Serializable
private data class GrupoResumenDto(val tipo: Int, val puntos: Int, val cantidad: Int)

@Serializable
private data class CharResumenDto(@SerialName("char_id") val charId: Int, @SerialName("en_name") val enName: String?)

@Serializable
private data class LinajeDto(
    val hijo: CharResumenDto,
    val padre: CharResumenDto,
    val madre: CharResumenDto,
    val abuelos: List<List<CharResumenDto>>,
    val puntos: Int,
)

@Serializable
private data class VinculoDto(val tipo: String, val ids: List<Int>, val esCorredora: Boolean = false)

class ParidadTest {

    private lateinit var modelo: AffinityModel
    private lateinit var ids: List<Int>

    private fun recurso(ruta: String): String =
        javaClass.getResourceAsStream("/$ruta")!!.bufferedReader().use { it.readText() }

    private inline fun <reified T> fixture(nombre: String): T =
        jsonParser.decodeFromString(recurso("fixtures/$nombre"))

    @Before
    fun preparar() {
        val characters = jsonParser.decodeFromString<List<CharacterDto>>(recurso("data/characters.json"))
        val relations = jsonParser.decodeFromString<List<RelationDto>>(recurso("data/succession_relation.json"))
        val members = jsonParser.decodeFromString<List<MemberDto>>(recurso("data/succession_relation_member.json"))
        modelo = AffinityModel(
            characters.map { it.toDomain() },
            relations.map { it.toDomain() },
            members.map { it.toDomain() },
        )
        ids = characters.map { it.toDomain() }
            .filter { it.playable == true && it.active == true }
            .map { it.charId }
    }

    /* ===== Afinidad ===== */

    @Test
    fun puntajeParCoincideConWeb() {
        val pares = fixture<List<List<Int>>>("pares.json")
        for ((a, b, esperado) in pares) {
            assertEquals("puntajePar($a,$b)", esperado, modelo.puntajePar(a, b))
        }
    }

    @Test
    fun puntajeTrioCoincideConWeb() {
        val trios = fixture<List<List<Int>>>("trios.json")
        for ((a, b, c, esperado) in trios) {
            assertEquals("puntajeTrio($a,$b,$c)", esperado, modelo.puntajeTrio(a, b, c))
        }
    }

    @Test
    fun gruposCompartidosCoincidenConWeb() {
        val casos = fixture<Map<String, List<GrupoCompartidoDto>>>("grupos_compartidos.json")
        for ((clave, esperados) in casos) {
            val (a, b) = clave.split("-").map { it.toInt() }
            assertEquals(clave, esperados.map { it.toDomain() }, modelo.gruposCompartidos(listOf(a, b)))
        }
    }

    @Test
    fun rangosCoincidenConWeb() {
        val rangos = fixture<Map<String, RangosDto>>("rangos.json")
        for ((valor, esperado) in rangos) {
            val v = valor.toInt()
            assertEquals("rango($v)", esperado.par?.let { Rango(it.simbolo, it.clase) }, modelo.rango(v))
            assertEquals("rangoTotal($v)", Rango(esperado.total.simbolo, esperado.total.clase), modelo.rangoTotal(v))
        }
    }

    @Test
    fun gruposDeCharCoincidenConWeb() {
        val casos = fixture<Map<String, List<GrupoInfoDto>>>("grupos_char.json")
        for ((id, esperados) in casos) {
            assertEquals(id, esperados.map { it.toDomain() }, modelo.gruposDeChar(id.toInt()))
        }
    }

    @Test
    fun todosLosGruposCoincidenConWeb() {
        val esperados = fixture<List<GrupoResumenDto>>("todos_grupos.json")
        val actual = modelo.todosLosGrupos()
        assertEquals(esperados.size, actual.size)
        for (i in esperados.indices) {
            val g = actual[i]
            assertEquals("todosLosGrupos[$i]", esperados[i], GrupoResumenDto(g.tipo, g.puntos, g.cantidad))
        }
    }

    @Test
    fun topLinajesCoincidenConWeb() {
        val esperados = fixture<List<LinajeDto>>("top_linajes.json")
        val actual = modelo.topLinajes(10)
        assertEquals(esperados.size, actual.size)
        for (i in esperados.indices) {
            val e = esperados[i]
            val a = actual[i]
            assertEquals("top[$i] hijo", e.hijo.charId, a.hijo.charId)
            assertEquals("top[$i] padre", e.padre.charId, a.padre.charId)
            assertEquals("top[$i] madre", e.madre.charId, a.madre.charId)
            assertEquals("top[$i] abuelos", e.abuelos.map { r -> r.map { it.charId } }, a.abuelos.map { r -> r.map { it.charId } })
            assertEquals("top[$i] puntos", e.puntos, a.puntos)
            assertEquals(e.hijo.enName, a.hijo.enName)
        }
    }

    /* ===== Herencia ===== */

    /* Mismas selecciones base que usa el generador de fixtures. */
    private fun seleccionesBase(): List<Array<Int?>> {
        val sel0 = arrayOfNulls<Int?>(SLOTS)
        val sel1 = arrayOfNulls<Int?>(SLOTS).also {
            it[0] = ids[0]; it[1] = ids[1]; it[2] = ids[2]
        }
        val sel2 = arrayOfNulls<Int?>(SLOTS).also {
            it[0] = ids[0]; it[1] = ids[1]; it[2] = ids[2]
            it[3] = ids[0]; it[4] = ids[3]; it[5] = ids[4]; it[6] = ids[5]
        }
        return listOf(sel0, sel1, sel2)
    }

    @Test
    fun puedeIrEnCoincideConWeb() {
        val filas = jsonParser.decodeFromString<JsonArray>(recurso("fixtures/puede_ir_en.json"))
        val selecciones = seleccionesBase()
        assertEquals(231, filas.size) /* 3 selecciones × 7 slots × 11 candidatos */
        for (filaJson in filas) {
            val fila = filaJson as JsonArray
            val selIdx = fila[0].jsonPrimitive.int; val slot = fila[1].jsonPrimitive.int; val idReal = fila[2].jsonPrimitive.int; val esperado = fila[3].jsonPrimitive.boolean
            assertEquals(
                "puedeIrEn(sel$selIdx, slot$slot, $idReal)",
                esperado,
                puedeIrEn(selecciones[selIdx], slot, idReal),
            )
        }
    }

    @Test
    fun slotParaCoincideConWeb() {
        val filas = jsonParser.decodeFromString<JsonArray>(recurso("fixtures/slot_para.json"))
        val selecciones = seleccionesBase()
        for (filaJson in filas) {
            val fila = filaJson as JsonArray
            val selIdx = fila[0].jsonPrimitive.int; val idReal = fila[1].jsonPrimitive.int; val esperado = fila[2].jsonPrimitive.int
            assertEquals(
                "slotPara(sel$selIdx, $idReal)",
                esperado,
                slotPara(selecciones[selIdx], idReal),
            )
        }
    }

    @Test
    fun vinculosCoincidenConWeb() {
        val esperadosPorSeleccion = fixture<List<List<VinculoDto>>>("vinculos.json")
        val selecciones = seleccionesBase()
        for ((i, esperados) in esperadosPorSeleccion.withIndex()) {
            val actual = vinculos(armarArbol(selecciones[i])).map { v ->
                VinculoDto(
                    tipo = when (v.tipo) {
                        TipoVinculo.HIJO_PADRE -> "hijo-padre"
                        TipoVinculo.ENTRE_PADRES -> "entre-padres"
                        TipoVinculo.HIJO_PADRE_ABUELO -> "hijo-padre-abuelo"
                    },
                    ids = v.ids,
                    esCorredora = v.esCorredora,
                )
            }
            assertEquals("vinculos(sel$i)", esperados, actual)
        }
    }
}
