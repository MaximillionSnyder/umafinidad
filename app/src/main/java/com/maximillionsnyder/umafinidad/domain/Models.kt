package com.maximillionsnyder.umafinidad.domain

/* Modelos de entrada (JSON datamined de GameTora). Solo los campos que la
   app usa; el parser ignora el resto. */

data class Character(
    val charId: Int,
    val enName: String?,
    val jpName: String?,
    val playable: Boolean?,
    val active: Boolean?,
    val urlName: String?,
) {
    /* Nombre visible según idioma del teléfono: japonés → jp_name,
       cualquier otro → en_name (con fallback cruzado). */
    fun displayName(japones: Boolean): String =
        (if (japones) jpName ?: enName else enName ?: jpName)
            ?: "char $charId"
}

data class Relation(
    val relationType: Int,
    val relationPoint: Int,
)

data class Member(
    val charaId: Int,
    val relationType: Int,
)
