/* Resultado de compatibilidad. Porte de AppViewModel.calcular()
   (que a su vez porta montarResultado() de result.js). */

import type { AffinityModel, GrupoCompartido, Rango } from '../domain/affinity'
import { armarArbol, TipoVinculo, vinculos, type Seleccion, type Vinculo } from '../domain/herencia'

/* Resultado de intentar colocar/quitar un personaje. La pantalla traduce
   estos casos a mensajes localizados. */
export enum ToggleResultado {
  COLOCADO = 'COLOCADO',
  QUITADO = 'QUITADO',
  SELECCION_COMPLETA = 'SELECCION_COMPLETA',
  REGLA = 'REGLA',
}

export enum QuitarResultado {
  OK = 'OK',
  NECESITA_CONFIRMACION = 'NECESITA_CONFIRMACION',
}

/* Estado de cada sección del resultado (equivale a las notas de result.js). */
export enum EstadoSeccion {
  CON_FILAS = 'CON_FILAS',
  FALTA_HIJO = 'FALTA_HIJO',
  ELIGE_PADRE = 'ELIGE_PADRE',
  OTRO_PADRE = 'OTRO_PADRE',
  FALTAN_PADRES = 'FALTAN_PADRES',
  SIN_ABUELOS = 'SIN_ABUELOS',
}

export interface FilaVinculoUi {
  ids: number[]
  puntos: number
  rango: Rango | null
  esCorredora: boolean
  compartidos: GrupoCompartido[]
}

export interface ResultadoCompat {
  vacio: boolean
  total: number | null
  rangoTotal: Rango | null
  hijoPadres: FilaVinculoUi[]
  estadoHijoPadres: EstadoSeccion
  entrePadres: FilaVinculoUi | null
  estadoEntrePadres: EstadoSeccion
  hijoPadreAbuelos: FilaVinculoUi[]
  estadoHijoPadreAbuelos: EstadoSeccion
  notaSinHijo: boolean
}

function fila(modelo: AffinityModel, v: Vinculo): FilaVinculoUi {
  const puntos = v.esCorredora
    ? 0
    : v.ids.length === 3
      ? modelo.puntajeTrio(v.ids[0], v.ids[1], v.ids[2])
      : modelo.puntajePar(v.ids[0], v.ids[1])
  return {
    ids: v.ids,
    puntos,
    rango: modelo.rango(puntos),
    esCorredora: v.esCorredora,
    compartidos: modelo.gruposCompartidos(v.ids),
  }
}

export function calcularResultado(modelo: AffinityModel, seleccion: Seleccion): ResultadoCompat {
  if (seleccion.every((v) => v === null)) {
    return {
      vacio: true,
      total: null,
      rangoTotal: null,
      hijoPadres: [],
      estadoHijoPadres: EstadoSeccion.FALTA_HIJO,
      entrePadres: null,
      estadoEntrePadres: EstadoSeccion.FALTAN_PADRES,
      hijoPadreAbuelos: [],
      estadoHijoPadreAbuelos: EstadoSeccion.FALTA_HIJO,
      notaSinHijo: false,
    }
  }

  const arbol = armarArbol(seleccion)
  const vs = vinculos(arbol)

  const total = vs.reduce((suma, v) => {
    if (v.esCorredora) return suma
    if (v.ids.length === 3) return suma + modelo.puntajeTrio(v.ids[0], v.ids[1], v.ids[2])
    return suma + modelo.puntajePar(v.ids[0], v.ids[1])
  }, 0)

  const hp = vs.filter((v) => v.tipo === TipoVinculo.HIJO_PADRE).map((v) => fila(modelo, v))
  const ep = vs.filter((v) => v.tipo === TipoVinculo.ENTRE_PADRES).map((v) => fila(modelo, v))
  const hpa = vs
    .filter((v) => v.tipo === TipoVinculo.HIJO_PADRE_ABUELO)
    .map((v) => fila(modelo, v))

  const ambosPadres = arbol.padres[0] !== null && arbol.padres[1] !== null
  const algunPadre = arbol.padres.some((p) => p !== null)

  return {
    vacio: false,
    total,
    rangoTotal: modelo.rangoTotal(total),
    hijoPadres: hp,
    estadoHijoPadres:
      hp.length > 0
        ? EstadoSeccion.CON_FILAS
        : arbol.hijo === null
          ? EstadoSeccion.FALTA_HIJO
          : EstadoSeccion.ELIGE_PADRE,
    entrePadres: ep[0] ?? null,
    estadoEntrePadres: ambosPadres
      ? EstadoSeccion.CON_FILAS
      : algunPadre
        ? EstadoSeccion.OTRO_PADRE
        : EstadoSeccion.FALTAN_PADRES,
    hijoPadreAbuelos: hpa,
    estadoHijoPadreAbuelos:
      arbol.hijo === null
        ? EstadoSeccion.FALTA_HIJO
        : hpa.length === 0
          ? EstadoSeccion.SIN_ABUELOS
          : EstadoSeccion.CON_FILAS,
    notaSinHijo: arbol.hijo === null,
  }
}
