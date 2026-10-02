import { describe, expect, it } from 'vitest'
import {
  ColocacionResultado,
  SLOTS,
  SlotEstado,
  agregarEn,
  alternar,
  puedeIrEn,
  seleccionVacia,
  slotsPara,
  type Seleccion,
} from '../herencia'

/* Volver a elegir un personaje ya colocado: el dominio es lo que decide si
   un personaje puede estar en dos slots a la vez (el hijo también de abuelo)
   y cuál está libre. */

function sel(...ids: (number | null)[]): Seleccion {
  const s = seleccionVacia()
  ids.forEach((id, i) => (s[i] = id))
  return s
}

function estados(seleccion: Seleccion, id: number): SlotEstado[] {
  return slotsPara(seleccion, id).map((o) => o.estado)
}

describe('herencia: volver a elegir el mismo personaje', () => {
  it('el hijo puede ser también abuelo, pero no padre', () => {
    const conHijo = sel(1)
    expect(estados(conHijo, 1)).toEqual([
      SlotEstado.ACTUAL, // hijo
      SlotEstado.BLOQUEADO, // padre 1: el hijo no puede ser padre
      SlotEstado.BLOQUEADO, // padre 2
      SlotEstado.VALIDO, // abuelo 1 rama 1
      SlotEstado.VALIDO,
      SlotEstado.VALIDO,
      SlotEstado.VALIDO,
    ])
  })

  it('agregar una segunda copia no saca el personaje de su slot', () => {
    const conHijo = sel(1)
    const nueva = agregarEn(conHijo, 3, 1)
    expect(nueva).not.toBeNull()
    expect(nueva![0]).toBe(1)
    expect(nueva![3]).toBe(1)
  })

  it('un padre no puede ser abuelo de su propia rama ni repetirse en ella', () => {
    const conPadre = sel(null, 2)
    expect(estados(conPadre, 2)).toEqual([
      SlotEstado.BLOQUEADO, // hijo: un padre no puede ser el hijo
      SlotEstado.ACTUAL, // padre 1
      SlotEstado.BLOQUEADO, // padre 2: los padres son distintos
      SlotEstado.BLOQUEADO, // abuelo de su rama
      SlotEstado.BLOQUEADO,
      SlotEstado.VALIDO, // la otra rama sí vale
      SlotEstado.VALIDO,
    ])
  })

  it('los dos abuelos de una misma rama no se repiten', () => {
    const conAbuelo = sel(null, null, null, 5)
    expect(estados(conAbuelo, 5)[4]).toBe(SlotEstado.BLOQUEADO)
    expect(estados(conAbuelo, 5)[6]).toBe(SlotEstado.VALIDO)
  })

  it('un slot con otro personaje sale ocupado y no se puede agregar ahí', () => {
    const conOtro = sel(null, null, null, 9)
    expect(slotsPara(conOtro, 5)[3].estado).toBe(SlotEstado.OCUPADO)
    expect(agregarEn(conOtro, 3, 5)).toBeNull()
  })

  it('agregar respeta el rango del slot', () => {
    expect(agregarEn(seleccionVacia(), -1, 1)).toBeNull()
    expect(agregarEn(seleccionVacia(), SLOTS, 1)).toBeNull()
  })

  it('sin destino alternar quita, con destino mueve', () => {
    const colocado = alternar(seleccionVacia(), 1).seleccion
    expect(alternar(colocado, 1).resultado).toBe(ColocacionResultado.QUITADO)

    const movido = alternar(colocado, 1, 5)
    expect(movido.resultado).toBe(ColocacionResultado.COLOCADO)
    /* Mover: se libera el slot viejo (la segunda copia la agrega agregarEn). */
    expect(movido.seleccion).toEqual(sel(null, null, null, null, null, 1))
  })

  it('con destino, una selección llena y un slot no válido no rompen', () => {
    const llena = sel(1, 2, 3, 4, 5, 6, 7)
    const r = alternar(llena, 8, 5)
    expect(r.resultado).toBe(ColocacionResultado.REGLA)
    expect(r.seleccion).toBe(llena)
  })

  it('la segunda copia es legal para puedeIrEn pero la automática no', () => {
    const conHijo = sel(1)
    expect(puedeIrEn(conHijo, 3, 1)).toBe(true)
    expect(puedeIrEn(conHijo, 1, 1)).toBe(false)
  })
})