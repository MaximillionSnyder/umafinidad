package com.maximillionsnyder.umafinidad.cartas.ui.skills

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.cartas.domain.CartasModelo
import com.maximillionsnyder.umafinidad.cartas.domain.etiquetaRareza
import com.maximillionsnyder.umafinidad.cartas.ui.components.COLADOR_ES
import com.maximillionsnyder.umafinidad.cartas.ui.components.CampoBusqueda
import com.maximillionsnyder.umafinidad.cartas.ui.components.ChipFiltro
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresAceleracion
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresDebuff
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresEscenario
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresFavorito
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresEventoApoyo
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresEventoPersonaje
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresEvolucion
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresHint
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresNormal
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresOcultar
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresOrden
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresPersonaje
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresRara
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresTodas
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresUnica
import com.maximillionsnyder.umafinidad.cartas.ui.components.ColoresVelocidad
import com.maximillionsnyder.umafinidad.cartas.ui.components.ContadorResultados
import com.maximillionsnyder.umafinidad.cartas.ui.components.EstadoVacioBusqueda
import com.maximillionsnyder.umafinidad.cartas.ui.components.EstadoVacio
import com.maximillionsnyder.umafinidad.cartas.ui.components.FilaChips
import com.maximillionsnyder.umafinidad.cartas.ui.components.FilaSkill
import com.maximillionsnyder.umafinidad.cartas.ui.components.ListaConArriba
import com.maximillionsnyder.umafinidad.cartas.ui.components.PanelFiltros
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.TX_PANTALLAS
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.t

/* Fuentes de una skill: el id filtra y la clave de texto da la etiqueta. La
   etiqueta no se guarda aquí porque depende del idioma elegido en la app. */
private val FUENTES = listOf(
    Triple("char", "fuente_personaje", ColoresPersonaje),
    Triple("char_e", "fuente_evento_personaje", ColoresEventoPersonaje),
    Triple("sup_hint", "fuente_hint_apoyo", ColoresHint),
    Triple("sup_e", "fuente_evento_apoyo", ColoresEventoApoyo),
    Triple("sce_e", "fuente_escenario", ColoresEscenario),
)

/* Rarezas de los chips: el id es el que guarda el filtro (no cambia) y el
   número es el que entiende el dominio para dar la etiqueta traducida. */
private val RAREZAS = listOf(
    Triple("normal", 1, ColoresNormal),
    Triple("rara", 2, ColoresRara),
    Triple("unica", 3, ColoresUnica),
    Triple("evolucion", 4, ColoresEvolucion),
)

