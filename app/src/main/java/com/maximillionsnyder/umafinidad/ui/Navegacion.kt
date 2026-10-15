package com.maximillionsnyder.umafinidad.ui

/* Destinos de navegación compartidos entre la app y la burbuja flotante.
   Formato de la pila: "tab:N" para las pestañas, u otros literales
   ("grupos", "ranking", "ranking-padres") para los overlays. */
object Destino {

    const val EXTRA = "destino"
    const val TAB_COMPAT = "tab:0"
    const val TAB_CORREDORA = "tab:1"
    const val TAB_ELENCO = "tab:2"
    const val TAB_AJUSTES = "tab:3"

    /* Mejores linajes: archivada de la barra inferior, se abre desde Más. */
    const val TOP = "top"

    /* Pantalla Pro (activación + lista de funciones): se abre desde Ajustes,
       desde los candados de la app y desde el panel de la burbuja. */
    const val PRO = "pro"

    /* Buscador de padres en uma.moe: se abre desde Ajustes. */
    const val BUSCAR_PADRES = "buscar-padres"

    /* Página del pager para un destino "tab:N"; null para los demás. */
    fun pagina(destino: String): Int? = destino.removePrefix("tab:").toIntOrNull()
}
