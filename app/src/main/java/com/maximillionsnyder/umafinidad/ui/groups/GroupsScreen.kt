package com.maximillionsnyder.umafinidad.ui.groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel

/* Porte de montarGrupos(): chips de filtro por puntos + lista expandible.
   Mismas opciones que la web: [0, 2, 5, 7, 8] pt. */
private val OPCIONES = listOf(0, 2, 5, 7, 8)

@Composable
fun GroupsScreen(modelo: AffinityModel, japones: Boolean) {
    var min by rememberSaveable { mutableIntStateOf(0) }
    var grupoAbierto by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
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
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(grupos, key = { _, g -> g.tipo }) { _, grupo ->
                val miembros = modelo.miembrosDeGrupo(grupo.tipo)
                val abierto = grupoAbierto == grupo.tipo
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { grupoAbierto = if (abierto) null else grupo.tipo },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("#${grupo.tipo}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${grupo.puntos}pt", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                stringResource(R.string.miembros_cantidad, miembros.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        AnimatedVisibility(visible = abierto) {
                            Text(
                                miembros.joinToString(" · ") { it.displayName(japones) },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
