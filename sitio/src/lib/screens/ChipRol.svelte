<script lang="ts">
/* Fila de rol del árbol (hijo fijo o editable con alternativas). */

import { displayName } from '$lib/domain/models'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import { colorDeGenealogia, colorDeRango } from '$lib/theme/theme'
import Avatar from '$lib/components/Avatar.svelte'

interface Props {
  slot: number
  etiqueta: string
  id: number | null
  puntos?: number | null
  onAbrir?: () => void
}

let { slot, etiqueta, id, puntos = null, onAbrir }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))
const personaje = $derived(id !== null && store.modelo !== null ? store.modelo.porId(id) : undefined)
const colorRol = $derived(colorDeGenealogia(slot))
const nombre = $derived(personaje ? displayName(personaje, i18n.japones) : '—')

/* Mismos umbrales que GruposScreen (par): ◎ ≥20, ○ ≥10, △ ≥4. */
function claseDePuntos(valor: number): string | null {
  if (valor >= 20) return 'rank-great'
  if (valor >= 10) return 'rank-good'
  if (valor >= 4) return 'rank-fair'
  return null
}
</script>

{#if !onAbrir}
  <div class="chip-rol fijo" style="border-color:transparent">
    {#if personaje}
      <Avatar id={personaje.charId} nombre={nombre} tamano={28} />
    {/if}
    <span class="chip-rol-texto">
      <span class="chip-rol-etiqueta" style="color:{colorRol}">{etiqueta}</span>
      <span class="chip-rol-nombre">{nombre}</span>
    </span>
    {#if personaje && puntos !== null}
      <span
        class="chip-rol-puntos"
        style="color:{colorDeRango(claseDePuntos(puntos)) ?? 'var(--texto)'}"
      >
        {puntos}pt
      </span>
    {/if}
  </div>
{:else}
  <button
    type="button"
    class="chip-rol"
    style={personaje
      ? `border-color:color-mix(in srgb, ${colorRol} 45%, transparent)`
      : undefined}
    onclick={onAbrir}
    aria-label={`${etiqueta}: ${nombre}${puntos !== null ? `, ${puntos} pt` : ''}`}
  >
    {#if personaje}
      <Avatar id={personaje.charId} nombre={nombre} tamano={28} />
    {/if}
    <span class="chip-rol-texto">
      <span class="chip-rol-etiqueta" style="color:{colorRol}">{etiqueta}</span>
      <span class="chip-rol-nombre">{nombre}</span>
    </span>
    {#if personaje && puntos !== null}
      <span
        class="chip-rol-puntos"
        style="color:{colorDeRango(claseDePuntos(puntos)) ?? 'var(--texto)'}"
      >
        {puntos}pt
      </span>
    {/if}
    <span class="chip-rol-flecha" aria-hidden="true">⌄</span>
  </button>
{/if}
