package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.ArbolGuardado
import com.maximillionsnyder.umafinidad.data.deserializarArboles
import com.maximillionsnyder.umafinidad.data.fusionarArbol
import com.maximillionsnyder.umafinidad.data.serializarArboles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/* Tests de persistencia pura de configuraciones guardadas. */

class ArbolesTest {

    private fun ejemplo(id: Long, hijoId: Int, sel: List<Int?>, creadoEn: Long) =
        ArbolGuardado(id = id, hijoId = hijoId, nombre = "test", seleccion = sel, total = 100, creadoEn = creadoEn)

    private val selCompleta = listOf<Int?>(1001, 1015, 1023, 1015, 1045, 1001, 1030)

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
