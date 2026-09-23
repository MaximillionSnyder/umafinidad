package com.maximillionsnyder.umafinidad.data

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

/* ===== Versión Pro =====

   La app es offline y sin cuentas: la licencia Pro se valida en el propio
   dispositivo con un código `UMA-XXXX-XXXX-XXXX`. El generador vive en
   `scripts/generar-codigos-pro.mjs` y comparte el algoritmo con
   `LicenciaPro`; los vectores de `ProTest` fijan el contrato entre ambos. */

/* Alfabeto sin caracteres ambiguos (sin 0/O, 1/I/L) para dictar el código
   por voz o copiarlo a mano sin equivocarse. */
const val ALFABETO_PRO = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"

/* Funciones que la versión Pro desbloquea. Es la única lista de verdad: la
   pantalla Pro, los candados de Ajustes y el panel de la burbuja salen de
   acá. Si algún día una función pasa a ser gratis, se marca acá. */
enum class FuncionPro(val requierePro: Boolean = true) {
    BURBUJA_REDIMENSIONAR,
    RANKING_PADRES,
    ARBOLES_GUARDADOS,
}

/* ¿La función está disponible con el estado Pro actual? */
fun funcionDisponible(funcion: FuncionPro, esPro: Boolean): Boolean =
    esPro || !funcion.requierePro

fun puedeGuardarArboles(esPro: Boolean): Boolean =
    funcionDisponible(FuncionPro.ARBOLES_GUARDADOS, esPro)

/* ===== Lógica pura (testeable en JVM sin Android) ===== */

object LicenciaPro {

    const val PREFIJO = "UMA"
    const val LARGO_CUERPO = 8
    const val LARGO_FIRMA = 4
    const val LARGO_TOTAL = PREFIJO.length + LARGO_CUERPO + LARGO_FIRMA

    /* Deja solo A-Z y 0-9 en mayúsculas: tolera guiones, espacios y
       minúsculas al pegar el código. */
    fun normalizar(bruto: String): String = buildString(bruto.length) {
        for (c in bruto.uppercase()) {
            if ((c in 'A'..'Z') || (c in '0'..'9')) append(c)
        }
    }

    /* Un código es válido si tiene el largo y el prefijo esperados y la
       firma coincide con la del cuerpo. */
    fun esValida(bruto: String): Boolean {
        val limpio = normalizar(bruto)
        if (limpio.length != LARGO_TOTAL) return false
        if (!limpio.startsWith(PREFIJO)) return false
        val cuerpo = limpio.substring(PREFIJO.length, PREFIJO.length + LARGO_CUERPO)
        val firma = limpio.substring(PREFIJO.length + LARGO_CUERPO)
        if (cuerpo.any { it !in ALFABETO_PRO }) return false
        return firma == firmaDe(cuerpo)
    }

    /* "umaab2c3d4e5f6g7" -> "UMA-AB2C-3D4E-5F6G" (si el largo no da, deja
       lo normalizado tal cual: la UI lo muestra como error). */
    fun formatear(bruto: String): String {
        val limpio = normalizar(bruto)
        if (limpio.length != LARGO_TOTAL) return limpio
        return buildString(LARGO_TOTAL + 3) {
            append(limpio, 0, 3)
            append('-')
            append(limpio, 3, 7)
            append('-')
            append(limpio, 7, 11)
            append('-')
            append(limpio, 11, 15)
        }
    }

    /* Firma de 4 caracteres (20 bits) del cuerpo: FNV-1a de 32 bits con el
       finalizador de murmur3 para que los bits bajos también se mezclen. */
    fun firmaDe(cuerpo: String): String {
        var h = 2166136261u
        for (c in cuerpo) {
            h = h xor c.code.toUInt()
            h *= 16777619u
        }
        h = h xor (h shr 16)
        h *= 0x85EBCA6Bu
        h = h xor (h shr 13)
        h *= 0xC2B2AE35u
        h = h xor (h shr 16)
        return buildString(LARGO_FIRMA) {
            var x = h
            repeat(LARGO_FIRMA) {
                append(ALFABETO_PRO[(x and 31u).toInt()])
                x = x shr 5
            }
        }
    }

    /* Código nuevo con cuerpo aleatorio. Lo usa el script del repo para
       emitir licencias; en la app solo se valida. */
    fun generar(azar: Random = Random.Default): String {
        val cuerpo = buildString(LARGO_CUERPO) {
            repeat(LARGO_CUERPO) { append(ALFABETO_PRO[azar.nextInt(ALFABETO_PRO.length)]) }
        }
        return formatear(PREFIJO + cuerpo + firmaDe(cuerpo))
    }
}

enum class ResultadoActivacion { ACTIVADO, YA_ACTIVO, CODIGO_INVALIDO }

/* ===== Wrapper de SharedPreferences ===== */

class ProRepository(context: Context) {

    private val prefs = context.getSharedPreferences("pro_estado", Context.MODE_PRIVATE)

    val esPro: Boolean
        get() = prefs.getBoolean(KEY_ES_PRO, false)

    /* Código ya formateado, solo para mostrarlo en la pantalla Pro. */
    val codigo: String?
        get() = prefs.getString(KEY_CODIGO, null)

    val activadoEn: Long
        get() = prefs.getLong(KEY_ACTIVADO_EN, 0L)

    fun activar(bruto: String): ResultadoActivacion {
        if (esPro) return ResultadoActivacion.YA_ACTIVO
        if (!LicenciaPro.esValida(bruto)) return ResultadoActivacion.CODIGO_INVALIDO
        prefs.edit()
            .putBoolean(KEY_ES_PRO, true)
            .putString(KEY_CODIGO, LicenciaPro.formatear(bruto))
            .putLong(KEY_ACTIVADO_EN, System.currentTimeMillis())
            .apply()
        return ResultadoActivacion.ACTIVADO
    }

    /* Se puede volver a activar con el mismo código: sirve para probar la
       versión gratis sin perder la licencia. */
    fun desactivar() {
        prefs.edit()
            .remove(KEY_ES_PRO)
            .remove(KEY_CODIGO)
            .remove(KEY_ACTIVADO_EN)
            .apply()
    }

    /* Avisa cuando cambia el estado Pro (lo observa el servicio de la
       burbuja para refrescar el panel sin reabrir la app). */
    fun observar(alCambiar: () -> Unit): SharedPreferences.OnSharedPreferenceChangeListener {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, clave ->
            if (clave == KEY_ES_PRO) alCambiar()
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return listener
    }

    fun dejarDeObservar(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private companion object {
        const val KEY_ES_PRO = "es_pro"
        const val KEY_CODIGO = "codigo"
        const val KEY_ACTIVADO_EN = "activado_en"
    }
}
