<script lang="ts">
/* "Mis Umas": elenco propio. Solapa 1: editar roster. Solapa 2: mejores
   linajes dentro del elenco. */

import { goto } from '$app/navigation'
import { resolve } from '$app/paths'
import { displayName } from '$lib/domain/models'
import { coincideDifuso } from '$lib/domain/busqueda'
import { store } from '$lib/state/store.svelte'
import { crearI18n } from '$lib/i18n'
import { pedirTopLinajesDeElenco } from '$lib/worker/cliente'
import { Async } from '$lib/state/async.svelte'
import Avatar from '$lib/components/Avatar.svelte'
import HeaderBar from '$lib/components/HeaderBar.svelte'
import CardFilaTop from '$lib/components/CardFilaTop.svelte'
import Dialogo from '$lib/components/Dialogo.svelte'
import Icono from '$lib/components/Icono.svelte'

const i18n = $derived(crearI18n(store.idioma))
let tab = $state(0)

let filtro = $state('')
let confirmarLimpiar = $state(false)

const jugables = $derived(
  store.modelo === null
    ? []
    : store.modelo.personajes.filter((c) => c.playable === true && c.active === true),
)
const filtrados = $derived(
  store.modelo === null ? [] : jugables.filter((c) => coincideDifuso(c, filtro)),
)
const totalJugables = $derived(jugables.length)
const claveElenco = $derived([...store.elenco].sort((a, b) => a - b).join(','))

const linajes = new Async(() => pedirTopLinajesDeElenco([...store.elenco], 40))
$effect(() => {
  if (store.modelo !== null && tab === 1) {
    void store.elenco
    void linajes.ejecutar()
  }
})
</script>

<svelte:head>
  <title>{i18n.t('tab_elenco')} · Uma Afinidad</title>
</svelte:head>

{#if store.modelo === null}
  <div class="centrado">{i18n.t('calculando')}</div>
{:else}
  <div class="pantalla">
    <HeaderBar
      titulo={i18n.t('tab_elenco')}
      pillTexto={i18n.t('elenco_contador', store.elenco.size, totalJugables)}
    />

    <div class="tab-row" role="tablist" aria-label={i18n.t('tab_elenco')}>
      <button
        type="button"
        role="tab"
        aria-selected={tab === 0}
        class={tab === 0 ? 'tab-item activo' : 'tab-item'}
        onclick={() => (tab = 0)}
      >
        {i18n.t('elenco_tab_editar')}
      </button>
      <button
        type="button"
        role="tab"
        aria-selected={tab === 1}
        class={tab === 1 ? 'tab-item activo' : 'tab-item'}
        onclick={() => (tab = 1)}
      >
        {i18n.t('elenco_tab_linajes')}
      </button>
    </div>

    {#if tab === 0}
      <div class="elenco-editor">
        <div class="buscador">
          <div class="buscador-campo">
            <Icono nombre="buscar" class="buscador-icono" />
            <input type="search" placeholder={i18n.t('buscar')} bind:value={filtro} />
            {#if filtro !== ''}
              <button
                type="button"
                class="icon-button"
                onclick={() => (filtro = '')}
                aria-label={i18n.t('limpiar_todo')}
              >
                <Icono nombre="cerrar" />
              </button>
            {/if}
          </div>
        </div>

        <div class="elenco-acciones">
          <button
            type="button"
            class="boton-texto"
            onclick={() => store.marcarElenco(filtrados.map((c) => c.charId))}
          >
            {i18n.t('elenco_marcar_visibles')}
          </button>
          <button
            type="button"
            class="boton-texto peligro"
            onclick={() => {
              if (store.elenco.size > 0) confirmarLimpiar = true
            }}
          >
            {i18n.t('limpiar_todo')}
          </button>
        </div>

        {#if filtrados.length === 0}
          <div class="centrado">{i18n.t('sin_resultados')}</div>
        {:else}
          <ul class="grilla-tarjetas grilla-elenco">
            {#each filtrados as c (c.charId)}
              {@const nombre = displayName(c, i18n.japones)}
              {@const marcado = store.elenco.has(c.charId)}
              <li>
                <button
                  type="button"
                  role="checkbox"
                  aria-checked={marcado}
                  class={marcado ? 'card-tarjeta seleccionada' : 'card-tarjeta'}
                  onclick={() => store.toggleElenco(c.charId)}
                >
                  <span class="avatar-envoltura">
                    <Avatar id={c.charId} nombre={nombre} tamano={56} />
                    {#if marcado}
                      <span class="check" aria-hidden="true">✓</span>
                    {/if}
                  </span>
                  <span class="nombre-principal">{nombre}</span>
                </button>
              </li>
            {/each}
          </ul>
        {/if}

        <Dialogo
          open={confirmarLimpiar}
          titulo={i18n.t('elenco_limpiar_titulo')}
          descripcion={i18n.t('elenco_limpiar_mensaje')}
          onClose={() => (confirmarLimpiar = false)}
        >
          <button
            type="button"
            class="boton-texto"
            onclick={() => {
              store.limpiarElenco()
              confirmarLimpiar = false
            }}
          >
            {i18n.t('limpiar_todo')}
          </button>
          <button type="button" class="boton-texto" onclick={() => (confirmarLimpiar = false)}>
            {i18n.t('cancelar')}
          </button>
        </Dialogo>
      </div>
    {:else if linajes.datos === null}
      <div class="centrado">{i18n.t('calculando')}</div>
    {:else if linajes.datos.length === 0}
      <div class="centrado">
        <p class="nota centrada">{i18n.t('elenco_minimo')}</p>
      </div>
    {:else}
      {#key claveElenco}
        <ul class="lista-top">
          {#each linajes.datos as combo, i (`${combo.hijo.charId}-${i}`)}
            <li>
              <CardFilaTop
                {i}
                {combo}
                modelo={store.modelo}
                onVerHerencia={() => {
                  store.cargarLinaje(combo)
                  void goto(resolve('/compat'))
                }}
              />
            </li>
          {/each}
        </ul>
      {/key}
    {/if}
  </div>
{/if}
