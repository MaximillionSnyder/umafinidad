package com.maximillionsnyder.umafinidad.cartas.domain

/* Tabla de textos de la app por idioma: los cuatro idiomas de los datos de
   GameTora más español (que solo existe en los textos de interfaz: los datos
   no lo traen y caen a inglés). Los tests comprueban que toda clave de la
   interfaz está en los cinco idiomas. */
class Textos(private val tabla: Map<String, Map<String, String>>) {
    operator fun get(clave: String, idioma: String): String {
        val textos = tabla[clave] ?: return clave
        return textos[idioma]?.takeIf { it.isNotBlank() }
            ?: textos["en"]?.takeIf { it.isNotBlank() }
            ?: clave
    }
    val claves: Set<String> get() = tabla.keys
}
