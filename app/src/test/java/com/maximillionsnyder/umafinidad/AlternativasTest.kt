package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.CharacterDto
import com.maximillionsnyder.umafinidad.data.MemberDto
import com.maximillionsnyder.umafinidad.data.RelationDto
import com.maximillionsnyder.umafinidad.data.jsonParser
import com.maximillionsnyder.umafinidad.data.toDomain
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.puedeIrEn
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/* Alternativas de un slot en Mi corredora: se ofrecen TODAS las que respetan
   las reglas del juego (antes se cortaban en 20) y el cálculo completo sigue
   entrando en el presupuesto de un toque. */
class AlternativasTest {

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

    private fun seleccionDeEjemplo(): List<Int?> {
        val l = modelo.mejorLinajeDe(1001)!!
        return listOf(
            l.hijo.charId,
            l.padre.charId,
            l.madre.charId,
            l.abuelos[0][0].charId,
            l.abuelos[0][1].charId,
            l.abuelos[1][0].charId,
            l.abuelos[1][1].charId,
        )
    }

    /* Todas las que pueden ir en el slot: la lista no se corta. */
    @Test
    fun devuelveTodasLasValidas() {
        val seleccion = seleccionDeEjemplo()
        val sel = seleccion.toTypedArray()
        for (slot in 1..6) {
            val alternativas = modelo.alternativasParaSlot(seleccion, slot, limite = Int.MAX_VALUE)
            val esperadas = modelo.personajes.filter { c ->
                c.playable == true && c.active == true &&
                    c.charId != seleccion[slot] && puedeIrEn(sel, slot, c.charId)
            }
            assertEquals("slot $slot", esperadas.size, alternativas.size)
            assertTrue("slot $slot debe pasar de 20", alternativas.size > 20)
        }
    }

    @Test
    fun respetaElOrdenPorTotalDescendente() {
        val alternativas = modelo.alternativasParaSlot(seleccionDeEjemplo(), 1, limite = Int.MAX_VALUE)
        val totales = alternativas.map { it.total }
        assertEquals(totales.sortedDescending(), totales)
        /* Ante el mismo total, gana el que más aporta directo. */
        alternativas.zipWithNext().forEach { (a, b) ->
            if (a.total == b.total) {
                assertTrue(a.puntosDirectos >= b.puntosDirectos)
            }
        }
    }

    /* El cálculo de todas las alternativas corre en el toque del usuario: no
       puede tardar como para notarse. */
    @Test
    fun elCalculoCompletoEsRapido() {
        val seleccion = seleccionDeEjemplo()
        repeat(2) { modelo.alternativasParaSlot(seleccion, 1, limite = Int.MAX_VALUE) }
        val inicio = System.nanoTime()
        val repeticiones = 5
        repeat(repeticiones) { modelo.alternativasParaSlot(seleccion, 3, limite = Int.MAX_VALUE) }
        val ms = (System.nanoTime() - inicio) / 1_000_000.0 / repeticiones
        assertTrue("tardó ${"%.1f".format(ms)} ms por slot", ms < 250)
    }
}
