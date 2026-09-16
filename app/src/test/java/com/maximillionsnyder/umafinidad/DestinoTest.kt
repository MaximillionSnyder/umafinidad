package com.maximillionsnyder.umafinidad

import com.maximillionsnyder.umafinidad.ui.Destino
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/* El protocolo de destinos compartido entre la app y la burbuja flotante. */
class DestinoTest {

    @Test
    fun extraeLaPaginaDelTab() {
        assertEquals(0, Destino.pagina(Destino.TAB_COMPAT))
        assertEquals(2, Destino.pagina(Destino.TAB_CORREDORA))
        assertEquals(3, Destino.pagina(Destino.TAB_ELENCO))
        assertEquals(4, Destino.pagina(Destino.TAB_AJUSTES))
    }

    @Test
    fun otrosDestinosNoTienenPagina() {
        assertNull(Destino.pagina("grupos"))
        assertNull(Destino.pagina("ranking"))
        assertNull(Destino.pagina("ranking-padres"))
        assertNull(Destino.pagina("tab:x"))
    }
}
