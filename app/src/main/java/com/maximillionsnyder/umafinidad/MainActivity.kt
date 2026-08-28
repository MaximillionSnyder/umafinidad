package com.maximillionsnyder.umafinidad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.maximillionsnyder.umafinidad.ui.AppViewModel
import com.maximillionsnyder.umafinidad.ui.compat.CompatScreen
import com.maximillionsnyder.umafinidad.ui.corredora.CorredoraScreen
import com.maximillionsnyder.umafinidad.ui.elenco.ElencoScreen
import com.maximillionsnyder.umafinidad.ui.groups.GroupsScreen
import com.maximillionsnyder.umafinidad.ui.ranking.RankingScreen
import com.maximillionsnyder.umafinidad.ui.settings.SettingsScreen
import com.maximillionsnyder.umafinidad.ui.theme.UmaAfinidadTheme
import com.maximillionsnyder.umafinidad.ui.theme.fondoGradiente
import com.maximillionsnyder.umafinidad.ui.top.TopLinajesScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UmaAfinidadTheme {
                App(vm)
            }
        }
    }
}

@Composable
private fun App(vm: AppViewModel) {
    val modelo by vm.modelo.collectAsState()
    val seleccion by vm.seleccion.collectAsState()
    val resultado by vm.resultado.collectAsState()
    val modoGrilla by vm.modoGrilla.collectAsState()
    val arboles by vm.arboles.collectAsState()
    val arbolPendiente by vm.arbolPendiente.collectAsState()
    val elenco by vm.elenco.collectAsState()

    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    /* Grupos es una referencia archivada: se abre a pantalla completa
       desde Ajustes y el botón atrás la cierra. */
    var verGrupos by rememberSaveable { mutableStateOf(false) }

    /* Aviso antes de salir: el botón/gesto atrás nunca cierra sin confirmar
       (salvo dentro de Grupos, donde primero vuelve). */
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    BackHandler { if (verGrupos) verGrupos = false else confirmarSalida = true }

    val japones = LocalConfiguration.current.locales[0].language == "ja"

    /* Una config pedida desde Ajustes abre Mi corredora. */
    LaunchedEffect(arbolPendiente) {
        if (arbolPendiente != null) tab = 3
    }

    /* Fondo con gradiente en toda la app. */
    Box(modifier = Modifier.fillMaxSize().fondoGradiente()) {
        if (verGrupos) {
            val m = modelo
            if (m == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                GroupsScreen(modelo = m, japones = japones, onVolver = { verGrupos = false })
            }
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.97f)) {
                        NavigationBarItem(
                            selected = tab == 0,
                            onClick = { tab = 0 },
                            icon = { Icon(painterResource(R.drawable.ic_tab_compat), contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_compat)) },
                        )
                        NavigationBarItem(
                            selected = tab == 1,
                            onClick = { tab = 1 },
                            icon = { Icon(painterResource(R.drawable.ic_tab_elenco), contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_elenco)) },
                        )
                        NavigationBarItem(
                            selected = tab == 2,
                            onClick = { tab = 2 },
                            icon = { Icon(painterResource(R.drawable.ic_tab_top), contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_top)) },
                        )
                        NavigationBarItem(
                            selected = tab == 3,
                            onClick = { tab = 3 },
                            icon = { Icon(painterResource(R.drawable.ic_tab_corredora), contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_corredora)) },
                        )
                        NavigationBarItem(
                            selected = tab == 4,
                            onClick = { tab = 4 },
                            icon = { Icon(painterResource(R.drawable.ic_tab_ranking), contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_ranking)) },
                        )
                        NavigationBarItem(
                            selected = tab == 5,
                            onClick = { tab = 5 },
                            icon = { Icon(painterResource(R.drawable.ic_tab_ajustes), contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_ajustes)) },
                        )
                    }
                },
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Crossfade(targetState = modelo to tab, label = "contenido") { (_, t) ->
                        val m = modelo
                        if (m == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                            return@Crossfade
                        }
                        when (t) {
                            0 -> CompatScreen(
                                modelo = m,
                                seleccion = seleccion,
                                resultado = resultado,
                                modoGrilla = modoGrilla,
                                japones = japones,
                                onToggle = vm::toggle,
                                onQuitarSlot = vm::quitarSlot,
                                onConfirmarQuitarSoloHijo = vm::confirmarQuitarSoloHijo,
                                onLimpiarTodo = vm::limpiarTodo,
                                avisar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                            )
                            1 -> ElencoScreen(
                                modelo = m,
                                japones = japones,
                                elenco = elenco,
                                onToggle = vm::toggleElenco,
                                onMarcar = vm::marcarElenco,
                                onLimpiar = vm::limpiarElenco,
                                onVerHerencia = { linaje ->
                                    vm.cargarLinaje(linaje)
                                    tab = 0
                                },
                            )
                            2 -> TopLinajesScreen(
                                modelo = m,
                                japones = japones,
                                onVerHerencia = { linaje ->
                                    vm.cargarLinaje(linaje)
                                    tab = 0
                                },
                            )
                            3 -> CorredoraScreen(
                                modelo = m,
                                japones = japones,
                                arboles = arboles,
                                pendiente = arbolPendiente,
                                onGuardarArbol = vm::guardarArbol,
                                onEliminarArbol = vm::eliminarArbol,
                                onConsumirPendiente = vm::consumirArbolPendiente,
                                avisar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                                onVerHerencia = { sel ->
                                    vm.cargarSeleccion(sel)
                                    tab = 0
                                },
                            )
                            4 -> RankingScreen(modelo = m, japones = japones)
                            else -> SettingsScreen(
                                modoGrilla = modoGrilla,
                                onModoGrilla = vm::setModoGrilla,
                                modelo = m,
                                japones = japones,
                                arboles = arboles,
                                onAbrirArbol = vm::abrirArbol,
                                onEliminarArbol = vm::eliminarArbol,
                                onAbrirGrupos = { verGrupos = true },
                            )
                        }
                    }
                }
            }
        }

        /* Diálogo de confirmación antes de salir de la app. */
        if (confirmarSalida) {
            AlertDialog(
                onDismissRequest = { confirmarSalida = false },
                title = { Text(stringResource(R.string.salir_titulo)) },
                text = { Text(stringResource(R.string.salir_mensaje)) },
                confirmButton = {
                    TextButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                        Text(stringResource(R.string.salir_salir))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmarSalida = false }) {
                        Text(stringResource(R.string.cancelar))
                    }
                },
            )
        }
}

}