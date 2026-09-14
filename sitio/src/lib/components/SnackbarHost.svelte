<script lang="ts">
/* Proveedor de avisos tipo snackbar con región viva. */

import { setContext, type Snippet } from 'svelte'
import { CLAVE_SNACKBAR, type SnackbarContexto } from './snackbar'

interface Props {
  children: Snippet
}

let { children }: Props = $props()

let mensaje = $state<string | null>(null)
let temporizador: number | null = null

function avisar(texto: string): void {
  mensaje = texto
  if (temporizador !== null) window.clearTimeout(temporizador)
  temporizador = window.setTimeout(() => {
    mensaje = null
    temporizador = null
  }, 4000)
}

setContext<SnackbarContexto>(CLAVE_SNACKBAR, { avisar })

$effect(() => {
  return () => {
    if (temporizador !== null) window.clearTimeout(temporizador)
  }
})
</script>

{@render children()}

<div class="snackbar-host">
  <div role="status" aria-live="polite" class={mensaje ? 'snackbar visible' : 'snackbar'}>
    {mensaje}
  </div>
</div>
