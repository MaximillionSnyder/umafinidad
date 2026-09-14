/* Estado global de la app. Espejo de ui/AppViewModel.kt, con reactividad de
   Svelte 5 (runas). Los estados grandes/estructurados usan $state.raw porque
   siempre se reemplazan por copias nuevas. */

import { AffinityModel } from '../domain/affinity'
import { SLOTS, slotPara, puedeIrEn, type Seleccion } from '../domain/herencia'
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
import { calcularResultado, QuitarResultado, ToggleResultado, type ResultadoCompat } from './resultado'
import { decodificarSeleccion } from './compartir'

const seleccionVacia = (): Seleccion => Array(SLOTS).fill(null)

export class AppStore {
  private prefs = new PrefsRepository()
  private arbolesRepo = new ArbolesRepository()
  private elencoRepo = new ElencoRepository()
  private snapshotBienvenida: { tema: ThemeMode; tamano: TamanoTexto; negrita: boolean } | null = null
  private errorCarga: string | null = null

  modelo = $state.raw<AffinityModel | null>(null)
  seleccion = $state.raw<Seleccion>(seleccionVacia())
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
    const sel = [...this.seleccion]
    const posiciones: number[] = []
    sel.forEach((v, i) => {
      if (v === id) posiciones.push(i)
    })
    if (posiciones.length > 0) {
      this.quitarSlot(posiciones[posiciones.length - 1])
      return ToggleResultado.QUITADO
    }
    const slot = slotPara(sel, id)
    if (slot >= 0 && puedeIrEn(sel, slot, id)) {
      sel[slot] = id
      this.setSeleccion(sel)
      return ToggleResultado.COLOCADO
    }
    return slot === -1 ? ToggleResultado.SELECCION_COMPLETA : ToggleResultado.REGLA
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
  }

  /* Carga una selección arbitraria (Mi corredora con alternativas). */
  cargarSeleccion(sel: Seleccion): void {
    this.setSeleccion(sel)
  }

  /* Botón "Ver herencia" del top: carga el linaje completo. */
  cargarLinaje(l: Linaje): void {
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
