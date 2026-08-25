package com.maximillionsnyder.umafinidad.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.domain.Rango
import com.maximillionsnyder.umafinidad.ui.theme.LocalColoresRango
import com.maximillionsnyder.umafinidad.ui.theme.fondoDeRango
import kotlin.math.floor

/* Mismo color base que la PWA: hsl((id × 137.508) % 360 55% 45%).
   Para los avatares se deriva un gradiente radial (claro → oscuro). */
fun colorDeAvatar(id: Int): Color =
    Color.hsl((id * 137.508f) % 360f, 0.55f, 0.45f)

fun gradienteDeAvatar(id: Int): Brush {
    val hue = (id * 137.508f) % 360f
    return Brush.radialGradient(
        listOf(
            Color.hsl(hue, 0.6f, 0.58f),
            Color.hsl(hue, 0.55f, 0.32f),
        ),
    )
}

fun inicialesDe(nombre: String): String =
    nombre.split(Regex("\\s+")).mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()

@Composable
fun Avatar(id: Int, nombre: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(gradienteDeAvatar(id), CircleShape),
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

/* Pastilla "◎ 257" con fondo tintado al 12% del color del rango
   (idéntico al estilo .puntos de la web). */
@Composable
fun RankPill(rango: Rango?, puntos: Int, modifier: Modifier = Modifier, grande: Boolean = false) {
    val colores = LocalColoresRango.current
    val frente = when (rango?.clase) {
        "rank-great" -> colores.great
        "rank-good" -> colores.good
        "rank-fair" -> colores.fair
        else -> MaterialTheme.colorScheme.onSurface
    }
    val fondo = fondoDeRango(rango?.clase) ?: MaterialTheme.colorScheme.surfaceVariant

    Text(
        text = (rango?.let { it.simbolo + " " } ?: "") + puntos,
        modifier = modifier.background(fondo, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = if (grande) 8.dp else 4.dp),
        style = if (grande) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = frente,
    )
}

/* Versión simple sin pastilla (texto coloreado). */
@Composable
fun PuntosRango(rango: Rango?, puntos: Int, modifier: Modifier = Modifier) {
    val colores = LocalColoresRango.current
    val color = when (rango?.clase) {
        "rank-great" -> colores.great
        "rank-good" -> colores.good
        "rank-fair" -> colores.fair
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = (rango?.let { it.simbolo + " " } ?: "") + puntos,
        modifier = modifier,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}
