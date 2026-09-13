/* Estado global de la app. Espejo de ui/AppViewModel.kt, sin dependencias:
   store externo + useSyncExternalStore. */

import { useSyncExternalStore } from 'react'
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

export interface EstadoApp {
  modelo: AffinityModel | null
  seleccion: Seleccion
  resultado: ResultadoCompat | null
  modoGrilla: ModoGrilla
  estiloAvatar: EstiloAvatar
  tema: ThemeMode
  idioma: Idioma
  tamanoTexto: TamanoTexto
  textoNegrita: boolean
  mostrarBienvenida: boolean
  arboles: ArbolGuardado[]
  arbolPendiente: ArbolGuardado | null
  elenco: Set<number>
}

const seleccionVacia = (): Seleccion => Array(SLOTS).fill(null)

class AppStore {
  private prefs = new PrefsRepository()
  private arbolesRepo = new ArbolesRepository()
  private elencoRepo = new ElencoRepository()
  private listeners = new Set<() => void>()
  private snapshotBienvenida: { tema: ThemeMode; tamano: TamanoTexto; negrita: boolean } | null = null

  private estado: EstadoApp = {
    modelo: null,
    seleccion: seleccionVacia(),
    resultado: null,
    modoGrilla: this.prefs.modoGrilla,
    estiloAvatar: this.prefs.estiloAvatar,
    tema: this.prefs.tema,
    idioma: this.prefs.idioma,
    tamanoTexto: this.prefs.tamanoTexto,
    textoNegrita: this.prefs.textoNegrita,
    mostrarBienvenida: !this.prefs.bienvenidaAccesibilidadVista,
    arboles: this.arbolesRepo.todos(),
    arbolPendiente: null,
    elenco: this.elencoRepo.obtener(),
  }

  getSnapshot = (): EstadoApp => this.estado

  subscribe = (listener: () => void): (() => void) => {
    this.listeners.add(listener)
    return () => this.listeners.delete(listener)
  }

  private set(parcial: Partial<EstadoApp>): void {
    this.estado = { ...this.estado, ...parcial }
    for (const listener of this.listeners) listener()
  }

  private setSeleccion(seleccion: Seleccion): void {
    const modelo = this.estado.modelo
    this.set({
      seleccion,
      resultado: modelo === null ? null : calcularResultado(modelo, seleccion),
    })
  }

  /* ===== Carga inicial (fuera del hilo de render) ===== */

  async init(): Promise<void> {
    try {
      const datos = await cargarDatos()
      const modelo = crearModelo(datos)
      const resultado = calcularResultado(modelo, this.estado.seleccion)
      this.set({ modelo, resultado })
    } catch (error) {
      console.error('No se pudieron cargar los datos datamined', error)
    }
  }

  /* ===== Preferencias ===== */

  setModoGrilla(modo: ModoGrilla): void {
    this.prefs.modoGrilla = modo
    this.set({ modoGrilla: modo })
  }

  setEstiloAvatar(valor: EstiloAvatar): void {
    this.prefs.estiloAvatar = valor
    this.set({ estiloAvatar: valor })
  }

  setTema(modo: ThemeMode): void {
    this.prefs.tema = modo
    this.set({ tema: modo })
  }

  setIdioma(valor: Idioma): void {
    this.prefs.idioma = valor
    this.set({ idioma: valor })
  }

  setTamanoTexto(valor: TamanoTexto): void {
    this.prefs.tamanoTexto = valor
    this.set({ tamanoTexto: valor })
  }

  setTextoNegrita(valor: boolean): void {
    this.prefs.textoNegrita = valor
    this.set({ textoNegrita: valor })
  }

  /* ===== Bienvenida de accesibilidad ===== */

  abrirBienvenida(): void {
    this.snapshotBienvenida = {
      tema: this.estado.tema,
      tamano: this.estado.tamanoTexto,
      negrita: this.estado.textoNegrita,
    }
    this.set({ mostrarBienvenida: true })
  }

  confirmarBienvenida(): void {
    this.snapshotBienvenida = null
    this.prefs.bienvenidaAccesibilidadVista = true
    this.set({ mostrarBienvenida: false })
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
    this.set({ mostrarBienvenida: false })
  }

  /* ===== Selección / herencia ===== */

  toggle(id: number): ToggleResultado {
    const sel = [...this.estado.seleccion]
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
    const sel = [...this.estado.seleccion]
    if (sel[i] === null) return QuitarResultado.OK
    if (i === 0 && sel.some((v, j) => j > 0 && v !== null)) {
      return QuitarResultado.NECESITA_CONFIRMACION
    }
    sel[i] = null
    this.setSeleccion(sel)
    return QuitarResultado.OK
  }

  confirmarQuitarSoloHijo(): void {
    const sel = [...this.estado.seleccion]
    sel[0] = null
    this.setSeleccion(sel)
  }

  limpiarTodo(): void {
    this.setSeleccion(seleccionVacia())
  }

  /* Carga una selección arbitraria (Mi corredora con alternativas). */
  cargarSeleccion(sel: Seleccion): void {
    this.setSeleccion([...sel])
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
    const ahora = Date.now()
    const nuevo: ArbolGuardado = {
      id: ahora,
      hijoId,
      nombre: nombre.trim(),
      seleccion: [...seleccion],
      total,
      creadoEn: ahora,
    }
    const fusion = fusionarArbol(this.estado.arboles, nuevo)
    this.arbolesRepo.reemplazarTodos(fusion)
    this.set({ arboles: fusion })
  }

  eliminarArbol(id: number): void {
    const resto = this.estado.arboles.filter((a) => a.id !== id)
    this.arbolesRepo.reemplazarTodos(resto)
    this.set({ arboles: resto })
  }

  arbolesDeHijo(hijoId: number): ArbolGuardado[] {
    return this.estado.arboles.filter((a) => a.hijoId === hijoId)
  }

  /* Navegación pendiente: Ajustes pide abrir una config en Mi corredora. */
  abrirArbol(a: ArbolGuardado): void {
    this.set({ arbolPendiente: a })
  }

  consumirArbolPendiente(): void {
    this.set({ arbolPendiente: null })
  }

  /* ===== Mi elenco ===== */

  toggleElenco(id: number): void {
    const nuevo = new Set(this.estado.elenco)
    if (!nuevo.delete(id)) nuevo.add(id)
    this.elencoRepo.reemplazar(nuevo)
    this.set({ elenco: nuevo })
  }

  marcarElenco(ids: Iterable<number>): void {
    const nuevo = new Set([...this.estado.elenco, ...ids])
    this.elencoRepo.reemplazar(nuevo)
    this.set({ elenco: nuevo })
  }

  limpiarElenco(): void {
    const vacio = new Set<number>()
    this.elencoRepo.reemplazar(vacio)
    this.set({ elenco: vacio })
  }
}

export const store = new AppStore()

/* Hook único: devuelve el estado completo (inmutable entre updates). */
export function useAppStore(): EstadoApp {
  return useSyncExternalStore(store.subscribe, store.getSnapshot)
}
