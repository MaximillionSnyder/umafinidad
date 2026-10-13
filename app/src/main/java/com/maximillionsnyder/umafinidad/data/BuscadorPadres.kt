package com.maximillionsnyder.umafinidad.data

import com.maximillionsnyder.umafinidad.domain.Criterios
import com.maximillionsnyder.umafinidad.domain.EstadoPadres
import com.maximillionsnyder.umafinidad.domain.Padre
import com.maximillionsnyder.umafinidad.domain.SalidaPadres
import com.maximillionsnyder.umafinidad.domain.TotalServidor
import com.maximillionsnyder.umafinidad.domain.parametrosDeConsulta
import com.maximillionsnyder.umafinidad.domain.resolverBusqueda

/* Una búsqueda completa: prefiltro del servidor, paginado y decisión final.
 *
 * El punto delicado es `completo`. La pantalla solo puede decir "no hay" si se
 * recorrió TODO el universo de resultados; si se cortó por el tope, la
 * respuesta honesta es "no se encontró en lo revisado". Por eso el tope de
 * páginas no se trata como un detalle de rendimiento sino como parte del
 * resultado. */
class BuscadorPadres(private val api: UmamoeApi) {

    suspend fun buscar(criterios: Criterios): SalidaPadres {
        if (criterios.vacio) return SalidaPadres(EstadoPadres.SIN_CRITERIOS)

        /* Pedir más estrellas de las que admite la rama es imposible, y eso se
         * sabe sin consultar: la respuesta es definitiva y no gasta red. */
        if (!criterios.esPosible()) return SalidaPadres(EstadoPadres.NO_HAY)

        val parametros = parametrosDeConsulta(criterios)
        val escaneados = mutableListOf<Padre>()
        var total = TotalServidor(0, false)
        var finAlcanzado = false

        for (pagina in 0 until MAX_PAGINAS) {
            when (val respuesta = api.buscar(parametros, pagina, UmamoeApi.LIMITE_MAX)) {
                is RespuestaUmamoe.Fallo ->
                    return SalidaPadres(EstadoPadres.ERROR, error = respuesta.error)

                is RespuestaUmamoe.Ok -> {
                    if (pagina == 0) total = respuesta.total
                    escaneados += respuesta.items

                    /* Una página corta o vacía es la última. También lo es la
                     * que el servidor marca como final: solo en esos dos casos
                     * sabemos que no queda nada por revisar. */
                    val corta = respuesta.items.size < UmamoeApi.LIMITE_MAX
                    if (corta || pagina + 1 >= respuesta.paginas) {
                        finAlcanzado = true
                        break
                    }
                }
            }
        }

        return resolverBusqueda(criterios, escaneados, total, finAlcanzado)
    }

    private companion object {
        /* Tope por búsqueda: 10 páginas de 100. Por encima de esto la pantalla
         * no puede afirmar que algo no exista, y lo dice. */
        const val MAX_PAGINAS = 10
    }
}
