package com.maximillionsnyder.umafinidad.cartas

import com.maximillionsnyder.umafinidad.cartas.data.CharacterCardDto
import com.maximillionsnyder.umafinidad.cartas.data.ScenarioDto
import com.maximillionsnyder.umafinidad.cartas.data.SkillDto
import com.maximillionsnyder.umafinidad.cartas.data.SupportCardDto
import com.maximillionsnyder.umafinidad.cartas.data.jsonParser
import com.maximillionsnyder.umafinidad.cartas.data.toDomain
import com.maximillionsnyder.umafinidad.cartas.domain.texto
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatosTest {

    private val skills: List<SkillDto> = jsonParser.decodeFromString(leer("data/skills.json"))
    private val personajes: Map<String, CharacterCardDto> = jsonParser.decodeFromString(leer("data/character-cards.json"))
    private val apoyos: Map<String, SupportCardDto> = jsonParser.decodeFromString(leer("data/support-cards.json"))
    private val escenarios: Map<String, ScenarioDto> = jsonParser.decodeFromString(leer("data/scenarios.json"))

    @Test
    fun conteos() {
        assertEquals(1921, skills.size)
        assertEquals(270, personajes.size)
        assertEquals(563, apoyos.size)
        assertEquals(14, escenarios.size)
    }

    @Test
    fun uniqueDeTokaiTeio() {
        val skill = skills.first { it.id == 110031 }
        assertEquals(1, skill.sources.characterCards.size)
        assertEquals(100302, skill.sources.characterCards[0].cardId)
        assertEquals(listOf("unique"), skill.sources.characterCards[0].kinds)
    }

    @Test
    fun totalesDeFuentes() {
        assertEquals(2825, skills.sumOf { it.sources.characterCards.size })
        assertEquals(1282, skills.sumOf { it.sources.characterEvents.size })
        assertEquals(1540, skills.sumOf { it.sources.supportEvents.size })
        assertEquals(4015, skills.sumOf { it.sources.supportHints.size })
        assertEquals(109, skills.sumOf { it.sources.scenarioEvents.size })
    }

    @Test
    fun referenciasResueltas() {
        val cartasSinMetadata = skills
            .flatMap { it.sources.characterCards.map { fuente -> fuente.cardId } + it.sources.characterEvents }
            .filterNot { personajes.containsKey(it.toString()) }
            .distinct()
        assertTrue("Cartas sin metadata: $cartasSinMetadata", cartasSinMetadata.isEmpty())

        val apoyosSinMetadata = skills
            .flatMap { it.sources.supportHints + it.sources.supportEvents }
            .filterNot { apoyos.containsKey(it.toString()) }
            .distinct()
        assertTrue("Apoyos sin metadata: $apoyosSinMetadata", apoyosSinMetadata.isEmpty())

        val escenariosSinMetadata = skills
            .flatMap { it.sources.scenarioEvents }
            .filterNot { escenarios.containsKey(it.toString()) }
            .distinct()
        assertTrue("Escenarios sin metadata: $escenariosSinMetadata", escenariosSinMetadata.isEmpty())
    }

    @Test
    fun efectosDeVelocidadYAceleracion() {
        val dominio = skills.map { it.toDomain() }
        assertEquals(1301, dominio.count { it.daVelocidad })
        assertEquals(389, dominio.count { it.daAceleracion })
    }

    @Test
    fun versionGenConTexto() {
        val dominio = skills.map { it.toDomain() }
        assertEquals(292, dominio.count { it.geneVersion != null })
        assertEquals(
            155,
            dominio.count { it.geneVersion?.let { gen -> gen.name.texto("en") != null || gen.desc.texto("en") != null } == true },
        )
    }

    @Test
    fun estrellasDeApoyosFiltrables() {
        /* Los chips de la pantalla de apoyos ofrecen 1★, 2★ y 3★: ninguna carta
           debe quedar fuera de esas tres opciones. */
        val rarities = apoyos.values.map { it.rarity }.distinct().sortedBy { it ?: 0 }
        assertEquals(listOf(1, 2, 3), rarities)
    }

    @Test
    fun tiposDeApoyosConocidos() {
        val tipos = apoyos.values.map { it.type ?: "?" }.distinct().sorted()
        assertEquals(
            listOf("friend", "group", "guts", "intelligence", "power", "speed", "stamina"),
            tipos,
        )
    }

    @Test
    fun duracionesDeCondicionesEnMilesimasExactas() {
        /* En la app las duraciones se muestran en segundos: el dataset usa
           milisegundos exactos, así que no hace falta redondear. */
        val tiempos = skills.flatMap { it.conditions }.mapNotNull { it.baseTime }.filter { it > 0 }
        assertEquals(1958, tiempos.size)
        assertEquals(emptyList<Long>(), tiempos.filter { it % 1000L != 0L }.distinct())
    }

    private fun leer(ruta: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(ruta)) { "Falta $ruta en el classpath" }
            .bufferedReader().use { it.readText() }

    @Test
    fun lasSkillsQueSoloEstanEnJaponLlevanElAvisoSinEn() {
        /* 200014 (右回りの鬼 / Right-Handed Demon) no está en el servidor global
           aunque GameTora no lo marque: la lista a mano del volcado lo corrige. */
        assertEquals(listOf("en"), skills.first { it.id == 200014 }.unreleased)
        assertTrue("en" in skills.first { it.id == 200024 }.unreleased)
    }
}
