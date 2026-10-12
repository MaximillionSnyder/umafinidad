/* Regresión: el buscador de Mi corredora solo sugiere personajes del pool
   jugable/activo. El resto (NPC, futuros, sementales) no tiene datos de
   relación y elegirlos mostraba "no tiene linaje calculable". */

import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/svelte'
import userEvent from '@testing-library/user-event'
import CorredoraScreen from '../CorredoraScreen.svelte'
import { store } from '../../state/store.svelte'
import { Idioma } from '../../data/prefs'
import { modelo } from '../../../test/data'

store.setIdioma(Idioma.ESPANOL)
store.modelo = modelo()

const PLACEHOLDER = 'Buscá una corredora…'

describe('CorredoraScreen', () => {
  it('no sugiere personajes sin linaje calculable', async () => {
    render(CorredoraScreen)
    await userEvent.type(screen.getByPlaceholderText(PLACEHOLDER), 'daring')
    expect(screen.queryByText('Daring Tact')).toBeNull()
  })

  it('sigue sugiriendo personajes del pool jugable', async () => {
    render(CorredoraScreen)
    await userEvent.type(screen.getByPlaceholderText(PLACEHOLDER), 'special')
    expect(await screen.findByText('Special Week')).toBeTruthy()
  })
})
