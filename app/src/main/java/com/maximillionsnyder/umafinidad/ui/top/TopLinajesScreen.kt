package com.maximillionsnyder.umafinidad.ui.top

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Linaje
import com.maximillionsnyder.umafinidad.ui.componentes.CardFilaTop
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* Porte de montarTop(): top 20 de linajes completos con cálculo diferido.
   Tocar la card carga la herencia. La fila vive en CardFilaTop
   (compartida con "Mis corredoras"). */
@Composable
fun TopLinajesScreen(modelo: AffinityModel, japones: Boolean, onVerHerencia: (Linaje) -> Unit) {
    var top by remember { mutableStateOf<List<Linaje>?>(null) }

    LaunchedEffect(modelo) {
        top = withContext(Dispatchers.Default) { modelo.topLinajes(20) }
    }

    val lista = top
    if (lista == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator()
                Text(stringResource(R.string.calculando), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    if (lista.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.sin_datos), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            HeaderBar(
                titulo = stringResource(R.string.tab_top),
                pillTexto = "${lista.size} ${stringResource(R.string.tab_top).lowercase()}"
            )
            Spacer(Modifier.padding(bottom = 4.dp))
        }
        itemsIndexed(lista) { i, combo ->
            CardFilaTop(i, combo, modelo, japones) { onVerHerencia(combo) }
        }
    }
}
