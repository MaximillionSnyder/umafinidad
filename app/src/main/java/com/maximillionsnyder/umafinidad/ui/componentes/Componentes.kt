package com.maximillionsnyder.umafinidad.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.domain.Rango
import com.maximillionsnyder.umafinidad.ui.theme.LocalColoresRango
import kotlin.math.floor

/* Mismo color que la PWA: hsl((id × 137.508) % 360 55% 45%). */
fun colorDeAvatar(id: Int): Color =
    Color.hsl((id * 137.508f) % 360f, 0.55f, 0.45f)

fun inicialesDe(nombre: String): String =
    nombre.split(Regex("\\s+")).mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()

@Composable
fun Avatar(id: Int, nombre: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(colorDeAvatar(id), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = inicialesDe(nombre),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

/* "◎ 257" con el color del rango (o el número solo si no hay rango). */
@Composable
fun PuntosRango(rango: Rango?, puntos: Int, modifier: Modifier = Modifier) {
    val colores = LocalColoresRango.current
    val color = colores.porClase(rango?.clase)
    Text(
        text = (rango?.let { it.simbolo + " " } ?: "") + puntos,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = color ?: MaterialTheme.colorScheme.onSurface,
    )
}

private fun com.maximillionsnyder.umafinidad.ui.theme.ColoresRango.porClase(clase: String?): Color? = when (clase) {
    "rank-great" -> great
    "rank-good" -> good
    "rank-fair" -> fair
    else -> null
}
