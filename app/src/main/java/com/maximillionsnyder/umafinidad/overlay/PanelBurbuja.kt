package com.maximillionsnyder.umafinidad.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.ui.Destino

/* Franja lateral del panel de acceso rápido. El servicio la hospeda en su
   propia ventana de overlay, ya recortada al tamaño de la franja: sin velo
   y dejando pasar los toques de afuera a la app de fondo.

   El composable no guarda estado propio (filtro, aviso y sugerencias llegan
   desde EstadoBurbuja): así cerrar y reabrir el panel conserva la búsqueda y
   no vuelve a rankear candidatos. */
@Composable
fun PanelBurbuja(
    modelo: AffinityModel?,
    japones: Boolean,
    seleccion: List<Int?>,
    filtro: String,
    aviso: Int?,
    sugerencias: List<Character>,
    autocompletando: Boolean,
    /* La franja puede estar en cualquier lado: la manija y el degradado del
       carrusel miran al centro según dónde la dejó el usuario. */
    panelDerecha: Boolean,
    /* Genealogía en dos columnas (hijo grande y el resto de a dos por fila,
       por rama). Se elige en Ajustes → Burbuja flotante. */
    dosColumnas: Boolean,
    translucido: Boolean,
    slotDestino: Int?,
    onFiltro: (String) -> Unit,
    onAlternar: (Int) -> Unit,
    onSlot: (Int) -> Unit,
    onLimpiar: () -> Unit,
    onAutocompletar: () -> Unit,
    onRedimensionar: (Float, Float) -> Unit,
    onFinRedimension: () -> Unit,
    onMover: (Float, Float) -> Unit,
    onFinMover: () -> Unit,
    puedeRedimensionar: Boolean,
    puedeMover: Boolean,
    onIrAPro: () -> Unit,
    onCerrar: () -> Unit,
    onOcultar: () -> Unit,
    onAbrirDestino: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    /* Cada slot se resuelve una sola vez por selección y modelo, no en cada
       recomposición del panel. */
    val personajes = remember(modelo, seleccion) {
        List(SLOTS) { i -> seleccion[i]?.let { modelo?.porId(it) } }
    }
    val total = remember(modelo, seleccion) {
        modelo?.let { totalDe(it, seleccion) }
    }
    val rango = remember(modelo, total) {
        if (modelo != null && total != null) modelo.rangoTotal(total) else null
    }
    val hayHuecos = remember(seleccion) { seleccion.any { it == null } }

    val colorPanel = if (translucido) {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = ALPHA_PANEL)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    Box(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorPanel),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (translucido) 0.dp else 1.dp,
            ),
            border = if (translucido) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            } else {
                null
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                /* ---- Cabecera ---- */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        /* La cabecera arrastra la franja entera (función Pro). */
                        .then(
                            if (puedeMover) {
                                Modifier.pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = { onFinMover() },
                                        onDragCancel = { onFinMover() },
                                    ) { cambio, delta ->
                                        cambio.consume()
                                        onMover(delta.x, delta.y)
                                    }
                                }
                            } else {
                                Modifier
                            },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (puedeMover) {
                        Icon(
                            painterResource(R.drawable.ic_mover),
                            contentDescription = stringResource(R.string.burbuja_mover),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        stringResource(R.string.burbuja_panel_titulo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { heading() },
                    )
                    /* Limpiar y autocompletar viven en la cabecera, junto a la
                       afinidad total: la franja es corta y no vale gastar una
                       fila entera en ellas. */
                    BotonCompacto(
                        iconoRes = R.drawable.ic_limpiar,
                        descripcionRes = R.string.burbuja_limpiar,
                        enabled = seleccion.any { it != null },
                        tonal = false,
                        onClick = onLimpiar,
                    )
                    BotonCompacto(
                        iconoRes = R.drawable.ic_autocompletar,
                        descripcionRes = R.string.burbuja_autocompletar,
                        enabled = !autocompletando && hayHuecos,
                        onClick = onAutocompletar,
                    )
                    if (rango != null && total != null) {
                        TotalCompacto(rango, total)
                    }
                    IconButton(onClick = onCerrar) {
                        Icon(
                            painterResource(R.drawable.ic_cerrar),
                            contentDescription = stringResource(R.string.cerrar),
                        )
                    }
                }
                if (autocompletando) {
                    BarraProgreso()
                }

                /* ---- Genealogía completa: hijo, dos padres y abuelos ---- */
                if (dosColumnas) {
                    GrillaGenealogia(personajes, japones, slotDestino, onSlot)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 0 until SLOTS) {
                            SlotGenealogia(
                                etiqueta = stringResource(etiquetaDeSlot(i)),
                                personaje = personajes[i],
                                japones = japones,
                                seleccionado = slotDestino == i,
                                onClick = { onSlot(i) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                slotDestino?.let { slot ->
                    Text(
                        stringResource(
                            R.string.burbuja_destino,
                            stringResource(etiquetaDeSlot(slot)),
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                OutlinedTextField(
                    value = filtro,
                    onValueChange = onFiltro,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(painterResource(R.drawable.ic_buscar), contentDescription = null)
                    },
                    trailingIcon = {
                        if (filtro.isNotEmpty()) {
                            IconButton(onClick = { onFiltro("") }) {
                                Icon(
                                    painterResource(R.drawable.ic_cerrar),
                                    contentDescription = stringResource(R.string.limpiar_todo),
                                )
                            }
                        }
                    },
                    placeholder = { Text(stringResource(R.string.buscar)) },
                )

                aviso?.let { texto ->
                    Text(
                        stringResource(texto),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                if (sugerencias.isNotEmpty()) {
                    CarruselSugerencias(sugerencias, japones, panelDerecha, onAlternar)
                }

                if (modelo == null || total == null) {
                    Text(
                        stringResource(R.string.burbuja_calc_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                /* ---- Atajos a la app ---- */
                Text(
                    stringResource(R.string.burbuja_abrir),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                BotonDestino(R.string.tab_compat, R.drawable.ic_tab_compat) {
                    onAbrirDestino(Destino.TAB_COMPAT)
                }
                BotonDestino(R.string.tab_corredora, R.drawable.ic_tab_corredora) {
                    onAbrirDestino(Destino.TAB_CORREDORA)
                }
                BotonDestino(R.string.tab_elenco, R.drawable.ic_tab_elenco) {
                    onAbrirDestino(Destino.TAB_ELENCO)
                }
                BotonDestino(R.string.tab_ajustes, R.drawable.ic_tab_ajustes) {
                    onAbrirDestino(Destino.TAB_AJUSTES)
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                /* Sin licencia, el atajo a Pro ocupa el lugar de la manija. */
                if (!puedeMover || !puedeRedimensionar) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_candado),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            stringResource(R.string.pro_burbuja_cta),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onIrAPro) {
                            Text(stringResource(R.string.pro_activar))
                        }
                    }
                }

                TextButton(onClick = onOcultar, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.burbuja_ocultar))
                }
            }
        }

        /* Redimensionar la franja es función Pro: sin licencia la manija no
           está y en su lugar queda el atajo a la pantalla Pro. */
        if (puedeRedimensionar) {
            ManijaRedimension(
                onRedimensionar = onRedimensionar,
                onFinRedimension = onFinRedimension,
                modifier = Modifier.align(
                    if (panelDerecha) Alignment.BottomStart else Alignment.BottomEnd,
                ),
            )
        }
    }
}

/* Manija de la esquina inferior interna: arrastrarla cambia el tamaño de la
   franja (el servicio aplica el nuevo tamaño a la ventana). */
@Composable
private fun ManijaRedimension(
    onRedimensionar: (Float, Float) -> Unit,
    onFinRedimension: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    val descripcion = stringResource(R.string.burbuja_redimensionar)
    Box(
        modifier = modifier
            .padding(6.dp)
            .size(TAMANO_MANIJA_DP.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { onFinRedimension() },
                    onDragCancel = { onFinRedimension() },
                ) { cambio, delta ->
                    cambio.consume()
                    onRedimensionar(delta.x, delta.y)
                }
            }
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val grosor = 2.dp.toPx()
            drawLine(
                color = color,
                start = Offset(size.width * 0.15f, size.height * 0.85f),
                end = Offset(size.width * 0.85f, size.height * 0.15f),
                strokeWidth = grosor,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = color,
                start = Offset(size.width * 0.45f, size.height * 0.85f),
                end = Offset(size.width * 0.85f, size.height * 0.45f),
                strokeWidth = grosor,
                cap = StrokeCap.Round,
            )
        }
    }
}

/* Carrusel de caras sugeridas: en vez de una lista vertical que come el alto
   de la franja, las caras van en fila y se deslizan a lo ancho. El degradado
   del borde que queda "tapado" avisa que hay más. */
@Composable
private fun CarruselSugerencias(
    sugerencias: List<Character>,
    japones: Boolean,
    panelDerecha: Boolean,
    onAlternar: (Int) -> Unit,
) {
    val scroll = rememberScrollState()
    val avisoOpciones = stringResource(R.string.buscar)
    val densidad = LocalDensity.current
    val hayMas by remember {
        derivedStateOf {
            scroll.value < scroll.maxValue
        }
    }
    val fondo = MaterialTheme.colorScheme.surfaceContainerHigh
    val degradado = remember(fondo, panelDerecha) {
        Brush.horizontalGradient(
            if (panelDerecha) {
                listOf(fondo.copy(alpha = 0f), fondo)
            } else {
                listOf(fondo, fondo.copy(alpha = 0f))
            },
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val anchoDisponible = with(densidad) { maxWidth.roundToPx() }
        val reparto = remember(anchoDisponible) {
            repartirOpciones(
                anchoDisponible = anchoDisponible,
                ficha = with(densidad) { FICHA_OPCION_DP.dp.roundToPx() },
                espacio = with(densidad) { ESPACIO_FICHAS_DP.dp.roundToPx() },
                maximo = MAX_FICHAS_VISIBLES,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
                .padding(vertical = 2.dp)
                .semantics { contentDescription = avisoOpciones },
            horizontalArrangement = Arrangement.spacedBy(ESPACIO_FICHAS_DP.dp),
        ) {
            sugerencias.forEach { c ->
                FichaOpcion(c, japones, ancho = reparto.anchoFicha) { onAlternar(c.charId) }
            }
        }
        if (hayMas) {
            /* El degradado acompaña el alto real de la fila, que ahora crece
               con la cara. */
            Box(
                modifier = Modifier
                    .align(if (panelDerecha) Alignment.CenterStart else Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(18.dp)
                    .background(degradado),
            )
        }
    }
}

/* Barra indeterminada mientras el autocompletado calcula: da señal de avance
   sin bloquear la franja (el botón ya queda deshabilitado). Es el componente
   de Material 3, no una copia propia. */
@Composable
private fun BarraProgreso() {
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
}

private fun etiquetaDeSlot(slot: Int): Int = when (slot) {
    0 -> R.string.rol_hijo
    1 -> R.string.rol_padre1
    2 -> R.string.rol_padre2
    /* 3 y 5 son el primer abuelo de cada rama; 4 y 6 el segundo. */
    3, 5 -> R.string.burbuja_abuelo_p1
    else -> R.string.burbuja_abuelo_p2
}

/* Genealogía en dos columnas: el hijo en una card grande y debajo las parejas
   por rama (la columna izquierda es la línea del Padre 1 y la derecha la del
   Padre 2). */
@Composable
private fun GrillaGenealogia(
    personajes: List<Character?>,
    japones: Boolean,
    slotDestino: Int?,
    onSlot: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FILAS_GENEALOGIA_POR_RAMA.forEach { fila ->
            if (fila.size == 1) {
                val slot = fila.first()
                SlotGenealogia(
                    etiqueta = stringResource(etiquetaDeSlot(slot)),
                    personaje = personajes[slot],
                    japones = japones,
                    seleccionado = slotDestino == slot,
                    onClick = { onSlot(slot) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    fila.forEach { slot ->
                        SlotGenealogia(
                            etiqueta = stringResource(etiquetaDeSlot(slot)),
                            personaje = personajes[slot],
                            japones = japones,
                            seleccionado = slotDestino == slot,
                            compacto = true,
                            onClick = { onSlot(slot) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
