package com.maximillionsnyder.umafinidad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import com.maximillionsnyder.umafinidad.ui.AppViewModel
import com.maximillionsnyder.umafinidad.ui.compat.CompatScreen
import com.maximillionsnyder.umafinidad.ui.groups.GroupsScreen
import com.maximillionsnyder.umafinidad.ui.theme.UmaAfinidadTheme
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
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val japones = LocalConfiguration.current.locales[0].language == "ja"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(painterResource(R.drawable.ic_tab_compat), contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_compat)) },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(painterResource(R.drawable.ic_tab_groups), contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_groups)) },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(painterResource(R.drawable.ic_tab_top), contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_top)) },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                0 -> modelo?.let { m ->
                    CompatScreen(
                        modelo = m,
                        seleccion = seleccion,
                        resultado = resultado,
                        japones = japones,
                        onToggle = vm::toggle,
                        onQuitarSlot = vm::quitarSlot,
                        onConfirmarQuitarSoloHijo = vm::confirmarQuitarSoloHijo,
                        onLimpiarTodo = vm::limpiarTodo,
                        avisar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                    )
                }
                1 -> modelo?.let { m ->
                    GroupsScreen(modelo = m, japones = japones)
                }
                2 -> modelo?.let { m ->
                    TopLinajesScreen(
                        modelo = m,
                        japones = japones,
                        onVerHerencia = { linaje ->
                            vm.cargarLinaje(linaje)
                            tab = 0
                        },
                    )
                }
            }

            /* Pantalla de carga mientras se parsean los datos. */
            if (modelo == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
