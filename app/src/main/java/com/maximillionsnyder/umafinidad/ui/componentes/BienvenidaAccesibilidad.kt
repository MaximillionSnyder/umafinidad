package com.maximillionsnyder.umafinidad.ui.componentes

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.TamanoTexto
import com.maximillionsnyder.umafinidad.data.ThemeMode
import com.maximillionsnyder.umafinidad.data.tamanoSegunFontScale

@Composable
fun BienvenidaAccesibilidad(
    tema: ThemeMode,
    onTema: (ThemeMode) -> Unit,
    tamanoTexto: TamanoTexto,
    onTamanoTexto: (TamanoTexto) -> Unit,
    textoNegrita: Boolean,
    onTextoNegrita: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onOmitir: () -> Unit,
) {
    val context = LocalContext.current
    val fontScale = LocalConfiguration.current.fontScale
    val toqueExploracion = remember(context) {
        (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager)
            ?.isTouchExplorationEnabled == true
    }
    val altoContrasteSistema = remember(context) {
        try {
            Settings.Secure.getInt(context.contentResolver, CLAVE_ALTO_CONTRASTE_TEXTO, 0) == 1
        } catch (_: Exception) {
            false
        }
    }

    val detectado = tamanoSegunFontScale(fontScale)
    val temaInicial = remember { tema }
    var tamanoSel by remember {
        mutableStateOf(if (detectado.escala > tamanoTexto.escala) detectado else tamanoTexto)
    }
    var negritaSel by remember { mutableStateOf(textoNegrita || toqueExploracion) }
    var temaSel by remember {
        mutableStateOf(if (altoContrasteSistema && tema != ThemeMode.ALTO_CONTRASTE) ThemeMode.ALTO_CONTRASTE else tema)
    }

    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.bienvenida_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.bienvenida_desc), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.bienvenida_tamano),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                TamanoTexto.entries.forEach { opcion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = tamanoSel == opcion,
                                role = Role.RadioButton,
                                onClick = {
                                    tamanoSel = opcion
                                    onTamanoTexto(opcion)
                                },
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RadioButton(selected = tamanoSel == opcion, onClick = null)
                        Text(stringResource(etiquetaTamano(opcion)))
                    }
                }
                FilaToggleBienvenida(
                    titulo = stringResource(R.string.negrita_titulo),
                    descripcion = stringResource(R.string.negrita_desc),
                    activado = negritaSel,
                    onCambio = {
                        negritaSel = it
                        onTextoNegrita(it)
                    },
                )
                FilaToggleBienvenida(
                    titulo = stringResource(R.string.tema_contraste),
                    descripcion = stringResource(R.string.tema_contraste_desc),
                    activado = temaSel == ThemeMode.ALTO_CONTRASTE,
                    onCambio = { activo ->
                        temaSel = if (activo) ThemeMode.ALTO_CONTRASTE else temaInicial
                        onTema(temaSel)
                    },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onTamanoTexto(tamanoSel)
                onTextoNegrita(negritaSel)
                onTema(temaSel)
                onGuardar()
            }) {
                Text(stringResource(R.string.guardar))
            }
        },
        dismissButton = {
            TextButton(onClick = onOmitir) {
                Text(stringResource(R.string.omitir))
            }
        },
    )
}

@Composable
private fun FilaToggleBienvenida(titulo: String, descripcion: String, activado: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = activado, role = Role.Switch, onValueChange = onCambio)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = activado, onCheckedChange = null)
    }
}

private fun etiquetaTamano(tamano: TamanoTexto): Int = when (tamano) {
    TamanoTexto.NORMAL -> R.string.tamano_normal
    TamanoTexto.GRANDE -> R.string.tamano_grande
    TamanoTexto.MUY_GRANDE -> R.string.tamano_muy_grande
}

private const val CLAVE_ALTO_CONTRASTE_TEXTO = "high_text_contrast_enabled"
