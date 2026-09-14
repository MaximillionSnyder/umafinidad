<script lang="ts">
/* Medalla del top 3 (oro/plata/bronce) o número para el resto. */

import { colorDeMedalla } from '../theme/theme'

interface Props {
  pos: number
}

let { pos }: Props = $props()
const color = $derived(colorDeMedalla(pos))
</script>

{#if color === null}
  <span class="medalla numero">{pos + 1}</span>
{:else}
  <span class="medalla" style="background:{color};color:#14161a">{pos + 1}</span>
{/if}
