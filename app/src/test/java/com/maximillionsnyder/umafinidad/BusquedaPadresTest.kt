package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.domain.Chispa
import com.maximillionsnyder.umafinidad.domain.Chispas
import com.maximillionsnyder.umafinidad.domain.ColorChispa
import com.maximillionsnyder.umafinidad.domain.Criterios
import com.maximillionsnyder.umafinidad.domain.EstadoPadres
import com.maximillionsnyder.umafinidad.domain.FACTORES_AZULES
import com.maximillionsnyder.umafinidad.domain.FACTORES_ROSA
import com.maximillionsnyder.umafinidad.domain.Padre
import com.maximillionsnyder.umafinidad.domain.Rama
import com.maximillionsnyder.umafinidad.domain.RamaPadre
import com.maximillionsnyder.umafinidad.domain.Requisito
import com.maximillionsnyder.umafinidad.domain.cumpleCriterios
import com.maximillionsnyder.umafinidad.domain.leerTotal
import com.maximillionsnyder.umafinidad.domain.parametrosDeConsulta
import com.maximillionsnyder.umafinidad.domain.resolverBusqueda
import com.maximillionsnyder.umafinidad.domain.topeEstrellas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/* Tests del buscador de padres.
 *
 * Los casos salen de búsquedas reales contra uma.moe, así que fijan el
 * contrato que hace que la pantalla sea puntual: los ids de factor, los topes
 * de estrellas por rama y la diferencia entre "no hay" y "no se encontró". */

class BusquedaPadresTest {

    private val speed = FACTORES_AZULES.first { it.clave == "speed" }
    private val stamina = FACTORES_AZULES.first { it.clave == "stamina" }
    private val medium = FACTORES_ROSA.first { it.clave == "medium" }
    private val frontRunner = FACTORES_ROSA.first { it.clave == "front_runner" }

    private fun req(
        rama: Rama,
        factor: com.maximillionsnyder.umafinidad.domain.FactorChispa,
        min: Int,
        max: Int = min,
    ) = Requisito(rama = rama, factor = factor, minEstrellas = min, maxEstrellas = max)

    /* Registro armado a mano: el padre aporta lo suyo y `combinadas` es el
     * total del linaje, que es una cosa distinta. */
    private fun padre(
        cartaId: Int? = MAYANO_ORIGINAL,
        propiasAzul: List<Chispa> = emptyList(),
        propiasRosa: List<Chispa> = emptyList(),
        combinadasAzul: List<Chispa> = emptyList(),
        combinadasRosa: List<Chispa> = emptyList(),
        entrenador: String? = "alguien",
        afinidad: Int? = 100,
    ) = Padre(
        entrenador = entrenador,
        padre = RamaPadre(cartaId, Chispas(azul = propiasAzul, rosa = propiasRosa)),
        abueloIzquierdo = RamaPadre(null),
        abueloDerecho = RamaPadre(null),
        combinadas = Chispas(azul = combinadasAzul, rosa = combinadasRosa),
        afinidad = afinidad,
    )

    /* ===== ids de factor: id*10 + estrellas ===== */

    @Test
    fun `los ids de factor son los verificados contra el catalogo`() {
        assertEquals(10, speed.id)
        assertEquals(20, stamina.id)
        assertEquals(330, medium.id)
        assertEquals(210, frontRunner.id)
    }

    @Test
    fun `la carta Original es personaje por 100 mas 1`() {
        assertEquals(102401, com.maximillionsnyder.umafinidad.domain.cartaOriginal(1024)) // Mayano
        assertEquals(106801, com.maximillionsnyder.umafinidad.domain.cartaOriginal(1068)) // Kitasan
        assertEquals(100201, com.maximillionsnyder.umafinidad.domain.cartaOriginal(1002)) // Suzuka
    }

    @Test
    fun `un requisito se expande a id por cada estrella del rango`() {
        assertEquals(listOf(103), req(Rama.PADRE, speed, 3).ids())
        assertEquals(listOf(204), req(Rama.LINEA, stamina, 4).ids())
        assertEquals(listOf(3308, 3309), req(Rama.LINEA, medium, 8, 9).ids())
        assertEquals(listOf(2109), req(Rama.LINEA, frontRunner, 9).ids())
    }

    /* ===== topes por rama ===== */

    @Test
    fun `el tope es 9 en el linaje y 3 en una sola uma`() {
        assertEquals(9, topeEstrellas(Rama.LINEA))
        assertEquals(3, topeEstrellas(Rama.PADRE))
        assertEquals(3, topeEstrellas(Rama.IZQUIERDO))
        assertEquals(3, topeEstrellas(Rama.DERECHO))
    }

    @Test
    fun `pedir 5 estrellas del padre es imposible y 9 del linaje no`() {
        assertFalse(req(Rama.PADRE, speed, 5).esPosible())
        assertFalse(req(Rama.PADRE, medium, 4).esPosible())
        assertTrue(req(Rama.LINEA, medium, 9).esPosible())
        assertTrue(req(Rama.PADRE, speed, 3).esPosible())
    }

