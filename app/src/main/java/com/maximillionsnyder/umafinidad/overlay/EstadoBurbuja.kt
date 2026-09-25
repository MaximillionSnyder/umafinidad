package com.maximillionsnyder.umafinidad.overlay

import com.maximillionsnyder.umafinidad.R
import com.maximillionsnyder.umafinidad.data.AffinityRepository
import com.maximillionsnyder.umafinidad.domain.sePuedeCompletar
import com.maximillionsnyder.umafinidad.domain.AffinityModel
import com.maximillionsnyder.umafinidad.domain.puedeIrEn
import com.maximillionsnyder.umafinidad.domain.rankearSugerencias
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* Estado de la burbuja flotante, fuera del ciclo de vida de las ventanas:
   la selección de la genealogía, el buscador y el autocompletado viven acá
   para que cerrar y reabrir el panel no los pierda ni los vuelva a calcular.

   Antes esto estaba repartido entre el servicio (selección) y el composable
   (filtro y aviso): el filtro se perdía al cerrar el panel y las sugerencias
   se recalculaban en cada recomposición del panel. */
internal class EstadoBurbuja(
    private val repositorio: AffinityRepository,
    private val alcance: CoroutineScope,
) {

    /* Modelo datamined; null mientras se carga desde los assets. */
    private val _modelo = MutableStateFlow<AffinityModel?>(null)
    val modelo = _modelo.asStateFlow()

    /* Selección de la genealogía: hijo, dos padres y abuelos. */
    private val _seleccion = MutableStateFlow(seleccionVacia)
    val seleccion = _seleccion.asStateFlow()

    /* Cálculo del autocompletado en curso (deshabilita el botón). */
    private val _autocompletando = MutableStateFlow(false)
    val autocompletando = _autocompletando.asStateFlow()

    /* Texto del buscador: sigue vivo aunque se cierre el panel. */
    private val _filtro = MutableStateFlow("")
    val filtro = _filtro.asStateFlow()

    /* Slot elegido como destino de la próxima colocación; null = automático
       (primer hueco válido en orden). */
    private val _slotDestino = MutableStateFlow<Int?>(null)
    val slotDestino = _slotDestino.asStateFlow()

    /* Aviso de regla o selección completa; se limpia al acertar. */
    private val _aviso = MutableStateFlow<Int?>(null)
    val aviso = _aviso.asStateFlow()

    /* Personajes que pueden entrar en la genealogía, ya filtrados: se calcula
       una sola vez por modelo y no en cada búsqueda. */
    val jugables = _modelo
        .map { modelo ->
            modelo?.personajes.orEmpty().filter { it.playable == true && it.active == true }
        }
        .flowOn(Dispatchers.Default)

    /* Sugerencias del buscador. `combine` recalcula solo cuando cambian los
       candidatos, el texto, el destino o la selección, y `flowOn` lo corre en
       el hilo de fondo porque la distancia de edición no es gratis. Con un
       destino elegido solo se ofrecen los personajes que pueden ir ahí. */
    val sugerencias = combine(
        jugables, _filtro, _slotDestino, _seleccion,
    ) { candidatos, texto, destino, seleccion ->
        val base = if (destino == null || seleccion.getOrNull(destino) != null) {
            candidatos
        } else {
            val actual = seleccion.toTypedArray()
            candidatos.filter { puedeIrEn(actual, destino, it.charId) }
        }
        rankearSugerencias(base, texto)
    }.flowOn(Dispatchers.Default)

    fun cargar() {
        alcance.launch {
            val cargado = withContext(Dispatchers.IO) {
                runCatching { repositorio.modelo }.getOrNull()
            }
            if (cargado != null) _modelo.value = cargado
        }
    }

    fun alternar(id: Int) {
        val destino = _slotDestino.value?.takeIf { _seleccion.value.getOrNull(it) == null }
        val colocacion = alternar(_seleccion.value, id, destino)
        if (colocacion.resultado == ColocacionResultado.COLOCADO ||
            colocacion.resultado == ColocacionResultado.QUITADO
        ) {
            _seleccion.value = colocacion.seleccion
            _filtro.value = ""
            _aviso.value = null
            if (destino != null && colocacion.resultado == ColocacionResultado.COLOCADO) {
                _slotDestino.value = null
            }
        } else {
            _aviso.value = if (colocacion.resultado == ColocacionResultado.COMPLETA) {
                R.string.seleccion_completa
            } else {
                R.string.regla_slots
            }
        }
    }

    /* Tocar un slot ocupado lo quita; tocar uno vacío lo marca o desmarca
       como destino de la próxima colocación. */
    fun tocarSlot(slot: Int) {
        if (_seleccion.value.getOrNull(slot) != null) {
            quitarSlot(slot)
            return
        }
        _slotDestino.value = if (_slotDestino.value == slot) null else slot
        _aviso.value = null
    }

    fun quitarSlot(slot: Int) {
        _seleccion.value = quitar(_seleccion.value, slot)
        if (_slotDestino.value == slot) _slotDestino.value = null
        _aviso.value = null
    }

    fun limpiar() {
        _seleccion.value = seleccionVacia
        _filtro.value = ""
        _aviso.value = null
        _slotDestino.value = null
    }

    fun buscar(texto: String) {
        _filtro.value = texto
        _aviso.value = null
    }

    /* Completa los huecos con la mayor afinidad posible respetando lo ya
       cargado (cálculo exacto, fuera del hilo principal). */
    fun autocompletar() {
        val modeloActual = _modelo.value ?: return
        if (_autocompletando.value) return
        val actual = _seleccion.value
        if (!sePuedeCompletar(actual)) return

        _slotDestino.value = null
        _autocompletando.value = true
        alcance.launch(Dispatchers.Default) {
            try {
                _seleccion.value = modeloActual.completarSeleccion(actual)
            } finally {
                _autocompletando.value = false
            }
        }
    }
}
