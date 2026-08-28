package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.CharacterDto
import com.maximillionsnyder.umafinidad.data.MemberDto
import com.maximillionsnyder.umafinidad.data.RelationDto
import com.maximillionsnyder.umafinidad.data.deserializarElenco
import com.maximillionsnyder.umafinidad.data.jsonParser
import com.maximillionsnyder.umafinidad.data.serializarElenco
import com.maximillionsnyder.umafinidad.data.toDomain
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Linaje
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/* Tests del elenco personal: persistencia pura y top de linajes
   restringido a los personajes que el usuario posee. */
class ElencoTest {

    private lateinit var modelo: AffinityModel

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
    }

    private fun idsJugables(): List<Int> =
        modelo.personajes.filter { it.playable == true && it.active == true }.map { it.charId }

    /* ===== Persistencia pura ===== */

    @Test
    fun elencoVacioRoundTrip() {
        assertEquals(emptySet<Int>(), deserializarElenco(serializarElenco(emptySet())))
    }

    @Test
    fun elencoConIdsRoundTrip() {
        val ids = setOf(1001, 1015, 1042)
        assertEquals(ids, deserializarElenco(serializarElenco(ids)))
    }

    @Test
    fun deserializacionIgnoraElOrdenDeInsercion() {
        assertEquals(setOf(1, 2, 3), deserializarElenco(serializarElenco(linkedSetOf(3, 1, 2))))
    }

    /* ===== topLinajesDeElenco ===== */

    /* Invariante fuerte del refactor: el elenco completo (todas las umas
       jugables) debe reproducir EXACTAMENTE el top global. */
    @Test
    fun elencoCompletoCoincideConElTopGlobal() {
        val ids = idsJugables().toSet()
        assertEquals(modelo.topLinajes(20), modelo.topLinajesDeElenco(ids, 20))
    }

    @Test
    fun todosLosMiembrosPertenecenAlElenco() {
        val ids = idsJugables().take(40).toSet()
        val top = modelo.topLinajesDeElenco(ids, 20)
        assertTrue(top.isNotEmpty())
        for (l in top) {
            val miembros = listOf(l.hijo, l.padre, l.madre) + l.abuelos.flatten()
            for (m in miembros) assertTrue("miembro ${m.charId} fuera del elenco", m.charId in ids)
        }
    }

    @Test
    fun respetaLasReglasDelJuego() {
        val ids = idsJugables().take(40).toSet()
        for (l in modelo.topLinajesDeElenco(ids, 20)) {
            assertTrue("padres distintos", l.padre.charId != l.madre.charId)
            assertTrue(
                "el hijo no puede ser padre",
                l.hijo.charId != l.padre.charId && l.hijo.charId != l.madre.charId,
            )
            val padres = listOf(l.padre.charId, l.madre.charId)
            l.abuelos.forEachIndexed { rama, abuelos ->
                for (a in abuelos) assertTrue("abuelo ≠ padre de su rama", a.charId != padres[rama])
                assertTrue("abuelos distintos en la rama", abuelos[0].charId != abuelos[1].charId)
            }
        }
    }

    @Test
    fun elencoInsuficienteDevuelveVacio() {
        assertEquals(emptyList<Linaje>(), modelo.topLinajesDeElenco(emptySet()))
        assertEquals(emptyList<Linaje>(), modelo.topLinajesDeElenco(setOf(idsJugables()[0], idsJugables()[1])))
    }

    @Test
    fun idsNoJugablesSeIgnoran() {
        val validos = idsJugables().take(5).toSet()
        val conInvalidos = validos + setOf(999999999, 888888888)
        val top = modelo.topLinajesDeElenco(conInvalidos, 20)
        assertTrue(top.isNotEmpty())
        for (l in top) {
            val miembros = listOf(l.hijo, l.padre, l.madre) + l.abuelos.flatten()
            for (m in miembros) assertTrue(m.charId in validos)
        }
    }

    @Test
    fun ordenDescendentePorPuntos() {
        val ids = idsJugables().take(60).toSet()
        val top = modelo.topLinajesDeElenco(ids, 20)
        for (i in 1 until top.size) assertTrue("fila $i", top[i - 1].puntos >= top[i].puntos)
    }

    @Test
    fun determinismo() {
        val ids = idsJugables().take(30).toSet()
        assertEquals(modelo.topLinajesDeElenco(ids, 10), modelo.topLinajesDeElenco(ids, 10))
    }

    /* Con exactamente 3 personajes hay un solo triple y por lo tanto
       exactamente 3 candidatos (uno por asignación de roles). */
    @Test
    fun elencoExactoDeTresGeneraTresCandidatos() {
        val ids = idsJugables().take(3).toSet()
        assertEquals(3, modelo.topLinajesDeElenco(ids, 20).size)
    }

    /* El cálculo sobre un elenco usa cache local: no debe contaminar el
       cache global del top ni alterar el resultado de topLinajes. */
    @Test
    fun noContaminaElCacheGlobalDelTop() {
        val antes = modelo.topLinajes(20)
        modelo.topLinajesDeElenco(idsJugables().take(20).toSet(), 5)
        assertEquals(antes, modelo.topLinajes(20))
    }
}
