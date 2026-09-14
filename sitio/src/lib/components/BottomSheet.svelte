<script lang="ts">
/* Modal inferior y diálogo de confirmación con semántica accesible:
   role dialog/alertdialog, foco inicial, Escape, trampa de foco y
   devolución del foco al cerrar. */

import type { Snippet } from 'svelte'
import { store } from '../state/store.svelte'
import { crearI18n } from '../i18n'

function usarFocoModal(
  getOpen: () => boolean,
  getContenedor: () => HTMLElement | null,
  onClose: () => void,
  bloqueante: () => boolean,
) {
  $effect(() => {
    if (!getOpen()) return
    const contenedor = getContenedor()
    const anterior = document.activeElement as HTMLElement | null
    contenedor?.focus()

    const onKeyDown = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') {
        if (bloqueante()) {
          evento.preventDefault()
          evento.stopPropagation()
          return
        }
        evento.stopPropagation()
        onClose()
        return
      }
      if (evento.key !== 'Tab' || !contenedor) return
      const focusables = contenedor.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      )
      if (focusables.length === 0) {
        evento.preventDefault()
        return
      }
      const primero = focusables[0]
      const ultimo = focusables[focusables.length - 1]
      if (evento.shiftKey && document.activeElement === primero) {
        evento.preventDefault()
        ultimo.focus()
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault()
        primero.focus()
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
}

interface BottomSheetProps {
  open: boolean
  onClose: () => void
  labelledBy: string
  children: Snippet
}

let { open, onClose, labelledBy, children }: BottomSheetProps = $props()
const i18n = $derived(crearI18n(store.idioma))
let contenedor: HTMLDivElement | null = $state(null)
usarFocoModal(
  () => open,
  () => contenedor,
  () => onClose(),
  () => false,
)
</script>

{#if open}
  <div class="sheet-backdrop">
    <button
      type="button"
      class="sheet-backdrop-boton"
      aria-label={i18n.t('volver')}
      tabindex={-1}
      onclick={onClose}
    ></button>
    <div
      bind:this={contenedor}
      tabindex={-1}
      role="dialog"
      aria-modal="true"
      aria-labelledby={labelledBy}
      class="sheet"
    >
      <div class="sheet-handle" aria-hidden="true"></div>
      {@render children()}
    </div>
  </div>
{/if}
