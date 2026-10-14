package com.maximillionsnyder.umafinidad.ui.buscar

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.Criterios
import com.maximillionsnyder.umafinidad.domain.ErrorPadres
import com.maximillionsnyder.umafinidad.domain.FACTORES_AZULES
import com.maximillionsnyder.umafinidad.domain.FACTORES_ROSA
import com.maximillionsnyder.umafinidad.domain.FactorChispa
import com.maximillionsnyder.umafinidad.domain.Padre
import com.maximillionsnyder.umafinidad.domain.Rama
import com.maximillionsnyder.umafinidad.domain.Requisito
import com.maximillionsnyder.umafinidad.domain.SalidaPadres
import com.maximillionsnyder.umafinidad.domain.cartaOriginal
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import com.maximillionsnyder.umafinidad.domain.topeEstrellas
import com.maximillionsnyder.umafinidad.ui.componentes.HeaderBarConVolver

/* Buscador de padres en uma.moe.
 *
 * La pantalla no decide nada por su cuenta: arma los criterios, se los pasa al
 * buscador y dibuja el estado que vuelve. En particular no inventa resultados
 * parecidos: si la búsqueda dice que no hay, muestra que no hay, y si se cortó
 * por el tope lo dice con otras palabras para no afirmar de más. */

@Composable
fun BuscarPadresScreen(
    modelo: AffinityModel,
    japones: Boolean,
    hayKey: Boolean,
    buscando: Boolean,
    salida: SalidaPadres?,
    onBuscar: (Criterios) -> Unit,
    onVolver: () -> Unit,
    onIrAAjustes: () -> Unit,
) {
    var filtro by rememberSaveable { mutableStateOf("") }
    var elegido by rememberSaveable { mutableStateOf<Int?>(null) }
    var rama by rememberSaveable { mutableStateOf(Rama.PADRE) }
    var factorId by rememberSaveable { mutableStateOf(FACTORES_AZULES.first().id) }
    var minimo by rememberSaveable { mutableStateOf(3) }
    var maximo by rememberSaveable { mutableStateOf(3) }

    val factor = remember(factorId) {
        (FACTORES_AZULES + FACTORES_ROSA).first { it.id == factorId }
    }
    val tope = topeEstrellas(rama)

    /* Al cambiar de rama el tope baja de 9 a 3: el rango se recorta para no
     * quedar pidiendo algo imposible. */
    fun ajustarRama(nueva: Rama) {
        rama = nueva
        val t = topeEstrellas(nueva)
        if (minimo > t) minimo = t
        if (maximo > t) maximo = t
        if (maximo < minimo) maximo = minimo
    }

    fun ajustarMinimo(valor: Int) {
        minimo = valor
        if (maximo < valor) maximo = valor
    }

    val jugables = remember(modelo) {
        modelo.personajes.filter { it.playable == true && it.active == true }
    }
    val sugerencias = remember(filtro, jugables) {
        if (filtro.trim().length < 2) emptyList() else rankearSugerencias(jugables, filtro)
    }
    val personaje = elegido?.let { id -> modelo.porId(id) }

    fun lanzar() {
        onBuscar(
            Criterios(
                cartaPadre = elegido?.let(::cartaOriginal),
                requisitos = listOf(
                    Requisito(
                        rama = rama,
                        factor = factor,
                        minEstrellas = minimo,
                        maxEstrellas = maximo,
                    ),
                ),
            ),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HeaderBarConVolver(
            titulo = stringResource(R.string.buscar_padres),
            onVolver = onVolver,
            iconoRes = R.drawable.ic_buscar,
        )

        if (!hayKey) {
            FaltaKey(onIrAAjustes = onIrAAjustes)
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.buscar_padres_sub),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            /* ---- personaje del padre ---- */
            item {
                OutlinedTextField(
                    value = if (personaje != null) personaje.displayName(japones) else filtro,
                    onValueChange = {
                        filtro = it
                        elegido = null
                    },
                    label = { Text(stringResource(R.string.buscar_personaje)) },
                    supportingText = { Text(stringResource(R.string.buscar_solo_original)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            items(sugerencias, key = { it.charId }) { c ->
                TextButton(
                    onClick = {
                        elegido = c.charId
                        filtro = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(c.displayName(japones), modifier = Modifier.fillMaxWidth())
                }
            }

            /* ---- rama ---- */
            item {
                Etiqueta(stringResource(R.string.buscar_rama))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = rama == Rama.PADRE,
                            onClick = { ajustarRama(Rama.PADRE) },
                            label = { Text(stringResource(R.string.buscar_rama_padre)) },
                        )
                    }
                    item {
                        FilterChip(
                            selected = rama == Rama.LINEA,
                            onClick = { ajustarRama(Rama.LINEA) },
                            label = { Text(stringResource(R.string.buscar_rama_linea)) },
                        )
                    }
                }
            }

            /* ---- chispa ---- */
            item {
                Etiqueta(stringResource(R.string.buscar_stats))
                FilaFactores(FACTORES_AZULES, factorId, japones) { factorId = it }
            }
            item {
                Etiqueta(stringResource(R.string.buscar_aptitudes))
                FilaFactores(FACTORES_ROSA, factorId, japones) { factorId = it }
            }

            /* ---- estrellas ---- */
            item {
                Etiqueta(stringResource(R.string.buscar_desde))
                FilaEstrellas(1, tope, minimo) { ajustarMinimo(it) }
            }
            item {
                Etiqueta(stringResource(R.string.buscar_hasta))
                FilaEstrellas(minimo, tope, maximo) { maximo = it }
            }

            item {
                Button(
                    onClick = { lanzar() },
                    enabled = !buscando,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.buscar_boton))
                }
            }

            /* ---- estado ---- */
            if (buscando) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.buscar_buscando),
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            } else if (salida != null) {
                item { EstadoDeLaBusqueda(salida) }
                items(salida.padres, key = { it.hashCode() }) { p -> FilaPadre(p) }
            }
        }
    }
}

