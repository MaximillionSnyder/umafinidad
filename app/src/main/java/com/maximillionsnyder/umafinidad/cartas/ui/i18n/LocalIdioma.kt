package com.maximillionsnyder.umafinidad.cartas.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.maximillionsnyder.umafinidad.cartas.domain.Textos
import java.util.Locale

/* Idioma visible de la sección Cartas: los cuatro de los datos más español.
   MainActivity lo provee desde el locale resuelto; los componentes que no
   reciben `idioma` lo leen de aquí. */
val LocalIdioma = staticCompositionLocalOf { "en" }

/* Idioma de interfaz de la sección Cartas a partir del locale ya resuelto de la
   app. El chino (simplificado o tradicional) usa la traducción tradicional, que
   es la única que traen los datos de GameTora. */
fun idiomaUiDe(locale: Locale): String = when (locale.language.lowercase()) {
    "ja" -> "ja"
    "ko" -> "ko"
    "zh" -> "tw"
    "es" -> "es"
    else -> "en"
}

@Composable
fun t(tabla: Textos, clave: String, vararg args: Any): String {
    val texto = tabla[clave, LocalIdioma.current]
    return if (args.isEmpty()) texto else String.format(Locale.US, texto, *args)
}
