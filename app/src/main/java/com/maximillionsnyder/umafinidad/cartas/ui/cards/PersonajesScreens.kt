package com.maximillionsnyder.umafinidad.cartas.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.cartas.domain.CartaPersonaje
import com.maximillionsnyder.umafinidad.cartas.domain.CartasModelo
import com.maximillionsnyder.umafinidad.cartas.ui.components.BarraDetalle
import com.maximillionsnyder.umafinidad.cartas.ui.components.TarjetaApp
import com.maximillionsnyder.umafinidad.cartas.ui.components.COLADOR_ES
import com.maximillionsnyder.umafinidad.cartas.ui.components.CampoBusqueda
import com.maximillionsnyder.umafinidad.cartas.ui.components.ChipFiltro
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresEstrella1
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresEstrella2
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresEstrella3
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresOrden
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresTodas
import com.maximillionsnyder.umafinidad.cartas.ui.components.ContadorResultados
import com.maximillionsnyder.umafinidad.cartas.ui.components.EstadoVacio
import com.maximillionsnyder.umafinidad.cartas.ui.components.EstadoVacioBusqueda
import com.maximillionsnyder.umafinidad.cartas.ui.components.Estrellas
import com.maximillionsnyder.umafinidad.cartas.ui.components.FilaChips
import com.maximillionsnyder.umafinidad.cartas.ui.components.FilaPersonaje
import com.maximillionsnyder.umafinidad.cartas.ui.components.FilaSkill
import com.maximillionsnyder.umafinidad.cartas.ui.components.ImagenRemota
import com.maximillionsnyder.umafinidad.cartas.ui.components.ListaConArriba
import com.maximillionsnyder.umafinidad.cartas.ui.components.PanelFiltros
import com.maximillionsnyder.umafinidad.cartas.ui.components.SeccionTitulo
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaKind
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaObtencion
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.TX_PANTALLAS
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.t

/* Chips de estrellas de la lista de personajes: el número filtra y la etiqueta
   de estrellas no cambia con el idioma. */
private val CHIPS_ESTRELLAS = listOf(
    Triple(3, "3★", ColoresEstrella3),
    Triple(2, "2★", ColoresEstrella2),
    Triple(1, "1★", ColoresEstrella1),
)

@Composable
fun PersonajesScreen(
    modelo: CartasModelo,
    idioma: String,
    compacta: Boolean,
    onAbrirCarta: (Int) -> Unit,
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var estrellas by rememberSaveable { mutableStateOf(0) }
    var alfabetico by rememberSaveable { mutableStateOf(false) }
    /* Los filtros arrancan plegados: la lista tiene más sitio de entrada y quien
       los use abre el panel con un toque. El estado se recuerda mientras se
       navega, así que al volver de una ficha sigue como lo dejó el usuario. */
    var filtrosAbiertos by rememberSaveable { mutableStateOf(false) }

    val consulta = busqueda.trim()
    val filtradas = modelo.cartasPersonaje.filter { carta ->
        val coincideEstrellas = estrellas == 0 || carta.rarity == estrellas
        val coincideBusqueda = consulta.isBlank() ||
            carta.name.values.any { it?.contains(consulta, ignoreCase = true) == true } ||
            carta.title.values.any { it?.contains(consulta, ignoreCase = true) == true }
        coincideEstrellas && coincideBusqueda
    }

    val ordenadas = if (alfabetico) {
        filtradas.sortedWith(compareBy(COLADOR_ES, { it.nombre(idioma) }))
    } else {
        filtradas
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            t(TX_PANTALLAS, "titulo_personajes"),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )
        CampoBusqueda(
            valor = busqueda,
            onValor = { busqueda = it },
            placeholder = t(TX_PANTALLAS, "buscar_por_nombre_o_titulo"),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        PanelFiltros(
            activos = if (estrellas != 0) 1 else 0,
            abierto = filtrosAbiertos,
            onAlternar = { filtrosAbiertos = !filtrosAbiertos },
            onLimpiar = { estrellas = 0 },
        ) {
            FilaChips {
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "todas"),
                    seleccionado = estrellas == 0,
                    colores = ColoresTodas,
                    onClick = { estrellas = 0 },
                )
                for ((valor, etiqueta, colores) in CHIPS_ESTRELLAS) {
                    ChipFiltro(
                        texto = etiqueta,
                        seleccionado = estrellas == valor,
                        colores = colores,
                        onClick = { estrellas = if (estrellas == valor) 0 else valor },
                    )
                }
            }
            FilaChips {
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "chip_orden_az"),
                    seleccionado = alfabetico,
                    colores = ColoresOrden,
                    onClick = { alfabetico = !alfabetico },
                )
            }
        }
        ContadorResultados(
            t(TX_PANTALLAS, "contador_cartas", "${ordenadas.size} / ${modelo.cartasPersonaje.size}"),
        )
        if (ordenadas.isEmpty()) {
            EstadoVacioBusqueda()
        } else {
            ListaConArriba(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            ) {
                items(ordenadas, key = { it.cardId }) { carta ->
                    /* Cuántas skills salen de esta carta: ayuda a elegir cuál abrir. */
                    FilaPersonaje(
                        carta = carta,
                        idioma = idioma,
                        kinds = null,
                        compacta = compacta,
                        numeroSkills = modelo.skillsPorCarta[carta.cardId]?.distinct()?.size,
                    ) { onAbrirCarta(carta.cardId) }
                }
            }
        }
    }
}

