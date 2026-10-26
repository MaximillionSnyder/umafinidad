package com.maximillionsnyder.umafinidad.ui

/* Destinos de navegación compartidos entre la app y la burbuja flotante.
   "tab:N" para las pestañas; otros literales ("grupos", "ranking", "top",
   "elenco", "ranking-padres", "pro", "buscar-padres") para las pantallas que
   se abren a pantalla completa desde Más. */
object Destino {

    const val EXTRA = "destino"
    const val TAB_COMPAT = "tab:0"
    const val TAB_CORREDORA = "tab:1"
    const val TAB_AJUSTES = "tab:2"

    /* Mejores linajes: archivada de la barra inferior, se abre desde Más. */
    const val TOP = "top"

    /* Mis Umas: archivada de la barra inferior, se abre desde Más. */
    const val ELENCO = "elenco"

    /* Pantalla Pro (activación + lista de funciones): se abre desde Ajustes,
       desde los candados de la app y desde el panel de la burbuja. */
    const val PRO = "pro"

    /* Buscador de padres en uma.moe: se abre desde Ajustes. */
    const val BUSCAR_PADRES = "buscar-padres"

    /* Página del pager para un destino "tab:N"; null para los demás. */
    fun pagina(destino: String): Int? = destino.removePrefix("tab:").toIntOrNull()
}
