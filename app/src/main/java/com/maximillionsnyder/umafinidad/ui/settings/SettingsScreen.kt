package com.maximillionsnyder.umafinidad.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.maximillionsnyder.umafinidad.BuildConfig
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.ArbolGuardado
import com.maximillionsnyder.umafinidad.data.ModoGrilla
import com.maximillionsnyder.umafinidad.data.ThemeMode
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBar

/* Apartado de ajustes: por ahora, el modo de grilla de personajes. */
@Composable
fun SettingsScreen(
    modoGrilla: ModoGrilla,
    onModoGrilla: (ModoGrilla) -> Unit,
    tema: ThemeMode,
    onTema: (ThemeMode) -> Unit,
    modelo: AffinityModel,
    japones: Boolean,
    arboles: List<ArbolGuardado>,
    onAbrirArbol: (ArbolGuardado) -> Unit,
    onEliminarArbol: (Long) -> Unit,
    onAbrirGrupos: () -> Unit,
    onAbrirRanking: () -> Unit,
    onAbrirElenco: () -> Unit,
) {
    var aparienciaAbierta by rememberSaveable { mutableStateOf(false) }
    var temaAbierto by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HeaderBar(titulo = stringResource(R.string.tab_mas))

        Text(
            stringResource(R.string.tab_ajustes),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        SeccionDesplegable(
            titulo = stringResource(R.string.ajustes_apariencia),
            abierto = aparienciaAbierta,
            onToggle = { aparienciaAbierta = !aparienciaAbierta },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.modo_grilla_pregunta),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.modo_vertical),
                    descripcion = stringResource(R.string.modo_vertical_desc),
                    seleccionado = modoGrilla == ModoGrilla.TARJETAS,
                    onClick = { onModoGrilla(ModoGrilla.TARJETAS) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.modo_lista),
                    descripcion = stringResource(R.string.modo_lista_desc),
                    seleccionado = modoGrilla == ModoGrilla.LISTA,
                    onClick = { onModoGrilla(ModoGrilla.LISTA) },
                )
            }
        }

        SeccionDesplegable(
            titulo = stringResource(R.string.tema_titulo),
            subtitulo = stringResource(R.string.tema_desc),
            abierto = temaAbierto,
            onToggle = { temaAbierto = !temaAbierto },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OpcionGrilla(
                    titulo = stringResource(R.string.tema_sistema),
                    descripcion = stringResource(R.string.tema_sistema_desc),
                    seleccionado = tema == ThemeMode.SISTEMA,
                    onClick = { onTema(ThemeMode.SISTEMA) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.tema_claro),
                    descripcion = stringResource(R.string.tema_claro_desc),
                    seleccionado = tema == ThemeMode.CLARO,
                    onClick = { onTema(ThemeMode.CLARO) },
                )
                OpcionGrilla(
                    titulo = stringResource(R.string.tema_oscuro),
                    descripcion = stringResource(R.string.tema_oscuro_desc),
                    seleccionado = tema == ThemeMode.OSCURO,
                    onClick = { onTema(ThemeMode.OSCURO) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        /* ===== Grupos (referencia, archivada de la barra inferior) ===== */
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrirGrupos),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_tab_groups),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(stringResource(R.string.tab_groups), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.grupos_ajustes_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        /* ===== Ranking (referencia, archivado de la barra inferior) ===== */
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrirRanking),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_tab_ranking),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(stringResource(R.string.tab_ranking), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.ranking_ajustes_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        /* ===== Mis Umas (referencia, archivado de la barra inferior) ===== */
        Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrirElenco),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_tab_elenco),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column {
                    Text(stringResource(R.string.tab_elenco), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(
                        stringResource(R.string.elenco_ajustes_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        /* ===== Árboles guardados (global) ===== */
        if (arboles.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.arboles_ajustes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            arboles.forEach { a ->
                val nombreHijo = modelo.porId(a.hijoId)?.displayName(japones) ?: "#${'$'}{a.hijoId}"
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = { onAbrirArbol(a) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Row(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(
                                nombreHijo + " · ◎ " + a.total,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { onEliminarArbol(a.id) }) {
                            Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.cancelar))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            "Uma Afinidad v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OpcionGrilla(titulo: String, descripcion: String, seleccionado: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = if (seleccionado) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = seleccionado, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SeccionDesplegable(
    titulo: String,
    abierto: Boolean,
    onToggle: () -> Unit,
    subtitulo: String? = null,
    contenido: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (subtitulo != null) {
                        Text(subtitulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    if (abierto) "∧" else "∨",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            AnimatedVisibility(visible = abierto) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    contenido()
                }
            }
        }
    }
}
