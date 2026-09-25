package com.maximillionsnyder.umafinidad

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.maximillionsnyder.umafinidad.data.ThemeMode
import com.maximillionsnyder.umafinidad.data.aplicarIdioma
import com.maximillionsnyder.umafinidad.overlay.BurbujaService
import com.maximillionsnyder.umafinidad.ui.AppViewModel
import com.maximillionsnyder.umafinidad.ui.Destino
import com.maximillionsnyder.umafinidad.ui.componentes.BienvenidaAccesibilidad
import com.maximillionsnyder.umafinidad.ui.componentes.ClavesTransicion
import com.maximillionsnyder.umafinidad.ui.componentes.LocalAnimatedVisibilityScope
import com.maximillionsnyder.umafinidad.ui.componentes.LocalEstiloAvatar
import com.maximillionsnyder.umafinidad.ui.componentes.LocalSharedTransitionScope
import com.maximillionsnyder.umafinidad.ui.compat.CompatScreen
import com.maximillionsnyder.umafinidad.ui.corredora.CorredoraScreen
import com.maximillionsnyder.umafinidad.ui.elenco.ElencoScreen
import com.maximillionsnyder.umafinidad.ui.groups.GroupsScreen
import com.maximillionsnyder.umafinidad.ui.pro.ProScreen
import com.maximillionsnyder.umafinidad.ui.ranking.ModoRanking
import com.maximillionsnyder.umafinidad.ui.ranking.RankingScreen
import com.maximillionsnyder.umafinidad.ui.settings.SettingsScreen
import com.maximillionsnyder.umafinidad.ui.theme.UmaAfinidadTheme
import com.maximillionsnyder.umafinidad.ui.theme.fondoGradiente
import com.maximillionsnyder.umafinidad.ui.top.TopLinajesScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm by viewModels<AppViewModel>()

    /* Destino pedido por la burbuja flotante (p. ej. "tab:2"). */
    private val destinoPendiente = mutableStateOf<String?>(null)

    /* Se pidió "Mostrar sobre otras apps" y se espera el regreso de Ajustes. */
    private var esperandoPermisoOverlay = false

    private val lanzadorNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (vm.burbujaActiva.value && Settings.canDrawOverlays(this)) {
                BurbujaService.iniciar(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica idioma guardado antes de inflar para que resources ya estén localizados
        try {
            val prefs = com.maximillionsnyder.umafinidad.data.PrefsRepository(this)
            aplicarIdioma(prefs.idioma)
        } catch (_: Exception) {}
        super.onCreate(savedInstanceState)
        destinoPendiente.value = intent?.getStringExtra(Destino.EXTRA)
        enableEdgeToEdge()
        setContent {
            val tema by vm.tema.collectAsState()
            val tamanoTexto by vm.tamanoTexto.collectAsState()
            val textoNegrita by vm.textoNegrita.collectAsState()
            val estiloAvatar by vm.estiloAvatar.collectAsState()
            UmaAfinidadTheme(
                tema = tema,
                tamanoTexto = tamanoTexto,
                negrita = textoNegrita,
            ) {
                CompositionLocalProvider(LocalEstiloAvatar provides estiloAvatar) {
                    App(
                        vm = vm,
                        destinoPendiente = destinoPendiente.value,
                        onDestinoConsumido = { destinoPendiente.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        destinoPendiente.value = intent.getStringExtra(Destino.EXTRA)
    }

    override fun onResume() {
        super.onResume()
        /* La burbuja pudo ocultarse desde su notificación: el switch se
           sincroniza y, si sigue activa, el servicio se asegura al volver. */
        vm.refrescarBurbuja()
        if (vm.burbujaActiva.value) {
            if (Settings.canDrawOverlays(this)) {
                BurbujaService.iniciar(this)
            } else if (esperandoPermisoOverlay) {
                /* Volvió de Ajustes sin conceder el permiso: revierte el
                   switch para que el estado sea consistente. */
                vm.setBurbujaActiva(false)
            }
        } else {
            BurbujaService.detener(this)
        }
        esperandoPermisoOverlay = false
    }

    /* Enciende la burbuja pidiendo antes los permisos que falten. */
    fun activarBurbuja() {
        if (!Settings.canDrawOverlays(this)) {
            esperandoPermisoOverlay = true
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"),
                ),
            )
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            lanzadorNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        BurbujaService.iniciar(this)
    }
}

@Composable
private fun App(
    vm: AppViewModel,
    destinoPendiente: String?,
    onDestinoConsumido: () -> Unit,
) {
    val modelo by vm.modelo.collectAsState()
    val seleccion by vm.seleccion.collectAsState()
    val resultado by vm.resultado.collectAsState()
    val modoGrilla by vm.modoGrilla.collectAsState()
    val arboles by vm.arboles.collectAsState()
    val arbolPendiente by vm.arbolPendiente.collectAsState()
    val elenco by vm.elenco.collectAsState()
    val tema by vm.tema.collectAsState()
    val tamanoTexto by vm.tamanoTexto.collectAsState()
    val textoNegrita by vm.textoNegrita.collectAsState()
    val mostrarBienvenida by vm.mostrarBienvenida.collectAsState()
    val burbujaActiva by vm.burbujaActiva.collectAsState()
    val panelTranslucido by vm.panelTranslucido.collectAsState()
    val panelDosColumnas by vm.panelDosColumnas.collectAsState()
    val tamanoBurbuja by vm.tamanoBurbuja.collectAsState()
    val esPro by vm.esPro.collectAsState()
    val codigoPro by vm.codigoPro.collectAsState()
    val activadoEnPro by vm.activadoEnPro.collectAsState()
    val estiloAvatar = LocalEstiloAvatar.current

    val pagerState = rememberPagerState(initialPage = 0) { 5 }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val idioma by vm.idioma.collectAsState()

    LaunchedEffect(idioma) {
        aplicarIdioma(context, idioma)
    }

    /* ¿Está concedido "Mostrar sobre otras apps"? Se revalida al volver
       de los ajustes del sistema. */
    var permisoOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    DisposableEffect(context) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) {
                permisoOverlay = Settings.canDrawOverlays(context)
            }
        }
        (context as? LifecycleOwner)?.lifecycle?.addObserver(observador)
        onDispose { (context as? LifecycleOwner)?.lifecycle?.removeObserver(observador) }
    }

    /* Grupos y Ranking son referencias archivadas: se abren a
       pantalla completa desde Ajustes y el botón atrás las cierra. Mis Umas ya es tab. */
    var verGrupos by rememberSaveable { mutableStateOf(false) }
    var verRanking by rememberSaveable { mutableStateOf(false) }
    var verRankingPadres by rememberSaveable { mutableStateOf(false) }
    var verPro by rememberSaveable { mutableStateOf(false) }

    /* Pila de navegación ("tab:N", "grupos", "ranking", "ranking-padres", "pro"):
       atrás desapila hasta volver al inicio y recién ahí pregunta si salir. */
    val historial = rememberSaveable { mutableStateListOf("tab:0") }

    /* Aplica un destino de la pila (flags de overlay + página del pager). */
    fun aplicarDestino(destino: String) {
        verGrupos = destino == "grupos"
        verRanking = destino == "ranking"
        verRankingPadres = destino == "ranking-padres"
        verPro = destino == Destino.PRO
        Destino.pagina(destino)?.let { pagina ->
            if (pagerState.currentPage != pagina) scope.launch { pagerState.animateScrollToPage(pagina) }
        }
    }

    /* Abrir un overlay apilándolo (si no es el tope actual). */
    fun irA(destino: String) {
        if (historial.last() == destino) return
        historial.add(destino)
        aplicarDestino(destino)
    }

    /* Retrocede una pantalla; false si ya estamos al inicio. */
    fun volver(): Boolean {
        if (historial.size <= 1) return false
        historial.removeLast()
        aplicarDestino(historial.last())
        return true
    }

    /* Un atajo del panel de la burbuja flotante pide abrir un destino. */
    LaunchedEffect(destinoPendiente) {
        if (destinoPendiente != null) {
            irA(destinoPendiente)
            onDestinoConsumido()
        }
    }

    /* Registra taps, swipes y saltos (herencias, árbol pendiente): toda
       página donde el pager se asienta suma historial, salvo que un overlay
       esté abierto o que ya sea el tope (evita duplicados y loops al volver).
       settledPage (y no currentPage) para no apilar intermedias de la animación. */
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { pagina ->
            if (!verGrupos && !verRanking && !verRankingPadres && !verPro) {
                val destino = "tab:$pagina"
                if (historial.last() != destino) historial.add(destino)
            }
        }
    }

    /* Aviso antes de salir: solo cuando la pila está en su inicio; antes
       de eso, atrás siempre vuelve a la pantalla anterior. */
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    BackHandler {
        if (!volver()) confirmarSalida = true
    }

    val japones = LocalConfiguration.current.locales[0].language == "ja"
    val esOscuro = when (tema) {
        ThemeMode.CLARO, ThemeMode.ALTO_CONTRASTE -> false
        ThemeMode.OSCURO -> true
        ThemeMode.SISTEMA -> isSystemInDarkTheme()
    }

    /* Una config pedida desde Ajustes abre Mi corredora. */
    LaunchedEffect(arbolPendiente) {
        if (arbolPendiente != null) pagerState.animateScrollToPage(2)
    }

    /* Fondo con gradiente en toda la app. */
    Box(modifier = Modifier.fillMaxSize().fondoGradiente(esOscuro)) {
        /* Destino activo: null = tabs; overlay = pantalla de referencia.
           SharedTransitionLayout + AnimatedContent dan continuidad visual
           entre la card de Ajustes y la pantalla que abre. */
        val destinoActivo = when {
            verGrupos -> ClavesTransicion.OVERLAY_GRUPOS
            verRanking -> ClavesTransicion.OVERLAY_RANKING
            verRankingPadres -> ClavesTransicion.OVERLAY_RANKING_PADRES
            verPro -> ClavesTransicion.OVERLAY_PRO
            else -> null
        }

        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
            val scopeCompartido = this
            AnimatedContent(
                targetState = destinoActivo,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "destino",
            ) { destino ->
                val scopeVisibilidad = this
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides scopeCompartido,
                    LocalAnimatedVisibilityScope provides scopeVisibilidad,
                ) {
                    if (destino == ClavesTransicion.OVERLAY_PRO) {
                        ProScreen(
                            esPro = esPro,
                            codigo = codigoPro,
                            activadoEn = activadoEnPro,
                            onActivar = vm::activarPro,
                            onDesactivar = vm::desactivarPro,
                            onVolver = { volver() },
                        )
                    } else if (destino == ClavesTransicion.OVERLAY_GRUPOS) {
                        val m = modelo
                        if (m == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            GroupsScreen(modelo = m, japones = japones, onVolver = { volver() })
                        }
                    } else if (destino == ClavesTransicion.OVERLAY_RANKING) {
                        val m = modelo
                        if (m == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            RankingScreen(
                                modelo = m,
                                japones = japones,
                                onVolver = { volver() },
                                claveOverlay = ClavesTransicion.OVERLAY_RANKING,
                                esPro = esPro,
                                onIrAPro = { irA(Destino.PRO) },
                            )
                        }
                    } else if (destino == ClavesTransicion.OVERLAY_RANKING_PADRES) {
                        val m = modelo
                        if (m == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            RankingScreen(
                                modelo = m,
                                japones = japones,
                                onVolver = { volver() },
                                modoInicial = ModoRanking.PADRES,
                                claveOverlay = ClavesTransicion.OVERLAY_RANKING_PADRES,
                                esPro = esPro,
                                onIrAPro = { irA(Destino.PRO) },
                            )
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
                                                onGuardarArbol = { hijoId, nombre, sel, total ->
                                                    vm.guardarArbol(hijoId, nombre, sel, total)
                                                },
                                                onEliminarArbol = vm::eliminarArbol,
                                                onConsumirPendiente = vm::consumirArbolPendiente,
                                                avisar = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                                                onVerHerencia = { sel ->
                                                    vm.cargarSeleccion(sel)
                                                    scope.launch { pagerState.animateScrollToPage(0) }
                                                },
                                                esPro = esPro,
                                                onIrAPro = { irA(Destino.PRO) },
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
                                                estiloAvatar = estiloAvatar,
                                                onEstiloAvatar = vm::setEstiloAvatar,
                                                tema = tema,
                                                onTema = vm::setTema,
                                                idioma = idioma,
                                                onIdioma = vm::setIdioma,
                                                burbujaActiva = burbujaActiva,
                                                burbujaPermiso = permisoOverlay,
                                                onBurbuja = { activo ->
                                                    vm.setBurbujaActiva(activo)
                                                    if (activo) {
                                                        (context as? MainActivity)?.activarBurbuja()
                                                    } else {
                                                        BurbujaService.detener(context)
                                                    }
                                                },
                                                panelTranslucido = panelTranslucido,
                                                onPanelTranslucido = vm::setPanelTranslucido,
                                                dosColumnas = panelDosColumnas,
                                                onDosColumnas = vm::setPanelDosColumnas,
                                                tamanoBurbuja = tamanoBurbuja,
                                                onTamanoBurbuja = vm::setTamanoBurbuja,
                                                onRestablecerTamanos = vm::restablecerTamanos,
                                                modelo = m,
                                                japones = japones,
                                                arboles = arboles,
                                                onAbrirArbol = { a ->
                                                    vm.abrirArbol(a)
                                                    scope.launch { pagerState.animateScrollToPage(2) }
                                                },
                                                onEliminarArbol = vm::eliminarArbol,
                                                onAbrirGrupos = { irA("grupos") },
                                                onAbrirRanking = { irA("ranking") },
                                                onAbrirRankingPadres = { irA("ranking-padres") },
                                                esPro = esPro,
                                                onAbrirPro = { irA(Destino.PRO) },
                                                tamanoTexto = tamanoTexto,
                                                onTamanoTexto = vm::setTamanoTexto,
                                                textoNegrita = textoNegrita,
                                                onTextoNegrita = vm::setTextoNegrita,
                                                onAbrirBienvenida = vm::abrirBienvenida,
                                                onAbrirElenco = { scope.launch { pagerState.animateScrollToPage(3) } },
                                            )
                                        }
                                    }
                                }
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

        /* Bienvenida de accesibilidad: primer inicio o reapertura desde Ajustes. */
        if (mostrarBienvenida) {
            BienvenidaAccesibilidad(
                tema = tema,
                onTema = vm::setTema,
                tamanoTexto = tamanoTexto,
                onTamanoTexto = vm::setTamanoTexto,
                textoNegrita = textoNegrita,
                onTextoNegrita = vm::setTextoNegrita,
                onGuardar = vm::confirmarBienvenida,
                onOmitir = vm::omitirBienvenida,
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
        autoSize = TextAutoSize.StepBased(minFontSize = 7.sp, maxFontSize = 11.sp),
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

