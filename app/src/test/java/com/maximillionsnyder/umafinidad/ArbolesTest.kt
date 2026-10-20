package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.ArbolGuardado
import com.maximillionsnyder.umafinidad.data.crearArbolGuardado
import com.maximillionsnyder.umafinidad.data.decodificarArboles
import com.maximillionsnyder.umafinidad.data.deserializarArboles
import com.maximillionsnyder.umafinidad.data.fusionarArbol
import com.maximillionsnyder.umafinidad.data.serializarArboles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/* Tests de persistencia pura de configuraciones guardadas. */

class ArbolesTest {

    private fun ejemplo(id: Long, hijoId: Int, sel: List<Int?>, creadoEn: Long) =
        ArbolGuardado(id = id, hijoId = hijoId, nombre = "test", seleccion = sel, total = 100, creadoEn = creadoEn)

    private val selCompleta = listOf<Int?>(1001, 1015, 1023, 1015, 1045, null, 1030)
    private val selIncompleta = listOf<Int?>(1001, 1015, 1023, null, null, null, null)

    @Test
    fun roundTripConNulls() {
        val lista = listOf(ejemplo(1L, 1001, selCompleta, 100L))
        val vuelta = deserializarArboles(serializarArboles(lista))
        assertEquals(lista, vuelta)
        assertTrue(vuelta[0].seleccion.contains(null as Int?))
    }

    @Test
    fun listaVaciaRoundTrip() {
        assertEquals(emptyList<ArbolGuardado>(), deserializarArboles(serializarArboles(emptyList())))
    }

    /* ===== Deserialización segura (prefs corruptas) ===== */

    @Test
    fun jsonInvalidoDevuelveListaVacia() {
        assertEquals(emptyList<ArbolGuardado>(), deserializarArboles("{esto no es json"))
        assertEquals(emptyList<ArbolGuardado>(), deserializarArboles(""))
        assertEquals(emptyList<ArbolGuardado>(), deserializarArboles("null"))
    }

    @Test
    fun jsonConEstructuraEquivocadaDevuelveListaVacia() {
        assertEquals(emptyList<ArbolGuardado>(), deserializarArboles("""{"lista":[]}"""))
        assertEquals(emptyList<ArbolGuardado>(), deserializarArboles("""[{"id":1}]"""))
    }

    @Test
    fun decodificarDistingueCorruptoDeListaVacia() {
        assertNull(decodificarArboles("{corrupto"))
        assertEquals(emptyList<ArbolGuardado>(), decodificarArboles("[]"))
    }

    /* ===== Ids únicos (dos guardados en el mismo milisegundo) ===== */

    @Test
    fun dosGuardadosEnElMismoMilisegundoTienenIdsDistintos() {
        val ahora = 1_700_000_000_000L
        val primero = crearArbolGuardado(emptyList(), 1001, "uno", selCompleta, 100, ahora)
        val segundo = crearArbolGuardado(listOf(primero), 1042, "dos", selCompleta, 100, ahora)

        assertNotEquals(primero.id, segundo.id)
        assertTrue("el segundo id debe avanzar", segundo.id > primero.id)
        /* creadoEn es la fecha visible, independiente del id. */
        assertEquals(ahora, primero.creadoEn)
        assertEquals(ahora, segundo.creadoEn)
    }

    @Test
    fun elIdNoChocaConIdsGuardadosMayoresQueElReloj() {
        val ahora = 1_700_000_000_000L
        val guardado = ejemplo(ahora + 500, 1001, selCompleta, ahora)
        val nuevo = crearArbolGuardado(listOf(guardado), 1042, "nuevo", selCompleta, 100, ahora)

        assertEquals(ahora + 501, nuevo.id)
        assertEquals(ahora, nuevo.creadoEn)
    }

    @Test
    fun elIdAvanzaSiLosUltimosIdYaEstanOcupados() {
        val ahora = 1_700_000_000_000L
        val lista = listOf(
            ejemplo(ahora, 1001, selCompleta, ahora),
            ejemplo(ahora + 1, 1042, selCompleta, ahora),
        )
        assertEquals(ahora + 2, crearArbolGuardado(lista, 1071, "otro", selCompleta, 100, ahora).id)
    }

    /* El bug original: dos configuraciones distintas del mismo hijo guardadas
       en el mismo milisegundo compartían id y eliminar una borraba ambas. */
    @Test
    fun dosArbolesDelMismoHijoYMilisegundoNoSePisanAlEliminar() {
        val ahora = 1_700_000_000_000L
        val primero = crearArbolGuardado(emptyList(), 1001, "uno", selCompleta, 100, ahora)
        val segundo = crearArbolGuardado(listOf(primero), 1001, "dos", selIncompleta, 80, ahora)
        val fusion = fusionarArbol(listOf(primero), segundo)

        assertEquals(2, fusion.size)
        assertEquals(2, fusion.map { it.id }.toSet().size)

        val resto = fusion.filterNot { it.id == segundo.id }
        assertEquals(listOf(primero), resto)
    }

    @Test
    fun elNombreSeGuardaSinEspaciosSobrantes() {
        val nuevo = crearArbolGuardado(emptyList(), 1001, "  mi corredora  ", selCompleta, 100, 1L)
        assertEquals("mi corredora", nuevo.nombre)
    }

    /* ===== Dedupe y orden ===== */

    @Test
    fun dedupeSeleccionIdenticaReemplaza() {
        val vieja = ejemplo(1L, 1001, selCompleta, 100L)
        val nueva = ejemplo(2L, 1001, selCompleta, 200L)
        val fusion = fusionarArbol(listOf(vieja), nueva)
        assertEquals(1, fusion.size)
        assertEquals(2L, fusion[0].id)
    }

    @Test
    fun seleccionDistintaAgrega() {
        val a = ejemplo(1L, 1001, selCompleta, 100L)
        val b = ejemplo(2L, 1001, listOf(1001, 1015, 1023, null, null, null, null), 200L)
        val fusion = fusionarArbol(listOf(a), b)
        assertEquals(2, fusion.size)
    }

    @Test
    fun otroHijoNoSeConsideraDuplicado() {
        val a = ejemplo(1L, 1001, selCompleta, 100L)
        val b = ejemplo(2L, 1042, selCompleta, 200L)
        assertEquals(2, fusionarArbol(listOf(a), b).size)
    }

    @Test
    fun ordenadoPorFechaDescendente() {
        val vieja = ejemplo(1L, 1001, selCompleta, 100L)
        val media = ejemplo(2L, 1042, selCompleta, 150L)
        val nueva = ejemplo(3L, 1071, selCompleta, 200L)
        val fusion = fusionarArbol(listOf(vieja, media), nueva)
        assertEquals(listOf(3L, 2L, 1L), fusion.map { it.id })
    }
}
