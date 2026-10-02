/* Estado global de la app. Espejo de ui/AppViewModel.kt, con reactividad de
   Svelte 5 (runas). Los estados grandes/estructurados usan $state.raw porque
   siempre se reemplazan por copias nuevas. */

import { AffinityModel } from '../domain/affinity'
import {
  ColocacionResultado,
  SlotEstado,
  agregarEn,
  alternar,
  seleccionVacia,
  slotsPara,
  type Seleccion,
  type SlotOpcion,
} from '../domain/herencia'
import type { Linaje } from '../domain/affinity'
import { cargarDatos, crearModelo } from '../data/loadData'
import { ArbolesRepository, fusionarArbol, type ArbolGuardado } from '../data/arboles'
import { ElencoRepository } from '../data/elenco'
import {
  EstiloAvatar,
  Idioma,
  ModoGrilla,
  PrefsRepository,
  TamanoTexto,
  ThemeMode,
} from '../data/prefs'
import {
  AgregarResultado,
  calcularResultado,
  QuitarResultado,
  ToggleResultado,
  type ResultadoCompat,
} from './resultado'
import { decodificarSeleccion } from './compartir'

export class AppStore {
  private prefs = new PrefsRepository()
  private arbolesRepo = new ArbolesRepository()
  private elencoRepo = new ElencoRepository()
  private snapshotBienvenida: { tema: ThemeMode; tamano: TamanoTexto; negrita: boolean } | null = null
  private errorCarga: string | null = null

  modelo = $state.raw<AffinityModel | null>(null)
  seleccion = $state.raw<Seleccion>(seleccionVacia())
  /* Slot marcado como destino de la próxima colocación; null = automático
     (el primer hueco válido). Mismo criterio que el panel de la burbuja. */
  slotDestino = $state.raw<number | null>(null)
  resultado = $derived<ResultadoCompat | null>(
    this.modelo === null ? null : calcularResultado(this.modelo, this.seleccion),
  )
  modoGrilla = $state(this.prefs.modoGrilla)
  estiloAvatar = $state<EstiloAvatar>(this.prefs.estiloAvatar)
  tema = $state<ThemeMode>(this.prefs.tema)
  idioma = $state<Idioma>(this.prefs.idioma)
  tamanoTexto = $state<TamanoTexto>(this.prefs.tamanoTexto)
  textoNegrita = $state(this.prefs.textoNegrita)
  mostrarBienvenida = $state(!this.prefs.bienvenidaAccesibilidadVista)
  arboles = $state.raw<ArbolGuardado[]>(this.arbolesRepo.todos())
  arbolPendiente = $state.raw<ArbolGuardado | null>(null)
  elenco = $state.raw<Set<number>>(this.elencoRepo.obtener())
  cargando = $state(true)

  get fallo(): string | null {
    return this.errorCarga
  }

  /* ===== Carga inicial (fuera del hilo de render) ===== */

  async init(): Promise<void> {
    try {
      const datos = await cargarDatos()
      const modelo = crearModelo(datos)
      this.modelo = modelo
    } catch (error) {
      this.errorCarga = error instanceof Error ? error.message : 'Error desconocido'
      console.error('No se pudieron cargar los datos datamined', error)
    } finally {
      this.cargando = false
    }
  }

  /* Aplica una selección compartida por URL (si es válida). */
  aplicarSeleccionCompartida(raw: string | null): boolean {
    if (this.modelo === null) return false
    const sel = decodificarSeleccion(raw, (id) => this.modelo!.porId(id) !== null)
    if (sel === null) return false
    this.setSeleccion(sel)
    return true
  }

  /* ===== Preferencias ===== */

  setModoGrilla(modo: ModoGrilla): void {
    this.prefs.modoGrilla = modo
    this.modoGrilla = modo
  }

  setEstiloAvatar(valor: EstiloAvatar): void {
    this.prefs.estiloAvatar = valor
    this.estiloAvatar = valor
  }

  setTema(modo: ThemeMode): void {
    this.prefs.tema = modo
    this.tema = modo
  }

  setIdioma(valor: Idioma): void {
    this.prefs.idioma = valor
    this.idioma = valor
  }

  setTamanoTexto(valor: TamanoTexto): void {
    this.prefs.tamanoTexto = valor
    this.tamanoTexto = valor
  }

  setTextoNegrita(valor: boolean): void {
    this.prefs.textoNegrita = valor
    this.textoNegrita = valor
  }

  /* ===== Bienvenida de accesibilidad ===== */

  abrirBienvenida(): void {
    this.snapshotBienvenida = {
      tema: this.tema,
      tamano: this.tamanoTexto,
      negrita: this.textoNegrita,
    }
    this.mostrarBienvenida = true
  }

  confirmarBienvenida(): void {
    this.snapshotBienvenida = null
    this.prefs.bienvenidaAccesibilidadVista = true
    this.mostrarBienvenida = false
  }

  omitirBienvenida(): void {
    const snapshot = this.snapshotBienvenida
    if (snapshot !== null) {
      this.setTema(snapshot.tema)
      this.setTamanoTexto(snapshot.tamano)
      this.setTextoNegrita(snapshot.negrita)
    }
    this.snapshotBienvenida = null
    this.prefs.bienvenidaAccesibilidadVista = true
    this.mostrarBienvenida = false
  }

  /* ===== Selección / herencia ===== */

