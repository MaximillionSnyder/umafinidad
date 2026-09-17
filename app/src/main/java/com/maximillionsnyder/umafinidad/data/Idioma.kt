package com.maximillionsnyder.umafinidad.data

/* Idioma elegible por el usuario. SISTEMA respeta el idioma del sistema. */
enum class Idioma {
    SISTEMA,
    ESPANOL,
    INGLES,
    JAPONES,
    CHINO_SIMPLIFICADO,
    CHINO_TRADICIONAL,
    COREANO,
    INDONESIO,
    TAILANDES,
    VIETNAMITA,
}

fun Idioma.codigo(): String? = when (this) {
    Idioma.SISTEMA -> null
    Idioma.ESPANOL -> "es"
    Idioma.INGLES -> "en"
    Idioma.JAPONES -> "ja"
    Idioma.CHINO_SIMPLIFICADO -> "zh-CN"
    Idioma.CHINO_TRADICIONAL -> "zh-TW"
    Idioma.COREANO -> "ko"
    Idioma.INDONESIO -> "id"
    Idioma.TAILANDES -> "th"
    Idioma.VIETNAMITA -> "vi"
}

fun Idioma.displayNameRes(): Int = when (this) {
    Idioma.SISTEMA -> com.maximillionsnyder.umafinidad.R.string.idioma_sistema
    Idioma.ESPANOL -> com.maximillionsnyder.umafinidad.R.string.idioma_espanol
    Idioma.INGLES -> com.maximillionsnyder.umafinidad.R.string.idioma_ingles
    Idioma.JAPONES -> com.maximillionsnyder.umafinidad.R.string.idioma_japones
    Idioma.CHINO_SIMPLIFICADO -> com.maximillionsnyder.umafinidad.R.string.idioma_chino_simplificado
    Idioma.CHINO_TRADICIONAL -> com.maximillionsnyder.umafinidad.R.string.idioma_chino_tradicional
    Idioma.COREANO -> com.maximillionsnyder.umafinidad.R.string.idioma_coreano
    Idioma.INDONESIO -> com.maximillionsnyder.umafinidad.R.string.idioma_indonesio
    Idioma.TAILANDES -> com.maximillionsnyder.umafinidad.R.string.idioma_tailandes
    Idioma.VIETNAMITA -> com.maximillionsnyder.umafinidad.R.string.idioma_vietnamita
}
