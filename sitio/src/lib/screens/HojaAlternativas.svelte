<script lang="ts">
/* Hoja de alternativas de un slot: lista de candidatos rankeados con su
   total resultante y sus puntos directos. */

import type { AlternativaSlot } from '$lib/domain/affinity'
import type { Seleccion } from '$lib/domain/herencia'
import { displayName } from '$lib/domain/models'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import { Async } from '$lib/state/async.svelte'
import { pedirAlternativas } from '$lib/worker/cliente'
import { colorDeRango } from '$lib/theme/theme'
import Avatar from '$lib/components/Avatar.svelte'

interface Props {
  seleccion: Seleccion
  slot: number
  ocupanteEtiqueta: string
  onElegir: (id: number) => void
}

let { seleccion, slot, ocupanteEtiqueta, onElegir }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))
const alternativas = new Async(() => pedirAlternativas(seleccion, slot, 20))

$effect(() => {
  void slot
  void seleccion
  void alternativas.ejecutar()
})

/* Mismos umbrales que GruposScreen (par): ◎ ≥20, ○ ≥10, △ ≥4. */
function claseDePuntos(puntos: number): string | null {
  if (puntos >= 20) return 'rank-great'
  if (puntos >= 10) return 'rank-good'
  if (puntos >= 4) return 'rank-fair'
  return null
}
</script>

<div class="hoja-alternativas">
  <h2 id="alternativas-titulo">{i18n.t('alternativas_titulo', ocupanteEtiqueta)}</h2>
  {#if alternativas.datos === null}
    <p class="nota">{i18n.t('calculando')}</p>
  {:else if alternativas.datos.length === 0}
    <p class="nota">{i18n.t('sin_alternativas')}</p>
  {:else}
    <ul>
      {#each alternativas.datos as alt (alt.personaje.charId)}
        {@render fila(alt)}
      {/each}
    </ul>
  {/if}
</div>

{#snippet fila(alt: AlternativaSlot)}
  {@const nombre = displayName(alt.personaje, i18n.japones)}
  <li>
    <button type="button" class="alternativa" onclick={() => onElegir(alt.personaje.charId)}>
      <Avatar id={alt.personaje.charId} nombre={nombre} tamano={36} />
      <span class="alternativa-texto">
        <span class="alternativa-nombre">{nombre}</span>
        <span class="secundario alternativa-total">Total {alt.total}</span>
      </span>
      <span
        class="alternativa-puntos"
        style="color:{colorDeRango(claseDePuntos(alt.puntosDirectos)) ?? 'var(--texto)'}"
      >
        {alt.puntosDirectos}pt
      </span>
    </button>
  </li>
{/snippet}
