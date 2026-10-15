/* Regresión: Mejores linajes salió de la barra inferior y ahora se abre
   desde Más, igual que Grupos y Rankings. */

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/svelte'
import userEvent from '@testing-library/user-event'
import SettingsScreen from '../SettingsScreen.svelte'
import { store } from '../../state/store.svelte'
import { Idioma } from '../../data/prefs'
import { modelo } from '../../../test/data'

const { goto } = vi.hoisted(() => ({ goto: vi.fn() }))
vi.mock('$app/navigation', () => ({ goto }))

store.setIdioma(Idioma.ESPANOL)
store.modelo = modelo()

describe('Ajustes · acceso a Mejores linajes', () => {
  beforeEach(() => {
    goto.mockClear()
  })

  it('la tarjeta de Mejores linajes abre /top', async () => {
    render(SettingsScreen)

    const tarjeta = screen.getByRole('button', { name: /Mejores linajes/ })
    expect(tarjeta).toHaveTextContent('Top 20 de linajes completos por afinidad total')

    await userEvent.click(tarjeta)

    expect(goto).toHaveBeenCalledWith('/top')
  })
})
