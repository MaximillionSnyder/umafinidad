package com.maximillionsnyder.umafinidad.data

import android.content.Context
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import kotlinx.serialization.decodeFromString

/* Carga una única vez los JSON embebidos en assets/data y arma el modelo. */
class AffinityRepository(private val context: Context) {

    val modelo: AffinityModel by lazy { cargar() }

    private fun cargar(): AffinityModel {
        val characters = jsonParser.decodeFromString<List<CharacterDto>>(
            context.assets.open("data/characters.json").bufferedReader().use { it.readText() },
        ).map { it.toDomain() }
        val relations = jsonParser.decodeFromString<List<RelationDto>>(
            context.assets.open("data/succession_relation.json").bufferedReader().use { it.readText() },
        ).map { it.toDomain() }
        val members = jsonParser.decodeFromString<List<MemberDto>>(
            context.assets.open("data/succession_relation_member.json").bufferedReader().use { it.readText() },
        ).map { it.toDomain() }
        return AffinityModel(characters, relations, members)
    }
}
