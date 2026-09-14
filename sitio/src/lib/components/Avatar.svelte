<script lang="ts">
/* Avatar pixel-art con fallback de iniciales + gradiente HSL. */

import { asset } from '$app/paths'
import { store } from '../state/store.svelte'
import { EstiloAvatar } from '../data/prefs'
import { gradienteDeAvatar, inicialesDe } from '../theme/theme'

const CARPETA: Record<EstiloAvatar, string> = {
  [EstiloAvatar.COLOR]: 'color',
  [EstiloAvatar.GRISES]: 'bw5',
  [EstiloAvatar.MONOCROMO]: 'bw1',
}

interface Props {
  id: number
  nombre: string
  tamano?: number
  class?: string
}

let { id, nombre, tamano = 40, class: clase = '' }: Props = $props()

const url = $derived(asset(`/avatars/${CARPETA[store.estiloAvatar]}/${id}.png`))
let urlConError = $state<string | null>(null)
const fallo = $derived(urlConError === url)
</script>

{#if fallo}
  <span
    aria-hidden="true"
    class={clase}
    style="width:{tamano}px;height:{tamano}px;border-radius:50%;flex-shrink:0;background:{gradienteDeAvatar(
      id,
    )};display:inline-flex;align-items:center;justify-content:center;color:#fff;font-weight:700;font-size:{Math.max(
      10,
      Math.round(tamano * 0.3),
    )}px"
  >
    {inicialesDe(nombre)}
  </span>
{:else}
  <img
    src={url}
    alt=""
    aria-hidden="true"
    class={clase}
    style="width:{tamano}px;height:{tamano}px;border-radius:50%;flex-shrink:0;object-fit:cover"
    onerror={() => (urlConError = url)}
    loading="lazy"
  />
{/if}
