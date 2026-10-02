package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.domain.ColocacionResultado
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.SlotEstado
import com.maximillionsnyder.umafinidad.domain.agregarEn
import com.maximillionsnyder.umafinidad.domain.alternar
import com.maximillionsnyder.umafinidad.domain.seleccionVacia
import com.maximillionsnyder.umafinidad.domain.slotsPara
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/* Volver a elegir un personaje ya colocado: el dominio es lo que decide si
   un personaje puede estar en dos slots a la vez (el hijo también de abuelo,
   la "corredora" que vale 0) y cuál está libre. Espejo de
   sitio/src/lib/domain/__tests__/herencia.test.ts. */

class HerenciaSlotsTest {

    private fun sel(vararg ids: Int?): Array<Int?> = Array(SLOTS) { i -> ids.getOrNull(i) }

    private fun vacia(): Array<Int?> = seleccionVacia.toTypedArray()

    private fun estados(seleccion: Array<Int?>, id: Int): List<SlotEstado> =
        slotsPara(seleccion, id).map { it.estado }

    @Test
    fun elHijoPuedeSerTambienAbueloPeroNoPadre() {
        val conHijo = sel(1)
        assertEquals(
            listOf(
                SlotEstado.ACTUAL, // hijo
                SlotEstado.BLOQUEADO, // padre 1: el hijo no puede ser padre
                SlotEstado.BLOQUEADO, // padre 2
                SlotEstado.VALIDO, // abuelo 1 rama 1
                SlotEstado.VALIDO,
                SlotEstado.VALIDO,
                SlotEstado.VALIDO,
            ),
            estados(conHijo, 1),
        )
    }

    @Test
    fun agregarUnaSegundaCopiaNoSacaElPersonajeDeSuSlot() {
        val conHijo = sel(1)
        val nueva = agregarEn(conHijo, 3, 1)
        assertEquals(1, nueva?.get(0))
        assertEquals(1, nueva?.get(3))
    }

    @Test
    fun unPadreNoPuedeSerAbueloDeSuPropiaRama() {
        val conPadre = sel(null, 2)
        assertEquals(
            listOf(
                SlotEstado.BLOQUEADO, // hijo: un padre no puede ser el hijo
                SlotEstado.ACTUAL, // padre 1
                SlotEstado.BLOQUEADO, // padre 2: los padres son distintos
                SlotEstado.BLOQUEADO, // abuelo de su rama
                SlotEstado.BLOQUEADO,
                SlotEstado.VALIDO, // la otra rama sí vale
                SlotEstado.VALIDO,
            ),
            estados(conPadre, 2),
        )
    }

    @Test
    fun losDosAbuelosDeUnaMismaRamaNoSeRepiten() {
        val conAbuelo = sel(null, null, null, 5)
        assertEquals(SlotEstado.BLOQUEADO, estados(conAbuelo, 5)[4])
        assertEquals(SlotEstado.VALIDO, estados(conAbuelo, 5)[6])
    }

    @Test
    fun unSlotConOtroPersonajeSaleOcupadoYNoSePuedeAgregar() {
        val conOtro = sel(null, null, null, 9)
        assertEquals(SlotEstado.OCUPADO, slotsPara(conOtro, 5)[3].estado)
        assertNull(agregarEn(conOtro, 3, 5))
    }

    @Test
    fun agregarRespetaElRangoDelSlot() {
        assertNull(agregarEn(vacia(), -1, 1))
        assertNull(agregarEn(vacia(), SLOTS, 1))
    }

    @Test
    fun sinDestinoAlternarQuitaYConDestinoMueve() {
        val colocado = alternar(seleccionVacia, 1).seleccion
        assertEquals(ColocacionResultado.QUITADO, alternar(colocado, 1).resultado)

        val movido = alternar(colocado, 1, 5)
        assertEquals(ColocacionResultado.COLOCADO, movido.resultado)
        /* Mover: se libera el slot viejo (la segunda copia la agrega agregarEn). */
        assertEquals(sel(null, null, null, null, null, 1).toList(), movido.seleccion)
    }

    @Test
    fun conDestinoUnaSeleccionLlenaNoRompe() {
        val llena = sel(1, 2, 3, 4, 5, 6, 7)
        val r = alternar(llena.toList(), 8, 5)
        assertEquals(ColocacionResultado.REGLA, r.resultado)
        assertEquals(llena.toList(), r.seleccion)
    }

    @Test
    fun hayUnSlotValidoCuandoElPersonajeSePuedeRepetir() {
        assertTrue(slotsPara(sel(1), 1).any { it.estado == SlotEstado.VALIDO })
        /* Un padre en su propia rama no tiene dónde ir, pero sí en la otra. */
        assertTrue(slotsPara(sel(null, 2), 2).any { it.estado == SlotEstado.VALIDO })
    }
}