package com.maximillionsnyder.umafinidad.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Fondo = Color(0xFF14161A)
val Primario = Color(0xFFFFB782)
val SobrePrimario = Color(0xFF4F2500)
val ContenedorPrimario = Color(0xFF6F3A06)
val SobreContenedorPrimario = Color(0xFFFFDBC2)
val ContenedorSecundario = Color(0xFF544239)
val SobreContenedorSecundario = Color(0xFFFFDCC2)
val SuperficieBaja = Color(0xFF1D2026)
val Superficie = Color(0xFF22262C)
val SuperficieAlta = Color(0xFF262A32)
val SuperficieMaxima = Color(0xFF2B3038)
val TextoPrincipal = Color(0xFFE8EAF0)
val TextoSecundario = Color(0xFF9AA0AD)
val Contorno = Color(0xFF8D919B)
val ContornoVariante = Color(0xFF2C313B)

/* Rangos de afinidad (mismos colores que la PWA). */
val RankGreat = Color(0xFF7ED07E)
val RankGood = Color(0xFFE7C86A)
val RankFair = Color(0xFFD98F8F)

fun colorDeRango(clase: String?): Color? = when (clase) {
    "rank-great" -> RankGreat
    "rank-good" -> RankGood
    "rank-fair" -> RankFair
    else -> null
}

private val EsquemaOscuro = darkColorScheme(
    primary = Primario,
    onPrimary = SobrePrimario,
    primaryContainer = ContenedorPrimario,
    onPrimaryContainer = SobreContenedorPrimario,
    secondaryContainer = ContenedorSecundario,
    onSecondaryContainer = SobreContenedorSecundario,
    background = Fondo,
    onBackground = TextoPrincipal,
    surface = Fondo,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieAlta,
    onSurfaceVariant = TextoSecundario,
    outline = Contorno,
    outlineVariant = ContornoVariante,
)

/* Colores de rango accesibles desde cualquier composable. */
data class ColoresRango(val great: Color, val good: Color, val fair: Color)

val LocalColoresRango = staticCompositionLocalOf {
    ColoresRango(RankGreat, RankGood, RankFair)
}

/* La app es oscura por diseño (igual que la PWA): se usa siempre el esquema
   oscuro sin importar la preferencia del sistema. */
@Composable
fun UmaAfinidadTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaOscuro) {
        CompositionLocalProvider(
            LocalColoresRango provides ColoresRango(RankGreat, RankGood, RankFair),
            content = content,
        )
    }
}
