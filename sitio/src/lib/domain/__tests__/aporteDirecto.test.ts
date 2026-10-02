/* Aporte directo por personaje que la lista de Mi corredora muestra en cada
   fila: los vínculos del personaje dentro del árbol, con la misma semántica
   que el "pt" de la hoja de alternativas. Espejo de AporteDirectoTest.kt. */

import { describe, expect, it } from 'vitest'
import { modelo } from '../../../test/data'
import { armarArbol, SLOTS, TipoVinculo, vinculos, type Seleccion } from '../herencia'

const m = modelo()

function totalPublico(seleccion: Seleccion): number {
  return vinculos(armarArbol(seleccion)).reduce((total, v) => {
    if (v.esCorredora) return total
    if (v.tipo === TipoVinculo.HIJO_PADRE_ABUELO) {
      return total + m.puntajeTrio(v.ids[0], v.ids[1], v.ids[2])
    }
    return total + m.puntajePar(v.ids[0], v.ids[1])
  }, 0)
}

function seleccionOptima(): Seleccion {
  const l = m.mejorLinajeDe(1001)!
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

/* Expresión manual con los puntajes públicos, independiente del helper. */
function aporteManual(sel: Seleccion, slot: number): number {
  const h = sel[0]!
  if (slot === 0) {
    let d = 0
    for (const p of [sel[1], sel[2]]) {
      if (p !== null) d += m.puntajePar(h, p)
    }
    for (let s = 3; s <= 6; s++) {
      const g = sel[s]
      if (g === null || g === h) continue
      d += m.puntajeTrio(h, sel[1 + Math.floor((s - 3) / 2)]!, g)
    }
    return d
  }
  if (slot <= 2) {
    const p = sel[slot]!
    const otro = sel[slot === 1 ? 2 : 1]!
    let d = m.puntajePar(h, p) + m.puntajePar(p, otro)
    const rama = slot - 1
    for (const s of [3 + rama * 2, 4 + rama * 2]) {
      const g = sel[s]
      if (g !== null && g !== p && g !== h) d += m.puntajeTrio(h, p, g)
    }
    return d
  }
  const p = sel[1 + Math.floor((slot - 3) / 2)]!
  const g = sel[slot]!
  return g === h ? 0 : m.puntajeTrio(h, p, g)
}

describe('aportesDirectos', () => {
  it('cada slot coincide con la suma manual de sus vínculos', () => {
    const seleccion = seleccionOptima()
    const aportes = m.aportesDirectos(seleccion)
    expect(aportes).toHaveLength(SLOTS)
    for (let slot = 0; slot < SLOTS; slot++) {
      expect(aportes[slot], `slot ${slot}`).toBe(aporteManual(seleccion, slot))
    }
  })

  it('el hijo suma todos los vínculos menos el par entre padres', () => {
    const seleccion = seleccionOptima()
    const total = totalPublico(seleccion)
    const parPadres = m.puntajePar(seleccion[1]!, seleccion[2]!)
    expect(m.aportesDirectos(seleccion)[0]).toBe(total - parPadres)
  })

  it('la corredora como abuela aporta 0', () => {
    const seleccion = [...seleccionOptima()]
    seleccion[3] = seleccion[0]
    const aportes = m.aportesDirectos(seleccion)
    expect(aportes[3]).toBe(0)
    expect(aportes[0]).toBe(
      totalPublico(seleccion) - m.puntajePar(seleccion[1]!, seleccion[2]!),
    )
  })

  it('los slots vacíos aportan 0', () => {
    const [h, p1] = seleccionOptima()
    const aportes = m.aportesDirectos([h!, p1!, null, null, null, null, null])
    expect(aportes).toHaveLength(SLOTS)
    expect(aportes[0]).toBe(m.puntajePar(h!, p1!))
    expect(aportes[1]).toBe(m.puntajePar(h!, p1!))
    for (let slot = 2; slot < SLOTS; slot++) expect(aportes[slot]).toBe(0)
  })

  it('coincide con los puntos directos de las alternativas', () => {
    const seleccion = seleccionOptima()
    for (let slot = 1; slot <= 6; slot++) {
      const alternativas = m.alternativasParaSlot(seleccion, slot)
      if (alternativas.length === 0) continue
      const alt = alternativas[0]
      expect(m.aporteDirectoDeCandidato(seleccion, slot, alt.personaje.charId)).toBe(
        alt.puntosDirectos,
      )
    }
  })
})
