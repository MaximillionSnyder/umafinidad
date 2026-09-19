package com.maximillionsnyder.umafinidad.ui.componentes

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/* Claves estables de los elementos compartidos entre pantallas.
   Fuente y destino deben usar exactamente la misma clave. */
object ClavesTransicion {

    const val OVERLAY_GRUPOS = "overlay-grupos"
    const val OVERLAY_RANKING = "overlay-ranking"
    const val OVERLAY_RANKING_PADRES = "overlay-ranking-padres"

    fun titulo(claveOverlay: String): String = "$claveOverlay-titulo"

    fun icono(claveOverlay: String): String = "$claveOverlay-icono"

    const val ELENCO_PANEL = "elenco-panel"
    const val ELENCO_CONTADOR = "elenco-contador"
}

/* Scopes de la transición compartida. Nulos fuera del host (previews, tests
   de componentes): los helpers de abajo se vuelven no-op en ese caso. */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/* sharedBounds: el contenedor morph entre origen y destino. */
@Composable
fun Modifier.compartidoBounds(clave: String): Modifier {
    val compartido = LocalSharedTransitionScope.current ?: return this
    val visibilidad = LocalAnimatedVisibilityScope.current ?: return this
    return with(compartido) {
        sharedBounds(
            rememberSharedContentState(key = clave),
            visibilidad,
        )
    }
}

/* sharedElement: mismo contenido visual viajando entre origen y destino. */
@Composable
fun Modifier.compartidoElemento(clave: String): Modifier {
    val compartido = LocalSharedTransitionScope.current ?: return this
    val visibilidad = LocalAnimatedVisibilityScope.current ?: return this
    return with(compartido) {
        sharedElement(
            rememberSharedContentState(key = clave),
            visibilidad,
        )
    }
}
