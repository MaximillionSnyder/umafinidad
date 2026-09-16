package com.maximillionsnyder.umafinidad.overlay

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
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
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.ui.Destino
import com.maximillionsnyder.umafinidad.ui.componentes.LocalEstiloAvatar
import com.maximillionsnyder.umafinidad.ui.theme.UmaAfinidadTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/* Burbuja flotante de acceso rápido (estilo grabador de pantalla): una
   ventana de overlay arrastrable que al tocarla abre un panel lateral con
   la calculadora de afinidad y atajos a la app. */
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
    private val modelo = MutableStateFlow<AffinityModel?>(null)

    /* Selección de la calculadora: vive en el servicio para que el panel la
       recuerde al cerrarse y reabrirse mientras la burbuja siga activa. */
    private val trio = MutableStateFlow(TrioEstado())
    /* Resaltado de la zona de descarte mientras se arrastra la burbuja. */
    private val sobreQuitar = MutableStateFlow(false)

    private var vistaBurbuja: ComposeView? = null
    private var vistaPanel: ComposeView? = null
    private var vistaQuitar: ComposeView? = null
    private lateinit var parametrosBurbuja: WindowManager.LayoutParams
    private lateinit var parametrosQuitar: WindowManager.LayoutParams

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
        tamanoBurbuja = dp(TAMANO_BURBUJA_DP)
        margen = dp(MARGEN_BURBUJA_DP)
        tamanoQuitar = dp(TAMANO_QUITAR_DP)
        margenQuitar = dp(MARGEN_QUITAR_DP)
        agarreQuitar = dp(AGARRE_QUITAR_DP)
        anfitrion.crear()
        iniciarForeground()
        crearBurbuja()
        alcance.launch(Dispatchers.Default) {
            modelo.value = AffinityRepository(applicationContext).modelo
        }
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

        if (vistaPanel != null) {
            val parametros = parametrosPanel()
            vistaPanel?.let { runCatching { ventanas.updateViewLayout(it, parametros) } }
        }

        if (vistaQuitar != null) {
            parametrosQuitar = crearParametrosQuitar()
            sobreQuitar.value = false
            vistaQuitar?.let { runCatching { ventanas.updateViewLayout(it, parametrosQuitar) } }
        }

        vistaBurbuja?.let { vista ->
            val acotada = PosicionBurbuja.acotar(
                parametrosBurbuja.x, parametrosBurbuja.y,
                pantalla.x, pantalla.y, tamanoBurbuja, margen,
            )
            parametrosBurbuja.x = acotada.x
            parametrosBurbuja.y = acotada.y
            runCatching { ventanas.updateViewLayout(vista, parametrosBurbuja) }
        }
    }

    override fun onDestroy() {
        alcance.cancel()
        quitarPanel()
        ocultarQuitar()
        vistaBurbuja?.let { vista ->
            runCatching { ventanas.removeView(vista) }
            vistaBurbuja = null
        }
        anfitrion.destruir()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    /* ===== Ventana de la burbuja ===== */

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
            BurbujaContenido(
                descripcion = descripcion,
                onTap = { alternarPanel() },
                onIniciarArrastre = ::mostrarQuitar,
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

    private fun moverBurbuja(dx: Float, dy: Float) {
        val pantalla = tamanoPantalla()
        val acotada = PosicionBurbuja.acotar(
            parametrosBurbuja.x + dx.toInt(),
            parametrosBurbuja.y + dy.toInt(),
            pantalla.x, pantalla.y, tamanoBurbuja, margen,
        )
        parametrosBurbuja.x = acotada.x
        parametrosBurbuja.y = acotada.y
        vistaBurbuja?.let { runCatching { ventanas.updateViewLayout(it, parametrosBurbuja) } }

        /* Mientras se arrastra, resalta la zona si la burbuja está encima. */
        if (vistaQuitar != null) {
            sobreQuitar.value = ZonaQuitar.sobre(
                parametrosBurbuja.x, parametrosBurbuja.y, tamanoBurbuja,
                parametrosQuitar.x, parametrosQuitar.y, tamanoQuitar, agarreQuitar,
            )
        }
    }

    /* Al soltar: si quedó sobre la X se quita de pantalla; si no, se pega al
       borde más cercano y se recuerda la posición. */
    private fun soltarBurbuja() {
        val sobreZona = vistaQuitar != null && ZonaQuitar.sobre(
            parametrosBurbuja.x, parametrosBurbuja.y, tamanoBurbuja,
            parametrosQuitar.x, parametrosQuitar.y, tamanoQuitar, agarreQuitar,
        )
        ocultarQuitar()
        if (sobreZona) {
            ocultarBurbuja()
            return
        }

        val pantalla = tamanoPantalla()
        parametrosBurbuja.x = PosicionBurbuja.iman(
            parametrosBurbuja.x, pantalla.x, tamanoBurbuja, margen,
        )
        prefs.burbujaX = parametrosBurbuja.x
        prefs.burbujaY = parametrosBurbuja.y
        vistaBurbuja?.let { runCatching { ventanas.updateViewLayout(it, parametrosBurbuja) } }
    }

    /* Apaga la burbuja: se recupera desde Ajustes. */
    private fun ocultarBurbuja() {
        prefs.burbujaActiva = false
        stopSelf()
    }

    /* ===== Zona de descarte ===== */

    private fun mostrarQuitar() {
        if (vistaQuitar != null) return
        val vista = crearComposeView()
        parametrosQuitar = crearParametrosQuitar()
        val descripcion = getString(R.string.burbuja_quitar_zona)
        vista.setContent {
            val activo by sobreQuitar.collectAsState()
            ObjetivoQuitar(descripcion = descripcion, activo = activo)
        }
        val agregada = runCatching { ventanas.addView(vista, parametrosQuitar) }.isSuccess
        if (agregada) vistaQuitar = vista
    }

    private fun ocultarQuitar() {
        sobreQuitar.value = false
        vistaQuitar?.let { vista ->
            runCatching { ventanas.removeView(vista) }
        }
        vistaQuitar = null
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

    private fun alternarPanel() {
        if (vistaPanel != null) quitarPanel() else abrirPanel()
    }

    private fun abrirPanel() {
        if (vistaPanel != null) return
        val vista = crearComposeView()
        val parametros = parametrosPanel()

        val tema = prefs.tema
        val tamanoTexto = prefs.tamanoTexto
        val negrita = prefs.textoNegrita
        val estiloAvatar = prefs.estiloAvatar
        val japones = resources.configuration.locales[0].language == "ja"

        vista.setContent {
            val modeloActual by modelo.collectAsState()
            val trioActual by trio.collectAsState()
            UmaAfinidadTheme(tema = tema, tamanoTexto = tamanoTexto, negrita = negrita) {
                CompositionLocalProvider(LocalEstiloAvatar provides estiloAvatar) {
                    PanelBurbuja(
                        modelo = modeloActual,
                        japones = japones,
                        estado = trioActual,
                        onEstado = { trio.value = it },
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

        /* Toque fuera de la franja: cierra el panel sin robarle el evento
           a la app de fondo (FLAG_NOT_TOUCH_MODAL + FLAG_WATCH_OUTSIDE_TOUCH). */
        vista.setOnTouchListener { _, evento ->
            if (evento.action == MotionEvent.ACTION_OUTSIDE) {
                quitarPanel()
                true
            } else {
                false
            }
        }

        val agregada = runCatching { ventanas.addView(vista, parametros) }.isSuccess
        if (agregada) {
            vistaPanel = vista
            /* La ventana es focusable (buscador + atrás): pide el foco. */
            vista.isFocusableInTouchMode = true
            vista.requestFocus()
        }
    }

    /* Franja en el borde opuesto a la burbuja, centrada y con paso de toques
       hacia la app de fondo. Ancho y alto escalan con la pantalla. */
    private fun parametrosPanel(): WindowManager.LayoutParams {
        val pantalla = tamanoPantalla()
        val anchoPanel = PosicionPanel.ancho(
            pantalla.x,
            FRACCION_ANCHO_PANEL,
            dp(ANCHO_PANEL_MIN_DP),
            dp(ANCHO_PANEL_MAX_DP),
        )
        val altoPanel = (pantalla.y * FRACCION_ALTO_PANEL).toInt()
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
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
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

    private fun quitarPanel() {
        vistaPanel?.let { vista ->
            runCatching { ventanas.removeView(vista) }
        }
        vistaPanel = null
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
}
