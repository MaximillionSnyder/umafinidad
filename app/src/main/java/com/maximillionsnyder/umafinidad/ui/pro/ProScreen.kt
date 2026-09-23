package com.maximillionsnyder.umafinidad.ui.pro

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.ResultadoActivacion
import com.maximillionsnyder.umafinidad.ui.componentes.ClavesTransicion
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBarConVolver
import com.maximillionsnyder.umafinidad.ui.componentes.compartidoBounds
import com.maximillionsnyder.umafinidad.ui.componentes.compartidoElemento
import com.maximillionsnyder.umafinidad.ui.componentes.headingSemantica
import java.text.DateFormat
import java.util.Date

/* Dónde se consiguen los códigos: el repo explica las opciones y el
   generador (`scripts/generar-codigos-pro.mjs`) emite las licencias. */
private const val URL_PRO = "https://github.com/MaximillionSnyder/umafinidad#uma-afinidad-pro"

/* Pantalla Pro: estado de la licencia, qué desbloquea y el campo para
   activarla. Se abre desde Ajustes, desde los candados de la app y desde el
   panel de la burbuja. */
@Composable
fun ProScreen(
    esPro: Boolean,
    codigo: String?,
    activadoEn: Long,
    onActivar: (String) -> ResultadoActivacion,
    onDesactivar: () -> Unit,
    onVolver: () -> Unit,
    claveOverlay: String = ClavesTransicion.OVERLAY_PRO,
) {
    val context = LocalContext.current
    var entrada by rememberSaveable { mutableStateOf("") }
    var resultado by rememberSaveable { mutableStateOf<ResultadoActivacion?>(null) }
    var confirmarDesactivar by rememberSaveable { mutableStateOf(false) }
    var verComo by rememberSaveable { mutableStateOf(false) }

    val fecha = remember(activadoEn) {
        if (activadoEn <= 0L) null
        else DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(activadoEn))
    }

    Surface(
        modifier = Modifier.fillMaxSize().compartidoBounds(claveOverlay),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            HeaderBarConVolver(
                titulo = stringResource(R.string.pro_titulo),
                onVolver = onVolver,
                iconoRes = R.drawable.ic_pro,
                pillTexto = if (esPro) stringResource(R.string.pro_badge) else null,
                modifierTitulo = Modifier.compartidoBounds(ClavesTransicion.titulo(claveOverlay)),
                modifierIcono = Modifier.compartidoElemento(ClavesTransicion.icono(claveOverlay)),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                /* ---- Estado de la licencia ---- */
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esPro) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            painterResource(if (esPro) R.drawable.ic_pro else R.drawable.ic_candado),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(
                                    if (esPro) R.string.pro_estado_activo else R.string.pro_estado_gratis,
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            if (esPro) {
                                codigo?.let {
                                    Text(
                                        stringResource(R.string.pro_codigo_actual, it),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                fecha?.let {
                                    Text(
                                        stringResource(R.string.pro_activado_el, it),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                Text(
                                    stringResource(R.string.pro_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                /* ---- Qué desbloquea ---- */
                Text(
                    stringResource(R.string.pro_funciones_titulo),
                    modifier = Modifier.headingSemantica(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                FuncionProCard(
                    iconoRes = R.drawable.ic_pro,
                    tituloRes = R.string.pro_func_burbuja,
                    descripcionRes = R.string.pro_func_burbuja_desc,
                    incluida = esPro,
                )
                FuncionProCard(
                    iconoRes = R.drawable.ic_tab_ranking,
                    tituloRes = R.string.pro_func_padres,
                    descripcionRes = R.string.pro_func_padres_desc,
                    incluida = esPro,
                )
                FuncionProCard(
                    iconoRes = R.drawable.ic_tab_top,
                    tituloRes = R.string.pro_func_arboles,
                    descripcionRes = R.string.pro_func_arboles_desc,
                    incluida = esPro,
                )

                /* ---- Activar ---- */
                if (!esPro) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                stringResource(R.string.pro_activar_titulo),
                                modifier = Modifier.headingSemantica(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                stringResource(R.string.pro_activar_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            OutlinedTextField(
                                value = entrada,
                                onValueChange = {
                                    entrada = it
                                    resultado = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                label = { Text(stringResource(R.string.pro_codigo_etiqueta)) },
                                placeholder = { Text(stringResource(R.string.pro_codigo_ejemplo)) },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Done,
                                ),
                            )
                            resultado?.let { r ->
                                val texto = when (r) {
                                    ResultadoActivacion.CODIGO_INVALIDO -> R.string.pro_codigo_invalido
                                    ResultadoActivacion.YA_ACTIVO -> R.string.pro_ya_activo
                                    ResultadoActivacion.ACTIVADO -> R.string.pro_activado_ok
                                }
                                Text(
                                    stringResource(texto),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (r == ResultadoActivacion.CODIGO_INVALIDO) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                )
                            }
                            Button(
                                onClick = { resultado = onActivar(entrada) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = entrada.isNotBlank(),
                            ) {
                                Text(stringResource(R.string.pro_activar), fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = { verComo = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.pro_como_obtener))
                            }
                        }
                    }
                } else {
                    TextButton(
                        onClick = { confirmarDesactivar = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.pro_desactivar))
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (verComo) {
        AlertDialog(
            onDismissRequest = { verComo = false },
            title = { Text(stringResource(R.string.pro_como_obtener)) },
            text = { Text(stringResource(R.string.pro_como_obtener_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    verComo = false
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(URL_PRO)))
                    }
                }) {
                    Text(stringResource(R.string.pro_abrir_repo))
                }
            },
            dismissButton = {
                TextButton(onClick = { verComo = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
        )
    }

    if (confirmarDesactivar) {
        AlertDialog(
            onDismissRequest = { confirmarDesactivar = false },
            title = { Text(stringResource(R.string.pro_desactivar_titulo)) },
            text = { Text(stringResource(R.string.pro_desactivar_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarDesactivar = false
                    resultado = null
                    onDesactivar()
                }) {
                    Text(stringResource(R.string.pro_desactivar))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarDesactivar = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            },
        )
    }
}

/* Fila de una función Pro: con licencia activa muestra el tilde y, sin ella,
   la marca de bloqueada. */
@Composable
private fun FuncionProCard(
    iconoRes: Int,
    tituloRes: Int,
    descripcionRes: Int,
    incluida: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                painterResource(iconoRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(tituloRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(descripcionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                painterResource(if (incluida) R.drawable.ic_check else R.drawable.ic_candado),
                contentDescription = stringResource(
                    if (incluida) R.string.pro_incluida else R.string.pro_bloqueada,
                ),
                tint = if (incluida) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
