package com.maximillionsnyder.umafinidad.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
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

/* Cabecera uniforme: altura fija 56dp + statusBars inset.
   Todas las pantallas (tabs y overlays de Más) usan la misma altura
   para no romper el top de la app. */
@Composable
fun HeaderBar(
    titulo: String,
    pillTexto: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        pillTexto?.let { texto ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Text(
                    text = texto,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/* Variante con botón volver: misma altura/insets que HeaderBar
   para que las pantallas de Más no rompan la altura. */
@Composable
fun HeaderBarConVolver(
    titulo: String,
    onVolver: () -> Unit,
    pillTexto: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(56.dp)
            .padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onVolver) {
            Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.volver))
        }
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        pillTexto?.let { texto ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Text(
                    text = texto,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        }
    }
}
