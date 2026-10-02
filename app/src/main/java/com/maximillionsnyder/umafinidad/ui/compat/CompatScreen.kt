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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.maximillionsnyder.umafinidad.overlay.BotonCompacto
import com.maximillionsnyder.umafinidad.ui.AgregarResultado
import com.maximillionsnyder.umafinidad.ui.AutocompletarResultado
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBar
import com.maximillionsnyder.umafinidad.ui.componentes.headingSemantica
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.ModoGrilla
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.coincideDifuso
import com.maximillionsnyder.umafinidad.domain.puedeIrEn
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.SlotEstado
import com.maximillionsnyder.umafinidad.domain.SlotOpcion
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
    autocompletando: Boolean = false,
    /* Mismo autocompletar que el panel de la burbuja: rellena los huecos con
       la mejor afinidad. Devuelve por qué no se pudo, si no se pudo. */
    onAutocompletar: () -> AutocompletarResultado = { AutocompletarResultado.CALCULANDO },
    /* Slot marcado como destino de la próxima colocación (tocar un chip
       vacío). null = se coloca en el primer hueco válido. */
    slotDestino: Int? = null,
    onMarcarDestino: (Int) -> Unit = {},
    /* Estado de los 7 slots para un personaje ya colocado, y la colocación
       de una segunda copia (el hijo también puede ser abuelo). */
    slotsPara: (Int) -> List<SlotOpcion> = { emptyList() },
    onAgregar: (Int, Int) -> AgregarResultado = { _, _ -> AgregarResultado.NO_PUDO },
) {
    var filtro by rememberSaveable { mutableStateOf("") }
    var sheetAbierto by rememberSaveable { mutableStateOf(false) }
    var dialogoQuitar by rememberSaveable { mutableStateOf(false) }
    /* Personaje al que se le está eligiendo lugar; -1 = hoja cerrada. */
    var idElegir by rememberSaveable { mutableStateOf(-1) }

    val sugerencias = remember(filtro, modelo, slotDestino, seleccion) {
        if (filtro.trim().length < 2) emptyList()
        else {
            val base = if (slotDestino == null) {
                modelo.personajes
            } else {
                /* Con destino marcado solo lo que las reglas dejan poner ahí. */
                val actual = seleccion.toTypedArray()
                modelo.personajes.filter { puedeIrEn(actual, slotDestino, it.charId) }
            }
            rankearSugerencias(base, filtro)
        }
    }

    val msgSeleccionCompleta = stringResource(R.string.seleccion_completa)
    val msgRegla = stringResource(R.string.regla_slots)
    val msgFaltaHijo = stringResource(R.string.elegi_hijo_empezar)

    fun manejarQuitar(i: Int) {
        if (onQuitarSlot(i) == QuitarResultado.NECESITA_CONFIRMACION) dialogoQuitar = true
    }

    /* Tocar un chip de la genealogía: si está ocupado lo quita; si está libre
       lo marca como destino de la próxima colocación, como en la burbuja. */
    fun tocarSlot(i: Int) {
        if (seleccion[i] != null) manejarQuitar(i) else onMarcarDestino(i)
    }

    /* Quita la última copia del personaje, pasando por la confirmación si es
       el hijo (igual que tocar su chip en la genealogía). */
    fun quitarUltima(id: Int) {
        val pos = seleccion.indexOfLast { it == id }
        if (pos >= 0) manejarQuitar(pos)
    }

    /* Elegir un personaje ya colocado abre el selector de lugar, para ponerlo
       también en otro slot (el hijo puede ser abuelo) o para quitar una de
       sus copias. Solo si hay algún slot libre donde las reglas lo dejan;
       si no, se quita como antes. Con un destino marcado manda el destino.
       Devuelve true si la genealogía llegó a cambiar. */
    fun manejarToggle(id: Int): Boolean {
        val yaElegido = seleccion.any { it == id }
        if (yaElegido && slotDestino == null) {
            if (slotsPara(id).any { it.estado == SlotEstado.VALIDO }) {
                idElegir = id
                return false
            }
            quitarUltima(id)
            return true
        }
        val r = onToggle(id)
        when (r) {
            ToggleResultado.SELECCION_COMPLETA -> avisar(msgSeleccionCompleta)
            ToggleResultado.REGLA -> avisar(msgRegla)
            else -> {}
        }
        return r == ToggleResultado.COLOCADO || r == ToggleResultado.QUITADO
    }

    /* Opción A: al elegir una sugerencia se coloca el personaje y se limpia
       el buscador; si no pudo colocarse, el texto queda para corregir. Con el
       selector de lugar abierto el texto se conserva. */
    fun elegirSugerencia(id: Int) {
        if (manejarToggle(id)) filtro = ""
    }

    /* Toca un slot del selector: si el personaje ya lo ocupa lo quita (con la
       confirmación del hijo), si está libre y las reglas lo dejan lo agrega
       como segunda copia. */
    fun elegirSlot(slot: Int, estado: SlotEstado) {
        val id = idElegir
        if (id < 0) return
        if (estado == SlotEstado.ACTUAL) {
            quitarUltima(id)
            idElegir = -1
        } else if (onAgregar(id, slot) == AgregarResultado.AGREGADO) {
            idElegir = -1
        } else {
            avisar(msgRegla)
        }
    }

    fun manejarAutocompletar() {
        when (onAutocompletar()) {
            AutocompletarResultado.FALTA_HIJO -> avisar(msgFaltaHijo)
            AutocompletarResultado.SELECCION_COMPLETA -> avisar(msgSeleccionCompleta)
            AutocompletarResultado.CALCULANDO -> {}
        }
    }

    val seleccionSet = remember(seleccion) { seleccion.filterNotNull().toSet() }
    val filtrados = remember(modelo, filtro, seleccionSet) {
        modelo.personajes
            .filter { it.playable == true && it.active == true }
            .filter { coincideDifuso(it, filtro) }
            .sortedWith(compareBy { if (it.charId in seleccionSet) 0 else 1 })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            /* ---- Cabecera Herencia + slots SIEMPRE visibles ---- */
            HeaderBar(
                titulo = stringResource(R.string.seccion_herencia),
                pillTexto = stringResource(R.string.herencia_contador, seleccion.count { it != null }),
                chip = {
                    /* Mismo atajo que la cabecera del panel de la burbuja. */
                    if (autocompletando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    } else {
                        BotonCompacto(
                            iconoRes = R.drawable.ic_autocompletar,
                            descripcionRes = R.string.burbuja_autocompletar,
                            enabled = true,
                            onClick = ::manejarAutocompletar,
                        )
                    }
                },
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
                            destino = slotDestino == i,
                            onClick = { tocarSlot(i) },
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
                            destino = slotDestino == i,
                            onClick = { tocarSlot(i) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                /* Con destino marcado se dice a qué rol va el próximo
                   personaje (mismo texto que el panel de la burbuja). */
                slotDestino?.let { destino ->
                    Text(
                        stringResource(R.string.burbuja_destino, etiquetaRol(destino)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
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
                                        .clickable(role = Role.Button) { elegirSugerencia(c.charId) }
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
                // Al venir de ver herencia, los seleccionados se acomodan primero para no tener que scrollear
                androidx.compose.runtime.key(seleccion) {
                    when (modoGrilla) {
                        ModoGrilla.TARJETAS -> GrillaTarjetas(filtrados, seleccion, japones, { manejarToggle(it) }, Modifier.weight(1f))
                        ModoGrilla.LISTA -> GrillaLista(filtrados, seleccion, japones, { manejarToggle(it) }, Modifier.weight(1f))
                    }
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
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
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

    /* Elegir de nuevo un personaje ya colocado: hoja con los 7 slots. */
    val personajeElegir = if (idElegir >= 0) modelo.porId(idElegir) else null
    if (personajeElegir != null) {
        ModalBottomSheet(onDismissRequest = { idElegir = -1 }) {
            SelectorSlots(
                modelo = modelo,
                personaje = personajeElegir,
                opciones = slotsPara(idElegir),
                seleccion = seleccion,
                japones = japones,
                onSlot = ::elegirSlot,
            )
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
private fun SlotChip(
    etiqueta: String,
    personaje: Character?,
    slot: Int,
    japones: Boolean,
    destino: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rolColor = colorDeGenealogia(slot)
    /* Ocupado = quitar; libre = marcar como destino (mismo texto que la
       burbuja, así el lector de pantalla no lo anuncia como un botón vacío). */
    val etiquetaAccion = if (personaje != null) {
        stringResource(R.string.quitar_personaje)
    } else {
        stringResource(R.string.burbuja_elegir_lugar)
    }
    Card(
        modifier = modifier.clickable(
            role = Role.Button,
            onClickLabel = etiquetaAccion,
            onClick = onClick,
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                destino -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (destino) 2.dp else 1.dp,
            when {
                destino -> MaterialTheme.colorScheme.primary
                personaje != null -> rolColor.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.outlineVariant
            },
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

/* ---------- Selector de lugar (elegir un personaje ya colocado) ---------- */

/* Un personaje puede estar en más de un slot (el hijo también de abuelo: es
   la corredora que vale 0). Esta hoja deja elegir cuál se agrega y cuál se
   quita, con las reglas mandando: los slots que no admiten al personaje salen
   deshabilitados y los que tiene otro también. */
@Composable
private fun SelectorSlots(
    modelo: AffinityModel,
    personaje: Character,
    opciones: List<SlotOpcion>,
    seleccion: List<Int?>,
    japones: Boolean,
    onSlot: (Int, SlotEstado) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            stringResource(R.string.elegir_slot_titulo, personaje.displayName(japones)),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.headingSemantica(),
        )
        opciones.forEach { opcion ->
            FilaSlot(opcion, modelo, seleccion, japones, onSlot)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FilaSlot(
    opcion: SlotOpcion,
    modelo: AffinityModel,
    seleccion: List<Int?>,
    japones: Boolean,
    onSlot: (Int, SlotEstado) -> Unit,
) {
    val rolColor = colorDeGenealogia(opcion.slot)
    val ocupante = seleccion[opcion.slot]?.let { modelo.porId(it) }
    val detalle = when (opcion.estado) {
        SlotEstado.ACTUAL -> stringResource(R.string.slot_aqui)
        SlotEstado.VALIDO -> "—"
        SlotEstado.OCUPADO -> ocupante?.displayName(japones) ?: "—"
        SlotEstado.BLOQUEADO -> stringResource(R.string.slot_bloqueado)
    }
    /* Solo dos estados se pueden tocar: agregar una copia o quitar la que ya
       está. Los otros dos se anuncian pero no hacen nada. */
    val accion = when (opcion.estado) {
        SlotEstado.ACTUAL -> stringResource(R.string.slot_quitar_aca)
        SlotEstado.VALIDO -> stringResource(R.string.slot_agregar)
        SlotEstado.OCUPADO, SlotEstado.BLOQUEADO -> null
    }
    val habilitado = accion != null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = habilitado,
                role = if (habilitado) Role.Button else null,
                onClickLabel = accion,
                onClick = { onSlot(opcion.slot, opcion.estado) },
            )
            .semantics { stateDescription = detalle },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habilitado) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.55f)
            },
        ),
        border = if (habilitado) androidx.compose.foundation.BorderStroke(1.5.dp, rolColor) else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    etiquetaRol(opcion.slot),
                    style = MaterialTheme.typography.labelSmall,
                    color = rolColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    detalle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (habilitado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (opcion.estado == SlotEstado.ACTUAL) {
                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
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
    val rolesTexto = roles.map { stringResource(it) }.joinToString(", ")

    Card(
        modifier = Modifier
            .toggleable(value = seleccionado, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { if (rolesTexto.isNotEmpty()) stateDescription = rolesTexto },
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
            Text(nombrePrincipal, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            if (nombreSecundario.isNotEmpty()) {
                Text(nombreSecundario, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
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
    val rolesTexto = roles.map { stringResource(it) }.joinToString(", ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = seleccionado, role = Role.Checkbox, onValueChange = { onClick() })
            .semantics { if (rolesTexto.isNotEmpty()) stateDescription = rolesTexto },
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
                    Text(nombrePrincipal, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (nombreSecundario.isNotEmpty()) {
                        Text(nombreSecundario, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                .semantics { liveRegion = LiveRegionMode.Polite }
                .padding(horizontal = 16.dp)
                .background(fondoTotal, RoundedCornerShape(20.dp))
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.total_herencia),
                    modifier = Modifier.headingSemantica(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
        Text(titulo, modifier = Modifier.headingSemantica(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
        Text(stringResource(R.string.sec_entre_padres), modifier = Modifier.headingSemantica(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
