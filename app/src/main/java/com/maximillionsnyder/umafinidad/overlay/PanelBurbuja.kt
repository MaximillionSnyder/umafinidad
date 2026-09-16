package com.maximillionsnyder.umafinidad.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import com.maximillionsnyder.umafinidad.ui.Destino

/* Franja lateral del panel de acceso rápido. El servicio la hospeda en su
   propia ventana de overlay, ya recortada al tamaño de la franja: sin velo
   y dejando pasar los toques de afuera a la app de fondo. */
@Composable
fun PanelBurbuja(
    modelo: AffinityModel?,
    japones: Boolean,
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

    Card(
        modifier = modifier.fillMaxSize(),
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

            /* ---- Calculadora rápida: hijo + dos padres, en columna ---- */
            Text(
                stringResource(R.string.burbuja_calc_titulo),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until SLOTS_TRIO) {
                    SlotTrio(
                        etiqueta = stringResource(
                            if (i == 0) R.string.rol_corto_hijo else R.string.rol_corto_padre,
                        ),
                        personaje = estado.ids[i]?.let { modelo?.porId(it) },
                        japones = japones,
                        onClick = { estado = estado.quitar(i) },
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