  private setSeleccion(seleccion: Seleccion): void {
    this.seleccion = [...seleccion]
  }

  toggle(id: number): ToggleResultado {
    const destino = this.slotDestino
    const colocacion = alternar(this.seleccion, id, destino)
    switch (colocacion.resultado) {
      case ColocacionResultado.COLOCADO:
        this.setSeleccion(colocacion.seleccion)
        if (destino !== null) this.slotDestino = null
        return ToggleResultado.COLOCADO
      case ColocacionResultado.QUITADO:
        this.setSeleccion(colocacion.seleccion)
        return ToggleResultado.QUITADO
      case ColocacionResultado.COMPLETA:
        return ToggleResultado.SELECCION_COMPLETA
      case ColocacionResultado.REGLA:
        return ToggleResultado.REGLA
    }
  }

  /* ===== Volver a elegir un personaje ===== */

  /* Tocar un chip vacío lo marca como destino (o lo desmarca). Un chip
     ocupado lo quita la pantalla, que además pide confirmación. */
  marcarDestino(slot: number): void {
    if (this.seleccion[slot] !== null) return
    this.slotDestino = this.slotDestino === slot ? null : slot
  }

  /* Los 7 slots con su estado para este personaje: los vacíos donde las
     reglas lo dejan sirven para elegirlo de nuevo (el hijo también puede ser
     abuelo) y los que ya ocupa se pueden quitar. */
  slotsPara(id: number): SlotOpcion[] {
    return slotsPara(this.seleccion, id)
  }

  haySlotValido(id: number): boolean {
    return this.slotsPara(id).some((o) => o.estado === SlotEstado.VALIDO)
  }

  /* Segunda copia: el personaje queda además en ese slot, sin salir del que
     ya tenía. */
  agregar(id: number, slot: number): AgregarResultado {
    const nueva = agregarEn(this.seleccion, slot, id)
    if (nueva === null) return AgregarResultado.NO_PUDO
    this.setSeleccion(nueva)
    return AgregarResultado.AGREGADO
  }

  quitarSlot(i: number): QuitarResultado {
    const sel = [...this.seleccion]
    if (sel[i] === null) return QuitarResultado.OK
    if (i === 0 && sel.some((v, j) => j > 0 && v !== null)) {
      return QuitarResultado.NECESITA_CONFIRMACION
    }
    sel[i] = null
    this.setSeleccion(sel)
    return QuitarResultado.OK
  }

  confirmarQuitarSoloHijo(): void {
    const sel = [...this.seleccion]
    sel[0] = null
    this.setSeleccion(sel)
  }

  limpiarTodo(): void {
    this.setSeleccion(seleccionVacia())
    this.slotDestino = null
  }

  /* Carga una selección arbitraria (Mi corredora con alternativas). */
  cargarSeleccion(sel: Seleccion): void {
    this.setSeleccion(sel)
    this.slotDestino = null
  }

  /* Botón "Ver herencia" del top: carga el linaje completo. */
  cargarLinaje(l: Linaje): void {
    this.slotDestino = null
    this.setSeleccion([
      l.hijo.charId,
      l.padre.charId,
      l.madre.charId,
      l.abuelos[0][0].charId,
      l.abuelos[0][1].charId,
      l.abuelos[1][0].charId,
      l.abuelos[1][1].charId,
    ])
  }

  /* ===== Configuraciones de árbol guardadas ===== */

  guardarArbol(hijoId: number, nombre: string, seleccion: Seleccion, total: number): void {
    const nuevo: ArbolGuardado = {
      id: this.siguienteId(),
      hijoId,
      nombre: nombre.trim(),
      seleccion: [...seleccion],
      total,
      creadoEn: Date.now(),
    }
    this.reemplazarArboles(fusionarArbol(this.arboles, nuevo))
  }

  eliminarArbol(id: number): void {
    this.reemplazarArboles(this.arboles.filter((a) => a.id !== id))
  }

  reemplazarArboles(lista: ArbolGuardado[]): void {
    this.arbolesRepo.reemplazarTodos(lista)
    this.arboles = lista
  }

  arbolesDeHijo(hijoId: number): ArbolGuardado[] {
    return this.arboles.filter((a) => a.hijoId === hijoId)
  }

  /* Id único: evita la colisión de dos guardados en el mismo milisegundo
     (hallazgo del informe3). */
  private siguienteId(): number {
    const maximo = this.arboles.reduce((max, a) => Math.max(max, a.id), 0)
    return Math.max(Date.now(), maximo + 1)
  }

  /* Navegación pendiente: Ajustes pide abrir una config en Mi corredora. */
  abrirArbol(a: ArbolGuardado): void {
    this.arbolPendiente = a
  }

  consumirArbolPendiente(): void {
    this.arbolPendiente = null
  }

  /* ===== Mi elenco ===== */

  toggleElenco(id: number): void {
    const nuevo = new Set(this.elenco)
    if (!nuevo.delete(id)) nuevo.add(id)
    this.elencoRepo.reemplazar(nuevo)
    this.elenco = nuevo
  }

  marcarElenco(ids: Iterable<number>): void {
    const nuevo = new Set([...this.elenco, ...ids])
    this.elencoRepo.reemplazar(nuevo)
    this.elenco = nuevo
  }

  limpiarElenco(): void {
    const vacio = new Set<number>()
    this.elencoRepo.reemplazar(vacio)
    this.elenco = vacio
  }
}

export const store = new AppStore()
