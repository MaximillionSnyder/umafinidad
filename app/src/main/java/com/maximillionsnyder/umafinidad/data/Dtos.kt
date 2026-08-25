package com.maximillionsnyder.umafinidad.data

import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Member
import com.maximillionsnyder.umafinidad.domain.Relation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class CharacterDto(
    @SerialName("char_id") val charId: Int,
    @SerialName("en_name") val enName: String? = null,
    @SerialName("jp_name") val jpName: String? = null,
    val playable: Boolean? = null,
    val active: Boolean? = null,
)

@Serializable
internal data class RelationDto(
    @SerialName("relation_type") val relationType: Int,
    @SerialName("relation_point") val relationPoint: Int,
)

@Serializable
internal data class MemberDto(
    @SerialName("chara_id") val charaId: Int,
    @SerialName("relation_type") val relationType: Int,
)

@Serializable
internal data class DataBundle(
    val characters: List<CharacterDto>,
    val relations: List<RelationDto>,
    val members: List<MemberDto>,
)

/* Configuración del parser: los JSON datamined traen muchísimos campos que
   la app no usa. */
val jsonParser: Json = Json { ignoreUnknownKeys = true }

internal fun CharacterDto.toDomain() = Character(charId, enName, jpName, playable, active)
internal fun RelationDto.toDomain() = Relation(relationType, relationPoint)
internal fun MemberDto.toDomain() = Member(charaId, relationType)
