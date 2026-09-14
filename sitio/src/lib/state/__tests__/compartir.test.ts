import { describe, expect, it } from 'vitest'
import { codificarSeleccion, decodificarSeleccion, enlaceSeleccion, PARAM_SELECCION } from '../compartir'
import { SLOTS } from '../../domain/herencia'

const existe = (id: number): boolean => id >= 1000 && id <= 1200

describe('codificarSeleccion', () => {
  it('recorta los slots vacíos del final', () => {
    const sel = [1001, 1002, 1003, null, null, null, null]
    expect(codificarSeleccion(sel)).toBe('1001-1002-1003')
  })

  it('conserva los huecos intermedios', () => {
    const sel = [1001, null, 1002, null, 1003, null, null]
    expect(codificarSeleccion(sel)).toBe('1001--1002--1003')
  })

  it('devuelve cadena vacía para una selección vacía', () => {
    expect(codificarSeleccion(Array(SLOTS).fill(null))).toBe('')
  })
})

describe('decodificarSeleccion', () => {
  it('hace round-trip con la codificación', () => {
    const sel = [1001, 1002, 1003, 1004, 1005, null, null]
    const decodificada = decodificarSeleccion(codificarSeleccion(sel), existe)
    expect(decodificada).toEqual(sel)
  })

  it('rechaza null, vacío y demasiados slots', () => {
    expect(decodificarSeleccion(null, existe)).toBeNull()
    expect(decodificarSeleccion('', existe)).toBeNull()
    expect(decodificarSeleccion('1-2-3-4-5-6-7-8', existe)).toBeNull()
  })

  it('rechaza valores no numéricos o ids inexistentes', () => {
    expect(decodificarSeleccion('1001-abc', existe)).toBeNull()
    expect(decodificarSeleccion('1001-9999', existe)).toBeNull()
    expect(decodificarSeleccion('0', existe)).toBeNull()
  })

  it('acepta una selección corta y rellena con null', () => {
    expect(decodificarSeleccion('1001', existe)).toEqual([1001, null, null, null, null, null, null])
  })
})

describe('enlaceSeleccion', () => {
  it('arma una URL absoluta con el parámetro de selección', () => {
    const enlace = enlaceSeleccion('/compat', [1001, 1002, null, null, null, null, null])
    const url = new URL(enlace)
    expect(url.pathname).toBe('/compat')
    expect(url.searchParams.get(PARAM_SELECCION)).toBe('1001-1002')
  })

  it('omite el parámetro si no hay selección', () => {
    const enlace = enlaceSeleccion('/compat', Array(SLOTS).fill(null))
    expect(new URL(enlace).searchParams.has(PARAM_SELECCION)).toBe(false)
  })
})
