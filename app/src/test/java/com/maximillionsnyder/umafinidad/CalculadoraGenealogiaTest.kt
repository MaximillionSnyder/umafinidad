package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Member
import com.maximillionsnyder.umafinidad.domain.Relation
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.overlay.ColocacionResultado
import com.maximillionsnyder.umafinidad.overlay.alternar
import com.maximillionsnyder.umafinidad.overlay.quitar
import com.maximillionsnyder.umafinidad.overlay.seleccionVacia
import com.maximillionsnyder.umafinidad.overlay.totalDe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/* Tests de la calculadora de genealogía de la burbuja flotante. */
class CalculadoraGenealogiaTest {

    private fun personaje(id: Int) = Character(id, "Char $id", null, true, true, null)

    /* 1 y 2 comparten los grupos 10 (5pt) y 11 (7pt); 3 y 4 solo el 11;
       3 tiene además el 12 en solitario. */
    private val modelo = AffinityModel(
        characters = (1..7).map { personaje(it) },
        relations = listOf(Relation(10, 5), Relation(11, 7), Relation(12, 2)),
        members = listOf(
            Member(1, 10), Member(2, 10),
            Member(1, 11), Member(2, 11), Member(3, 11), Member(4, 11),
            Member(3, 12),
        ),
    )

    private fun llenos(): List<Int?> {
        var seleccion = seleccionVacia
        for (id in 1..7) {
            val colocacion = alternar(seleccion, id)
            assertEquals(ColocacionResultado.COLOCADO, colocacion.resultado)
            seleccion = colocacion.seleccion
        }
        return seleccion
    }

    @Test
    fun laSeleccionVaciaTieneSieteHuecos() {
        assertEquals(SLOTS, seleccionVacia.size)
        assertEquals(List<Int?>(SLOTS) { null }, seleccionVacia)
    }

    @Test
    fun colocaEnElPrimerHuecoValido() {
        val colocacion = alternar(seleccionVacia, 1)
        assertEquals(ColocacionResultado.COLOCADO, colocacion.resultado)
        assertEquals(1, colocacion.seleccion[0])
        assertEquals(1, colocacion.seleccion.count { it != null })
    }

    @Test
    fun alternarElMismoPersonajeLoQuita() {
        val colocado = alternar(seleccionVacia, 1).seleccion
        val quitado = alternar(colocado, 1)
        assertEquals(ColocacionResultado.QUITADO, quitado.resultado)
        assertEquals(seleccionVacia, quitado.seleccion)
    }

    @Test
    fun llenaLosSieteSlotsYElOctavoNoEntra() {
        val seleccion = llenos()
        assertEquals(List(7) { it + 1 }, seleccion)

        val octavo = alternar(seleccion, 8)
        assertEquals(ColocacionResultado.COMPLETA, octavo.resultado)
        assertEquals(seleccion, octavo.seleccion)
    }

    @Test
    fun quitarVaciaEseSlotSolamente() {
        val seleccion = quitar(llenos(), 1)
        assertEquals(listOf<Int?>(1, null, 3, 4, 5, 6, 7), seleccion)
    }

    @Test
    fun sinVinculosNoHayTotal() {
        /* Hijo y abuelo sin padres: todavía no hay ningún vínculo. */
        assertNull(totalDe(modelo, listOf(1, null, null, 2, null, null, null)))
        assertNull(totalDe(modelo, seleccionVacia))
    }

    @Test
    fun elTotalSumaVinculosDelArbol() {
        /* HP(1,2)=12 + HP(1,3)=7 + EP(2,3)=7 + HPA(1,2,4)=7. */
        val total = totalDe(modelo, listOf(1, 2, 3, 4, null, null, null))
        assertEquals(33, total)
    }

    @Test
    fun laMismaCorredoraComoAbueloValeCero() {
        /* El vínculo hijo-padre-abuelo donde el abuelo es el propio hijo
           (la corredora) no suma. */
        val total = totalDe(modelo, listOf(1, 2, null, 1, null, null, null))
        assertEquals(12, total)
    }

    /* ---- Slot destino ---- */

    @Test
    fun colocaEnElSlotElegidoAunqueNoSeaElProximo() {
        /* Con solo el hijo cargado, el destino Abuelo 1 · Padre 2 (slot 5)
           recibe al personaje sin pasar por los padres. */
        val base = listOf<Int?>(1, null, null, null, null, null, null)
        val colocacion = alternar(base, 2, destino = 5)
        assertEquals(ColocacionResultado.COLOCADO, colocacion.resultado)
        assertEquals(listOf<Int?>(1, null, null, null, null, 2, null), colocacion.seleccion)
    }

    @Test
    fun moverAlDestinoLiberaElSlotViejo() {
        /* 3 estaba como Abuelo 2 · Padre 2 (slot 6): se mueve a Padre 1. */
        val base = listOf<Int?>(1, null, null, null, null, null, 3)
        val colocacion = alternar(base, 3, destino = 1)
        assertEquals(ColocacionResultado.COLOCADO, colocacion.resultado)
        assertEquals(listOf<Int?>(1, 3, null, null, null, null, null), colocacion.seleccion)
    }

    @Test
    fun elDestinoOcupadoPorOtroRechazaLaColocacion() {
        val base = listOf<Int?>(1, 2, null, null, null, null, null)
        val colocacion = alternar(base, 3, destino = 1)
        assertEquals(ColocacionResultado.REGLA, colocacion.resultado)
        assertEquals(base, colocacion.seleccion)
    }

    @Test
    fun conDestinoMandaElDestinoYNoElOrden() {
        val colocacion = alternar(seleccionVacia, 1, destino = 2)
        assertEquals(ColocacionResultado.COLOCADO, colocacion.resultado)
        assertEquals(listOf<Int?>(null, null, 1, null, null, null, null), colocacion.seleccion)
    }

    @Test
    fun unDestinoFueraDeRangoNoRompe() {
        val colocacion = alternar(seleccionVacia, 1, destino = 99)
        assertEquals(ColocacionResultado.REGLA, colocacion.resultado)
        assertEquals(seleccionVacia, colocacion.seleccion)
    }
}
