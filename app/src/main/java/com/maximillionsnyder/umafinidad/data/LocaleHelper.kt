package com.maximillionsnyder.umafinidad.data

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/* Aplica el idioma a nivel app vía AppCompatDelegate (funciona en API 24+ y usa LocaleManager en 33+).
   SISTEMA = limpia el locale de la app y vuelve al del sistema. */
fun aplicarIdioma(idioma: Idioma) {
    val codigo = idioma.codigo()
    val locales = if (codigo == null) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(codigo)
    }
    AppCompatDelegate.setApplicationLocales(locales)
}

/* Compat: para Activity.attachBaseContext si se quiere forzar antes de inflar (no necesario con AppCompatDelegate,
   pero útil para pre-apply en attachBaseContext). */
fun localeDe(idioma: Idioma): java.util.Locale? = idioma.codigo()?.let { java.util.Locale.forLanguageTag(it) }
