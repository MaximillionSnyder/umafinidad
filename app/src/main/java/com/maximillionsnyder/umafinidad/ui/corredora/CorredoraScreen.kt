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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Linaje
import com.maximillionsnyder.umafinidad.domain.Rol
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import com.maximillionsnyder.umafinidad.ui.AppViewModel
import com.maximillionsnyder.umafinidad.ui.ResultadoCompat
import com.maximillionsnyder.umafinidad.ui.compat.ResultadoPanel
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar

/* Pestaña "Mi corredora": buscás una corredora y ves SU mejor linaje
   exacto (barrido exhaustivo de pares de padres en Dispatchers.Default). */
@Composable
fun CorredoraScreen(
    modelo: AffinityModel,
    japones: Boolean,
    onVerHerencia: (Linaje) -> Unit,
) {
    var filtro by rememberSaveable { mutableStateOf("") }
    var elegidaId by rememberSaveable { mutableIntStateOf(-1) }
    var linaje by remember { mutableStateOf<Linaje?>(null) }
    var calculando by remember { mutableStateOf(false) }

    val sugerencias = remember(filtro, modelo) {
        if (filtro.trim().length >= 2) rankearSugerencias(modelo.personajes, filtro)
        else emptyList()
    }

    LaunchedEffect(elegidaId) {
        if (elegidaId <= 0) {
            linaje = null
            return@LaunchedEffect
        }
        calculando = true
        linaje = withContext(Dispatchers.Default) { modelo.mejorLinajeDe(elegidaId) }
        calculando = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
    ) {
        Text(
            stringResource(R.string.tab_corredora),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 8.dp),
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
            linaje == null -> {
                Nota(stringResource(R.string.corredora_invalida))
            }
            else -> {
                PanelMejorLinaje(modelo, linaje!!, japones, onVerHerencia)
            }
        }

        Spacer(Modifier.height(24.dp))
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
private fun PanelMejorLinaje(modelo: AffinityModel, l: Linaje, japones: Boolean, onVerHerencia: (Linaje) -> Unit) {
    val nombreHijo = l.hijo.displayName(japones)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(l.hijo.charId, nombreHijo, modifier = Modifier.size(48.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.mejor_de, nombreHijo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                com.maximillionsnyder.umafinidad.ui.componentes.RankPill(
                    modelo.rangoTotal(l.puntos),
                    l.puntos,
                    grande = true,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            ChipRol(stringResource(R.string.rol_hijo), nombreHijo, l.hijo.charId, Rol.HIJO, japones)
            ChipRol(stringResource(R.string.rol_padre1), l.padre.displayName(japones), l.padre.charId, Rol.PADRE, japones)
            ChipRol(stringResource(R.string.rol_padre2), l.madre.displayName(japones), l.madre.charId, Rol.PADRE, japones)

            val etiquetasAbuelos = listOf(
                stringResource(R.string.rol_abuelo1_p1),
                stringResource(R.string.rol_abuelo2_p1),
                stringResource(R.string.rol_abuelo1_p2),
                stringResource(R.string.rol_abuelo2_p2),
            )
            val abuelos = l.abuelos.flatMap { it }
            abuelos.forEachIndexed { i, abuelo ->
                ChipRol(etiquetasAbuelos[i], abuelo.displayName(japones), abuelo.charId, Rol.ABUELO, japones)
            }

            Button(
                onClick = { onVerHerencia(l) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.ver_herencia), fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    /* Desglose de vínculos reutilizando el cálculo del panel de resultado. */
    val res: ResultadoCompat = remember(l) {
        AppViewModel.calcular(
            modelo,
            listOf(
                l.hijo.charId,
                l.padre.charId,
                l.madre.charId,
                l.abuelos[0][0].charId,
                l.abuelos[0][1].charId,
                l.abuelos[1][0].charId,
                l.abuelos[1][1].charId,
            ),
        )
    }
    ResultadoPanel(modelo, res, japones)
}

@Composable
private fun ChipRol(etiqueta: String, nombre: String, charId: Int, rol: Rol, japones: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Avatar(charId, nombre, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = colorDeRolLocal(rol), fontWeight = FontWeight.Bold)
                Text(nombre, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun colorDeRolLocal(rol: Rol): androidx.compose.ui.graphics.Color = when (rol) {
    Rol.HIJO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
    Rol.PADRE -> androidx.compose.ui.graphics.Color(0xFF82AADD)
    Rol.ABUELO -> androidx.compose.ui.graphics.Color(0xFFAEB4C2)
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
