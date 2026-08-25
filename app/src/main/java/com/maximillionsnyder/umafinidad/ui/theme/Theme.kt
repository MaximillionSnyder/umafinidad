package com.maximillionsnyder.umafinidad.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Fondo = Color(0xFF14161A)
val Superficie = Color(0xFF1C1F26)
val SuperficieVariante = Color(0xFF242831)
val Acento = Color(0xFFF08C3A)
val TextoPrincipal = Color(0xFFE8EAED)
val TextoSecundario = Color(0xFF9AA0A6)

private val EsquemaOscuro = darkColorScheme(
    primary = Acento,
    onPrimary = Color(0xFF1A1206),
    secondary = Color(0xFFD7B98A),
    background = Fondo,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = TextoSecundario,
)

/* La app es oscura por diseño (igual que la PWA): se usa siempre el esquema
   oscuro sin importar la preferencia del sistema. */
@Composable
fun UmaAfinidadTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaOscuro,
        content = content,
    )
}
