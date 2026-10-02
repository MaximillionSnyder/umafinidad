import { beforeEach, describe, expect, it } from 'vitest'
import { store } from '../store.svelte'
import { SlotEstado } from '../../domain/herencia'
import { AgregarResultado, ToggleResultado } from '../resultado'
import type { Linaje } from '../../domain/affinity'
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

describe('store: volver a elegir el mismo personaje', () => {
  beforeEach(() => {
    store.modelo = modelo()
    store.limpiarTodo()
  })

  it('agrega una segunda copia sin sacar el personaje del slot que tenía', () => {
    expect(store.toggle(1001)).toBe(ToggleResultado.COLOCADO)
    expect(store.seleccion[0]).toBe(1001)
    expect(store.agregar(1001, 3)).toBe(AgregarResultado.AGREGADO)
    expect(store.seleccion[0]).toBe(1001)
    expect(store.seleccion[3]).toBe(1001)
  })

  it('el slot destino manda sobre el orden y se consume al colocar', () => {
    store.marcarDestino(5)
    expect(store.slotDestino).toBe(5)
    expect(store.toggle(1001)).toBe(ToggleResultado.COLOCADO)
    expect(store.seleccion[5]).toBe(1001)
    expect(store.slotDestino).toBeNull()
  })

  it('con destino marcado un personaje ya colocado se mueve', () => {
    store.toggle(1001)
    store.marcarDestino(6)
    expect(store.toggle(1001)).toBe(ToggleResultado.COLOCADO)
    expect(store.seleccion[0]).toBeNull()
    expect(store.seleccion[6]).toBe(1001)
  })

  it('el mismo chip vacío desmarca el destino', () => {
    store.marcarDestino(4)
    store.marcarDestino(4)
    expect(store.slotDestino).toBeNull()
  })

  it('un slot ocupado no se puede marcar como destino', () => {
    store.toggle(1001)
    store.marcarDestino(0)
    expect(store.slotDestino).toBeNull()
  })

  it('no agrega donde las reglas lo vetan', () => {
    store.toggle(1001) // hijo
    store.toggle(1002) // padre 1
    expect(store.haySlotValido(1002)).toBe(true)
    expect(store.agregar(1002, 1)).toBe(AgregarResultado.NO_PUDO)
    expect(store.agregar(1001, 3)).toBe(AgregarResultado.AGREGADO)
  })

  it('slotsPara marca el slot actual y los libres donde lo dejan', () => {
    store.toggle(1001)
    const opciones = store.slotsPara(1001)
    expect(opciones).toHaveLength(7)
    expect(opciones[0].estado).toBe(SlotEstado.ACTUAL)
    expect(opciones[1].estado).toBe(SlotEstado.BLOQUEADO)
    expect(opciones[3].estado).toBe(SlotEstado.VALIDO)
  })

  it('limpiar y cargar borran el destino', () => {
    store.marcarDestino(4)
    store.limpiarTodo()
    expect(store.slotDestino).toBeNull()
    store.marcarDestino(4)
    store.cargarLinaje({
      hijo: { charId: 1001 },
      padre: { charId: 1002 },
      madre: { charId: 1003 },
      abuelos: [
        [
          { charId: 1004 },
          { charId: 1005 },
        ],
        [
          { charId: 1006 },
          { charId: 1007 },
        ],
      ],
    } as Linaje)
    expect(store.slotDestino).toBeNull()
    expect(store.seleccion[0]).toBe(1001)
  })
})
