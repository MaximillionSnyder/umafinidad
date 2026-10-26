package com.maximillionsnyder.umafinidad.ui.corredora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.ui.AppViewModel
import com.maximillionsnyder.umafinidad.ui.FilaVinculoUi
import com.maximillionsnyder.umafinidad.ui.ResultadoCompat
import com.maximillionsnyder.umafinidad.ui.componentes.RankPill
import com.maximillionsnyder.umafinidad.ui.componentes.headingSemantica
import com.maximillionsnyder.umafinidad.ui.theme.RankFair
import com.maximillionsnyder.umafinidad.ui.theme.RankGreat
import com.maximillionsnyder.umafinidad.ui.theme.VinculoEntrePadres
import com.maximillionsnyder.umafinidad.ui.theme.VinculoHijoPadres
import com.maximillionsnyder.umafinidad.ui.theme.VinculoTrios
import com.maximillionsnyder.umafinidad.ui.theme.colorDeGenealogia

/* Panel "Más info" de Mi corredora: complementa la pantalla con el desglose
   del total por vínculos, la explicación del reparto de las filas y las
   mejores alternativas por slot. Es SOLO lectura: no altera la selección ni
   ningún cálculo. */

private data class CambioSlot(val slot: Int, val personaje: Character, val total: Int)

/* Mejor alternativa de cada slot editable, con el total resultante. Se
   calcula solo con el árbol completo (las alternativas necesitan ocupante). */
private fun mejoresCambios(modelo: AffinityModel, seleccion: List<Int?>): List<CambioSlot> {
    if (seleccion.size != SLOTS || seleccion.any { it == null }) return emptyList()
    return (1 until SLOTS).mapNotNull { slot ->
        modelo.alternativasParaSlot(seleccion, slot, limite = 1).firstOrNull()?.let { alt ->
            CambioSlot(slot, alt.personaje, alt.total)
        }
    }
}

@Composable
internal fun HojaMasInfo(
    modelo: AffinityModel,
    seleccion: List<Int?>,
    japones: Boolean,
    onCerrar: () -> Unit,
) {
    val res: ResultadoCompat = remember(seleccion, modelo) {
        AppViewModel.calcular(modelo, seleccion)
    }
    val total = res.total ?: 0
    val subtotalHijoPadres = res.hijoPadres.sumOf { it.puntos }
    val subtotalEntrePadres = res.entrePadres?.puntos ?: 0
    val subtotalTrios = res.hijoPadreAbuelos.sumOf { it.puntos }
    val cambios = remember(seleccion, modelo) { mejoresCambios(modelo, seleccion) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.afinidad_info_titulo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.headingSemantica(),
        )
        Text(
            stringResource(R.string.afinidad_info_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!res.vacio) {
            BarraComposicion(subtotalHijoPadres, subtotalEntrePadres, subtotalTrios)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LeyendaVinculo(
                    color = VinculoHijoPadres,
                    etiqueta = stringResource(R.string.sec_hijo_padres),
                    subtotal = subtotalHijoPadres,
                )
                LeyendaVinculo(
                    color = VinculoEntrePadres,
                    etiqueta = stringResource(R.string.sec_entre_padres),
                    subtotal = subtotalEntrePadres,
                )
                LeyendaVinculo(
                    color = VinculoTrios,
                    etiqueta = stringResource(R.string.sec_hijo_padres_abuelos),
                    subtotal = subtotalTrios,
                )
            }

            GrupoVinculos(
                modelo = modelo,
                japones = japones,
                titulo = stringResource(R.string.sec_hijo_padres),
                color = VinculoHijoPadres,
                subtotal = subtotalHijoPadres,
                filas = res.hijoPadres,
            )
            GrupoVinculos(
                modelo = modelo,
                japones = japones,
                titulo = stringResource(R.string.sec_entre_padres),
                color = VinculoEntrePadres,
                subtotal = subtotalEntrePadres,
                filas = listOfNotNull(res.entrePadres),
            )
            GrupoVinculos(
                modelo = modelo,
                japones = japones,
                titulo = stringResource(R.string.sec_hijo_padres_abuelos),
                color = VinculoTrios,
                subtotal = subtotalTrios,
                filas = res.hijoPadreAbuelos,
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "$subtotalHijoPadres + $subtotalEntrePadres + $subtotalTrios = $total",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Text(
            stringResource(R.string.afinidad_info_filas),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.afinidad_info_medio_punto),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (cambios.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                stringResource(R.string.afinidad_info_cambios),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.afinidad_info_cambios_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            cambios.forEach { cambio ->
                FilaCambio(cambio = cambio, japones = japones, totalActual = total)
            }
        }

        TextButton(onClick = onCerrar, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cerrar))
        }
    }
}

/* Barra apilada con la proporción de los tres tipos de vínculo. */
@Composable
private fun BarraComposicion(hijoPadres: Int, entrePadres: Int, trios: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp)),
    ) {
        if (hijoPadres > 0) {
            Box(Modifier.weight(hijoPadres.toFloat()).fillMaxHeight().background(VinculoHijoPadres))
        }
        if (entrePadres > 0) {
            Box(Modifier.weight(entrePadres.toFloat()).fillMaxHeight().background(VinculoEntrePadres))
        }
        if (trios > 0) {
            Box(Modifier.weight(trios.toFloat()).fillMaxHeight().background(VinculoTrios))
        }
    }
}

@Composable
private fun LeyendaVinculo(color: Color, etiqueta: String, subtotal: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
        Text(
            "$subtotal pt",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun GrupoVinculos(
    modelo: AffinityModel,
    japones: Boolean,
    titulo: String,
    color: Color,
    subtotal: Int,
    filas: List<FilaVinculoUi>,
) {
    if (filas.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier
                    .weight(1f)
                    .headingSemantica(),
            )
            Text(
                "$subtotal pt",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        filas.forEach { fila -> FilaInfo(modelo = modelo, japones = japones, fila = fila) }
    }
}

@Composable
private fun FilaInfo(modelo: AffinityModel, japones: Boolean, fila: FilaVinculoUi) {
    val nombres = fila.ids
        .map { id -> modelo.porId(id)?.displayName(japones) ?: id.toString() }
        .joinToString(" × ")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                nombres,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            RankPill(fila.rango, fila.puntos)
        }
        if (fila.esCorredora) {
            Text(
                stringResource(R.string.corredora_nota),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilaCambio(cambio: CambioSlot, japones: Boolean, totalActual: Int) {
    val delta = cambio.total - totalActual
    val colorDelta = when {
        delta > 0 -> RankGreat
        delta < 0 -> RankFair
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                etiquetaRolDe(cambio.slot),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colorDeGenealogia(cambio.slot),
            )
            Text(
                cambio.personaje.displayName(japones),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            "Total ${cambio.total}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(shape = RoundedCornerShape(999.dp), color = colorDelta.copy(alpha = 0.16f)) {
            Text(
                if (delta > 0) "+$delta" else "$delta",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colorDelta,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            )
        }
    }
}
