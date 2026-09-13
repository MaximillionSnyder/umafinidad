/* Test de integración: la app arranca con datos mockeados, se puede
   seleccionar una corredora y ver el resultado; además corre axe (WCAG A/AA)
   sobre la pantalla inicial. */

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import * as axe from 'axe-core'
import { readFileSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import App from './App'

const raiz = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')

function stubFetch(): void {
  vi.stubGlobal('fetch', async (url: string | URL) => {
    const rel = String(url).replace(/^\/+/, '')
    const datos = JSON.parse(readFileSync(path.join(raiz, 'public', rel), 'utf8'))
    return { ok: true, status: 200, json: async () => datos } as Response
  })
}

beforeEach(() => {
  cleanup()
  localStorage.clear()
  window.location.hash = ''
  stubFetch()
})

describe('App', () => {
  it('selecciona una corredora y muestra el total de herencia', async () => {
    const usuario = userEvent.setup()
    render(<App />)

    const buscador = await screen.findByPlaceholderText('Search character…')
    await usuario.type(buscador, 'special week')

    const opcion = await screen.findByRole('option', { name: /special week/i })
    await usuario.click(within(opcion).getByRole('button'))

    const tarjeta = await screen.findByRole('checkbox', { name: /special week/i })
    expect(tarjeta).toHaveAttribute('aria-checked', 'true')

    const fab = screen.getByRole('button', { name: /view affinity/i })
    await usuario.click(fab)

    const dialogo = await screen.findByRole('dialog')
    expect(dialogo).toHaveTextContent('Total inheritance')
  })

  it('navega a Ajustes y muestra las secciones', async () => {
    const usuario = userEvent.setup()
    render(<App />)

    await screen.findByPlaceholderText('Search character…')
    await usuario.click(screen.getByRole('button', { name: 'More' }))

    expect(await screen.findByRole('heading', { name: 'Settings' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /appearance/i })).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: /accessibility/i }).length).toBeGreaterThan(0)
  })

  it('no tiene violaciones de accesibilidad en la pantalla inicial', async () => {
    const { container } = render(<App />)
    await screen.findByPlaceholderText('Search character…')

    const resultados = await axe.run(container, {
      rules: {
        /* jsdom no calcula estilos/layout: el contraste se valida a mano. */
        'color-contrast': { enabled: false },
      },
    })
    expect(resultados.violations).toEqual([])
  })
})
