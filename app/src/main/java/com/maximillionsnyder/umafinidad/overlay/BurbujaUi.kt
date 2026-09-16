package com.maximillionsnyder.umafinidad.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximillionsnyder.umafinidad.ui.theme.ContenedorPrimario
import com.maximillionsnyder.umafinidad.ui.theme.Primario

/* Burbuja circular flotante: tap = abrir/cerrar el panel; arrastrar = mover.
   El servicio mueve la ventana con los deltas que llegan por onMover. */
@Composable
fun BurbujaContenido(
    descripcion: String,
    onTap: () -> Unit,
    onMover: (Float, Float) -> Unit,
    onSoltar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Primario, ContenedorPrimario)))
            .pointerInput(Unit) {
                detectDragGestures(onDragEnd = { onSoltar() }) { cambio, delta ->
                    cambio.consume()
                    onMover(delta.x, delta.y)
                }
            }
            .pointerInput(Unit) { detectTapGestures { onTap() } }
            .semantics {
                contentDescription = descripcion
                role = Role.Button
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
