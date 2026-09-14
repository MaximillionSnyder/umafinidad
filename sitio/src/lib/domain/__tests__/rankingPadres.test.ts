/* Ranking “Mejores padres”. Espejo de RankingPadresTest.kt. */

import { describe, expect, it } from 'vitest'
import { ids, modelo } from '../../../test/data'

const m = modelo()

describe('rankingPadres', () => {
  it('cubre todo el pool', () => {
    const ranking = m.rankingPadres()
    expect(ranking.length).toBe(ids.length)
    expect(ranking.map((e) => e.personaje.charId).sort((a, b) => a - b)).toEqual(
      [...ids].sort((a, b) => a - b),
    )
  })

  it('cada linaje aporta dos padres', () => {
    const ranking = m.rankingPadres()
    expect(ranking.reduce((suma, e) => suma + e.veces, 0)).toBe(2 * ids.length)
  })

  it('el porcentaje es coherente con las veces', () => {
    const ranking = m.rankingPadres()
    for (const entry of ranking) {
      expect(entry.porcentaje).toBeCloseTo((entry.veces * 100) / ids.length, 3)
      expect(entry.veces).toBeGreaterThanOrEqual(0)
      expect(entry.veces).toBeLessThan(ids.length)
      expect(entry.puntosMedios).toBeGreaterThanOrEqual(0)
    }
  })

  it('el orden es determinista', () => {
    const primero = m.rankingPadres()
    const segundo = m.rankingPadres()
    expect(primero).toEqual(segundo)
    for (let i = 1; i < primero.length; i++) {
      const a = primero[i - 1]
      const b = primero[i]
      if (a.veces !== b.veces) expect(a.veces).toBeGreaterThan(b.veces)
      else if (a.puntosMedios !== b.puntosMedios) expect(a.puntosMedios).toBeGreaterThan(b.puntosMedios)
      else if (a.totalAfinidad !== b.totalAfinidad) expect(a.totalAfinidad).toBeGreaterThan(b.totalAfinidad)
      else expect(a.personaje.charId).toBeLessThanOrEqual(b.personaje.charId)
    }
  })

  it('el recuento independiente coincide', () => {
    const conteo = new Map<number, number>()
    const puntos = new Map<number, number[]>()
    for (const hijoId of ids) {
      const linaje = m.mejorLinajeDe(hijoId)
      if (!linaje) continue
      for (const padreId of [linaje.padre.charId, linaje.madre.charId]) {
        conteo.set(padreId, (conteo.get(padreId) ?? 0) + 1)
        const lista = puntos.get(padreId) ?? []
        lista.push(linaje.puntos)
        puntos.set(padreId, lista)
      }
    }
    const ranking = new Map(m.rankingPadres().map((e) => [e.personaje.charId, e]))
    for (const id of ids) {
      const entry = ranking.get(id)!
      expect(entry.veces, `veces de ${id}`).toBe(conteo.get(id) ?? 0)
      const lista = puntos.get(id) ?? []
      const esperadaMedia = lista.length === 0 ? 0 : Math.trunc(lista.reduce((s, v) => s + v, 0) / lista.length)
      expect(entry.puntosMedios, `media de ${id}`).toBe(esperadaMedia)
    }
  })
})