    @Test
    fun `una busqueda con un requisito imposible no es posible`() {
        val criterios = Criterios(
            cartaPadre = MAYANO_ORIGINAL,
            requisitos = listOf(req(Rama.PADRE, speed, 3), req(Rama.PADRE, medium, 4)),
        )
        assertFalse(criterios.esPosible())
    }

    /* ===== evaluador: suma las estrellas del factor en esa rama ===== */

    @Test
    fun `cumple exige que la suma caiga en el rango pedido`() {
        val r = req(Rama.LINEA, medium, 9)
        assertTrue(r.cumple(listOf(Chispa(medium.id, 9))))
        assertTrue(r.cumple(listOf(Chispa(medium.id, 3), Chispa(medium.id, 3), Chispa(medium.id, 3))))
        assertFalse(r.cumple(listOf(Chispa(medium.id, 8))))
        assertFalse(r.cumple(listOf(Chispa(medium.id, 3)))) // solo el padre, no el linaje
        assertFalse(r.cumple(emptyList()))
    }

    @Test
    fun `un factor ausente nunca cumple`() {
        assertFalse(req(Rama.LINEA, stamina, 1).cumple(listOf(Chispa(speed.id, 9))))
    }

    /* ===== criterios: el padre principal y sus propias chispas ===== */

    @Test
    fun `el personaje del padre se compara contra el id de carta, no el de personaje`() {
        val criterios = Criterios(MAYANO_ORIGINAL, emptyList())
        assertTrue(cumpleCriterios(padre(cartaId = MAYANO_ORIGINAL), criterios))
        assertFalse(cumpleCriterios(padre(cartaId = 106801), criterios)) // Kitasan, no Mayano
        assertFalse(cumpleCriterios(padre(cartaId = null), criterios))
        /* El id de personaje (4 cifras) NO sirve como filtro: el servidor
         * compara contra el de carta. */
        assertFalse(cumpleCriterios(padre(cartaId = 1024), criterios))
    }

    @Test
    fun `un requisito del padre mira sus chispas propias y no las combinadas`() {
        val criterios = Criterios(null, listOf(req(Rama.PADRE, speed, 3)))

        assertTrue(cumpleCriterios(padre(propiasAzul = listOf(Chispa(speed.id, 3))), criterios))
        /* El linaje suma 3 pero el padre no aporta nada: no cumple. */
        assertFalse(cumpleCriterios(padre(combinadasAzul = listOf(Chispa(speed.id, 3))), criterios))
    }

    @Test
    fun `un requisito del linaje mira las combinadas y no las del padre`() {
        val criterios = Criterios(null, listOf(req(Rama.LINEA, stamina, 4)))

        assertTrue(cumpleCriterios(padre(combinadasAzul = listOf(Chispa(stamina.id, 4))), criterios))
        assertFalse(cumpleCriterios(padre(propiasAzul = listOf(Chispa(stamina.id, 3))), criterios))
    }

    @Test
    fun `todos los requisitos deben cumplirse`() {
        val criterios = Criterios(
            cartaPadre = MAYANO_ORIGINAL,
            requisitos = listOf(
                req(Rama.PADRE, speed, 3),
                req(Rama.PADRE, medium, 3),
                req(Rama.LINEA, stamina, 4),
            ),
        )

        val completo = padre(
            propiasAzul = listOf(Chispa(speed.id, 3)),
            propiasRosa = listOf(Chispa(medium.id, 3)),
            combinadasAzul = listOf(Chispa(stamina.id, 4)),
        )
        assertTrue(cumpleCriterios(completo, criterios))

        /* Este es el caso real que dio 0 resultados: Mayano con Speed 3 y
         * Medium 3 propios, y 4 de Stamina en el linaje, pero Medium 2 en el
         * padre. Un solo requisito que falle descarta el registro. */
        val casi = completo.copy(padre = RamaPadre(MAYANO_ORIGINAL, Chispas(
            azul = listOf(Chispa(speed.id, 3)),
            rosa = listOf(Chispa(medium.id, 2)),
        )))
        assertFalse(cumpleCriterios(casi, criterios))
    }

    @Test
    fun `sin requisitos solo filtra el personaje`() {
        val criterios = Criterios(MAYANO_ORIGINAL, emptyList())
        assertTrue(cumpleCriterios(padre(), criterios))
        assertTrue(cumpleCriterios(padre(propiasAzul = listOf(Chispa(speed.id, 3))), criterios))
    }

    /* ===== prefiltro del servidor ===== */

    @Test
    fun `el personaje del padre viaja como main_parent_id`() {
        val params = parametrosDeConsulta(Criterios(MAYANO_ORIGINAL, emptyList()))
        assertEquals(listOf("main_parent_id" to "106801"), params)
    }