/* ------------------------------------------------------------- secciones */

@Composable
private fun Etiqueta(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
    )
}

@Composable
private fun FilaFactores(
    factores: List<FactorChispa>,
    elegido: Int,
    japones: Boolean,
    onElegir: (Int) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(factores, key = { it.id }) { f ->
            FilterChip(
                selected = f.id == elegido,
                onClick = { onElegir(f.id) },
                label = { Text(stringResource(nombreDeFactor(f.clave))) },
            )
        }
    }
}

@Composable
private fun FilaEstrellas(desde: Int, hasta: Int, elegido: Int, onElegir: (Int) -> Unit) {
    if (desde > hasta) return
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items((desde..hasta).toList()) { n ->
            FilterChip(
                selected = n == elegido,
                onClick = { onElegir(n) },
                label = { Text("$n\u2605") },
            )
        }
    }
}

/* --------------------------------------------------------------- estados */

@Composable
private fun FaltaKey(onIrAAjustes: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.padding(24.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.buscar_sin_key),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.buscar_sin_key_detalle),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = onIrAAjustes) {
                    Text(stringResource(R.string.buscar_ir_ajustes))
                }
            }
        }
    }
}

/* Los tres desenlaces posibles, cada uno dicho con sus palabras. */
@Composable
private fun EstadoDeLaBusqueda(salida: SalidaPadres) {
    val (titulo, detalle) = when (salida.estado) {
        com.maximillionsnyder.umafinidad.domain.EstadoPadres.HAY ->
            stringResource(R.string.buscar_resultados, salida.padres.size) to
                stringResource(R.string.buscar_revisados, salida.revisados)

        com.maximillionsnyder.umafinidad.domain.EstadoPadres.NO_HAY ->
            stringResource(R.string.buscar_no_hay) to
                stringResource(R.string.buscar_no_hay_detalle, salida.revisados)

        com.maximillionsnyder.umafinidad.domain.EstadoPadres.NO_SE_ENCONTRO ->
            stringResource(R.string.buscar_no_se_encontro) to
                stringResource(
                    R.string.buscar_no_se_encontro_detalle,
                    salida.revisados,
                    if (salida.totalExacto) salida.total.toString()
                    else stringResource(R.string.buscar_mas_de, salida.total),
                )

        com.maximillionsnyder.umafinidad.domain.EstadoPadres.SIN_CRITERIOS ->
            stringResource(R.string.buscar_sin_criterios) to ""

        com.maximillionsnyder.umafinidad.domain.EstadoPadres.BUSCANDO ->
            stringResource(R.string.buscar_buscando) to ""

        com.maximillionsnyder.umafinidad.domain.EstadoPadres.ERROR ->
            stringResource(mensajeDeError(salida.error)) to ""
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (detalle.isNotEmpty()) {
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@StringRes
private fun mensajeDeError(error: ErrorPadres?): Int = when (error) {
    ErrorPadres.SIN_KEY -> R.string.buscar_error_sin_key
    ErrorPadres.KEY_RECHAZADA -> R.string.buscar_error_key
    ErrorPadres.SIN_RED -> R.string.buscar_error_sin_red
    ErrorPadres.SERVIDOR -> R.string.buscar_error_servidor
    ErrorPadres.RESPUESTA_INVALIDA -> R.string.buscar_error_respuesta
    null -> R.string.buscar_error_servidor
}

@Composable
private fun FilaPadre(padre: Padre) {
    val estrellas = listOfNotNull(
        padre.estrellasAzul?.let { "$it\u2605" },
        padre.estrellasRosa?.let { "$it\u2605" },
        padre.estrellasVerde?.let { "$it\u2605" },
        padre.estrellasBlanco?.let { "$it\u2605" },
    ).joinToString("  ")

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = padre.entrenador ?: stringResource(R.string.buscar_sin_entrenador),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (estrellas.isNotEmpty()) {
                Text(text = estrellas, style = MaterialTheme.typography.bodyMedium)
            }
            padre.afinidad?.let { puntos ->
                Text(
                    text = stringResource(R.string.buscar_afinidad, puntos),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/* ------------------------------------------------------- nombres de factor */

/* Las 10 aptitudes ya tienen traducción en la app (apt_*); los 5 stats son
 * nuevos. */
@StringRes
private fun nombreDeFactor(clave: String): Int = when (clave) {
    "speed" -> R.string.factor_speed
    "stamina" -> R.string.factor_stamina
    "power" -> R.string.factor_power
    "guts" -> R.string.factor_guts
    "wit" -> R.string.factor_wit
    "turf" -> R.string.apt_turf
    "dirt" -> R.string.apt_dirt
    "front_runner" -> R.string.apt_fuga
    "pace_chaser" -> R.string.apt_vanguardia
    "late_surger" -> R.string.apt_remate
    "end_closer" -> R.string.apt_retraso
    "sprint" -> R.string.apt_corta
    "mile" -> R.string.apt_milla
    "medium" -> R.string.apt_media
    "long" -> R.string.apt_larga
    else -> R.string.factor_speed
}
