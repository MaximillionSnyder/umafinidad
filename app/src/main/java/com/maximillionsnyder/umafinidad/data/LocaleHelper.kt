package com.maximillionsnyder.umafinidad.data

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/* Aplica el idioma a nivel app. SISTEMA = limpia y vuelve al sistema.
   - En API 33+ usa LocaleManager (per-app language).
   - En <33 si es AppCompatActivity usa AppCompatDelegate, si no fallback manual + recreate. */
fun aplicarIdioma(idioma: Idioma) {
    val codigo = idioma.codigo()
    val locales = if (codigo == null) LocaleListCompat.getEmptyLocaleList()
    else LocaleListCompat.forLanguageTags(codigo)
    try {
        AppCompatDelegate.setApplicationLocales(locales)
    } catch (_: Exception) {}
    // Fallback manual para ComponentActivity en <33 donde AppCompatDelegate no recrea
    // (no hace daño si ya se aplicó vía delegate)
}

fun aplicarIdioma(context: Context, idioma: Idioma) {
    val codigo = idioma.codigo()
    // Evita recreate innecesario si ya está en ese idioma
    try {
        val current = context.resources.configuration.locales.get(0).language
        if (codigo != null && current == codigo) return
        if (codigo == null) {
            // SISTEMA: si ya sigue el sistema, no recrear; AppCompatDelegate ya limpia
            val appLocales = AppCompatDelegate.getApplicationLocales()
            if (appLocales.isEmpty) return
        }
    } catch (_: Exception) {}
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        try {
            val manager = context.getSystemService(android.app.LocaleManager::class.java)
            if (manager != null) {
                manager.applicationLocales = if (codigo == null) LocaleList.getEmptyLocaleList()
                else LocaleList.forLanguageTags(codigo)
                return
            }
        } catch (_: Exception) {}
    }
    // Intento AppCompatDelegate
    try {
        val locales = if (codigo == null) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(codigo)
        AppCompatDelegate.setApplicationLocales(locales)
        // Si es AppCompatActivity, el delegate recreará solo
        if (context is AppCompatActivity) return
        // Para ComponentActivity, AppCompatDelegate no recrea en <33, seguimos a fallback
        // pero si API >=33 el LocaleManager ya manejó arriba, si no, necesitamos manual
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return
    } catch (_: Exception) {}
    // Fallback manual para ComponentActivity en API <33
    try {
        val locale = codigo?.let { java.util.Locale.forLanguageTag(it) }
        if (locale != null) java.util.Locale.setDefault(locale)
        val res = context.resources
        val config = res.configuration
        if (codigo == null) {
            @Suppress("DEPRECATION")
            config.locale = java.util.Locale.getDefault()
        } else {
            config.setLocale(locale)
        }
        @Suppress("DEPRECATION")
        res.updateConfiguration(config, res.displayMetrics)
        if (context is Activity) context.recreate()
    } catch (_: Exception) {}
}

/* Compat: para Activity.attachBaseContext si se quiere forzar antes de inflar (no necesario con AppCompatDelegate,
   pero útil para pre-apply en attachBaseContext). */
fun localeDe(idioma: Idioma): java.util.Locale? = idioma.codigo()?.let { java.util.Locale.forLanguageTag(it) }
