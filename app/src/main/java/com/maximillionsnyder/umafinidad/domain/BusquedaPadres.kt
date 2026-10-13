package com.maximillionsnyder.umafinidad.domain

/* Búsqueda exacta de padres (veteranos que la comunidad comparte) en uma.moe.
 *
 * El endpoint de uma.moe NO es confiable para filtros exactos: hay
 * combinaciones que no sabe expresar (dos factores del mismo color y rama se
 * mandan como lista separada por comas y el servidor no las interpreta como
 * AND) y el tope de estrellas cambia según el parámetro (9 en los combinados,
 * 3 en los de una sola rama).
 *
 * Por eso el diseño es de dos etapas:
 *   1. `parametrosDeConsulta` arma un PREFILTRO con los parámetros cuya
 *      semántica está verificada. Sirve para bajar el volumen de datos.
 *   2. `cumpleCriterios` es el filtro AUTORITATIVO: se evalúa sobre cada
 *      registro que vuelve y decide qué se muestra. Todo lo que la app
 *      muestra cumple todos los criterios, sin depender del servidor.
 *
 * Este archivo es Kotlin puro: no depende de Android y se testea con JUnit. */

/* ------------------------------------------------------------ catálogo */

enum class ColorChispa {
    AZUL,
    ROSA,
    VERDE,
    BLANCO,
}

/* Un factor con id estable en el catálogo público de uma.moe
 * (resources/current/factors.json.gz). `clave` es la clave de traducción: el
 * dominio no conoce recursos de Android, la UI mapea clave -> string. */
data class FactorChispa(
    val id: Int,
    val clave: String,
    val color: ColorChispa,
)

/* Los 5 stats azules (type=0 en el catálogo). */
val FACTORES_AZULES: List<FactorChispa> = listOf(
    FactorChispa(10, "speed", ColorChispa.AZUL),
    FactorChispa(20, "stamina", ColorChispa.AZUL),
    FactorChispa(30, "power", ColorChispa.AZUL),
    FactorChispa(40, "guts", ColorChispa.AZUL),
    FactorChispa(50, "wit", ColorChispa.AZUL),
)

/* Las 10 aptitudes rosas (type=1 en el catálogo). */
val FACTORES_ROSA: List<FactorChispa> = listOf(
    FactorChispa(110, "turf", ColorChispa.ROSA),
    FactorChispa(120, "dirt", ColorChispa.ROSA),
    FactorChispa(210, "front_runner", ColorChispa.ROSA),
    FactorChispa(220, "pace_chaser", ColorChispa.ROSA),
    FactorChispa(230, "late_surger", ColorChispa.ROSA),
    FactorChispa(240, "end_closer", ColorChispa.ROSA),
    FactorChispa(310, "sprint", ColorChispa.ROSA),
    FactorChispa(320, "mile", ColorChispa.ROSA),
    FactorChispa(330, "medium", ColorChispa.ROSA),
    FactorChispa(340, "long", ColorChispa.ROSA),
)

/* Factores que ofrece la pantalla en esta primera versión. */
val FACTORES_DISPONIBLES: List<FactorChispa> = FACTORES_AZULES + FACTORES_ROSA

fun factorPorId(id: Int): FactorChispa? = FACTORES_DISPONIBLES.firstOrNull { it.id == id }

/* El filtro del servidor compara contra el id de CARTA (6 cifras: 102401),
 * no contra el de personaje (4 cifras: 1024). Verificado: con el de personaje
 * el endpoint devuelve 0 resultados.
 *
 * La skin "Original" es la 01, así que la carta es personaje*100 + 1. Esta
 * primera versión solo busca la Original; elegir skin queda para más adelante. */
fun cartaOriginal(charId: Int): Int = charId * 100 + 1

/* ------------------------------------------------------------- criterios */

/* Rama del linaje sobre la que se pide el requisito. */
enum class Rama {
    PADRE,     // las chispas propias del padre (main_parent_* en la API)
    LINEA,     // el total combinado de las tres umas (*_sparks)
    IZQUIERDO, // el abuelo izquierdo (solo filtro local: la API no lo expone)
    DERECHO,   // el abuelo derecho (solo filtro local)
}

