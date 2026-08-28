package com.maximillionsnyder.umafinidad.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Linaje
import com.maximillionsnyder.umafinidad.ui.theme.MedalBronce
import com.maximillionsnyder.umafinidad.ui.theme.MedalOro
import com.maximillionsnyder.umafinidad.ui.theme.MedalPlata

/* Fila del top de linajes, compartida por el Top global ("Mejores
   linajes") y "Mis corredoras". Los primeros 3 llevan medalla; tocar la
   card dispara onClick (ver herencia). */
@Composable
fun CardFilaTop(i: Int, combo: Linaje, modelo: AffinityModel, japones: Boolean, onClick: () -> Unit) {
    val nombres = listOf(combo.hijo, combo.padre, combo.madre)
        .joinToString(" × ") { it.displayName(japones) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (i < 3) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = if (i < 3) BorderStroke(1.dp, colorDeMedalla(i)!!.copy(alpha = 0.6f)) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Medalla(i)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(nombres, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    stringResource(R.string.ver_herencia),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            RankPill(modelo.rangoTotal(combo.puntos), combo.puntos)
        }
    }
}

@Composable
private fun Medalla(pos: Int) {
    val color = colorDeMedalla(pos)
    if (color == null) {
        Text(
            "${pos + 1}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(30.dp).wrapContentSize(),
        )
    } else {
        Box(
            modifier = Modifier.size(30.dp).background(color, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${pos + 1}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF14161A),
            )
        }
    }
}

private fun colorDeMedalla(pos: Int): Color? = when (pos) {
    0 -> MedalOro
    1 -> MedalPlata
    2 -> MedalBronce
    else -> null
}
