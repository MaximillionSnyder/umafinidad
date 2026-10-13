package com.maximillionsnyder.umafinidad.data

import com.maximillionsnyder.umafinidad.domain.Chispa
import com.maximillionsnyder.umafinidad.domain.Chispas
import com.maximillionsnyder.umafinidad.domain.ErrorPadres
import com.maximillionsnyder.umafinidad.domain.Padre
import com.maximillionsnyder.umafinidad.domain.RamaPadre
import com.maximillionsnyder.umafinidad.domain.TotalServidor
import com.maximillionsnyder.umafinidad.domain.leerTotal
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/* Cliente del endpoint /search/query de uma.moe.
 *
 * Sin dependencias nuevas: HttpURLConnection (parte de Android) y el parseo
 * con kotlinx-serialization, que el proyecto ya usa.
 *
 * El parseo navega el JSON en lugar de mapear DTOs a propósito: el servidor
 * decide el tipo de los campos `*_factors` según el color (a veces un número
 * suelto, a veces una lista) y conviene tolerar campos que aparezcan o
 * desaparezcan sin que la pantalla se caiga. */

sealed interface RespuestaUmamoe {

    data class Ok(
        val items: List<Padre>,
        val total: TotalServidor,
        val paginas: Int,
    ) : RespuestaUmamoe

    data class Fallo(val error: ErrorPadres) : RespuestaUmamoe
}

class UmamoeApi(private val apiKey: String?) {

    suspend fun buscar(
        parametros: List<Pair<String, String>>,
        pagina: Int,
        limite: Int,
    ): RespuestaUmamoe = withContext(Dispatchers.IO) {
        val key = apiKey?.takeIf { it.isNotBlank() }
        if (key == null) {
            RespuestaUmamoe.Fallo(ErrorPadres.SIN_KEY)
        } else {
            pedir("$BASE/search/query?${consulta(parametros, pagina, limite)}", key)
        }
    }

    private fun consulta(
        parametros: List<Pair<String, String>>,
        pagina: Int,
        limite: Int,
    ): String = buildString {
        append("search_type=inheritance")
        append("&page=").append(pagina)
        append("&limit=").append(limite.coerceIn(1, LIMITE_MAX))
        for ((nombre, valor) in parametros) {
            /* Los valores llevan comas (rangos de estrellas): hay que
             * codificarlas o el servidor parte el parámetro. */
            append('&').append(codificar(nombre)).append('=').append(codificar(valor))
        }
    }

    private fun codificar(valor: String): String = URLEncoder.encode(valor, "UTF-8")

