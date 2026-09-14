<script lang="ts">
/* Mi corredora: buscador de la hija + detalle con mejor linaje y
   configuraciones guardadas. */

import type { ArbolGuardado } from '$lib/data/arboles'
import type { Seleccion } from '$lib/domain/herencia'
import { displayName } from '$lib/domain/models'
import { rankearSugerencias } from '$lib/domain/busqueda'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import Avatar from '$lib/components/Avatar.svelte'
import HeaderBar from '$lib/components/HeaderBar.svelte'
import Icono from '$lib/components/Icono.svelte'
import DetalleCorredora from './DetalleCorredora.svelte'

const i18n = $derived(crearI18n(store.idioma))

let filtro = $state('')
let elegidaId = $state(-1)
let recarga = $state(0)
let conteoSeleccion = $state(0)
let override = $state.raw<Seleccion | null>(null)
let pendienteProcesado = $state.raw<ArbolGuardado | null>(null)

const sugerencias = $derived(
  store.modelo && filtro.trim().length >= 2 ? rankearSugerencias(store.modelo.personajes, filtro) : [],
)
const elegida = $derived(elegidaId > 0 && store.modelo ? store.modelo.porId(elegidaId) : null)

/* Config pedida desde Ajustes o desde una tarjeta guardada. */
$effect(() => {
  const pendiente = store.arbolPendiente
  if (pendiente === null) return
  if (pendiente !== pendienteProcesado) {
    pendienteProcesado = pendiente
    elegidaId = pendiente.hijoId
    filtro = ''
    override = pendiente.seleccion
    recarga += 1
  }
  store.consumirArbolPendiente()
})
</script>

<svelte:head>
  <title>{i18n.t('tab_corredora')} · Uma Afinidad</title>
</svelte:head>

{#if store.modelo === null}
  <div class="centrado">{i18n.t('calculando')}</div>
{:else}
  <div class="pantalla">
    <HeaderBar titulo={i18n.t('tab_corredora')} pillTexto={i18n.t('herencia_contador', conteoSeleccion)}>
      {#snippet chip()}
        {#if elegida}
          <span class="chip-nino">
            <Avatar id={elegidaId} nombre={displayName(elegida, i18n.japones)} tamano={22} />
            <span>{displayName(elegida, i18n.japones)}</span>
          </span>
        {/if}
      {/snippet}
    </HeaderBar>

    <div class="corredora-scroll">
      <div class="buscador">
        <div class="buscador-campo redondeado">
          <Icono nombre="buscar" class="buscador-icono" />
          <input type="search" placeholder={i18n.t('buscar_corredora')} bind:value={filtro} />
          {#if filtro !== ''}
            <button
              type="button"
              class="icon-button"
              onclick={() => {
                filtro = ''
                elegidaId = -1
              }}
              aria-label={i18n.t('limpiar_todo')}
            >
              <Icono nombre="cerrar" />
            </button>
          {/if}
        </div>

        {#if sugerencias.length > 0}
          <ul class="sugerencias">
            {#each sugerencias as c (c.charId)}
              {@const principal = displayName(c, i18n.japones)}
              {@const secundario = i18n.japones ? (c.enName ?? '') : (c.jpName ?? '')}
              <li>
                <button
                  type="button"
                  class="sugerencia"
                  onclick={() => {
                    elegidaId = c.charId
                    filtro = ''
                  }}
                >
                  <Avatar id={c.charId} nombre={principal} tamano={32} />
                  <span class="sugerencia-texto">
                    <span class="sugerencia-principal">{principal}</span>
                    {#if secundario !== ''}
                      <span class="sugerencia-secundaria">{secundario}</span>
                    {/if}
                  </span>
                </button>
              </li>
            {/each}
          </ul>
        {/if}
      </div>

      {#if elegidaId <= 0}
        <p class="nota">{i18n.t('corredora_hint')}</p>
      {:else}
        {#key `${elegidaId}-${recarga}`}
          <DetalleCorredora
            modelo={store.modelo}
            hijoId={elegidaId}
            {override}
            onRecargarCon={(seleccion) => {
              override = seleccion
              recarga += 1
            }}
            onConteo={(conteo) => (conteoSeleccion = conteo)}
          />
        {/key}
      {/if}
    </div>
  </div>
{/if}
