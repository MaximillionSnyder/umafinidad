import { beforeEach, describe, expect, it } from 'vitest'
import { store } from '../store.svelte'
import { modelo } from '../../../test/data'

describe('store: selección compartida', () => {
  beforeEach(() => {
    store.modelo = modelo()
    store.limpiarTodo()
  })

  it('aplica una selección válida por URL', () => {
    const ok = store.aplicarSeleccionCompartida('1001-1002-1003')
    expect(ok).toBe(true)
    expect(store.seleccion.slice(0, 3)).toEqual([1001, 1002, 1003])
    expect(store.seleccion.slice(3)).toEqual([null, null, null, null])
  })

  it('rechaza una selección con ids inexistentes sin tocar el estado', () => {
    const ok = store.aplicarSeleccionCompartida('999999')
    expect(ok).toBe(false)
    expect(store.seleccion.every((v) => v === null)).toBe(true)
  })

  it('calcula el resultado al aplicar la selección', () => {
    store.aplicarSeleccionCompartida('1001-1002-1003')
    expect(store.resultado).not.toBeNull()
    expect(store.resultado?.total).toBeGreaterThan(0)
  })
})
