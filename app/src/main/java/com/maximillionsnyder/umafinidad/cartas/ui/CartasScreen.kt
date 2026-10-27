package com.maximillionsnyder.umafinidad.cartas.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.cartas.domain.CartasModelo
import com.maximillionsnyder.umafinidad.cartas.domain.VISTA_COMPACTA
import com.maximillionsnyder.umafinidad.cartas.ui.cards.ApoyoDetalleScreen
import com.maximillionsnyder.umafinidad.cartas.ui.cards.ApoyosScreen
import com.maximillionsnyder.umafinidad.cartas.ui.cards.PersonajeDetalleScreen
import com.maximillionsnyder.umafinidad.cartas.ui.cards.PersonajesScreen
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.LocalIdioma
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.TX_UI
import com.maximillionsnyder.umafinidad.cartas.ui.i18n.t
import com.maximillionsnyder.umafinidad.cartas.ui.skills.SkillDetalleScreen
import com.maximillionsnyder.umafinidad.cartas.ui.skills.SkillsScreen

/* Host de la sección Cartas dentro de la 4.ª pestaña de la app: la barra
   inferior y el pager son de MainActivity; acá vive lo que era la navegación
   propia de cartasdebuff (skills, personajes y apoyos con sus fichas).

   El estado de la sección (densidad de las listas y skills favoritas) lo
   guarda AppViewModel en preferencias; el modelo de datos se carga recién
   cuando la pestaña se asienta, así el arranque de la app no paga los 3,7 MB
   de skills.json. */

/* Pantallas de la sección. La pila es de memoria y vive en CartasScreen: el
   botón atrás global de MainActivity sigue siendo el de la app. */
sealed interface PantallaCartas {
    data object Skills : PantallaCartas
    data object Personajes : PantallaCartas
    data object Apoyos : PantallaCartas
    data class SkillDetalle(val skillId: Int) : PantallaCartas
    data class PersonajeDetalle(val cardId: Int) : PantallaCartas
    data class ApoyoDetalle(val supportId: Int) : PantallaCartas
}

/* Clave estable por pantalla: permite restaurar scroll y filtros al volver. */
private fun claveDePantalla(pantalla: PantallaCartas): String = when (pantalla) {
    PantallaCartas.Skills -> "tab-skills"
    PantallaCartas.Personajes -> "tab-personajes"
    PantallaCartas.Apoyos -> "tab-apoyos"
    is PantallaCartas.SkillDetalle -> "skill-${pantalla.skillId}"
    is PantallaCartas.PersonajeDetalle -> "personaje-${pantalla.cardId}"
    is PantallaCartas.ApoyoDetalle -> "apoyo-${pantalla.supportId}"
}

/* Posición de la lista dentro de la TabRow; las fichas no la muestran. */
private fun indiceDe(pantalla: PantallaCartas): Int = when (pantalla) {
    PantallaCartas.Personajes -> 1
    PantallaCartas.Apoyos -> 2
    else -> 0
}

@Composable
fun CartasScreen(
    modelo: CartasModelo?,
    cargando: Boolean,
    fallo: Boolean,
    /* La pestaña está a la vista: el pager precompone las cuatro, así que sin
       esto el BackHandler de una ficha se comería el botón Atrás global aunque
       el usuario esté en otra pestaña. */
    activo: Boolean,
    idiomaUi: String,
    vista: String,
    favoritos: Set<String>,
    onAlternarFavorito: (Int) -> Unit,
    onReintentar: () -> Unit,
) {
    /* El idioma de la sección vale para los datos y para los textos de la
       interfaz de las pantallas portadas, que lo leen de aquí con t(). */
    CompositionLocalProvider(LocalIdioma provides idiomaUi) {
        /* Las pantallas portadas vienen de cartasdebuff, donde el Scaffold ponía
           el inset de la barra de estado. El Scaffold de acá solo da el de la
           barra de navegación (las otras pestañas se pintan su cabecera con
           statusBars): como esta sección no tiene HeaderBar, el inset superior
           se aplica una sola vez acá. */
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            val actual = modelo
            when {
                actual != null -> ContenidoCartas(
                    modelo = actual,
                    activo = activo,
                    idiomaUi = idiomaUi,
                    vista = vista,
                    favoritos = favoritos,
                    onAlternarFavorito = onAlternarFavorito,
                )
                /* Un reintento limpia el fallo antes de ponerse a cargar: si ambos
                   llegaran juntos, gana el spinner. */
                fallo && !cargando -> EstadoFallo(onReintentar = onReintentar)
                else -> EstadoCargando()
            }
        }
    }
}

