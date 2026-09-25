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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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

/* Slot de la genealogía en la franja: avatar (o "+") con nombre y rol.
   Tocado, quita al ocupante o marca el hueco como destino de la próxima
   colocación. */
@Composable
internal fun SlotGenealogia(
    etiqueta: String,
    personaje: Character?,
    japones: Boolean,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nombre = personaje?.displayName(japones)
    Card(
        modifier = modifier.clickable(
            role = Role.Button,
            onClickLabel = stringResource(
                if (personaje != null) R.string.quitar_personaje else R.string.burbuja_elegir_lugar,
            ),
            onClick = onClick,
        ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                personaje != null -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                seleccionado -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                else -> MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
        border = when {
            personaje != null -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            seleccionado -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            else -> null
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (personaje != null && nombre != null) {
                Avatar(personaje.charId, nombre, modifier = Modifier.size(32.dp))
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
                if (nombre != null) {
                    Text(
                        nombre,
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

/* Lado de la cara dentro de una ficha: ocupa el ancho repartido (menos un
   resto de aire) para que una franja ancha no deje huecos alrededor de la
   cara. Se acota para que no se vuelva gigante en tablets ni ilegible en
   franjas angostas. */
internal fun ladoCaraFicha(anchoFicha: Dp): Dp =
    (anchoFicha - 8.dp).coerceIn(24.dp, 96.dp)

/* Ficha de una cara sugerida por el buscador. El ancho llega ya repartido en
   píxeles (ver RepartoPanel.kt); el nombre completo sigue disponible para
   TalkBack aunque en pantalla se recorte. */
@Composable
internal fun FichaOpcion(
    c: Character,
    japones: Boolean,
    ancho: Int,
    onClick: () -> Unit,
) {
    val nombre = c.displayName(japones)
    val anchoDp = with(LocalDensity.current) { ancho.toDp() }
    Column(
        modifier = Modifier
            .width(anchoDp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                role = Role.Button,
                onClickLabel = nombre,
                onClick = onClick,
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Avatar(c.charId, nombre, modifier = Modifier.size(ladoCaraFicha(anchoDp)))
        Text(
            nombre,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
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
