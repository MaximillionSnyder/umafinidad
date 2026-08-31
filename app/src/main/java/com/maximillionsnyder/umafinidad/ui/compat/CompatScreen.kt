package com.maximillionsnyder.umafinidad.ui.compat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.ModoGrilla
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.coincideDifuso
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.ui.theme.colorDeGenealogia
import com.maximillionsnyder.umafinidad.ui.EstadoSeccion
import com.maximillionsnyder.umafinidad.ui.FilaVinculoUi
import com.maximillionsnyder.umafinidad.ui.QuitarResultado
import com.maximillionsnyder.umafinidad.ui.ResultadoCompat
import com.maximillionsnyder.umafinidad.ui.ToggleResultado

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
    modoGrilla: ModoGrilla,
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

    val sugerencias = remember(filtro, modelo) {
        if (filtro.trim().length >= 2) rankearSugerencias(modelo.personajes, filtro)
        else emptyList()
    }

    /* Opción A: al elegir una sugerencia se coloca el personaje y se limpia
       el buscador; si no pudo colocarse, el texto queda para corregir. */
    fun elegirSugerencia(id: Int) {
        when (onToggle(id)) {
            ToggleResultado.COLOCADO, ToggleResultado.QUITADO -> filtro = ""
            else -> {}
        }
    }

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

    val filtrados = modelo.personajes
        .filter { it.playable == true && it.active == true }
        .filter { coincideDifuso(it, filtro) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            /* ---- Cabecera Herencia + slots SIEMPRE visibles ---- */
            HeaderBar(
                titulo = stringResource(R.string.seccion_herencia),
                pillTexto = stringResource(R.string.herencia_contador, seleccion.count { it != null })
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                /* Fila genealógica 1: hijo + padres */
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0, 1, 2).forEach { i ->
                        SlotChip(
                            etiqueta = etiquetaRol(i),
                            personaje = seleccion[i]?.let { modelo.porId(it) },
                            slot = i,
                            japones = japones,
                            onClick = { manejarQuitar(i) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                /* Fila genealógica 2: los cuatro abuelos */
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 4, 5, 6).forEach { i ->
                        SlotChip(
                            etiqueta = etiquetaRol(i),
                            personaje = seleccion[i]?.let { modelo.porId(it) },
                            slot = i,
                            japones = japones,
                            onClick = { manejarQuitar(i) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            /* ---- Buscador con autocompletado difuso ---- */
            /* Las sugerencias se dibujan DENTRO de la pantalla (no en una
               ventana modal): así la grilla, el FAB y los tabs siguen siendo
               tocables y el teclado nunca pierde el foco. */
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = filtro,
                    onValueChange = { filtro = it },
                    modifier = Modifier.fillMaxWidth(),
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

                if (sugerencias.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        Column {
                            sugerencias.forEachIndexed { indice, c ->
                                val nombrePrincipal = c.displayName(japones)
                                val nombreSecundario = if (japones) c.enName ?: "" else c.jpName ?: ""
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { elegirSugerencia(c.charId) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Avatar(c.charId, nombrePrincipal, modifier = Modifier.size(32.dp))
                                    Column {
                                        Text(nombrePrincipal, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (nombreSecundario.isNotEmpty()) {
                                            Text(nombreSecundario, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                                if (indice < sugerencias.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }
            }

            /* ---- Grilla de personajes (dos modos) ---- */
            if (filtrados.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(painterResource(R.drawable.ic_buscar), contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        Text(stringResource(R.string.sin_resultados), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                when (modoGrilla) {
                    ModoGrilla.TARJETAS -> GrillaTarjetas(filtrados, seleccion, japones, ::manejarToggle, Modifier.weight(1f))
                    ModoGrilla.LISTA -> GrillaLista(filtrados, seleccion, japones, ::manejarToggle, Modifier.weight(1f))
                }
            }
        }

        /* ---- FAB para ver el resultado ---- */
        AnimatedVisibility(
            visible = seleccion.any { it != null },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            ExtendedFloatingActionButton(
                onClick = { sheetAbierto = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(painterResource(R.drawable.ic_corazon), contentDescription = null) },
                text = { Text(stringResource(R.string.ver_afinidad), fontWeight = FontWeight.Bold) },
            )
        }
    }

    if (sheetAbierto && resultado != null) {
        ModalBottomSheet(onDismissRequest = { sheetAbierto = false }) {
            /* El scroll ahora vive acá, en el contenedor del sheet. */
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                ResultadoPanel(modelo, resultado, japones)
            }
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

/* ---------- Slots coloreados por genealogía ---------- */

@Composable
private fun SlotChip(etiqueta: String, personaje: Character?, slot: Int, japones: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val rolColor = colorDeGenealogia(slot)
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (personaje != null) rolColor.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
        ) {
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = rolColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = personaje?.displayName(japones) ?: "—",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (personaje != null) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/* ---------- Búsqueda difusa (ver domain/Busqueda.kt) ---------- */

/* ---------- Modo TARJETAS: avatar grande centrado ---------- */

@Composable
private fun GrillaTarjetas(
    filtrados: List<Character>,
    seleccion: List<Int?>,
    japones: Boolean,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filtrados, key = { it.charId }) { c ->
            CardTarjeta(c, seleccion, japones) { onToggle(c.charId) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardTarjeta(personaje: Character, seleccion: List<Int?>, japones: Boolean, onClick: () -> Unit) {
    val nombrePrincipal = personaje.displayName(japones)
    val nombreSecundario = if (japones) personaje.enName ?: "" else personaje.jpName ?: ""
    val roles = posicionesRes(seleccion, personaje.charId)

    val seleccionado = roles.isNotEmpty()
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = if (seleccionado) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box {
                Avatar(personaje.charId, nombrePrincipal, modifier = Modifier.size(64.dp))
                androidx.compose.animation.AnimatedVisibility(visible = seleccionado, enter = androidx.compose.animation.scaleIn(), exit = androidx.compose.animation.scaleOut()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✓", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(nombrePrincipal, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            if (nombreSecundario.isNotEmpty()) {
                Text(nombreSecundario, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            }
            if (roles.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
    }
}

/* ---------- Modo LISTA: fila refinada con franja de color ---------- */

@Composable
private fun GrillaLista(
    filtrados: List<Character>,
    seleccion: List<Int?>,
    japones: Boolean,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(filtrados.size, key = { filtrados[it].charId }) { idx ->
            CardFila(filtrados[idx], seleccion, japones) { onToggle(filtrados[idx].charId) }
        }
    }
}

@Composable
private fun CardFila(personaje: Character, seleccion: List<Int?>, japones: Boolean, onClick: () -> Unit) {
    val nombrePrincipal = personaje.displayName(japones)
    val nombreSecundario = if (japones) personaje.enName ?: "" else personaje.jpName ?: ""
    val roles = posicionesRes(seleccion, personaje.charId)
    val seleccionado = roles.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = if (seleccionado) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(modifier = Modifier.height(androidx.compose.ui.unit.Dp.Unspecified)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(com.maximillionsnyder.umafinidad.ui.componentes.colorDeAvatar(personaje.charId)),
            )
            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Avatar(personaje.charId, nombrePrincipal, modifier = Modifier.size(48.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(nombrePrincipal, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (nombreSecundario.isNotEmpty()) {
                        Text(nombreSecundario, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                AnimatedVisibility(visible = seleccionado) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
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
                        Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun posicionesRes(seleccion: List<Int?>, id: Int): List<Int> =
    seleccion.withIndex().filter { it.value == id }.map { it.index }.map { rolCortoRes(it) }

/* ---------- Panel de resultado ---------- */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultadoPanel(modelo: AffinityModel, res: ResultadoCompat, japones: Boolean) {
    /* Sin scroll interno: el contenedor de cada pantalla decide el scroll
       (un scroll anidado en la misma dirección crashea en Compose). */
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        if (res.vacio) {
            Nota(stringResource(R.string.elegi_hijo_empezar))
            return@Column
        }

        /* Total gigante con fondo tintado por rango */
        val fondoTotal = com.maximillionsnyder.umafinidad.ui.theme.fondoDeRango(res.rangoTotal?.clase)
            ?: MaterialTheme.colorScheme.surfaceVariant
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(fondoTotal, RoundedCornerShape(20.dp))
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.total_herencia), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                RankPillGrande(res.rangoTotal, res.total ?: 0)
            }
        }

        Spacer(Modifier.height(12.dp))

        SeccionVinculos(titulo = stringResource(R.string.sec_hijo_padres), filas = res.hijoPadres, estado = res.estadoHijoPadres, modelo = modelo, japones = japones)
        SeccionEntrePadres(res, modelo, japones)
        SeccionVinculos(titulo = stringResource(R.string.sec_hijo_padres_abuelos), filas = res.hijoPadreAbuelos, estado = res.estadoHijoPadreAbuelos, modelo = modelo, japones = japones)

        if (res.notaSinHijo) {
            Nota(stringResource(R.string.sin_hijo_completa))
        }
    }
}

/* ---------- Panel de resultado ---------- */

@Composable
private fun RankPillGrande(rango: com.maximillionsnyder.umafinidad.domain.Rango?, puntos: Int) {
    val colores = com.maximillionsnyder.umafinidad.ui.theme.LocalColoresRango.current
    val frente = when (rango?.clase) {
        "rank-great" -> colores.great
        "rank-good" -> colores.good
        "rank-fair" -> colores.fair
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = (rango?.simbolo?.plus(" ") ?: "") + puntos,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Black,
        color = frente,
    )
}

@Composable
private fun SeccionVinculos(titulo: String, filas: List<FilaVinculoUi>, estado: EstadoSeccion, modelo: AffinityModel, japones: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(6.dp))
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
        Spacer(Modifier.height(6.dp))
        when (res.estadoEntrePadres) {
            EstadoSeccion.CON_FILAS -> res.entrePadres?.let { FilaVinculo(it, modelo, japones) }
            EstadoSeccion.OTRO_PADRE -> Nota(stringResource(R.string.elegi_otro_padre))
            EstadoSeccion.FALTAN_PADRES -> Nota(stringResource(R.string.faltan_padres))
            else -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilaVinculo(v: FilaVinculoUi, modelo: AffinityModel, japones: Boolean) {
    val nombres = v.ids.map { id -> modelo.porId(id)?.displayName(japones) ?: id.toString() }.joinToString(" × ")
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(nombres, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                com.maximillionsnyder.umafinidad.ui.componentes.RankPill(v.rango, v.puntos)
            }
            if (v.esCorredora) {
                Nota(stringResource(R.string.corredora_nota), compacta = true)
            }
        }
    }
}

@Composable
private fun ChipGrupo(texto: String, extra: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = if (extra.isEmpty()) texto else "$texto · $extra",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
