<script lang="ts">
/* Ranking con dos modos: "Umas más versátiles" (total afinidad) y
   "Mejores padres" (veces como padre óptimo + % + media). */

import type { RankingAfinidad, RankingPadre } from '$lib/domain/affinity'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import { displayName } from '$lib/domain/models'
import { pedirRankingAfinidad, pedirRankingPadres } from '$lib/worker/cliente'
import { Async } from '$lib/state/async.svelte'
import HeaderBar from '$lib/components/HeaderBar.svelte'
import RankPill from '$lib/components/RankPill.svelte'
import Medalla from '$lib/components/Medalla.svelte'
import Avatar from '$lib/components/Avatar.svelte'
import Dialogo from '$lib/components/Dialogo.svelte'
import Icono from '$lib/components/Icono.svelte'
import { colorDeMedalla } from '$lib/theme/theme'

export type ModoRanking = 'VERSATIL' | 'PADRES'

interface Props {
  onVolver: () => void
  modoInicial?: ModoRanking
}

let { onVolver, modoInicial = 'VERSATIL' }: Props = $props()
const i18n = $derived(crearI18n(store.idioma))

// svelte-ignore state_referenced_locally
let modo = $state<ModoRanking>(modoInicial)
let mostrarAyuda = $state(false)
const versatil = new Async(() => pedirRankingAfinidad())
const padres = new Async(() => pedirRankingPadres())

$effect(() => {
  if (store.modelo !== null) {
    void versatil.ejecutar()
    void padres.ejecutar()
  }
})

function estiloTop(pos: number): string {
  if (pos >= 3) return ''
  const color = colorDeMedalla(pos)
  return `background:color-mix(in srgb, var(--contenedor-primario) 55%, transparent);border-color:${
    color ? `color-mix(in srgb, ${color} 60%, transparent)` : 'var(--contorno-variante)'
  }`
}
</script>

<svelte:head>
  <title>{i18n.t('tab_ranking')} · Uma Afinidad</title>
</svelte:head>

{#if store.modelo === null}
  <div class="centrado">{i18n.t('calculando')}</div>
{:else}
  <div class="pantalla">
    <HeaderBar titulo={i18n.t('tab_ranking')} {onVolver} />

    <div class="segmentado" role="group" aria-label={i18n.t('tab_ranking')}>
      <button
        type="button"
        class={modo === 'VERSATIL' ? 'segmento activo' : 'segmento'}
        aria-pressed={modo === 'VERSATIL'}
        onclick={() => (modo = 'VERSATIL')}
      >
        {i18n.t('ranking_modo_versatil')}
      </button>
      <button
        type="button"
        class={modo === 'PADRES' ? 'segmento activo' : 'segmento'}
        aria-pressed={modo === 'PADRES'}
        onclick={() => (modo = 'PADRES')}
      >
        {i18n.t('ranking_modo_padres')}
      </button>
    </div>

    {#if modo === 'VERSATIL'}
      {#if versatil.datos === null}
        <div class="centrado">{i18n.t('calculando')}</div>
      {:else if versatil.datos.length === 0}
        <div class="centrado">{i18n.t('sin_datos')}</div>
      {:else}
        <ul class="lista-ranking">
          {#each versatil.datos as entry, i (entry.personaje.charId)}
            <li>
              {@render filaVersatil(entry, i, store.modelo)}
            </li>
          {/each}
        </ul>
      {/if}
    {:else}
      <button
        type="button"
        class="ayuda-ranking"
        onclick={() => (mostrarAyuda = true)}
        aria-label={i18n.t('ranking_padres_ayuda_titulo')}
      >
        <span class="secundario">{i18n.t('ranking_padres_ayuda_corta')}</span>
        <Icono nombre="info" class="icono-info" />
      </button>
      {#if padres.datos === null}
        <div class="centrado">{i18n.t('calculando')}</div>
      {:else if padres.datos.length === 0}
        <div class="centrado">{i18n.t('sin_datos')}</div>
      {:else}
        <ul class="lista-ranking">
          {#each padres.datos as entry, i (entry.personaje.charId)}
            <li>
              {@render filaPadre(entry, i)}
            </li>
          {/each}
        </ul>
      {/if}
    {/if}

    <Dialogo
      open={mostrarAyuda}
      titulo={i18n.t('ranking_padres_ayuda_titulo')}
      descripcion={i18n.t('ranking_padres_ayuda_larga')}
      onClose={() => (mostrarAyuda = false)}
    >
      <button type="button" class="boton-texto" onclick={() => (mostrarAyuda = false)}>
        {i18n.t('entendido')}
      </button>
    </Dialogo>
  </div>
{/if}

{#snippet filaVersatil(entry: RankingAfinidad, pos: number, modelo: import('$lib/domain/affinity').AffinityModel)}
  {@const nombre = displayName(entry.personaje, i18n.japones)}
  <article class="card fila-ranking" style={estiloTop(pos)}>
    <Medalla {pos} />
    <Avatar id={entry.personaje.charId} nombre={nombre} tamano={36} />
    <span class="fila-ranking-nombre">{nombre}</span>
    <RankPill rango={modelo.rangoRanking(entry.total)} puntos={entry.total} />
  </article>
{/snippet}

{#snippet filaPadre(entry: RankingPadre, pos: number)}
  {@const nombre = displayName(entry.personaje, i18n.japones)}
  <article class="card fila-ranking" style={estiloTop(pos)}>
    <Medalla {pos} />
    <Avatar id={entry.personaje.charId} nombre={nombre} tamano={36} />
    <span class="fila-ranking-texto">
      <span class="fila-ranking-nombre">{nombre}</span>
      <span class="secundario fila-ranking-media">
        {i18n.t('ranking_padres_media', entry.puntosMedios)}
      </span>
    </span>
    <span class="fila-ranking-veces">
      {i18n.t('ranking_padres_veces', entry.veces, Math.trunc(entry.porcentaje))}
    </span>
  </article>
{/snippet}
