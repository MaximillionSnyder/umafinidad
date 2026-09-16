package com.maximillionsnyder.umafinidad.overlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.maximillionsnyder.umafinidad.MainActivity
import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.AffinityRepository
import com.maximillionsnyder.umafinidad.data.PrefsRepository
import com.maximillionsnyder.umafinidad.domain.AffinityModel
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
        private const val CANAL = "burbuja_acceso_rapido"
        private const val ID_NOTIFICACION = 4101
        private const val ACCION_OCULTAR = "com.maximillionsnyder.umafinidad.OCULTAR_BURBUJA"

        /* Destinos de navegación, mismo formato que la pila de MainActivity. */
        const val EXTRA_DESTINO = "destino"
        const val DESTINO_COMPAT = "tab:0"
        const val DESTINO_CORREDORA = "tab:2"
        const val DESTINO_ELENCO = "tab:3"
        const val DESTINO_AJUSTES = "tab:4"

        fun iniciar(context: Context) {
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

    private var vistaBurbuja: ComposeView? = null
    private var vistaPanel: ComposeView? = null
    private lateinit var parametrosBurbuja: WindowManager.LayoutParams

    private var tamanoBurbuja = 0
    private var margen = 0

    override fun onCreate() {
        super.onCreate()
        if (!prefs.burbujaActiva) {
            stopSelf()
            return
        }
        ventanas = getSystemService(WINDOW_SERVICE) as WindowManager
        tamanoBurbuja = dp(56)
        margen = dp(8)
        anfitrion.crear()
        iniciarForeground()
        crearBurbuja()
        alcance.launch(Dispatchers.Default) {
            modelo.value = AffinityRepository(applicationContext).modelo
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACCION_OCULTAR) {
            prefs.burbujaActiva = false
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        alcance.cancel()
        quitarPanel()
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
    }

    /* Al soltar, la burbuja se pega al borde más cercano y se recuerda. */
    private fun soltarBurbuja() {
        val pantalla = tamanoPantalla()
        parametrosBurbuja.x = PosicionBurbuja.iman(
            parametrosBurbuja.x, pantalla.x, tamanoBurbuja, margen,
        )
        prefs.burbujaX = parametrosBurbuja.x
        prefs.burbujaY = parametrosBurbuja.y
        vistaBurbuja?.let { runCatching { ventanas.updateViewLayout(it, parametrosBurbuja) } }
    }

    /* ===== Panel ===== */

    private fun alternarPanel() {
        if (vistaPanel != null) quitarPanel() else abrirPanel()
    }

    private fun abrirPanel() {
        if (vistaPanel != null) return
        val vista = crearComposeView()
        val pantalla = tamanoPantalla()
        val ladoDerecho = PosicionBurbuja.enLadoDerecho(
            parametrosBurbuja.x, pantalla.x, tamanoBurbuja,
        )

        val tema = prefs.tema
        val tamanoTexto = prefs.tamanoTexto
        val negrita = prefs.textoNegrita
        val estiloAvatar = prefs.estiloAvatar
        val japones = resources.configuration.locales[0].language == "ja"

        vista.setContent {
            val modeloActual by modelo.collectAsState()
            UmaAfinidadTheme(tema = tema, tamanoTexto = tamanoTexto, negrita = negrita) {
                CompositionLocalProvider(LocalEstiloAvatar provides estiloAvatar) {
                    PanelBurbuja(
                        modelo = modeloActual,
                        japones = japones,
                        ladoDerecho = ladoDerecho,
                        onCerrar = { quitarPanel() },
                        onOcultar = {
                            prefs.burbujaActiva = false
                            stopSelf()
                        },
                        onAbrirDestino = { destino ->
                            quitarPanel()
                            abrirApp(destino)
                        },
                    )
                }
            }
        }

        val parametros = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            tipoVentana(),
            0,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        val agregada = runCatching { ventanas.addView(vista, parametros) }.isSuccess
        if (agregada) vistaPanel = vista
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
            putExtra(EXTRA_DESTINO, destino)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val gestor = getSystemService(NotificationManager::class.java)
            gestor.createNotificationChannel(
                NotificationChannel(
                    CANAL,
                    getString(R.string.burbuja_canal),
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }

        val abrir = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val ocultar = PendingIntent.getService(
            this, 1,
            Intent(this, BurbujaService::class.java).setAction(ACCION_OCULTAR),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notificacion = NotificationCompat.Builder(this, CANAL)
            .setSmallIcon(R.drawable.ic_tab_compat)
            .setContentTitle(getString(R.string.burbuja_notif_titulo))
            .setContentText(getString(R.string.burbuja_notif_texto))
            .setContentIntent(abrir)
            .addAction(0, getString(R.string.burbuja_ocultar), ocultar)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val tipo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, ID_NOTIFICACION, notificacion, tipo)
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

    /* Compose necesita dueños de ciclo de vida aunque no haya Activity. */
    private class AnfitrionOverlay : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

        private val registro = LifecycleRegistry(this)
        private val controladorGuardado = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = registro
        override val viewModelStore = ViewModelStore()
        override val savedStateRegistry: SavedStateRegistry get() = controladorGuardado.savedStateRegistry

        fun crear() {
            controladorGuardado.performRestore(null)
            registro.currentState = Lifecycle.State.CREATED
            registro.currentState = Lifecycle.State.RESUMED
        }

        fun destruir() {
            registro.currentState = Lifecycle.State.DESTROYED
            viewModelStore.clear()
        }
    }
}
