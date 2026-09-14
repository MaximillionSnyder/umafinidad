/* Export/import de las configuraciones guardadas como archivo JSON
   versionado. La importación valida el formato y nunca rompe los datos
   existentes: los inválidos se descartan. */

import { SLOTS } from '../domain/herencia'
import { fusionarArbol, type ArbolGuardado } from './arboles'

export const VERSION_PAQUETE = 1

export interface PaqueteArboles {
  version: number
  exportadoEn: string
  arboles: ArbolGuardado[]
}

export function exportarArboles(arboles: ArbolGuardado[]): string {
  const paquete: PaqueteArboles = {
    version: VERSION_PAQUETE,
    exportadoEn: new Date().toISOString(),
    arboles,
  }
  return `${JSON.stringify(paquete, null, 2)}\n`
}

function esArbol(valor: unknown): valor is ArbolGuardado {
  if (typeof valor !== 'object' || valor === null) return false
  const a = valor as Record<string, unknown>
  if (typeof a.id !== 'number' || typeof a.hijoId !== 'number') return false
  if (typeof a.nombre !== 'string' || typeof a.total !== 'number') return false
  if (typeof a.creadoEn !== 'number') return false
  if (!Array.isArray(a.seleccion) || a.seleccion.length !== SLOTS) return false
  return a.seleccion.every((v) => v === null || (typeof v === 'number' && Number.isSafeInteger(v)))
}

/* Acepta el paquete versionado o un array plano (compatibilidad). */
export function importarArboles(json: string): ArbolGuardado[] {
  let datos: unknown
  try {
    datos = JSON.parse(json)
  } catch {
    throw new Error('El archivo no es un JSON válido.')
  }
  const lista = Array.isArray(datos)
    ? datos
    : typeof datos === 'object' && datos !== null
      ? (datos as Record<string, unknown>).arboles
      : null
  if (!Array.isArray(lista)) throw new Error('El archivo no contiene una lista de configuraciones.')
  const validos = lista.filter(esArbol)
  if (validos.length === 0) throw new Error('El archivo no contiene configuraciones válidas.')
  return validos
}

/* Fusiona importados con los existentes: reasigna ids que colisionen y
   deduplica por hijo + selección (misma regla que guardar). */
export function fusionarImportados(
  existentes: ArbolGuardado[],
  importados: ArbolGuardado[],
): ArbolGuardado[] {
  let resultado = existentes
  let idMaximo = existentes.reduce((max, a) => Math.max(max, a.id), 0)
  for (const arbol of importados) {
    let id = arbol.id
    if (resultado.some((a) => a.id === id)) {
      idMaximo += 1
      id = idMaximo
    } else {
      idMaximo = Math.max(idMaximo, id)
    }
    resultado = fusionarArbol(resultado, { ...arbol, id })
  }
  return resultado
}
