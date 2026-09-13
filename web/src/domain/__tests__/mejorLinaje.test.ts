/* Tests del mejor linaje exacto por corredora (mejorLinajeDe) y de las
   alternativas por slot. Espejo de MejorLinajeTest.kt. */

import { describe, expect, it } from 'vitest'
import { armarArbol, puedeIrEn, TipoVinculo, vinculos, type Seleccion as Sel } from '../herencia'
import { modelo } from '../../test/data'

const m = modelo()

function seleccionDeTop1(): Sel {
  const l = m.topLinajes(1)[0]
  return [
    l.hijo.charId,
    l.padre.charId,
    l.madre.charId,
    l.abuelos[0][0].charId,
    l.abuelos[0][1].charId,
    l.abuelos[1][0].charId,
    l.abuelos[1][1].charId,
  ]
}

function totalPublico(seleccion: Sel): number {
  return vinculos(armarArbol(seleccion)).reduce((total, v) => {
    if (v.esCorredora) return total
    if (v.tipo === TipoVinculo.HIJO_PADRE_ABUELO) {
      return total + m.puntajeTrio(v.ids[0], v.ids[1], v.ids[2])
    }
    return total + m.puntajePar(v.ids[0], v.ids[1])
  }, 0)
}

describe('mejorLinajeDe', () => {
  it('el óptimo supera al heurístico del top global', () => {
    const top1 = m.topLinajes(1)[0]
    const suMejor = m.mejorLinajeDe(top1.hijo.charId)
    expect(suMejor).not.toBeNull()
    expect(suMejor!.puntos).toBeGreaterThanOrEqual(top1.puntos)
  })

  it('la estructura respeta las reglas del juego', () => {
    const top5 = m.topLinajes(5)
    for (const l of top5) {
      const mejor = m.mejorLinajeDe(l.hijo.charId)!
      expect(mejor.hijo.charId).toBe(l.hijo.charId)
      const padresIds = [mejor.padre.charId, mejor.madre.charId]
      expect(new Set(padresIds).size).toBe(2)
      expect(padresIds).not.toContain(mejor.hijo.charId)

      mejor.abuelos.forEach((abuelos, rama) => {
        for (const a of abuelos) {
          expect(a.charId, `abuelo de ${rama} ≠ padre de su rama`).not.toBe(padresIds[rama])
        }
        expect(abuelos[0].charId).not.toBe(abuelos[1].charId)
      })
    }
  })

  it('el total es coherente con los puntajes públicos', () => {
    const top3 = m.topLinajes(3)
    for (const l of top3) {
      const mejor = m.mejorLinajeDe(l.hijo.charId)!
      const seleccion: Sel = [
        mejor.hijo.charId,
        mejor.padre.charId,
        mejor.madre.charId,
        mejor.abuelos[0][0].charId,
        mejor.abuelos[0][1].charId,
        mejor.abuelos[1][0].charId,
        mejor.abuelos[1][1].charId,
      ]
      expect(totalPublico(seleccion)).toBe(mejor.puntos)
    }
  })

  it('es determinista', () => {
    const id = m.topLinajes(2)[1].hijo.charId
    const a = m.mejorLinajeDe(id)
    const b = m.mejorLinajeDe(id)
    expect(a?.puntos).toBe(b?.puntos)
    expect(a?.padre.charId).toBe(b?.padre.charId)
    expect(a?.madre.charId).toBe(b?.madre.charId)
  })

  it('personaje inválido devuelve null', () => {
    expect(m.mejorLinajeDe(999999999)).toBeNull()
  })
})

describe('alternativasParaSlot', () => {
  it('ordenadas, válidas y sin el ocupante', () => {
    const sel = seleccionDeTop1()
    const optimoHijo = m.mejorLinajeDe(sel[0]!)!.puntos
    for (let slot = 1; slot <= 6; slot++) {
      const alts = m.alternativasParaSlot([...sel], slot)
      if (alts.length === 0) continue

      for (let i = 1; i < alts.length; i++) {
        expect(alts[i - 1].total, `slot ${slot} fila ${i}`).toBeGreaterThanOrEqual(alts[i].total)
      }
      for (const alt of alts) {
        expect(alt.personaje.charId, '≠ ocupante').not.toBe(sel[slot])
        expect(puedeIrEn(sel, slot, alt.personaje.charId), `puedeIrEn(slot ${slot})`).toBe(true)
        expect(alt.total, `total ${alt.total} <= óptimo ${optimoHijo}`).toBeLessThanOrEqual(optimoHijo)
      }
    }
  })

  it('el total de la alternativa es coherente con los puntajes públicos', () => {
    const sel = seleccionDeTop1()
    const alts = m.alternativasParaSlot([...sel], 1)
    const primera = alts[0]
    if (!primera) return

    const nuevo = [...sel]
    nuevo[1] = primera.personaje.charId
    expect(totalPublico(nuevo)).toBe(primera.total)
  })

  it('el hijo no es intercambiable', () => {
    const sel = seleccionDeTop1()
    expect(m.alternativasParaSlot([...sel], 0)).toEqual([])
  })
})
