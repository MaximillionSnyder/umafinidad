<script lang="ts">
/* Fila del top de linajes, compartida por "Mejores linajes" y "Mis Umas". */

import type { AffinityModel, Linaje } from '../domain/affinity'
import { displayName } from '../domain/models'
import { store } from '../state/store.svelte'
import { crearI18n } from '../i18n'
import { colorDeMedalla } from '../theme/theme'
import RankPill from './RankPill.svelte'
import Medalla from './Medalla.svelte'

interface Props {
  i: number
  combo: Linaje
  modelo: AffinityModel
  onVerHerencia: () => void
}

let { i, combo, modelo, onVerHerencia }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))
const nombres = $derived(
  [combo.hijo, combo.padre, combo.madre].map((c) => displayName(c, i18n.japones)).join(' × '),
)
const color = $derived(colorDeMedalla(i))
const estilo = $derived(
  i < 3
    ? `background:color-mix(in srgb, var(--contenedor-primario) 55%, transparent);border-color:${
        color ? `color-mix(in srgb, ${color} 60%, transparent)` : 'var(--contorno-variante)'
      }`
    : '',
)
</script>

<button
  type="button"
  class="card clickable fila-top"
  onclick={onVerHerencia}
  aria-label={`${nombres}. ${i18n.t('ver_herencia')}`}
  style={estilo}
>
  <Medalla pos={i} />
  <span class="fila-top-texto">
    <span class="fila-top-nombres">{nombres}</span>
    <span class="fila-top-link">{i18n.t('ver_herencia')}</span>
  </span>
  <RankPill rango={modelo.rangoTotal(combo.puntos)} puntos={combo.puntos} />
</button>
