package com.maximillionsnyder.umafinidad.overlay

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.maximillionsnyder.umafinidad.MainActivity
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.AffinityRepository
import com.maximillionsnyder.umafinidad.data.PrefsRepository
import com.maximillionsnyder.umafinidad.ui.Destino
import com.maximillionsnyder.umafinidad.ui.componentes.LocalEstiloAvatar
import com.maximillionsnyder.umafinidad.ui.theme.UmaAfinidadTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/* Burbuja flotante de acceso rápido (estilo grabador de pantalla): una
   ventana de overlay arrastrable que al tocarla abre un panel lateral con la
   calculadora de afinidad y atajos a la app.

   La burbuja y la zona de descarte se crean una sola vez por servicio; el
   panel se monta en una ventana nueva en cada apertura (una ventana recién
   creada es la que engancha el teclado del buscador). */
class BurbujaService : Service() {

    companion object {
        fun iniciar(context: Context) {
            /* No arrancar con la pref apagada (race al alternar el interruptor):
               un startForegroundService sin startForeground posterior puede
               tumbar la app en Android 8+. */
            if (!PrefsRepository(context).burbujaActiva) return
            ContextCompat.startForegroundService(
                context,
                Intent(context, BurbujaService::class.java),
            )
        }

        fun detener(context: Context) {
            context.stopService(Intent(context, BurbujaService::class.java))
        }
    }

    private lateinit var ventanas: WindowManager
    private val anfitrion = AnfitrionOverlay()
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val prefs by lazy { PrefsRepository(this) }

    /* Estado de la calculadora, independiente de las ventanas. */
    private val estado by lazy { EstadoBurbuja(AffinityRepository(applicationContext), alcance) }

    /* Resaltado de la zona de descarte mientras se arrastra la burbuja. */
    private val sobreQuitar = MutableStateFlow(false)


    private var vistaBurbuja: ComposeView? = null
    private var vistaPanel: ComposeView? = null
    private var vistaQuitar: ComposeView? = null
    private lateinit var parametrosBurbuja: WindowManager.LayoutParams
    private var parametrosPanel: WindowManager.LayoutParams? = null
    private lateinit var parametrosQuitar: WindowManager.LayoutParams
    private var animacionIman: Job? = null

    /* Tamaño actual del círculo, en dp; la vista Compose lo observa. */
    private val tamanoBurbujaDp = MutableStateFlow(TAMANO_BURBUJA_DP)
    private var escuchaTamano: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private var tamanoBurbuja = 0
    private var margen = 0
    private var tamanoQuitar = 0
    private var margenQuitar = 0
    private var agarreQuitar = 0

    override fun onCreate() {
        super.onCreate()
        if (!prefs.burbujaActiva) {
            stopSelf()
            return
        }
        ventanas = getSystemService(WINDOW_SERVICE) as WindowManager
        tamanoBurbujaDp.value = prefs.tamanoBurbuja.dp
        tamanoBurbuja = dp(tamanoBurbujaDp.value)
        margen = dp(MARGEN_BURBUJA_DP)
        tamanoQuitar = dp(TAMANO_QUITAR_DP)
        margenQuitar = dp(MARGEN_QUITAR_DP)
        agarreQuitar = dp(AGARRE_QUITAR_DP)
        anfitrion.crear()
        iniciarForeground()
        crearBurbuja()
        crearZonaQuitar()
        escuchaTamano = prefs.observarTamanoBurbuja { aplicarTamanoBurbuja() }
        estado.cargar()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == NotificacionBurbuja.ACCION_OCULTAR) {
            ocultarBurbuja()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /* Al girar la pantalla se reacomodan la burbuja y la franja abierta. */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val pantalla = tamanoPantalla()

        animacionIman?.cancel()
        /* El panel se monta con el tamaño de pantalla del momento: si estaba
           abierto, se cierra para que la próxima apertura use el nuevo. */
        if (vistaPanel != null) quitarPanel()

        if (vistaQuitar != null) {
            parametrosQuitar = crearParametrosQuitar()
            sobreQuitar.value = false
            actualizarVentana(vistaQuitar, parametrosQuitar)
        }

        vistaBurbuja?.let { vista ->
            val acotada = PosicionBurbuja.acotar(
                parametrosBurbuja.x, parametrosBurbuja.y,
                pantalla.x, pantalla.y, tamanoBurbuja, margen,
            )
            moverVentanaBurbuja(vista, acotada.x, acotada.y)
        }
    }

