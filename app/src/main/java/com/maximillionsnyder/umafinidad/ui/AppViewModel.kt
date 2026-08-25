package com.maximillionsnyder.umafinidad.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.maximillionsnyder.umafinidad.data.AffinityRepository
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.GrupoCompartido
import com.maximillionsnyder.umafinidad.domain.Linaje
import com.maximillionsnyder.umafinidad.domain.Rango
import com.maximillionsnyder.umafinidad.domain.SLOTS
import com.maximillionsnyder.umafinidad.domain.TipoVinculo
import com.maximillionsnyder.umafinidad.domain.armarArbol
import com.maximillionsnyder.umafinidad.domain.slotPara
import com.maximillionsnyder.umafinidad.domain.vinculos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/* Resultado de intentar colocar/quitar un personaje. La pantalla traduce
   estos casos a mensajes localizados. */
enum class ToggleResultado { COLOCADO, QUITADO, SELECCION_COMPLETA, REGLA }
enum class QuitarResultado { OK, NECESITA_CONFIRMACION }

/* Estado de cada sección del resultado (equivale a las notas de result.js). */
enum class EstadoSeccion { CON_FILAS, FALTA_HIJO, ELIGE_PADRE, OTRO_PADRE, FALTAN_PADRES, SIN_ABUELOS }

data class FilaVinculoUi(
    val ids: List<Int>,
    val puntos: Int,
    val rango: Rango?,
    val esCorredora: Boolean,
    val compartidos: List<GrupoCompartido>,
)

