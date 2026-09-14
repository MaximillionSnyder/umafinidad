/* Cliente del worker de cálculo con API de promesas. Si el entorno no
   soporta Workers (tests), cae a cálculo síncrono con un modelo local. */

import type { AlternativaSlot, AffinityModel, Linaje, RankingAfinidad, RankingPadre } from '../domain/affinity'
import { cargarDatos, crearModelo } from '../data/loadData'
import type { PeticionCalculo, RespuestaCalculo } from './mensajes'

type DistributiveOmit<T, K extends PropertyKey> = T extends unknown ? Omit<T, K> : never
type SinId = DistributiveOmit<PeticionCalculo, 'id'>

let worker: Worker | null = null
let contador = 0
const pendientes = new Map<number, { resolver: (valor: never) => void; rechazar: (error: Error) => void }>()

function obtenerWorker(): Worker {
  if (worker === null) {
    worker = new Worker(new URL('./calc.worker.ts', import.meta.url), { type: 'module' })
    worker.onmessage = (evento: MessageEvent<RespuestaCalculo & { error?: string }>) => {
      const pendiente = pendientes.get(evento.data.id)
      if (!pendiente) return
      pendientes.delete(evento.data.id)
      if (evento.data.error !== undefined) pendiente.rechazar(new Error(evento.data.error))
      else pendiente.resolver(evento.data.resultado as never)
    }
    worker.onerror = (evento) => {
      for (const pendiente of pendientes.values()) pendiente.rechazar(new Error(evento.message))
      pendientes.clear()
    }
  }
  return worker
}

function hayWorker(): boolean {
  return typeof Worker !== 'undefined'
}

let modeloSync: AffinityModel | null = null
async function obtenerModeloSync(): Promise<AffinityModel> {
  if (modeloSync === null) modeloSync = crearModelo(await cargarDatos())
  return modeloSync
}

async function pedir<T>(peticion: SinId): Promise<T> {
  if (!hayWorker()) {
    const modelo = await obtenerModeloSync()
    switch (peticion.tipo) {
      case 'topLinajes':
        return modelo.topLinajes(peticion.n) as T
      case 'topLinajesDeElenco':
        return modelo.topLinajesDeElenco(peticion.ids, peticion.n) as T
      case 'rankingAfinidad':
        return modelo.rankingAfinidad() as T
      case 'rankingPadres':
        return modelo.rankingPadres() as T
      case 'mejorLinajeDe':
        return modelo.mejorLinajeDe(peticion.hijoId) as T
      case 'alternativasParaSlot':
        return modelo.alternativasParaSlot(peticion.seleccion, peticion.slot, peticion.limite) as T
    }
  }

  const id = ++contador
  const w = obtenerWorker()
  return new Promise<T>((resolver, rechazar) => {
    pendientes.set(id, { resolver: resolver as (valor: never) => void, rechazar })
    w.postMessage({ ...peticion, id })
  })
}

export function pedirTopLinajes(n = 20): Promise<Linaje[]> {
  return pedir({ tipo: 'topLinajes', n })
}

export function pedirTopLinajesDeElenco(ids: number[], n = 20): Promise<Linaje[]> {
  return pedir({ tipo: 'topLinajesDeElenco', ids, n })
}

export function pedirRankingAfinidad(): Promise<RankingAfinidad[]> {
  return pedir({ tipo: 'rankingAfinidad' })
}

export function pedirRankingPadres(): Promise<RankingPadre[]> {
  return pedir({ tipo: 'rankingPadres' })
}

export function pedirMejorLinaje(hijoId: number): Promise<Linaje | null> {
  return pedir({ tipo: 'mejorLinajeDe', hijoId })
}

export function pedirAlternativas(
  seleccion: (number | null)[],
  slot: number,
  limite = 8,
): Promise<AlternativaSlot[]> {
  return pedir({ tipo: 'alternativasParaSlot', seleccion, slot, limite })
}
