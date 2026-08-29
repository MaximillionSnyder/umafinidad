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

/* Candidato para reemplazar un slot del árbol de Mi corredora.
   puntosDirectos = aporte del candidato a SUS vínculos con el resto fijo;
   total = puntaje del árbol completo con el cambio aplicado. */
data class AlternativaSlot(
    val personaje: Character,
    val puntosDirectos: Int,
    val total: Int,
)

class AffinityModel private constructor(
    val personajes: List<Character>,
    private val porId: Map<Int, Character>,
) {
    private val puntoPorTipo = mutableMapOf<Int, Int>()
    private val tiposPorChar = mutableMapOf<Int, LinkedHashSet<Int>>()
    private val miembrosPorTipo = mutableMapOf<Int, MutableList<Int>>()
    private lateinit var relacionesOrdenadas: List<Relation>
    private val aptitudesPorId: Map<Int, List<String>>

    constructor(
        characters: List<Character>,
        relations: List<Relation>,
        members: List<Member>,
        aptitudes: Map<Int, List<String>> = emptyMap(),
    ) : this(characters, characters.associateBy { it.charId }) {
        relacionesOrdenadas = relations
        for (r in relations) puntoPorTipo[r.relationType] = r.relationPoint
        for (m in members) {
            tiposPorChar.getOrPut(m.charaId) { linkedSetOf() }.add(m.relationType)
            miembrosPorTipo.getOrPut(m.relationType) { mutableListOf() }.add(m.charaId)
        }
        aptitudesPorId = aptitudes
    }

    /* Aptitudes (pista/distancia/estilo) de la carta base; null si el
       personaje no está en la tabla. No afecta ningún cálculo. */
    fun aptitudesDe(id: Int): List<String>? = aptitudesPorId[id]

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

    private fun calcularTopLinajes(): List<Linaje> =
        calcularTopSobre((0 until charsTop.size).toList(), cacheAbuelos)

    /* Top de linajes calculado SOLO con los personajes que el usuario posee
       (ids de characters.json). Los ids fuera del pool jugable/activo se
       ignoran; con menos de 3 personajes no hay combinaciones posibles.
       Mismo algoritmo heurístico que el top global. */
    fun topLinajesDeElenco(idsElenco: Collection<Int>, n: Int = 20): List<Linaje> {
        val posPorId = HashMap<Int, Int>()
        for (i in idsTop.indices) posPorId[idsTop[i]] = i
        val indices = idsElenco.mapNotNull { posPorId[it] }.distinct().sorted()
        if (indices.size < 3) return emptyList()
        /* Cache local: los abuelos se eligen dentro del elenco, no valen
           las entradas calculadas para el pool completo. */
        return calcularTopSobre(indices, HashMap()).take(n)
    }

    /* Núcleo compartido del top de linajes sobre un subconjunto de índices
       de charsTop. Con indices = 0 until m y el cache global reproduce
       exactamente el algoritmo de la web: misma enumeración de triples
       (i < j < k sobre índices crecientes) y mismos ordenamientos
       estables, para que los empates queden idénticos. */
    private fun calcularTopSobre(
        indices: List<Int>,
        cacheAbuelos: HashMap<Long, MejorParAbuelos>,
    ): List<Linaje> {
        val chars = charsTop
        val m = chars.size
        val ids = idsTop

        /* Mejor par de abuelos para la rama del padre p con hijo h,
           restringido al subconjunto. Reglas del juego: nadie puede ser
           abuelo de su propia rama (g == p prohibido); el hijo sí puede
           ser abuelo pero esa relación vale 0 (corredora). */
        fun mejorPar(h: Int, p: Int): MejorParAbuelos {
            val key = h.toLong() * m + p
            cacheAbuelos[key]?.let { return it }
            var t1 = -1; var t2 = -1; var g1 = -1; var g2 = -1
            for (g in indices) {
                if (g == p) continue
                val t = if (g == h) 0 else puntajeTrioRapido(ids[h], ids[p], ids[g])
                if (t > t1) { t2 = t1; g2 = g1; t1 = t; g1 = g } else if (t > t2) { t2 = t; g2 = g }
            }
            val v = MejorParAbuelos(g1, g2, t1 + t2)
            cacheAbuelos[key] = v
            return v
        }

        val sets = mutableListOf<SetBase>()
        for (i in indices.indices)
            for (j in i + 1 until indices.size)
                for (k in j + 1 until indices.size) {
                    val h = indices[i]
                    val p1 = indices[j]
                    val p2 = indices[k]
                    val base = parEn(h, p1) + parEn(h, p2) + parEn(p1, p2)
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
                val b1 = mejorPar(h, p1)
                val b2 = mejorPar(h, p2)
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

    /* ===== Ranking “Umas más versátiles” (total afinidad) ===== */

    data class RankingAfinidad(
        val personaje: Character,
        val total: Int,
    )

    private var umbralRankingGreat: Int = Int.MIN_VALUE
    private var umbralRankingGood: Int = Int.MIN_VALUE

    private val cacheRanking: List<RankingAfinidad> by lazy { calcularRankingAfinidad() }

    fun rankingAfinidad(): List<RankingAfinidad> = cacheRanking

    fun rangoRanking(total: Int): Rango {
        if (umbralRankingGreat == Int.MIN_VALUE) {
            // Asegura que los umbrales estén calculados (inicializa cache si es necesario)
            if (charsTop.isEmpty()) return Rango("△", "rank-fair")
            rankingAfinidad()
        }
        return when {
            total >= umbralRankingGreat -> Rango("◎", "rank-great")
            total >= umbralRankingGood -> Rango("○", "rank-good")
            else -> Rango("△", "rank-fair")
        }
    }

    private fun calcularRankingAfinidad(): List<RankingAfinidad> {
        val m = charsTop.size
        if (m == 0) {
            umbralRankingGreat = Int.MAX_VALUE
            umbralRankingGood = Int.MAX_VALUE
            return emptyList()
        }
        // Suma por fila de la matriz simétrica (reusa matrizPares/parEn)
        val totales = IntArray(m)
        for (i in 0 until m) {
            var t = 0
            for (j in 0 until m) if (i != j) t += parEn(i, j)
            totales[i] = t
        }
        val ordenados = totales.sortedDescending()
        val idxGreat = (kotlin.math.ceil(m * 0.10).toInt() - 1).coerceIn(0, m - 1)
        val idxGood = (kotlin.math.ceil(m * 0.50).toInt() - 1).coerceIn(0, m - 1)
        umbralRankingGreat = ordenados[idxGreat]
        umbralRankingGood = ordenados[idxGood]
        return (0 until m).map { i -> RankingAfinidad(charsTop[i], totales[i]) }
            .sortedWith(compareByDescending<RankingAfinidad> { it.total }.thenBy { it.personaje.charId })
    }

    /* ===== Alternativas por slot (Mi corredora) ===== */

    /* Total del árbol completo con la semántica de result.js:
       hijo×padres + entre padres + tríos hijo-padre-abuelo (corredora 0). */
    private fun totalDeSeleccion(seleccion: Array<Int?>): Int =
        vinculos(armarArbol(seleccion)).sumOf { v ->
            if (v.esCorredora) 0
            else if (v.ids.size == 3) puntajeTrioRapido(v.ids[0], v.ids[1], v.ids[2])
            else puntajePar(v.ids[0], v.ids[1])
        }

    /* Candidatos para reemplazar el ocupante de un slot (1..6), ordenados
       por total resultante descendente. Respeta todas las reglas del juego
       vía puedeIrEn. El slot del hijo no es intercambiable. */
    fun alternativasParaSlot(
        seleccion: List<Int?>,
        slot: Int,
        limite: Int = 8,
    ): List<AlternativaSlot> {
        if (slot <= 0 || slot >= seleccion.size) return emptyList()
        val ocupante = seleccion[slot] ?: return emptyList()
        val hId = seleccion[0] ?: return emptyList()
        val selArr = seleccion.toTypedArray()

        val resultados = mutableListOf<AlternativaSlot>()
        val m = charsTop.size

        for (idx in 0 until m) {
            val candidato = charsTop[idx]
            if (candidato.charId == ocupante) continue
            if (!puedeIrEn(selArr, slot, candidato.charId)) continue

            /* Aporte directo según el rol del slot. */
            val directos = when (rolDeSlot(slot)) {
                Rol.PADRE -> {
                    val rama = slot - 1
                    val otroPadre = if (slot == 1) seleccion[2]!! else seleccion[1]!!
                    var d = puntajePar(hId, candidato.charId) +
                        puntajePar(candidato.charId, otroPadre)
                    for (g in listOf(seleccion[3 + rama * 2], seleccion[4 + rama * 2])) {
                        if (g != null && g != candidato.charId && g != hId) {
                            d += puntajeTrioRapido(hId, candidato.charId, g)
                        }
                    }
                    d
                }
                Rol.ABUELO -> {
                    val padreId = seleccion[1 + (slot - 3) / 2]!!
                    if (candidato.charId == hId) 0 else puntajeTrioRapido(hId, padreId, candidato.charId)
                }
                else -> continue
            }

            val nuevo = selArr.copyOf().also { it[slot] = candidato.charId }
            resultados += AlternativaSlot(candidato, directos, totalDeSeleccion(nuevo))
        }

        return resultados
            .sortedWith(
                compareByDescending<AlternativaSlot> { it.total }
                    .thenByDescending { it.puntosDirectos },
            )
            .take(limite)
    }
}