data class ResultadoCompat(
    val vacio: Boolean,
    val total: Int? = null,
    val rangoTotal: Rango? = null,
    val hijoPadres: List<FilaVinculoUi> = emptyList(),
    val estadoHijoPadres: EstadoSeccion = EstadoSeccion.FALTA_HIJO,
    val entrePadres: FilaVinculoUi? = null,
    val estadoEntrePadres: EstadoSeccion = EstadoSeccion.FALTAN_PADRES,
    val hijoPadreAbuelos: List<FilaVinculoUi> = emptyList(),
    val estadoHijoPadreAbuelos: EstadoSeccion = EstadoSeccion.FALTA_HIJO,
    val notaSinHijo: Boolean = false,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AffinityRepository(application)

    private val _modelo = MutableStateFlow<AffinityModel?>(null)
    val modelo: StateFlow<AffinityModel?> = _modelo

    private val _seleccion = MutableStateFlow<List<Int?>>(List(SLOTS) { null })
    val seleccion: StateFlow<List<Int?>> = _seleccion

    init {
        /* Carga de datos fuera del hilo principal (~800 KB de JSON). */
        viewModelScope.launch(Dispatchers.Default) {
            _modelo.value = repo.modelo
        }
    }

    fun toggle(id: Int): ToggleResultado {
        val sel = _seleccion.value.toTypedArray()
        val posiciones = sel.withIndex().filter { it.value == id }.map { it.index }
        if (posiciones.isNotEmpty()) {
            quitarSlot(posiciones.last())
            return ToggleResultado.QUITADO
        }
        val slot = slotPara(sel, id)
        if (slot >= 0 && puedeIrEn(sel, slot, id)) {
            _seleccion.value = sel.toMutableList().also { it[slot] = id }
            return ToggleResultado.COLOCADO
        }
        return if (slot == -1) ToggleResultado.SELECCION_COMPLETA else ToggleResultado.REGLA
    }

    fun quitarSlot(i: Int): QuitarResultado {
        val sel = _seleccion.value.toTypedArray()
        if (sel[i] == null) return QuitarResultado.OK
        if (i == 0 && sel.anyIndexed { j, v -> j > 0 && v != null }) {
            return QuitarResultado.NECESITA_CONFIRMACION
        }
        _seleccion.value = sel.toMutableList().also { it[i] = null }
        return QuitarResultado.OK
    }

    fun confirmarQuitarSoloHijo() {
        val sel = _seleccion.value.toMutableList()
        sel[0] = null
        _seleccion.value = sel
    }

    fun limpiarTodo() {
        _seleccion.value = List(SLOTS) { null }
    }

    /* Botón "Ver herencia" del top: carga el linaje completo y muestra la
       pestaña de compatibilidad. */
    fun cargarLinaje(l: Linaje) {
        _seleccion.value = listOf(
            l.hijo.charId,
            l.padre.charId,
            l.madre.charId,
            l.abuelos[0][0].charId,
            l.abuelos[0][1].charId,
            l.abuelos[1][0].charId,
            l.abuelos[1][1].charId,
        )
    }

    /* Resultado derivado, recalculado ante cada cambio de selección. */
    val resultado: StateFlow<ResultadoCompat?> =
        combine(_seleccion, _modelo) { sel, m -> m?.let { calcular(it, sel) } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    companion object {
        /* Porte de montarResultado(): mismos cálculos y mismas ramas. */
        fun calcular(modelo: AffinityModel, seleccion: List<Int?>): ResultadoCompat {
            if (seleccion.none { it != null }) return ResultadoCompat(vacio = true)

            val arbol = armarArbol(seleccion.toTypedArray())
            val vs = vinculos(arbol)

            fun fila(v: com.maximillionsnyder.umafinidad.domain.Vinculo): FilaVinculoUi {
                val puntos = if (v.esCorredora) {
                    0
                } else if (v.ids.size == 3) {
                    modelo.puntajeTrio(v.ids[0], v.ids[1], v.ids[2])
                } else {
                    modelo.puntajePar(v.ids[0], v.ids[1])
                }
                return FilaVinculoUi(
                    ids = v.ids,
                    puntos = puntos,
                    rango = modelo.rango(puntos),
                    esCorredora = v.esCorredora,
                    compartidos = modelo.gruposCompartidos(v.ids),
                )
            }

            val total = vs.sumOf { v ->
                if (v.esCorredora) 0
                else if (v.ids.size == 3) modelo.puntajeTrio(v.ids[0], v.ids[1], v.ids[2])
                else modelo.puntajePar(v.ids[0], v.ids[1])
            }

            val hp = vs.filter { it.tipo == TipoVinculo.HIJO_PADRE }.map(::fila)
            val ep = vs.filter { it.tipo == TipoVinculo.ENTRE_PADRES }.map(::fila)
            val hpa = vs.filter { it.tipo == TipoVinculo.HIJO_PADRE_ABUELO }.map(::fila)

            val ambosPadres = arbol.padres[0] != null && arbol.padres[1] != null
            val algunPadre = arbol.padres.any { it != null }

            return ResultadoCompat(
                vacio = false,
                total = total,
                rangoTotal = modelo.rangoTotal(total),
                hijoPadres = hp,
                estadoHijoPadres = if (hp.isNotEmpty()) EstadoSeccion.CON_FILAS
                else if (arbol.hijo == null) EstadoSeccion.FALTA_HIJO
                else EstadoSeccion.ELIGE_PADRE,
                entrePadres = ep.firstOrNull(),
                estadoEntrePadres = if (ambosPadres) EstadoSeccion.CON_FILAS
                else if (algunPadre) EstadoSeccion.OTRO_PADRE
                else EstadoSeccion.FALTAN_PADRES,
                hijoPadreAbuelos = hpa,
                estadoHijoPadreAbuelos = if (arbol.hijo == null) EstadoSeccion.FALTA_HIJO
                else if (hpa.isEmpty()) EstadoSeccion.SIN_ABUELOS
                else EstadoSeccion.CON_FILAS,
                notaSinHijo = arbol.hijo == null,
            )
        }
    }
}

private inline fun <T> Array<T>.anyIndexed(predicado: (Int, T) -> Boolean): Boolean {
    for (i in indices) if (predicado(i, this[i])) return true
    return false
}
