<script lang="ts">
/* Pastilla "◎ 257" con fondo tintado al 12% del color del rango. */

import type { Rango } from '../domain/affinity'
import { colorDeRango, fondoDeRango } from '../theme/theme'

interface Props {
  rango: Rango | null
  puntos: number
  grande?: boolean
}

let { rango, puntos, grande = false }: Props = $props()
const frente = $derived(colorDeRango(rango?.clase) ?? 'var(--texto)')
const fondo = $derived(fondoDeRango(rango?.clase) ?? 'var(--superficie-alta)')
</script>

<span class={grande ? 'rank-pill grande' : 'rank-pill'} style="color:{frente};background:{fondo}">
  {rango ? `${rango.simbolo} ` : ''}{puntos}
</span>
