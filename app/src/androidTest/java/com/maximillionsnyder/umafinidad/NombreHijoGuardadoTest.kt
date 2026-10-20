package com.maximillionsnyder.umafinidad

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.maximillionsnyder.umafinidad.data.AffinityRepository
import com.maximillionsnyder.umafinidad.ui.settings.nombreHijoGuardado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

/* El nombre que muestra un árbol guardado es lógica pura: se verifica el
   fallback sin recomponer la pantalla (ver AjustesSemanticaTest para el
   recorrido completo de la UI). */
@RunWith(AndroidJUnit4::class)
class NombreHijoGuardadoTest {

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val modelo by lazy { AffinityRepository(contexto).modelo }

    @Test
    fun hijoDelModeloUsaSuNombreVisible() {
        val personaje = modelo.personajes.first { it.playable == true && it.active == true }

        assertEquals(
            personaje.displayName(japones = false),
            nombreHijoGuardado(modelo, personaje.charId, japones = false),
        )
        assertEquals(
            personaje.displayName(japones = true),
            nombreHijoGuardado(modelo, personaje.charId, japones = true),
        )
    }

    /* Regresión: el "$" escapado hacía que la rama de fallback mostrara el
       template literal "#${a.hijoId}" en vez del id del hijo. */
    @Test
    fun hijoSinModeloUsaElIdYNoElLiteral() {
        val idInexistente = modelo.personajes.maxOf { it.charId } + 1

        assertEquals("#$idInexistente", nombreHijoGuardado(modelo, idInexistente, japones = false))
        assertEquals("#$idInexistente", nombreHijoGuardado(modelo, idInexistente, japones = true))
        assertNotEquals(
            "#${'$'}{a.hijoId}",
            nombreHijoGuardado(modelo, idInexistente, japones = false),
        )
    }
}
