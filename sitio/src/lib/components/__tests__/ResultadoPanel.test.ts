import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/svelte'
import axe from 'axe-core'
import ResultadoPanel from '../ResultadoPanel.svelte'
import { calcularResultado } from '../../state/resultado'
import { store } from '../../state/store.svelte'
import { Idioma } from '../../data/prefs'
import { ids, modelo, seleccionesBase } from '../../../test/data'

store.setIdioma(Idioma.ESPANOL)

describe('ResultadoPanel', () => {
  it('muestra el total y las secciones de vínculos', () => {
    const res = calcularResultado(modelo(), seleccionesBase()[2])
    render(ResultadoPanel, { props: { modelo: modelo(), res, japones: false } })
    expect(screen.getByText('Total herencia')).toBeTruthy()
    expect(screen.getByText('Hijo × Padres')).toBeTruthy()
    expect(screen.getByText('Entre padres')).toBeTruthy()
    expect(screen.getByText('Hijo × Padres × Abuelos')).toBeTruthy()
  })

  it('no tiene violaciones de accesibilidad detectables por axe', async () => {
    const res = calcularResultado(modelo(), [ids[0], ids[1], ids[2], null, null, null, null])
    render(ResultadoPanel, { props: { modelo: modelo(), res, japones: false } })
    const resultado = await axe.run(document.body, {
      /* El panel es un fragmento: la regla de landmarks aplica a páginas. */
      rules: { region: { enabled: false } },
    })
    expect(resultado.violations).toEqual([])
  })
})
