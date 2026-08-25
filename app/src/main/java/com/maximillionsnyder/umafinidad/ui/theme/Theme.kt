package com.maximillionsnyder.umafinidad.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val FondoArriba = Color(0xFF1B1E24)
val FondoAbajo = Color(0xFF14161A)
val Primario = Color(0xFFFFB782)
val AcentoNaranja = Color(0xFFF08C3A)
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

/* Rangos de afinidad (mismos colores que la PWA) + fondo tintado al 12%. */
val RankGreat = Color(0xFF7ED07E)
val RankGood = Color(0xFFE7C86A)
val RankFair = Color(0xFFD98F8F)

/* Colores por genealogía de los slots de herencia:
   hijo naranja; rama del Padre 1 azul (slots 1,3,4);
   rama del Padre 2 verde (slots 2,5,6). */
val RolHijo = Color(0xFFF08C3A)
val GenealogiaRama1 = Color(0xFF82AADD)
val GenealogiaRama2 = Color(0xFF7ED07E)

fun colorDeGenealogia(slot: Int): Color = when {
    slot == 0 -> RolHijo
    slot == 1 || slot == 3 || slot == 4 -> GenealogiaRama1
    else -> GenealogiaRama2
}

/* Medallas del top de linajes. */
val MedalOro = Color(0xFFFFD54F)
val MedalPlata = Color(0xFFCFD8DC)
val MedalBronce = Color(0xFFCE9B64)

fun colorDeRango(clase: String?): Color? = when (clase) {
    "rank-great" -> RankGreat
    "rank-good" -> RankGood
    "rank-fair" -> RankFair
    else -> null
}

fun fondoDeRango(clase: String?): Color? = colorDeRango(clase)?.copy(alpha = 0.12f)

private val EsquemaOscuro = darkColorScheme(
    primary = Primario,
    onPrimary = SobrePrimario,
    primaryContainer = ContenedorPrimario,
    onPrimaryContainer = SobreContenedorPrimario,
    secondaryContainer = ContenedorSecundario,
    onSecondaryContainer = SobreContenedorSecundario,
    background = FondoAbajo,
    onBackground = TextoPrincipal,
    surface = FondoAbajo,
    onSurface = TextoPrincipal,
    surfaceVariant = SuperficieAlta,
    onSurfaceVariant = TextoSecundario,
    outline = Contorno,
    outlineVariant = ContornoVariante,
)

data class ColoresRango(val great: Color, val good: Color, val fair: Color)

val LocalColoresRango = staticCompositionLocalOf {
    ColoresRango(RankGreat, RankGood, RankFair)
}

/* Gradiente vertical de fondo (identidad del ícono). */
val BrushFondo: Brush = Brush.verticalGradient(listOf(FondoArriba, FondoAbajo))

fun Modifier.fondoGradiente(): Modifier = background(BrushFondo)

/* La app es oscura por diseño (igual que la PWA): se usa siempre el esquema
   oscuro sin importar la preferencia del sistema. */
@Composable
fun UmaAfinidadTheme(content: @Composable () -> Unit) {
    isSystemInDarkTheme() // el tema no cambia con el sistema
    MaterialTheme(colorScheme = EsquemaOscuro) {
        CompositionLocalProvider(
            LocalColoresRango provides ColoresRango(RankGreat, RankGood, RankFair),
            content = content,
        )
    }
}