    @Test
    fun `una sola chispa por rama y color si viaja al servidor`() {
        val params = parametrosDeConsulta(
            Criterios(MAYANO_ORIGINAL, listOf(req(Rama.PADRE, speed, 3))),
        )
        assertEquals(
            listOf("main_parent_id" to "106801", "main_parent_blue_sparks" to "103"),
            params,
        )
    }

    @Test
    fun `color distinto o rama distinta son parametros distintos y viajan los dos`() {
        val params = parametrosDeConsulta(
            Criterios(
                null,
                listOf(req(Rama.PADRE, speed, 3), req(Rama.PADRE, medium, 3)),
            ),
        )
        assertEquals(
            listOf("main_parent_blue_sparks" to "103", "main_parent_pink_sparks" to "3303"),
            params,
        )

        val porRama = parametrosDeConsulta(
            Criterios(null, listOf(req(Rama.PADRE, speed, 3), req(Rama.LINEA, speed, 3))),
        )
        assertEquals(
            listOf("main_parent_blue_sparks" to "103", "blue_sparks" to "103"),
            porRama,
        )
    }

    /* Este es el caso que hoy devuelve basura en uma.moe: dos factores azules
     * del padre mandados como "103,203" dan 147 resultados, un número
     * incoherente. No se manda ninguno y lo resuelve el filtro local. */
    @Test
    fun `dos chispas del mismo color y rama no viajan al servidor`() {
        val params = parametrosDeConsulta(
            Criterios(
                MAYANO_ORIGINAL,
                listOf(req(Rama.PADRE, speed, 3), req(Rama.PADRE, stamina, 3)),
            ),
        )
        assertEquals(listOf("main_parent_id" to "106801"), params)
    }

    @Test
    fun `los abuelos no tienen parametro propio en la API`() {
        val params = parametrosDeConsulta(
            Criterios(null, listOf(req(Rama.IZQUIERDO, stamina, 3), req(Rama.DERECHO, stamina, 3))),
        )
        assertTrue(params.isEmpty())
    }

    @Test
    fun `un requisito imposible no se manda al servidor`() {
        val params = parametrosDeConsulta(
            Criterios(null, listOf(req(Rama.PADRE, speed, 5))),
        )
        assertTrue(params.isEmpty())
    }

    /* ===== total del servidor ===== */

    @Test
    fun `el total llega como texto y a veces como cota inferior`() {
        assertEquals(16, leerTotal("16").valor)
        assertTrue(leerTotal("16").exacto)

        assertEquals(10000, leerTotal("over 10000").valor)
        assertFalse(leerTotal("over 10000").exacto)
        assertFalse(leerTotal("OVER 5000").exacto)
        assertEquals(5000, leerTotal("OVER 5000").valor)

        assertFalse(leerTotal(null).exacto)
        assertFalse(leerTotal("").exacto)
        assertFalse(leerTotal("basura").exacto)
    }

    /* ===== la decisión final: "no hay" contra "no se encontró" ===== */

    @Test
    fun `con resultados el estado es HAY`() {
        val criterios = Criterios(null, listOf(req(Rama.PADRE, speed, 3)))
        val salida = resolverBusqueda(
            criterios = criterios,
            escaneados = listOf(
                padre(propiasAzul = listOf(Chispa(speed.id, 3))),
                padre(propiasAzul = listOf(Chispa(speed.id, 2))),
            ),
            total = com.maximillionsnyder.umafinidad.domain.TotalServidor(2, true),
            completo = true,
        )
        assertEquals(EstadoPadres.HAY, salida.estado)
        assertEquals(1, salida.padres.size)
        assertEquals(2, salida.revisados)
    }

    @Test
    fun `sin resultados y universo agotado el estado es NO_HAY`() {
        val criterios = Criterios(null, listOf(req(Rama.PADRE, medium, 4)))
        val salida = resolverBusqueda(
            criterios = criterios,
            escaneados = listOf(padre(), padre()),
            total = com.maximillionsnyder.umafinidad.domain.TotalServidor(2, true),
            completo = true,
        )
        assertEquals(EstadoPadres.NO_HAY, salida.estado)
        assertTrue(salida.padres.isEmpty())
    }

    /* La distinción que evita que la pantalla mienta: cortar por el tope NO
     * autoriza a decir "no hay". */
    @Test
    fun `sin resultados pero con el universo sin agotar el estado es NO_SE_ENCONTRO`() {
        val criterios = Criterios(null, listOf(req(Rama.PADRE, speed, 3)))
        val salida = resolverBusqueda(
            criterios = criterios,
            escaneados = listOf(padre(propiasAzul = listOf(Chispa(speed.id, 2)))),
            total = com.maximillionsnyder.umafinidad.domain.TotalServidor(10000, false),
            completo = false,
        )
        assertEquals(EstadoPadres.NO_SE_ENCONTRO, salida.estado)
        assertTrue(salida.padres.isEmpty())
        assertEquals(1, salida.revisados)
        assertEquals(10000, salida.total)
        assertFalse(salida.totalExacto)
    }

    private companion object {
        const val MAYANO_ORIGINAL = 102401
    }
}
