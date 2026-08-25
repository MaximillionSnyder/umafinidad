package com.maximillionsnyder.umafinidad.ui.compat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.GrupoCompartido
import com.maximillionsnyder.umafinidad.domain.Rango
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.posicionesDe
import com.maximillionsnyder.umafinidad.ui.EstadoSeccion
import com.maximillionsnyder.umafinidad.ui.FilaVinculoUi
import com.maximillionsnyder.umafinidad.ui.QuitarResultado
import com.maximillionsnyder.umafinidad.ui.ResultadoCompat
import com.maximillionsnyder.umafinidad.ui.ToggleResultado
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import com.maximillionsnyder.umafinidad.ui.componentes.PuntosRango
import java.text.Normalizer

private fun normalizar(v: String): String =
    Normalizer.normalize(v, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()

@Composable
fun etiquetaRol(i: Int): String = stringResource(
    when (i) {
        0 -> R.string.rol_hijo
        1 -> R.string.rol_padre1
        2 -> R.string.rol_padre2
        3 -> R.string.rol_abuelo1_p1
        4 -> R.string.rol_abuelo2_p1
        5 -> R.string.rol_abuelo1_p2
        else -> R.string.rol_abuelo2_p2
    },
)

private fun rolCortoRes(i: Int): Int = when {
    i == 0 -> R.string.rol_corto_hijo
    i <= 2 -> R.string.rol_corto_padre
    else -> R.string.rol_corto_abuelo
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatScreen(
    modelo: AffinityModel,
    seleccion: List<Int?>,
    resultado: ResultadoCompat?,
    japones: Boolean,
    onToggle: (Int) -> ToggleResultado,
    onQuitarSlot: (Int) -> QuitarResultado,
    onConfirmarQuitarSoloHijo: () -> Unit,
    onLimpiarTodo: () -> Unit,
    avisar: (String) -> Unit,
) {
    var filtro by rememberSaveable { mutableStateOf("") }
    var sheetAbierto by rememberSaveable { mutableStateOf(false) }
    var dialogoQuitar by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    /* Mensajes resueltos en composición para usarlos desde callbacks. */
    val msgSeleccionCompleta = stringResource(R.string.seleccion_completa)
    val msgRegla = stringResource(R.string.regla_slots)

    fun manejarToggle(id: Int) {
        when (onToggle(id)) {
            ToggleResultado.SELECCION_COMPLETA -> avisar(msgSeleccionCompleta)
            ToggleResultado.REGLA -> avisar(msgRegla)
            else -> {}
        }
    }

    fun manejarQuitar(i: Int) {
        if (onQuitarSlot(i) == QuitarResultado.NECESITA_CONFIRMACION) dialogoQuitar = true
    }

    Column(modifier = Modifier.fillMaxSize()) {

        /* ---- Fila de slots fijos (hijo/padres/abuelos) ---- */
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            lazyRowItems((0 until SLOTS).toList()) { i ->
                SlotChip(
                    etiqueta = etiquetaRol(i),
                    personaje = seleccion[i]?.let { modelo.porId(it) },
                    japones = japones,
                    onClick = { manejarQuitar(i) },
                )
            }
        }

        /* ---- Buscador ---- */
        OutlinedTextField(
            value = filtro,
            onValueChange = { filtro = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.buscar)) },
        )

        /* ---- Grilla de personajes ---- */
        val filtrados = modelo.personajes
            .filter { it.playable == true && it.active == true }
            .filter { coincide(it, filtro) }

        if (filtrados.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.sin_resultados), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtrados, key = { it.charId }) { c ->
                    CharacterCard(
                        personaje = c,
                        seleccion = seleccion,
                        japones = japones,
                        onClick = { manejarToggle(c.charId) },
                    )
                }
            }
        }

        /* ---- Botón flotante de resultado ---- */
        if (seleccion.any { it != null }) {
            Button(
                onClick = { sheetAbierto = true },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            ) {
                Text(stringResource(R.string.ver_afinidad), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (sheetAbierto && resultado != null) {
        ModalBottomSheet(onDismissRequest = { sheetAbierto = false }, sheetState = sheetState) {
            ResultadoPanel(modelo, resultado, japones)
        }
    }

    if (dialogoQuitar) {
        AlertDialog(
            onDismissRequest = { dialogoQuitar = false },
            title = { Text(stringResource(R.string.quitar_hijo_titulo)) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmarQuitarSoloHijo(); dialogoQuitar = false
                }) { Text(stringResource(R.string.quitar_solo_hijo)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    onLimpiarTodo(); dialogoQuitar = false
                }) { Text(stringResource(R.string.limpiar_todo)) }
            },
        )
    }
}

/* ---------- Slots ---------- */

