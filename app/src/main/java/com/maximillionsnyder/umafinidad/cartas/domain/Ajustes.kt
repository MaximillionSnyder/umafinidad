package com.maximillionsnyder.umafinidad.cartas.domain

import java.util.Locale

/* Ajustes propios de la sección Cartas. Tema, idioma y tamaño de texto los pone
   la app principal (Ajustes de Uma Afinidad); acá quedan la densidad de las
   listas de skills/cartas y los formateos que dependen del idioma. */

/* Cómo de apretadas se ven las listas: cómoda (por defecto) o compacta, para
   ver más filas de un vistazo. */
const val VISTA_COMODA = "comoda"
const val VISTA_COMPACTA = "compacta"

val VISTAS = listOf(VISTA_COMODA, VISTA_COMPACTA)

/* Los cinco idiomas de la interfaz, en el mismo orden que IDIOMAS_UI. */
private fun textosAjuste(en: String, ja: String, ko: String, tw: String, es: String): Map<String, String> =
    mapOf("en" to en, "ja" to ja, "ko" to ko, "tw" to tw, "es" to es)

private val TABLA_AJUSTES: Map<String, Map<String, String>> = mapOf(
    VISTA_COMODA to textosAjuste("Comfortable", "標準", "여유", "舒適", "Cómoda"),
    VISTA_COMPACTA to textosAjuste("Compact", "コンパクト", "컴팩트", "緊湊", "Compacta"),
)

private val AJUSTES = Textos(TABLA_AJUSTES)

fun etiquetaVista(vista: String, idioma: String): String = when (vista) {
    VISTA_COMPACTA -> AJUSTES[VISTA_COMPACTA, idioma]
    else -> AJUSTES[VISTA_COMODA, idioma]
}

/* El base_time de las condiciones viene en milisegundos desde GameTora; en la app
   se muestra en segundos (con un decimal cuando no es exacto).

   Decimales con punto en todos los idiomas: es el separador que usan los datos
   de GameTora y el que se lee igual en en/ja/ko/tw (ninguno de los cuatro exige
   la coma). Se formatea con Locale.US justo para que el móvil no lo cambie a
   coma, tampoco en español. */
fun duracionEnSegundos(milisegundos: Long, idioma: String): String {
    val texto = if (milisegundos % 1000L == 0L) {
        (milisegundos / 1000L).toString()
    } else {
        String.format(Locale.US, "%.1f", milisegundos / 1000.0)
    }
    return when (idioma) {
        "ja", "tw" -> "${texto}秒"
        "ko" -> "${texto}초"
        else -> "$texto s"
    }
}

/* Todas las filas de las etiquetas, para el test que exige los cinco idiomas de
   la interfaz. */
val FILAS_AJUSTES: List<Pair<String, Map<String, String>>> =
    TABLA_AJUSTES.entries.map { it.key to it.value }
