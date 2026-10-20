package com.maximillionsnyder.umafinidad

import android.content.SharedPreferences

/* Doble en memoria de SharedPreferences para los tests JVM.
 *
 * Los repositorios reciben la interfaz por constructor (el constructor de
 * producción delega en Context) así que se pueden probar sin Android. Solo
 * se guardan cadenas, que es lo único que usan ArbolesRepository y
 * ElencoRepository, pero se implementa la interfaz completa para no dejar
 * métodos abstractos sin resolver. */
class SharedPreferencesFake : SharedPreferences {

    private val valores = mutableMapOf<String, Any?>()

    /* Android lanza ClassCastException si la clave guarda un tipo distinto
       del pedido. Este interruptor reproduce el caso de una preferencia
       escrita por otra versión o por código ajeno. */
    var lanzarAlLeer: Boolean = false

    private fun valor(clave: String?): Any? = if (clave == null) null else valores[clave]

    override fun getAll(): MutableMap<String, *> = HashMap<String, Any?>(valores)

    override fun getString(key: String?, defValue: String?): String? {
        if (lanzarAlLeer) throw ClassCastException("la clave $key no es un String")
        return valor(key) as? String ?: defValue
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        if (lanzarAlLeer) throw ClassCastException("la clave $key no es un Set<String>")
        return valor(key) as? MutableSet<String> ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int = valor(key) as? Int ?: defValue

    override fun getLong(key: String?, defValue: Long): Long = valor(key) as? Long ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float = valor(key) as? Float ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        valor(key) as? Boolean ?: defValue

    override fun contains(key: String?): Boolean = key != null && valores.containsKey(key)

    override fun edit(): SharedPreferences.Editor = EditorFake()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) {
        /* Los repositorios bajo prueba no observan cambios. */
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) {
        /* Nada que desregistrar. */
    }

    private inner class EditorFake : SharedPreferences.Editor {

        private val cambios = mutableMapOf<String, Any?>()
        private var limpiar = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            cambios[key!!] = value
            return this
        }

        override fun putStringSet(
            key: String?,
            values: MutableSet<String>?,
        ): SharedPreferences.Editor {
            cambios[key!!] = values
            return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            cambios[key!!] = value
            return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            cambios[key!!] = value
            return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            cambios[key!!] = value
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            cambios[key!!] = value
            return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            cambios[key!!] = null
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            limpiar = true
            cambios.clear()
            return this
        }

        override fun commit(): Boolean {
            volcar()
            return true
        }

        override fun apply() {
            volcar()
        }

        /* Un valor null representa un remove, igual que en el Editor real. */
        private fun volcar() {
            if (limpiar) valores.clear()
            for ((clave, valor) in cambios) {
                if (valor == null) valores.remove(clave) else valores[clave] = valor
            }
        }
    }
}
