<script lang="ts">
/* Cabecera uniforme de página. Con `onVolver` agrega el botón atrás. */

import type { Snippet } from 'svelte'
import { store } from '../state/store.svelte'
import { crearI18n } from '../i18n'
import Icono from './Icono.svelte'

interface Props {
  titulo: string
  pillTexto?: string
  chip?: Snippet
  onVolver?: () => void
}

let { titulo, pillTexto, chip, onVolver }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))
</script>

<header class="header-bar">
  {#if onVolver}
    <button type="button" class="icon-button" onclick={onVolver} aria-label={i18n.t('volver')}>
      <Icono nombre="atras" />
    </button>
  {/if}
  <h1>{titulo}</h1>
  {#if chip}{@render chip()}{/if}
  {#if pillTexto}<span class="header-pill">{pillTexto}</span>{/if}
</header>
