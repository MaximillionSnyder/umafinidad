package com.maximillionsnyder.umafinidad.cartas.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.TX_UI
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.t
import kotlinx.coroutines.launch

/* Formas compartidas de la zona de búsqueda y filtros. */
val FORMA_CAMPO = RoundedCornerShape(14.dp)
val FORMA_PANEL = RoundedCornerShape(16.dp)

/* Campo de búsqueda común a las tres listas: lupa a la izquierda y una "x" para
   vaciarlo de un toque, sin borrar letra a letra. */
@Composable
fun CampoBusqueda(
    valor: String,
    onValor: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValor,
        placeholder = { Text(placeholder) },
        singleLine = true,
        shape = FORMA_CAMPO,
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_buscar),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = {
            if (valor.isNotEmpty()) {
                IconButton(onClick = { onValor("") }) {
                    Icon(
                        painterResource(R.drawable.ic_cerrar),
                        contentDescription = t(TX_UI, "borrar_busqueda"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/* Fila de chips que se desplaza en horizontal cuando no entran en la pantalla. */
@Composable
fun FilaChips(contenido: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = contenido,
    )
}

/* Panel plegable de filtros: recoge los chips en una tarjeta para que la lista
   tenga más sitio, y avisa cuántos filtros hay puestos sin abrirlo. */
@Composable
fun PanelFiltros(
    activos: Int,
    abierto: Boolean,
    onAlternar: () -> Unit,
    onLimpiar: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(FORMA_PANEL)
            /* `surface` es el mismo color que las tarjetas de la lista: contrasta
               con el fondo tanto en tema claro como en oscuro. */
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, FORMA_PANEL),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAlternar)
                .padding(start = 12.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painterResource(R.drawable.ic_filtros),
                contentDescription = null,
                tint = if (activos > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(t(TX_UI, "filtros_titulo"), style = MaterialTheme.typography.titleSmall)
            if (activos > 0) {
                Spacer(Modifier.width(8.dp))
                ContadorFiltros(activos)
            }
            Spacer(Modifier.weight(1f))
            if (activos > 0) {
                TextButton(
                    onClick = onLimpiar,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(t(TX_UI, "filtro_limpiar"), style = MaterialTheme.typography.labelLarge)
                }
            }
            Icon(
                painterResource(R.drawable.ic_desplegar),
                contentDescription = if (abierto) t(TX_UI, "filtros_ocultar") else t(TX_UI, "filtros_mostrar"),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .rotate(if (abierto) 180f else 0f),
            )
        }
        AnimatedVisibility(
            visible = abierto,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = contenido,
            )
        }
    }
}

/* Pastilla con cuántos filtros están puestos. */
@Composable
fun ContadorFiltros(activos: Int) {
    Text(
        if (activos == 1) t(TX_UI, "activos_uno") else t(TX_UI, "activos_varios", activos),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/* Contador de resultados: "N de M", en una pastilla discreta bajo los filtros. */
@Composable
fun ContadorResultados(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 2.dp),
    )
}

/* Botón para volver de un salto al principio de la lista. */
@Composable
fun BotonVolverArriba(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier,
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(painterResource(R.drawable.ic_arriba), contentDescription = t(TX_UI, "volver_arriba"))
        }
    }
}

/* Lista larga con el botón "volver arriba" ya resuelto: con 1900 skills, bajar
   y tener que subir a mano era lo más molesto de la pantalla.

   Es extensión de ColumnScope a propósito: con `weight(1f)` la lista ocupa
   exactamente el alto que sobra bajo el encabezado. Con `fillMaxSize()` se
   quedaba con el alto entero de la columna, así que en las fichas se comía el
   final del contenido y el botón caía fuera de la pantalla. */
@Composable
fun ColumnScope.ListaConArriba(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    contenido: LazyListScope.() -> Unit,
) {
    val estado = rememberLazyListState()
    val alcance = rememberCoroutineScope()
    /* Aparece en cuanto la primera fila se va de la pantalla: si se exige bajar
       más, el botón se esconde justo cuando hace falta. */
    val mostrar by remember {
        derivedStateOf { estado.firstVisibleItemIndex > 0 || estado.firstVisibleItemScrollOffset > 200 }
    }
    Box(modifier.fillMaxWidth().weight(1f)) {
        LazyColumn(
            state = estado,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            content = contenido,
        )
        BotonVolverArriba(
            visible = mostrar,
            onClick = { alcance.launch { estado.animateScrollToItem(0) } },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
        )
    }
}
