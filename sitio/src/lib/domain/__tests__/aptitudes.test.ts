/* Aptitudes (pista / distancia / estilo de la carta base).
   Espejo de AptitudesTest.kt. */

import { describe, expect, it } from 'vitest'
import { aptitudValida, aptitudesDestacadas } from '../aptitudes'
import { AffinityModel } from '../affinity'
import { aptitudes, characters, ids, modelo, relations, members } from '../../../test/data'

describe('deserialización de aptitudes', () => {
  it('las claves se convierten a números', () => {
    expect(aptitudes.size).toBeGreaterThan(0)
    expect(aptitudes.has(1001)).toBe(true)
    expect([...aptitudes.keys()].every((k) => k > 0)).toBe(true)
  })

  it('todas las entradas son válidas', () => {
    expect([...aptitudes.values()].every((apt) => aptitudValida(apt))).toBe(true)
  })

  it('la tabla cubre a los jugables', () => {
    const jugables = new Set(ids)
    const extras = [...aptitudes.keys()].filter((id) => !jugables.has(id))
    expect(extras.length, `entradas de no jugables: ${extras}`).toBeLessThanOrEqual(5)
    expect(jugables.size - aptitudes.size).toBeLessThanOrEqual(3)
  })
})

describe('accessor del modelo', () => {
  it('Special Week verificado contra GameTora', () => {
    const m = new AffinityModel(characters, [], [], aptitudes)
    expect(m.aptitudesDe(1001)).toEqual(['A', 'G', 'F', 'C', 'A', 'A', 'G', 'A', 'A', 'C'])
  })

  it('id desconocido o sin tabla devuelve null', () => {
    const m = new AffinityModel(characters, [], [], aptitudes)
    expect(m.aptitudesDe(999999999)).toBeNull()
    /* Sin la tabla (constructor por defecto) nunca crashea. */
    expect(modelo().aptitudesDe(1001)).toBeNull()
  })

  it('el modelo real (con relaciones) también expone aptitudes', () => {
    const m = new AffinityModel(characters, relations, members, aptitudes)
    expect(m.aptitudesDe(1001)).not.toBeNull()
  })
})

describe('aptitudes destacadas (A o B)', () => {
  it('destacadas de Special Week', () => {
    expect(aptitudesDestacadas(['A', 'G', 'F', 'C', 'A', 'A', 'G', 'A', 'A', 'C'])).toEqual([
      0, 4, 5, 7, 8, /* turf, media, larga, vanguardia, remate */
    ])
  })

  it('incluyen la letra B', () => {
    expect(aptitudesDestacadas(['C', 'B', 'A', 'C', 'C', 'C', 'C', 'C', 'C', 'C'])).toEqual([1, 2])
  })

  it('vacías cuando no destaca en nada', () => {
    expect(aptitudesDestacadas(Array(10).fill('C'))).toEqual([])
  })
})

describe('validación', () => {
  it('rechaza aptitudes inválidas', () => {
    expect(aptitudValida(Array(10).fill('A'))).toBe(true)
    expect(aptitudValida(Array(9).fill('A'))).toBe(false)
    expect(aptitudValida(Array(10).fill('S'))).toBe(false)
    expect(aptitudValida(['A', 'G', 'F', 'C', 'A', 'A', 'G', 'A', 'A', 'AA'])).toBe(false)
  })

  it('especialistas conocidos', () => {
    const smartFalcon = aptitudes.get(1046)
    expect(smartFalcon).toBeDefined()
    expect(smartFalcon![1]).toBe('A')

    const twinTurbo = aptitudes.get(1066)
    expect(twinTurbo).toBeDefined()
    expect(twinTurbo![6]).toBe('A')
    expect(twinTurbo![7]).toBe('G')
    expect(twinTurbo![8]).toBe('G')
    expect(twinTurbo![9]).toBe('G')
  })
})
