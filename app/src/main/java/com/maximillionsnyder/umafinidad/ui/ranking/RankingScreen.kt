package com.maximillionsnyder.umafinidad.ui.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import com.maximillionsnyder.umafinidad.ui.componentes.ClavesTransicion
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBarConVolver
import com.maximillionsnyder.umafinidad.ui.componentes.RankPill
import com.maximillionsnyder.umafinidad.ui.componentes.compartidoBounds
import com.maximillionsnyder.umafinidad.ui.componentes.compartidoElemento
import com.maximillionsnyder.umafinidad.ui.theme.MedalBronce
import com.maximillionsnyder.umafinidad.ui.theme.MedalOro
import com.maximillionsnyder.umafinidad.ui.theme.MedalPlata
import com.maximillionsnyder.umafinidad.ui.theme.cardFondo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* Ranking con dos modos para probar: versátiles (total afinidad) y
   mejores padres (veces como padre óptimo + % + media). Se abre desde
   Ajustes por la card clásica o por la card nueva de padres.
   Mejores padres es función Pro: sin licencia el modo queda a la vista pero
   bloqueado, con el botón que lleva a la pantalla Pro. */
enum class ModoRanking { VERSATIL, PADRES }

@Composable
fun RankingScreen(
    modelo: AffinityModel,
    japones: Boolean,
    onVolver: () -> Unit,
    modoInicial: ModoRanking = ModoRanking.VERSATIL,
    claveOverlay: String = ClavesTransicion.OVERLAY_RANKING,
    esPro: Boolean = true,
    onIrAPro: () -> Unit = {},
) {
    var modo by rememberSaveable { mutableStateOf(modoInicial) }
    var mostrarAyuda by rememberSaveable { mutableStateOf(false) }
    var ranking by remember { mutableStateOf<List<AffinityModel.RankingAfinidad>?>(null) }
    var rankingPadres by remember { mutableStateOf<List<AffinityModel.RankingPadre>?>(null) }

    /* El ranking de padres es el cálculo más caro de la app: sin licencia ni
       se calcula (el modo muestra el candado). */
    LaunchedEffect(modelo, esPro) {
        ranking = withContext(Dispatchers.Default) { modelo.rankingAfinidad() }
        if (esPro) {
            rankingPadres = withContext(Dispatchers.Default) { modelo.rankingPadres() }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize().compartidoBounds(claveOverlay),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            HeaderBarConVolver(
                titulo = stringResource(R.string.tab_ranking),
                onVolver = onVolver,
                iconoRes = R.drawable.ic_tab_ranking,
                modifierTitulo = Modifier.compartidoBounds(ClavesTransicion.titulo(claveOverlay)),
                modifierIcono = Modifier.compartidoElemento(ClavesTransicion.icono(claveOverlay)),
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                SegmentedButton(
                    selected = modo == ModoRanking.VERSATIL,
                    onClick = { modo = ModoRanking.VERSATIL },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) {
                    Text(stringResource(R.string.ranking_modo_versatil))
                }
                SegmentedButton(
                    selected = modo == ModoRanking.PADRES,
                    onClick = { modo = ModoRanking.PADRES },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {
                        if (esPro) {
                            SegmentedButtonDefaults.Icon(active = modo == ModoRanking.PADRES)
                        } else {
                            Icon(
                                painterResource(R.drawable.ic_candado),
                                contentDescription = stringResource(R.string.pro_bloqueada),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    },
                ) {
                    Text(stringResource(R.string.ranking_modo_padres))
                }
            }
    
            if (modo == ModoRanking.VERSATIL) {
                ContenidoVersatiles(ranking, modelo, japones)
            } else if (!esPro) {
                ContenidoBloqueado(onIrAPro)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.ranking_padres_ayuda_titulo),
                            onClick = { mostrarAyuda = true },
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(R.string.ranking_padres_ayuda_corta),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        painterResource(R.drawable.ic_info),
                        contentDescription = stringResource(R.string.ranking_padres_ayuda_titulo),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                ContenidoPadres(rankingPadres, japones)
            }
        }
    }

    if (mostrarAyuda) {
        AlertDialog(
            onDismissRequest = { mostrarAyuda = false },
            title = { Text(stringResource(R.string.ranking_padres_ayuda_titulo)) },
            text = { Text(stringResource(R.string.ranking_padres_ayuda_larga)) },
            confirmButton = {
                TextButton(onClick = { mostrarAyuda = false }) {
                    Text(stringResource(R.string.entendido))
                }
            },
        )
    }
}

@Composable
private fun ContenidoBloqueado(onIrAPro: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_candado),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp),
            )
            Text(
                stringResource(R.string.pro_func_padres),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.pro_func_padres_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onIrAPro) {
                Text(stringResource(R.string.pro_padres_cta), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ContenidoVersatiles(
    ranking: List<AffinityModel.RankingAfinidad>?,
    modelo: AffinityModel,
    japones: Boolean,
) {
    if (ranking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator()
                Text(stringResource(R.string.calculando), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    if (ranking.isEmpty()) {
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
        itemsIndexed(ranking) { i, entry ->
            CardFilaRanking(i, entry, modelo, japones)
        }
    }
}

@Composable
private fun ContenidoPadres(
    ranking: List<AffinityModel.RankingPadre>?,
    japones: Boolean,
) {
    if (ranking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator()
                Text(stringResource(R.string.calculando), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    if (ranking.isEmpty()) {
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
        itemsIndexed(ranking) { i, entry ->
            CardFilaPadre(i, entry, japones)
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
            else cardFondo(),
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            RankPill(rango, entry.total)
        }
    }
}

@Composable
private fun CardFilaPadre(
    pos: Int,
    entry: AffinityModel.RankingPadre,
    japones: Boolean,
) {
    val nombre = entry.personaje.displayName(japones)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pos < 3) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else cardFondo(),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    stringResource(R.string.ranking_padres_media, entry.puntosMedios),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
                Text(
                    stringResource(R.string.ranking_padres_veces, entry.veces, entry.porcentaje.toInt()),
                    style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
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
