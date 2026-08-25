package com.maximillionsnyder.umafinidad.domain

import java.text.Normalizer

/* Búsqueda difusa de personajes para el autocompletado del buscador.
   Tolerante a errores de tipeo: susuka→Suzuka, szuka, suzuak, etc. */

fun normalizarTexto(v: String): String =
    Normalizer.normalize(v, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace(Regex("[-_.']"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
        .lowercase()

/* Distancia de Damerau-Levenshtein (incluye transposición de adyacentes).
   Implementación clásica de tres filas, O(n·m). */
fun distanciaDamerau(a: String, b: String): Int {
    if (a == b) return 0
    val n = a.length
    val m = b.length
    if (n == 0) return m
    if (m == 0) return n

    var prevPrev = IntArray(m + 1)
    var prev = IntArray(m + 1) { it }
    var curr = IntArray(m + 1)

    for (i in 1..n) {
        curr[0] = i
        val ca = a[i - 1]
        for (j in 1..m) {
            val costo = if (ca == b[j - 1]) 0 else 1
            var v = minOf(
                curr[j - 1] + 1,   // inserción
                prev[j] + 1,       // omisión
                prev[j - 1] + costo, // sustitución o igual
            )
            if (i > 1 && j > 1 && ca == b[j - 2] && a[i - 2] == b[j - 1]) {
                v = minOf(v, prevPrev[j - 2] + 1) // transposición
            }
            curr[j] = v
        }
        val tmp = prevPrev
        prevPrev = prev
        prev = curr
        curr = tmp
    }
    return prev[m]
}

/* Umbral adaptativo: los textos cortos exigen menos errores para no
   sugerir basura. */
internal fun umbralFuzzy(largoQuery: Int): Int = when {
    largoQuery <= 3 -> 1
    largoQuery <= 6 -> 2
    else -> 3
}

/* Puntaje de la query contra un nombre ya normalizado.
   Menor es mejor; null significa descartado. */
internal fun puntajeCandidato(nombreNorm: String, q: String): Int? {
    if (q.isEmpty()) return null
    val idx = nombreNorm.indexOf(q)
    return when {
        idx == 0 -> 0 // empieza igual que la query
        nombreNorm.split(' ').any { it.startsWith(q) } -> 100 // alguna palabra empieza igual
        idx > 0 -> 200 + idx.coerceAtMost(50) // lo contiene
        else -> {
            val umbral = umbralFuzzy(q.length)
            var mejorDist = distanciaDamerau(q, nombreNorm)
            for (palabra in nombreNorm.split(' ')) {
                if (kotlin.math.abs(palabra.length - q.length) <= umbral) {
                    mejorDist = minOf(mejorDist, distanciaDamerau(q, palabra))
                }
                if (mejorDist == 0) break
            }
            if (mejorDist in 1..umbral) 300 + mejorDist * 10 else null
        }
    }
}

private fun puntajePersonaje(c: Character, q: String): Int? {
    var mejor: Int? = null
    for (nombre in listOfNotNull(c.enName, c.jpName, c.urlName)) {
        val p = puntajeCandidato(normalizarTexto(nombre), q)
        if (p != null && (mejor == null || p < mejor)) mejor = p
    }
    return mejor
}

/* Top N personajes para el dropdown de sugerencias, ordenados por calidad. */
fun rankearSugerencias(personajes: List<Character>, query: String, limite: Int = 5): List<Character> {
    val q = normalizarTexto(query)
    if (q.length < 2) return emptyList()
    return personajes
        .mapNotNull { c -> puntajePersonaje(c, q)?.let { c to it } }
        .sortedWith(compareBy({ it.second }, { it.first.enName ?: "" }))
        .take(limite)
        .map { it.first }
}

/* Filtro difuso para la grilla: mismo criterio que las sugerencias pero
   sin límite ni reordenamiento. */
fun coincideDifuso(c: Character, query: String): Boolean {
    val q = normalizarTexto(query)
    if (q.isEmpty()) return true
    return puntajePersonaje(c, q) != null
}
