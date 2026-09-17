package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Member
import com.maximillionsnyder.umafinidad.domain.Relation
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.puedeIrEn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/* El autocompletar debe ser exacto y nunca proponer al hijo como abuelo
   (como corredora aporta 0): se compara contra fuerza bruta sobre todas las
   completaciones posibles que respetan esa regla. */
class CompletarSeleccionTest {

    private fun personaje(id: Int) = Character(id, "Char $id", null, true, true, null)

    /* Cada rama tiene al menos dos candidatos positivos, así un ranking mal
       armado (por ejemplo con el hijo puntuado como par) cambia el total. */
    private val modelo = AffinityModel(
        characters = (1..6).map { personaje(it) },
        relations = listOf(Relation(10, 5), Relation(11, 7), Relation(12, 2), Relation(13, 9)),
        members = listOf(
            Member(1, 10), Member(1, 11),
            Member(2, 10), Member(2, 11),
            Member(3, 10), Member(3, 12),
            Member(4, 10), Member(4, 13),
            Member(5, 11), Member(5, 12),
            Member(6, 11), Member(6, 13),
        ),
    )

    private fun mejorBruto(base: List<Int?>): Int {
        val hijo = base[0]
        var mejor = -1
        fun rec(sel: MutableList<Int?>, slot: Int) {
            if (slot == SLOTS) {
                val total = modelo.totalDeSeleccion(sel)
                if (total > mejor) mejor = total
                return
            }
            if (sel[slot] != null) {
                rec(sel, slot + 1)
                return
            }
            rec(sel, slot + 1) /* dejarlo vacío también es una completación */
            for (id in 1..6) {
                if (slot >= 3 && id == hijo) continue /* el hijo no va de abuelo */
                if (puedeIrEn(sel.toTypedArray(), slot, id)) {
                    sel[slot] = id
                    rec(sel, slot + 1)
                    sel[slot] = null
                }
            }
        }
        rec(base.toMutableList(), 0)
        return mejor
    }

    private fun verificar(base: List<Int?>) {
        val resultado = modelo.completarSeleccion(base)
        for (i in base.indices) {
            if (base[i] != null) assertEquals("slot $i fijo", base[i], resultado[i])
        }
        /* El hijo nunca queda propuesto como abuelo. */
        if (base[0] != null) {
            for (slot in 3 until SLOTS) {
                assertNotEquals("el hijo no va de abuelo", base[0], resultado[slot])
            }
        }
        assertEquals(mejorBruto(base), modelo.totalDeSeleccion(resultado))
    }

    @Test
    fun sinHijoNoCambiaNada() {
        val vacia = List<Int?>(SLOTS) { null }
        assertEquals(vacia, modelo.completarSeleccion(vacia))
        assertEquals(
            listOf<Int?>(null, 1, null, null, null, null, null),
            modelo.completarSeleccion(listOf(null, 1, null, null, null, null, null)),
        )
    }

    @Test
    fun soloConHijoEsOptimo() {
        verificar(listOf(1, null, null, null, null, null, null))
    }

    @Test
    fun conLosDosPadresFijosEsOptimo() {
        verificar(listOf(1, 2, 3, null, null, null, null))
    }

    @Test
    fun conUnPadreFijoYAbuelosFijosEsOptimo() {
        verificar(listOf(1, 2, null, 3, 4, null, null))
    }

    /* Regresión: hijo + un padre fijo (caso Oguri + Vodka). El autocompletar
       no debe poner al hijo de abuelo ni perder puntos por eso. */
    @Test
    fun conUnPadreFaltanteNoUsaAlHijoDeAbuelo() {
        val base = listOf<Int?>(1, 2, null, null, null, null, null)
        val resultado = modelo.completarSeleccion(base)
        for (slot in 3 until SLOTS) assertNotEquals(base[0], resultado[slot])
        verificar(base)
    }

    @Test
    fun conAbuelosFijosYPadresFaltantesEsOptimo() {
        /* Ambos padres faltan y hay abuelos fijos en las dos ramas. */
        verificar(listOf(1, null, null, 2, 3, 4, 5))
    }

    @Test
    fun conCasiTodoLlenoEsOptimo() {
        verificar(listOf(1, 2, 3, 4, 5, 6, null))
    }

    @Test
    fun conservaLosFijosYSoloLlenaLosHuecos() {
        val base = listOf<Int?>(1, 2, null, 3, 4, null, null)
        val resultado = modelo.completarSeleccion(base)
        assertEquals(1, resultado[0])
        assertEquals(2, resultado[1])
        assertEquals(3, resultado[3])
        assertEquals(4, resultado[4])
        assertEquals(0, resultado.count { it == null })
    }
}
