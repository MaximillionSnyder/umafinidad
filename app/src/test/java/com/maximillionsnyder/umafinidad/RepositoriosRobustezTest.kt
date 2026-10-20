package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.data.ArbolGuardado
import com.maximillionsnyder.umafinidad.data.ArbolesRepository
import com.maximillionsnyder.umafinidad.data.ElencoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/* Los repositorios son la primera lectura del arranque. Si SharedPreferences
   tiene basura (JSON truncado, datos de otra versión o un valor de otro
   tipo), tienen que devolver vacío y limpiar la clave, nunca lanzar. */
class RepositoriosRobustezTest {

    private val seleccion = listOf<Int?>(1001, 1015, 1023, 1015, 1045, null, 1030)

    /* Las claves son parte del contrato de persistencia. */
    private val claveArboles = "lista"
    private val claveElenco = "ids"

    private fun arbol(id: Long) = ArbolGuardado(
        id = id,
        hijoId = 1001,
        nombre = "guardado",
        seleccion = seleccion,
        total = 123,
        creadoEn = 456L,
    )

    /* ===== ArbolesRepository ===== */

    @Test
    fun arbolesSinDatosDevuelveVacio() {
        val repo = ArbolesRepository(SharedPreferencesFake())
        assertEquals(emptyList<ArbolGuardado>(), repo.todos())
    }

    @Test
    fun arbolesRoundTripAPreviaPreferencias() {
        val repo = ArbolesRepository(SharedPreferencesFake())
        val lista = listOf(arbol(1L), arbol(2L))
        repo.reemplazarTodos(lista)
        assertEquals(lista, repo.todos())
    }

    @Test
    fun arbolesConJsonCorruptoDevuelveVacioYLimpiaLaClave() {
        val prefs = SharedPreferencesFake()
        prefs.edit().putString(claveArboles, """{"lista":[""").apply()

        assertTrue(ArbolesRepository(prefs).todos().isEmpty())
        assertFalse("la clave corrupta debe limpiarse", prefs.contains(claveArboles))
    }

    @Test
    fun arbolesConValorDeOtroTipoNoCrasheaYLimpiaLaClave() {
        val prefs = SharedPreferencesFake()
        prefs.edit().putString(claveArboles, "[]").apply()
        prefs.lanzarAlLeer = true

        assertTrue(ArbolesRepository(prefs).todos().isEmpty())
        assertFalse(prefs.contains(claveArboles))
    }

    /* Un guardado vacío legítimo también es "[]": no hay que confundirlo con
       corrupción ni borrar la clave. */
    @Test
    fun arbolesConListaVaciaValidaConservaLaClave() {
        val prefs = SharedPreferencesFake()
        ArbolesRepository(prefs).reemplazarTodos(emptyList())

        assertEquals(emptyList<ArbolGuardado>(), ArbolesRepository(prefs).todos())
        assertTrue(prefs.contains(claveArboles))
    }

    /* ===== ElencoRepository ===== */

    @Test
    fun elencoSinDatosDevuelveVacio() {
        val repo = ElencoRepository(SharedPreferencesFake())
        assertEquals(emptySet<Int>(), repo.obtener())
    }

    @Test
    fun elencoRoundTripAPreviaPreferencias() {
        val repo = ElencoRepository(SharedPreferencesFake())
        val ids = setOf(1001, 1015, 1042)
        repo.reemplazar(ids)
        assertEquals(ids, repo.obtener())
    }

    @Test
    fun elencoConJsonCorruptoDevuelveVacioYLimpiaLaClave() {
        val prefs = SharedPreferencesFake()
        prefs.edit().putString(claveElenco, "[1001, 1015").apply()

        assertTrue(ElencoRepository(prefs).obtener().isEmpty())
        assertFalse("la clave corrupta debe limpiarse", prefs.contains(claveElenco))
    }

    @Test
    fun elencoConValorDeOtroTipoNoCrasheaYLimpiaLaClave() {
        val prefs = SharedPreferencesFake()
        prefs.edit().putString(claveElenco, "[]").apply()
        prefs.lanzarAlLeer = true

        assertTrue(ElencoRepository(prefs).obtener().isEmpty())
        assertFalse(prefs.contains(claveElenco))
    }

    @Test
    fun elencoVacioValidoConservaLaClave() {
        val prefs = SharedPreferencesFake()
        ElencoRepository(prefs).reemplazar(emptySet())

        assertEquals(emptySet<Int>(), ElencoRepository(prefs).obtener())
        assertTrue(prefs.contains(claveElenco))
    }
}
