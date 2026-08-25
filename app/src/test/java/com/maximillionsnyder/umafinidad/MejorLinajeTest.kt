package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.CharacterDto
import com.maximillionsnyder.umafinidad.data.MemberDto
import com.maximillionsnyder.umafinidad.data.RelationDto
import com.maximillionsnyder.umafinidad.data.jsonParser
import com.maximillionsnyder.umafinidad.data.toDomain
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.TipoVinculo
import com.maximillionsnyder.umafinidad.domain.armarArbol
import com.maximillionsnyder.umafinidad.domain.vinculos
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/* Tests del mejor linaje exacto por corredora (mejorLinajeDe). */
class MejorLinajeTest {

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

    /* El óptimo exhaustivo para el hijo del top-1 global no puede ser peor
       que el candidato heurístico de ese mismo hijo. */
    @Test
    fun optimoSuperaAlHeuristicoDelTopGlobal() {
        val top1 = modelo.topLinajes(1).first()
        val suMejor = modelo.mejorLinajeDe(top1.hijo.charId)
        assertNotNull(suMejor)
        assertTrue(
            "exhaustivo ${suMejor!!.puntos} >= heurístico ${top1.puntos}",
            suMejor.puntos >= top1.puntos,
        )
    }

    @Test
    fun estructuraRespetaLasReglasDelJuego() {
        val top5 = modelo.topLinajes(5)
        for (l in top5) {
            val mejor = modelo.mejorLinajeDe(l.hijo.charId)!!
            assertEquals(l.hijo.charId, mejor.hijo.charId)
            val ids = listOf(mejor.padre.charId, mejor.madre.charId)
            assertEquals("padres distintos", 2, ids.toSet().size)
            assertTrue("el hijo no puede ser padre", mejor.hijo.charId !in ids)

            /* Cada abuelo no puede ser el padre de su propia rama. */
            val padres = listOf(mejor.padre.charId, mejor.madre.charId)
            mejor.abuelos.forEachIndexed { rama, abuelos ->
                abuelos.forEach { a ->
                    assertTrue(
                        "abuelo ${a.charId} ≠ padre de su rama ${padres[rama]}",
                        a.charId != padres[rama],
                    )
                }
                assertNotEquals("abuelos distintos en la rama", abuelos[0].charId, abuelos[1].charId)
            }
        }
    }

    private fun assertNotEquals(mensaje: String, a: Int, b: Int) {
        assertTrue(mensaje, a != b)
    }

    /* El total debe reconstruirse con los puntajes públicos:
       hijo×padres (par) + entre padres (par) + hijo×padre×abuelo (trío,
       corredora vale 0). */
    @Test
    fun totalCoherenteConPuntajesPublicos() {
        val top3 = modelo.topLinajes(3)
        for (l in top3) {
            val mejor = modelo.mejorLinajeDe(l.hijo.charId)!!
            val seleccion: Array<Int?> = arrayOf(
                mejor.hijo.charId,
                mejor.padre.charId,
                mejor.madre.charId,
                mejor.abuelos[0][0].charId,
                mejor.abuelos[0][1].charId,
                mejor.abuelos[1][0].charId,
                mejor.abuelos[1][1].charId,
            )
            val recalculado = vinculos(armarArbol(seleccion)).sumOf { v ->
                if (v.esCorredora) 0
                else if (v.tipo == TipoVinculo.HIJO_PADRE_ABUELO)
                    modelo.puntajeTrio(v.ids[0], v.ids[1], v.ids[2])
                else modelo.puntajePar(v.ids[0], v.ids[1])
            }
            assertEquals(recalculado, mejor.puntos)
        }
    }

    @Test
    fun determinismo() {
        val id = modelo.topLinajes(2)[1].hijo.charId
        val a = modelo.mejorLinajeDe(id)
        val b = modelo.mejorLinajeDe(id)
        assertEquals(a?.puntos, b?.puntos)
        assertEquals(a?.padre?.charId, b?.padre?.charId)
        assertEquals(a?.madre?.charId, b?.madre?.charId)
    }

    @Test
    fun personajeInvalidoDevuelveNull() {
        assertNull(modelo.mejorLinajeDe(999999999))
    }

    /* ===== Alternativas por slot ===== */

    private fun seleccionDeTop1(): Array<Int?> {
        val l = modelo.topLinajes(1).first()
        return arrayOf(
            l.hijo.charId,
            l.padre.charId,
            l.madre.charId,
            l.abuelos[0][0].charId,
            l.abuelos[0][1].charId,
            l.abuelos[1][0].charId,
            l.abuelos[1][1].charId,
        )
    }

    @Test
    fun alternativasOrdenadasValidasYSinOcupante() {
        val sel = seleccionDeTop1()
        for (slot in 1..6) {
            val alts = modelo.alternativasParaSlot(sel.toList(), slot)
            if (alts.isEmpty()) continue

            /* Orden descendente por total. */
            for (i in 1 until alts.size) {
                assertTrue("slot $slot fila $i", alts[i - 1].total >= alts[i].total)
            }
            /* Sin el ocupante actual y con reglas válidas. */
            for (alt in alts) {
                assertTrue("≠ ocupante", alt.personaje.charId != sel[slot])
                assertTrue(
                    "puedeIrEn(slot $slot, ${alt.personaje.charId})",
                    com.maximillionsnyder.umafinidad.domain.puedeIrEn(sel, slot, alt.personaje.charId),
                )
                /* Ningún cambio puede superar el óptimo exhaustivo del hijo. */
                val optimoHijo = modelo.mejorLinajeDe(sel[0]!!)!!.puntos
                assertTrue("total ${alt.total} <= óptimo $optimoHijo", alt.total <= optimoHijo)
            }
        }
    }

    @Test
    fun totalDeAlternativaCoherenteConPuntajesPublicos() {
        val sel = seleccionDeTop1()
        val alts = modelo.alternativasParaSlot(sel.toList(), 1)
        val primera = alts.firstOrNull() ?: return

        val nuevo = sel.copyOf().also { it[1] = primera.personaje.charId }
        val recalculado = vinculos(armarArbol(nuevo)).sumOf { v ->
            if (v.esCorredora) 0
            else if (v.tipo == TipoVinculo.HIJO_PADRE_ABUELO)
                modelo.puntajeTrio(v.ids[0], v.ids[1], v.ids[2])
            else modelo.puntajePar(v.ids[0], v.ids[1])
        }
        assertEquals(recalculado, primera.total)
    }

    @Test
    fun hijoNoEsIntercambiable() {
        val sel = seleccionDeTop1()
        assertTrue(modelo.alternativasParaSlot(sel.toList(), 0).isEmpty())
    }
}