    private fun pedir(url: String, key: String): RespuestaUmamoe {
        var conexion: HttpURLConnection? = null
        return try {
            conexion = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = CONEXION_MS
                readTimeout = LECTURA_MS
                setRequestProperty("x-api-key", key)
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }
            val codigo = conexion.responseCode
            if (codigo == HttpURLConnection.HTTP_OK) {
                val cuerpo = conexion.inputStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                interpretar(cuerpo)
            } else {
                cuandoFalla(codigo)
            }
        } catch (_: IOException) {
            /* Sin red, DNS caído, timeout: no es un error del servidor. */
            RespuestaUmamoe.Fallo(ErrorPadres.SIN_RED)
        } catch (_: Exception) {
            RespuestaUmamoe.Fallo(ErrorPadres.RESPUESTA_INVALIDA)
        } finally {
            conexion?.disconnect()
        }
    }

    private fun cuandoFalla(codigo: Int): RespuestaUmamoe = when (codigo) {
        HttpURLConnection.HTTP_UNAUTHORIZED -> RespuestaUmamoe.Fallo(ErrorPadres.KEY_RECHAZADA)
        /* 403 es lo que responde sin key (browser_proof_required). */
        HttpURLConnection.HTTP_FORBIDDEN -> RespuestaUmamoe.Fallo(ErrorPadres.KEY_RECHAZADA)
        else -> RespuestaUmamoe.Fallo(ErrorPadres.SERVIDOR)
    }

    private fun interpretar(cuerpo: String): RespuestaUmamoe {
        val raiz = try {
            Json.parseToJsonElement(cuerpo) as? JsonObject
        } catch (_: Exception) {
            null
        } ?: return RespuestaUmamoe.Fallo(ErrorPadres.RESPUESTA_INVALIDA)

        val items = (raiz["items"] as? JsonArray)
            ?.mapNotNull { (it as? JsonObject)?.let(::decodificarPadre) }
            .orEmpty()
        val total = leerTotal((raiz["total"] as? JsonPrimitive)?.contentOrNull)
        val paginas = (raiz["total_pages"] as? JsonPrimitive)?.contentOrNull?.toIntOrNull() ?: 1
        return RespuestaUmamoe.Ok(items, total, paginas.coerceAtLeast(1))
    }

    private fun decodificarPadre(item: JsonObject): Padre {
        val inh = item["inheritance"] as? JsonObject ?: JsonObject(emptyMap())
        return Padre(
            entrenador = (item["trainer_name"] as? JsonPrimitive)?.contentOrNull,
            padre = ramaDe(inh, "main_parent_id", "main"),
            abueloIzquierdo = ramaDe(inh, "parent_left_id", "left"),
            abueloDerecho = ramaDe(inh, "parent_right_id", "right"),
            /* Sin prefijo = total combinado de las tres umas del linaje. */
            combinadas = Chispas(
                azul = chispasDe(inh["blue_sparks"]),
                rosa = chispasDe(inh["pink_sparks"]),
                verde = chispasDe(inh["green_sparks"]),
                blanco = chispasDe(inh["white_sparks"]),
            ),
            afinidad = enteroDe(inh["affinity_score"]),
            estrellasAzul = enteroDe(inh["blue_stars_sum"]),
            estrellasRosa = enteroDe(inh["pink_stars_sum"]),
            estrellasVerde = enteroDe(inh["green_stars_sum"]),
            estrellasBlanco = enteroDe(inh["white_stars_sum"]),
        )
    }

    /* Cada rama usa un prefijo distinto para sus factores: main_*, left_* y
     * right_*, que no coincide con el nombre del campo que trae su id. */
    private fun ramaDe(inh: JsonObject, campoId: String, prefijo: String): RamaPadre = RamaPadre(
        cartaId = enteroDe(inh[campoId]),
        chispas = Chispas(
            azul = chispasDe(inh["${prefijo}_blue_factors"]),
            rosa = chispasDe(inh["${prefijo}_pink_factors"]),
            verde = chispasDe(inh["${prefijo}_green_factors"]),
            blanco = chispasDe(inh["${prefijo}_white_factors"]),
        ),
    )

    private fun enteroDe(elemento: JsonElement?): Int? =
        (elemento as? JsonPrimitive)?.contentOrNull?.toIntOrNull()

    /* El mismo campo llega como número suelto o como lista según el color. */
    private fun idsDe(elemento: JsonElement?): List<Int> = when (elemento) {
        null -> emptyList()
        is JsonArray -> elemento.mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.toIntOrNull() }
        is JsonPrimitive -> listOfNotNull(elemento.contentOrNull?.toIntOrNull())
        else -> emptyList()
    }

    /* Codificación de uma.moe: id = factor*10 + estrellas. */
    private fun chispasDe(elemento: JsonElement?): List<Chispa> =
        idsDe(elemento)
            .filter { it > 0 }
            .map { Chispa(factorId = it / 10, estrellas = it % 10) }

    companion object {
        const val LIMITE_MAX = 100
        private const val BASE = "https://uma.moe"
        private const val USER_AGENT = "umafinidad/1.0 (app Android)"
        private const val CONEXION_MS = 10_000
        private const val LECTURA_MS = 20_000
    }
}
