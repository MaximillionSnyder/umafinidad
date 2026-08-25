package com.maximillionsnyder.umafinidad.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.BuildConfig
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.ModoGrilla

/* Apartado de ajustes: por ahora, el modo de grilla de personajes. */
@Composable
fun SettingsScreen(modoGrilla: ModoGrilla, onModoGrilla: (ModoGrilla) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.ajustes_apariencia),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Text(
            stringResource(R.string.modo_grilla_pregunta),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OpcionGrilla(
            titulo = stringResource(R.string.modo_vertical),
            descripcion = stringResource(R.string.modo_vertical_desc),
            seleccionado = modoGrilla == ModoGrilla.TARJETAS,
            onClick = { onModoGrilla(ModoGrilla.TARJETAS) },
        )
        OpcionGrilla(
            titulo = stringResource(R.string.modo_lista),
            descripcion = stringResource(R.string.modo_lista_desc),
            seleccionado = modoGrilla == ModoGrilla.LISTA,
            onClick = { onModoGrilla(ModoGrilla.LISTA) },
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            "Uma Afinidad v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OpcionGrilla(titulo: String, descripcion: String, seleccionado: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = if (seleccionado) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = seleccionado, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