/* Modelo ya leído: listas con su TabRow y fichas a pantalla completa. */
@Composable
private fun ContenidoCartas(
    modelo: CartasModelo,
    activo: Boolean,
    idiomaUi: String,
    vista: String,
    favoritos: Set<String>,
    onAlternarFavorito: (Int) -> Unit,
) {
    val compacta = vista == VISTA_COMPACTA

    /* Pila en memoria, igual que el MainActivity original de cartasdebuff: al
       cambiar de lista se reemplaza el fondo de la pila y las fichas se apilan
       encima. */
    val pila = remember { mutableStateListOf<PantallaCartas>(PantallaCartas.Skills) }
    val holder = rememberSaveableStateHolder()
    val actual = pila.last()

    /* Atrás deshace un paso de la sección; con la pila en la raíz no se
       habilita y el BackHandler de MainActivity decide (pestaña anterior o
       diálogo de salir). */
    BackHandler(enabled = activo && pila.size > 1) { pila.removeAt(pila.lastIndex) }

    fun abrir(destino: PantallaCartas) {
        pila.add(destino)
    }

    fun irATab(tab: PantallaCartas) {
        if (pila.size == 1 && pila[0] == tab) return
        pila.clear()
        pila.add(tab)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        /* Las tres listas comparten la TabRow; las fichas ocupan la sección
           entera porque ya traen su barra con la flecha de volver. */
        if (pila.size == 1) {
            TabRow(
                selectedTabIndex = indiceDe(actual),
                /* Sin fondo propio: el gradiente de la app se ve detrás. */
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Tab(
                    selected = actual == PantallaCartas.Skills,
                    onClick = { irATab(PantallaCartas.Skills) },
                    text = { Text(t(TX_UI, "tab_skills")) },
                )
                Tab(
                    selected = actual == PantallaCartas.Personajes,
                    onClick = { irATab(PantallaCartas.Personajes) },
                    text = { Text(t(TX_UI, "tab_personajes")) },
                )
                Tab(
                    selected = actual == PantallaCartas.Apoyos,
                    onClick = { irATab(PantallaCartas.Apoyos) },
                    text = { Text(t(TX_UI, "tab_apoyos")) },
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            holder.SaveableStateProvider(claveDePantalla(actual)) {
                when (val pantalla = actual) {
                    PantallaCartas.Skills -> SkillsScreen(
                        modelo = modelo,
                        idioma = idiomaUi,
                        compacta = compacta,
                        favoritos = favoritos,
                        onAbrirSkill = { abrir(PantallaCartas.SkillDetalle(it)) },
                    )
                    PantallaCartas.Personajes -> PersonajesScreen(
                        modelo = modelo,
                        idioma = idiomaUi,
                        compacta = compacta,
                        onAbrirCarta = { abrir(PantallaCartas.PersonajeDetalle(it)) },
                    )
                    PantallaCartas.Apoyos -> ApoyosScreen(
                        modelo = modelo,
                        idioma = idiomaUi,
                        compacta = compacta,
                        onAbrirCarta = { abrir(PantallaCartas.ApoyoDetalle(it)) },
                    )
                    is PantallaCartas.SkillDetalle -> SkillDetalleScreen(
                        modelo = modelo,
                        skillId = pantalla.skillId,
                        idioma = idiomaUi,
                        favorito = pantalla.skillId.toString() in favoritos,
                        onFavorito = onAlternarFavorito,
                        onVolver = { pila.removeAt(pila.lastIndex) },
                        onAbrirPersonaje = { abrir(PantallaCartas.PersonajeDetalle(it)) },
                        onAbrirApoyo = { abrir(PantallaCartas.ApoyoDetalle(it)) },
                    )
                    is PantallaCartas.PersonajeDetalle -> PersonajeDetalleScreen(
                        modelo = modelo,
                        cardId = pantalla.cardId,
                        idioma = idiomaUi,
                        onVolver = { pila.removeAt(pila.lastIndex) },
                        onAbrirSkill = { abrir(PantallaCartas.SkillDetalle(it)) },
                    )
                    is PantallaCartas.ApoyoDetalle -> ApoyoDetalleScreen(
                        modelo = modelo,
                        supportId = pantalla.supportId,
                        idioma = idiomaUi,
                        onVolver = { pila.removeAt(pila.lastIndex) },
                        onAbrirSkill = { abrir(PantallaCartas.SkillDetalle(it)) },
                    )
                }
            }
        }
    }
}

/* Column centrada para los dos estados sin datos. El fondo es el de la app: no
   hace falta ninguna superficie propia. */
@Composable
private fun EstadoCentrado(contenido: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        contenido()
    }
}

@Composable
private fun EstadoCargando() {
    EstadoCentrado {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(R.string.cartas_cargando),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EstadoFallo(onReintentar: () -> Unit) {
    EstadoCentrado {
        Text(
            stringResource(R.string.cartas_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onReintentar) {
            Text(stringResource(R.string.cartas_reintentar))
        }
    }
}
