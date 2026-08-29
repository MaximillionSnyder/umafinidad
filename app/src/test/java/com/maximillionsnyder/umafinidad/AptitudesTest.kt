package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.CharacterDto
import com.maximillionsnyder.umafinidad.data.MemberDto
import com.maximillionsnyder.umafinidad.data.RelationDto
import com.maximillionsnyder.umafinidad.data.deserializarAptitudes
import com.maximillionsnyder.umafinidad.data.jsonParser
import com.maximillionsnyder.umafinidad.data.toDomain
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.aptitudValida
import com.maximillionsnyder.umafinidad.domain.aptitudesDestacadas
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/* Tests de aptitudes (pista / distancia / estilo de la carta base):
   validación del JSON embebido, accessor del modelo y destacadas. */
class AptitudesTest {

    private lateinit var modelo: AffinityModel
    private lateinit var aptitudes: Map<Int, List<String>>

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
        aptitudes = deserializarAptitudes(recurso("data/aptitudes.json"))
    }

    /* ===== Deserialización ===== */

    @Test
    fun clavesConvertidasAInt() {
        assertTrue(aptitudes.isNotEmpty())
        assertTrue(1001 in aptitudes)
        assertTrue(aptitudes.keys.all { it > 0 })
    }

    @Test
    fun todasLasEntradasSonValidas() {
        assertTrue(aptitudes.values.all { aptitudValida(it) })
    }

    /* La tabla cubre a los personajes jugables (tolerancia mínima por
       desfasajes de hasta un día entre los JSON de GameTora). */
    @Test
    fun coberturaDeJugables() {
        val jugables = modelo.personajes.filter { it.playable == true && it.active == true }.map { it.charId }.toSet()
        val extras = aptitudes.keys - jugables
        assertTrue("demasiadas entradas de no jugables: $extras", extras.size <= 5)
        assertTrue(
            "cobertura insuficiente: ${jugables.size - aptitudes.size} jugables sin aptitudes",
            jugables.size - aptitudes.size <= 3,
        )
    }

    /* ===== Accessor del modelo ===== */

    @Test
    fun specialWeekVerificadoContraGameTora() {
        val m = AffinityModel(
            modelo.personajes,
            emptyList(),
            emptyList(),
            aptitudes,
        )
        assertEquals(
            listOf("A", "G", "F", "C", "A", "A", "G", "A", "A", "C"),
            m.aptitudesDe(1001),
        )
    }

    @Test
    fun idDesconocidoOSinTablaDevuelveNull() {
        val m = AffinityModel(modelo.personajes, emptyList(), emptyList(), aptitudes)
        assertNull(m.aptitudesDe(999999999))
        /* Sin la tabla (constructor por defecto) nunca crashea. */
        assertNull(modelo.aptitudesDe(1001))
    }

    /* ===== Destacadas (A o B) ===== */

    @Test
    fun destacadasDeSpecialWeek() {
        assertEquals(
            listOf(0, 4, 5, 7, 8), /* turf, media, larga, vanguardia, remate */
            aptitudesDestacadas(listOf("A", "G", "F", "C", "A", "A", "G", "A", "A", "C")),
        )
    }

    @Test
    fun destacadasIncluyenLaLetraB() {
        assertEquals(
            listOf(1, 2),
            aptitudesDestacadas(listOf("C", "B", "A", "C", "C", "C", "C", "C", "C", "C")),
        )
    }

    @Test
    fun destacadasVaciasCuandoNoDestacaEnNada() {
        assertTrue(aptitudesDestacadas(List(10) { "C" }).isEmpty())
    }

    /* ===== Validación ===== */

    @Test
    fun aptitudInvalidaRechazada() {
        assertTrue(aptitudValida(List(10) { "A" }))
        assertTrue(!aptitudValida(List(9) { "A" }))
        assertTrue(!aptitudValida(List(10) { "S" }))
        assertTrue(!aptitudValida(listOf("A", "G", "F", "C", "A", "A", "G", "A", "A", "AA")))
    }

    /* Especialistas conocidos: la dirt de Smart Falcon (1046) es A y el
       estilo de Twin Turbo (1066) es 逃げ puro. */
    @Test
    fun especialistasConocidos() {
        val smartFalcon = aptitudes[1046]
        assertNotNull(smartFalcon)
        assertEquals("A", smartFalcon!![1])

        val twinTurbo = aptitudes[1066]
        assertNotNull(twinTurbo)
        assertEquals("A", twinTurbo!![6])
        assertEquals("G", twinTurbo[7])
        assertEquals("G", twinTurbo[8])
        assertEquals("G", twinTurbo[9])
    }
}
