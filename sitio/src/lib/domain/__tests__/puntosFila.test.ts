/* Tests del reparto SOLO visual de los puntos por fila de "Mi corredora":
   las filas deben sumar el total sin tocar el algoritmo. Espejo de
   PuntosFilaTest.kt de la app Android. */

import { describe, expect, it } from 'vitest'
import { modelo } from '../../../test/data'
import { SLOTS, type Seleccion } from '../herencia'
import { formatearPuntos, puntosFilaPresentacion } from '../presentacion'

const m = modelo()

function seleccionDeHijo(hijoId: number): Seleccion {
  const linaje = m.mejorLinajeDe(hijoId)
  if (linaje === null) throw new Error(`sin linaje para ${hijoId}`)
  return [
    linaje.hijo.charId,
    linaje.padre.charId,
    linaje.madre.charId,
    linaje.abuelos[0][0].charId,
    linaje.abuelos[0][1].charId,
    linaje.abuelos[1][0].charId,
    linaje.abuelos[1][1].charId,
  ]
}

function totalDe(seleccion: Seleccion): number {
  let total = 0
  const [hijo, p1, p2] = seleccion
  if (hijo === null) return 0
  if (p1 !== null) total += m.puntajePar(hijo, p1)
  if (p2 !== null) total += m.puntajePar(hijo, p2)
  if (p1 !== null && p2 !== null) total += m.puntajePar(p1, p2)
  for (let slot = 3; slot < SLOTS; slot++) {
    const abuelo = seleccion[slot]
    const padre = seleccion[1 + Math.floor((slot - 3) / 2)]
    if (abuelo === undefined || abuelo === null) continue
    if (padre === undefined || padre === null) continue
    total += abuelo === hijo ? 0 : m.puntajeTrio(hijo, padre, abuelo)
  }
  return total
}

describe('puntosFilaPresentacion', () => {
  const seleccion = seleccionDeHijo(1006)

  it('las filas suman el total del árbol', () => {
    const filas = puntosFilaPresentacion(m, seleccion)
    const suma = filas.reduce((acc: number, valor) => acc + (valor ?? 0), 0)
    expect(suma).toBe(totalDe(seleccion))
  })

  it('el hijo no lleva puntaje propio', () => {
    expect(puntosFilaPresentacion(m, seleccion)[0]).toBeNull()
  })

  it('el padre lleva hijo-padre + la mitad de padre-padre', () => {
    const filas = puntosFilaPresentacion(m, seleccion)
    const [hijo, padre1, padre2] = seleccion
    const mitad = m.puntajePar(padre1!, padre2!) / 2
    expect(filas[1]).toBe(m.puntajePar(hijo!, padre1!) + mitad)
    expect(filas[2]).toBe(m.puntajePar(hijo!, padre2!) + mitad)
  })

  it('el abuelo lleva su trío y la corredora vale cero', () => {
    const filas = puntosFilaPresentacion(m, seleccion)
    const [hijo, padre1] = seleccion
    expect(filas[3]).toBe(m.puntajeTrio(hijo!, padre1!, seleccion[3]!))

    const conCorredora = [...seleccion]
    conCorredora[3] = conCorredora[0]
    expect(puntosFilaPresentacion(m, conCorredora)[3]).toBe(0)
  })

  it('muestra medio punto cuando el par entre padres es impar', () => {
    const jugables = m.personajes
      .filter((c) => c.playable === true && c.active === true)
      .map((c) => c.charId)
    let padre1 = -1
    let padre2 = -1
    for (const a of jugables) {
      for (const b of jugables) {
        if (a !== b && m.puntajePar(a, b) % 2 === 1) {
          padre1 = a
          padre2 = b
          break
        }
      }
      if (padre1 !== -1) break
    }
    const hijo = jugables.find((id) => id !== padre1 && id !== padre2)!
    const abuelos = jugables.filter((id) => id !== hijo && id !== padre1 && id !== padre2).slice(0, 4)
    const conParImpar: Seleccion = [hijo, padre1, padre2, ...abuelos]

    const filas = puntosFilaPresentacion(m, conParImpar)
    expect(m.puntajePar(padre1, padre2) % 2).toBe(1)
    expect(filas[1]! % 1).not.toBe(0)
    expect(formatearPuntos(filas[1]!, 'es')).toContain(',5')
    const suma = filas.reduce((acc: number, valor) => acc + (valor ?? 0), 0)
    expect(suma).toBe(totalDe(conParImpar))
  })

  it('sin hijo no hay puntajes de fila', () => {
    const vacia: Seleccion = Array(SLOTS).fill(null)
    expect(puntosFilaPresentacion(m, vacia).every((valor) => valor === null)).toBe(true)
  })

  it('formatea el medio punto según el idioma', () => {
    expect(formatearPuntos(51, 'es')).toBe('51')
    expect(formatearPuntos(51.5, 'es')).toBe('51,5')
    expect(formatearPuntos(51.5, 'en')).toBe('51.5')
  })
})
