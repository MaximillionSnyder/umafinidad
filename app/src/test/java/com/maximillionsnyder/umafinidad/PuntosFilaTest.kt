package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.CharacterDto
import com.maximillionsnyder.umafinidad.data.MemberDto
import com.maximillionsnyder.umafinidad.data.RelationDto
import com.maximillionsnyder.umafinidad.data.jsonParser
import com.maximillionsnyder.umafinidad.data.toDomain
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Linaje
import com.maximillionsnyder.umafinidad.domain.puntosFilaPresentacion
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/* Tests del reparto SOLO visual de los puntos por fila de "Mi corredora":
   las filas deben sumar el total sin tocar el algoritmo. */
class PuntosFilaTest {

    private lateinit var modelo: AffinityModel

    /* mejorLinajeDe es exhaustivo: se calcula una sola vez para todo el test. */
    private val linajeOguri: Linaje by lazy { modelo.mejorLinajeDe(1006)!! }

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

    private fun seleccionDeLinaje(l: Linaje): List<Int?> = listOf(
        l.hijo.charId,
        l.padre.charId,
        l.madre.charId,
        l.abuelos[0][0].charId,
        l.abuelos[0][1].charId,
        l.abuelos[1][0].charId,
        l.abuelos[1][1].charId,
    )

    private fun jugables(): List<Int> =
        modelo.personajes.filter { it.playable == true && it.active == true }.map { it.charId }

    /* Ejemplo real de la pantalla: Oguri Cap total 222. */
    @Test
    fun lasFilasSumanElTotalDelArbol() {
        val seleccion = seleccionDeLinaje(linajeOguri)
        val filas = modelo.puntosFilaPresentacion(seleccion)

        assertEquals(
            "la suma de las filas debe ser el total",
            modelo.totalDeSeleccion(seleccion).toDouble(),
            filas.filterNotNull().sum(),
            0.0,
        )
    }

    @Test
    fun elHijoNoLlevaPuntajePropio() {
        val filas = modelo.puntosFilaPresentacion(seleccionDeLinaje(linajeOguri))
        assertNull(filas[0])
    }

    @Test
    fun elPadreLlevaHijoPadreMasLaMitadEntrePadres() {
        val seleccion = seleccionDeLinaje(linajeOguri)
        val filas = modelo.puntosFilaPresentacion(seleccion)
        val hijo = seleccion[0]!!
        val padre1 = seleccion[1]!!
        val padre2 = seleccion[2]!!
        val mitad = modelo.puntajePar(padre1, padre2) / 2.0

        assertEquals(modelo.puntajePar(hijo, padre1) + mitad, filas[1]!!, 0.0)
        assertEquals(modelo.puntajePar(hijo, padre2) + mitad, filas[2]!!, 0.0)
    }

    /* El par padre-padre puede ser impar: la mitad lleva medio punto y con
       eso las filas siguen sumando exacto. */
    @Test
    fun elMedioPuntoApareceCuandoElParEntrePadresEsImpar() {
        val jugables = jugables()
        val parImpar = jugables.asSequence()
            .flatMap { b -> jugables.asSequence().map { c -> b to c } }
            .first { (b, c) -> b != c && modelo.puntajePar(b, c) % 2 == 1 }
        val (padre1, padre2) = parImpar
        val hijo = jugables.first { it != padre1 && it != padre2 }
        val abuelos = jugables.filter { it != hijo && it != padre1 && it != padre2 }.take(4)
        assertEquals(4, abuelos.size)
        val seleccion = listOf<Int?>(hijo, padre1, padre2, abuelos[0], abuelos[1], abuelos[2], abuelos[3])

        val filas = modelo.puntosFilaPresentacion(seleccion)
        val mitad = modelo.puntajePar(padre1, padre2) / 2.0
        assertEquals(padre1, seleccion[1])

        assertTrue("el par entre padres debería ser impar", modelo.puntajePar(padre1, padre2) % 2 == 1)
        assertEquals(modelo.puntajePar(hijo, padre1) + mitad, filas[1]!!, 0.0)
        assertTrue("la fila del padre lleva medio punto", filas[1]!! % 1.0 != 0.0)
        assertEquals(
            modelo.totalDeSeleccion(seleccion).toDouble(),
            filas.filterNotNull().sum(),
            0.0,
        )
    }

    @Test
    fun elAbueloLlevaSuTrioYLaCorredoraValeCero() {
        val seleccion = seleccionDeLinaje(linajeOguri)
        val filas = modelo.puntosFilaPresentacion(seleccion)
        val (hijo, padre1) = seleccion
        assertEquals(
            modelo.puntajeTrio(hijo!!, padre1!!, seleccion[3]!!).toDouble(),
            filas[3]!!,
            0.0,
        )

        val conCorredora = seleccion.toMutableList().also { it[3] = it[0] }
        assertEquals(0.0, modelo.puntosFilaPresentacion(conCorredora)[3]!!, 0.0)
    }

    @Test
    fun sinHijoNoHayNingunPuntajeDeFila() {
        val seleccion = listOf<Int?>(null, null, null, null, null, null, null)
        assertTrue(modelo.puntosFilaPresentacion(seleccion).all { it == null })
    }
}
