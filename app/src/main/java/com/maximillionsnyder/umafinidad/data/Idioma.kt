package com.maximillionsnyder.umafinidad.data

/* Idioma elegible por el usuario. SISTEMA respeta el idioma del sistema. */
enum class Idioma { SISTEMA, ESPANOL, INGLES, JAPONES }

fun Idioma.codigo(): String? = when (this) {
    Idioma.SISTEMA -> null
    Idioma.ESPANOL -> "es"
    Idioma.INGLES -> "en"
    Idioma.JAPONES -> "ja"
}

fun Idioma.displayNameRes(): Int = when (this) {
    Idioma.SISTEMA -> com.maximillionsnyder.umafinidad.R.string.idioma_sistema
    Idioma.ESPANOL -> com.maximillionsnyder.umafinidad.R.string.idioma_espanol
    Idioma.INGLES -> com.maximillionsnyder.umafinidad.R.string.idioma_ingles
    Idioma.JAPONES -> com.maximillionsnyder.umafinidad.R.string.idioma_japones
}
