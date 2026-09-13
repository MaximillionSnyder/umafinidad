/* Tests de paridad contra la lógica JS original de la PWA.
   Mismos fixtures y mismas verificaciones que ParidadTest.kt: los
   fixtures se generaron ejecutando affinity.js/herencia.js reales. */

import { describe, expect, it } from 'vitest'
import {
  armarArbol,
  puedeIrEn,
  slotPara,
  TipoVinculo,
  vinculos,
  type Seleccion,
} from '../herencia'
import { fixture, ids, modelo, seleccionesBase } from '../../test/data'

const m = modelo()

interface GrupoCompartidoDto {
  tipo: number
  puntos: number
}

interface GrupoInfoDto extends GrupoCompartidoDto {
  miembros: string[]
}

interface GrupoResumenDto extends GrupoCompartidoDto {
  cantidad: number
}

interface RangoDto {
  simbolo: string
  clase: string
}

interface RangosDto {
  par: (RangoDto & { min?: number }) | null
  total: RangoDto
}

interface CharResumenDto {
  char_id: number
  en_name: string | null
}

interface LinajeDto {
  hijo: CharResumenDto
  padre: CharResumenDto
  madre: CharResumenDto
  abuelos: CharResumenDto[][]
  puntos: number
}

interface VinculoDto {
  tipo: string
  ids: number[]
  esCorredora: boolean
}

describe('paridad con la PWA', () => {
  it('puntajePar coincide con la web', () => {
    const pares = fixture<number[][]>('pares.json')
    for (const [a, b, esperado] of pares) {
      expect(m.puntajePar(a, b), `puntajePar(${a},${b})`).toBe(esperado)
    }
  })

  it('puntajeTrio coincide con la web', () => {
    const trios = fixture<number[][]>('trios.json')
    for (const [a, b, c, esperado] of trios) {
      expect(m.puntajeTrio(a, b, c), `puntajeTrio(${a},${b},${c})`).toBe(esperado)
    }
  })

  it('gruposCompartidos coinciden con la web', () => {
    const casos = fixture<Record<string, GrupoCompartidoDto[]>>('grupos_compartidos.json')
    for (const [clave, esperados] of Object.entries(casos)) {
      const [a, b] = clave.split('-').map(Number)
      expect(m.gruposCompartidos([a, b]), clave).toEqual(esperados)
    }
  })

  it('rangos coinciden con la web', () => {
    const rangos = fixture<Record<string, RangosDto>>('rangos.json')
    for (const [valor, esperado] of Object.entries(rangos)) {
      const v = Number(valor)
      const parEsperado = esperado.par
        ? { simbolo: esperado.par.simbolo, clase: esperado.par.clase }
        : null
      expect(m.rango(v), `rango(${v})`).toEqual(parEsperado)
      expect(m.rangoTotal(v), `rangoTotal(${v})`).toEqual(esperado.total)
    }
  })

  it('gruposDeChar coinciden con la web', () => {
    const casos = fixture<Record<string, GrupoInfoDto[]>>('grupos_char.json')
    for (const [id, esperados] of Object.entries(casos)) {
      expect(m.gruposDeChar(Number(id)), id).toEqual(esperados)
    }
  })

  it('todosLosGrupos coinciden con la web', () => {
    const esperados = fixture<GrupoResumenDto[]>('todos_grupos.json')
    const actual = m.todosLosGrupos()
    expect(actual.length).toBe(esperados.length)
    for (let i = 0; i < esperados.length; i++) {
      expect(actual[i], `todosLosGrupos[${i}]`).toEqual(esperados[i])
    }
  })

  it('topLinajes coinciden con la web', () => {
    const esperados = fixture<LinajeDto[]>('top_linajes.json')
    const actual = m.topLinajes(10)
    expect(actual.length).toBe(esperados.length)
    for (let i = 0; i < esperados.length; i++) {
      const e = esperados[i]
      const a = actual[i]
      expect(a.hijo.charId, `top[${i}] hijo`).toBe(e.hijo.char_id)
      expect(a.padre.charId, `top[${i}] padre`).toBe(e.padre.char_id)
      expect(a.madre.charId, `top[${i}] madre`).toBe(e.madre.char_id)
      expect(
        a.abuelos.map((rama) => rama.map((c) => c.charId)),
        `top[${i}] abuelos`,
      ).toEqual(e.abuelos.map((rama) => rama.map((c) => c.char_id)))
      expect(a.puntos, `top[${i}] puntos`).toBe(e.puntos)
      expect(a.hijo.enName).toBe(e.hijo.en_name)
    }
  })

  it('ranking de afinidad es consistente', () => {
    const ranking = m.rankingAfinidad()

    expect(ranking.length).toBe(ids.length)
    expect(ranking.every((entry, i) => i === 0 || ranking[i - 1].total >= entry.total)).toBe(true)
    expect(
      ranking.every(
        (entry, i) => i === 0 || ranking[i - 1].total !== entry.total || ranking[i - 1].personaje.charId <= entry.personaje.charId,
      ),
    ).toBe(true)

    const muestra = [...ranking.slice(0, 3), ...ranking.slice(-3)]
    for (const entry of muestra) {
      const id = entry.personaje.charId
      const esperado = ids.filter((otro) => otro !== id).reduce((suma, otro) => suma + m.puntajePar(id, otro), 0)
      expect(esperado, `total de ${entry.personaje.enName}`).toBe(entry.total)
    }

    expect(m.rangoRanking(ranking[0].total).clase).toBe('rank-great')
    expect(ranking.every((entry) => entry.total >= 0)).toBe(true)
  })

  it('puedeIrEn coincide con la web', () => {
    const filas = fixture<[number, number, number, boolean][]>('puede_ir_en.json')
    const selecciones = seleccionesBase()
    expect(filas.length).toBe(231) /* 3 selecciones × 7 slots × 11 candidatos */
    for (const [selIdx, slot, idReal, esperado] of filas) {
      expect(
        puedeIrEn(selecciones[selIdx] as Seleccion, slot, idReal),
        `puedeIrEn(sel${selIdx}, slot${slot}, ${idReal})`,
      ).toBe(esperado)
    }
  })

  it('slotPara coincide con la web', () => {
    const filas = fixture<[number, number, number][]>('slot_para.json')
    const selecciones = seleccionesBase()
    for (const [selIdx, idReal, esperado] of filas) {
      expect(slotPara(selecciones[selIdx] as Seleccion, idReal), `slotPara(sel${selIdx}, ${idReal})`).toBe(esperado)
    }
  })

  it('vinculos coinciden con la web', () => {
    const esperadosPorSeleccion = fixture<VinculoDto[][]>('vinculos.json')
    const selecciones = seleccionesBase()
    esperadosPorSeleccion.forEach((esperados, i) => {
      const actual = vinculos(armarArbol(selecciones[i] as Seleccion)).map((v) => ({
        tipo: v.tipo as string,
        ids: v.ids,
        esCorredora: v.esCorredora,
      }))
      expect(actual, `vinculos(sel${i})`).toEqual(esperados)
    })
  })
})

describe('tipos de vínculo', () => {
  it('usa los identificadores de la PWA', () => {
    expect(TipoVinculo.HIJO_PADRE).toBe('hijo-padre')
    expect(TipoVinculo.ENTRE_PADRES).toBe('entre-padres')
    expect(TipoVinculo.HIJO_PADRE_ABUELO).toBe('hijo-padre-abuelo')
  })
})
