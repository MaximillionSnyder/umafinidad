package com.maximillionsnyder.umafinidad.cartas.data

import android.content.Context
import com.maximillionsnyder.umafinidad.cartas.domain.CartasModelo
import com.maximillionsnyder.umafinidad.cartas.domain.Skill
import kotlinx.serialization.decodeFromString

/* Carga una única vez los JSON embebidos en assets/data y arma el modelo,
   incluyendo los índices inversos carta → skills. */
class CartasRepository(private val context: Context) {

    val modelo: CartasModelo by lazy { cargar() }

    private fun cargar(): CartasModelo {
        val skills = jsonParser
            .decodeFromString<List<SkillDto>>(leer("data/skills.json"))
            .map { it.toDomain() }
        val personajes = jsonParser
            .decodeFromString<Map<String, CharacterCardDto>>(leer("data/character-cards.json"))
            .values.map { it.toDomain() }
            .sortedBy { it.cardId }
        val apoyos = jsonParser
            .decodeFromString<Map<String, SupportCardDto>>(leer("data/support-cards.json"))
            .values.map { it.toDomain() }
            .sortedBy { it.supportId }
        val escenarios = jsonParser
            .decodeFromString<Map<String, ScenarioDto>>(leer("data/scenarios.json"))
            .values.map { it.toDomain() }
            .sortedBy { it.id }

        val porCarta = mutableMapOf<Int, MutableSet<Skill>>()
        val porApoyo = mutableMapOf<Int, MutableSet<Skill>>()
        for (skill in skills) {
            for (fuente in skill.sources.characterCards) {
                porCarta.getOrPut(fuente.cardId) { linkedSetOf() }.add(skill)
            }
            for (cardId in skill.sources.characterEvents) {
                porCarta.getOrPut(cardId) { linkedSetOf() }.add(skill)
            }
            for (supportId in skill.sources.supportHints) {
                porApoyo.getOrPut(supportId) { linkedSetOf() }.add(skill)
            }
            for (supportId in skill.sources.supportEvents) {
                porApoyo.getOrPut(supportId) { linkedSetOf() }.add(skill)
            }
        }

        return CartasModelo(
            skills = skills,
            cartasPersonaje = personajes,
            cartasApoyo = apoyos,
            escenarios = escenarios,
            skillsPorCarta = porCarta.mapValues { it.value.toList() },
            skillsPorApoyo = porApoyo.mapValues { it.value.toList() },
        )
    }

    private fun leer(ruta: String): String =
        context.assets.open(ruta).bufferedReader().use { it.readText() }
}
