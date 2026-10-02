package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.CharacterDto
import com.maximillionsnyder.umafinidad.data.MemberDto
import com.maximillionsnyder.umafinidad.data.RelationDto
import com.maximillionsnyder.umafinidad.data.jsonParser
import com.maximillionsnyder.umafinidad.data.toDomain
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.SLOTS
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/* Aporte directo por personaje que la lista de Mi corredora muestra en cada
   fila: los vínculos del personaje dentro del árbol, con la misma semántica
   que el "pt" de la hoja de alternativas. */
class AporteDirectoTest {

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

    private fun seleccionOptima(): List<Int?> {
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

    /* Expresión manual con los puntajes públicos, independiente del helper. */
    private fun aporteManual(sel: List<Int?>, slot: Int): Int {
        val h = sel[0]!!
        return when (slot) {
            0 -> {
                var d = 0
                for (p in 1..2) sel[p]?.let { d += modelo.puntajePar(h, it) }
                for (s in 3..6) {
                    val g = sel[s] ?: continue
                    if (g == h) continue
                    d += modelo.puntajeTrio(h, sel[1 + (s - 3) / 2]!!, g)
                }
                d
            }
            in 1..2 -> {
                val p = sel[slot]!!
                val otro = sel[if (slot == 1) 2 else 1]!!
                var d = modelo.puntajePar(h, p) + modelo.puntajePar(p, otro)
                val rama = slot - 1
                for (s in listOf(3 + rama * 2, 4 + rama * 2)) {
                    val g = sel[s]
                    if (g != null && g != p && g != h) d += modelo.puntajeTrio(h, p, g)
                }
                d
            }
            else -> {
                val p = sel[1 + (slot - 3) / 2]!!
                val g = sel[slot]!!
                if (g == h) 0 else modelo.puntajeTrio(h, p, g)
            }
        }
    }

    @Test
    fun cadaSlotCoincideConLaSumaManualDeSusVinculos() {
        val seleccion = seleccionOptima()
        val aportes = modelo.aportesDirectos(seleccion)
        assertEquals(SLOTS, aportes.size)
        for (slot in 0 until SLOTS) {
            assertEquals("slot $slot", aporteManual(seleccion, slot), aportes[slot])
        }
    }

    /* El hijo participa de todos los vínculos salvo el par entre padres. */
    @Test
    fun elHijoSumanosElParEntrePadres() {
        val seleccion = seleccionOptima()
        val total = modelo.totalDeSeleccion(seleccion)
        val parPadres = modelo.puntajePar(seleccion[1]!!, seleccion[2]!!)
        assertEquals(total - parPadres, modelo.aportesDirectos(seleccion)[0])
    }

    /* Regla del juego: la corredora como abuela aporta 0. */
    @Test
    fun abuelaQueEsLaCorredoraAportaCero() {
        val seleccion = seleccionOptima().toMutableList()
        seleccion[3] = seleccion[0]
        val aportes = modelo.aportesDirectos(seleccion)
        assertEquals(0, aportes[3])
        assertEquals(
            modelo.totalDeSeleccion(seleccion) - modelo.puntajePar(seleccion[1]!!, seleccion[2]!!),
            aportes[0],
        )
    }

    @Test
    fun slotsVaciosAportanCero() {
        val seleccion = seleccionOptima()
        val parcial = listOf(seleccion[0], seleccion[1], null, null, null, null, null)
        val aportes = modelo.aportesDirectos(parcial)
        assertEquals(SLOTS, aportes.size)
        assertEquals(modelo.puntajePar(seleccion[0]!!, seleccion[1]!!), aportes[0])
        assertEquals(modelo.puntajePar(seleccion[0]!!, seleccion[1]!!), aportes[1])
        for (slot in 2 until SLOTS) assertEquals(0, aportes[slot])
    }

    /* El valor de una fila es el mismo "pt" que la hoja de alternativas le
       asigna a ese personaje como candidato. */
    @Test
    fun coincideConLosPuntosDirectosDeLasAlternativas() {
        val seleccion = seleccionOptima()
        for (slot in 1..6) {
            val alternativas = modelo.alternativasParaSlot(seleccion, slot)
            if (alternativas.isEmpty()) continue
            val alt = alternativas.first()
            assertEquals(
                alt.puntosDirectos,
                modelo.aporteDirectoDeCandidato(seleccion, slot, alt.personaje.charId),
            )
        }
    }
}