    override fun onDestroy() {
        animacionIman?.cancel()
        escuchaTamano?.let { prefs.dejarDeObservar(it) }
        escuchaTamano = null
        alcance.cancel()
        listOf(vistaBurbuja, vistaPanel, vistaQuitar).forEach { vista ->
            vista?.let { runCatching { ventanas.removeView(it) } }
        }
        vistaBurbuja = null
        vistaPanel = null
        vistaQuitar = null
        anfitrion.destruir()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    /* ===== Ventanas ===== */

    /* Último estado aplicado de cada ventana: los reacomodos (giro de
       pantalla, reabrir el panel) no repiten la transacción si nada cambió,
       porque updateViewLayout es una llamada al sistema, no un setter.
       Se comparan también los flags: mostrar un panel que no se movió sigue
       necesitando que la ventana vuelva a aceptar toques. */
    private val ultimoEstado = HashMap<View, EstadoVentana>()
    private data class EstadoVentana(val x: Int, val y: Int, val flags: Int)

    private fun actualizarVentana(vista: View?, parametros: WindowManager.LayoutParams) {
        vista ?: return
        val estado = EstadoVentana(parametros.x, parametros.y, parametros.flags)
        if (ultimoEstado[vista] == estado) return
        ultimoEstado[vista] = estado
        runCatching { ventanas.updateViewLayout(vista, parametros) }
    }

    private fun moverVentanaBurbuja(vista: View, x: Int, y: Int) {
        if (parametrosBurbuja.x == x && parametrosBurbuja.y == y) return
        parametrosBurbuja.x = x
        parametrosBurbuja.y = y
        runCatching { ventanas.updateViewLayout(vista, parametrosBurbuja) }
    }

    /* ===== Burbuja ===== */

    private fun crearBurbuja() {
        val vista = crearComposeView()
        val pantalla = tamanoPantalla()
        val posicion = posicionInicial(pantalla)

        parametrosBurbuja = WindowManager.LayoutParams(
            tamanoBurbuja,
            tamanoBurbuja,
            tipoVentana(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = posicion.x
            y = posicion.y
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        val descripcion = getString(R.string.burbuja_descripcion)
        vista.setContent {
            val tamanoDp by tamanoBurbujaDp.collectAsState()
            BurbujaContenido(
                tamanoDp = tamanoDp,
                descripcion = descripcion,
                onTap = { alternarPanel() },
                onIniciarArrastre = ::iniciarArrastre,
                onMover = ::moverBurbuja,
                onSoltar = ::soltarBurbuja,
            )
        }

        val agregada = runCatching { ventanas.addView(vista, parametrosBurbuja) }.isSuccess
        if (!agregada) {
            stopSelf()
            return
        }
        vistaBurbuja = vista
    }

    private fun posicionInicial(pantalla: Point): Posicion {
        val guardada = if (prefs.burbujaX >= 0 && prefs.burbujaY >= 0) {
            Posicion(prefs.burbujaX, prefs.burbujaY)
        } else {
            Posicion(
                x = pantalla.x - tamanoBurbuja - margen,
                y = (pantalla.y * 0.42f).toInt(),
            )
        }
        return PosicionBurbuja.acotar(
            guardada.x, guardada.y, pantalla.x, pantalla.y, tamanoBurbuja, margen,
        )
    }

    /* El gesto acumula desplazamientos en Float y el servicio los aplica a la
       ventana: sin recomposición y sin arrastrar errores de redondeo. */
    private var arrastre: ArrastreBurbuja? = null

    private fun iniciarArrastre() {
        animacionIman?.cancel()
        /* El panel no se cierra por toques afuera; arrastrar la burbuja sí,
           para no quedar con la franja y la burbuja superpuestas. */
        if (vistaPanel != null) quitarPanel()
        val pantalla = tamanoPantalla()
        arrastre = ArrastreBurbuja(
            origenX = parametrosBurbuja.x,
            origenY = parametrosBurbuja.y,
            tamano = tamanoBurbuja,
            margen = margen,
            pantalla = Posicion(pantalla.x, pantalla.y),
        ).also { it.iniciar() }
        mostrarQuitar()
    }

    private fun moverBurbuja(dx: Float, dy: Float) {
        val gesto = arrastre ?: return
        gesto.mover(dx, dy)
        val vista = vistaBurbuja ?: return
        val acotada = gesto.posicion
        moverVentanaBurbuja(vista, acotada.x, acotada.y)

        /* Mientras se arrastra, resalta la zona si la burbuja está encima. */
        sobreQuitar.value = vistaQuitar != null && ZonaQuitar.sobre(
            acotada.x, acotada.y, tamanoBurbuja,
            parametrosQuitar.x, parametrosQuitar.y, tamanoQuitar, agarreQuitar,
        )
    }

    /* Al soltar: si quedó sobre la X se quita de pantalla; si no, se pega al
       borde más cercano con una animación corta y se recuerda la posición. */
    private fun soltarBurbuja() {
        arrastre?.terminar()
        arrastre = null
        val vista = vistaBurbuja ?: return

        val sobreZona = vistaQuitar != null && ZonaQuitar.sobre(
            parametrosBurbuja.x, parametrosBurbuja.y, tamanoBurbuja,
            parametrosQuitar.x, parametrosQuitar.y, tamanoQuitar, agarreQuitar,
        )
        ocultarQuitar()
        if (sobreZona) {
            ocultarBurbuja()
            return
        }

        val pasos = pasosIman(
            parametrosBurbuja.x, parametrosBurbuja.y,
            tamanoPantalla().x, tamanoBurbuja, margen,
        )
        animacionIman = alcance.launch {
            for (paso in pasos) {
                moverVentanaBurbuja(vista, paso.x, paso.y)
                delay(RETARDO_FOTOGRAMA_MS)
            }
            prefs.burbujaX = parametrosBurbuja.x
            prefs.burbujaY = parametrosBurbuja.y
        }
    }

    /* El tamaño del círculo se elige en Ajustes: se aplica en vivo y se
       reacomoda la burbuja pegada a su borde (puede haber cambiado de
       tamaño y ya no entrar donde estaba). */
    private fun aplicarTamanoBurbuja() {
        val nuevoDp = prefs.tamanoBurbuja.dp
        if (nuevoDp == tamanoBurbujaDp.value) return
        tamanoBurbujaDp.value = nuevoDp
        tamanoBurbuja = dp(nuevoDp)
        val vista = vistaBurbuja ?: return
        val pantalla = tamanoPantalla()
        val acotada = PosicionBurbuja.acotar(
            parametrosBurbuja.x, parametrosBurbuja.y,
            pantalla.x, pantalla.y, tamanoBurbuja, margen,
        )
        val x = PosicionBurbuja.iman(acotada.x, pantalla.x, tamanoBurbuja, margen)
        parametrosBurbuja.width = tamanoBurbuja
        parametrosBurbuja.height = tamanoBurbuja
        parametrosBurbuja.x = x
        parametrosBurbuja.y = acotada.y
        runCatching { ventanas.updateViewLayout(vista, parametrosBurbuja) }
        prefs.burbujaX = x
        prefs.burbujaY = acotada.y
    }

    /* Apaga la burbuja: se recupera desde Ajustes. */
    private fun ocultarBurbuja() {
        prefs.burbujaActiva = false
        stopSelf()
    }

    /* ===== Zona de descarte ===== */

    private fun crearZonaQuitar() {
        val vista = crearComposeView()
        parametrosQuitar = crearParametrosQuitar()
        val descripcion = getString(R.string.burbuja_quitar_zona)
        vista.setContent {
            val activo by sobreQuitar.collectAsState()
            ObjetivoQuitar(descripcion = descripcion, activo = activo)
        }
        vista.visibility = View.GONE
        val agregada = runCatching { ventanas.addView(vista, parametrosQuitar) }.isSuccess
        if (agregada) vistaQuitar = vista
    }

    /* La X existe siempre pero solo se ve al arrastrar: mostrarla u ocultarla
       cuesta una propiedad de la vista, no una ventana nueva. */
    private fun mostrarQuitar() {
        sobreQuitar.value = false
        vistaQuitar?.visibility = View.VISIBLE
    }

    private fun ocultarQuitar() {
        sobreQuitar.value = false
        vistaQuitar?.visibility = View.GONE
    }

    /* Ventana de la X: nunca recibe toques (solo es referencia visual y de
       soltado); la app de fondo sigue interactiva. */
    private fun crearParametrosQuitar(): WindowManager.LayoutParams {
        val pantalla = tamanoPantalla()
        val centro = ZonaQuitar.centro(pantalla.x, pantalla.y, tamanoQuitar, margenQuitar)
        return WindowManager.LayoutParams(
            tamanoQuitar,
            tamanoQuitar,
            tipoVentana(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = centro.x
            y = centro.y
        }
    }

    /* ===== Panel ===== */

    /* El panel se monta en una ventana nueva cada vez que se abre y se
       desmonta al cerrarlo. Es a propósito: el buscador necesita el teclado, y
       una ventana de overlay recién creada es la que engancha el IME. Lo que
       se conserva entre aperturas es el estado (búsqueda, selección,
       sugerencias), que vive en EstadoBurbuja, no la vista. */
    private fun alternarPanel() {
        if (vistaPanel != null) quitarPanel() else abrirPanel()
    }

    private fun abrirPanel() {
        if (vistaPanel != null) return
        val vista = crearComposeView()
        parametrosPanel = crearParametrosPanel()
        val parametros = parametrosPanel ?: return

        val japones = resources.configuration.locales[0].language == "ja"
        val translucido = prefs.panelTranslucido

        vista.setContent {
            val modeloActual by estado.modelo.collectAsState()
            val seleccionActual by estado.seleccion.collectAsState()
            val calculando by estado.autocompletando.collectAsState()
            val filtroActual by estado.filtro.collectAsState()
            val avisoActual by estado.aviso.collectAsState()
            val sugerenciasActuales by estado.sugerencias.collectAsState(initial = emptyList())
            val slotDestinoActual by estado.slotDestino.collectAsState()
            val pantalla = tamanoPantalla()
            val ladoDerecho = PosicionBurbuja.enLadoDerecho(
                parametrosBurbuja.x, pantalla.x, tamanoBurbuja,
            )
            UmaAfinidadTheme(
                tema = prefs.tema,
                tamanoTexto = prefs.tamanoTexto,
                negrita = prefs.textoNegrita,
            ) {
                CompositionLocalProvider(LocalEstiloAvatar provides prefs.estiloAvatar) {
                    PanelBurbuja(
                        modelo = modeloActual,
                        japones = japones,
                        seleccion = seleccionActual,
                        filtro = filtroActual,
                        aviso = avisoActual,
                        sugerencias = sugerenciasActuales,
                        autocompletando = calculando,
                        ladoDerecho = ladoDerecho,
                        translucido = translucido,
                        slotDestino = slotDestinoActual,
                        onFiltro = estado::buscar,
                        onAlternar = estado::alternar,
                        onSlot = estado::tocarSlot,
                        onLimpiar = estado::limpiar,
                        onAutocompletar = estado::autocompletar,
                        onRedimensionar = ::redimensionarPanel,
                        onFinRedimension = ::guardarTamanoPanel,
                        onCerrar = { quitarPanel() },
                        onOcultar = ::ocultarBurbuja,
                        onAbrirDestino = { destino ->
                            quitarPanel()
                            abrirApp(destino)
                        },
                    )
                }
            }
        }

        vista.setOnKeyListener { _, codigo, evento ->
            if (codigo == KeyEvent.KEYCODE_BACK && evento.action == KeyEvent.ACTION_UP) {
                quitarPanel()
                true
            } else {
                false
            }
        }

        val agregada = runCatching { ventanas.addView(vista, parametros) }.isSuccess
        if (!agregada) return
        vistaPanel = vista
        /* La ventana es focusable (buscador + atrás): pide el foco y con eso
           sale el teclado, como en cualquier ventana de la app. */
        vista.isFocusableInTouchMode = true
        vista.requestFocus()
    }

    /* Franja en el borde opuesto a la burbuja, centrada y con paso de toques
       hacia la app de fondo. Los toques de afuera no la cierran: solo se
       cierra de forma explícita (burbuja, X, Atrás, Ocultar o giro). Si el
       usuario la redimensionó, manda el tamaño guardado en dp. */
    private fun crearParametrosPanel(): WindowManager.LayoutParams {
        val pantalla = tamanoPantalla()
        val minAncho = dp(ANCHO_PANEL_MIN_DP)
        val maxAncho = PosicionPanel.maxAncho(pantalla.x, tamanoBurbuja, margen, minAncho)
        val anchoPanel = prefs.panelAnchoDp.takeIf { it > 0 }?.coerceIn(minAncho, maxAncho)
            ?: PosicionPanel.ancho(
                pantalla.x,
                FRACCION_ANCHO_PANEL,
                minAncho,
                dp(ANCHO_PANEL_MAX_DP),
            )
        val minAlto = dp(ALTO_PANEL_MIN_DP)
        val maxAlto = (pantalla.y - 2 * margen).coerceAtLeast(minAlto)
        val altoPanel = prefs.panelAltoDp.takeIf { it > 0 }?.coerceIn(minAlto, maxAlto)
            ?: (pantalla.y * FRACCION_ALTO_PANEL).toInt()
        val burbujaDerecha = PosicionBurbuja.enLadoDerecho(
            parametrosBurbuja.x, pantalla.x, tamanoBurbuja,
        )
        val posicion = PosicionPanel.calcular(
            pantalla.x, pantalla.y, anchoPanel, altoPanel, margen, burbujaDerecha,
        )

        return WindowManager.LayoutParams(
            anchoPanel,
            altoPanel,
            tipoVentana(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = posicion.x
            y = posicion.y
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    /* Arrastre de la manija del panel: cambia ancho y alto en vivo, con el
       borde superior anclado (la franja no se recentra durante el gesto). */
    private fun redimensionarPanel(dx: Float, dy: Float) {
        val vista = vistaPanel ?: return
        val parametros = parametrosPanel ?: return
        val pantalla = tamanoPantalla()
        val burbujaDerecha = PosicionBurbuja.enLadoDerecho(
            parametrosBurbuja.x, pantalla.x, tamanoBurbuja,
        )
        val tamano = PosicionPanel.redimensionar(
            anchoActual = parametros.width,
            altoActual = parametros.height,
            dx = dx,
            dy = dy,
            pantallaAncho = pantalla.x,
            pantallaAlto = pantalla.y,
            y = parametros.y,
            margen = margen,
            burbujaDerecha = burbujaDerecha,
            tamanoBurbuja = tamanoBurbuja,
            minAncho = dp(ANCHO_PANEL_MIN_DP),
            minAlto = dp(ALTO_PANEL_MIN_DP),
        )
        if (tamano.ancho == parametros.width && tamano.alto == parametros.height) return
        parametros.width = tamano.ancho
        parametros.height = tamano.alto
        parametros.x = PosicionPanel.x(pantalla.x, tamano.ancho, margen, burbujaDerecha)
        runCatching { ventanas.updateViewLayout(vista, parametros) }
    }

    /* Al soltar la manija se recuerda el tamaño para las próximas aperturas. */
    private fun guardarTamanoPanel() {
        val parametros = parametrosPanel ?: return
        prefs.panelAnchoDp = pxADp(parametros.width)
        prefs.panelAltoDp = pxADp(parametros.height)
    }

    private fun quitarPanel() {
        vistaPanel?.let { vista ->
            runCatching { ventanas.removeView(vista) }
        }
        vistaPanel = null
        parametrosPanel = null
    }

    private fun abrirApp(destino: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(Destino.EXTRA, destino)
        }
        startActivity(intent)
    }

    /* ===== Infraestructura ===== */

    private fun crearComposeView(): ComposeView {
        val vista = ComposeView(this)
        vista.setViewTreeLifecycleOwner(anfitrion)
        vista.setViewTreeViewModelStoreOwner(anfitrion)
        vista.setViewTreeSavedStateRegistryOwner(anfitrion)
        return vista
    }

    private fun iniciarForeground() {
        NotificacionBurbuja.crearCanal(this)

        val abrir = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val ocultar = PendingIntent.getService(
            this, 1,
            Intent(this, BurbujaService::class.java).setAction(NotificacionBurbuja.ACCION_OCULTAR),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notificacion = NotificacionBurbuja.construir(this, abrir, ocultar)
        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NotificacionBurbuja.ID_NOTIFICACION, notificacion, tipo)
    }

    private fun tipoVentana(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE
    }

    private fun tamanoPantalla(): Point = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val limites = ventanas.currentWindowMetrics.bounds
        Point(limites.width(), limites.height())
    } else {
        val punto = Point()
        @Suppress("DEPRECATION")
        ventanas.defaultDisplay.getRealSize(punto)
        punto
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()

    private fun pxADp(valor: Int): Int = (valor / resources.displayMetrics.density).roundToInt()
}
