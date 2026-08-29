package com.maximillionsnyder.umafinidad.ui.corredora

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AlternativaSlot
import com.maximillionsnyder.umafinidad.data.ArbolGuardado
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Linaje
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import com.maximillionsnyder.umafinidad.ui.componentes.AptitudesDetalle
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBar
import com.maximillionsnyder.umafinidad.ui.AppViewModel
import com.maximillionsnyder.umafinidad.ui.ResultadoCompat
import com.maximillionsnyder.umafinidad.ui.compat.ResultadoPanel
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* Pestaña "Mi corredora": buscás una corredora y ves SU mejor linaje
   exacto. Los 6 roles secundarios son intercambiables: al tocarlos se
   ofrecen alternativas ordenadas por total resultante (con los puntos
   directos del candidato como referencia). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorredoraScreen(
    modelo: AffinityModel,
    japones: Boolean,
    arboles: List<ArbolGuardado>,
    pendiente: ArbolGuardado?,
    onGuardarArbol: (hijoId: Int, nombre: String, seleccion: List<Int?>, total: Int) -> Unit,
    onEliminarArbol: (Long) -> Unit,
    onConsumirPendiente: () -> Unit,
    avisar: (String) -> Unit,
    onVerHerencia: (List<Int?>) -> Unit,
) {
    var filtro by rememberSaveable { mutableStateOf("") }
    var elegidaId by rememberSaveable { mutableIntStateOf(-1) }
    var linaje by remember { mutableStateOf<Linaje?>(null) }
    var calculando by remember { mutableStateOf(false) }

    /* Selección editable: arranca en el óptimo y cambia con alternativas. */
    var seleccionActual by remember { mutableStateOf<List<Int?>>(emptyList()) }
    var seleccionOptima by remember { mutableStateOf<List<Int?>>(emptyList()) }
    var sheetSlot by remember { mutableStateOf<Int?>(null) }

    /* Cargas externas (config guardada desde Ajustes o de la lista). */
    var recarga by remember { mutableIntStateOf(0) }
    var overrideCarga by remember { mutableStateOf<List<Int?>?>(null) }

    var mostrarGuardar by rememberSaveable { mutableStateOf(false) }
    var textoNombre by rememberSaveable { mutableStateOf("") }

    /* Mensajes resueltos en composición para usarlos desde callbacks. */
    val msgEliminado = stringResource(R.string.eliminado_snack)
    val msgGuardado = stringResource(R.string.guardado_snack)

    fun listaDeLinaje(l: Linaje): List<Int?> = listOf(
        l.hijo.charId,
        l.padre.charId,
        l.madre.charId,
        l.abuelos[0][0].charId,
        l.abuelos[0][1].charId,
        l.abuelos[1][0].charId,
        l.abuelos[1][1].charId,
    )

    val sugerencias = remember(filtro, modelo) {
        if (filtro.trim().length >= 2) rankearSugerencias(modelo.personajes, filtro)
        else emptyList()
    }

    LaunchedEffect(elegidaId, recarga) {
        if (elegidaId <= 0) {
            linaje = null
            seleccionOptima = emptyList()
            seleccionActual = emptyList()
            return@LaunchedEffect
        }
        calculando = true
        val l = withContext(Dispatchers.Default) { modelo.mejorLinajeDe(elegidaId) }
        linaje = l
        seleccionOptima = l?.let { listaDeLinaje(it) } ?: emptyList()
        seleccionActual = overrideCarga?.takeIf { it.size == SLOTS } ?: seleccionOptima
        overrideCarga = null
        calculando = false
    }

    /* Config pedida desde Ajustes. */
    LaunchedEffect(pendiente) {
        pendiente?.let { a ->
            overrideCarga = a.seleccion
            filtro = ""
            elegidaId = a.hijoId
            recarga++
            onConsumirPendiente()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
    ) {
        HeaderBar(
            titulo = stringResource(R.string.tab_corredora),
            pillTexto = stringResource(R.string.herencia_contador, seleccionActual.count { it != null })
        )

        /* Buscador con sugerencias integradas (no modal). */
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            OutlinedTextField(
                value = filtro,
                onValueChange = { filtro = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                leadingIcon = { Icon(painterResource(R.drawable.ic_buscar), contentDescription = null) },
                trailingIcon = {
                    if (filtro.isNotEmpty()) {
                        IconButton(onClick = {
                            filtro = ""
                            elegidaId = -1
                        }) {
                            Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.limpiar_todo))
                        }
                    }
                },
                placeholder = { Text(stringResource(R.string.buscar_corredora)) },
            )

            if (sugerencias.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column {
                        sugerencias.forEachIndexed { indice, c ->
                            FilaSugerencia(c, japones) {
                                elegidaId = c.charId
                                filtro = ""
                            }
                            if (indice < sugerencias.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }

        when {
            elegidaId <= 0 -> {
                Nota(stringResource(R.string.corredora_hint))
            }
            calculando -> {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            linaje == null || seleccionActual.isEmpty() -> {
                Nota(stringResource(R.string.corredora_invalida))
            }
            else -> {
                Column {
                    PanelMejorLinaje(
                        modelo = modelo,
                        nombreHijoId = elegidaId,
                        seleccionActual = seleccionActual,
                        japones = japones,
                        esOptimo = seleccionActual == seleccionOptima,
                        onRestablecer = { seleccionActual = seleccionOptima },
                        onAbrirAlternativas = { sheetSlot = it },
                        onVerHerencia = { onVerHerencia(seleccionActual) },
                    )

                    /* Guardar la configuración actual (aunque difiera del óptimo). */
                    val totalActual = AppViewModel.calcular(modelo, seleccionActual).total ?: 0
                    Button(
                        onClick = { mostrarGuardar = true },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    ) {
                        Text(stringResource(R.string.guardar_config), fontWeight = FontWeight.Bold)
                    }

                    /* Configuraciones guardadas de esta corredora. */
                    val guardadas = arboles.filter { it.hijoId == elegidaId }
                    if (guardadas.isNotEmpty()) {
                        Text(
                            stringResource(R.string.guardadas_de, modelo.porId(elegidaId)?.displayName(japones) ?: ""),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 14.dp, bottom = 4.dp),
                        )
                        guardadas.forEach { a ->
                            TarjetaGuardada(
                                guardada = a,
                                japones = japones,
                                alTocar = {
                                    overrideCarga = a.seleccion
                                    recarga++
                                },
                                alBorrar = {
                                    onEliminarArbol(a.id)
                                    avisar(msgEliminado)
                                },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    /* Diálogo de guardado con nombre opcional. */
    if (mostrarGuardar && seleccionActual.isNotEmpty()) {
        val totalActual = AppViewModel.calcular(modelo, seleccionActual).total ?: 0
        val nombreSugerido = stringResource(
            R.string.nombre_sugerido,
            modelo.porId(elegidaId)?.displayName(japones) ?: "",
            totalActual,
        )
        AlertDialog(
            onDismissRequest = { mostrarGuardar = false },
            title = { Text(stringResource(R.string.guardar_config)) },
            text = {
                OutlinedTextField(
                    value = textoNombre,
                    onValueChange = { textoNombre = it },
                    singleLine = true,
                    placeholder = { Text(nombreSugerido) },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onGuardarArbol(elegidaId, textoNombre.ifBlank { nombreSugerido }, seleccionActual, totalActual)
                    mostrarGuardar = false
                    textoNombre = ""
                    avisar(msgGuardado)
                }) { Text(stringResource(R.string.guardar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarGuardar = false }) { Text(stringResource(R.string.cancelar)) }
            },
        )
    }

    /* Sheet de alternativas para el slot tocado. */
    sheetSlot?.let { slot ->
        ModalBottomSheet(onDismissRequest = { sheetSlot = null }) {
            HojaAlternativas(
                modelo = modelo,
                seleccion = seleccionActual,
                slot = slot,
                japones = japones,
                onElegir = { nuevoId ->
                    seleccionActual = seleccionActual.toMutableList().also { it[slot] = nuevoId }
                    sheetSlot = null
                },
            )
        }
    }
}

@Composable
private fun FilaSugerencia(c: Character, japones: Boolean, onClick: () -> Unit) {
    val nombrePrincipal = c.displayName(japones)
    val nombreSecundario = if (japones) c.enName ?: "" else c.jpName ?: ""
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
}

@Composable
private fun PanelMejorLinaje(
    modelo: AffinityModel,
    nombreHijoId: Int,
    seleccionActual: List<Int?>,
    japones: Boolean,
    esOptimo: Boolean,
    onRestablecer: () -> Unit,
    onAbrirAlternativas: (Int) -> Unit,
    onVerHerencia: () -> Unit,
) {
    val res: ResultadoCompat = remember(seleccionActual) {
        AppViewModel.calcular(modelo, seleccionActual)
    }
    val nombreHijo = modelo.porId(nombreHijoId)?.displayName(japones) ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(nombreHijoId, nombreHijo, modifier = Modifier.size(48.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.mejor_de, nombreHijo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                com.maximillionsnyder.umafinidad.ui.componentes.RankPill(
                    modelo.rangoTotal(res.total ?: 0),
                    res.total ?: 0,
                    grande = true,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            /* Aptitudes de la corredora elegida (carta base). */
            modelo.aptitudesDe(nombreHijoId)?.let { AptitudesDetalle(it) }

            /* Hijo: fijo (define la búsqueda). */
            ChipRol(
                etiqueta = stringResource(R.string.rol_hijo),
                personaje = modelo.porId(seleccionActual[0]!!),
                slot = 0,
                japones = japones,
                onClick = null,
            )
            /* Los otros seis: tocar abre las alternativas del slot. El color
               marca la genealogía (rama del padre 1 azul, rama del padre 2 verde). */
            for (slot in 1..6) {
                ChipRol(
                    etiqueta = etiquetaRolDe(slot),
                    personaje = seleccionActual[slot]?.let { modelo.porId(it) },
                    slot = slot,
                    japones = japones,
                    onClick = { onAbrirAlternativas(slot) },
                )
            }

            Button(
                onClick = onVerHerencia,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.ver_herencia), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (!esOptimo) {
        TextButton(onClick = {
            onRestablecer()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.restablecer_optimo), color = MaterialTheme.colorScheme.primary)
        }
    }

    Spacer(Modifier.height(12.dp))

    ResultadoPanel(modelo, res, japones)
}

/* Etiquetas de rol por slot (mismas strings que Compatibilidad). */
@Composable
internal fun etiquetaRolDe(i: Int): String = stringResource(
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

@Composable
private fun ChipRol(etiqueta: String, personaje: Character?, slot: Int, japones: Boolean, onClick: (() -> Unit)?) {
    val colorRol = com.maximillionsnyder.umafinidad.ui.theme.colorDeGenealogia(slot)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (personaje != null && onClick != null) BorderStroke(1.dp, colorRol.copy(alpha = 0.45f)) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            personaje?.let { Avatar(it.charId, it.displayName(japones), modifier = Modifier.size(28.dp)) }
            Column(modifier = Modifier.weight(1f)) {
                Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = colorRol, fontWeight = FontWeight.Bold)
                Text(
                    personaje?.displayName(japones) ?: "—",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onClick != null) {
                Text("⌄", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/* Hoja de alternativas: ordenadas por total resultante; muestra los puntos
   directos grandes y el total pequeño debajo de cada candidato. */
@Composable
private fun HojaAlternativas(
    modelo: AffinityModel,
    seleccion: List<Int?>,
    slot: Int,
    japones: Boolean,
    onElegir: (Int) -> Unit,
) {
    val ocupanteEtiqueta = etiquetaRolDe(slot)
    val alternativas = remember(seleccion, slot) {
        modelo.alternativasParaSlot(seleccion, slot, limite = 10)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            stringResource(R.string.alternativas_titulo, ocupanteEtiqueta),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )

        if (alternativas.isEmpty()) {
            Text(
                stringResource(R.string.sin_alternativas),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().height(420.dp)) {
            itemsIndexedAlt(alternativas) { alt, esUltima ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onElegir(alt.personaje.charId) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Avatar(alt.personaje.charId, alt.personaje.displayName(japones), modifier = Modifier.size(36.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            alt.personaje.displayName(japones),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "Total ${alt.total}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colorClase(claseDePuntos(alt.total)),
                        )
                    }
                    Text(
                        "${alt.puntosDirectos}pt",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = colorClase(claseDePuntos(alt.puntosDirectos)),
                    )
                }
                if (!esUltima) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

/* Mismos umbrales que GruposScreen (par): ◎ ≥20, ○ ≥10, △ ≥4. */
private fun claseDePuntos(puntos: Int): String? = when {
    puntos >= 20 -> "rank-great"
    puntos >= 10 -> "rank-good"
    puntos >= 4 -> "rank-fair"
    else -> null
}

@Composable
private fun colorClase(clase: String?): Color = when (clase) {
    "rank-great" -> com.maximillionsnyder.umafinidad.ui.theme.RankGreat
    "rank-good" -> com.maximillionsnyder.umafinidad.ui.theme.RankGood
    "rank-fair" -> com.maximillionsnyder.umafinidad.ui.theme.RankFair
    else -> MaterialTheme.colorScheme.onSurface
}

/* Helper local para no importar itemsIndexed dos veces con alias. */
private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexedAlt(
    lista: List<AlternativaSlot>,
    contenido: @Composable androidx.compose.foundation.lazy.LazyItemScope.(AlternativaSlot, Boolean) -> Unit,
) {
    items(lista.size) { i ->
        contenido(lista[i], i == lista.lastIndex)
    }
}

@Composable
private fun Nota(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
    )
}

@Composable
private fun TarjetaGuardada(guardada: ArbolGuardado, japones: Boolean, alTocar: () -> Unit, alBorrar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = alTocar).padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(guardada.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "◎ ${guardada.total} · " + SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(guardada.creadoEn)),
                    style = MaterialTheme.typography.labelSmall,
                    color = com.maximillionsnyder.umafinidad.ui.theme.RankGood,
                )
            }
            IconButton(onClick = alBorrar) {
                Icon(painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.cancelar))
            }
        }
    }
}
