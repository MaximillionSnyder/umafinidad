/* Regresión del panel "Más info" de Mi corredora: el botón debe abrir el
   desglose del total y los puntos de las filas deben sumar el total. */

import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/svelte'
import userEvent from '@testing-library/user-event'
import DetalleCorredora from '../DetalleCorredora.svelte'
import { modelo } from '../../../test/data'
import { Idioma } from '../../data/prefs'
import { store } from '../../state/store.svelte'

const linaje = (() => {
  const m = modelo()
  const l = m.mejorLinajeDe(1006)!
  return l
})()

vi.mock('$lib/worker/cliente', () => ({
  pedirMejorLinaje: vi.fn(async () => linaje),
  pedirAlternativas: vi.fn(async () => []),
}))

store.setIdioma(Idioma.ESPANOL)
store.modelo = modelo()

describe('DetalleCorredora · Más info', () => {
  it('muestra las filas que suman el total y abre el desglose', async () => {
    const { container } = render(DetalleCorredora, {
      modelo: modelo(),
      hijoId: 1006,
      override: null,
      onRecargarCon: () => {},
      onConteo: () => {},
    })

    /* Las filas usan el reparto de presentación: el padre lleva medio punto
       cuando el par padre-padre es impar (aquí 36 → 18 + 18, entero). */
    expect(await screen.findByText(/Mejor linaje de Oguri Cap/)).toBeTruthy()
    const puntos = [...container.querySelectorAll('.chip-rol-puntos')].map((n) => n.textContent)
    expect(puntos.length).toBe(6) // el hijo no lleva puntaje
    const suma = puntos.reduce((total, texto) => total + Number.parseFloat(texto!.replace('pt', '').replace(',', '.')), 0)
    expect(suma).toBe(222)

    await userEvent.click(screen.getByRole('button', { name: 'Más info' }))

    expect(await screen.findByText('Cómo se calcula la afinidad')).toBeTruthy()
    expect(screen.getByText('64 + 36 + 122 = 222')).toBeTruthy()
    expect(screen.getByText('Si cambiás un personaje')).toBeTruthy()
  })
})
