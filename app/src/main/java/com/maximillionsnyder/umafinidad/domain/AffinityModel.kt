package com.maximillionsnyder.umafinidad.domain

/* Porte 1:1 de src/affinity.js de la PWA.
   Los puntajes y rankings deben dar idénticos a la web validada; los tests
   de paridad (fixtures generados desde la lógica JS) lo garantizan. */

private const val K_TOP = 300

data class GrupoCompartido(val tipo: Int, val puntos: Int)

data class GrupoInfo(
    val tipo: Int,
    val puntos: Int,
    val miembros: List<String>,
)

data class GrupoResumen(val tipo: Int, val puntos: Int, val cantidad: Int)

data class Rango(val simbolo: String, val clase: String)

data class MejorParAbuelos(val g1: Int, val g2: Int, val puntos: Int)

data class Linaje(
    val hijo: Character,
    val padre: Character,
    val madre: Character,
    /* [rama del padre, rama de la madre][abuelo 1, abuelo 2] */
    val abuelos: List<List<Character>>,
    val puntos: Int,
)

class AffinityModel private constructor(
    val personajes: List<Character>,
    private val porId: Map<Int, Character>,
) {
    private val puntoPorTipo = mutableMapOf<Int, Int>()
    private val tiposPorChar = mutableMapOf<Int, LinkedHashSet<Int>>()
    private val miembrosPorTipo = mutableMapOf<Int, MutableList<Int>>()
    private lateinit var relacionesOrdenadas: List<Relation>

    constructor(characters: List<Character>, relations: List<Relation>, members: List<Member>) : this(characters, characters.associateBy { it.charId }) {
        relacionesOrdenadas = relations
        for (r in relations) puntoPorTipo[r.relationType] = r.relationPoint
        for (m in members) {
            tiposPorChar.getOrPut(m.charaId) { linkedSetOf() }.add(m.relationType)
            miembrosPorTipo.getOrPut(m.relationType) { mutableListOf() }.add(m.charaId)
        }
    }

    fun gruposCompartidos(ids: List<Int>): List<GrupoCompartido> {
        if (ids.size < 2) return emptyList()
        val sets = ids.map { id -> tiposPorChar[id] ?: linkedSetOf() }
        val primero = sets.first()
        val resto = sets.drop(1)
        return primero
            .filter { tipo -> resto.all { set -> tipo in set } }
            .map { tipo -> GrupoCompartido(tipo, puntoPorTipo[tipo] ?: 0) }
            .sortedWith(compareByDescending<GrupoCompartido> { it.puntos }.thenBy { it.tipo })
    }

    fun puntajePar(a: Int, b: Int): Int {
        if (a == b) return 0
        return gruposCompartidos(listOf(a, b)).sumOf { it.puntos }
    }

    fun puntajeTrio(a: Int, b: Int, c: Int): Int =
        gruposCompartidos(listOf(a, b, c)).sumOf { it.puntos }

    fun rango(puntos: Int): Rango? = when {
        puntos >= 20 -> Rango("◎", "rank-great")
        puntos >= 10 -> Rango("○", "rank-good")
        puntos >= 4 -> Rango("△", "rank-fair")
        else -> null
    }

    /* Umbrales del juego sobre el total de herencia: ○ ≥ 51, ◎ ≥ 151. */
    fun rangoTotal(puntos: Int): Rango = when {
        puntos >= 151 -> Rango("◎", "rank-great")
        puntos >= 51 -> Rango("○", "rank-good")
        else -> Rango("△", "rank-fair")
    }

    fun gruposDeChar(id: Int): List<GrupoInfo> =
        (tiposPorChar[id] ?: linkedSetOf())
            .map { tipo ->
                GrupoInfo(
                    tipo = tipo,
                    puntos = puntoPorTipo[tipo] ?: 0,
                    miembros = (miembrosPorTipo[tipo] ?: emptyList()).map { mid -> porId[mid]?.enName ?: mid.toString() },
                )
            }
            .sortedWith(compareByDescending<GrupoInfo> { it.puntos }.thenBy { it.tipo })

    fun todosLosGrupos(): List<GrupoResumen> =
        relacionesOrdenadas
            .map { r -> GrupoResumen(r.relationType, r.relationPoint, miembrosPorTipo[r.relationType]?.size ?: 0) }
            .sortedWith(
                compareByDescending<GrupoResumen> { it.puntos }
                    .thenByDescending { it.cantidad }
                    .thenBy { it.tipo },
            )

    fun miembrosDeGrupo(tipo: Int): List<Character> =
        (miembrosPorTipo[tipo] ?: emptyList()).mapNotNull { porId[it] }

    /* Trío sin allocations, para los barridos masivos del top. */
    fun puntajeTrioRapido(a: Int, b: Int, c: Int): Int {
        val sa = tiposPorChar[a] ?: return 0
        val sb = tiposPorChar[b] ?: return 0
        val sc = tiposPorChar[c] ?: return 0
        var total = 0
        for (tipo in sa) {
            if (tipo in sb && tipo in sc) total += puntoPorTipo[tipo] ?: 0
        }
        return total
    }

    private val cacheTop: List<Linaje> by lazy { calcularTopLinajes() }

    /* ===== Estructuras compartidas (top global y mejorLinajeDe) ===== */

    private val charsTop: List<Character> by lazy { personajes.filter { it.playable == true && it.active == true } }
    private val idsTop: List<Int> by lazy { charsTop.map { it.charId } }

    /* Matriz triangular superior de puntajes par-a-par. */
    private val matrizPares: IntArray by lazy {
        val m = charsTop.size
        val ids = idsTop
        IntArray(m * m).also { s ->
            for (i in 0 until m)
                for (j in i + 1 until m) s[i * m + j] = puntajePar(ids[i], ids[j])
        }
    }

    private fun parEn(i: Int, j: Int): Int {
        val m = charsTop.size
        return if (i < j) matrizPares[i * m + j] else matrizPares[j * m + i]
    }

    private val cacheAbuelos = HashMap<Long, MejorParAbuelos>()

    /* Mejor par de abuelos para la rama del padre p con hijo h.
       Reglas del juego: nadie puede ser abuelo de su propia rama (g === p
       prohibido); el hijo sí puede ser abuelo pero esa relación vale 0
       (corredora). */
    private fun mejorParAbuelos(h: Int, p: Int): MejorParAbuelos {
        val m = charsTop.size
        val key = h.toLong() * m + p
        cacheAbuelos[key]?.let { return it }
        var t1 = -1; var t2 = -1; var g1 = -1; var g2 = -1
        for (g in 0 until m) {
            if (g == p) continue
            val t = if (g == h) 0 else puntajeTrioRapido(idsTop[h], idsTop[p], idsTop[g])
            if (t > t1) { t2 = t1; g2 = g1; t1 = t; g1 = g } else if (t > t2) { t2 = t; g2 = g }
        }
        val v = MejorParAbuelos(g1, g2, t1 + t2)
        cacheAbuelos[key] = v
        return v
    }

    /* Top de linajes completos (hijo + padres + mejores abuelos por rama).
       Mismo algoritmo heurístico que la web (K=300 triples base + mejor par
       de abuelos por rama), con los mismos criterios de orden para que los
       empates queden idénticos. */
    fun topLinajes(n: Int = 20): List<Linaje> = cacheTop.take(n)

    private class SetBase(val a: Int, val b: Int, val c: Int, val base: Int)

    private class Candidato(val h: Int, val p1: Int, val p2: Int, val base: Int, val total: Int, val b1: MejorParAbuelos, val b2: MejorParAbuelos)

    private fun calcularTopLinajes(): List<Linaje> {
        val chars = charsTop
        val m = chars.size
        val ids = idsTop

        val sets = mutableListOf<SetBase>()
        for (h in 0 until m)
            for (p1 in h + 1 until m)
                for (p2 in p1 + 1 until m) {
                    val base = matrizPares[h * m + p1] + matrizPares[h * m + p2] + matrizPares[p1 * m + p2]
                    if (sets.size < K_TOP || base > sets.last().base) {
                        sets.add(SetBase(h, p1, p2, base))
                        sets.sortWith(compareByDescending { it.base }) /* estable, igual que JS */
                        if (sets.size > K_TOP) sets.subList(K_TOP, sets.size).clear()
                    }
                }

        val candidatos = mutableListOf<Candidato>()
        for (st in sets)
            for ((h, p1, p2) in listOf(
                Triple(st.a, st.b, st.c),
                Triple(st.b, st.a, st.c),
                Triple(st.c, st.a, st.b),
            )) {
                val b1 = mejorParAbuelos(h, p1)
                val b2 = mejorParAbuelos(h, p2)
                candidatos.add(Candidato(h, p1, p2, st.base, st.base + b1.puntos + b2.puntos, b1, b2))
            }

        candidatos.sortWith(compareByDescending { it.total }) /* estable, igual que JS */

        fun charEn(i: Int): Character = chars[i]

        return candidatos.map { r ->
            Linaje(
                hijo = charEn(r.h),
                padre = charEn(r.p1),
                madre = charEn(r.p2),
                abuelos = listOf(
                    listOf(charEn(r.b1.g1), charEn(r.b1.g2)),
                    listOf(charEn(r.b2.g1), charEn(r.b2.g2)),
                ),
                puntos = r.total,
            )
        }
    }

    /* Mejor linaje EXACTO para un hijo dado: se recorren todos los pares de
       padres posibles (~m²/2) y se completa cada rama con su mejor par de
       abuelos. Sin heurística: es el óptimo real para esa corredora.
       Devuelve null si el personaje no está en el pool jugable/activo. */
    fun mejorLinajeDe(hijoId: Int): Linaje? {
        val chars = charsTop
        val m = chars.size
        val h = idsTop.indexOf(hijoId)
        if (h < 0) return null

        /* Calienta la matriz una sola vez. */
        matrizPares

        var mejorP1 = -1; var mejorP2 = -1
        var mejorTotal = -1
        var mejorB1: MejorParAbuelos? = null
        var mejorB2: MejorParAbuelos? = null

        for (p1 in 0 until m) {
            if (p1 == h) continue
            for (p2 in p1 + 1 until m) {
                if (p2 == h) continue
                val base = parEn(h, p1) + parEn(h, p2) + parEn(p1, p2)
                val b1 = mejorParAbuelos(h, p1)
                val b2 = mejorParAbuelos(h, p2)
                val total = base + b1.puntos + b2.puntos
                if (total > mejorTotal) {
                    mejorTotal = total; mejorP1 = p1; mejorP2 = p2; mejorB1 = b1; mejorB2 = b2
                }
            }
        }
        if (mejorB1 == null || mejorB2 == null) return null

        fun charEn(i: Int): Character = chars[i]

        return Linaje(
            hijo = charEn(h),
            padre = charEn(mejorP1),
            madre = charEn(mejorP2),
            abuelos = listOf(
                listOf(charEn(mejorB1.g1), charEn(mejorB1.g2)),
                listOf(charEn(mejorB2.g1), charEn(mejorB2.g2)),
            ),
            puntos = mejorTotal,
        )
    }

    fun porId(id: Int): Character? = porId[id]
}
