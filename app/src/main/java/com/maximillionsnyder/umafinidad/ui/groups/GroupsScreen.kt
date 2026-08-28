package com.maximillionsnyder.umafinidad.ui.groups

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.ui.theme.colorDeRango

/* Porte de montarGrupos(): chips de filtro por puntos + lista expandible.
   Mismas opciones que la web: [0, 2, 5, 7, 8] pt.
   Referencia archivada: se abre a pantalla completa desde Ajustes. */
private val OPCIONES = listOf(0, 2, 5, 7, 8)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupsScreen(modelo: AffinityModel, japones: Boolean, onVolver: () -> Unit) {
    var min by rememberSaveable { mutableIntStateOf(0) }
    var grupoAbierto by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        /* Cabecera con botón volver (ya no es una pestaña de la barra). */
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onVolver) {
                Icon(painterResource(R.drawable.ic_atras), contentDescription = stringResource(R.string.volver))
            }
            Text(
                stringResource(R.string.tab_groups),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(OPCIONES) { _, valor ->
                FilterChip(
                    selected = valor == min,
                    onClick = { min = valor },
                    label = {
                        Text(if (valor == 0) stringResource(R.string.filtro_todos) else stringResource(R.string.filtro_pt, valor))
                    },
                )
            }
        }

        val grupos = modelo.todosLosGrupos().filter { it.puntos >= min }

        if (grupos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.sin_grupos_filtro), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(grupos, key = { _, g -> g.tipo }) { _, grupo ->
                val miembros = modelo.miembrosDeGrupo(grupo.tipo)
                val abierto = grupoAbierto == grupo.tipo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable { grupoAbierto = if (abierto) null else grupo.tipo },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    "#${grupo.tipo}",
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Text(
                                stringResource(R.string.miembros_cantidad, miembros.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                "${grupo.puntos}pt",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = colorDeRango(claseDePuntos(grupo.puntos)) ?: MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        AnimatedVisibility(visible = abierto) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                miembros.forEach { m ->
                                    Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                                        Text(
                                            m.displayName(japones),
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* Mismos umbrales de rango que el modelo (par): ◎ ≥20, ○ ≥10, △ ≥4. */
private fun claseDePuntos(puntos: Int): String? = when {
    puntos >= 20 -> "rank-great"
    puntos >= 10 -> "rank-good"
    puntos >= 4 -> "rank-fair"
    else -> null
}