/* Tope de estrellas por rama, verificado contra el armador de consultas de
 * uma.moe: los parámetros combinados aceptan hasta 9 (3+3+3) y los de una
 * sola rama hasta 3, porque una uma no puede pasar de 3 estrellas por factor. */
fun topeEstrellas(rama: Rama): Int = if (rama == Rama.LINEA) 9 else 3

data class Requisito(
    val rama: Rama,
    val factor: FactorChispa,
    val minEstrellas: Int,
    val maxEstrellas: Int,
) {
    init {
        require(minEstrellas in 1..9) { "minEstrellas fuera de 1..9: $minEstrellas" }
        require(maxEstrellas in minEstrellas..9) { "maxEstrellas fuera de rango: $maxEstrellas" }
    }

    /* El factor con sus estrellas, tal como lo codifica uma.moe: id*10+n. */
    fun ids(): List<Int> = (minEstrellas..maxEstrellas).map { factor.id * 10 + it }

    /* Suma las estrellas de ese factor dentro de la rama y exige que caiga en
     * el rango pedido. */
    fun cumple(chispas: List<Chispa>): Boolean {
        val suma = chispas.filter { it.factorId == factor.id }.sumOf { it.estrellas }
        return suma in minEstrellas..maxEstrellas
    }

    /* Si pide más estrellas de las que la rama admite, es imposible y no hace
     * falta ni salir a la red: la respuesta es "no hay". */
    fun esPosible(): Boolean = minEstrellas <= topeEstrellas(rama)
}

data class Criterios(
    val cartaPadre: Int?,
    val requisitos: List<Requisito>,
) {
    val vacio: Boolean get() = cartaPadre == null && requisitos.isEmpty()

    /* Un criterio imposible vuelve imposible a toda la búsqueda. */
    fun esPosible(): Boolean = requisitos.all { it.esPosible() }
}

/* --------------------------------------------------------------- registros */

/* Una chispa ya decodificada: el id del factor y sus estrellas. */
data class Chispa(
    val factorId: Int,
    val estrellas: Int,
)

data class Chispas(
    val azul: List<Chispa> = emptyList(),
    val rosa: List<Chispa> = emptyList(),
    val verde: List<Chispa> = emptyList(),
    val blanco: List<Chispa> = emptyList(),
) {
    fun de(color: ColorChispa): List<Chispa> = when (color) {
        ColorChispa.AZUL -> azul
        ColorChispa.ROSA -> rosa
        ColorChispa.VERDE -> verde
        ColorChispa.BLANCO -> blanco
    }
}

data class RamaPadre(
    val cartaId: Int?,
    val chispas: Chispas = Chispas(),
)

data class Padre(
    val entrenador: String?,
    val padre: RamaPadre,
    val abueloIzquierdo: RamaPadre,
    val abueloDerecho: RamaPadre,
    val combinadas: Chispas,
    val afinidad: Int? = null,
    val estrellasAzul: Int? = null,
    val estrellasRosa: Int? = null,
    val estrellasVerde: Int? = null,
    val estrellasBlanco: Int? = null,
)

private fun chispasDe(padre: Padre, rama: Rama): Chispas = when (rama) {
    Rama.PADRE -> padre.padre.chispas
    Rama.LINEA -> padre.combinadas
    Rama.IZQUIERDO -> padre.abueloIzquierdo.chispas
    Rama.DERECHO -> padre.abueloDerecho.chispas
}

/* Filtro autoritativo. Se evalúa registro por registro sobre lo que devolvió
 * el servidor: lo que pasa esta función es lo único que se muestra. */
fun cumpleCriterios(padre: Padre, criterios: Criterios): Boolean {
    if (criterios.cartaPadre != null && padre.padre.cartaId != criterios.cartaPadre) return false
    return criterios.requisitos.all { requisito ->
        requisito.cumple(chispasDe(padre, requisito.rama).de(requisito.factor.color))
    }
}

/* ------------------------------------------------------------- prefiltro */

