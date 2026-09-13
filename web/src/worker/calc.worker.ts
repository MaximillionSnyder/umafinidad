/* Worker de cálculo: rankings, top de linajes y alternativas por slot.
   Corre fuera del hilo principal (equivalente a Dispatchers.Default en
   Android) para no congelar la UI. */

/// <reference lib="webworker" />

import { cargarDatos, crearModelo } from '../data/loadData'
import type { AffinityModel } from '../domain/affinity'
import type { PeticionCalculo } from './mensajes'

let modeloPromesa: Promise<AffinityModel> | null = null

function obtenerModelo(): Promise<AffinityModel> {
  if (modeloPromesa === null) {
    modeloPromesa = cargarDatos().then(crearModelo)
  }
  return modeloPromesa
}

self.onmessage = async (evento: MessageEvent<PeticionCalculo>) => {
  const peticion = evento.data
  try {
    const modelo = await obtenerModelo()
    let resultado: unknown
    switch (peticion.tipo) {
      case 'topLinajes':
        resultado = modelo.topLinajes(peticion.n)
        break
      case 'topLinajesDeElenco':
        resultado = modelo.topLinajesDeElenco(peticion.ids, peticion.n)
        break
      case 'rankingAfinidad':
        resultado = modelo.rankingAfinidad()
        break
      case 'rankingPadres':
        resultado = modelo.rankingPadres()
        break
      case 'mejorLinajeDe':
        resultado = modelo.mejorLinajeDe(peticion.hijoId)
        break
      case 'alternativasParaSlot':
        resultado = modelo.alternativasParaSlot(peticion.seleccion, peticion.slot, peticion.limite)
        break
    }
    self.postMessage({ id: peticion.id, resultado })
  } catch (error) {
    self.postMessage({ id: peticion.id, error: error instanceof Error ? error.message : String(error) })
  }
}
