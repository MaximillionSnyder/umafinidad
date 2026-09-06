package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.CharacterDto
import com.maximillionsnyder.umafinidad.data.MemberDto
import com.maximillionsnyder.umafinidad.data.RelationDto
import com.maximillionsnyder.umafinidad.data.jsonParser
import com.maximillionsnyder.umafinidad.data.toDomain
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/* Ranking “Mejores padres”: cada hijo aporta su linaje óptimo exacto y
   cada uma cuenta cuántas veces sale como padre/madre. */
class RankingPadresTest {

    private lateinit var modelo: AffinityModel
    private lateinit var ids: List<Int>

    private fun recurso(ruta: String): String =
        javaClass.getResourceAsStream("/$ruta")!!.bufferedReader().use { it.readText() }

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

    @Test
    fun cubreTodoElPool() {
        val ranking = modelo.rankingPadres()
        assertEquals(ids.size, ranking.size)
        assertEquals(ids.sorted(), ranking.map { it.personaje.charId }.sorted())
    }

    @Test
    fun cadaLinajeAportaDosPadres() {
        val ranking = modelo.rankingPadres()
        // Cada hijo con linaje computable aporta exactamente padre + madre.
        assertEquals(2 * ids.size, ranking.sumOf { it.veces })
    }

    @Test
    fun porcentajeCoherenteConVeces() {
        val ranking = modelo.rankingPadres()
        for (entry in ranking) {
            assertEquals(entry.veces * 100f / ids.size, entry.porcentaje, 0.001f)
            assertTrue("veces de ${entry.personaje.charId} en rango", entry.veces in 0 until ids.size)
            assertTrue(entry.puntosMedios >= 0)
        }
    }

    @Test
    fun ordenDeterminista() {
        val primero = modelo.rankingPadres()
        val segundo = modelo.rankingPadres()
        assertEquals(primero, segundo)
        assertTrue(primero.zipWithNext().all { (a, b) ->
            if (a.veces != b.veces) a.veces > b.veces
            else if (a.puntosMedios != b.puntosMedios) a.puntosMedios > b.puntosMedios
            else if (a.totalAfinidad != b.totalAfinidad) a.totalAfinidad > b.totalAfinidad
            else a.personaje.charId <= b.personaje.charId
        })
    }

    @Test
    fun recuentoIndependienteCoincide() {
        // Recuenta desde fuera con mejorLinajeDe y compara con el ranking.
        val conteo = HashMap<Int, Int>()
        val puntos = HashMap<Int, MutableList<Int>>()
        for (hijoId in ids) {
            val linaje = modelo.mejorLinajeDe(hijoId) ?: continue
            for (padreId in listOf(linaje.padre.charId, linaje.madre.charId)) {
                conteo[padreId] = (conteo[padreId] ?: 0) + 1
                puntos.getOrPut(padreId) { mutableListOf() }.add(linaje.puntos)
            }
        }
        val ranking = modelo.rankingPadres().associateBy { it.personaje.charId }
        for (id in ids) {
            val entry = ranking.getValue(id)
            assertEquals("veces de $id", conteo[id] ?: 0, entry.veces)
            val esperadaMedia = puntos[id]?.let { it.sum() / it.size } ?: 0
            assertEquals("media de $id", esperadaMedia, entry.puntosMedios)
        }
    }
}