@Composable
fun PersonajeDetalleScreen(
    modelo: CartasModelo,
    cardId: Int,
    idioma: String,
    onVolver: () -> Unit,
    onAbrirSkill: (Int) -> Unit,
) {
    val carta = modelo.cartaPersonaje(cardId)
    if (carta == null) {
        Column(Modifier.fillMaxSize()) {
            BarraDetalle(t(TX_PANTALLAS, "titulo_carta_personaje"), onVolver)
            EstadoVacio(t(TX_PANTALLAS, "carta_no_encontrada"))
        }
        return
    }

    val skills = modelo.skillsPorCarta[cardId].orEmpty().distinct()

    Column(Modifier.fillMaxSize()) {
        BarraDetalle(carta.nombre(idioma), onVolver)
        /* Mismo botón de volver arriba que en las listas: las fichas también
           se hacen largas (fuentes, condiciones, evoluciones). */
        ListaConArriba(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            item { EncabezadoCartaPersonaje(carta, idioma, skills.size) }
            val grupos = listOf("unique", "innate", "awakening", "evolution")
            for (kind in grupos) {
                val delGrupo = skills.filter { skill ->
                    skill.sources.characterCards.any { it.cardId == cardId && kind in it.kinds }
                }
                if (delGrupo.isNotEmpty()) {
                    item(key = "titulo-$kind") {
                        SeccionTitulo(etiquetaKind(kind, idioma), delGrupo.size)
                    }
                    items(delGrupo, key = { "$kind-${it.id}" }) { skill ->
                        FilaSkill(skill, idioma) { onAbrirSkill(skill.id) }
                    }
                }
            }
            val eventos = skills.filter { cardId in it.sources.characterEvents }
            if (eventos.isNotEmpty()) {
                item(key = "titulo-evento") {
                    SeccionTitulo(t(TX_PANTALLAS, "seccion_eventos_personaje"), eventos.size)
                }
                items(eventos, key = { "evento-${it.id}" }) { skill ->
                    FilaSkill(skill, idioma) { onAbrirSkill(skill.id) }
                }
            }
            if (skills.isEmpty()) {
                item { EstadoVacio(t(TX_PANTALLAS, "sin_skills_carta")) }
            }
        }
    }
}

@Composable
private fun EncabezadoCartaPersonaje(carta: CartaPersonaje, idioma: String, numeroSkills: Int) {
    TarjetaApp(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp)) {
            ImagenRemota(carta.image, null, Modifier.size(96.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(carta.nombre(idioma), style = MaterialTheme.typography.titleMedium)
                val japones = carta.name["ja"]
                if (idioma != "ja" && !japones.isNullOrBlank()) {
                    Text(japones, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val titulo = carta.titulo(idioma)
                if (titulo.isNotBlank()) {
                    Text(titulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Estrellas(carta.rarity)
                    Text(
                        if (numeroSkills == 1) {
                            t(TX_PANTALLAS, "una_skill")
                        } else {
                            t(TX_PANTALLAS, "n_skills", numeroSkills.toString())
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    etiquetaObtencion(carta.obtained, idioma),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
