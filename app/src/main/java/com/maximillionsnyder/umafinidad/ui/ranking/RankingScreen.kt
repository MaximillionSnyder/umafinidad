package com.maximillionsnyder.umafinidad.ui.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import com.maximillionsnyder.umafinidad.ui.componentes.RankPill
import com.maximillionsnyder.umafinidad.ui.theme.MedalBronce
import com.maximillionsnyder.umafinidad.ui.theme.MedalOro
import com.maximillionsnyder.umafinidad.ui.theme.MedalPlata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* Ranking “Umas más versátiles”: lista pura informativa ordenada por total afinidad.
   Sin búsqueda, sin click, sin navegación cruzada — solo el listado. */
@Composable
fun RankingScreen(modelo: AffinityModel, japones: Boolean) {
    var ranking by remember { mutableStateOf<List<AffinityModel.RankingAfinidad>?>(null) }

    LaunchedEffect(modelo) {
        ranking = withContext(Dispatchers.Default) { modelo.rankingAfinidad() }
    }

    val lista = ranking
    if (lista == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator()
                Text(stringResource(R.string.calculando), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    if (lista.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.sin_datos), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(lista) { i, entry ->
            CardFilaRanking(i, entry, modelo, japones)
        }
    }
}

@Composable
private fun CardFilaRanking(
    pos: Int,
    entry: AffinityModel.RankingAfinidad,
    modelo: AffinityModel,
    japones: Boolean,
) {
    val nombre = entry.personaje.displayName(japones)
    val rango = modelo.rangoRanking(entry.total)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pos < 3) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = if (pos < 3) androidx.compose.foundation.BorderStroke(1.dp, colorDeMedalla(pos)!!.copy(alpha = 0.6f)) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Medalla(pos)
            Avatar(id = entry.personaje.charId, nombre = nombre, modifier = Modifier.size(36.dp))
            Text(
                nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary,
            )
            RankPill(rango, entry.total)
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
