/* Configuraciones de árbol guardadas por el usuario en "Mi corredora".
   Espejo de data/ArbolesRepository.kt (localStorage en vez de
   SharedPreferences). */

import type { Seleccion } from '../domain/herencia'

export interface ArbolGuardado {
  id: number
  hijoId: number
  nombre: string
  /* Los 7 slots; nulls permitidos por si guarda algo incompleto. */
  seleccion: Seleccion
  total: number
  creadoEn: number
}

/* ===== Lógica pura (testeable sin navegador) ===== */

export function serializarArboles(arboles: ArbolGuardado[]): string {
  return JSON.stringify(arboles)
}

export function deserializarArboles(json: string): ArbolGuardado[] {
  const datos = JSON.parse(json) as ArbolGuardado[]
  if (!Array.isArray(datos)) throw new Error('Formato de árboles inválido')
  return datos
}

/* Dedupe: si ya existía una config del mismo hijo con selección idéntica,
   se reemplaza; si no, se agrega. Devuelve ordenada por fecha desc. */
export function fusionarArbol(existente: ArbolGuardado[], nuevo: ArbolGuardado): ArbolGuardado[] {
  const sinDuplicado = existente.filter(
    (a) => !(a.hijoId === nuevo.hijoId && JSON.stringify(a.seleccion) === JSON.stringify(nuevo.seleccion)),
  )
  return [nuevo, ...sinDuplicado].sort((a, b) => b.creadoEn - a.creadoEn)
}

/* ===== Wrapper de localStorage ===== */

const CLAVE = 'arboles_guardados.lista'

export class ArbolesRepository {
  todos(): ArbolGuardado[] {
    try {
      const raw = localStorage.getItem(CLAVE)
      return raw === null ? [] : deserializarArboles(raw)
    } catch {
      return []
    }
  }

  reemplazarTodos(lista: ArbolGuardado[]): void {
    try {
      localStorage.setItem(CLAVE, serializarArboles(lista))
    } catch {
      /* sin storage disponible */
    }
  }
}
