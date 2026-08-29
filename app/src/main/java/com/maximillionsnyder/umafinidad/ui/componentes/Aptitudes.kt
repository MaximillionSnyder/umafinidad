package com.maximillionsnyder.umafinidad.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.APT_CORTA
import com.maximillionsnyder.umafinidad.domain.APT_DIRT
import com.maximillionsnyder.umafinidad.domain.APT_FUGA
import com.maximillionsnyder.umafinidad.domain.APT_LARGA
import com.maximillionsnyder.umafinidad.domain.APT_MEDIA
import com.maximillionsnyder.umafinidad.domain.APT_MILLA
import com.maximillionsnyder.umafinidad.domain.APT_REMATE
import com.maximillionsnyder.umafinidad.domain.APT_RETRASO
import com.maximillionsnyder.umafinidad.domain.APT_TURF
import com.maximillionsnyder.umafinidad.domain.APT_VANGUARDIA
import com.maximillionsnyder.umafinidad.domain.aptitudesDestacadas

/* Escala fija de color por letra (A la mejor → G la peor), igual en ambos
   temas: es información, no decoración. */
fun colorDeLetra(letra: String): Color = when (letra) {
    "A" -> Color(0xFF7BD88F)
    "B" -> Color(0xFFAED581)
    "C" -> Color(0xFFF2DC6D)
    "D" -> Color(0xFFF5B455)
    "E" -> Color(0xFFF08A5D)
    "F" -> Color(0xFFE85D5D)
    else -> Color(0xFF8A8F98)
}

@Composable
private fun etiquetaAptitud(i: Int): String = stringResource(
    when (i) {
        APT_TURF -> R.string.apt_turf
        APT_DIRT -> R.string.apt_dirt
        APT_CORTA -> R.string.apt_corta
        APT_MILLA -> R.string.apt_milla
        APT_MEDIA -> R.string.apt_media
        APT_LARGA -> R.string.apt_larga
        APT_FUGA -> R.string.apt_fuga
        APT_VANGUARDIA -> R.string.apt_vanguardia
        APT_REMATE -> R.string.apt_remate
        else -> R.string.apt_retraso
    },
)

/* Chip "Césped A". tintado: fondo con el color de la letra (variante
   compacta de las grillas); si no, fondo neutro (detalle completo). */
@Composable
private fun ChipAptitud(indice: Int, letra: String, tintado: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (tintado) colorDeLetra(letra).copy(alpha = 0.16f)
        else MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                etiquetaAptitud(indice),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                letra,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = colorDeLetra(letra),
            )
        }
    }
}

private val SECCIONES = listOf(
    R.string.apt_pista to (APT_TURF..APT_DIRT),
    R.string.apt_distancia to (APT_CORTA..APT_LARGA),
    R.string.apt_estilo to (APT_FUGA..APT_RETRASO),
)

/* Detalle completo: una fila por categoría (pista / distancia / estilo)
   con las 10 aptitudes. Para el panel de Mi corredora. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AptitudesDetalle(apt: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SECCIONES.forEach { (titulo, rango) ->
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    stringResource(titulo),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    rango.forEach { i -> ChipAptitud(i, apt[i]) }
                }
            }
        }
    }
}

/* Variante compacta: solo las destacadas (A o B), chips tintados. Para
   las cards de las grillas. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AptitudesChips(apt: List<String>) {
    val destacadas = aptitudesDestacadas(apt)
    if (destacadas.isEmpty()) return
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        destacadas.forEach { i -> ChipAptitud(i, apt[i], tintado = true) }
    }
}
