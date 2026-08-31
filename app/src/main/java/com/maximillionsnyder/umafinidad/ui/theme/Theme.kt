package com.maximillionsnyder.umafinidad.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.maximillionsnyder.umafinidad.data.ThemeMode

val FondoArriba = Color(0xFF000000)
val FondoAbajo = Color(0xFF000000)
val Primario = Color(0xFFFF8C42)
val AcentoNaranja = Color(0xFFFF8C42)
val SobrePrimario = Color(0xFF4F2500)
val ContenedorPrimario = Color(0xFF7A3E00)
val SobreContenedorPrimario = Color(0xFFFFDBC2)
val ContenedorSecundario = Color(0xFF3A2F27)
val SobreContenedorSecundario = Color(0xFFFFDCC2)
val SuperficieBaja = Color(0xFF0A0A0A)
val Superficie = Color(0xFF121212)
val SuperficieAlta = Color(0xFF1E1E1E)
val SuperficieMaxima = Color(0xFF2A2A2A)
val TextoPrincipal = Color(0xFFF2F2F2)
val TextoSecundario = Color(0xFF9CA3AF)
val Contorno = Color(0xFF8D919B)
val ContornoVariante = Color(0xFF2A2A2A)

/* ---- Tema claro: fondo blanco puro + superficies neutras ---- */
val FondoArribaClaro = Color(0xFFFFFFFF)
val FondoAbajoClaro = Color(0xFFFFFFFF)
val PrimarioClaro = Color(0xFFC2410C)
val SobrePrimarioClaro = Color(0xFFFFFFFF)
val ContenedorPrimarioClaro = Color(0xFFFFE8D6)
val SobreContenedorPrimarioClaro = Color(0xFF4F2500)
val ContenedorSecundarioClaro = Color(0xFFFFDCC2)
val SobreContenedorSecundarioClaro = Color(0xFF544239)
val SuperficieBajaClaro = Color(0xFFFFFFFF)
val SuperficieClaro = Color(0xFFF9F9F9)
val SuperficieAltaClaro = Color(0xFFF1F1F1)
val SuperficieMaximaClaro = Color(0xFFEAEAEA)
val TextoPrincipalClaro = Color(0xFF111111)
val TextoSecundarioClaro = Color(0xFF6B7280)
val ContornoClaro = Color(0xFF9CA3AF)
val ContornoVarianteClaro = Color(0xFFE5E7EB)
val CardFondoClaro = Color(0xFFF9F9F9)

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

/* Fondo de cards: sutil elevación sobre el fondo puro (negro/blanco). */
val CardFondo = Color(0xFF1A1A1A)

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
    surfaceContainerLowest = FondoAbajo,
    surfaceContainerLow = SuperficieBaja,
    surfaceContainer = Superficie,
    surfaceContainerHigh = SuperficieAlta,
    surfaceContainerHighest = SuperficieMaxima,
    outline = Contorno,
    outlineVariant = ContornoVariante,
)

private val EsquemaClaro = lightColorScheme(
    primary = PrimarioClaro,
    onPrimary = SobrePrimarioClaro,
    primaryContainer = ContenedorPrimarioClaro,
    onPrimaryContainer = SobreContenedorPrimarioClaro,
    secondaryContainer = ContenedorSecundarioClaro,
    onSecondaryContainer = SobreContenedorSecundarioClaro,
    background = FondoAbajoClaro,
    onBackground = TextoPrincipalClaro,
    surface = FondoAbajoClaro,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = SuperficieAltaClaro,
    onSurfaceVariant = TextoSecundarioClaro,
    surfaceContainerLowest = FondoAbajoClaro,
    surfaceContainerLow = SuperficieBajaClaro,
    surfaceContainer = SuperficieClaro,
    surfaceContainerHigh = SuperficieAltaClaro,
    surfaceContainerHighest = SuperficieMaximaClaro,
    outline = ContornoClaro,
    outlineVariant = ContornoVarianteClaro,
)

data class ColoresRango(val great: Color, val good: Color, val fair: Color)

val LocalColoresRango = staticCompositionLocalOf {
    ColoresRango(RankGreat, RankGood, RankFair)
}

val LocalIsDark = staticCompositionLocalOf { true }

@Composable
fun cardFondo(): Color = if (LocalIsDark.current) CardFondo else CardFondoClaro

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/* Gradiente vertical de fondo (identidad del ícono). */
val BrushFondo: Brush = Brush.verticalGradient(listOf(FondoArriba, FondoAbajo))
val BrushFondoClaro: Brush = Brush.verticalGradient(listOf(FondoArribaClaro, FondoAbajoClaro))

fun Modifier.fondoGradiente(isDark: Boolean = true): Modifier =
    background(if (isDark) BrushFondo else BrushFondoClaro)

@Composable
fun UmaAfinidadTheme(
    tema: ThemeMode = ThemeMode.SISTEMA,
    content: @Composable () -> Unit,
) {
    val esOscuro = when (tema) {
        ThemeMode.CLARO -> false
        ThemeMode.OSCURO -> true
        ThemeMode.SISTEMA -> isSystemInDarkTheme()
    }
    val esquema = if (esOscuro) EsquemaOscuro else EsquemaClaro

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            // Transparent nav bar where supported; fallback to solid background on legacy 3-button devices
            window.navigationBarColor = if (Build.VERSION.SDK_INT >= 29) {
                Color.Transparent.toArgb()
            } else {
                esquema.background.toArgb()
            }
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !esOscuro
            if (Build.VERSION.SDK_INT >= 26) {
                controller.isAppearanceLightNavigationBars = !esOscuro
            }
        }
    }

    MaterialTheme(colorScheme = esquema, shapes = AppShapes) {
        CompositionLocalProvider(
            LocalColoresRango provides ColoresRango(RankGreat, RankGood, RankFair),
            LocalIsDark provides esOscuro,
            content = content,
        )
    }
}
