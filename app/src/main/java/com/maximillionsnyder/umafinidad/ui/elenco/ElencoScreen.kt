package com.maximillionsnyder.umafinidad.ui.elenco

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Linaje
import com.maximillionsnyder.umafinidad.domain.coincideDifuso
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import com.maximillionsnyder.umafinidad.ui.componentes.CardFilaTop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* "Mis corredoras": elenco propio del usuario. Solapa 1: marcar qué
   personajes posee (grilla + búsqueda difusa). Solapa 2: los mejores
   linajes calculados SOLO con ese elenco (mismo algoritmo que el Top). */
@Composable
fun ElencoScreen(
    modelo: AffinityModel,
    japones: Boolean,
    elenco: Set<Int>,
    onToggle: (Int) -> Unit,
    onMarcar: (List<Int>) -> Unit,
    onLimpiar: () -> Unit,
    onVerHerencia: (Linaje) -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val totalJugables = remember(modelo) {
        modelo.personajes.count { it.playable == true && it.active == true }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        /* Cabecera: título + contador del elenco. */
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.tab_elenco),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
            Surface(shape = RoundedCornerShape(999.dp), color = MaterialTheme.colorScheme.primary) {
                Text(
                    stringResource(R.string.elenco_contador, elenco.size, totalJugables),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        TabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text(stringResource(R.string.elenco_tab_editar)) },
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text(stringResource(R.string.elenco_tab_linajes)) },
            )
        }

        when (tab) {
            0 -> EditorElenco(modelo, japones, elenco, onToggle, onMarcar, onLimpiar)
            else -> LinajesElenco(modelo, japones, elenco, onVerHerencia)
        }
    }
}

/* ---------- Solapa "Editar elenco" ---------- */

@Composable
private fun EditorElenco(
    modelo: AffinityModel,
    japones: Boolean,
    elenco: Set<Int>,
    onToggle: (Int) -> Unit,
    onMarcar: (List<Int>) -> Unit,
    onLimpiar: () -> Unit,
) {
    var filtro by rememberSaveable { mutableStateOf("") }
    var confirmarLimpiar by rememberSaveable { mutableStateOf(false) }

    val jugables = remember(modelo) { modelo.personajes.filter { it.playable == true && it.active == true } }
    val filtrados = remember(filtro, jugables) { jugables.filter { coincideDifuso(it, filtro) } }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = filtro,
            onValueChange = { filtro = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null) },
            trailingIcon = {
                if (filtro.isNotEmpty()) {
                    IconButton(onClick = { filtro = "" }) {
                        Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.limpiar_todo))
                    }
                }
            },
            placeholder = { Text(stringResource(R.string.buscar)) },
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = { onMarcar(filtrados.map { it.charId }) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.elenco_marcar_visibles))
            }
            TextButton(
                onClick = { if (elenco.isNotEmpty()) confirmarLimpiar = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.limpiar_todo), color = MaterialTheme.colorScheme.error)
            }
        }

        if (filtrados.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.sin_resultados), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 104.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtrados, key = { it.charId }) { c ->
                    CardElenco(c, marcado = elenco.contains(c.charId), japones = japones) { onToggle(c.charId) }
                }
            }
        }
    }

    if (confirmarLimpiar) {
        AlertDialog(
            onDismissRequest = { confirmarLimpiar = false },
            title = { Text(stringResource(R.string.elenco_limpiar_titulo)) },
            text = { Text(stringResource(R.string.elenco_limpiar_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    onLimpiar()
                    confirmarLimpiar = false
                }) { Text(stringResource(R.string.limpiar_todo)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpiar = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
        )
    }
}

@Composable
private fun CardElenco(personaje: Character, marcado: Boolean, japones: Boolean, onClick: () -> Unit) {
    val nombrePrincipal = personaje.displayName(japones)

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (marcado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = if (marcado) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box {
                Avatar(personaje.charId, nombrePrincipal, modifier = Modifier.size(56.dp))
                AnimatedVisibility(visible = marcado, enter = scaleIn(), exit = scaleOut()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "✓",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Text(
                nombrePrincipal,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/* ---------- Solapa "Mis linajes" ---------- */

@Composable
private fun LinajesElenco(
    modelo: AffinityModel,
    japones: Boolean,
    elenco: Set<Int>,
    onVerHerencia: (Linaje) -> Unit,
) {
    var linajes by remember { mutableStateOf<List<Linaje>?>(null) }

    LaunchedEffect(elenco, modelo) {
        linajes = null
        linajes = withContext(Dispatchers.Default) { modelo.topLinajesDeElenco(elenco, 20) }
    }

    val lista = linajes
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
            Text(
                stringResource(R.string.elenco_minimo),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(lista) { i, combo ->
            CardFilaTop(i, combo, modelo, japones) { onVerHerencia(combo) }
        }
    }
}
