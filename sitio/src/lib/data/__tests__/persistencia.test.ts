/* Persistencia pura de configuraciones guardadas. Espejo de ArbolesTest.kt
   + round-trip del elenco. */

import { describe, expect, it } from 'vitest'
import {
  deserializarArboles,
  fusionarArbol,
  serializarArboles,
  type ArbolGuardado,
} from '../arboles'
import { deserializarElenco, serializarElenco } from '../elenco'

function ejemplo(id: number, hijoId: number, sel: (number | null)[], creadoEn: number): ArbolGuardado {
  return { id, hijoId, nombre: 'test', seleccion: sel, total: 100, creadoEn }
}

const selCompleta: (number | null)[] = [1001, 1015, 1023, 1015, 1045, null, 1030]

describe('árboles guardados', () => {
  it('round-trip con nulls', () => {
    const lista = [ejemplo(1, 1001, selCompleta, 100)]
    const vuelta = deserializarArboles(serializarArboles(lista))
    expect(vuelta).toEqual(lista)
    expect(vuelta[0].seleccion).toContain(null)
  })

  it('lista vacía round-trip', () => {
    expect(deserializarArboles(serializarArboles([]))).toEqual([])
  })

  it('selección idéntica reemplaza (dedupe)', () => {
    const vieja = ejemplo(1, 1001, selCompleta, 100)
    const nueva = ejemplo(2, 1001, selCompleta, 200)
    const fusion = fusionarArbol([vieja], nueva)
    expect(fusion.length).toBe(1)
    expect(fusion[0].id).toBe(2)
  })

  it('selección distinta agrega', () => {
    const a = ejemplo(1, 1001, selCompleta, 100)
    const b = ejemplo(2, 1001, [1001, 1015, 1023, null, null, null, null], 200)
    expect(fusionarArbol([a], b).length).toBe(2)
  })

  it('otro hijo no se considera duplicado', () => {
    const a = ejemplo(1, 1001, selCompleta, 100)
    const b = ejemplo(2, 1042, selCompleta, 200)
    expect(fusionarArbol([a], b).length).toBe(2)
  })

  it('ordenado por fecha descendente', () => {
    const vieja = ejemplo(1, 1001, selCompleta, 100)
    const media = ejemplo(2, 1042, selCompleta, 150)
    const nueva = ejemplo(3, 1071, selCompleta, 200)
    const fusion = fusionarArbol([vieja, media], nueva)
    expect(fusion.map((a) => a.id)).toEqual([3, 2, 1])
  })
})

describe('elenco', () => {
  it('round-trip de ids', () => {
    const ids = new Set([1001, 1042, 1071])
    expect(deserializarElenco(serializarElenco(ids))).toEqual(ids)
  })

  it('round-trip vacío', () => {
    expect(deserializarElenco(serializarElenco(new Set()))).toEqual(new Set())
  })
})
