package com.maximillionsnyder.umafinidad.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import com.maximillionsnyder.umafinidad.ui.componentes.RankPill

/* Panel de acciones rápidas que se despliega desde la burbuja flotante.
   El servicio lo hospeda en su propia ventana de overlay. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PanelBurbuja(
    modelo: AffinityModel?,
    japones: Boolean,
    ladoDerecho: Boolean,
    onCerrar: () -> Unit,
    onOcultar: () -> Unit,
    onAbrirDestino: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var estado by remember { mutableStateOf(TrioEstado()) }
    var filtro by remember { mutableStateOf("") }

    val sugerencias = remember(filtro, modelo) {
        if (modelo == null || filtro.trim().length < 2) {
            emptyList()
        } else {
            rankearSugerencias(
                modelo.personajes.filter { it.playable == true && it.active == true },
                filtro,
            )
        }
    }

    fun elegirSugerencia(id: Int) {
        estado = estado.alternar(id)
        filtro = ""
    }

    val resultado = remember(modelo, estado) { modelo?.let { calcularTrio(it, estado) } }

    Box(modifier = modifier.fillMaxSize()) {
        /* Velo: tocar fuera del panel lo cierra. */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.cerrar), onClick = onCerrar),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 10.dp, vertical = 16.dp),
            contentAlignment = if (ladoDerecho) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.94f)
                    /* Consume los taps del panel para no cerrar el velo. */
                    .pointerInput(Unit) { detectTapGestures {} },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    /* ---- Cabecera ---- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.burbuja_panel_titulo),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = onCerrar) {
                            Icon(
                                painterResource(R.drawable.ic_cerrar),
                                contentDescription = stringResource(R.string.cerrar),
                            )
                        }
                    }

                    /* ---- Calculadora rápida: hijo + dos padres ---- */
                    Text(
                        stringResource(R.string.burbuja_calc_titulo),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 0 until SLOTS_TRIO) {
                            SlotTrio(
                                etiqueta = stringResource(
                                    if (i == 0) R.string.rol_corto_hijo else R.string.rol_corto_padre,
                                ),
                                personaje = estado.ids[i]?.let { modelo?.porId(it) },
                                japones = japones,
                                onClick = { estado = estado.quitar(i) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = filtro,
                        onValueChange = { filtro = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(painterResource(R.drawable.ic_buscar), contentDescription = null)
                        },
                        trailingIcon = {
                            if (filtro.isNotEmpty()) {
                                IconButton(onClick = { filtro = "" }) {
                                    Icon(
                                        painterResource(R.drawable.ic_cerrar),
                                        contentDescription = stringResource(R.string.limpiar_todo),
                                    )
                                }
                            }
                        },
                        placeholder = { Text(stringResource(R.string.buscar)) },
                    )

                    if (sugerencias.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        ) {
                            Column {
                                sugerencias.forEach { c ->
                                    FilaSugerencia(c, japones) { elegirSugerencia(c.charId) }
                                }
                            }
                        }
                    }

                    when {
                        resultado != null -> ResumenTrio(resultado)
                        else -> Text(
                            stringResource(R.string.burbuja_calc_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (estado.ids.any { it != null }) {
                        TextButton(onClick = { estado = estado.limpiar() }) {
                            Text(stringResource(R.string.limpiar_todo))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    /* ---- Atajos a la app ---- */
                    Text(
                        stringResource(R.string.burbuja_abrir),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    BotonDestino(R.string.tab_compat, R.drawable.ic_tab_compat) {
                        onAbrirDestino(BurbujaService.DESTINO_COMPAT)
                    }
                    BotonDestino(R.string.tab_corredora, R.drawable.ic_tab_corredora) {
                        onAbrirDestino(BurbujaService.DESTINO_CORREDORA)
                    }
                    BotonDestino(R.string.tab_elenco, R.drawable.ic_tab_elenco) {
                        onAbrirDestino(BurbujaService.DESTINO_ELENCO)
                    }
                    BotonDestino(R.string.tab_ajustes, R.drawable.ic_tab_ajustes) {
                        onAbrirDestino(BurbujaService.DESTINO_AJUSTES)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    TextButton(onClick = onOcultar, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.burbuja_ocultar))
                    }
                }
            }
        }
    }
}

/* Slot de la calculadora: muestra el avatar o un "+" para elegir. */
@Composable
private fun SlotTrio(
    etiqueta: String,
    personaje: Character?,
    japones: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(
            enabled = personaje != null,
            role = Role.Button,
            onClickLabel = stringResource(R.string.quitar_personaje),
            onClick = onClick,
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (personaje != null) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        border = if (personaje != null) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (personaje != null) {
                Avatar(personaje.charId, personaje.displayName(japones), modifier = Modifier.size(40.dp))
                Text(
                    personaje.displayName(japones),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "＋",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FilaSugerencia(c: Character, japones: Boolean, onClick: () -> Unit) {
    val nombrePrincipal = c.displayName(japones)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Avatar(c.charId, nombrePrincipal, modifier = Modifier.size(32.dp))
        Text(
            nombrePrincipal,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResumenTrio(resultado: TrioResultado) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.burbuja_total),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                RankPill(resultado.rango, resultado.puntos, grande = true)
            }
            if (resultado.compartidos.isEmpty()) {
                Text(
                    stringResource(R.string.sin_grupos_comun),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    resultado.compartidos.forEach { grupo ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Text(
                                "#${grupo.tipo} · ${grupo.puntos}pt",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonDestino(textoRes: Int, iconoRes: Int, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(painterResource(iconoRes), contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(textoRes),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
