/* Búsqueda difusa del autocompletado. Espejo de BusquedaTest.kt. */

import { describe, expect, it } from 'vitest'
import { distanciaDamerau, rankearSugerencias } from '../busqueda'
import type { Character } from '../models'

function char(id: number, en: string, jp = '', url = ''): Character {
  return {
    charId: id,
    enName: en,
    jpName: jp === '' ? en : jp,
    playable: true,
    active: true,
    urlName: url === '' ? null : url,
  }
}

const personajes = [
  char(1001, 'Special Week', 'スペシャルウィーク', 'special-week'),
  char(1032, 'Silence Suzuka', 'サイレンススズカ', 'silence-suzuka'),
  char(1042, 'Air Groove', 'エアグルーヴ', 'air-groove'),
  char(1015, 'TM Opera O', 'テイエムオペラオー', 'tm-opera-o'),
]

describe('distanciaDamerau', () => {
  it('casos básicos', () => {
    expect(distanciaDamerau('abc', 'abc')).toBe(0)
    expect(distanciaDamerau('kitten', 'sitting')).toBe(3)
    expect(distanciaDamerau('', 'abc')).toBe(3)
    expect(distanciaDamerau('ab', '')).toBe(2)
  })

  it('la transposición cuesta uno', () => {
    expect(distanciaDamerau('suzuak', 'suzuka')).toBe(1)
    expect(distanciaDamerau('ab', 'ba')).toBe(1)
  })
})

describe('rankearSugerencias', () => {
  it('susuka sugiere Suzuka', () => {
    const top = rankearSugerencias(personajes, 'susuka')
    expect(top.length).toBeGreaterThan(0)
    expect(top[0].enName).toBe('Silence Suzuka')
  })

  it('omisión y exceso de letras', () => {
    expect(rankearSugerencias(personajes, 'szuka')[0].enName).toBe('Silence Suzuka')
    expect(rankearSugerencias(personajes, 'suzukaa')[0].enName).toBe('Silence Suzuka')
    expect(rankearSugerencias(personajes, 'suzuak')[0].enName).toBe('Silence Suzuka')
  })

  it('typo en nombre compuesto', () => {
    expect(rankearSugerencias(personajes, 'special wek')[0].enName).toBe('Special Week')
  })

  it('el prefijo rankea primero', () => {
    expect(rankearSugerencias(personajes, 'spec')[0].enName).toBe('Special Week')
  })

  it('japonés exacto', () => {
    expect(rankearSugerencias(personajes, 'スペシャル')[0].enName).toBe('Special Week')
  })

  it('romaji por urlName', () => {
    expect(rankearSugerencias(personajes, 'air grove')[0].enName).toBe('Air Groove')
  })

  it('query basura no sugiere nada', () => {
    expect(rankearSugerencias(personajes, 'xyzwq')).toEqual([])
  })

  it('query corta no sugiere', () => {
    expect(rankearSugerencias(personajes, 's')).toEqual([])
  })

  it('límite de sugerencias', () => {
    const muchas = Array.from({ length: 100 }, (_, i) => char(2000 + i, `Name ${2000 + i}`))
    expect(rankearSugerencias(muchas, 'nam').length).toBe(5)
  })
})
