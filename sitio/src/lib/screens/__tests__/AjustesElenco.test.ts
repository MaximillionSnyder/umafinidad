/* Regresión: Mis Umas salió de la barra inferior y ahora se abre a pantalla
   completa desde Más, igual que Grupos, Rankings y Mejores linajes. */

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/svelte'
import userEvent from '@testing-library/user-event'
import SettingsScreen from '../SettingsScreen.svelte'
import ElencoScreen from '../ElencoScreen.svelte'
import { store } from '../../state/store.svelte'
import { Idioma } from '../../data/prefs'
import { modelo } from '../../../test/data'

const { goto } = vi.hoisted(() => ({ goto: vi.fn() }))
vi.mock('$app/navigation', () => ({ goto }))

store.setIdioma(Idioma.ESPANOL)
store.modelo = modelo()

describe('Ajustes · acceso a Mis Umas', () => {
  beforeEach(() => {
    goto.mockClear()
  })

  it('la tarjeta de Mis Umas abre /elenco', async () => {
    render(SettingsScreen)

    const tarjeta = screen.getByRole('button', { name: /Mis Umas/ })
    expect(tarjeta).toHaveTextContent('Tu elenco personal y sus mejores linajes')

    await userEvent.click(tarjeta)

    expect(goto).toHaveBeenCalledWith('/elenco')
  })
})

describe('Mis Umas · volver a Más', () => {
  it('el botón atrás cierra la pantalla', async () => {
    const onVolver = vi.fn()
    render(ElencoScreen, { onVolver })

    await userEvent.click(screen.getByRole('button', { name: 'Volver' }))

    expect(onVolver).toHaveBeenCalledOnce()
  })
})
