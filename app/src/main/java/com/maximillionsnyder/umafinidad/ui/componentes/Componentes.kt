package com.maximillionsnyder.umafinidad.ui.componentes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.maximillionsnyder.umafinidad.ui.theme.LocalIsDark
import com.maximillionsnyder.umafinidad.ui.theme.brushPrimario
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
    val avatar = avatarResFor(id, LocalEstiloAvatar.current)
    if (avatar != null) {
        Image(
            painter = painterResource(avatar),
            contentDescription = null,
            modifier = modifier.clip(CircleShape),
        )
    } else {
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
    val forma = RoundedCornerShape(999.dp)

    Text(
        text = (rango?.let { it.simbolo + " " } ?: "") + puntos,
        modifier = modifier
            .background(fondo, forma)
            .border(1.dp, frente.copy(alpha = 0.28f), forma)
            .padding(horizontal = 10.dp, vertical = if (grande) 8.dp else 4.dp),
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

/* ---- Cabeceras ----
   Cabecera uniforme: altura fija 56dp + statusBars inset (todas las
   pantallas y overlays comparten la misma altura para no romper el top).
   Sobre el fondo puro lleva tres señales de identidad: un lavado del
   primario que da profundidad, el icono de la sección en un badge con
   degradado y un hilo de acento que se desvanece hacia la derecha. */

/* Lavado vertical del primario: más intenso arriba (bajo la barra de
   estado) y casi transparente al llegar al contenido. */
@Composable
private fun brushLavadoCabecera(): Brush {
    val primario = MaterialTheme.colorScheme.primary
    val arriba = if (LocalIsDark.current) 0.18f else 0.10f
    return Brush.verticalGradient(listOf(primario.copy(alpha = arriba), primario.copy(alpha = 0.01f)))
}

/* Hilo inferior de 1dp: acento que se apaga hacia la derecha. */
@Composable
private fun brushHiloCabecera(): Brush {
    val primario = MaterialTheme.colorScheme.primary
    val inicio = if (LocalIsDark.current) 0.55f else 0.40f
    return Brush.horizontalGradient(listOf(primario.copy(alpha = inicio), primario.copy(alpha = 0f)))
}

@Composable
private fun CabeceraBase(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brushLavadoCabecera()),
    ) {
        contenido()
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(brushHiloCabecera()),
        )
    }
}

@Composable
private fun TituloCabecera(titulo: String, modifier: Modifier = Modifier) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.headingSemantica(),
    )
}

/* Icono de la sección en un badge cuadrado con el degradado primario.
   `modifierIcono` viaja al icono (elemento compartido con Ajustes). */
@Composable
private fun BadgeIcono(res: Int, modifier: Modifier = Modifier, modifierIcono: Modifier = Modifier) {
    val forma = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(forma)
            .background(brushPrimario()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(res),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(18.dp).then(modifierIcono),
        )
    }
}

/* Pastilla de la cabecera (contador de herencia, badge Pro). La comparte
   el elenco, que la anima como elemento compartido con la lista. */
@Composable
fun PillCabecera(texto: String, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .clip(forma)
            .background(brushPrimario())
            .border(1.dp, Color.White.copy(alpha = 0.20f), forma),
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

@Composable
fun HeaderBar(
    titulo: String,
    pillTexto: String? = null,
    modifier: Modifier = Modifier,
    chip: (@Composable () -> Unit)? = null,
    modifierPill: Modifier = Modifier,
    iconoRes: Int? = null,
) {
    CabeceraBase(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                iconoRes?.let { BadgeIcono(it) }
                TituloCabecera(titulo)
            }
            chip?.let { contenido ->
                Box(modifier = Modifier.padding(start = 10.dp)) { contenido() }
            }
            pillTexto?.let { texto ->
                PillCabecera(texto, Modifier.padding(start = 12.dp).then(modifierPill))
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
    iconoRes: Int? = null,
    chip: (@Composable () -> Unit)? = null,
    modifierTitulo: Modifier = Modifier,
    modifierIcono: Modifier = Modifier,
    modifierPill: Modifier = Modifier,
) {
    CabeceraBase(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onVolver) {
                Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.volver))
            }
            iconoRes?.let { id ->
                BadgeIcono(
                    res = id,
                    modifier = Modifier.padding(end = 10.dp),
                    modifierIcono = modifierIcono,
                )
            }
            TituloCabecera(titulo, Modifier.weight(1f).then(modifierTitulo))
            chip?.let { contenido ->
                Box(modifier = Modifier.padding(start = 10.dp)) { contenido() }
            }
            pillTexto?.let { texto ->
                PillCabecera(texto, Modifier.padding(start = 12.dp).then(modifierPill))
            }
        }
    }
}