/* Parámetros de /search/query. Solo se manda un requisito por rama+color: si
 * hay más de uno, el servidor recibiría una lista separada por comas que no
 * interpreta como AND, así que se omite y lo resuelve el filtro local.
 *
 * Los abuelos no tienen parámetro propio en la API: sus requisitos siempre
 * quedan para el filtro local. */
fun parametrosDeConsulta(criterios: Criterios): List<Pair<String, String>> {
    val parametros = mutableListOf<Pair<String, String>>()
    criterios.cartaPadre?.let { parametros += "main_parent_id" to it.toString() }

    val porGrupo = criterios.requisitos.groupBy { it.rama to it.factor.color }
    for ((_, grupo) in porGrupo) {
        if (grupo.size != 1) continue
        val requisito = grupo.first()
        val nombre = nombreDeParametro(requisito.rama, requisito.factor.color) ?: continue
        if (!requisito.esPosible()) continue
        parametros += nombre to requisito.ids().joinToString(",")
    }
    return parametros
}

/* Nombre del parámetro de chispas para una rama, o null si la API no lo
 * expone (los abuelos). */
fun nombreDeParametro(rama: Rama, color: ColorChispa): String? {
    val sufijo = when (color) {
        ColorChispa.AZUL -> "blue_sparks"
        ColorChispa.ROSA -> "pink_sparks"
        ColorChispa.VERDE -> "green_sparks"
        ColorChispa.BLANCO -> "white_sparks"
    }
    return when (rama) {
        Rama.LINEA -> sufijo
        Rama.PADRE -> "main_parent_$sufijo"
        Rama.IZQUIERDO, Rama.DERECHO -> null
    }
}

/* ---------------------------------------------------------------- salida */

/* El servidor manda `total` como texto: "16", o "over 10000" cuando hay más
 * resultados de los que cuenta. El que viene exacto es `total_pages`. */
data class TotalServidor(val valor: Int, val exacto: Boolean)

fun leerTotal(crudo: String?): TotalServidor {
    val texto = crudo?.trim().orEmpty()
    texto.toIntOrNull()?.let { return TotalServidor(it, true) }
    val exceso = Regex("^over\\s+(\\d+)$", RegexOption.IGNORE_CASE).find(texto)
    if (exceso != null) return TotalServidor(exceso.groupValues[1].toInt(), false)
    return TotalServidor(0, false)
}

enum class EstadoPadres {
    SIN_CRITERIOS,
    BUSCANDO,
    HAY,
    NO_HAY,        // definitivo: se revisó el universo entero y ninguno cumple
    NO_SE_ENCONTRO, // se llegó al tope sin agotar el universo: no afirma nada
    ERROR,
}

enum class ErrorPadres {
    SIN_KEY,
    KEY_RECHAZADA,
    SIN_RED,
    SERVIDOR,
    RESPUESTA_INVALIDA,
}

data class SalidaPadres(
    val estado: EstadoPadres,
    val padres: List<Padre> = emptyList(),
    val revisados: Int = 0,
    val total: Int = 0,
    val totalExacto: Boolean = true,
    val error: ErrorPadres? = null,
)

/* Decide el estado a partir de lo efectivamente revisado.
 *
 * `completo` significa que se recorrió el universo entero de resultados. Solo
 * entonces "no hay" es una afirmación definitiva; si se cortó por el tope, la
 * respuesta honesta es "no se encontró en lo revisado". Confundir las dos es
 * justamente lo que haría mentir a la pantalla. */
fun resolverBusqueda(
    criterios: Criterios,
    escaneados: List<Padre>,
    total: TotalServidor,
    completo: Boolean,
): SalidaPadres {
    val cumplen = escaneados.filter { cumpleCriterios(it, criterios) }
    if (cumplen.isNotEmpty()) {
        return SalidaPadres(
            estado = EstadoPadres.HAY,
            padres = cumplen,
            revisados = escaneados.size,
            total = total.valor,
            totalExacto = total.exacto,
        )
    }
    return SalidaPadres(
        estado = if (completo) EstadoPadres.NO_HAY else EstadoPadres.NO_SE_ENCONTRO,
        padres = emptyList(),
        revisados = escaneados.size,
        total = total.valor,
        totalExacto = total.exacto,
    )
}
