package com.maximillionsnyder.umafinidad.overlay

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.ui.theme.ContenedorPrimario
import com.maximillionsnyder.umafinidad.ui.theme.Primario

/* Geometría compartida con BurbujaService: la ventana del overlay usa
   estos mismos valores para arrastre y "imán" a los bordes. */
const val TAMANO_BURBUJA_DP = 56
const val MARGEN_BURBUJA_DP = 8

/* Zona de descarte (la X de abajo): tamaño, altura sobre el borde inferior
   y margen de agarre para soltar la burbuja encima. */
const val TAMANO_QUITAR_DP = 64
const val MARGEN_QUITAR_DP = 64
const val AGARRE_QUITAR_DP = 14

/* Franja del panel: ancho proporcional a la pantalla (con topes para
   teléfonos chicos y tablets) y alto como fracción de la pantalla.
   El tamaño de la franja no se toca: lo que se reparte distinto adentro son
   las caras del buscador (ver RepartoPanel.kt). */
const val FRACCION_ANCHO_PANEL = 0.50f
const val ANCHO_PANEL_MIN_DP = 190
const val ANCHO_PANEL_MAX_DP = 300
const val FRACCION_ALTO_PANEL = 0.66f

/* Ficha de personaje del carrusel del buscador. */
const val FICHA_OPCION_DP = 44
const val ESPACIO_FICHAS_DP = 6
const val MAX_FICHAS_VISIBLES = 6

/* Burbuja circular flotante: tap = abrir/cerrar el panel; arrastrar = mover.
   El servicio mueve la ventana con los deltas que llegan por onMover.

   Un pointerInput para el tap (con su reacción visual al presionar) y otro
   para el arrastre: cada uno tiene su propio ciclo de vida, así que la
   animación de presión no reinicia el reconocedor de arrastre ni al revés.
   Ni el tamaño de la ventana ni la posición final del imán cambian. */
@Composable
fun BurbujaContenido(
    descripcion: String,
    onTap: () -> Unit,
    onIniciarArrastre: () -> Unit,
    onMover: (Float, Float) -> Unit,
    onSoltar: () -> Unit,
) {
    var presionada by remember { mutableStateOf(false) }
    val escala by animateFloatAsState(
        targetValue = if (presionada) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "escalaBurbuja",
    )

    Box(
        modifier = Modifier
            .size(TAMANO_BURBUJA_DP.dp)
            .scale(escala)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Primario, ContenedorPrimario)))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        presionada = true
                        tryAwaitRelease()
                        presionada = false
                    },
                    onTap = { onTap() },
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        presionada = false
                        onIniciarArrastre()
                    },
                    onDragEnd = { onSoltar() },
                    onDragCancel = {
                        presionada = false
                        onSoltar()
                    },
                ) { cambio, delta ->
                    cambio.consume()
                    onMover(delta.x, delta.y)
                }
            }
            .semantics {
                contentDescription = descripcion
                role = Role.Button
                /* El tap por gestos no se expone solo a TalkBack: la acción
                   declarada sí permite activar la burbuja por accesibilidad. */
                onClick(label = descripcion) {
                    onTap()
                    true
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "◎",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

/* Zona de descarte que aparece al arrastrar la burbuja: soltarla encima
   la quita de pantalla. Se resalta cuando la burbuja está por encima. */
@Composable
fun ObjetivoQuitar(descripcion: String, activo: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(if (activo) Color(0xFFB3261E) else Color(0xCC2B2B2B))
            .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(R.drawable.ic_cerrar),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}
