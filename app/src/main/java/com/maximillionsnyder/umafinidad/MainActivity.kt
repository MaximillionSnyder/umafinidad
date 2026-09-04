package com.maximillionsnyder.umafinidad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximillionsnyder.umafinidad.data.ThemeMode
import com.maximillionsnyder.umafinidad.data.aplicarIdioma
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
        // Aplica idioma guardado antes de inflar para que resources ya estén localizados
        try {
            val prefs = com.maximillionsnyder.umafinidad.data.PrefsRepository(this)
            aplicarIdioma(prefs.idioma)
        } catch (_: Exception) {}
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val tema by vm.tema.collectAsState()
            UmaAfinidadTheme(tema = tema) {
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
    val tema by vm.tema.collectAsState()

    val pagerState = rememberPagerState(initialPage = 0) { 5 }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val idioma by vm.idioma.collectAsState()

    LaunchedEffect(idioma) {
        aplicarIdioma(context, idioma)
    }

    /* Grupos y Ranking son referencias archivadas: se abren a
       pantalla completa desde Ajustes y el botón atrás las cierra. Mis Umas ya es tab. */
    var verGrupos by rememberSaveable { mutableStateOf(false) }
    var verRanking by rememberSaveable { mutableStateOf(false) }

    /* Aviso antes de salir: el botón/gesto atrás nunca cierra sin confirmar
       (salvo dentro de Grupos/Ranking, donde primero vuelve). */
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    BackHandler {
        if (verGrupos) verGrupos = false
        else if (verRanking) verRanking = false
        else confirmarSalida = true
    }

    val japones = LocalConfiguration.current.locales[0].language == "ja"
    val esOscuro = when (tema) {
        ThemeMode.CLARO -> false
        ThemeMode.OSCURO -> true
        ThemeMode.SISTEMA -> isSystemInDarkTheme()
    }

    /* Una config pedida desde Ajustes abre Mi corredora. */
    LaunchedEffect(arbolPendiente) {
        if (arbolPendiente != null) pagerState.animateScrollToPage(2)
    }

    /* Fondo con gradiente en toda la app. */
    Box(modifier = Modifier.fillMaxSize().fondoGradiente(esOscuro)) {
        if (verGrupos) {
            val m = modelo
            if (m == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                GroupsScreen(modelo = m, japones = japones, onVolver = { verGrupos = false })
            }
        } else if (verRanking) {
            val m = modelo
            if (m == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                RankingScreen(modelo = m, japones = japones, onVolver = { verRanking = false })
            }
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.navigationBars,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        NavigationBar(
                            modifier = Modifier
                                .widthIn(max = 640.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .shadow(8.dp, RoundedCornerShape(28.dp)),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            tonalElevation = 3.dp,
                            windowInsets = WindowInsets(0.dp),
                        ) {
                            NavigationBarItem(
                                selected = pagerState.currentPage == 0,
                                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                                icon = { TabIcon(R.drawable.ic_tab_compat, pagerState.currentPage == 0) },
                                label = { TabLabel(stringResource(R.string.tab_compat)) },
                                alwaysShowLabel = true,
                            )
                            NavigationBarItem(
                                selected = pagerState.currentPage == 1,
                                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                                icon = { TabIcon(R.drawable.ic_tab_top, pagerState.currentPage == 1) },
                                label = { TabLabel(stringResource(R.string.tab_top)) },
                                alwaysShowLabel = true,
                            )
                            NavigationBarItem(
                                selected = pagerState.currentPage == 2,
                                onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                                icon = { TabIcon(R.drawable.ic_tab_corredora, pagerState.currentPage == 2) },
                                label = { TabLabel(stringResource(R.string.tab_corredora)) },
                                alwaysShowLabel = true,
                            )
                            NavigationBarItem(
                                selected = pagerState.currentPage == 3,
                                onClick = { scope.launch { pagerState.animateScrollToPage(3) } },
                                icon = { TabIcon(R.drawable.ic_tab_elenco, pagerState.currentPage == 3) },
                                label = { TabLabel(stringResource(R.string.tab_elenco)) },
                                alwaysShowLabel = true,
                            )
                            NavigationBarItem(
                                selected = pagerState.currentPage == 4,
                                onClick = { scope.launch { pagerState.animateScrollToPage(4) } },
                                icon = { TabIcon(R.drawable.ic_tab_ajustes, pagerState.currentPage == 4) },
                                label = { TabLabel(stringResource(R.string.tab_mas)) },
                                alwaysShowLabel = true,
                            )
                        }
                    }
                },
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    val m = modelo
                    if (m == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            when (page) {
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
                                1 -> TopLinajesScreen(
                                    modelo = m,
                                    japones = japones,
                                    onVerHerencia = { linaje ->
                                        vm.cargarLinaje(linaje)
                                        scope.launch { pagerState.animateScrollToPage(0) }
                                    },
                                )
                                2 -> CorredoraScreen(
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
                                        scope.launch { pagerState.animateScrollToPage(0) }
                                    },
                                )
                                3 -> ElencoScreen(
                                    modelo = m,
                                    japones = japones,
                                    elenco = elenco,
                                    onToggle = vm::toggleElenco,
                                    onMarcar = vm::marcarElenco,
                                    onLimpiar = vm::limpiarElenco,
                                    onVerHerencia = { linaje ->
                                        vm.cargarLinaje(linaje)
                                        scope.launch { pagerState.animateScrollToPage(0) }
                                    },
                                    onVolver = null,
                                )
                                else -> SettingsScreen(
                                    modoGrilla = modoGrilla,
                                    onModoGrilla = vm::setModoGrilla,
                                    tema = tema,
                                    onTema = vm::setTema,
                                    idioma = idioma,
                                    onIdioma = vm::setIdioma,
                                    modelo = m,
                                    japones = japones,
                                    arboles = arboles,
                                    onAbrirArbol = { a ->
                                        vm.abrirArbol(a)
                                        scope.launch { pagerState.animateScrollToPage(2) }
                                    },
                                    onEliminarArbol = vm::eliminarArbol,
                                    onAbrirGrupos = { verGrupos = true },
                                    onAbrirRanking = { verRanking = true },
                                    onAbrirElenco = { scope.launch { pagerState.animateScrollToPage(3) } },
                                )
                            }
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

/* Label de tab que se achica hasta caber (7..11sp) antes de truncar. */
@Composable
private fun TabLabel(texto: String) {    BasicText(
        text = texto,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepDown(minFontSize = 7.sp, maxFontSize = 11.sp),
        style = MaterialTheme.typography.labelSmall.copy(
            textAlign = TextAlign.Center,
            color = LocalContentColor.current,
        ),
    )
}

/* Icono de tab con transición animada de color al seleccionar. */
@Composable
private fun TabIcon(id: Int, selected: Boolean) {
    val tint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "tintTab",
    )
    Icon(painterResource(id), contentDescription = null, tint = tint)
}

