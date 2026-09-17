package com.maximillionsnyder.umafinidad.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import com.maximillionsnyder.umafinidad.ui.Destino

/* Franja lateral del panel de acceso rápido. El servicio la hospeda en su
   propia ventana de overlay, ya recortada al tamaño de la franja: sin velo
   y dejando pasar los toques de afuera a la app de fondo. */
@Composable
fun PanelBurbuja(
    modelo: AffinityModel?,
    japones: Boolean,
    seleccion: List<Int?>,
    autocompletando: Boolean,
    onSeleccion: (List<Int?>) -> Unit,
    onAutocompletar: () -> Unit,
    onCerrar: () -> Unit,
    onOcultar: () -> Unit,
    onAbrirDestino: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filtro by remember { mutableStateOf("") }
    /* Aviso de regla o selección completa al intentar elegir una sugerencia. */
    var aviso by remember { mutableStateOf<Int?>(null) }

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
        val colocacion = alternar(seleccion, id)
        if (colocacion.resultado == ColocacionResultado.COLOCADO ||
            colocacion.resultado == ColocacionResultado.QUITADO
        ) {
            onSeleccion(colocacion.seleccion)
            filtro = ""
            aviso = null
        } else {
            aviso = if (colocacion.resultado == ColocacionResultado.COMPLETA) {
                R.string.seleccion_completa
            } else {
                R.string.regla_slots
            }
        }
    }

    val total = remember(modelo, seleccion) { modelo?.let { totalDe(it, seleccion) } }

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
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
                    modifier = Modifier.weight(1f),
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
                    onClick = { onSeleccion(seleccionVacia) },
                )
                BotonCompacto(
                    iconoRes = R.drawable.ic_autocompletar,
                    descripcionRes = R.string.burbuja_autocompletar,
                    enabled = !autocompletando &&
                        seleccion[0] != null &&
                        seleccion.any { it == null },
                    onClick = onAutocompletar,
                )
                Spacer(Modifier.weight(1f))
                if (modelo != null && total != null) {
                    TotalCompacto(modelo.rangoTotal(total), total)
                }
            }

            /* ---- Genealogía completa: hijo, dos padres y abuelos ---- */
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until SLOTS) {
                    SlotGenealogia(
                        etiqueta = stringResource(etiquetaDeSlot(i)),
                        personaje = seleccion[i]?.let { modelo?.porId(it) },
                        japones = japones,
                        onClick = { onSeleccion(quitar(seleccion, i)) },
                        modifier = Modifier.fillMaxWidth(),
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

            aviso?.let { texto ->
                Text(
                    stringResource(texto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

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

private fun etiquetaDeSlot(slot: Int): Int = when (slot) {
    0 -> R.string.rol_hijo
    1 -> R.string.rol_padre1
    2 -> R.string.rol_padre2
    3, 4 -> R.string.burbuja_abuelo_p1
    else -> R.string.burbuja_abuelo_p2
}
