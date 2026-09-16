package com.maximillionsnyder.umafinidad.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.maximillionsnyder.umafinidad.R

/* Notificación persistente del servicio en primer plano de la burbuja:
   mantiene viva la ventana del overlay y ofrece ocultar la burbuja. */
internal object NotificacionBurbuja {

    const val CANAL = "burbuja_acceso_rapido"
    const val ID_NOTIFICACION = 4101
    const val ACCION_OCULTAR = "com.maximillionsnyder.umafinidad.OCULTAR_BURBUJA"

    fun crearCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            contexto.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    CANAL,
                    contexto.getString(R.string.burbuja_canal),
                    NotificationManager.IMPORTANCE_LOW,
                ),
            )
        }
    }

    fun construir(contexto: Context, abrir: PendingIntent, ocultar: PendingIntent): Notification =
        NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(R.drawable.ic_tab_compat)
            .setContentTitle(contexto.getString(R.string.burbuja_notif_titulo))
            .setContentText(contexto.getString(R.string.burbuja_notif_texto))
            .setContentIntent(abrir)
            .addAction(0, contexto.getString(R.string.burbuja_ocultar), ocultar)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
}
