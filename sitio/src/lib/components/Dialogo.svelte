<script lang="ts">
/* Diálogo de confirmación accesible (role alertdialog). */

import type { Snippet } from 'svelte'
import { store } from '../state/store.svelte'
import { crearI18n } from '../i18n'

interface Props {
  open: boolean
  titulo: string
  descripcion?: string
  onClose: () => void
  /* Bloqueante: sin cierre por fondo ni Escape (bienvenida de accesibilidad). */
  bloqueante?: boolean
  children: Snippet
}

let { open, titulo, descripcion, onClose, bloqueante = false, children }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))
let contenedor: HTMLDivElement | null = $state(null)

$effect(() => {
  if (!open) return
  const el = contenedor
  const anterior = document.activeElement as HTMLElement | null
  el?.focus()
  const onKeyDown = (evento: KeyboardEvent) => {
    if (evento.key === 'Escape') {
      if (bloqueante) {
        evento.preventDefault()
        evento.stopPropagation()
        return
      }
      evento.stopPropagation()
      onClose()
    }
  }
  document.addEventListener('keydown', onKeyDown, true)
  const overflowPrevio = document.body.style.overflow
  document.body.style.overflow = 'hidden'
  return () => {
    document.removeEventListener('keydown', onKeyDown, true)
    document.body.style.overflow = overflowPrevio
    anterior?.focus()
  }
})
</script>

{#if open}
  <div class="sheet-backdrop centrado-arriba">
    {#if !bloqueante}
      <button
        type="button"
        class="sheet-backdrop-boton"
        aria-label={i18n.t('volver')}
        tabindex={-1}
        onclick={onClose}
      ></button>
    {/if}
    <div
      bind:this={contenedor}
      tabindex={-1}
      role="alertdialog"
      aria-modal="true"
      aria-labelledby="dialogo-titulo"
      class="dialogo"
    >
      <h2 id="dialogo-titulo">{titulo}</h2>
      {#if descripcion}<p class="secundario">{descripcion}</p>{/if}
      <div class="dialogo-acciones">
        {@render children()}
      </div>
    </div>
  </div>
{/if}
