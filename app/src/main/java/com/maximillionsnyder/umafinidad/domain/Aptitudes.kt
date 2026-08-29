package com.maximillionsnyder.umafinidad.domain

/* Aptitudes de la carta base (data de GameTora): 10 letras A–G en orden
   fijo. La UI las agrupa en pista / distancia / estilo. */

const val APT_TURF = 0
const val APT_DIRT = 1
const val APT_CORTA = 2
const val APT_MILLA = 3
const val APT_MEDIA = 4
const val APT_LARGA = 5
const val APT_FUGA = 6
const val APT_VANGUARDIA = 7
const val APT_REMATE = 8
const val APT_RETRASO = 9

fun aptitudValida(apt: List<String>): Boolean =
    apt.size == 10 && apt.all { it.length == 1 && it[0] in 'A'..'G' }

/* Índices de lo que la Uma hace bien (A o B). Para los chips compactos
   de las grillas. */
fun aptitudesDestacadas(apt: List<String>): List<Int> =
    apt.withIndex().filter { it.value == "A" || it.value == "B" }.map { it.index }
