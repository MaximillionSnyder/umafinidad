package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Member
import com.maximillionsnyder.umafinidad.domain.Relation
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.puedeIrEn
import org.junit.Assert.assertEquals
import org.junit.Test

/* El autocompletar debe ser exacto: se compara contra fuerza bruta sobre
   todas las completaciones posibles (incluido dejar huecos). */
class CompletarSeleccionTest {

    private fun personaje(id: Int) = Character(id, "Char $id", null, true, true, null)

    /* Membresías variadas para que ningún atajo alcance por casualidad. */
    private val modelo = AffinityModel(
        characters = (1..6).map { personaje(it) },
        relations = listOf(Relation(10, 5), Relation(11, 7), Relation(12, 2), Relation(13, 9)),
        members = listOf(
            Member(1, 10), Member(1, 11),
            Member(2, 10), Member(2, 12),
            Member(3, 11), Member(3, 13),
            Member(4, 10), Member(4, 13),
            Member(5, 12), Member(5, 13),
            Member(6, 11), Member(6, 12),
        ),
    )

    private fun mejorBruto(base: List<Int?>): Int {
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
        assertEquals(mejorBruto(base), modelo.totalDeSeleccion(resultado))
    }

    @Test
    fun sinHijoNoCambiaNada() {
        val vacia = List<Int?>(SLOTS) { null }
        assertEquals(vacia, modelo.completarSeleccion(vacia))
        assertEquals(listOf<Int?>(null, 1, null, null, null, null, null), modelo.completarSeleccion(listOf(null, 1, null, null, null, null, null)))
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
        /* Caso de uso: Oguri + padre1 fijo + abuelos de esa rama; falta la
           madre y sus abuelos. */
        verificar(listOf(1, 2, null, 3, 4, null, null))
    }

    @Test
    fun conUnPadreFaltanteEsOptimo() {
        verificar(listOf(1, 2, null, null, null, null, null))
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