@Composable
fun SkillsScreen(
    modelo: CartasModelo,
    idioma: String,
    compacta: Boolean,
    favoritos: Set<String>,
    onAbrirSkill: (Int) -> Unit,
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var rarezaFiltro by rememberSaveable { mutableStateOf("todas") }
    var soloDebuff by rememberSaveable { mutableStateOf(false) }
    var soloVelocidad by rememberSaveable { mutableStateOf(false) }
    var soloAceleracion by rememberSaveable { mutableStateOf(false) }
    var ocultarSinEn by rememberSaveable { mutableStateOf(false) }
    var fuentes by rememberSaveable { mutableStateOf(listOf<String>()) }
    var alfabetico by rememberSaveable { mutableStateOf(false) }
    var soloFavoritos by rememberSaveable { mutableStateOf(false) }
    /* Los filtros arrancan plegados: la lista tiene más sitio de entrada y quien
       los use abre el panel con un toque. El estado se recuerda mientras se
       navega, así que al volver de una ficha sigue como lo dejó el usuario. */
    var filtrosAbiertos by rememberSaveable { mutableStateOf(false) }

    val consulta = busqueda.trim()
    val filtradas = modelo.skills.filter { skill ->
        val coincideBusqueda = consulta.isBlank() ||
            skill.id.toString() == consulta ||
            skill.name.values.any { it?.contains(consulta, ignoreCase = true) == true }
        val coincideRareza = when (rarezaFiltro) {
            "normal" -> skill.rarity == 1
            "rara" -> skill.rarity == 2
            "unica" -> skill.rarity == 3 || skill.rarity == 5
            "evolucion" -> skill.rarity == 4 || skill.rarity == 6
            else -> true
        }
        val coincideDebuff = !soloDebuff || "dbf" in skill.types
        val coincideEfecto = (!soloVelocidad || skill.daVelocidad) && (!soloAceleracion || skill.daAceleracion)
        val coincideLanzamiento = !ocultarSinEn || "en" !in skill.unreleased
        val coincideFuente = fuentes.isEmpty() || fuentes.any { fuente ->
            when (fuente) {
                "char" -> skill.sources.characterCards.isNotEmpty()
                "char_e" -> skill.sources.characterEvents.isNotEmpty()
                "sup_hint" -> skill.sources.supportHints.isNotEmpty()
                "sup_e" -> skill.sources.supportEvents.isNotEmpty()
                "sce_e" -> skill.sources.scenarioEvents.isNotEmpty()
                else -> true
            }
        }
        val coincideFavorito = !soloFavoritos || skill.id.toString() in favoritos
        coincideBusqueda && coincideRareza && coincideDebuff && coincideEfecto &&
            coincideLanzamiento && coincideFuente && coincideFavorito
    }

    /* El orden de los datos originales no dice nada al usuario: poder ponerlo
       alfabético (respetando acentos y ñ) ayuda a encontrar una skill. */
    val ordenadas = if (alfabetico) {
        filtradas.sortedWith(compareBy(COLADOR_ES, { it.nombre(idioma) }))
    } else {
        filtradas
    }

    /* Cuántos filtros hay puestos: lo que muestra la pastilla del panel. */
    val filtrosActivos = (if (rarezaFiltro != "todas") 1 else 0) +
        (if (soloDebuff) 1 else 0) +
        (if (soloVelocidad) 1 else 0) +
        (if (soloAceleracion) 1 else 0) +
        (if (ocultarSinEn) 1 else 0) +
        fuentes.size

    Column(Modifier.fillMaxSize()) {
        Text(
            t(TX_PANTALLAS, "titulo_skills"),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        )
        CampoBusqueda(
            valor = busqueda,
            onValor = { busqueda = it },
            placeholder = t(TX_PANTALLAS, "buscar_por_nombre"),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        PanelFiltros(
            activos = filtrosActivos,
            abierto = filtrosAbiertos,
            onAlternar = { filtrosAbiertos = !filtrosAbiertos },
            onLimpiar = {
                rarezaFiltro = "todas"
                soloDebuff = false
                soloVelocidad = false
                soloAceleracion = false
                ocultarSinEn = false
                fuentes = emptyList()
            },
        ) {
            FilaChips {
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "todas"),
                    seleccionado = rarezaFiltro == "todas",
                    colores = ColoresTodas,
                    onClick = { rarezaFiltro = "todas" },
                )
                for ((id, rareza, colores) in RAREZAS) {
                    ChipFiltro(
                        texto = etiquetaRareza(rareza, idioma),
                        seleccionado = rarezaFiltro == id,
                        colores = colores,
                        onClick = { rarezaFiltro = if (rarezaFiltro == id) "todas" else id },
                    )
                }
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "chip_debuff"),
                    seleccionado = soloDebuff,
                    colores = ColoresDebuff,
                    onClick = { soloDebuff = !soloDebuff },
                )
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "chip_velocidad"),
                    seleccionado = soloVelocidad,
                    colores = ColoresVelocidad,
                    onClick = { soloVelocidad = !soloVelocidad },
                )
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "chip_aceleracion"),
                    seleccionado = soloAceleracion,
                    colores = ColoresAceleracion,
                    onClick = { soloAceleracion = !soloAceleracion },
                )
            }
            FilaChips {
                for ((id, clave, colores) in FUENTES) {
                    ChipFiltro(
                        texto = t(TX_PANTALLAS, clave),
                        seleccionado = id in fuentes,
                        colores = colores,
                        onClick = { fuentes = if (id in fuentes) fuentes - id else fuentes + id },
                    )
                }
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "chip_ocultar_sin_en"),
                    seleccionado = ocultarSinEn,
                    colores = ColoresOcultar,
                    onClick = { ocultarSinEn = !ocultarSinEn },
                )
            }
            FilaChips {
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "chip_favoritas"),
                    seleccionado = soloFavoritos,
                    colores = ColoresFavorito,
                    onClick = { soloFavoritos = !soloFavoritos },
                )
                ChipFiltro(
                    texto = t(TX_PANTALLAS, "chip_orden_az"),
                    seleccionado = alfabetico,
                    colores = ColoresOrden,
                    onClick = { alfabetico = !alfabetico },
                )
            }
        }
        ContadorResultados(
            t(TX_PANTALLAS, "contador_skills", "${ordenadas.size} / ${modelo.skills.size}"),
        )
        if (ordenadas.isEmpty()) {
            /* Si el filtro que deja la lista vacía es el de favoritas, el mensaje
               genérico no ayuda: hay que explicar cómo marcar una. */
            if (soloFavoritos && favoritos.isEmpty()) {
                EstadoVacio(
                    t(TX_PANTALLAS, "sin_favoritas_titulo"),
                    t(TX_PANTALLAS, "sin_favoritas_detalle"),
                    R.drawable.ic_estrella,
                )
            } else {
                EstadoVacioBusqueda()
            }
        } else {
            ListaConArriba(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            ) {
                items(ordenadas, key = { it.id }) { skill ->
                    FilaSkill(
                        skill = skill,
                        idioma = idioma,
                        compacta = compacta,
                        favorito = skill.id.toString() in favoritos,
                        onClick = { onAbrirSkill(skill.id) },
                    )
                }
            }
        }
    }
}
