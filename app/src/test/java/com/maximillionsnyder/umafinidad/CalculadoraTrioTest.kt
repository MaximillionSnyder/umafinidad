package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Member
import com.maximillionsnyder.umafinidad.domain.Relation
import com.maximillionsnyder.umafinidad.overlay.SLOTS_TRIO
import com.maximillionsnyder.umafinidad.overlay.TrioEstado
import com.maximillionsnyder.umafinidad.overlay.calcularTrio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/* Tests de la calculadora rápida de la burbuja flotante. */
class CalculadoraTrioTest {

    private fun personaje(id: Int) = Character(id, "Char $id", null, true, true, null)

    /* 1 y 2 comparten los grupos 10 (5pt) y 11 (7pt);
       3 solo comparte el 11 con ellas y tiene el 12 en solitario. */
    private val modelo = AffinityModel(
        characters = listOf(personaje(1), personaje(2), personaje(3)),
        relations = listOf(Relation(10, 5), Relation(11, 7), Relation(12, 2)),
        members = listOf(
            Member(1, 10), Member(2, 10),
            Member(1, 11), Member(2, 11), Member(3, 11),
            Member(3, 12),
        ),
    )

    @Test
    fun llenaElPrimerHueco() {
        val estado = TrioEstado().alternar(1).alternar(2).alternar(3)
        assertEquals(listOf(1, 2, 3), estado.ids)
    }

    @Test
    fun alternarElMismoPersonajeLoQuita() {
        val estado = TrioEstado().alternar(1).alternar(2).alternar(1)
        assertEquals(listOf<Int?>(null, 2, null), estado.ids)
    }

    @Test
    fun noEntraUnCuartoPersonaje() {
        val lleno = TrioEstado().alternar(1).alternar(2).alternar(3)
        assertEquals(lleno.ids, lleno.alternar(4).ids)
        assertEquals(3, SLOTS_TRIO)
    }

    @Test
    fun quitarVaciaEseSlotSolamente() {
        val estado = TrioEstado().alternar(1).alternar(2).alternar(3).quitar(1)
        assertEquals(listOf<Int?>(1, null, 3), estado.ids)
    }

    @Test
    fun limpiarDejaTodoVacio() {
        assertEquals(List<Int?>(SLOTS_TRIO) { null }, TrioEstado().alternar(1).alternar(2).limpiar().ids)
    }

    @Test
    fun conDosElegidosCalculaPar() {
        val resultado = calcularTrio(modelo, TrioEstado().alternar(1).alternar(2))!!
        assertEquals(12, resultado.puntos)
        assertEquals("rank-good", resultado.rango.clase)
        assertEquals(listOf(11, 10), resultado.compartidos.map { it.tipo })
    }

    @Test
    fun conTresElegidosCalculaTrio() {
        val resultado = calcularTrio(
            modelo,
            TrioEstado().alternar(1).alternar(2).alternar(3),
        )!!
        assertEquals(7, resultado.puntos)
        assertEquals("rank-fair", resultado.rango.clase)
        assertEquals(listOf(11), resultado.compartidos.map { it.tipo })
    }

    @Test
    fun conMenosDeDosNoHayResultado() {
        assertNull(calcularTrio(modelo, TrioEstado()))
        assertNull(calcularTrio(modelo, TrioEstado().alternar(1)))
    }
}
