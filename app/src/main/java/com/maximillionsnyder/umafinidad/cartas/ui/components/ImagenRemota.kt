package com.maximillionsnyder.umafinidad.cartas.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.util.Collections

/* Imagen remota mínima (sin dependencias). Tres capas para que las listas de
   1900 filas no vayan pidiendo lo mismo una y otra vez:
     - memoria, medida en bytes (no en número de imágenes: un retrato grande y un
       icono pequeño no ocupan igual);
     - disco, en la caché de la app, para no volver a descargar al reabrir;
     - lista de fallos, para no reintentar en cada scroll lo que no existe. */

private const val CARPETA_CACHE = "imagenes"
private const val MAX_DISCO_BYTES = 80L * 1024 * 1024

private val cacheMemoria = object : LruCache<String, Bitmap>(memoriaMaximaKb()) {
    override fun sizeOf(clave: String, valor: Bitmap): Int = valor.byteCount / 1024
}

private fun memoriaMaximaKb(): Int {
    val disponible = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    return (disponible / 8).coerceIn(4 * 1024, 32 * 1024)
}

private val fallidas: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

/* Cuántas imágenes hay guardadas en memoria (lo enseña Ajustes). */
fun imagenesEnCache(): Int = cacheMemoria.snapshot().size

/* Cuántos archivos hay en la caché de disco. */
fun imagenesEnDisco(context: Context): Int = carpetaCache(context).listFiles()?.size ?: 0

/* Vacía memoria y disco y devuelve cuántas imágenes se descartaron. */
fun limpiarCacheImagenes(context: Context): Int {
    val enMemoria = cacheMemoria.snapshot().size
    cacheMemoria.evictAll()
    fallidas.clear()
    val archivos = carpetaCache(context).listFiles().orEmpty()
    for (archivo in archivos) archivo.delete()
    return enMemoria + archivos.size
}

private fun carpetaCache(context: Context): File =
    File(context.cacheDir, CARPETA_CACHE).apply { mkdirs() }

private fun archivoDe(context: Context, url: String): File =
    File(carpetaCache(context), md5(url))

private fun md5(texto: String): String =
    MessageDigest.getInstance("MD5")
        .digest(texto.toByteArray())
        .joinToString("") { "%02x".format(it) }

/* Deja la caché de disco por debajo del límite, borrando primero lo más viejo. */
private fun recortarDisco(carpeta: File) {
    val archivos = carpeta.listFiles() ?: return
    var total = archivos.sumOf { it.length() }
    if (total <= MAX_DISCO_BYTES) return
    for (archivo in archivos.sortedBy { it.lastModified() }) {
        if (total <= MAX_DISCO_BYTES) break
        total -= archivo.length()
        archivo.delete()
    }
}

/* Decodifica sin pasarse de tamaño: un retrato de 1024 px para una fila de 48 dp
   ocupa 20 veces más memoria de la que se ve. */
private fun decodificar(bytes: ByteArray, ancho: Int, alto: Int): Bitmap? {
    val medida = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, medida)
    if (medida.outWidth <= 0 || medida.outHeight <= 0) return null
    var muestra = 1
    while (medida.outWidth / (muestra * 2) >= ancho && medida.outHeight / (muestra * 2) >= alto) {
        muestra *= 2
    }
    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)
}

/* Devuelve la imagen: primero memoria, luego disco y, si no hay nada, la red
   (guardando el resultado en los dos sitios). */
private suspend fun obtener(context: Context, url: String, ancho: Int, alto: Int): Bitmap? {
    cacheMemoria.get(url)?.let { return it }
    if (url in fallidas) return null

    val archivo = archivoDe(context, url)
    val bytes = withContext(Dispatchers.IO) {
        runCatching {
            if (archivo.exists() && archivo.length() > 0) {
                archivo.readBytes()
            } else {
                val descargado = URL(url).openStream().use { it.readBytes() }
                runCatching {
                    archivo.writeBytes(descargado)
                    recortarDisco(carpetaCache(context))
                }
                descargado
            }
        }.getOrNull()
    }
    if (bytes == null || bytes.isEmpty()) {
        fallidas.add(url)
        return null
    }

    val bitmap = withContext(Dispatchers.Default) { decodificar(bytes, ancho, alto) }
    if (bitmap == null) {
        fallidas.add(url)
        return null
    }
    cacheMemoria.put(url, bitmap)
    return bitmap
}

/* Se recorta con esquinas redondeadas para que combine con las tarjetas. */
@Composable
fun ImagenRemota(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    forma: Shape = RoundedCornerShape(10.dp),
) {
    val context = LocalContext.current
    var bitmap by remember(url) { mutableStateOf(url?.let { cacheMemoria.get(it) }) }
    /* El tamaño real se sabe tras el primer layout: hasta entonces se enseña el
       hueco y no se descarga nada. */
    var tamano by remember { mutableStateOf(0 to 0) }

    val recorte = modifier
        .clip(forma)
        .onSizeChanged { tamano = it.width to it.height }

    LaunchedEffect(url, tamano) {
        val actual = url
        if (actual.isNullOrBlank() || bitmap != null) return@LaunchedEffect
        val (ancho, alto) = tamano
        if (ancho <= 0 || alto <= 0) return@LaunchedEffect
        val cargado = obtener(context, actual, ancho, alto) ?: return@LaunchedEffect
        bitmap = cargado
    }

    val actual = bitmap
    if (actual != null) {
        Image(
            bitmap = actual.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = recorte,
            contentScale = ContentScale.Fit,
        )
    } else {
        Box(recorte.background(MaterialTheme.colorScheme.surfaceVariant))
    }
}
