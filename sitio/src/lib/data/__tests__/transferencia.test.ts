import { describe, expect, it } from 'vitest'
import {
  exportarArboles,
  fusionarImportados,
  importarArboles,
  VERSION_PAQUETE,
} from '../transferencia'
import type { ArbolGuardado } from '../arboles'

function arbol(id: number, hijoId: number, nombre: string): ArbolGuardado {
  return {
    id,
    hijoId,
    nombre,
    seleccion: [hijoId, 1001, 1002, null, null, null, null],
    total: 42,
    creadoEn: 1_700_000_000_000 + id,
  }
}

describe('exportar / importar', () => {
  it('hace round-trip con el paquete versionado', () => {
    const arboles = [arbol(1, 1001, 'A'), arbol(2, 1002, 'B')]
    const json = exportarArboles(arboles)
    const paquete = JSON.parse(json) as { version: number }
    expect(paquete.version).toBe(VERSION_PAQUETE)
    expect(importarArboles(json)).toEqual(arboles)
  })

  it('acepta un array plano por compatibilidad', () => {
    const arboles = [arbol(1, 1001, 'A')]
    expect(importarArboles(JSON.stringify(arboles))).toEqual(arboles)
  })

  it('rechaza JSON inválido', () => {
    expect(() => importarArboles('no es json')).toThrow()
  })

  it('rechaza archivos sin lista', () => {
    expect(() => importarArboles(JSON.stringify({ version: 1, arboles: 'x' }))).toThrow()
  })

  it('descarta entradas inválidas y falla si no queda ninguna', () => {
    const bueno = arbol(1, 1001, 'A')
    const json = JSON.stringify({ version: 1, arboles: [bueno, { nombre: 'roto' }] })
    expect(importarArboles(json)).toEqual([bueno])
    expect(() => importarArboles(JSON.stringify({ version: 1, arboles: [{ nombre: 'roto' }] }))).toThrow()
  })
})

describe('fusionarImportados', () => {
  it('reasigna ids que colisionan y deduplica por hijo + selección', () => {
    const existente = [arbol(1, 1001, 'local')]
    const importadoMismoContenido = arbol(1, 1001, 'local')
    const importadoNuevo = arbol(2, 1002, 'nuevo')
    const fusion = fusionarImportados(existente, [importadoMismoContenido, importadoNuevo])
    expect(fusion).toHaveLength(2)
    const ids = fusion.map((a) => a.id)
    expect(new Set(ids).size).toBe(2)
    expect(fusion.some((a) => a.hijoId === 1002)).toBe(true)
  })
})