@Composable
private fun SlotChip(etiqueta: String, personaje: Character?, japones: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (personaje != null) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (personaje != null) {
                Avatar(personaje.charId, personaje.displayName(japones), modifier = Modifier.size(28.dp))
            }
            Column {
                Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = personaje?.displayName(japones) ?: "—",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/* ---------- Cards de personajes (porte de grid.js) ---------- */

private fun coincide(c: Character, filtro: String): Boolean {
    val f = normalizar(filtro.trim())
    if (f.isEmpty()) return true
    return normalizar(c.enName ?: "").contains(f) ||
        (c.jpName ?: "").contains(filtro.trim()) ||
        (c.urlName ?: "").contains(f)
}

@Composable
private fun CharacterCard(personaje: Character, seleccion: List<Int?>, japones: Boolean, onClick: () -> Unit) {
    val nombrePrincipal = personaje.displayName(japones)
    val nombreSecundario = if (japones) personaje.enName ?: "" else personaje.jpName ?: ""
    val roles = posicionesDe(seleccion.toTypedArray(), personaje.charId)
        .map { rolCortoRes(it) }
        .distinct()

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Avatar(personaje.charId, nombrePrincipal, modifier = Modifier.size(42.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nombrePrincipal, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(nombreSecundario, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (roles.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        roles.forEach { r ->
                            Text(
                                stringResource(r),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
            }
            if (roles.isNotEmpty()) {
                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* ---------- Panel de resultado (porte de result.js) ---------- */

@Composable
fun ResultadoPanel(modelo: AffinityModel, res: ResultadoCompat, japones: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        if (res.vacio) {
            Nota(stringResource(R.string.elegi_hijo_empezar))
            return@Column
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.total_herencia), style = MaterialTheme.typography.titleMedium)
                PuntosRango(res.rangoTotal, res.total ?: 0)
            }
        }

        Spacer(Modifier.height(10.dp))

        SeccionVinculos(titulo = stringResource(R.string.sec_hijo_padres), filas = res.hijoPadres, estado = res.estadoHijoPadres, modelo = modelo, japones = japones)

        SeccionEntrePadres(res, modelo, japones)

        SeccionVinculos(titulo = stringResource(R.string.sec_hijo_padres_abuelos), filas = res.hijoPadreAbuelos, estado = res.estadoHijoPadreAbuelos, modelo = modelo, japones = japones)

        if (res.notaSinHijo) {
            Nota(stringResource(R.string.sin_hijo_completa))
        }
    }
}

@Composable
private fun SeccionVinculos(titulo: String, filas: List<FilaVinculoUi>, estado: EstadoSeccion, modelo: AffinityModel, japones: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        when (estado) {
            EstadoSeccion.CON_FILAS -> filas.forEach { FilaVinculo(it, modelo, japones) }
            EstadoSeccion.FALTA_HIJO -> Nota(stringResource(R.string.falta_hijo))
            EstadoSeccion.ELIGE_PADRE -> Nota(stringResource(R.string.elige_un_padre))
            EstadoSeccion.OTRO_PADRE -> Nota(stringResource(R.string.elegi_otro_padre))
            EstadoSeccion.FALTAN_PADRES -> Nota(stringResource(R.string.faltan_padres))
            EstadoSeccion.SIN_ABUELOS -> Nota(stringResource(R.string.no_hay_abuelos))
        }
    }
}

@Composable
private fun SeccionEntrePadres(res: ResultadoCompat, modelo: AffinityModel, japones: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(stringResource(R.string.sec_entre_padres), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        when (res.estadoEntrePadres) {
            EstadoSeccion.CON_FILAS -> res.entrePadres?.let { FilaVinculo(it, modelo, japones) }
            EstadoSeccion.OTRO_PADRE -> Nota(stringResource(R.string.elegi_otro_padre))
            EstadoSeccion.FALTAN_PADRES -> Nota(stringResource(R.string.faltan_padres))
            else -> {}
        }
    }
}

@Composable
private fun FilaVinculo(v: FilaVinculoUi, modelo: AffinityModel, japones: Boolean) {
    val nombres = v.ids.map { id -> modelo.porId(id)?.displayName(japones) ?: id.toString() }.joinToString(" × ")
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(nombres, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            PuntosRango(v.rango, v.puntos)
        }
        if (v.esCorredora) {
            Nota(stringResource(R.string.corredora_nota), compacta = true)
        }
        if (v.compartidos.isEmpty()) {
            Nota(stringResource(R.string.sin_grupos_comun), compacta = true)
        } else {
            val visibles = v.compartidos.take(6)
            visibles.forEach { g ->
                Text(
                    "#${g.tipo} · ${g.puntos}pt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (v.compartidos.size > 6) {
                Text(
                    stringResource(R.string.mas_grupos, v.compartidos.size - 6),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun Nota(texto: String, compacta: Boolean = false) {
    Text(
        texto,
        style = if (compacta) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start,
        modifier = Modifier.padding(vertical = 2.dp),
    )
}
