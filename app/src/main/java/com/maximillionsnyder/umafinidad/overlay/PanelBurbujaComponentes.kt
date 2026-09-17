package com.maximillionsnyder.umafinidad.overlay

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.Character
import com.maximillionsnyder.umafinidad.domain.Rango
import com.maximillionsnyder.umafinidad.ui.componentes.Avatar
import com.maximillionsnyder.umafinidad.ui.theme.LocalColoresRango
import com.maximillionsnyder.umafinidad.ui.theme.fondoDeRango

/* Piezas del panel de acceso rápido de la burbuja flotante. */

/* Botón chico de acción (limpiar / autocompletar) para la cabecera. */
@Composable
internal fun BotonCompacto(
    iconoRes: Int,
    descripcionRes: Int,
    enabled: Boolean,
    tonal: Boolean = true,
    onClick: () -> Unit,
) {
    val modifier = Modifier.size(36.dp)
    val contenido: @Composable () -> Unit = {
        Icon(
            painterResource(iconoRes),
            contentDescription = stringResource(descripcionRes),
            modifier = Modifier.size(20.dp),
        )
    }
    if (tonal) {
        FilledTonalIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = contenido,
        )
    } else {
        OutlinedIconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = contenido,
        )
    }
}

/* Slot de la genealogía en la franja: avatar (o "+") con nombre y rol. */
@Composable
internal fun SlotGenealogia(
    etiqueta: String,
    personaje: Character?,
    japones: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(
            enabled = personaje != null,
            role = Role.Button,
            onClickLabel = stringResource(R.string.quitar_personaje),
            onClick = onClick,
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (personaje != null) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        border = if (personaje != null) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (personaje != null) {
                Avatar(personaje.charId, personaje.displayName(japones), modifier = Modifier.size(32.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "＋",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                if (personaje != null) {
                    Text(
                        personaje.displayName(japones),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
internal fun FilaSugerencia(c: Character, japones: Boolean, onClick: () -> Unit) {
    val nombrePrincipal = c.displayName(japones)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Avatar(c.charId, nombrePrincipal, modifier = Modifier.size(32.dp))
        Text(
            nombrePrincipal,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/* Total de afinidad compacto "◎156", del mismo alto que los botones. */
@Composable
internal fun TotalCompacto(rango: Rango, puntos: Int, modifier: Modifier = Modifier) {
    val colores = LocalColoresRango.current
    val frente = when (rango.clase) {
        "rank-great" -> colores.great
        "rank-good" -> colores.good
        "rank-fair" -> colores.fair
        else -> MaterialTheme.colorScheme.onSurface
    }
    val fondo = fondoDeRango(rango.clase) ?: MaterialTheme.colorScheme.surfaceVariant
    val descripcion = stringResource(R.string.burbuja_total)

    Box(
        modifier = modifier
            .height(36.dp)
            .background(fondo, RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp)
            .semantics { contentDescription = "$descripcion $puntos" },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "(${rango.simbolo})$puntos",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = frente,
        )
    }
}

@Composable
internal fun BotonDestino(textoRes: Int, iconoRes: Int, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(painterResource(iconoRes), contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(textoRes),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
