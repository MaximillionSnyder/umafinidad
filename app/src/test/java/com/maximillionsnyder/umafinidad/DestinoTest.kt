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
        assertEquals(1, Destino.pagina(Destino.TAB_CORREDORA))
        assertEquals(2, Destino.pagina(Destino.TAB_CARTAS))
        assertEquals(3, Destino.pagina(Destino.TAB_AJUSTES))
    }

    @Test
    fun otrosDestinosNoTienenPagina() {
        assertNull(Destino.pagina("grupos"))
        assertNull(Destino.pagina("ranking"))
        /* Mejores linajes ya no es tab: se abre como overlay desde Más. */
        assertNull(Destino.pagina(Destino.TOP))
        /* Mis Umas ya no es tab: se abre como overlay desde Más. */
        assertNull(Destino.pagina(Destino.ELENCO))
        assertNull(Destino.pagina("ranking-padres"))
        assertNull(Destino.pagina("tab:x"))
    }
}
