package com.maximillionsnyder.umafinidad.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
    ladoDerecho: Boolean,
    translucido: Boolean,
    onFiltro: (String) -> Unit,
    onAlternar: (Int) -> Unit,
    onQuitarSlot: (Int) -> Unit,
    onLimpiar: () -> Unit,
    onAutocompletar: () -> Unit,
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
    Card(
        modifier = modifier.fillMaxSize(),
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.burbuja_panel_titulo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() },
                )
                IconButton(onClick = onCerrar) {
                    Icon(
                        painterResource(R.drawable.ic_cerrar),
                        contentDescription = stringResource(R.string.cerrar),
                    )
                }
            }

            /* ---- Acciones rápidas y afinidad total ---- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                Spacer(Modifier.weight(1f))
                if (rango != null && total != null) {
                    TotalCompacto(rango, total)
                }
            }
            if (autocompletando) {
                BarraProgreso()
            }

            /* ---- Genealogía completa: hijo, dos padres y abuelos ---- */
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until SLOTS) {
                    SlotGenealogia(
                        etiqueta = stringResource(etiquetaDeSlot(i)),
                        personaje = personajes[i],
                        japones = japones,
                        onClick = { onQuitarSlot(i) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
                CarruselSugerencias(sugerencias, japones, ladoDerecho, onAlternar)
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

            TextButton(onClick = onOcultar, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.burbuja_ocultar))
            }
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
    ladoDerecho: Boolean,
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
    val degradado = remember(fondo, ladoDerecho) {
        Brush.horizontalGradient(
            if (ladoDerecho) {
                listOf(fondo, fondo.copy(alpha = 0f))
            } else {
                listOf(fondo.copy(alpha = 0f), fondo)
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
            Box(
                modifier = Modifier
                    .align(if (ladoDerecho) Alignment.CenterEnd else Alignment.CenterStart)
                    .size(width = 18.dp, height = FICHA_OPCION_DP.dp)
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
    3, 4 -> R.string.burbuja_abuelo_p1
    else -> R.string.burbuja_abuelo_p2
}
